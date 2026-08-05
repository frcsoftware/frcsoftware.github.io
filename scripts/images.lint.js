import { styleText } from 'node:util';
import sharp from 'sharp';

const [, , ...images] = process.argv;

/** @type {string[]} */
const offending = [];

for (const image of images) {
    if (!image.endsWith('.webp')) {
        offending.push(image);
        continue;
    }
    const meta = await sharp(image).metadata();
    if (meta.format !== 'webp') {
        offending.push(image);
    }
}
if (offending.length >= 1) {
    process.stderr.write(
        `${styleText('red', '✖')} All images should be in WebP format. See contribution guidelines for more info. Offending files: \n - ${offending.map((img) => img.replace(process.cwd() + '/', '')).join('\n - ')}\n`,
    );
    process.exit(1);
}
