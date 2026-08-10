'use client';

import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import type { Notification } from '@/types';

export function useNotifications() {
  return useQuery<{ received: Notification[]; sent: Notification[] }>({
    queryKey: ['notifications'],
    queryFn: api.getNotifications,
  });
}
