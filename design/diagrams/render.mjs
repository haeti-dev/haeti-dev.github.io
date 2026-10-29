// 이 폴더(하위 폴더 포함)의 *.html을 라이트/다크 투명 배경 PNG(2배 해상도)로 굽는다.
// 출력 위치는 각 파일의 <meta name="out">(저장소 루트 기준 또는 절대 경로, 확장자 없이)이 정한다.
// templates/는 복사해서 쓰는 틀이라 굽지 않는다.
//
//   node design/diagrams/render.mjs                         모두
//   node design/diagrams/render.mjs flow posts/<글>/<그림>    이 폴더 기준 경로로 골라서
import { createRequire } from 'node:module';
import { readdirSync, mkdirSync } from 'node:fs';
import { dirname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';

const here = dirname(fileURLToPath(import.meta.url));
const root = resolve(here, '../..');
const { chromium } = createRequire(import.meta.url)('playwright');

const picked = process.argv.slice(2).map((name) => (name.endsWith('.html') ? name : `${name}.html`));
const all = readdirSync(here, { recursive: true })
  .filter((f) => f.endsWith('.html') && !f.startsWith('templates/') && !f.includes('node_modules'));
const files = picked.length > 0 ? picked : all;
const browser = await chromium.launch();
for (const file of files) {
  for (const theme of ['light', 'dark']) {
    const page = await browser.newPage({ viewport: { width: 1400, height: 1000 }, deviceScaleFactor: 2 });
    await page.goto(`file://${join(here, file)}?theme=${theme}`);
    await page.waitForSelector('body[data-ready="true"]');
    const out = await page.locator('meta[name="out"]').getAttribute('content');
    const target = resolve(root, `${out}-${theme}.png`);
    mkdirSync(dirname(target), { recursive: true });
    await page.locator('#diagram').screenshot({ path: target, omitBackground: true });
    console.log(`${file} ${theme} -> ${target}`);
    await page.close();
  }
}
await browser.close();
