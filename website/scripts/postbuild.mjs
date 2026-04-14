/**
 * Post-build script that places the Astro server worker inside dist/client/
 * where Cloudflare Pages expects the build output to be.
 */
import { cpSync, mkdirSync, rmSync, existsSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';

const dist = 'dist';
const clientDir = join(dist, 'client');
const serverDir = join(dist, 'server');
const workerDir = join(clientDir, '_worker.js');

if (!existsSync(clientDir) || !existsSync(serverDir)) {
  console.log('[postbuild] Expected dist/client and dist/server not found, skipping.');
  process.exit(0);
}

// Step 1: Copy server files into dist/client/_worker.js/
console.log('[postbuild] Copying server output into dist/client/_worker.js/ ...');
mkdirSync(workerDir, { recursive: true });
cpSync(serverDir, workerDir, { recursive: true });

// Step 2: Create the entry point Cloudflare Pages expects
writeFileSync(join(workerDir, 'index.js'), `export { default } from './entry.mjs';\n`);

// Step 3: Clean up dist/server/ (no longer needed)
rmSync(serverDir, { recursive: true });

// Step 4: Delete the .wrangler/deploy/config.json redirect
const deployConfig = join('.wrangler', 'deploy', 'config.json');
if (existsSync(deployConfig)) {
  rmSync(deployConfig);
  console.log('[postbuild] Deleted stale .wrangler/deploy/config.json redirect');
}

console.log('[postbuild] Done! _worker.js placed inside dist/client/ for Cloudflare Pages.');
