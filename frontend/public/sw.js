self.addEventListener('install', (event) => {
  event.waitUntil(
    Promise.all([
      caches.open('pushpal-v2').then((cache) =>
        cache.addAll(['/manifest.json', '/icons/icon-192.png', '/icons/icon-512.png'])
      ),
      self.skipWaiting(),
    ])
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    Promise.all([
      caches.keys().then((keys) =>
        Promise.all(keys.filter((key) => key !== 'pushpal-v2').map((key) => caches.delete(key)))
      ),
      self.clients.claim(),
    ])
  );
});

self.addEventListener('fetch', (event) => {
  const requestUrl = new URL(event.request.url);
  const isStaticAsset = requestUrl.pathname.startsWith('/_next/static/')
    || requestUrl.pathname.startsWith('/icons/')
    || requestUrl.pathname === '/manifest.json';
  if (event.request.method !== 'GET'
      || requestUrl.origin !== self.location.origin
      || !isStaticAsset) return;

  event.respondWith(
    fetch(event.request)
      .then((response) => {
        if (response.ok) {
          const copy = response.clone();
          return caches.open('pushpal-v2')
            .then((cache) => cache.put(event.request, copy))
            .then(() => response);
        }
        return response;
      })
      .catch(async () => {
        const cached = await caches.match(event.request);
        if (cached) return cached;
        return Response.error();
      })
  );
});

self.addEventListener('push', (event) => {
  const data = event.data ? event.data.json() : {};
  const payload = data.data || {};
  const options = {
    body: data.body || '',
    icon: '/icons/icon-192.png',
    badge: '/icons/icon-192.png',
    data: { notificationId: payload.notificationId },
    tag: payload.notificationId,
  };
  event.waitUntil(
    Promise.all([
      self.registration.showNotification(data.title || 'PushPal', options),
      reportDelivered(payload),
    ])
  );
});

async function reportDelivered(payload) {
  if (!payload.apiUrl || !payload.deliveryToken || !payload.notificationId) return;
  try {
    const response = await fetch(
      `${payload.apiUrl}/api/notifications/${payload.notificationId}/delivered`, {
      method: 'POST',
      headers: { 'X-PushPal-Delivery-Token': payload.deliveryToken },
      }
    );
    if (!response.ok) throw new Error('Delivery report rejected');
  } catch (e) {
    // best-effort delivery reporting
  }
}

self.addEventListener('notificationclick', (event) => {
  event.notification.close();
  const notificationId = event.notification.data && event.notification.data.notificationId;
  const target = notificationId ? `/dashboard?viewed=${notificationId}` : '/';
  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then((windowClients) => {
      for (const client of windowClients) {
        if (client.url.includes('/') && 'focus' in client) {
          client.navigate(target);
          return client.focus();
        }
      }
      return clients.openWindow(target);
    })
  );
});
