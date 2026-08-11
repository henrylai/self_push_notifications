'use client';

import { useState, useEffect, type FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import Input from '@/components/ui/input';
import Textarea from '@/components/ui/textarea';
import Button from '@/components/ui/button';
import { api } from '@/lib/api';
import { useToast } from '@/components/ui/toast';
import type { Relationship } from '@/types';

export default function CreateNotificationForm() {
  const router = useRouter();
  const { addToast } = useToast();
  const [relationships, setRelationships] = useState<Relationship[]>([]);
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [recipient, setRecipient] = useState('me');
  const [scheduledTime, setScheduledTime] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [relationshipsError, setRelationshipsError] = useState('');

  useEffect(() => {
    api.getRelationships().then(setRelationships).catch((err: unknown) => {
      setRelationshipsError(err instanceof Error ? err.message : 'Unable to load linked partners');
    });
  }, []);

  const linkedPartner = relationships.find((r) => r.status === 'ACCEPTED' && r.partnerId);
  const earliestSchedule = new Date(Date.now() + 60_000);
  earliestSchedule.setMinutes(
    earliestSchedule.getMinutes() - earliestSchedule.getTimezoneOffset()
  );

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);

    const recipientId =
      recipient === 'partner' && linkedPartner ? linkedPartner.partnerId ?? undefined : undefined;

    try {
      await api.createNotification({
        title,
        body: body || undefined,
        recipientId,
        scheduledTime: new Date(scheduledTime).toISOString(),
      });
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
          <option value="partner" disabled={!linkedPartner}>
            {linkedPartner ? `Partner (${linkedPartner.partnerName})` : 'Partner (no partner linked)'}
          </option>
        </select>
        {!linkedPartner && (
          <p className="mt-1 text-xs text-gray-500">
            Link a partner in Settings to send reminders to them.
          </p>
        )}
        {relationshipsError && <p className="mt-1 text-xs text-red-600">{relationshipsError}</p>}
      </div>
      <Input
        label="Date & Time"
        type="datetime-local"
        value={scheduledTime}
        onChange={(e) => setScheduledTime(e.target.value)}
        min={earliestSchedule.toISOString().slice(0, 16)}
        required
      />
      {error && <p className="text-sm text-red-600">{error}</p>}
      <Button type="submit" size="lg" disabled={submitting}>
        {submitting ? 'Scheduling...' : 'Schedule Reminder'}
      </Button>
    </form>
  );
}
