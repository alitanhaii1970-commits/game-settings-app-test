/* PC Max — service worker
 * پوسته‌ی برنامه (HTML/CSS/JS) به‌صورت نسخه‌بندی‌شده کش می‌شه تا بدون اینترنت هم باز بشه.
 * عکس بازی‌ها و فونت‌ها هم بعد از اولین دیدن کش می‌شن. داده‌ی Firestore کش نمی‌شه (خود اپ کش می‌کنه).
 * موقع آپدیت فقط عدد BUILD رو بالا ببر (همراه app.js و version.json). */
const BUILD = 1;
const SHELL_CACHE = 'pcmax-shell-v' + BUILD;
const FONT_CACHE = 'pcmax-fonts-v1';
const IMG_CACHE = 'pcmax-img-v1';
const SHELL = [
  './', 'index.html', 'styles.css', 'app.js', 'manifest.webmanifest',
  'assets/logo.png', 'icons/icon-192.png', 'icons/apple-touch-icon.png'
];

self.addEventListener('install', (e) => {
  e.waitUntil((async () => {
    const cache = await caches.open(SHELL_CACHE);
    await Promise.all(SHELL.map(async (u) => {
      try {
        const res = await fetch(u, { cache: 'reload' });
        if (res.ok) await cache.put(u, res);
      } catch (_) { /* آفلاین یا خطا: بعداً موقع استفاده کش می‌شه */ }
    }));
    await self.skipWaiting();
  })());
});

self.addEventListener('activate', (e) => {
  e.waitUntil((async () => {
    const names = await caches.keys();
    await Promise.all(names
      .filter((n) => n.startsWith('pcmax-shell-') && n !== SHELL_CACHE)
      .map((n) => caches.delete(n)));
    await self.clients.claim();
  })());
});

async function shell(req) {
  const cache = await caches.open(SHELL_CACHE);
  const hit = await cache.match(req, { ignoreSearch: true });
  if (hit) return hit;
  try {
    const res = await fetch(req);
    if (res && res.ok) cache.put(req, res.clone());
    return res;
  } catch (err) {
    if (req.mode === 'navigate') {
      const index = await cache.match('index.html');
      if (index) return index;
    }
    throw err;
  }
}

async function cacheFirst(req, name) {
  const cache = await caches.open(name);
  const hit = await cache.match(req);
  if (hit) return hit;
  const res = await fetch(req);
  if (res && res.ok) cache.put(req, res.clone());
  return res;
}

async function trim(cache, max) {
  const keys = await cache.keys();
  if (keys.length > max) {
    await Promise.all(keys.slice(0, keys.length - max).map((k) => cache.delete(k)));
  }
}

async function image(req) {
  const cache = await caches.open(IMG_CACHE);
  const hit = await cache.match(req.url);
  if (hit) return hit;
  try {
    const res = await fetch(req.url, { mode: 'cors', credentials: 'omit' });
    if (res && res.ok) {
      cache.put(req.url, res.clone()).then(() => trim(cache, 250)).catch(() => {});
    }
    return res;
  } catch (_) {
    return fetch(req); // سرور CORS نمی‌ده: بدون کش، مثل حالت عادی نمایش بده
  }
}

self.addEventListener('fetch', (e) => {
  const req = e.request;
  if (req.method !== 'GET') return;
  const url = new URL(req.url);

  if (url.origin === self.location.origin) {
    if (/\/(version\.json|sw\.js)$/.test(url.pathname)) return; // همیشه از شبکه
    if (url.pathname.includes('/fonts/')) { e.respondWith(cacheFirst(req, FONT_CACHE)); return; }
    e.respondWith(shell(req));
    return;
  }
  if (req.destination === 'image') e.respondWith(image(req));
});
