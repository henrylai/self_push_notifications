'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import Button from '@/components/ui/button';
import Card from '@/components/ui/card';
import { Bell } from 'lucide-react';

export default function LoginPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);

  const handleGoogleLogin = () => {
    setLoading(true);
    // MVP placeholder — will integrate Google OAuth
    alert('Google login coming soon! For now, use email magic link.');
    setLoading(false);
  };

  return (
    <div className="flex min-h-screen items-center justify-center px-4">
      <Card className="w-full max-w-sm">
        <div className="mb-6 flex flex-col items-center gap-3">
          <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-primary-600">
            <Bell className="h-7 w-7 text-white" />
          </div>
          <h1 className="text-2xl font-bold text-gray-900">PushPal</h1>
          <p className="text-center text-sm text-gray-500">
            Scheduled push notifications for the people you care about
          </p>
        </div>

        <div className="flex flex-col gap-3">
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
