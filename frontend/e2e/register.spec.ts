import { test, expect } from '@playwright/test';
import { readVerificationToken } from './verification';

test.describe('Registration and email verification', () => {
  test('a new user can register, verify their email, and sign in', async ({ page }) => {
    const email = `e2e-register-${Date.now()}@mizan.test`;
    const password = 'E2ePassword123!';

    await page.goto('/register');
    await page.getByLabel('Email').fill(email);
    await page.getByLabel('Password', { exact: true }).fill(password);
    await page.getByLabel('Confirm password').fill(password);
    await page.getByLabel('I agree to the').check();
    await page.getByRole('button', { name: 'Create account' }).click();

    await expect(page.getByText('Check your email')).toBeVisible();

    const token = readVerificationToken(email);
    await page.goto(`/verify-email?token=${token}`);
    await expect(page.getByRole('heading', { name: 'Email verified' })).toBeVisible();

    await page.getByRole('link', { name: 'Sign in' }).click();
    await page.getByLabel('Email').fill(email);
    await page.getByLabel('Password', { exact: true }).fill(password);
    await page.getByRole('button', { name: 'Sign in' }).click();

    await page.waitForURL('**/dashboard');
    await expect(page.getByRole('button', { name: 'Logout' })).toBeVisible();
  });

  test('logging in before verifying is rejected', async ({ page }) => {
    const email = `e2e-unverified-${Date.now()}@mizan.test`;
    const password = 'E2ePassword123!';

    await page.goto('/register');
    await page.getByLabel('Email').fill(email);
    await page.getByLabel('Password', { exact: true }).fill(password);
    await page.getByLabel('Confirm password').fill(password);
    await page.getByLabel('I agree to the').check();
    await page.getByRole('button', { name: 'Create account' }).click();
    await expect(page.getByText('Check your email')).toBeVisible();

    await page.goto('/login');
    await page.getByLabel('Email').fill(email);
    await page.getByLabel('Password', { exact: true }).fill(password);
    await page.getByRole('button', { name: 'Sign in' }).click();

    await expect(page.getByText('Please verify your email before signing in.')).toBeVisible();
    await expect(page).toHaveURL(/\/login$/);
  });
});
