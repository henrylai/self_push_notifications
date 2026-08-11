export interface User {
  id: string;
  email: string;
  name: string;
}

export interface Notification {
  id: string;
  senderId: string | null;
  recipientId: string;
  senderName?: string;
  recipientName?: string;
  title: string;
  body: string | null;
  scheduledTime: string;
  status: NotificationStatus;
  createdAt: string;
  sentAt: string | null;
  deliveredAt: string | null;
  viewedAt: string | null;
  failureReason: string | null;
}

export type NotificationStatus = 'PENDING' | 'SENT' | 'DELIVERED' | 'VIEWED' | 'FAILED' | 'CANCELLED';

export interface Device {
  id: string;
  userAgent: string | null;
  createdAt: string;
  lastUsedAt: string;
}

export interface Relationship {
  id: string;
  partnerId: string | null;
  partnerName: string | null;
  partnerEmail: string | null;
  status: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface RegisterDeviceInput {
  endpoint: string;
  p256dh: string;
  auth: string;
  userAgent?: string;
}

export interface CreateNotificationInput {
  title: string;
  body?: string;
  recipientId?: string;
  scheduledTime: string;
}

export interface MessageResponse {
  message: string;
}
