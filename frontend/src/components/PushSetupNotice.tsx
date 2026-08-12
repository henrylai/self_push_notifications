'use client';

import { useEffect, useState } from 'react';
import { BellRing } from 'lucide-react';
import { api } from '@/lib/api';
import { usePush } from '@/hooks/usePush';
import Button from '@/components/ui/button';
import Card from '@/components/ui/card';

export default function PushSetupNotice() {
  const { permission, loading, error, register } = usePush();
  const [hasRegisteredDevice, setHasRegisteredDevice] = useState<boolean | null>(null);
  const [devicesError, setDevicesError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    api.getDevices()
      .then((devices) => {
        if (active) setHasRegisteredDevice(devices.length > 0);
      })
      .catch(() => {
        if (active) setDevicesError('Unable to check notification setup.');
      });
    return () => {
      active = false;
    };
  }, []);

  if (hasRegisteredDevice || (hasRegisteredDevice === null && !devicesError)) return null;

  const message = permission === 'denied'
    ? 'Notifications are blocked in your browser. Allow them in browser settings, then return here.'
    : 'Enable notifications on this device before scheduling reminders.';

  const enableNotifications = async () => {
    if (await register(true)) {
      setHasRegisteredDevice(true);
    }
  };

  return (
    <Card className="border-primary-200 bg-primary-50">
      <div className="flex items-start gap-3">
        <BellRing className="mt-0.5 h-5 w-5 shrink-0 text-primary-600" />
        <div className="flex-1">
          <h2 className="font-semibold text-gray-900">Set up notifications</h2>
          <p className="mt-1 text-sm text-gray-700">{devicesError || message}</p>
          {permission !== 'denied' && (
            <Button className="mt-3" size="sm" onClick={enableNotifications} disabled={loading}>
              {loading ? 'Enabling...' : 'Enable notifications'}
            </Button>
          )}
          {error && <p className="mt-2 text-sm text-red-600">{error}</p>}
        </div>
      </div>
    </Card>
  );
}
