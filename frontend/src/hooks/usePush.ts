'use client';

import { useState, useCallback } from 'react';
import { registerServiceWorker, subscribeToPush, getPushPermissionStatus } from '@/lib/push';

export function usePush() {
  const [permission, setPermission] = useState<NotificationPermission>(
    typeof window !== 'undefined' ? getPushPermissionStatus() : 'default'
  );
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const requestPermission = useCallback(async () => {
    if (!('Notification' in window)) {
      setError('Push notifications are not supported');
      return false;
    }

    const result = await Notification.requestPermission();
    setPermission(result);
    return result === 'granted';
  }, []);

  const register = useCallback(async (replaceExisting = false) => {
    setLoading(true);
    setError(null);
    try {
      const granted = await requestPermission();
      if (!granted) {
        setError('Notification permission denied');
        return false;
      }

      const registration = await registerServiceWorker();
      await subscribeToPush(registration, replaceExisting);
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to register push notifications');
      return false;
    } finally {
      setLoading(false);
    }
  }, [requestPermission]);

  return { permission, loading, error, register, requestPermission };
}
