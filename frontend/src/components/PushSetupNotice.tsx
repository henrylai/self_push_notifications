'use client';

import { useEffect, useState } from 'react';
import { BellRing } from 'lucide-react';
import { syncExistingPushSubscription } from '@/lib/push';
import { isIos, isStandalone } from '@/lib/pwa';
import { usePush } from '@/hooks/usePush';
import Button from '@/components/ui/button';
import Card from '@/components/ui/card';

export default function PushSetupNotice() {
  const { permission, loading, error, register } = usePush();
  const [hasRegisteredDevice, setHasRegisteredDevice] = useState<boolean | null>(null);
  const [devicesError, setDevicesError] = useState<string | null>(null);
  const [requiresIosInstall, setRequiresIosInstall] = useState(false);

  useEffect(() => {
    let active = true;
    if (isIos() && !isStandalone()) {
      setRequiresIosInstall(true);
      setHasRegisteredDevice(false);
      return () => {
        active = false;
      };
    }

    syncExistingPushSubscription()
      .then((isCurrentDeviceRegistered) => {
        if (active) setHasRegisteredDevice(isCurrentDeviceRegistered);
      })
      .catch(() => {
        if (active) setDevicesError('Unable to check notification setup.');
      });
    return () => {
      active = false;
    };
  }, []);

  if (hasRegisteredDevice || (hasRegisteredDevice === null && !devicesError)) return null;

  if (requiresIosInstall) {
    return (
      <Card className="border-primary-200 bg-primary-50">
        <div className="flex items-start gap-3">
          <BellRing className="mt-0.5 h-5 w-5 shrink-0 text-primary-600" />
          <div className="flex-1">
            <h2 className="font-semibold text-gray-900">Install PushPal to enable notifications</h2>
            <p className="mt-1 text-sm text-gray-700">
              On iPhone and iPad, notifications only work from the Home Screen app. Open
              your browser’s Share menu, choose <strong>Add to Home Screen</strong>, then open
              PushPal from its new icon and enable notifications there.
            </p>
          </div>
        </div>
      </Card>
    );
  }

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
