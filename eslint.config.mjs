import js from '@eslint/js';
import tseslint from 'typescript-eslint';
import astro from 'eslint-plugin-astro';

export default [
    { ignores: ['dist/', '.astro/', 'node_modules/', 'scripts/'] },

    js.configs.recommended,

    ...tseslint.configs.recommended,

    ...astro.configs['flat/recommended'],

    {
        rules: {
            '@typescript-eslint/no-unused-vars': [
                'warn',
                { argsIgnorePattern: '^_' },
            ],
            '@typescript-eslint/no-explicit-any': 'warn',
        },
    },

    {
        files: ['**/*.astro'],
        languageOptions: {
            globals: {
                ImageMetadata: 'readonly',
            },
        },
        rules: {
            'no-undef': 'off',
        },
    },
];
