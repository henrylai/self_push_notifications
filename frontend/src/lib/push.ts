import { api } from './api';

export async function registerServiceWorker(): Promise<ServiceWorkerRegistration> {
  if (!('serviceWorker' in navigator)) {
    throw new Error('Service workers are not supported in this browser');
  }
  const registration = await navigator.serviceWorker.register('/sw.js', {
    updateViaCache: 'none',
  });
  // Do not leave a phone using an old service worker after a deploy. A failed update
  // check is non-fatal because the current worker can still receive notifications.
  await registration.update().catch(() => undefined);
  return navigator.serviceWorker.ready;
}

export async function subscribeToPush(
  registration: ServiceWorkerRegistration,
  replaceExisting = false
): Promise<PushSubscription> {
  const vapidPublicKey = process.env.NEXT_PUBLIC_VAPID_PUBLIC_KEY;
  if (!vapidPublicKey) {
    throw new Error('VAPID public key is not configured');
  }

  const existingSubscription = await registration.pushManager.getSubscription();
  if (replaceExisting && existingSubscription) {
    await existingSubscription.unsubscribe();
  }

  const subscription = !replaceExisting && existingSubscription
    ? existingSubscription
    : await registration.pushManager.subscribe({
      userVisibleOnly: true,
      applicationServerKey: urlBase64ToUint8Array(vapidPublicKey),
    });

  await registerPushSubscription(subscription);

  return subscription;
}

/**
 * Re-register the current browser's existing subscription when the app opens.
 * This repairs server-side records that were removed after a subscription expired,
 * without showing a browser permission prompt or replacing a working subscription.
 */
export async function syncExistingPushSubscription(): Promise<boolean> {
  if (getPushPermissionStatus() !== 'granted') return false;

  const registration = await registerServiceWorker();
  const subscription = await registration.pushManager.getSubscription();
  if (!subscription) return false;

  await registerPushSubscription(subscription);
  return true;
}

async function registerPushSubscription(subscription: PushSubscription): Promise<void> {
  await api.registerDevice({
    endpoint: subscription.endpoint,
    p256dh: encodeSubscriptionKey(subscription, 'p256dh'),
    auth: encodeSubscriptionKey(subscription, 'auth'),
    userAgent: navigator.userAgent,
  });
}

function encodeSubscriptionKey(subscription: PushSubscription, name: 'p256dh' | 'auth'): string {
  const key = subscription.getKey(name);
  if (!key) throw new Error(`Push subscription is missing its ${name} key`);
  return btoa(String.fromCharCode.apply(null, Array.from(new Uint8Array(key))));
}

export function urlBase64ToUint8Array(base64String: string): Uint8Array<ArrayBuffer> {
  const padding = '='.repeat((4 - (base64String.length % 4)) % 4);
  const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
  const rawData = window.atob(base64);
  const outputArray = new Uint8Array(rawData.length);
  for (let i = 0; i < rawData.length; ++i) {
    outputArray[i] = rawData.charCodeAt(i);
  }
  return outputArray;
}

export function getPushPermissionStatus(): NotificationPermission {
  if (!('Notification' in window)) return 'denied';
  return Notification.permission;
}
