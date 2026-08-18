'use client';

import { useState } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import Button from '@/components/ui/button';
import Card from '@/components/ui/card';

export default function LoginPage() {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleGoogleLogin = () => {
    const clientId = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID;
    if (!clientId) {
      setError('Google login is not configured yet. Please use email magic link.');
      return;
    }
    setError('');
    setLoading(true);
    const redirectUri = `${window.location.origin}/auth/callback`;
    const stateBytes = crypto.getRandomValues(new Uint8Array(32));
    const state = Array.from(stateBytes, (byte) => byte.toString(16).padStart(2, '0')).join('');
    sessionStorage.setItem('pushpal_oauth_state', state);
    const params = new URLSearchParams({
      client_id: clientId,
      redirect_uri: redirectUri,
      response_type: 'code',
      scope: 'openid email profile',
      prompt: 'select_account',
      state,
    });
    window.location.href = `https://accounts.google.com/o/oauth2/v2/auth?${params.toString()}`;
  };

  return (
    <div className="flex min-h-screen items-center justify-center px-4">
      <Card className="w-full max-w-sm">
        <div className="mb-6 flex flex-col items-center gap-3">
          <Image
            src="/icons/icon-192.png"
            alt=""
            width={56}
            height={56}
            className="h-14 w-14 rounded-2xl"
            priority
          />
          <h1 className="text-2xl font-bold text-gray-900">PushPal</h1>
          <p className="text-center text-sm text-gray-500">
            Scheduled push notifications for the people you care about
          </p>
        </div>

        <div className="flex flex-col gap-3">
          {error && <p role="alert" className="text-center text-sm text-red-600">{error}</p>}
          <Button
            variant="secondary"
            size="lg"
            onClick={handleGoogleLogin}
            disabled={loading}
            className="w-full"
          >
            Sign in with Google
          </Button>
          <Link href="/magic-link" className="w-full">
            <Button variant="primary" size="lg" className="w-full">
              Sign in with Email
            </Button>
          </Link>
        </div>
      </Card>
    </div>
  );
}
