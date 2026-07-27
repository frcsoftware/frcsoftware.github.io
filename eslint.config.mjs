// @ts-check
import js from '@eslint/js';
import tseslint from 'typescript-eslint';
import astro from 'eslint-plugin-astro';
import process from 'node:process';
import globals from 'globals';

import { defineConfig } from 'eslint/config';
export default defineConfig([
    { ignores: ['dist/', '.astro/', 'node_modules/'] },

    js.configs.recommended,

    ...tseslint.configs.recommended,

    ...astro.configs['flat/recommended'],

    {
        rules: {
            '@typescript-eslint/no-unused-vars': [
                'warn',
                { argsIgnorePattern: '^_' },
            ],
            '@typescript-eslint/no-explicit-any': 'error',
            // ci runs `astro check` which runs a full typescript checker
            'no-undef': process.env.CI ? 'off' : 'error',
        },
    },
    {
        files: ['scripts/*', 'src/plugins/*'],
        languageOptions: {
            globals: {
                // scripts and plugins run in a node env
                ...globals.node,
            },
        },
    },
    {
        // also catch astro virtual files
        files: ['**/*.astro', '**/*.astro/**/*.ts'],
        languageOptions: {
            globals: {
                ImageMetadata: 'readonly',
            },
        },
        rules: {
            'no-undef': 'off',
        },
    },
]);
