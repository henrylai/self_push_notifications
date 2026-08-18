'use client';

import { useState, useCallback, useEffect, createContext, useContext, type ReactNode } from 'react';
import { useRouter } from 'next/navigation';
import {
  getToken,
  setToken,
  removeToken,
  getStoredUser,
  setStoredUser,
  removeStoredUser,
} from '@/lib/auth';
import type { User } from '@/types';
import { disableCurrentPushSubscription } from '@/lib/push';

interface AuthContextValue {
  user: User | null;
  token: string | null;
  loading: boolean;
  login: (token: string, user: User) => void;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setAuthToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const timer = window.setTimeout(() => {
      const storedToken = getToken();
      const storedUser = getStoredUser();
      if (storedToken) setAuthToken(storedToken);
      if (storedUser) setUser(storedUser);
      setLoading(false);
    }, 0);
    return () => window.clearTimeout(timer);
  }, []);

  const login = useCallback((newToken: string, newUser: User) => {
    setToken(newToken);
    setStoredUser(newUser);
    setAuthToken(newToken);
    setUser(newUser);
  }, []);

  const logout = useCallback(async () => {
    try {
      await disableCurrentPushSubscription();
    } catch {
      // Unsubscribing locally still prevents future delivery if the API is unavailable.
    } finally {
      removeToken();
      removeStoredUser();
      setAuthToken(null);
      setUser(null);
      router.replace('/login');
    }
  }, [router]);

  return (
    <AuthContext.Provider value={{ user, token, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}
