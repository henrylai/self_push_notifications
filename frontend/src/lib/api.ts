import type { AuthResponse, Notification, Relationship } from '@/types';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const token = typeof window !== 'undefined' ? localStorage.getItem('token') : null;
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...((options?.headers as Record<string, string>) || {}),
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  const response = await fetch(`${API_BASE}${path}`, { ...options, headers });
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Request failed' }));
    throw new Error(error.message || `HTTP ${response.status}`);
  }
  return response.json();
}

export const api = {
  // Auth
  requestMagicLink: (email: string) =>
    request('/api/auth/magic-link', { method: 'POST', body: JSON.stringify({ email }) }),
  verifyMagicLink: (token: string) =>
    request<AuthResponse>(`/api/auth/magic-link/verify?token=${token}`),
  googleLogin: (code: string, redirectUri: string) =>
    request<AuthResponse>('/api/auth/google', {
      method: 'POST',
      body: JSON.stringify({ code, redirectUri }),
    }),

  // User
  getMe: () => request<any>('/api/users/me'),
  updateMe: (data: { name: string }) =>
    request<any>('/api/users/me', { method: 'PUT', body: JSON.stringify(data) }),

  // Relationships
  createInvite: () =>
    request<{ inviteCode: string }>('/api/relationships/invite', { method: 'POST' }),
  acceptInvite: (inviteCode: string) =>
    request<any>('/api/relationships/accept', { method: 'POST', body: JSON.stringify({ inviteCode }) }),
  getRelationships: () => request<Relationship[]>('/api/relationships'),

  // Devices
  registerDevice: (subscription: any) =>
    request<any>('/api/devices/register', { method: 'POST', body: JSON.stringify(subscription) }),
  removeDevice: (id: string) =>
    request<void>(`/api/devices/${id}`, { method: 'DELETE' }),
  getDevices: () => request<any[]>('/api/devices'),

  // Notifications
  createNotification: (data: any) =>
    request<any>('/api/notifications', { method: 'POST', body: JSON.stringify(data) }),
  getNotifications: () =>
    request<{ received: Notification[]; sent: Notification[] }>('/api/notifications'),
  getNotification: (id: string) => request<any>(`/api/notifications/${id}`),
  cancelNotification: (id: string) =>
    request<void>(`/api/notifications/${id}`, { method: 'DELETE' }),
  markViewed: (id: string) =>
    request<any>(`/api/notifications/${id}/viewed`, { method: 'POST' }),
  markDelivered: (id: string) =>
    request<any>(`/api/notifications/${id}/delivered`, { method: 'POST' }),
};
