import { styleText } from 'node:util';

const [, , ...images] = process.argv;
process.stderr.write(
    `${styleText('red', '✖')} All images should be in WebP format. See contribution guidelines for more info. Offending files: \n - ${images.map((img) => img.replace(process.cwd() + '/', '')).join('\n - ')}\n`,
);
process.exit(1);
