'use client';

import { useEffect, useState } from 'react';
import { Download } from 'lucide-react';
import Button from '@/components/ui/button';
import { registerSW } from '@/lib/pwa';

interface BeforeInstallPromptEvent extends Event {
  prompt: () => Promise<void>;
  userChoice: Promise<{ outcome: 'accepted' | 'dismissed' }>;
}

export default function PwaSetup() {
  const [installPrompt, setInstallPrompt] = useState<BeforeInstallPromptEvent | null>(null);

  useEffect(() => {
    void registerSW();

    const handleInstallPrompt = (event: Event) => {
      event.preventDefault();
      setInstallPrompt(event as BeforeInstallPromptEvent);
    };
    const handleInstalled = () => setInstallPrompt(null);

    window.addEventListener('beforeinstallprompt', handleInstallPrompt);
    window.addEventListener('appinstalled', handleInstalled);
    return () => {
      window.removeEventListener('beforeinstallprompt', handleInstallPrompt);
      window.removeEventListener('appinstalled', handleInstalled);
    };
  }, []);

  if (!installPrompt) return null;

  const install = async () => {
    await installPrompt.prompt();
    await installPrompt.userChoice;
    setInstallPrompt(null);
  };

  return (
    <div className="fixed bottom-4 left-1/2 z-50 w-[calc(100%-2rem)] max-w-sm -translate-x-1/2 rounded-xl border border-gray-200 bg-white p-3 shadow-lg sm:left-auto sm:right-4 sm:translate-x-0">
      <div className="flex items-center justify-between gap-3">
        <p className="text-sm font-medium text-gray-800">Install PushPal for quicker access.</p>
        <Button size="sm" onClick={install}>
          <Download className="mr-1 h-4 w-4" />
          Install
        </Button>
      </div>
    </div>
  );
}
