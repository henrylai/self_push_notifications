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
  if (!payload.apiUrl || !payload.token || !payload.notificationId) return;
  try {
    await fetch(`${payload.apiUrl}/api/notifications/${payload.notificationId}/delivered`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${payload.token}` },
    });
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
