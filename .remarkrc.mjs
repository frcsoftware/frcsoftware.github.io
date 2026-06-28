import remarkPresetLintRecommended from 'remark-preset-lint-recommended';
import remarkFrontmatter from 'remark-frontmatter';
import remarkMdx from 'remark-mdx';
import remarkNoInlineCodeFences from './src/plugins/remark-no-inline-code-fences.mjs';

export default {
    plugins: [
        remarkFrontmatter,
        remarkMdx,
        remarkPresetLintRecommended,
        remarkNoInlineCodeFences,
    ],
};
