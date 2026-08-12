'use client';

import { useState, useEffect } from 'react';
import Card from '@/components/ui/card';
import Button from '@/components/ui/button';
import Input from '@/components/ui/input';
import InviteCodeDisplay from '@/components/InviteCodeDisplay';
import { api } from '@/lib/api';
import { useAuth } from '@/hooks/useAuth';
import { useToast } from '@/components/ui/toast';
import { Link as LinkIcon } from 'lucide-react';
import type { Relationship } from '@/types';

export default function SettingsPage() {
  const { user, logout } = useAuth();
  const { addToast } = useToast();
  const [inviteCode, setInviteCode] = useState('');
  const [enterCode, setEnterCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [relationships, setRelationships] = useState<Relationship[]>([]);
  const [relationshipsError, setRelationshipsError] = useState('');
  const [removingPalId, setRemovingPalId] = useState<string | null>(null);

  useEffect(() => {
    api.getRelationships().then(setRelationships).catch((err: unknown) => {
      setRelationshipsError(err instanceof Error ? err.message : 'Unable to load linked Pals');
    });
  }, []);

  const handleGenerateInvite = async () => {
    setLoading(true);
    try {
      const res = await api.createInvite();
      setInviteCode(res.inviteCode);
    } catch (err) {
      addToast(err instanceof Error ? err.message : 'Failed to generate code', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleAcceptInvite = async () => {
    if (!enterCode.trim()) return;
    setLoading(true);
    try {
      await api.acceptInvite(enterCode.trim());
      addToast('Pal linked!');
      setEnterCode('');
      const rels = await api.getRelationships();
      setRelationships(rels);
      setRelationshipsError('');
    } catch (err) {
      addToast(err instanceof Error ? err.message : 'Invalid code', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleRemovePal = async (relationship: Relationship) => {
    const palName = relationship.palName || relationship.partnerName || 'this Pal';
    if (!window.confirm(`Remove ${palName}? Pending reminders between you will be cancelled.`)) {
      return;
    }

    setRemovingPalId(relationship.id);
    try {
      await api.removePal(relationship.id);
      setRelationships((current) => current.filter((pal) => pal.id !== relationship.id));
      addToast('Pal removed. Pending reminders were cancelled.');
    } catch (err) {
      addToast(err instanceof Error ? err.message : 'Unable to remove Pal', 'error');
    } finally {
      setRemovingPalId(null);
    }
  };

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-xl font-bold text-gray-900">Settings</h1>

      {/* User Info */}
      <Card>
        <h2 className="mb-2 font-semibold text-gray-900">Account</h2>
        <div className="text-sm text-gray-600">
          <p>
            <span className="font-medium">Name:</span> {user?.name || '—'}
          </p>
          <p>
            <span className="font-medium">Email:</span> {user?.email || '—'}
          </p>
        </div>
      </Card>

      {/* Pals */}
      <Card>
        <h2 className="mb-2 font-semibold text-gray-900">Linked Pals</h2>
        {relationshipsError ? (
          <p className="text-sm text-red-600">{relationshipsError}</p>
        ) : relationships.length === 0 ? (
          <p className="text-sm text-gray-500">No Pals linked yet.</p>
        ) : (
          relationships.map((r) => (
            <div key={r.id} className="flex items-center justify-between gap-3 py-1 text-sm text-gray-600">
              <div className="flex min-w-0 items-center gap-2">
                <LinkIcon className="h-4 w-4 shrink-0 text-primary-600" />
                <span className="truncate">
                  {r.palName || r.partnerName} ({r.palEmail || r.partnerEmail})
                </span>
              </div>
              <Button
                variant="danger"
                size="sm"
                onClick={() => handleRemovePal(r)}
                disabled={removingPalId === r.id}
              >
                {removingPalId === r.id ? 'Removing...' : 'Remove'}
              </Button>
            </div>
          ))
        )}
      </Card>

      {/* Invite Code */}
      <Card>
        <h2 className="mb-2 font-semibold text-gray-900">Invite Code</h2>
        {inviteCode ? (
          <InviteCodeDisplay code={inviteCode} />
        ) : (
          <Button onClick={handleGenerateInvite} disabled={loading}>
            {loading ? 'Generating...' : 'Generate Invite Code'}
          </Button>
        )}
      </Card>

      {/* Enter Invite Code */}
      <Card>
        <h2 className="mb-2 font-semibold text-gray-900">Link with a Pal</h2>
        <div className="flex gap-2">
          <Input
            placeholder="Enter a Pal's invite code"
            value={enterCode}
            onChange={(e) => setEnterCode(e.target.value.toUpperCase())}
            maxLength={6}
          />
          <Button onClick={handleAcceptInvite} disabled={loading || !enterCode.trim()}>
            Link
          </Button>
        </div>
      </Card>

      {/* Logout */}
      <Button variant="danger" size="lg" onClick={logout} className="w-full">
        Log Out
      </Button>
    </div>
  );
}
