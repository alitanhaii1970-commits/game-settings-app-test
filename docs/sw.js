/* نسخه‌ی قدیمی این صفحه منتقل شده؛ این فایل کش‌ها و خودش رو پاک می‌کنه تا نصب‌های قدیمی به آدرس جدید برن */
self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', (e) => e.waitUntil((async () => {
  const keys = await caches.keys();
  await Promise.all(keys.map((k) => caches.delete(k)));
  await self.registration.unregister();
  const clients = await self.clients.matchAll({ type: 'window' });
  clients.forEach((c) => c.navigate(c.url));
})()));
