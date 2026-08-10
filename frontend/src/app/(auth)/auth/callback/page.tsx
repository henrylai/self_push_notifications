'use client';

import { Suspense, useEffect, useRef, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { api } from '@/lib/api';
import { setToken, setStoredUser } from '@/lib/auth';
import { Loader2 } from 'lucide-react';

export default function AuthCallbackPage() {
  return (
    <Suspense fallback={<Loader2 className="h-8 w-8 animate-spin text-primary-600" />}>
      <AuthCallbackContent />
    </Suspense>
  );
}

function AuthCallbackContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [error, setError] = useState('');
  const handled = useRef(false);

  useEffect(() => {
    if (handled.current) return;
    handled.current = true;

    const token = searchParams.get('token');
    const code = searchParams.get('code');

    if (!token && !code) {
      setError('No token found in URL');
      return;
    }

    if (token) {
      api
        .verifyMagicLink(token)
        .then((res) => {
          setToken(res.token);
          setStoredUser(res.user);
          router.replace('/dashboard');
        })
        .catch(() => {
          setError('Invalid or expired magic link');
        });
      return;
    }

    const redirectUri = `${window.location.origin}/auth/callback`;
    api
      .googleLogin(code!, redirectUri)
      .then((res) => {
        setToken(res.token);
        setStoredUser(res.user);
        router.replace('/dashboard');
      })
      .catch((err: unknown) => {
        setError(err instanceof Error ? err.message : 'Google sign-in failed');
      });
  }, [searchParams, router]);

  if (error) {
    return (
      <div className="flex min-h-screen items-center justify-center px-4">
        <div className="text-center">
          <p className="mb-4 text-red-600">{error}</p>
          <a href="/login" className="text-primary-600 hover:underline">
            Back to login
          </a>
        </div>
      </div>
    );
  }

  return (
    <div className="flex min-h-screen items-center justify-center">
      <Loader2 className="h-8 w-8 animate-spin text-primary-600" />
    </div>
  );
}
