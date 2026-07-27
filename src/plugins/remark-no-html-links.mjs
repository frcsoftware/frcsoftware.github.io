import { visit } from 'unist-util-visit';
/**
 * Forbid raw HTML/JSX anchor tags (`<a href="...">`) in favour of markdown
 * link syntax (`[text](url)`). Keeps links consistent and portable.
 */
export default function remarkNoHtmlLinks() {
    /**
     * @param {import('mdast').Root} tree
     * @param {import('vfile').VFile} file
     */
    return (tree, file) => {
        // MDX parses `<a>` into JSX element nodes; `remark-mdx` never emits raw
        // `html` nodes, but check that type too for plain-markdown safety.
        visit(
            tree,
            ['mdxJsxTextElement', 'mdxJsxFlowElement', 'html'],
            (node) => {
                const isAnchor =
                    ((node.type === 'mdxJsxTextElement' ||
                        node.type === 'mdxJsxFlowElement') &&
                        node.name === 'a') ||
                    (node.type === 'html' &&
                        /<a[\s/>]/i.test(node.value ?? ''));
                if (!isAnchor) return;

                const msg = file.message(
                    'Use markdown link syntax `[text](url)` instead of an HTML `<a>` tag.',
                    node,
                );
                msg.fatal = true;
            },
        );
    };
}
