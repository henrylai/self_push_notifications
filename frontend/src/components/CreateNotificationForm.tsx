'use client';

import { useState, useEffect, type FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import { useQueryClient } from '@tanstack/react-query';
import Input from '@/components/ui/input';
import Textarea from '@/components/ui/textarea';
import NotificationIconPicker from '@/components/NotificationIconPicker';
import Button from '@/components/ui/button';
import { api } from '@/lib/api';
import { useToast } from '@/components/ui/toast';
import type { NotificationIcon, NotificationPage, Relationship } from '@/types';

export default function CreateNotificationForm() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { addToast } = useToast();
  const [relationships, setRelationships] = useState<Relationship[]>([]);
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [icon, setIcon] = useState<NotificationIcon>('bell');
  const [recipient, setRecipient] = useState('me');
  const [scheduledTime, setScheduledTime] = useState('');
  const [minimumScheduledTime, setMinimumScheduledTime] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [relationshipsError, setRelationshipsError] = useState('');

  useEffect(() => {
    api.getRelationships().then(setRelationships).catch((err: unknown) => {
      setRelationshipsError(err instanceof Error ? err.message : 'Unable to load linked Pals');
    });
  }, []);

  const linkedPals = relationships.filter((relationship) => relationship.status === 'ACCEPTED'
    && (relationship.palId || relationship.partnerId));
  const refreshMinimumScheduledTime = () => {
    const earliestSchedule = new Date(Date.now() + 60_000);
    earliestSchedule.setMinutes(
      earliestSchedule.getMinutes() - earliestSchedule.getTimezoneOffset()
    );
    setMinimumScheduledTime(earliestSchedule.toISOString().slice(0, 16));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);

    const selectedPal = linkedPals.find((pal) => pal.id === recipient);
    const recipientId = selectedPal?.palId || selectedPal?.partnerId || undefined;

    try {
      const notification = await api.createNotification({
        title,
        body: body || undefined,
        icon,
        recipientId,
        scheduledTime: new Date(scheduledTime).toISOString(),
      });
      queryClient.setQueryData<NotificationPage>(
        ['notifications', 0],
        (existing) => existing
          ? { ...existing, sent: [notification, ...existing.sent] }
          : existing
      );
      void queryClient.invalidateQueries({ queryKey: ['notifications'] });
      addToast('Reminder scheduled!');
      router.push('/dashboard');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create reminder');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <Input
        label="Title"
        placeholder="What's the reminder about?"
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        required
      />
      <Textarea
        label="Body (optional)"
        placeholder="Additional details..."
        value={body}
        onChange={(e) => setBody(e.target.value)}
        rows={3}
      />
      <NotificationIconPicker value={icon} onChange={setIcon} />
      <div className="w-full">
        <label htmlFor="recipient" className="mb-1 block text-sm font-medium text-gray-700">
          Recipient
        </label>
        <select
          id="recipient"
          value={recipient}
          onChange={(e) => setRecipient(e.target.value)}
          className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
        >
          <option value="me">Me</option>
          {linkedPals.map((pal) => (
            <option key={pal.id} value={pal.id}>
              {pal.palName || pal.partnerName || 'Unnamed Pal'}
            </option>
          ))}
        </select>
        {!linkedPals.length && (
          <p className="mt-1 text-xs text-gray-500">
            Link a Pal in Settings to send reminders to them.
          </p>
        )}
        {relationshipsError && <p className="mt-1 text-xs text-red-600">{relationshipsError}</p>}
      </div>
      <Input
        label="Date & Time"
        type="datetime-local"
        value={scheduledTime}
        onChange={(e) => setScheduledTime(e.target.value)}
        min={minimumScheduledTime || undefined}
        onFocus={refreshMinimumScheduledTime}
        required
      />
      {error && <p className="text-sm text-red-600">{error}</p>}
      <Button type="submit" size="lg" disabled={submitting}>
        {submitting ? 'Scheduling...' : 'Schedule Reminder'}
      </Button>
    </form>
  );
}
