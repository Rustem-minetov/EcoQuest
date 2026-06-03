/* ════════════════════════════════════════════════════
   EcoQuest — app.js
   Vanilla JS, Leaflet for real OSM map.
   ════════════════════════════════════════════════════ */
(() => {
  'use strict';

  // ── State ──────────────────────────────────────────
  const state = {
    currentCity: 'Ташкент',
    xp: 1240,
    cleans: 23,
    activeTab: 'map',
    mapInited: false,
    map: null,
    markersLayer: null,
    sheetTarget: null,
    activeFilter: 'all',
    sheetOpen: false,
    geoCoords: null,        // {lat, lng}
    geoWatchId: null,       // watchPosition id
    selfieBlob: null,       // captured selfie image blob
    videoBlob: null,        // recorded video blob
    cameraStream: null,     // active MediaStream for selfie camera
    cameraFacing: 'user',   // 'user' (front) or 'environment' (back)
    videoStream: null,      // active MediaStream for video recording
    mediaRecorder: null,    // MediaRecorder instance
    videoChunks: [],        // recorded video chunks
    videoTimerInterval: null,
    videoSeconds: 0
  };
  let sheetCloseTimer = null;

  // ── City data (real coords) ────────────────────────
  // Each spot: lat, lng, name, status, xp, urgent, tags
  const CITIES = {
    'Ташкент': {
      center: [41.3111, 69.2797],
      zoom: 12,
      spots: [
        { id: 't1', name: 'Чиланзар: рынок',        lat: 41.2829, lng: 69.2065, status: 'red',   xp: 150, urgent: true,  desc: 'Большое скопление мусора возле рынка Чиланзар.', icon: 'flame', dist: 1.4 },
        { id: 't2', name: 'Сергели: парк',          lat: 41.2280, lng: 69.2185, status: 'amber', xp: 90,  urgent: false, desc: 'Очистить территорию у центрального парка.', icon: 'trees', dist: 1.2 },
        { id: 't3', name: 'Юнусабад: школа №34',     lat: 41.3650, lng: 69.2870, status: 'red',   xp: 120, urgent: false, desc: 'Уборка территории у школы по ул. Амира Темура.', icon: 'school', dist: 2.8 },
        { id: 't4', name: 'Бектемир: жилой двор',   lat: 41.2270, lng: 69.3850, status: 'red',   xp: 130, urgent: true,  desc: 'Двор по ул. Нурафшон, нужна срочная помощь.', icon: 'trash', dist: 5.2 },
        { id: 't5', name: 'М. Улугбек: сквер',      lat: 41.3370, lng: 69.3380, status: 'green', xp: 60,  urgent: false, desc: 'Поддержка чистоты в зелёной зоне.', icon: 'leaf', dist: 3.6 },
        { id: 't6', name: 'Алмазар: набережная',    lat: 41.3450, lng: 69.2210, status: 'amber', xp: 110, urgent: false, desc: 'Уборка вдоль канала Анхор.', icon: 'wave', dist: 2.1 },
        { id: 't7', name: 'Яшнабад: рынок Куйлюк',  lat: 41.2885, lng: 69.3360, status: 'red',   xp: 170, urgent: true,  desc: 'Высокая нагрузка возле рынка Куйлюк.', icon: 'flame', dist: 4.0 }
      ]
    },
    'Самарканд': {
      center: [39.6542, 66.9750],
      zoom: 13,
      spots: [
        { id: 's1', name: 'Регистан: сквер',        lat: 39.6550, lng: 66.9750, status: 'green', xp: 70,  urgent: false, desc: 'Поддержка туристической зоны.', icon: 'leaf',  dist: 0.4 },
        { id: 's2', name: 'Сиёб базар',             lat: 39.6664, lng: 66.9760, status: 'red',   xp: 160, urgent: true,  desc: 'Сильное загрязнение возле рынка.', icon: 'flame', dist: 1.6 },
        { id: 's3', name: 'Багишамал: парк',        lat: 39.6730, lng: 66.9590, status: 'amber', xp: 110, urgent: false, desc: 'Зелёная зона требует уборки.', icon: 'trees', dist: 2.4 },
        { id: 's4', name: 'Темирйул: вокзал',       lat: 39.6260, lng: 66.9760, status: 'red',   xp: 140, urgent: false, desc: 'Окрестности железнодорожного вокзала.', icon: 'trash', dist: 3.1 },
        { id: 's5', name: 'Пастдаргом',             lat: 39.6020, lng: 66.9300, status: 'amber', xp: 100, urgent: false, desc: 'Жилые дворы района.', icon: 'school', dist: 5.5 }
      ]
    },
    'Нукус': {
      center: [42.4530, 59.6100],
      zoom: 13,
      spots: [
        { id: 'n1', name: 'Центр: парк Бердаха',    lat: 42.4612, lng: 59.6135, status: 'red',   xp: 180, urgent: true,  desc: 'Центральный парк требует срочной уборки.', icon: 'flame', dist: 0.8 },
        { id: 'n2', name: 'Север: Туркистанская',   lat: 42.4790, lng: 59.6020, status: 'amber', xp: 130, urgent: false, desc: 'Уборка вдоль улицы.', icon: 'trash', dist: 2.0 },
        { id: 'n3', name: 'Запад: рынок',           lat: 42.4520, lng: 59.5800, status: 'red',   xp: 200, urgent: true,  desc: 'Сильное загрязнение у рынка.', icon: 'flame', dist: 2.6 },
        { id: 'n4', name: 'Юг: набережная',         lat: 42.4380, lng: 59.6230, status: 'green', xp: 80,  urgent: false, desc: 'Поддержка чистоты у Амударьи.', icon: 'wave', dist: 3.4 },
        { id: 'n5', name: 'Юго-Восток: школьная',   lat: 42.4400, lng: 59.6450, status: 'amber', xp: 120, urgent: false, desc: 'Зона у школы.', icon: 'school', dist: 4.1 }
      ]
    }
  };

  const STATUS_META = {
    red:   { color: '#FF4D4D', label: '🔴 Загрязнено', cls: 'r' },
    amber: { color: '#FFA500', label: '🟠 В процессе', cls: 'a' },
    green: { color: '#1DB87A', label: '🟢 Чисто',       cls: 'g' }
  };

  const ICON_MAP = {
    flame:  'ti-flame',
    trees:  'ti-trees',
    school: 'ti-school',
    trash:  'ti-trash',
    leaf:   'ti-leaf',
    wave:   'ti-wave-saw-tool',
    star:   'ti-star'
  };

  // ── DOM helpers ────────────────────────────────────
  const $ = (sel, root = document) => root.querySelector(sel);
  const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));

  function fmtNum(n) {
    return n.toLocaleString('ru-RU').replace(/,/g, ' ');
  }

  // ── Toast ──────────────────────────────────────────
  let toastTimer;
  function showToast(msg) {
    const el = $('#toast');
    $('#toast-msg').textContent = msg;
    el.classList.add('show');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.classList.remove('show'), 2600);
  }

  // ── Tabs / Auth ────────────────────────────────────
  function goTab(id) {
    state.activeTab = id;

    // Close any open Leaflet popups when switching tabs
    if (state.map) {
      state.map.eachLayer(l => { if (l.closePopup) l.closePopup(); });
      state.map.closePopup();
    }

    // Close city dropdown and sheet if open
    closeCityDropdown();
    if (state.sheetOpen) closeSheet();

    $$('.screen').forEach(s => s.classList.remove('active'));
    $$('.nav-btn').forEach(b => b.classList.toggle('active', b.dataset.tab === id));
    const target = $('#screen-' + id);
    if (target) target.classList.add('active');

    if (id === 'map') {
      // map already rendered; just invalidate size in case of layout change
      requestAnimationFrame(() => {
        if (state.map) state.map.invalidateSize();
      });
    }
  }

  function switchAuthTab(name) {
    $$('.auth-tab').forEach(t => t.classList.toggle('active', t.dataset.tab === name));
    $('#auth-reg').hidden = name !== 'reg';
    $('#auth-login').hidden = name !== 'login';
  }

  function selectRegCity(name) {
    state.currentCity = name;
    $$('#reg-city-pills .city-pill').forEach(p => {
      p.classList.toggle('selected', p.dataset.city === name);
    });
  }

  function login() {
    $('#screen-auth').classList.remove('active');
    $('#screen-map').classList.add('active');
    $('#bottomNav').hidden = false;
    $$('.nav-btn').forEach(b => b.classList.toggle('active', b.dataset.tab === 'map'));
    state.activeTab = 'map';

    // Init map now that container is visible
    if (!state.mapInited) initMap();

    // Wait for screen transition, then size map properly
    requestAnimationFrame(() => {
      if (state.map) state.map.invalidateSize();
      setCity(state.currentCity);
      // Double-check after a short delay (some browsers need extra time)
      setTimeout(() => {
        if (state.map) state.map.invalidateSize();
      }, 300);
    });
  }


  function logout() {
    $$('.screen').forEach(s => s.classList.remove('active'));
    $('#screen-auth').classList.add('active');
    $('#bottomNav').hidden = true;
  }

  // ── Map / Leaflet ──────────────────────────────────
  function initMap() {
    state.map = L.map('leafletMap', {
      zoomControl: true,
      attributionControl: true,
      scrollWheelZoom: true,
      worldCopyJump: true
    }).setView(CITIES['Ташкент'].center, CITIES['Ташкент'].zoom);

    L.control.zoom({ position: 'topright' }).addTo(state.map);

    // CartoDB Voyager tiles — colorful and clean, free for use with attribution
    L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
      maxZoom: 19,
      subdomains: 'abcd'
    }).addTo(state.map);

    state.markersLayer = L.layerGroup().addTo(state.map);
    state.mapInited = true;
  }

  function makeMarker(spot) {
    const meta = STATUS_META[spot.status];
    const html = `
      <div class="eq-pin ${spot.urgent ? 'urgent' : ''}">
        <svg viewBox="0 0 36 44" xmlns="http://www.w3.org/2000/svg">
          <path d="M18 2C9.7 2 3 8.7 3 17c0 11 15 25 15 25s15-14 15-25c0-8.3-6.7-15-15-15z"
                fill="${meta.color}" stroke="#fff" stroke-width="2.5"/>
        </svg>
        <span class="dot" style="color:${meta.color}">●</span>
      </div>`;

    const icon = L.divIcon({
      className: 'eq-marker',
      html,
      iconSize: [36, 44],
      iconAnchor: [18, 42],
      popupAnchor: [0, -38]
    });

    const popupHtml = `
      <div class="eq-popup">
        <div class="eq-popup-title">${spot.name}</div>
        <div class="eq-popup-status" style="color:${meta.color}">${meta.label}</div>
        <div class="eq-popup-xp">+${spot.xp} XP</div>
        <button class="eq-popup-btn" data-spot="${spot.id}">
          <i class="ti ti-camera"></i> Начать уборку
        </button>
      </div>`;

    const m = L.marker([spot.lat, spot.lng], { icon }).bindPopup(popupHtml, {
      closeButton: true,
      autoPan: true,
      maxWidth: 240
    });

    m.on('popupopen', (e) => {
      const node = e.popup.getElement();
      const btn = node && node.querySelector('.eq-popup-btn');
      if (btn) {
        const handler = () => {
          state.map.closePopup();
          openSheet(spot);
        };
        btn.addEventListener('click', handler);
        // Clean up listener when popup closes to prevent memory leaks / stale handlers
        m.once('popupclose', () => {
          btn.removeEventListener('click', handler);
        });
      }
    });

    return m;
  }

  function setCity(name) {
    if (!CITIES[name]) return;
    state.currentCity = name;
    const city = CITIES[name];

    // Update headers
    $('#current-city-name').textContent = name;
    const lbCity = $('#lb-city-name'); if (lbCity) lbCity.textContent = name;
    const profCityName = $('#profile-city-line span'); if (profCityName) profCityName.textContent = name;
    $$('.city-opt').forEach(o => o.classList.toggle('active', o.dataset.city === name));
    closeCityDropdown();

    // Map
    if (state.map) {
      state.markersLayer.clearLayers();
      city.spots.forEach(s => makeMarker(s).addTo(state.markersLayer));
      state.map.flyTo(city.center, city.zoom, { duration: 0.6 });
    }

    // Tasks list
    renderTasks();
  }

  // ── City dropdown ──────────────────────────────────
  function toggleCityDropdown() {
    const wrap = $('#citySelector');
    const open = wrap.classList.toggle('open');
    $('#cityToggle').setAttribute('aria-expanded', String(open));
  }
  function closeCityDropdown() {
    const wrap = $('#citySelector');
    if (!wrap) return;
    wrap.classList.remove('open');
    $('#cityToggle').setAttribute('aria-expanded', 'false');
  }

  // ── Tasks list ─────────────────────────────────────
  function renderTasks() {
    const list = $('#tasksList');
    const city = CITIES[state.currentCity];
    const spots = city.spots.slice();

    let filtered = spots;
    switch (state.activeFilter) {
      case 'urgent':    filtered = spots.filter(s => s.urgent); break;
      case 'near':      filtered = spots.slice().sort((a, b) => a.dist - b.dist); break;
      case 'top':       filtered = spots.slice().sort((a, b) => b.xp - a.xp); break;
      case 'challenge': filtered = []; break; // shown via synthetic challenge below
      default: filtered = spots;
    }

    let html = '';

    if (state.activeFilter === 'all' || state.activeFilter === 'challenge') {
      // Synthetic weekly challenge card on top
      html += `
        <button class="task-item" data-challenge="weekly">
          <div class="task-ico b"><i class="ti ti-star"></i></div>
          <div class="task-content">
            <div class="task-title">Еженедельный челлендж</div>
            <div class="task-desc">Убери в 3 разных районах за 7 дней — получи бонус.</div>
            <div class="task-meta">
              <span class="xp-badge-sm">+400 XP</span>
              <span class="dist-tag"><i class="ti ti-clock" style="font-size:13px"></i> 5 дней</span>
            </div>
          </div>
        </button>`;
    }

    if (state.activeFilter !== 'challenge') {
      const urgentCount = spots.filter(s => s.urgent).length;
      $('.tasks-header p').textContent = `${urgentCount} срочных задания · ${state.currentCity}`;

      if (!filtered.length) {
        html += `<div class="task-empty">Нет заданий по этому фильтру.</div>`;
      } else {
        filtered.forEach(s => {
          const meta = STATUS_META[s.status];
          const tIcon = ICON_MAP[s.icon] || 'ti-trash';
          html += `
            <button class="task-item" data-spot="${s.id}">
              <div class="task-ico ${meta.cls}"><i class="ti ${tIcon}"></i></div>
              <div class="task-content">
                <div class="task-title">${s.name}</div>
                <div class="task-desc">${s.desc}</div>
                <div class="task-meta">
                  <span class="xp-badge-sm">+${s.xp} XP</span>
                  ${s.urgent
                    ? `<span class="urgent-tag">🔴 Срочно</span>`
                    : `<span class="dist-tag"><i class="ti ti-map-pin" style="font-size:13px"></i> ${s.dist} км</span>`}
                </div>
              </div>
            </button>`;
        });
      }
    }

    list.innerHTML = html;
  }

  function setFilter(name) {
    state.activeFilter = name;
    $$('.filter-chip').forEach(c => c.classList.toggle('active', c.dataset.filter === name));
    renderTasks();
  }

  // ── Leaderboard list ───────────────────────────────
  const LB_ROWS = [
    { rank: 4, name: 'Бехзод Р.',  initials: 'БР', score: 2455, bg: '#EEEDFE', fg: '#26215C' },
    { rank: 5, name: 'Зулайхо М.', initials: 'ЗМ', score: 2010, bg: '#E1F5EE', fg: '#04342C' },
    { rank: 6, name: 'Отабек Т.',  initials: 'ОТ', score: 1880, bg: '#F1EFE8', fg: '#2C2C2A' },
    { rank: 7, name: 'Азиз К. (Вы)', initials: 'АК', score: 1240, bg: 'var(--green-mid)', fg: 'var(--green-dark)', me: true },
    { rank: 8, name: 'Шоира Б.',   initials: 'ШБ', score: 1105, bg: '#FAECE7', fg: '#4A1B0C' },
    { rank: 9, name: 'Фаррух А.',  initials: 'ФА', score: 980,  bg: '#E6F1FB', fg: '#042C53' }
  ];

  function renderLeaderboard() {
    const list = $('#lbList');
    list.innerHTML = LB_ROWS.map(r => {
      const meRow = r.me ? ' me' : '';
      return `
        <div class="lb-row${meRow}">
          <div class="lb-row-rank">${r.rank}</div>
          <div class="lb-row-av" style="background:${r.bg};color:${r.fg}">${r.initials}</div>
          <div class="lb-row-name">${r.name}</div>
          <div class="lb-row-score">${fmtNum(r.score)}</div>
        </div>`;
    }).join('');
    $('#lb-me-xp').textContent = fmtNum(state.xp);
    $('#lb-me-xp-text').textContent = fmtNum(state.xp);
  }

  // ── Cleanup sheet ──────────────────────────────────
  function openSheet(spot = null) {
    // Prevent double-open
    if (state.sheetOpen) return;

    // Clear any pending close timeout (race condition guard)
    if (sheetCloseTimer) {
      clearTimeout(sheetCloseTimer);
      sheetCloseTimer = null;
    }

    state.sheetOpen = true;
    state.sheetTarget = spot;

    // Close any open Leaflet popups
    if (state.map) state.map.closePopup();

    const bg = $('#sheetBg');
    bg.hidden = false;
    // force reflow so transition runs
    void bg.offsetWidth;
    bg.classList.add('open');
    document.body.dataset.locked = 'true';

    if (spot) {
      $('#sheetTitle').textContent = `Уборка: ${spot.name}`;
      $('#sheetSub').textContent = `Выполни все шаги для подтверждения уборки. Награда +${spot.xp} XP.`;
    } else {
      $('#sheetTitle').textContent = 'Подтверди уборку';
      $('#sheetSub').textContent = 'Выполни все шаги для подтверждения';
    }

    // Reset verification state
    resetVerification();
    // Auto-start geolocation
    requestGeolocation();
  }

  function closeSheet() {
    if (!state.sheetOpen) return;

    const bg = $('#sheetBg');
    bg.classList.remove('open');
    state.sheetOpen = false;
    document.body.dataset.locked = 'false';

    // Stop camera if open
    stopCameraStream();
    stopVideoRecording();

    // Clear previous timer before setting new one
    if (sheetCloseTimer) clearTimeout(sheetCloseTimer);
    sheetCloseTimer = setTimeout(() => {
      // Only hide if still closed (guards against rapid reopen)
      if (!state.sheetOpen) {
        bg.hidden = true;
      }
      sheetCloseTimer = null;
    }, 300);
  }

  function submitClean() {
    // Enforce required steps
    if (!state.geoCoords) {
      showToast('Геолокация не определена');
      return;
    }
    if (!state.selfieBlob) {
      showToast('Сделайте селфи для верификации');
      return;
    }

    const earned = state.sheetTarget ? state.sheetTarget.xp : 120;
    state.xp += earned;
    state.cleans += 1;

    $('#xp-amount').textContent = fmtNum(state.xp);
    $('#ps-xp').textContent = fmtNum(state.xp);
    $('#ps-cleans').textContent = fmtNum(state.cleans);
    $('#lb-me-xp').textContent = fmtNum(state.xp);
    $('#lb-me-xp-text').textContent = fmtNum(state.xp);

    closeSheet();
    showToast(`+${earned} XP · уборка отправлена на проверку`);
  }

  // ── Verification helpers ───────────────────────────
  function resetVerification() {
    state.geoCoords = null;
    state.selfieBlob = null;
    state.videoBlob = null;
    state.videoChunks = [];
    state.videoSeconds = 0;

    // Reset geo UI
    $('#geoStatus').hidden = false;
    $('#geoCoords').hidden = true;
    $('#geoError').hidden = true;

    // Reset selfie UI
    $('#selfiePreview').hidden = true;
    $('#selfieBtn').hidden = false;
    $('#selfieImg').src = '';

    // Reset video UI
    $('#videoPreview').hidden = true;
    $('#videoRecordArea').hidden = false;
    $('#videoTimer').hidden = true;
    $('#videoBtn').hidden = false;
    const playback = $('#videoPlayback');
    playback.src = '';
    $('#videoTime').textContent = '00:00';

    // Reset checklist
    updateVerifyChecklist();
    updateSubmitButton();
  }

  function updateVerifyChecklist() {
    const geoRow = $('#vr-geo');
    const selfieRow = $('#vr-selfie');
    const videoRow = $('#vr-video');

    // Geo
    if (state.geoCoords) {
      geoRow.classList.add('done');
      geoRow.querySelector('i').className = 'ti ti-circle-check';
    } else {
      geoRow.classList.remove('done');
      geoRow.querySelector('i').className = 'ti ti-circle';
    }

    // Selfie
    if (state.selfieBlob) {
      selfieRow.classList.add('done');
      selfieRow.querySelector('i').className = 'ti ti-circle-check';
    } else {
      selfieRow.classList.remove('done');
      selfieRow.querySelector('i').className = 'ti ti-circle';
    }

    // Video (optional)
    if (state.videoBlob) {
      videoRow.classList.add('done');
      videoRow.querySelector('i').className = 'ti ti-circle-check';
    } else {
      videoRow.classList.remove('done');
      videoRow.querySelector('i').className = 'ti ti-circle-dashed';
    }
  }

  function updateSubmitButton() {
    const btn = $('#submitClean');
    btn.disabled = !(state.geoCoords && state.selfieBlob);
  }

  // ── Geolocation ────────────────────────────────────
  function requestGeolocation() {
    if (!navigator.geolocation) {
      showGeoError();
      return;
    }

    $('#geoStatus').hidden = false;
    $('#geoCoords').hidden = true;
    $('#geoError').hidden = true;

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        state.geoCoords = {
          lat: pos.coords.latitude,
          lng: pos.coords.longitude
        };
        $('#geoStatus').hidden = true;
        $('#geoCoords').hidden = false;
        $('#geoLat').textContent = `Широта: ${state.geoCoords.lat.toFixed(6)}`;
        $('#geoLng').textContent = `Долгота: ${state.geoCoords.lng.toFixed(6)}`;
        updateVerifyChecklist();
        updateSubmitButton();
      },
      (err) => {
        console.warn('Geolocation error:', err);
        showGeoError();
      },
      { enableHighAccuracy: true, timeout: 15000, maximumAge: 0 }
    );
  }

  function showGeoError() {
    $('#geoStatus').hidden = true;
    $('#geoCoords').hidden = true;
    $('#geoError').hidden = false;
    state.geoCoords = null;
    updateVerifyChecklist();
    updateSubmitButton();
  }

  // ── Selfie camera ──────────────────────────────────
  async function openSelfieCamera() {
    try {
      // Try to open front camera
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: state.cameraFacing, width: { ideal: 640 }, height: { ideal: 640 } },
        audio: false
      });
      state.cameraStream = stream;

      const modal = $('#cameraModal');
      const video = $('#cameraStream');
      video.srcObject = stream;
      modal.hidden = false;
    } catch (err) {
      console.warn('Camera error:', err);
      // Fallback to file input
      $('#selfieInput').click();
    }
  }

  function stopCameraStream() {
    if (state.cameraStream) {
      state.cameraStream.getTracks().forEach(t => t.stop());
      state.cameraStream = null;
    }
    const modal = $('#cameraModal');
    if (modal) modal.hidden = true;
  }

  async function switchCamera() {
    state.cameraFacing = state.cameraFacing === 'user' ? 'environment' : 'user';
    if (state.cameraStream) {
      state.cameraStream.getTracks().forEach(t => t.stop());
    }
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: state.cameraFacing, width: { ideal: 640 }, height: { ideal: 640 } },
        audio: false
      });
      state.cameraStream = stream;
      $('#cameraStream').srcObject = stream;
    } catch (err) {
      console.warn('Switch camera error:', err);
    }
  }

  function captureSelfie() {
    const video = $('#cameraStream');
    const canvas = $('#cameraCanvas');
    canvas.width = video.videoWidth || 640;
    canvas.height = video.videoHeight || 640;
    const ctx = canvas.getContext('2d');
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

    canvas.toBlob((blob) => {
      if (!blob) return;
      state.selfieBlob = blob;
      const url = URL.createObjectURL(blob);
      $('#selfieImg').src = url;
      $('#selfiePreview').hidden = false;
      $('#selfieBtn').hidden = true;
      stopCameraStream();
      updateVerifyChecklist();
      updateSubmitButton();
    }, 'image/jpeg', 0.85);
  }

  function retakeSelfie() {
    state.selfieBlob = null;
    $('#selfiePreview').hidden = true;
    $('#selfieBtn').hidden = false;
    $('#selfieImg').src = '';
    updateVerifyChecklist();
    updateSubmitButton();
  }

  // Handle file input fallback for selfie
  function handleSelfieInput(e) {
    const file = e.target.files[0];
    if (!file) return;
    state.selfieBlob = file;
    const url = URL.createObjectURL(file);
    $('#selfieImg').src = url;
    $('#selfiePreview').hidden = false;
    $('#selfieBtn').hidden = true;
    updateVerifyChecklist();
    updateSubmitButton();
    e.target.value = '';
  }

  // ── Video recording ────────────────────────────────
  async function startVideoRecording() {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment', width: { ideal: 1280 }, height: { ideal: 720 } },
        audio: true
      });
      state.videoStream = stream;
      state.videoChunks = [];

      const recorder = new MediaRecorder(stream, { mimeType: 'video/webm' });
      state.mediaRecorder = recorder;

      recorder.ondataavailable = (e) => {
        if (e.data.size > 0) state.videoChunks.push(e.data);
      };

      recorder.onstop = () => {
        const blob = new Blob(state.videoChunks, { type: 'video/webm' });
        state.videoBlob = blob;
        const url = URL.createObjectURL(blob);
        const playback = $('#videoPlayback');
        playback.src = url;
        $('#videoPreview').hidden = false;
        $('#videoRecordArea').hidden = true;

        // Stop stream tracks
        if (state.videoStream) {
          state.videoStream.getTracks().forEach(t => t.stop());
          state.videoStream = null;
        }
        updateVerifyChecklist();
        updateSubmitButton();
      };

      recorder.start();
      state.videoSeconds = 0;
      updateVideoTimer();
      $('#videoTimer').hidden = false;
      $('#videoBtn').hidden = true;
      state.videoTimerInterval = setInterval(() => {
        state.videoSeconds++;
        updateVideoTimer();
      }, 1000);

    } catch (err) {
      console.warn('Video recording error:', err);
      // Fallback to file input
      $('#videoInput').click();
    }
  }

  function stopVideoRecording() {
    if (state.mediaRecorder && state.mediaRecorder.state !== 'inactive') {
      state.mediaRecorder.stop();
    }
    if (state.videoTimerInterval) {
      clearInterval(state.videoTimerInterval);
      state.videoTimerInterval = null;
    }
    $('#videoTimer').hidden = true;
  }

  function updateVideoTimer() {
    const m = Math.floor(state.videoSeconds / 60);
    const s = state.videoSeconds % 60;
    $('#videoTime').textContent = `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  }

  function retakeVideo() {
    state.videoBlob = null;
    $('#videoPreview').hidden = true;
    $('#videoRecordArea').hidden = false;
    $('#videoBtn').hidden = false;
    const playback = $('#videoPlayback');
    if (playback.src) {
      URL.revokeObjectURL(playback.src);
      playback.src = '';
    }
    updateVerifyChecklist();
    updateSubmitButton();
  }

  // Handle file input fallback for video
  function handleVideoInput(e) {
    const file = e.target.files[0];
    if (!file) return;
    state.videoBlob = file;
    const url = URL.createObjectURL(file);
    const playback = $('#videoPlayback');
    playback.src = url;
    $('#videoPreview').hidden = false;
    $('#videoRecordArea').hidden = true;
    updateVerifyChecklist();
    updateSubmitButton();
    e.target.value = '';
  }

  // ── Wire up events ─────────────────────────────────
  function wireEvents() {
    // Bottom nav
    $$('.nav-btn').forEach(b => b.addEventListener('click', () => goTab(b.dataset.tab)));

    // Auth tabs
    $$('.auth-tab').forEach(t => t.addEventListener('click', () => switchAuthTab(t.dataset.tab)));

    // City pills (registration)
    $$('#reg-city-pills .city-pill').forEach(p => {
      p.addEventListener('click', () => selectRegCity(p.dataset.city));
    });

    // Auth forms — both submit "log in"
    $('#auth-reg').addEventListener('submit', e => { e.preventDefault(); login(); });
    $('#auth-login').addEventListener('submit', e => { e.preventDefault(); login(); });

    // City dropdown toggle
    $('#cityToggle').addEventListener('click', e => {
      e.stopPropagation();
      toggleCityDropdown();
    });
    $$('.city-opt').forEach(o => {
      o.addEventListener('click', e => {
        e.stopPropagation();
        setCity(o.dataset.city);
      });
    });
    document.addEventListener('click', e => {
      if (!e.target.closest('#citySelector')) closeCityDropdown();
    });

    // FAB clean button
    $('#fabClean').addEventListener('click', () => openSheet());

    // Filter chips
    $$('.filter-chip').forEach(c => c.addEventListener('click', () => setFilter(c.dataset.filter)));

    // Tasks list (delegate)
    $('#tasksList').addEventListener('click', e => {
      const btn = e.target.closest('.task-item');
      if (!btn) return;
      if (btn.dataset.challenge) {
        showToast('Челлендж принят. Удачи!');
        return;
      }
      const id = btn.dataset.spot;
      const spot = CITIES[state.currentCity].spots.find(s => s.id === id);
      if (spot) openSheet(spot);
    });

    // Sheet — close on overlay click (mouse + touch)
    $('#sheetClose').addEventListener('click', closeSheet);
    const sheetBgEl = $('#sheetBg');
    sheetBgEl.addEventListener('click', e => {
      if (e.target === sheetBgEl) closeSheet();
    });
    sheetBgEl.addEventListener('touchend', e => {
      if (e.target === sheetBgEl) {
        e.preventDefault();
        closeSheet();
      }
    });
    $('#submitClean').addEventListener('click', submitClean);

    // Geolocation retry
    $('#geoRetry').addEventListener('click', requestGeolocation);

    // Selfie camera
    $('#selfieBtn').addEventListener('click', openSelfieCamera);
    $('#selfieRetake').addEventListener('click', retakeSelfie);
    $('#selfieInput').addEventListener('change', handleSelfieInput);

    // Camera modal controls
    $('#cameraSnap').addEventListener('click', captureSelfie);
    $('#cameraClose').addEventListener('click', () => {
      stopCameraStream();
    });
    $('#cameraSwitch').addEventListener('click', switchCamera);

    // Video recording
    $('#videoBtn').addEventListener('click', startVideoRecording);
    $('#videoStop').addEventListener('click', stopVideoRecording);
    $('#videoRetake').addEventListener('click', retakeVideo);
    $('#videoInput').addEventListener('change', handleVideoInput);

    // Profile
    $('#rewardsBtn').addEventListener('click', () => showToast('Каталог наград скоро будет доступен'));
    $('#logoutBtn').addEventListener('click', logout);
    $$('.settings-row[data-toast]').forEach(r => {
      r.addEventListener('click', () => showToast(r.dataset.toast));
    });

    // ESC closes sheet / dropdown
    document.addEventListener('keydown', e => {
      if (e.key === 'Escape') {
        if ($('#sheetBg').classList.contains('open')) closeSheet();
        closeCityDropdown();
      }
    });

    // Resize: keep map sized correctly
    window.addEventListener('resize', () => {
      if (state.map) state.map.invalidateSize();
    });
  }

  // ── Init ───────────────────────────────────────────
  document.addEventListener('DOMContentLoaded', () => {
    wireEvents();
    renderLeaderboard();
    renderTasks();
    // Do NOT init map here — container has display:none so Leaflet gets zero size.
    // Map will be initialized on first login in the login() function.
  });
})();
