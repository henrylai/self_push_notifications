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
  palId: string | null;
  palName: string | null;
  palEmail: string | null;
  status: string;
  /** @deprecated Compatibility fields for an older API deployment. */
  partnerId?: string | null;
  /** @deprecated Compatibility fields for an older API deployment. */
  partnerName?: string | null;
  /** @deprecated Compatibility fields for an older API deployment. */
  partnerEmail?: string | null;
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
