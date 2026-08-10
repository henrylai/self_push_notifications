'use client';

import { Suspense, useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useQuery } from '@tanstack/react-query';
import Link from 'next/link';
import { api } from '@/lib/api';
import NotificationCard from '@/components/NotificationCard';
import Button from '@/components/ui/button';
import { PlusCircle, Inbox } from 'lucide-react';
import { cn } from '@/lib/utils';
import type { Notification } from '@/types';

export default function DashboardPage() {
  return (
    <Suspense
      fallback={<div className="py-12 text-center text-sm text-gray-500">Loading...</div>}
    >
      <DashboardContent />
    </Suspense>
  );
}

function DashboardContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [tab, setTab] = useState<'sent' | 'received'>('sent');
  const { data: notifications, isLoading, refetch } = useQuery<{
    received: Notification[];
    sent: Notification[];
  }>({
    queryKey: ['notifications'],
    queryFn: api.getNotifications,
  });

  const { data: user } = useQuery({
    queryKey: ['me'],
    queryFn: api.getMe,
  });

  const viewedId = searchParams.get('viewed');

  useEffect(() => {
    if (!viewedId) return;
    api.markViewed(viewedId).catch(() => {});
    const url = new URL(window.location.href);
    url.searchParams.delete('viewed');
    router.replace(url.pathname + url.search);
  }, [viewedId, router]);

  const filtered = notifications
    ? [...notifications.sent, ...notifications.received].filter((n) =>
        tab === 'sent' ? n.senderId === user?.id : n.recipientId === user?.id
      )
    : undefined;

  return (
    <div className="flex flex-col gap-4">
      {/* Tabs */}
      <div className="flex rounded-lg bg-gray-100 p-1">
        {(['sent', 'received'] as const).map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={cn(
              'flex-1 rounded-md py-2 text-sm font-medium transition-colors',
              tab === t ? 'bg-white text-gray-900 shadow-sm' : 'text-gray-500 hover:text-gray-700'
            )}
          >
            {t.charAt(0).toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>

      {/* Content */}
      {isLoading ? (
        <div className="py-12 text-center text-sm text-gray-500">Loading...</div>
      ) : !filtered || filtered.length === 0 ? (
        <div className="flex flex-col items-center gap-3 py-16 text-center">
          <Inbox className="h-12 w-12 text-gray-300" />
          <p className="text-gray-500">No reminders yet. Create your first one!</p>
          <Link href="/dashboard/new">
            <Button>
              <PlusCircle className="mr-2 h-4 w-4" />
              New Reminder
            </Button>
          </Link>
        </div>
      ) : (
        <div className="flex flex-col gap-3">
          {filtered.map((n) => (
            <NotificationCard
              key={n.id}
              notification={n}
              currentUserId={user?.id}
              onCancel={() => refetch()}
            />
          ))}
        </div>
      )}

      {/* FAB */}
      <Link href="/dashboard/new" className="fixed bottom-20 right-4 sm:bottom-6 sm:right-6">
        <div className="flex h-14 w-14 items-center justify-center rounded-full bg-primary-600 text-white shadow-lg hover:bg-primary-700 transition-colors">
          <PlusCircle className="h-6 w-6" />
        </div>
      </Link>
    </div>
  );
}
