const apiUrl = process.env.NEXT_PUBLIC_API_URL;
const vapidPublicKey = process.env.NEXT_PUBLIC_VAPID_PUBLIC_KEY;
const errors = [];

if (!apiUrl) {
  errors.push('NEXT_PUBLIC_API_URL is required');
} else {
  try {
    const parsed = new URL(apiUrl);
    const localDevelopment = parsed.hostname === 'localhost' || parsed.hostname === '127.0.0.1';
    if (parsed.protocol !== 'https:' && !localDevelopment) {
      errors.push('NEXT_PUBLIC_API_URL must use HTTPS outside local development');
    }
  } catch {
    errors.push('NEXT_PUBLIC_API_URL must be an absolute URL');
  }
}

if (!vapidPublicKey) {
  errors.push('NEXT_PUBLIC_VAPID_PUBLIC_KEY is required');
}

if (errors.length > 0) {
  console.error(`Invalid frontend build configuration:\n- ${errors.join('\n- ')}`);
  process.exit(1);
}
