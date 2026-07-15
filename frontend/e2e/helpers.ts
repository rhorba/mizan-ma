import { readFileSync } from 'node:fs';
import path from 'node:path';
import type { Page } from '@playwright/test';
import type { TestUser } from './global-setup';

export interface SeededUsers {
  individual: TestUser;
  admin: TestUser;
}

export function seededUsers(): SeededUsers {
  const raw = readFileSync(path.resolve(__dirname, '.test-users.json'), 'utf-8');
  return JSON.parse(raw) as SeededUsers;
}

export async function loginViaUi(page: Page, user: TestUser): Promise<void> {
  await page.goto('/login');
  await page.getByLabel('Email').fill(user.email);
  await page.getByLabel('Password').fill(user.password);
  await page.getByRole('button', { name: 'Sign in' }).click();
  await page.waitForURL('**/dashboard');
}
