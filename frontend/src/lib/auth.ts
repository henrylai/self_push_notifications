export function getToken(): string | null {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem('token');
}

export function setToken(token: string): void {
  localStorage.setItem('token', token);
}

export function removeToken(): void {
  localStorage.removeItem('token');
}

export function removeStoredUser(): void {
  localStorage.removeItem('user');
}

export function isAuthenticated(): boolean {
  return getToken() !== null;
}

export function getStoredUser(): { id: string; email: string; name: string } | null {
  if (typeof window === 'undefined') return null;
  const raw = localStorage.getItem('user');
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function setStoredUser(user: { id: string; email: string; name: string }): void {
  localStorage.setItem('user', JSON.stringify(user));
}
