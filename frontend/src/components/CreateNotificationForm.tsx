'use client';

import { useState, type FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import Input from '@/components/ui/input';
import Textarea from '@/components/ui/textarea';
import Button from '@/components/ui/button';
import { api } from '@/lib/api';
import { useToast } from '@/components/ui/toast';

export default function CreateNotificationForm() {
  const router = useRouter();
  const { addToast } = useToast();
  const [title, setTitle] = useState('');
  const [body, setBody] = useState('');
  const [recipient, setRecipient] = useState('me');
  const [scheduledTime, setScheduledTime] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);

    try {
      await api.createNotification({
        title,
        body: body || undefined,
        recipientId: recipient,
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
          <option value="partner">Partner</option>
        </select>
      </div>
      <Input
        label="Date & Time"
        type="datetime-local"
        value={scheduledTime}
        onChange={(e) => setScheduledTime(e.target.value)}
        required
      />
      {error && <p className="text-sm text-red-600">{error}</p>}
      <Button type="submit" size="lg" disabled={submitting}>
        {submitting ? 'Scheduling...' : 'Schedule Reminder'}
      </Button>
    </form>
  );
}
