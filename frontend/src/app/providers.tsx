'use client';

import { AuthProvider } from '@/hooks/useAuth';
import { ToastProvider } from '@/components/ui/toast';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useState, type ReactNode } from 'react';
import PwaSetup from '@/components/PwaSetup';

export default function Providers({ children }: { children: ReactNode }) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 30_000,
            refetchOnWindowFocus: false,
          },
        },
      })
  );

  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ToastProvider>
          {children}
          <PwaSetup />
        </ToastProvider>
      </AuthProvider>
    </QueryClientProvider>
  );
}
