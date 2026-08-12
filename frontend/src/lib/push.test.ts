import { beforeEach, describe, expect, it, vi } from 'vitest';
import { api } from './api';
import {
  registerServiceWorker,
  subscribeToPush,
  syncExistingPushSubscription,
  urlBase64ToUint8Array,
} from './push';

vi.mock('./api', () => ({
  api: {
    registerDevice: vi.fn(),
  },
}));

describe('subscribeToPush', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('reuses and registers an existing valid browser subscription', async () => {
    const existing = subscription('https://push.example.test/existing');
    const pushManager = {
      getSubscription: vi.fn().mockResolvedValue(existing),
      subscribe: vi.fn(),
    };

    await subscribeToPush(registration(pushManager));

    expect(existing.unsubscribe).not.toHaveBeenCalled();
    expect(pushManager.subscribe).not.toHaveBeenCalled();
    expect(api.registerDevice).toHaveBeenCalledWith(expect.objectContaining({
      endpoint: 'https://push.example.test/existing',
    }));
  });

  it('replaces a stale browser subscription before registering the device', async () => {
    const stale = subscription('https://push.example.test/stale');
    const replacement = subscription('https://push.example.test/replacement');
    const pushManager = {
      getSubscription: vi.fn().mockResolvedValue(stale),
      subscribe: vi.fn().mockResolvedValue(replacement),
    };

    await subscribeToPush(registration(pushManager), true);

    expect(stale.unsubscribe).toHaveBeenCalledOnce();
    expect(pushManager.subscribe).toHaveBeenCalledWith({
      userVisibleOnly: true,
      applicationServerKey: expect.any(Uint8Array),
    });
    expect(api.registerDevice).toHaveBeenCalledWith(expect.objectContaining({
      endpoint: 'https://push.example.test/replacement',
    }));
  });
});

describe('syncExistingPushSubscription', () => {
  it('re-registers this browser subscription without prompting or replacing it', async () => {
    const existing = subscription('https://push.example.test/current-phone');
    const pushManager = {
      getSubscription: vi.fn().mockResolvedValue(existing),
      subscribe: vi.fn(),
    };
    const readyRegistration = registration(pushManager);
    const pendingRegistration = {
      update: vi.fn().mockResolvedValue(undefined),
    };

    vi.stubGlobal('Notification', { permission: 'granted' });
    Object.defineProperty(navigator, 'serviceWorker', {
      configurable: true,
      value: {
        register: vi.fn().mockResolvedValue(pendingRegistration),
        ready: Promise.resolve(readyRegistration),
      },
    });

    await expect(syncExistingPushSubscription()).resolves.toBe(true);

    expect(existing.unsubscribe).not.toHaveBeenCalled();
    expect(pushManager.subscribe).not.toHaveBeenCalled();
    expect(api.registerDevice).toHaveBeenCalledWith(expect.objectContaining({
      endpoint: 'https://push.example.test/current-phone',
    }));
  });

  it('uses the active service worker after checking for an update', async () => {
    const readyRegistration = registration({
      getSubscription: vi.fn(),
      subscribe: vi.fn(),
    });
    const pendingRegistration = {
      update: vi.fn().mockResolvedValue(undefined),
    };
    const register = vi.fn().mockResolvedValue(pendingRegistration);

    Object.defineProperty(navigator, 'serviceWorker', {
      configurable: true,
      value: { register, ready: Promise.resolve(readyRegistration) },
    });

    await expect(registerServiceWorker()).resolves.toBe(readyRegistration);
    expect(register).toHaveBeenCalledWith('/sw.js', { updateViaCache: 'none' });
    expect(pendingRegistration.update).toHaveBeenCalledOnce();
  });
});

describe('urlBase64ToUint8Array', () => {
  it('converts URL-safe base64 values into browser push bytes', () => {
    expect(Array.from(urlBase64ToUint8Array('-_8'))).toEqual([251, 255]);
  });
});

function registration(pushManager: {
  getSubscription: ReturnType<typeof vi.fn>;
  subscribe: ReturnType<typeof vi.fn>;
}): ServiceWorkerRegistration {
  return { pushManager } as unknown as ServiceWorkerRegistration;
}

function subscription(endpoint: string): PushSubscription & { unsubscribe: ReturnType<typeof vi.fn> } {
  const keys: Record<string, ArrayBuffer> = {
    p256dh: Uint8Array.from([1, 2, 3]).buffer,
    auth: Uint8Array.from([4, 5, 6]).buffer,
  };
  return {
    endpoint,
    unsubscribe: vi.fn().mockResolvedValue(true),
    getKey: vi.fn((name: string) => keys[name] || null),
  } as unknown as PushSubscription & { unsubscribe: ReturnType<typeof vi.fn> };
}
