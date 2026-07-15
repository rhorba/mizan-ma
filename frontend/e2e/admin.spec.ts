import { test, expect } from '@playwright/test';
import { loginViaUi, seededUsers } from './helpers';

test.describe('Admin stats', () => {
  test('an admin can view usage and flag stats', async ({ page }) => {
    const { admin } = seededUsers();
    await loginViaUi(page, admin);

    await page.goto('/admin/stats');

    await expect(page.getByText('Admin — Usage & Flag Stats')).toBeVisible();
    await expect(page.getByText('Contracts by Status')).toBeVisible();
    await expect(page.getByText('Flagged Clauses by Risk Level')).toBeVisible();
  });
});
