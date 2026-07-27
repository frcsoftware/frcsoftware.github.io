#!/usr/bin/env node
// scripts/post-checkout.ts
//
// Cross-platform pnpm sync check for git's post-checkout hook.
// Git calls this with three args: <prevHead> <newHead> <isBranchCheckout>

import { execSync } from 'node:child_process';
import { hashLockfile, readStamp } from './stamp.mjs';

function main() {
    const [, , , isBranchCheckoutArg] = process.argv;

    if (isBranchCheckoutArg !== '1') {
        process.exit(0);
    }

    const currentHash = hashLockfile();
    if (!currentHash) {
        process.exit(0);
    }

    if (currentHash === readStamp()) {
        process.exit(0);
    }

    console.log('lockfile changed since last checkout, running `pnpm install`');

    try {
        execSync('pnpm install', { stdio: 'inherit' });
    } catch {
        console.error('pnpm install failed. retry manually.');
        process.exit(1);
    }
}

if (!process.env.CI) {
    main();
}
