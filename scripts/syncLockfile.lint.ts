// Script to check if pnpm-lock.yaml is in sync with package.json.
import { exec } from 'node:child_process';
import { promisify } from 'node:util';

await promisify(exec)(
    // check if lockfile is in sync, don't run scripts, don't modify node_modules or lockfile
    'pnpm install --frozen-lockfile --lockfile-only --ignore-scripts',
).catch(() => {
    console.error(
        'pnpm-lock.yaml is out of sync with package.json. Run `pnpm install` to update the lockfile.',
    );
    process.exit(1);
});
