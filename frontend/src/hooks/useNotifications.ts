'use client';

import { useQuery } from '@tanstack/react-query';
import { api } from '@/lib/api';
import type { NotificationPage } from '@/types';

export function useNotifications(page = 0) {
  return useQuery<NotificationPage>({
    queryKey: ['notifications', page],
    queryFn: () => api.getNotifications(page),
  });
}
