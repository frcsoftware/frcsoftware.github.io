import remarkPresetLintRecommended from 'remark-preset-lint-recommended';
import remarkFrontmatter from 'remark-frontmatter';
import remarkMdx from 'remark-mdx';

export default {
    plugins: [remarkFrontmatter, remarkMdx, remarkPresetLintRecommended],
};
