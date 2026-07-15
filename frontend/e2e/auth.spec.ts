import { test, expect } from '@playwright/test';
import { loginViaUi, seededUsers } from './helpers';

test.describe('Authentication', () => {
  test('a registered user can sign in and lands on the dashboard', async ({ page }) => {
    const { individual } = seededUsers();

    await loginViaUi(page, individual);

    await expect(page.getByText('Mizan.ma')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Logout' })).toBeVisible();
  });

  test('an invalid password is rejected with an error message', async ({ page }) => {
    const { individual } = seededUsers();

    await page.goto('/login');
    await page.getByLabel('Email').fill(individual.email);
    await page.getByLabel('Password').fill('wrong-password-entirely');
    await page.getByRole('button', { name: 'Sign in' }).click();

    await expect(page.getByText('Invalid email or password.')).toBeVisible();
    await expect(page).toHaveURL(/\/login$/);
  });
});
