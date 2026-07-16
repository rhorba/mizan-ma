import { execSync } from 'node:child_process';
import path from 'node:path';

const REPO_ROOT = path.resolve(__dirname, '../..');
const API_BASE_URL = process.env['E2E_API_BASE_URL'] ?? 'http://localhost:8085/api/v1';

function escapeRegExp(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

// Playwright can't read server-side logs directly, and the raw verification token is only
// ever stored hashed in the DB (same pattern as refresh tokens) — so the dev-only
// LOG_VERIFICATION_LINKS=true log line is the only place the plaintext token is observable.
export function readVerificationToken(email: string): string {
  const output = execSync('docker compose logs auth-service --no-color', {
    cwd: REPO_ROOT,
    encoding: 'utf-8',
    maxBuffer: 1024 * 1024 * 20,
  });
  const pattern = new RegExp(`verification link for ${escapeRegExp(email)}: \\S*[?&]token=(\\S+)`);
  const match = pattern.exec(output);
  if (!match) {
    throw new Error(
      `No verification link found in auth-service logs for ${email}. Is LOG_VERIFICATION_LINKS=true set?`,
    );
  }
  return match[1];
}

export async function verifyEmailViaApi(email: string): Promise<void> {
  const token = readVerificationToken(email);
  const response = await fetch(`${API_BASE_URL}/auth/verify-email`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ token }),
  });
  if (!response.ok) {
    throw new Error(`Verify-email failed for ${email}: ${response.status} ${await response.text()}`);
  }
}
