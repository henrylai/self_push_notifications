'use client';

import Card from '@/components/ui/card';
import CreateNotificationForm from '@/components/CreateNotificationForm';

export default function NewNotificationPage() {
  return (
    <div className="flex flex-col gap-4">
      <h1 className="text-xl font-bold text-gray-900">New Reminder</h1>
      <Card>
        <CreateNotificationForm />
      </Card>
    </div>
  );
}
