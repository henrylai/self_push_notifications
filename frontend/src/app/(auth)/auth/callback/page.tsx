'use client';

import { Suspense, useEffect, useRef, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { api } from '@/lib/api';
import { useAuth } from '@/hooks/useAuth';
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
  const { login } = useAuth();
  const [asyncError, setAsyncError] = useState('');
  const [credentials] = useState(() => ({
    token: searchParams.get('token'),
    code: searchParams.get('code'),
    state: searchParams.get('state'),
    oauthError: searchParams.get('error_description') || searchParams.get('error'),
  }));
  const handled = useRef(false);
  const { token, code, state, oauthError } = credentials;
  const requestError = oauthError || (!token && !code
    ? 'No sign-in credentials were provided'
    : '');
  const error = requestError || asyncError;

  useEffect(() => {
    if (handled.current) return;
    handled.current = true;

    window.history.replaceState(null, '', '/auth/callback');

    if (requestError) {
      sessionStorage.removeItem('pushpal_oauth_state');
      return;
    }

    if (token) {
      api
        .verifyMagicLink(token)
        .then((res) => {
          login(res.token, res.user);
          router.replace('/dashboard');
        })
        .catch(() => {
          setAsyncError('Invalid or expired magic link');
        });
      return;
    }

    const expectedState = sessionStorage.getItem('pushpal_oauth_state');
    sessionStorage.removeItem('pushpal_oauth_state');
    if (!state || !expectedState || state !== expectedState) {
      Promise.resolve().then(() => {
        setAsyncError('Google sign-in could not be verified. Please try again.');
      });
      return;
    }

    const redirectUri = `${window.location.origin}/auth/callback`;
    api
      .googleLogin(code!, redirectUri)
      .then((res) => {
        login(res.token, res.user);
        router.replace('/dashboard');
      })
      .catch((err: unknown) => {
        setAsyncError(err instanceof Error ? err.message : 'Google sign-in failed');
      });
  }, [code, login, requestError, router, state, token]);

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
