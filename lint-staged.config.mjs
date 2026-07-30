const runPrettierOn =
    '**/*.{' +
    [
        '.js',
        '.mjs',
        '.ts',
        '.json',
        '.json5',
        '.jsonc',
        '.css',
        '.md',
        '.mdx',
        '.yaml',
        '.yml',
        '.astro',
    ].join(',') +
    '}';

/** @type {import('lint-staged').Configuration} */
export default {
    [runPrettierOn]: (files) => `prettier --write --ignore-unknown ${files.join(' ')}`,
    '**/*.{astro,ts,mjs,js}': (files) => `eslint --fix ${files.join(' ')}`,
    'src/content/**/*.{md,mdx}': (files) => [
        `pnpm remark ${files.join(' ')} --ext mdx --frail --no-stdout --quiet`,
    ],
    'examples/**/*.{java,gradle}': () => [
        `./examples/gradlew -p examples spotlessApply`,
    ],
    'src/data/glossary.ts': () => [
        'pnpm generate:glossary',
        'git add src/content/docs/resources/glossary.mdx',
    ],
    // Yes, I know this should be a FunctionTask but those are kinda bad until https://github.com/lint-staged/lint-staged/issues/1826 is resolved
    'package.json': () => 'pnpm tsx scripts/syncLockfile.lint.ts',
};