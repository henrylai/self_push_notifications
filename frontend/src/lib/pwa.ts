export async function registerSW(): Promise<ServiceWorkerRegistration | null> {
  if (!('serviceWorker' in navigator)) return null;
  try {
    const registration = await navigator.serviceWorker.register('/sw.js', {
      updateViaCache: 'none',
    });
    await registration.update().catch(() => undefined);
    return navigator.serviceWorker.ready;
  } catch {
    return null;
  }
}

export function isStandalone(): boolean {
  if (typeof window === 'undefined') return false;
  return (
    window.matchMedia('(display-mode: standalone)').matches ||
    (window.navigator as Navigator & { standalone?: boolean }).standalone === true
  );
}

export function canInstall(): boolean {
  if (typeof window === 'undefined') return false;
  return 'beforeinstallprompt' in window;
}
