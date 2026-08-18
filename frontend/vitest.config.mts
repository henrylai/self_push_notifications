import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    environment: 'jsdom',
    include: ['src/**/*.test.ts'],
    env: {
      NEXT_PUBLIC_VAPID_PUBLIC_KEY: 'BPL_PBpJFrFFD6A78a68wc8DlwFBsJBTwxOSTWp6uf50szrlQYjq-ECSsJQqv_7GPsbzKawn_EARmWHPa6hF1rc',
    },
  },
});
