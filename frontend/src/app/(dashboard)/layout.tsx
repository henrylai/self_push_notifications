'use client';

import { useEffect, useState } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import Link from 'next/link';
import { isAuthenticated, getToken, removeToken } from '@/lib/auth';
import { api } from '@/lib/api';
import { Bell, LayoutDashboard, PlusCircle, Settings, LogOut } from 'lucide-react';
import { cn } from '@/lib/utils';

const navItems = [
  { href: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { href: '/dashboard/new', label: 'New', icon: PlusCircle },
  { href: '/dashboard/settings', label: 'Settings', icon: Settings },
];

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const [userName, setUserName] = useState('');

  useEffect(() => {
    if (!isAuthenticated()) {
      router.replace('/login');
      return;
    }
    api.getMe().then((user) => setUserName(user.name)).catch(() => {
      removeToken();
      router.replace('/login');
    });
  }, [router]);

  const handleLogout = () => {
    removeToken();
    router.replace('/login');
  };

  return (
    <div className="flex min-h-screen flex-col">
      {/* Top Nav */}
      <header className="sticky top-0 z-40 border-b border-gray-200 bg-white">
        <div className="mx-auto flex h-14 max-w-2xl items-center justify-between px-4">
          <Link href="/dashboard" className="flex items-center gap-2">
            <Bell className="h-5 w-5 text-primary-600" />
            <span className="text-lg font-bold text-gray-900">PushPal</span>
          </Link>
          <div className="flex items-center gap-3">
            {userName && (
              <span className="text-sm text-gray-600 hidden sm:inline">{userName}</span>
            )}
            <button
              onClick={handleLogout}
              className="rounded-lg p-2 text-gray-500 hover:bg-gray-100"
              aria-label="Logout"
            >
              <LogOut className="h-5 w-5" />
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="mx-auto w-full max-w-2xl flex-1 px-4 py-6">{children}</main>

      {/* Bottom Nav (mobile) */}
      <nav className="sticky bottom-0 z-40 border-t border-gray-200 bg-white sm:hidden">
        <div className="flex items-center justify-around py-2">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive =
              item.href === '/dashboard'
                ? pathname === '/dashboard'
                : pathname.startsWith(item.href);
            return (
              <Link
                key={item.href}
                href={item.href}
                className={cn(
                  'flex flex-col items-center gap-0.5 px-3 py-1 text-xs',
                  isActive ? 'text-primary-600 font-medium' : 'text-gray-500'
                )}
              >
                <Icon className="h-5 w-5" />
                {item.label}
              </Link>
            );
          })}
        </div>
      </nav>
    </div>
  );
}
