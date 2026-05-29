package com.example.waynixgoapp.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.example.waynixgoapp.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Helper for linking a Google account to an already-signed-in Firebase user
 * (the user signed in via phone OTP first).
 *
 * Strategy:
 *   1. User completes phone OTP → FirebaseAuth.currentUser is set.
 *   2. We ask Credential Manager for a Google ID token.
 *   3. We call FirebaseUser.linkWithCredential(googleCredential).
 *      → links Google as a second provider on the same UID.
 *
 * If the Google account is already attached to a different Firebase user
 * (different phone), Firebase throws FirebaseAuthUserCollisionException; we
 * surface a "this Google is already linked to another phone" error and do NOT
 * sign the user out.
 *
 * Phone remains the primary identifier; Google is a 2-factor / recovery
 * additional provider.
 */
object GoogleLinker {

    sealed class Result {
        data class Success(val email: String) : Result()
        object NoCurrentUser : Result()
        object AlreadyLinkedToOtherUser : Result()
        object Cancelled : Result()
        data class Error(val cause: Throwable) : Result()
    }

    suspend fun linkGoogleToCurrentUser(context: Context): Result {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
            ?: return Result.NoCurrentUser

        // Already linked? Don't re-prompt.
        firebaseUser.providerData
            .firstOrNull { it.providerId == GoogleAuthProvider.PROVIDER_ID }
            ?.let { return Result.Success(it.email ?: "") }

        val webClientId = context.getString(R.string.default_web_client_id)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(context)

        val response = try {
            credentialManager.getCredential(context, request)
        } catch (e: GetCredentialException) {
            return Result.Cancelled
        } catch (e: Exception) {
            return Result.Error(e)
        }

        val cred = response.credential
        val idToken = if (cred is CustomCredential &&
            cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {
                GoogleIdTokenCredential.createFrom(cred.data).idToken
            } catch (e: GoogleIdTokenParsingException) {
                return Result.Error(e)
            }
        } else {
            return Result.Error(IllegalStateException("Unexpected credential type"))
        }

        val firebaseCred = GoogleAuthProvider.getCredential(idToken, null)

        return try {
            val linkResult = firebaseUser.linkWithCredential(firebaseCred).await()
            val email = linkResult.user
                ?.providerData
                ?.firstOrNull { it.providerId == GoogleAuthProvider.PROVIDER_ID }
                ?.email
                ?: ""
            Result.Success(email)
        } catch (e: FirebaseAuthUserCollisionException) {
            // The Google account is already linked to a different Firebase
            // user (different phone number). Do NOT sign-out the phone user.
            Result.AlreadyLinkedToOtherUser
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
