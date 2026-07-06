import { writeFileSync } from 'fs';
import { resolve } from 'path';
import { fileURLToPath } from 'url';
import { glossaryTerms } from '../src/data/glossary';

const ROOT = fileURLToPath(new URL('..', import.meta.url));
const OUTPUT = resolve(ROOT, '.styles/config/ignore/vale.txt');

const terms = [...new Set(glossaryTerms.map(({ term }) => term))].sort(
    (a, b) => a.toLowerCase().localeCompare(b.toLowerCase()),
);

const content = terms.join('\n') + '\n';
writeFileSync(OUTPUT, content);

console.log(`Wrote ${terms.length} glossary terms to ${OUTPUT}.`);
