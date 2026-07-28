import { visit } from 'unist-util-visit';
import type { Root } from 'hast';

export default function rehypeTargetBlank() {
    return (tree: Root) => {
        visit(tree, 'element', (node) => {
            if (
                node.tagName !== 'a' ||
                typeof node.properties.href !== 'string' ||
                !URL.canParse(node.properties.href) ||
                // only match http/s
                !/^https?:/gm.test(URL.parse(node.properties.href)!.protocol)
            ) {
                // only run on valid external links
                return;
            }
            node.properties.target = '_blank';
            node.properties.rel = ['noreferrer', 'noopener'];
        });
    };
}
