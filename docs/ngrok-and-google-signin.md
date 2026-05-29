# Backend over ngrok + Google Sign-In linking — setup guide

This guide covers the two pieces of plumbing that Waynix GO needs during
development:

1. Exposing the local Django backend over **ngrok** so a real Android phone
   (not just an emulator) can reach it.
2. Enabling **Google Sign-In as a second provider** linked to an existing
   phone-OTP account (2FA-style; phone remains the primary identifier).

---

## 1. ngrok tunnel for the Django backend

### Why

The default `BASE_URL` used to be a hardcoded LAN IP
(`http://192.168.1.15:8000/`). That only works for devices on the same Wi-Fi.
ngrok gives you a public HTTPS URL that forwards to your local Django.

### Install ngrok

```bash
# Linux:
curl -sSL https://ngrok-agent.s3.amazonaws.com/ngrok.asc \
  | sudo tee /etc/apt/trusted.gpg.d/ngrok.asc >/dev/null
echo "deb https://ngrok-agent.s3.amazonaws.com buster main" \
  | sudo tee /etc/apt/sources.list.d/ngrok.list
sudo apt update && sudo apt install ngrok
```

(Or download from <https://ngrok.com/download>.)

Sign up at <https://dashboard.ngrok.com>, copy the auth token, then:

```bash
ngrok config add-authtoken <YOUR_TOKEN>
```

### Run Django + ngrok

```bash
# Terminal 1 — Django
cd taxi_backend
python manage.py runserver 0.0.0.0:8000

# Terminal 2 — ngrok
ngrok http 8000
```

ngrok will print something like:

```
Forwarding   https://5f3c-94-158-12-34.ngrok-free.app -> http://localhost:8000
```

Copy that URL **with the trailing slash**.

### Tell Django to accept that host

Add the ngrok host to `ALLOWED_HOSTS` in
[`taxi_backend/config/settings.py`](../taxi_backend/config/settings.py:13).
For dev it is already set to `['*']`, so this is fine. For prod it should be
the explicit domain.

### Tell the Android app where to find the backend

The base URL is now read from `gradle.properties`. Set it once:

```properties
# WaynixGoApp/gradle.properties
WAYNIX_API_BASE_URL=https://5f3c-94-158-12-34.ngrok-free.app/
```

…or pass it on the command line per-build (does not require editing the file):

```bash
./gradlew :app:assembleDebug -PWAYNIX_API_BASE_URL=https://5f3c-94-158-12-34.ngrok-free.app/
```

### Known gotchas

- **ngrok free tier shows an HTML interstitial** the first time a non-browser
  client hits it. Waynix bypasses this by sending the
  `ngrok-skip-browser-warning: true` header on every Retrofit call (configured
  in [`ApiService.kt`](../WaynixGoApp/app/src/main/java/com/example/waynixgoapp/data/network/ApiService.kt:90)).
  Do **not** remove that interceptor while developing on ngrok.
- Free ngrok URLs change every restart. Update `WAYNIX_API_BASE_URL` and
  rebuild the app each time.
- ngrok URLs are HTTPS; the app no longer needs `usesCleartextTraffic` for
  ngrok-based testing. Keep it enabled only if you also test against
  `http://10.0.2.2:8000/` from the emulator.

---

## 2. Google Sign-In linked to phone account (2FA-style)

### Concept

The auth flow is:

```
Onboarding → Phone entry → SMS OTP → [Google linking] → Name → Done
```

After the user signs in via Firebase **Phone Auth**, we ask them to **link** a
Google account to that same Firebase user via
[`FirebaseUser.linkWithCredential`](../WaynixGoApp/app/src/main/java/com/example/waynixgoapp/auth/GoogleLinker.kt:1).
The Google account becomes a **secondary provider** on the same UID. Phone
remains primary; Google is the second factor / recovery option.

If the chosen Google account is already linked to a different phone-Firebase
user, Firebase throws `FirebaseAuthUserCollisionException`. We surface a
"this Google is already linked to another phone" error to the user without
signing them out.

The user can also tap "Skip for now" — phone-only auth still works, and the
link can be done later from the profile (TODO).

### Firebase Console setup

1. Open <https://console.firebase.google.com/> → project **waynixgo**.
2. **Authentication → Sign-in method**:
   - Enable **Phone** (already done).
   - Enable **Google** → fill in Project support email → Save.
3. **Project settings → General → Your apps → Android app** → make sure your
   debug & release SHA-1 / SHA-256 fingerprints are added. Without these,
   Google Sign-In returns `DEVELOPER_ERROR` / `10:`.

   ```bash
   # Debug keystore (the one Android Studio creates automatically):
   keytool -list -v \
     -keystore ~/.android/debug.keystore \
     -alias androiddebugkey -storepass android -keypass android
   ```

   Copy the **SHA1** and **SHA-256** lines into the Firebase console, click
   **Save**, then **Download google-services.json** again and replace
   [`WaynixGoApp/app/google-services.json`](../WaynixGoApp/app/google-services.json:1).
4. In the freshly downloaded `google-services.json` find the entry under
   `oauth_client` whose `"client_type": 3` — that is the **Web client ID**
   for the project. Copy its `client_id` value.
5. Paste it into
   [`strings.xml`](../WaynixGoApp/app/src/main/res/values/strings.xml:14)
   as `default_web_client_id`. Without this the Credential Manager call will
   fail with "the user did not select an account" or
   "no credentials available."

### Gradle dependencies

Already added in
[`app/build.gradle.kts`](../WaynixGoApp/app/build.gradle.kts:42):

```kotlin
implementation("androidx.credentials:credentials:1.3.0")
implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
```

These give us the modern Credential Manager API (replacement for the deprecated
`GoogleSignIn` client) plus `Task.await()` so the linker is suspend-friendly.

### Code map

| File | Role |
| --- | --- |
| [`auth/GoogleLinker.kt`](../WaynixGoApp/app/src/main/java/com/example/waynixgoapp/auth/GoogleLinker.kt:1) | Suspend helper. Uses Credential Manager → Google ID token → `linkWithCredential`. |
| [`AuthScreens.kt` — `GoogleLinkScreen`](../WaynixGoApp/app/src/main/java/com/example/waynixgoapp/AuthScreens.kt:1) | UI for the linking step with a "Continue with Google" CTA and a "Skip" button. |
| [`AuthScreens.kt` — `AuthRoot`](../WaynixGoApp/app/src/main/java/com/example/waynixgoapp/AuthScreens.kt:1) | State machine: OTP success → `GoogleLink` → `NameEntry`. |
| [`data/UserPreferences.kt`](../WaynixGoApp/app/src/main/java/com/example/waynixgoapp/data/UserPreferences.kt:1) | Persists `googleEmail` alongside phone/name. |

### Testing

1. Start the backend over ngrok (above).
2. Run the app on a device with at least one Google account configured.
3. Phone entry → OTP → after success the **Google link** screen appears.
4. Tap **Continue with Google** → pick an account → returns to **Name** screen.
5. Open Firebase Console → Authentication → find your user. Under
   "Provider data" you should see **two providers**: `phone` and `google.com`.
6. Skip path: tap "Skip for now" → user proceeds without Google. Provider data
   shows only `phone`.

### Troubleshooting

| Symptom | Likely cause |
| --- | --- |
| `[16] No credentials available` from Credential Manager | `default_web_client_id` placeholder still set, or no Google account on device. |
| `FirebaseAuthUserCollisionException` | The chosen Google is already linked to a different phone account. Expected — show the "already linked" message. |
| `DEVELOPER_ERROR` / `Status{statusCode=10}` | SHA-1 not added to Firebase, or `google-services.json` not refreshed after adding it. |
| Linking always fails silently with cancelled | User cancelled the sheet, or device has no Google services. |
| Backend health check fails after switching to ngrok | Forgot to update `WAYNIX_API_BASE_URL`, or ngrok URL rotated since last build. |
