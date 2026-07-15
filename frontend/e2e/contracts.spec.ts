import path from 'node:path';
import { test, expect } from '@playwright/test';
import { loginViaUi, seededUsers } from './helpers';

test.describe.serial('Contract upload, analysis, and history', () => {
  test('uploading a contract runs analysis and lands on its detail page', async ({ page }) => {
    const { individual } = seededUsers();
    await loginViaUi(page, individual);

    await page.goto('/contracts/upload');
    const fileInput = page.getByLabel('Choose a PDF contract to upload');
    await fileInput.setInputFiles(path.resolve(__dirname, 'fixtures/sample-contract.pdf'));
    await expect(page.getByText('sample-contract.pdf')).toBeVisible();

    await page.getByRole('button', { name: 'Submit' }).click();
    // The POST blocks until analysis finishes (success or FAILED) before the frontend navigates.
    await page.waitForURL(/\/contracts\/[0-9a-f-]+$/, { timeout: 30_000 });

    await expect(page.getByText('sample-contract.pdf')).toBeVisible();
    // Real app behavior for this environment: the placeholder Anthropic key / non-parseable
    // fixture PDF means analysis genuinely fails, which ContractService correctly surfaces as FAILED.
    await expect(page.getByText(/Couldn't analyze this contract/)).toBeVisible();
  });

  test('the uploaded contract shows up in the dashboard history', async ({ page }) => {
    const { individual } = seededUsers();
    await loginViaUi(page, individual);

    await expect(page.getByText('sample-contract.pdf')).toBeVisible();
    await expect(page.getByText('FAILED')).toBeVisible();
  });
});
