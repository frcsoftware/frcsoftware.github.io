import { createWriteStream, writeFileSync, existsSync, mkdirSync } from 'fs';
import { resolve, dirname } from 'path';
import { fileURLToPath } from 'url';
import { glossaryTerms } from '../src/data/glossary';
import { pipeline } from 'stream/promises';

// Update glossary terms
const ROOT = fileURLToPath(new URL('..', import.meta.url));
const OUTPUT = resolve(ROOT, '.styles/config/ignore/glossary.txt');
const OUTPUT_DIR = dirname(OUTPUT);

if (!existsSync(OUTPUT_DIR)) {
    mkdirSync(OUTPUT_DIR, { recursive: true });
}

const terms = [...new Set(glossaryTerms.map(({ term }) => term))].sort((a, b) =>
    a.toLowerCase().localeCompare(b.toLowerCase()),
);

const content = terms.join('\n') + '\n';
writeFileSync(OUTPUT, content);

console.log(`Wrote ${terms.length} glossary terms to ${OUTPUT}.`);

// If not already present, download dictionary
async function downloadFile(url: string, path: string) {
    const response = await fetch(url);
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

    const fileStream = createWriteStream(path);
    await pipeline(response.body, fileStream);
}

const dictsToDownload = [
    [
        resolve(ROOT, '.styles/config/dictionaries/en_US.dic'),
        'https://raw.githubusercontent.com/LibreOffice/dictionaries/refs/tags/libreoffice-26.2.5.1/en/en_US.dic',
    ],
    [
        resolve(ROOT, '.styles/config/dictionaries/en_US.aff'),
        'https://raw.githubusercontent.com/LibreOffice/dictionaries/refs/tags/libreoffice-26.2.5.1/en/en_US.aff',
    ],
];

dictsToDownload.forEach((dict) => {
    if (!existsSync(dict[0])) {
        mkdirSync(dirname(dict[0]), { recursive: true });
        downloadFile(dict[1], dict[0]);
    }
});
