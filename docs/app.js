/* PC Max — نسخه وب (PWA)
 * پورت مستقیم نسخه‌ی SwiftUI/اندروید: همان داده (Firestore)، همان منطق، همان طراحی.
 * موقع آپدیت: عدد BUILD رو اینجا، توی sw.js و version.json با هم بالا ببر. */
(() => {
'use strict';

const BUILD = 1;
const VERSION = '1.0.' + BUILD;
const PROJECT_ID = 'pc-max-a0a4b';
const API_KEY = 'AIzaSyDloW_gBy_P7WZEBlEEa9wzOxX-PMmlvkQ';

const $ = (s, r = document) => r.querySelector(s);
const $$ = (s, r = document) => Array.from(r.querySelectorAll(s));
const esc = (s) => String(s == null ? '' : s).replace(/[&<>"']/g, (c) =>
  ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const store = {
  get(k, d) { try { const v = localStorage.getItem(k); return v === null ? d : v; } catch (_) { return d; } },
  set(k, v) { try { localStorage.setItem(k, v); } catch (_) { /* حافظه پر یا بسته */ } }
};
const pick = (v, allowed, d) => (allowed.includes(v) ? v : d);

/* ---------------- رشته‌ها (فارسی / انگلیسی) ---------------- */
const T = {
  fa: {
    search_hint: 'جستجوی بازی…', empty_games: 'هنوز بازی‌ای اضافه نشده', no_results: 'بازی‌ای با این اسم پیدا نشد',
    offline_empty: 'اتصال به اینترنت برقرار نیست.\nلیست قبلی موجود نیست.',
    offline_toast: 'اتصال برقرار نشد، لیست قبلی نشون داده می‌شه', list_updated: 'لیست به‌روز شد ✅', retry: 'تلاش دوباره',
    green_settings: 'تنظیمات بهینه سبز', yellow_settings: 'تنظیمات بهینه زرد',
    no_settings: 'برای این بازی هنوز تنظیماتی ثبت نشده.',
    youtube_hint: 'برای دیدن تنظیمات این بازی روی لینک یوتیوب کلیک کنید', watch_on_youtube: 'مشاهده در یوتیوب',
    settings_title: 'تنظیمات', settings_language: 'زبان برنامه', settings_font: 'فونت برنامه',
    settings_system_power: 'قدرت سیستم شما (کامپیوتر)', tier_weak: 'ضعیف', tier_medium: 'متوسط', tier_strong: 'قوی',
    lang_fa: 'فارسی', lang_en: 'English',
    check_for_update: 'بررسی آپدیت', checking_for_update: 'در حال بررسی…', up_to_date: 'شما به‌روز هستید ✅',
    update_check_failed: 'بررسی آپدیت ناموفق بود', update_found_title: 'نسخه‌ی جدید پیدا شد',
    update_web_hint: 'نسخه‌ی جدید آماده‌ست. با بروزرسانی، برنامه یک‌بار دوباره بارگذاری می‌شه.',
    update_now: 'بروزرسانی', later: 'بعداً', version_label: 'نسخه‌ی فعلی: %s',
    onboard_welcome_title: 'به PC Max خوش آمدید', onboard_welcome_subtitle: 'تنظیمات بهینه بازی‌ها، همیشه در دسترس شما',
    onboard_lang_title: 'زبان برنامه را انتخاب کنید', onboard_lang_subtitle: 'می‌توانید بعداً از تنظیمات آن را تغییر دهید',
    onboard_tier_title: 'قدرت سیستم شما (کامپیوتر) چطوره؟',
    onboard_tier_subtitle: 'بر این اساس، توی هر بازی بهت نشون می‌دیم کدوم تنظیمات مناسب‌تره',
    onboard_next: 'بعدی', onboard_finish: 'شروع کنید',
    install_title: 'نصب روی صفحه‌ی اصلی', install_row: 'اضافه کردن به صفحه‌ی اصلی',
    install_sub: 'مثل یه اپ واقعی، بدون اپ‌استور و بدون دانلود فایل نصبی',
    install_ios_1: 'پایین Safari دکمه‌ی اشتراک‌گذاری (Share) رو بزن',
    install_ios_2: 'پایین بیا و «Add to Home Screen» رو انتخاب کن',
    install_ios_3: 'روی «Add» بزن — تمام! آیکون PC Max میاد روی صفحه‌ات',
    install_and_1: 'منوی ⋮ بالای مرورگر رو باز کن',
    install_and_2: '«Install app» یا «Add to Home screen» رو انتخاب کن',
    install_btn: 'نصب', got_it: 'متوجه شدم'
  },
  en: {
    search_hint: 'Search game…', empty_games: 'No games added yet', no_results: 'No game found with that name',
    offline_empty: "No internet connection.\nNo previous list available.",
    offline_toast: "Couldn't connect, showing the previous list", list_updated: 'List updated ✅', retry: 'Retry',
    green_settings: 'Green Optimal Settings', yellow_settings: 'Yellow Optimal Settings',
    no_settings: 'No settings have been added for this game yet.',
    youtube_hint: "Tap the YouTube link to see this game's settings", watch_on_youtube: 'Watch on YouTube',
    settings_title: 'Settings', settings_language: 'App language', settings_font: 'App font',
    settings_system_power: 'Your System Power (PC)', tier_weak: 'Weak', tier_medium: 'Medium', tier_strong: 'Strong',
    lang_fa: 'فارسی', lang_en: 'English',
    check_for_update: 'Check for Update', checking_for_update: 'Checking…', up_to_date: "You're up to date ✅",
    update_check_failed: 'Update check failed', update_found_title: 'New version found',
    update_web_hint: 'A new version is ready. Updating reloads the app once.',
    update_now: 'Update', later: 'Later', version_label: 'Current version: %s',
    onboard_welcome_title: 'Welcome to PC Max', onboard_welcome_subtitle: 'Optimal game settings, always at hand',
    onboard_lang_title: 'Choose your language', onboard_lang_subtitle: 'You can change this later in Settings',
    onboard_tier_title: 'How powerful is your system (PC)?',
    onboard_tier_subtitle: "We'll use this to show which settings fit your PC best, for each game",
    onboard_next: 'Next', onboard_finish: 'Get Started',
    install_title: 'Install on Home Screen', install_row: 'Add to Home Screen',
    install_sub: 'Works like a real app: no App Store, no installer file',
    install_ios_1: 'Tap the Share button at the bottom of Safari',
    install_ios_2: 'Scroll down and choose “Add to Home Screen”',
    install_ios_3: 'Tap “Add” — done! The PC Max icon appears on your home screen',
    install_and_1: 'Open the ⋮ menu at the top of your browser',
    install_and_2: 'Choose “Install app” or “Add to Home screen”',
    install_btn: 'Install', got_it: 'Got it'
  }
};

const FONTS = [
  { id: 'system', name: 'پیش‌فرض سیستم / System Default', css: null },
  { id: 'vazirmatn', name: 'وزیرمتن', css: "'Vazirmatn'" },
  { id: 'sahel', name: 'ساحل', css: "'Sahel'" },
  { id: 'montserrat', name: 'Montserrat', css: "'Montserrat'" },
  { id: 'inter', name: 'Inter', css: "'Inter'" }
];
const SYSTEM_STACK = '-apple-system,BlinkMacSystemFont,"SF Pro Text","Segoe UI",Roboto,"Helvetica Neue",Arial,sans-serif';

const state = {
  lang: pick(store.get('language', 'fa'), ['fa', 'en'], 'fa'),
  fontId: pick(store.get('app_font', 'system'), FONTS.map((f) => f.id), 'system'),
  tier: pick(store.get('system_tier', ''), ['weak', 'medium', 'strong'], ''),
  onboardingDone: store.get('onboarding_done', '0') === '1',
  games: [], loading: false, emptyMessage: null, query: ''
};
const t = (k, lang = state.lang) => (T[lang] && T[lang][k]) || T.fa[k] || k;

/* ---------------- آیکون‌ها ---------------- */
const line = (inner) => `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">${inner}</svg>`;
const ICON = {
  back: line('<path d="M15 5l-7 7 7 7"/>'),
  chev: line('<path d="M9 5l7 7-7 7"/>'),
  gear: '<svg viewBox="0 0 24 24" fill="currentColor" fill-rule="evenodd"><path d="M19.26 9.77 L22.26 10.30 L22.26 13.70 L19.26 14.23 L18.72 15.56 L20.46 18.05 L18.05 20.46 L15.56 18.72 L14.23 19.26 L13.70 22.26 L10.30 22.26 L9.77 19.26 L8.44 18.72 L5.95 20.46 L3.54 18.05 L5.28 15.56 L4.74 14.23 L1.74 13.70 L1.74 10.30 L4.74 9.77 L5.28 8.44 L3.54 5.95 L5.95 3.54 L8.44 5.28 L9.77 4.74 L10.30 1.74 L13.70 1.74 L14.23 4.74 L15.56 5.28 L18.05 3.54 L20.46 5.95 L18.72 8.44Z M15.6 12a3.6 3.6 0 1 0-7.2 0 3.6 3.6 0 0 0 7.2 0Z"/></svg>',
  refresh: line('<path d="M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8"/><path d="M21 3v5h-5"/>'),
  search: line('<circle cx="11" cy="11" r="7"/><path d="M21 21l-4.3-4.3"/>'),
  expand: line('<path d="M15 3h6v6M9 21H3v-6M21 3l-7 7M3 21l7-7"/>'),
  close: line('<path d="M6 6l12 12M18 6L6 18"/>'),
  photo: line('<rect x="3" y="4" width="18" height="16" rx="3"/><circle cx="9" cy="10" r="1.8"/><path d="M4 18l5-5 4 4 3-3 4 4"/>'),
  yt: '<svg class="ico" viewBox="0 0 24 24"><rect x="1.5" y="4.5" width="21" height="15" rx="4.5" fill="currentColor"/><path d="M10 9l5.2 3L10 15z" fill="#18181B"/></svg>',
  play: '<svg viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>',
  share: line('<path d="M12 15V3M8 7l4-4 4 4"/><path d="M6 11H5a1 1 0 0 0-1 1v8a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-8a1 1 0 0 0-1-1h-1"/>')
};

/* ---------------- ظاهر: زبان، جهت، فونت ---------------- */
function applyDir(lang) {
  const r = document.documentElement;
  r.lang = lang;
  r.dir = lang === 'fa' ? 'rtl' : 'ltr';
}
function applyFont() {
  const f = FONTS.find((x) => x.id === state.fontId) || FONTS[0];
  document.documentElement.style.setProperty('--font', f.css ? f.css + ',' + SYSTEM_STACK : SYSTEM_STACK);
}

/* ---------------- توست ---------------- */
let toastTimer = null;
function toast(msg) {
  const el = $('#toast');
  el.firstElementChild.textContent = msg;
  el.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => el.classList.remove('show'), 2200);
}

/* ---------------- داده: Firestore (REST) + کش محلی ---------------- */
const fstr = (f, k) => (f[k] && typeof f[k].stringValue === 'string' ? f[k].stringValue : '');

async function fetchPage(token, useKey) {
  const u = new URL('https://firestore.googleapis.com/v1/projects/' + PROJECT_ID + '/databases/(default)/documents/games');
  u.searchParams.set('pageSize', '300');
  if (token) u.searchParams.set('pageToken', token);
  if (useKey) u.searchParams.set('key', API_KEY);
  const ctrl = typeof AbortController === 'function' ? new AbortController() : null;
  const timer = setTimeout(() => ctrl && ctrl.abort(), 15000);
  try {
    const res = await fetch(u.toString(), { cache: 'no-store', signal: ctrl ? ctrl.signal : undefined });
    if (!res.ok) { const e = new Error('http ' + res.status); e.status = res.status; throw e; }
    return await res.json();
  } finally { clearTimeout(timer); }
}

async function fetchGames() {
  const all = [];
  let token = null;
  let useKey = true;
  do {
    let data;
    try {
      data = await fetchPage(token, useKey);
    } catch (e) {
      // اگر کلید API برای وب محدود شده باشه، یک‌بار بدون کلید (با Security Rules عمومی) تلاش می‌کنیم
      if (useKey && (e.status === 400 || e.status === 403)) { useKey = false; data = await fetchPage(token, false); }
      else throw e;
    }
    for (const d of data.documents || []) {
      const f = d.fields || {};
      all.push({
        id: String(d.name || '').split('/').pop(),
        name: fstr(f, 'name'), imageUrl: fstr(f, 'imageUrl'),
        settingsGreen: fstr(f, 'settingsGreen'), settingsYellow: fstr(f, 'settingsYellow'),
        youtubeUrl: fstr(f, 'youtubeUrl'),
        showYoutubeButton: !!(f.showYoutubeButton && f.showYoutubeButton.booleanValue === true),
        updatedAt: Number(f.updatedAt && f.updatedAt.integerValue) || 0
      });
    }
    token = data.nextPageToken || null;
  } while (token);
  return all.sort((a, b) => { const x = a.name.toLowerCase(), y = b.name.toLowerCase(); return x < y ? -1 : x > y ? 1 : 0; });
}

function readCache() {
  try { const a = JSON.parse(store.get('games_cache', '')); return Array.isArray(a) && a.length ? a : null; } catch (_) { return null; }
}
const saveCache = (games) => store.set('games_cache', JSON.stringify(games));

/* مثل اپ اندروید: فقط بار اول از سرور می‌خونه؛ بعدش از کش محلی، تا وقتی کاربر خودش رفرش بزنه */
async function loadInitial() {
  const cached = readCache();
  if (store.get('has_loaded_games', '0') === '1' && cached) { state.games = cached; updateList(); }
  else await refresh(true);
  if ((location.hash || '').startsWith('#/game/')) route();
}

async function refresh(force) {
  if (state.loading) return;
  state.loading = true; state.emptyMessage = null; syncRefresh(); updateList();
  try {
    const fresh = await fetchGames();
    state.games = fresh; saveCache(fresh);
    if (force) { store.set('has_loaded_games', '1'); toast(t('list_updated')); }
  } catch (_) {
    if (!state.games.length) state.emptyMessage = t('offline_empty');
    else toast(t('offline_toast'));
  } finally {
    state.loading = false; syncRefresh(); updateList();
  }
}

/* ---------------- منطق بازی ---------------- */
const norm = (s) => String(s || '').toLowerCase().replace(/ي/g, 'ی').replace(/ك/g, 'ک').trim();
function ytMode(g) {
  const u = (g.youtubeUrl || '').trim();
  return !!g.showYoutubeButton && u !== '' && (u.includes('youtube') || u.startsWith('http'));
}

/* ---------------- تصویر با کش، محو شدن و حالت خطا ---------------- */
function imgWrap(url, spinner, lazy) {
  const u = (url || '').trim();
  return '<div class="imgwrap' + (u ? '' : ' err') + '">' +
    (u ? '<img src="' + esc(u) + '" alt="" decoding="async" referrerpolicy="no-referrer"' + (lazy ? ' loading="lazy"' : '') + '>' : '') +
    (spinner ? '<span class="sp"><i></i></span>' : '') +
    '<span class="ph">' + ICON.photo + '</span></div>';
}
document.addEventListener('load', (e) => {
  const img = e.target;
  if (img && img.tagName === 'IMG' && img.parentElement && img.parentElement.classList.contains('imgwrap')) {
    img.classList.add('ok'); img.parentElement.classList.add('done');
  }
}, true);
document.addEventListener('error', (e) => {
  const img = e.target;
  if (img && img.tagName === 'IMG' && img.parentElement && img.parentElement.classList.contains('imgwrap')) {
    img.parentElement.classList.add('err', 'done');
  }
}, true);

/* ---------------- ناوبری ---------------- */
const VIEWS = ['onb', 'list', 'detail', 'settings'];
function show(name) { VIEWS.forEach((v) => $('#v-' + v).classList.toggle('active', v === name)); }
let idx = 0;
function go(hash) { idx++; history.pushState({ i: idx }, '', hash); route(); }
function back() {
  if (idx > 0) history.back();
  else { history.replaceState({ i: 0 }, '', '#/'); route(); }
}
function route() {
  closePreview(); closeModal();
  if (!state.onboardingDone) { applyDir('fa'); show('onb'); return; }
  let h = (location.hash || '#/').slice(1);
  try { h = decodeURIComponent(h); } catch (_) { /* لینک خراب */ }
  if (h.startsWith('/game/')) {
    const g = state.games.find((x) => x.id === h.slice(6));
    if (g) { renderDetail(g); show('detail'); return; }
  } else if (h === '/settings') { renderSettings(); show('settings'); return; }
  show('list');
}
window.addEventListener('popstate', (e) => { idx = (e.state && e.state.i) || 0; route(); });

const backBtn = () => '<button class="rbtn" data-act="back" aria-label="back"><span class="flip">' + ICON.back + '</span></button>';

/* ---------------- صفحه‌ی اصلی (لیست) ---------------- */
function buildList() {
  $('#v-list').innerHTML =
    '<div class="head"><img src="assets/logo.png" alt=""><h1>PC Max</h1>' +
    '<button class="rbtn red" data-act="settings" aria-label="settings">' + ICON.gear + '</button>' +
    '<button class="rbtn red" id="btnRefresh" data-act="refresh" aria-label="refresh">' + ICON.refresh + '</button></div>' +
    '<label class="search">' + ICON.search +
    '<input id="q" type="text" inputmode="search" enterkeyhint="search" autocomplete="off" autocapitalize="off" autocorrect="off" spellcheck="false" placeholder="' +
    esc(t('search_hint')) + '" value="' + esc(state.query) + '"></label>' +
    '<div class="scroll" id="listBody"></div>';
  syncRefresh(); updateList();
}
function syncRefresh() { const b = $('#btnRefresh'); if (b) b.disabled = state.loading; }

function rowHtml(g) {
  return '<button class="row press" data-act="open" data-id="' + esc(g.id) + '">' +
    '<span class="thumb">' + imgWrap(g.imageUrl, false, true) + '</span>' +
    '<span class="name">' + esc(g.name) + '</span>' +
    '<span class="chev flip">' + ICON.chev + '</span></button>';
}

function updateList() {
  const box = $('#listBody');
  if (!box) return;
  const q = norm(state.query);
  const games = q ? state.games.filter((g) => norm(g.name).includes(q)) : state.games;
  let html;
  if (state.loading && !state.games.length) html = '<div class="center"><div class="spinner"></div></div>';
  else if (state.emptyMessage && !state.games.length)
    html = '<div class="center"><div class="msg">' + esc(state.emptyMessage).replace(/\n/g, '<br>') + '</div><button class="pill" data-act="refresh">' + esc(t('retry')) + '</button></div>';
  else if (!state.games.length) html = '<div class="center"><div class="msg">' + esc(t('empty_games')) + '</div></div>';
  else if (!games.length) html = '<div class="center"><div class="msg">' + esc(t('no_results')) + '</div></div>';
  else html = '<div class="list">' + games.map(rowHtml).join('') + '</div>';
  box.innerHTML = html;
}

/* ---------------- جزئیات بازی ---------------- */
function block(tag, color, text, glow) {
  return '<div class="block" style="--c:var(--' + color + ');--cbg:var(--' + color + '-bg)"><span class="tag">' + esc(tag) +
    '</span><div class="card' + (glow ? ' glow' : '') + '">' + esc(text) + '</div></div>';
}
function blocksHtml(g) {
  const hasGreen = (g.settingsGreen || '').trim() !== '';
  const hasYellow = (g.settingsYellow || '').trim() !== '';
  const fallback = !hasGreen && !hasYellow;
  // مثل اندروید: فقط اگه قدرت سیستم انتخاب شده، بخشِ مناسب‌تر (سبز برای ضعیف/متوسط، زرد برای قوی) درخشان و «نفس‌کش» می‌شه
  const recGreen = state.tier !== 'strong';
  const hasTier = state.tier !== '';
  let out = '';
  if (hasGreen || fallback) out += block(t('green_settings'), 'green', fallback ? t('no_settings') : g.settingsGreen, !fallback && recGreen && hasTier);
  if (hasYellow) out += block(t('yellow_settings'), 'yellow', g.settingsYellow, !recGreen && hasTier);
  return '<div class="blocks">' + out + '</div>';
}
function ytHtml(g) {
  let url = g.youtubeUrl.trim();
  if (!/^https?:\/\//i.test(url)) url = 'https://' + url;
  return '<div class="yt">' + ICON.yt + '<p>' + esc(t('youtube_hint')) + '</p>' +
    '<a class="btn" href="' + esc(url) + '" target="_blank" rel="noopener">' + ICON.play + '<span>' + esc(t('watch_on_youtube')) + '</span></a></div>';
}
function renderDetail(g) {
  $('#v-detail').innerHTML =
    '<div class="topbar">' + backBtn() + '<h1>' + esc(g.name) + '</h1></div>' +
    '<div class="scroll"><div class="pad">' +
    '<div class="hero" data-act="preview" data-url="' + esc(g.imageUrl) + '">' + imgWrap(g.imageUrl, true, false) +
    '<span class="expand">' + ICON.expand + '</span></div>' +
    (ytMode(g) ? ytHtml(g) : blocksHtml(g)) + '</div></div>';
}

/* پیش‌نمایش تمام‌صفحه‌ی عکس */
function openPreview(url) {
  if (!url) return;
  const p = $('#preview');
  $('img', p).src = url;
  p.classList.add('show'); p.setAttribute('aria-hidden', 'false');
}
function closePreview() {
  const p = $('#preview');
  if (!p.classList.contains('show')) return;
  p.classList.remove('show'); p.setAttribute('aria-hidden', 'true');
}

/* ---------------- تنظیمات ---------------- */
let updState = 'idle';
const isStandalone = () => window.navigator.standalone === true || (window.matchMedia && window.matchMedia('(display-mode: standalone)').matches);
const isIOS = () => /iphone|ipad|ipod/i.test(navigator.userAgent) || (navigator.platform === 'MacIntel' && navigator.maxTouchPoints > 1);

function radio(opts, selected, act, yellow) {
  return '<div class="radio' + (yellow ? ' yellow' : '') + '">' + opts.map((o) =>
    '<button class="opt' + (o.id === selected ? ' sel' : '') + '" data-act="' + act + '" data-id="' + esc(o.id) + '">' +
    '<span class="lbl">' + esc(o.label) + '</span><span class="dot"></span></button>').join('') + '</div>';
}
function updLabel() {
  return t(updState === 'checking' ? 'checking_for_update' : updState === 'ok' ? 'up_to_date' : 'check_for_update');
}
function renderSettings() {
  const old = $('#v-settings .scroll');
  const top = old ? old.scrollTop : 0;
  $('#v-settings').innerHTML =
    '<div class="topbar">' + backBtn() + '<h1>' + esc(t('settings_title')) + '</h1></div><div class="scroll">' +
    '<div class="sec">' + esc(t('settings_language')) + '</div>' +
    radio([{ id: 'fa', label: t('lang_fa') }, { id: 'en', label: t('lang_en') }], state.lang, 'lang') +
    '<div class="sec">' + esc(t('settings_system_power')) + '</div>' +
    radio([{ id: 'weak', label: t('tier_weak') }, { id: 'medium', label: t('tier_medium') }, { id: 'strong', label: t('tier_strong') }], state.tier, 'tier') +
    '<div class="sec y">' + esc(t('settings_font')) + '</div>' +
    radio(FONTS.map((f) => ({ id: f.id, label: f.name })), state.fontId, 'font', true) +
    (isStandalone() ? '' :
      '<div class="sec">' + esc(t('install_title')) + '</div><div class="radio"><button class="opt" data-act="install"><span class="lbl">' +
      esc(t('install_row')) + '</span><span class="go flip">' + ICON.chev + '</span></button></div>') +
    '<div class="about"><img src="assets/logo.png" alt=""><b>PC Max</b><small>' + esc(t('version_label').replace('%s', VERSION)) + '</small>' +
    '<button class="pill" id="btnUpdate" data-act="upd">' + esc(updLabel()) + '</button></div></div>';
  const sc = $('#v-settings .scroll');
  if (sc && top) sc.scrollTop = top;
}
function setLang(l) {
  if (l !== 'fa' && l !== 'en') return;
  state.lang = l; store.set('language', l); applyDir(l); buildList(); renderSettings();
}

/* ---------------- بررسی و اعمال آپدیت ---------------- */
function syncUpd() { const b = $('#btnUpdate'); if (b) b.textContent = updLabel(); }
async function checkUpdate() {
  if (updState === 'checking') return;
  updState = 'checking'; syncUpd();
  try {
    const res = await fetch('version.json?t=' + Date.now(), { cache: 'no-store' });
    if (!res.ok) throw new Error('http ' + res.status);
    const info = await res.json();
    if (Number(info.build) > BUILD) { updState = 'idle'; openUpdateModal(); }
    else { updState = 'ok'; setTimeout(() => { if (updState === 'ok') { updState = 'idle'; syncUpd(); } }, 2500); }
  } catch (_) {
    updState = 'idle'; toast(t('update_check_failed'));
  }
  syncUpd();
}
function openUpdateModal() {
  const m = $('#modal');
  m.innerHTML = '<div class="modal"><h3>' + esc(t('update_found_title')) + '</h3><p>' + esc(t('update_web_hint')) + '</p>' +
    '<button class="btn" data-act="upd-apply">' + esc(t('update_now')) + '</button>' +
    '<button class="later" data-act="modal-close">' + esc(t('later')) + '</button></div>';
  m.dataset.act = 'modal-close'; m.classList.add('show');
}
function closeModal() { const m = $('#modal'); m.classList.remove('show'); }
async function applyUpdate() {
  try {
    const reg = navigator.serviceWorker && await navigator.serviceWorker.getRegistration();
    if (reg) {
      await reg.update();
      const sw = reg.installing || reg.waiting;
      if (sw && sw.state !== 'activated') {
        await new Promise((resolve) => {
          sw.addEventListener('statechange', () => { if (sw.state === 'activated') resolve(); });
          setTimeout(resolve, 8000);
        });
      }
    }
  } catch (_) { /* ادامه بده و فقط رفرش کن */ }
  location.reload();
}

/* ---------------- نصب روی صفحه‌ی اصلی (Add to Home Screen) ---------------- */
let deferredPrompt = null;
window.addEventListener('beforeinstallprompt', (e) => { e.preventDefault(); deferredPrompt = e; });
function openSheet() {
  const ios = isIOS();
  const steps = ios
    ? [t('install_ios_1') + ' ' + ICON.share, esc(t('install_ios_2')), esc(t('install_ios_3'))]
    : [esc(t('install_and_1')), esc(t('install_and_2'))];
  if (ios) steps[0] = esc(t('install_ios_1')) + ' ' + ICON.share;
  const s = $('#sheet');
  s.innerHTML = '<div class="sheet"><h3>' + esc(t('install_title')) + '</h3><p class="sub">' + esc(t('install_sub')) + '</p>' +
    '<ol class="steps">' + steps.map((x, i) => '<li><span class="n">' + (i + 1) + '</span><span>' + x + '</span></li>').join('') + '</ol>' +
    (deferredPrompt && !ios
      ? '<button class="btn" data-act="install-go">' + esc(t('install_btn')) + '</button>'
      : '<button class="btn" data-act="install-close">' + esc(t('got_it')) + '</button>') + '</div>';
  s.dataset.act = 'install-close'; s.classList.add('show');
}
function closeSheet() { store.set('install_hint_seen', '1'); $('#sheet').classList.remove('show'); }
function maybeInstallHint() {
  if (isStandalone() || store.get('install_hint_seen', '0') === '1') return;
  setTimeout(() => { if (state.onboardingDone) openSheet(); }, 1200);
}

/* ---------------- ورود اولیه (همیشه فارسی، مثل اندروید) ---------------- */
const onb = { page: 0, lang: 'fa', tier: 'medium' };
const TIER_COLORS = {
  weak: 'linear-gradient(135deg,#E31E24,#8E1216)',
  medium: 'linear-gradient(135deg,#F5C518,#B38600)',
  strong: 'linear-gradient(135deg,#2ECC71,#1E9E52)'
};
const ocard = (act, id, label, sel, swatch) =>
  '<button class="ocard' + (sel ? ' sel' : '') + '" data-act="' + act + '" data-id="' + id + '">' +
  (swatch ? '<span class="sw" style="background:' + swatch + '"></span>' : '') +
  '<span class="t">' + esc(label) + '</span><span class="dot"></span></button>';

function renderOnboarding() {
  applyDir('fa');
  $('#v-onb').innerHTML = '<div class="onb-body" id="onbBody"></div><div class="dots" id="onbDots"><i></i><i></i><i></i></div>' +
    '<div class="onb-foot"><button class="btn" id="onbNext" data-act="onb-next"></button></div>';
  onbUpdate();
}
function onbUpdate() {
  const f = (k) => esc(T.fa[k]);
  const head = (a, b) => '<div class="onb-head"><h2>' + f(a) + '</h2><p>' + f(b) + '</p></div>';
  let inner;
  if (onb.page === 0) {
    inner = '<div class="onb-welcome"><img src="assets/logo.png" alt=""><div><h1>' + f('onboard_welcome_title') + '</h1><p>' + f('onboard_welcome_subtitle') + '</p></div></div>';
  } else if (onb.page === 1) {
    inner = '<div class="onb-page">' + head('onboard_lang_title', 'onboard_lang_subtitle') +
      ocard('onb-lang', 'fa', T.fa.lang_fa, onb.lang === 'fa') + ocard('onb-lang', 'en', T.fa.lang_en, onb.lang === 'en') + '</div>';
  } else {
    inner = '<div class="onb-page">' + head('onboard_tier_title', 'onboard_tier_subtitle') +
      ['weak', 'medium', 'strong'].map((k) => ocard('onb-tier', k, T.fa['tier_' + k], onb.tier === k, TIER_COLORS[k])).join('') + '</div>';
  }
  const body = $('#onbBody');
  body.style.animation = 'none'; void body.offsetWidth; body.style.animation = '';
  body.innerHTML = inner;
  $$('#onbDots i').forEach((d, i) => d.classList.toggle('on', i === onb.page));
  $('#onbNext').textContent = T.fa[onb.page === 2 ? 'onboard_finish' : 'onboard_next'];
}
function finishOnboarding() {
  state.lang = onb.lang; state.tier = onb.tier; state.onboardingDone = true;
  store.set('language', state.lang); store.set('system_tier', state.tier); store.set('onboarding_done', '1');
  applyDir(state.lang); buildList();
  idx = 0; history.replaceState({ i: 0 }, '', '#/');
  route();
  loadInitial().then(maybeInstallHint);
}

/* ---------------- رویدادها ---------------- */
document.addEventListener('click', (e) => {
  const el = e.target.closest('[data-act]');
  if (!el) return;
  const act = el.dataset.act, id = el.dataset.id;
  // پس‌زمینه‌ی پنجره‌ها فقط وقتی مستقیم لمس شد ببنده
  if ((act === 'modal-close' || act === 'install-close') && el.classList.contains('overlay') && e.target !== el) return;
  switch (act) {
    case 'open': go('#/game/' + encodeURIComponent(id)); break;
    case 'settings': go('#/settings'); break;
    case 'back': back(); break;
    case 'refresh': {
      const b = $('#btnRefresh');
      if (b) { b.classList.remove('spinning'); void b.offsetWidth; b.classList.add('spinning'); }
      refresh(true); break;
    }
    case 'preview': openPreview(el.dataset.url); break;
    case 'preview-close': closePreview(); break;
    case 'lang': setLang(id); break;
    case 'tier': state.tier = id; store.set('system_tier', id); renderSettings(); break;
    case 'font': state.fontId = id; store.set('app_font', id); applyFont(); renderSettings(); break;
    case 'upd': checkUpdate(); break;
    case 'upd-apply': applyUpdate(); break;
    case 'modal-close': closeModal(); break;
    case 'install': openSheet(); break;
    case 'install-close': closeSheet(); break;
    case 'install-go':
      if (deferredPrompt) { deferredPrompt.prompt(); deferredPrompt = null; }
      closeSheet(); break;
    case 'onb-next': if (onb.page < 2) { onb.page++; onbUpdate(); } else finishOnboarding(); break;
    case 'onb-lang': case 'onb-tier':
      if (act === 'onb-lang') onb.lang = id; else onb.tier = id;
      $$('.ocard', el.parentElement).forEach((c) => c.classList.toggle('sel', c === el)); break;
    default: break;
  }
});
document.addEventListener('input', (e) => {
  if (e.target && e.target.id === 'q') { state.query = e.target.value; updateList(); }
});
document.addEventListener('keydown', (e) => { if (e.key === 'Escape') { closePreview(); closeModal(); } });

/* ---------------- شروع ---------------- */
function boot() {
  $('#preview').dataset.act = 'preview-close';
  const pc = $('#previewClose');
  pc.dataset.act = 'preview-close'; pc.innerHTML = ICON.close;
  applyFont();
  history.replaceState({ i: 0 }, '');
  if (state.onboardingDone) {
    applyDir(state.lang); buildList(); route();
    loadInitial().then(maybeInstallHint);
  } else {
    renderOnboarding(); route();
  }
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => { navigator.serviceWorker.register('sw.js').catch(() => {}); });
  }
}
boot();
})();
