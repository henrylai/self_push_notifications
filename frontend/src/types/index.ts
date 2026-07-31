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
}

export type NotificationStatus = 'PENDING' | 'SENT' | 'DELIVERED' | 'VIEWED' | 'FAILED' | 'CANCELLED';

export interface Device {
  id: string;
  userAgent: string;
  createdAt: string;
  lastUsedAt: string;
}

export interface Relationship {
  id: string;
  partnerName: string;
  partnerEmail: string;
  status: string;
}

export interface AuthResponse {
  token: string;
  user: User;
}
