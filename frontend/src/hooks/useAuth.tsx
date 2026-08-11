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

interface AuthContextValue {
  user: User | null;
  token: string | null;
  loading: boolean;
  login: (token: string, user: User) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setAuthToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    const storedToken = getToken();
    const storedUser = getStoredUser();
    if (storedToken) setAuthToken(storedToken);
    if (storedUser) setUser(storedUser);
    setLoading(false);
  }, []);

  const login = useCallback((newToken: string, newUser: User) => {
    setToken(newToken);
    setStoredUser(newUser);
    setAuthToken(newToken);
    setUser(newUser);
  }, []);

  const logout = useCallback(() => {
    removeToken();
    removeStoredUser();
    setAuthToken(null);
    setUser(null);
    router.replace('/login');
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
