import type {
  AuthResponse,
  CreateNotificationInput,
  Device,
  MessageResponse,
  Notification,
  RegisterDeviceInput,
  Relationship,
  User,
} from '@/types';

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
    const error: unknown = await response.json().catch(() => null);
    const message =
      typeof error === 'object' && error !== null && 'message' in error
        ? String(error.message)
        : `Request failed (HTTP ${response.status})`;
    throw new Error(message);
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export const api = {
  // Auth
  requestMagicLink: (email: string) =>
    request<MessageResponse>('/api/auth/magic-link', {
      method: 'POST',
      body: JSON.stringify({ email }),
    }),
  verifyMagicLink: (token: string) =>
    request<AuthResponse>('/api/auth/magic-link/verify', {
      method: 'POST',
      body: JSON.stringify({ token }),
    }),
  googleLogin: (code: string, redirectUri: string) =>
    request<AuthResponse>('/api/auth/google', {
      method: 'POST',
      body: JSON.stringify({ code, redirectUri }),
    }),

  // User
  getMe: () => request<User>('/api/users/me'),
  updateMe: (data: { name: string }) =>
    request<User>('/api/users/me', { method: 'PUT', body: JSON.stringify(data) }),

  // Relationships
  createInvite: () =>
    request<{ inviteCode: string }>('/api/relationships/invite', { method: 'POST' }),
  acceptInvite: (inviteCode: string) =>
    request<MessageResponse>('/api/relationships/accept', {
      method: 'POST',
      body: JSON.stringify({ inviteCode }),
    }),
  getRelationships: () => request<Relationship[]>('/api/relationships'),

  // Devices
  registerDevice: (subscription: RegisterDeviceInput) =>
    request<MessageResponse>('/api/devices/register', {
      method: 'POST',
      body: JSON.stringify(subscription),
    }),
  removeDevice: (id: string) =>
    request<MessageResponse>(`/api/devices/${id}`, { method: 'DELETE' }),
  getDevices: () => request<Device[]>('/api/devices'),

  // Notifications
  createNotification: (data: CreateNotificationInput) =>
    request<Notification>('/api/notifications', {
      method: 'POST',
      body: JSON.stringify(data),
    }),
  getNotifications: () =>
    request<{ received: Notification[]; sent: Notification[] }>('/api/notifications'),
  getNotification: (id: string) => request<Notification>(`/api/notifications/${id}`),
  cancelNotification: (id: string) =>
    request<MessageResponse>(`/api/notifications/${id}`, { method: 'DELETE' }),
  markViewed: (id: string) =>
    request<Notification>(`/api/notifications/${id}/viewed`, { method: 'POST' }),
};
