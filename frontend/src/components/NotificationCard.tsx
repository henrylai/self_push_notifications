'use client';

import { useState } from 'react';
import Card from '@/components/ui/card';
import Badge from '@/components/ui/badge';
import Button from '@/components/ui/button';
import { formatDate } from '@/lib/utils';
import { api } from '@/lib/api';
import { useToast } from '@/components/ui/toast';
import type { Notification } from '@/types';

interface NotificationCardProps {
  notification: Notification;
  currentUserId: string;
  onCancel?: () => void;
}

export default function NotificationCard({ notification, currentUserId, onCancel }: NotificationCardProps) {
  const [cancelling, setCancelling] = useState(false);
  const { addToast } = useToast();

  const isSender = notification.senderId === currentUserId;
  const isSelfReminder = notification.recipientId === currentUserId
    && notification.senderId === currentUserId;
  const canCancel = notification.status === 'PENDING' && isSender;

  const handleCancel = async () => {
    setCancelling(true);
    try {
      await api.cancelNotification(notification.id);
      addToast('Reminder cancelled');
      onCancel?.();
    } catch (err) {
      addToast(err instanceof Error ? err.message : 'Failed to cancel', 'error');
    } finally {
      setCancelling(false);
    }
  };

  return (
    <Card className="flex flex-col gap-2">
      <div className="flex items-start justify-between gap-2">
        <h3 className="font-semibold text-gray-900">{notification.title}</h3>
        <Badge status={notification.status} />
      </div>
      {notification.body && (
        <p className="text-sm text-gray-600">{notification.body}</p>
      )}
      {notification.failureReason && (
        <p className="text-sm text-red-600">
          {notification.status === 'FAILED' ? 'Delivery failed' : 'Delivery retry scheduled'}:
          {' '}{notification.failureReason}
        </p>
      )}
      <div className="flex items-center justify-between text-xs text-gray-500">
        <span>
          {isSelfReminder
            ? 'For: Me'
            : isSender
            ? `To: ${notification.recipientName || 'Pal'}`
            : `From: ${notification.senderName || 'Pal'}`}
        </span>
        <span>{formatDate(notification.scheduledTime)}</span>
      </div>
      {canCancel && (
        <div className="mt-2">
          <Button variant="danger" size="sm" onClick={handleCancel} disabled={cancelling}>
            {cancelling ? 'Cancelling...' : 'Cancel'}
          </Button>
        </div>
      )}
    </Card>
  );
}
