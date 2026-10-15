// Logs in to the running app with the demo account and saves screenshots for the README.
// Usage: BASE_URL=http://localhost:4200 node screenshots.mjs
import { chromium } from 'playwright';
import { mkdir } from 'node:fs/promises';

const baseUrl = process.env.BASE_URL ?? 'http://localhost:4200';
const outDir = new URL('../docs/screenshots/', import.meta.url).pathname;

await mkdir(outDir, { recursive: true });
const browser = await chromium.launch();
const page = await browser.newPage({ viewport: { width: 1280, height: 800 } });

try {
  await page.goto(`${baseUrl}/login`);
  await page.waitForSelector('form');
  await page.screenshot({ path: `${outDir}login.png` });

  await page.fill('input[type=email]', 'demo@workorderhub.dev');
  await page.fill('input[type=password]', 'demo12345');
  await page.click('button[type=submit]');

  await page.waitForSelector('table tbody tr');
  await page.screenshot({ path: `${outDir}work-orders.png`, fullPage: true });

  await page.click('text=Camera inspection of sewer line');
  await page.waitForSelector('.actions button');
  await page.screenshot({ path: `${outDir}work-order-detail.png`, fullPage: true });

  console.log(`Screenshots saved to ${outDir}`);
} finally {
  await browser.close();
}
