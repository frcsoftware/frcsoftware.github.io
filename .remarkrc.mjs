// @ts-check

import remarkFrontmatter from 'remark-frontmatter';
import remarkMdx from 'remark-mdx';
import remarkPresetLintRecommended from 'remark-preset-lint-recommended';
import remarkNoInlineCodeFences from './src/plugins/remark-no-inline-code-fences.mjs';

export default {
    plugins: [
        remarkFrontmatter,
        remarkMdx,
        remarkPresetLintRecommended,
        remarkNoInlineCodeFences,
    ],
};
