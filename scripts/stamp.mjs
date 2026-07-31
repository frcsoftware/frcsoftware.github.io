import { createHash } from 'node:crypto';
import { existsSync, readFileSync, writeFileSync } from 'node:fs';
import { resolve } from 'node:path';

export const LOCKFILE = resolve(process.cwd(), 'pnpm-lock.yaml');
export const STAMP = resolve(process.cwd(), 'node_modules', '.checksum.sha1');

/**
 * @returns {string | null} the hash or null if the lockfile doesn't exist
 */
export function hashLockfile() {
    if (!existsSync(LOCKFILE)) return null;
    const contents = readFileSync(LOCKFILE);
    return createHash('sha1').update(contents).digest('hex');
}
/**
 * @returns {string} the hash from the stamp file, or an empty string if it doesn't exist
 */
export function readStamp() {
    return existsSync(STAMP) ? readFileSync(STAMP, 'utf-8').trim() : '';
}
/**
 * @param {string} hash
 */
export function writeStamp(hash) {
    writeFileSync(STAMP, hash, 'utf-8');
}
