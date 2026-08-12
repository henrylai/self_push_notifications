'use client';

import { Bell, CalendarDays, Check, Gift, Heart, Star } from 'lucide-react';
import { cn } from '@/lib/utils';
import type { NotificationIcon } from '@/types';

interface NotificationIconPickerProps {
  value: NotificationIcon;
  onChange: (icon: NotificationIcon) => void;
}

const icons: Array<{ value: NotificationIcon; label: string; Icon: typeof Bell }> = [
  { value: 'bell', label: 'Bell', Icon: Bell },
  { value: 'heart', label: 'Heart', Icon: Heart },
  { value: 'star', label: 'Star', Icon: Star },
  { value: 'check', label: 'Check', Icon: Check },
  { value: 'calendar', label: 'Calendar', Icon: CalendarDays },
  { value: 'gift', label: 'Gift', Icon: Gift },
];

export default function NotificationIconPicker({ value, onChange }: NotificationIconPickerProps) {
  return (
    <fieldset>
      <legend className="mb-2 text-sm font-medium text-gray-700">Notification icon</legend>
      <div className="grid grid-cols-6 gap-2">
        {icons.map(({ value: icon, label, Icon }) => (
          <button
            key={icon}
            type="button"
            aria-label={label}
            aria-pressed={value === icon}
            onClick={() => onChange(icon)}
            className={cn(
              'flex aspect-square items-center justify-center rounded-lg border transition-colors',
              value === icon
                ? 'border-primary-600 bg-primary-50 text-primary-700 ring-2 ring-primary-200'
                : 'border-gray-200 bg-white text-gray-600 hover:border-primary-300 hover:text-primary-700'
            )}
          >
            <Icon className="h-5 w-5" aria-hidden="true" />
          </button>
        ))}
      </div>
    </fieldset>
  );
}
