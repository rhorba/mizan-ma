import { execSync } from 'node:child_process';
import { writeFileSync } from 'node:fs';
import path from 'node:path';
import { verifyEmailViaApi } from './verification';

const API_BASE_URL = process.env['E2E_API_BASE_URL'] ?? 'http://localhost:8085/api/v1';
const RUN_ID = Date.now();

export interface TestUser {
  email: string;
  password: string;
}

interface SeededUsers {
  individual: TestUser;
  admin: TestUser;
}

async function register(email: string, password: string): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password, role: 'INDIVIDUAL' }),
  });
  if (!response.ok) {
    throw new Error(`Register failed for ${email}: ${response.status} ${await response.text()}`);
  }
}

function promoteToAdmin(email: string): void {
  // Public registration can never create an ADMIN (RegistrableRole deliberately excludes it),
  // so the only way to get an admin test account is a direct SQL promotion against the local stack.
  const sql = `UPDATE users SET role = 'ADMIN' WHERE email = '${email}';`;
  execSync(`docker compose exec -T postgres psql -U mizan_user -d auth_db -c "${sql}"`, {
    cwd: path.resolve(__dirname, '../..'),
    stdio: 'pipe',
  });
}

export default async function globalSetup(): Promise<void> {
  const individual: TestUser = { email: `e2e-user-${RUN_ID}@mizan.test`, password: 'E2ePassword123!' };
  const admin: TestUser = { email: `e2e-admin-${RUN_ID}@mizan.test`, password: 'E2ePassword123!' };

  await register(individual.email, individual.password);
  await verifyEmailViaApi(individual.email);
  await register(admin.email, admin.password);
  await verifyEmailViaApi(admin.email);
  promoteToAdmin(admin.email);

  const seeded: SeededUsers = { individual, admin };
  writeFileSync(path.resolve(__dirname, '.test-users.json'), JSON.stringify(seeded, null, 2));
}
