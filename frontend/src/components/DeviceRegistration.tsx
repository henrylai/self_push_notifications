'use client';

import { useState, useEffect } from 'react';
import Button from '@/components/ui/button';
import Card from '@/components/ui/card';
import { usePush } from '@/hooks/usePush';
import { api } from '@/lib/api';
import type { Device } from '@/types';
import { useToast } from '@/components/ui/toast';

export default function DeviceRegistration() {
  const { permission, loading, error, register } = usePush();
  const [devices, setDevices] = useState<Device[]>([]);
  const [loadingDevices, setLoadingDevices] = useState(true);
  const [devicesError, setDevicesError] = useState('');
  const { addToast } = useToast();

  useEffect(() => {
    loadDevices();
  }, []);

  const loadDevices = async () => {
    try {
      const data = await api.getDevices();
      setDevices(data);
      setDevicesError('');
    } catch (err) {
      setDevicesError(err instanceof Error ? err.message : 'Unable to load devices');
    } finally {
      setLoadingDevices(false);
    }
  };

  const handleRegister = async () => {
    if (await register()) {
      addToast('Device registered');
      await loadDevices();
    }
  };

  const handleRemove = async (id: string) => {
    try {
      await api.removeDevice(id);
      setDevices((prev) => prev.filter((d) => d.id !== id));
      addToast('Device removed');
    } catch (err) {
      addToast(err instanceof Error ? err.message : 'Unable to remove device', 'error');
    }
  };

  return (
    <div className="flex flex-col gap-4">
      <Card>
        <h3 className="mb-2 font-semibold text-gray-900">Push Notification Status</h3>
        <p className="mb-4 text-sm text-gray-600">
          Status:{' '}
          <span
            className={
              permission === 'granted'
                ? 'text-green-600 font-medium'
                : permission === 'denied'
                ? 'text-red-600 font-medium'
                : 'text-yellow-600 font-medium'
            }
          >
            {permission === 'granted' ? 'Enabled' : permission === 'denied' ? 'Denied' : 'Not set'}
          </span>
        </p>
        <Button onClick={handleRegister} disabled={loading || permission === 'denied'}>
          {loading ? 'Registering...' : 'Register This Device'}
        </Button>
        {error && <p className="mt-2 text-sm text-red-600">{error}</p>}
      </Card>

      <div>
        <h3 className="mb-2 text-lg font-semibold text-gray-900">Registered Devices</h3>
        {loadingDevices ? (
          <p className="text-sm text-gray-500">Loading...</p>
        ) : devicesError ? (
          <div className="flex items-center gap-2">
            <p className="text-sm text-red-600">{devicesError}</p>
            <Button variant="secondary" size="sm" onClick={loadDevices}>Retry</Button>
          </div>
        ) : devices.length === 0 ? (
          <p className="text-sm text-gray-500">No devices registered.</p>
        ) : (
          <div className="flex flex-col gap-3">
            {devices.map((device) => (
              <Card key={device.id} className="flex items-center justify-between">
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-medium text-gray-900">
                    {device.userAgent || 'Unknown browser'}
                  </p>
                  <p className="text-xs text-gray-500">
                    Added {new Date(device.createdAt).toLocaleDateString()}
                  </p>
                </div>
                <Button variant="danger" size="sm" onClick={() => handleRemove(device.id)}>
                  Remove
                </Button>
              </Card>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
