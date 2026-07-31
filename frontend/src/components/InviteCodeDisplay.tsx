'use client';

import { useState } from 'react';
import Card from '@/components/ui/card';
import Button from '@/components/ui/button';
import { Copy, Check } from 'lucide-react';

interface InviteCodeDisplayProps {
  code: string;
}

export default function InviteCodeDisplay({ code }: InviteCodeDisplayProps) {
  const [copied, setCopied] = useState(false);

  const handleCopy = async () => {
    try {
      await navigator.clipboard.writeText(code);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      // fallback
      const textarea = document.createElement('textarea');
      textarea.value = code;
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand('copy');
      document.body.removeChild(textarea);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <Card>
      <p className="mb-2 text-sm text-gray-600">Your invite code:</p>
      <div className="flex items-center gap-3">
        <code className="flex-1 rounded-lg bg-gray-100 px-4 py-3 text-center text-2xl font-bold tracking-widest text-gray-900">
          {code}
        </code>
        <Button variant="secondary" size="sm" onClick={handleCopy}>
          {copied ? <Check size={16} /> : <Copy size={16} />}
        </Button>
      </div>
      <p className="mt-2 text-xs text-gray-500">
        Share this code with your partner so they can link their account.
      </p>
    </Card>
  );
}
