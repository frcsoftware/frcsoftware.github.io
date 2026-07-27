import { hashLockfile, STAMP, writeStamp } from './stamp.mjs';
import { existsSync, unlinkSync } from 'node:fs';

function updateHash(): void {
    const hash = hashLockfile();
    if (!hash) {
        // no lockfile for some reason, remove stale stamp if it exists
        if (existsSync(STAMP)) {
            unlinkSync(STAMP);
        }
        process.exit(0);
    }
    writeStamp(hash);
}
// main function. put other functions in here to run on postinstall. do not put loose code in main, make your own method
function main(): void {
    updateHash();
}

if (!process.env.CI) {
    main();
}
