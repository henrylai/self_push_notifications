'use client';

import DeviceRegistration from '@/components/DeviceRegistration';

export default function DevicesPage() {
  return (
    <div className="flex flex-col gap-4">
      <h1 className="text-xl font-bold text-gray-900">Devices</h1>
      <DeviceRegistration />
    </div>
  );
}
