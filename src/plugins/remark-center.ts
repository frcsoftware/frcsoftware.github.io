import { visit } from 'unist-util-visit';
import type { Root } from 'mdast';

export function remarkCenter() {
    return (tree: Root) => {
        visit(tree, 'containerDirective', (node) => {
            if (node.name !== 'center') return;

            node.data = node.data || {};
            node.data.hName = 'div';
            node.data.hProperties = node.data.hProperties || {};
            node.data.hProperties.class = 'centered-content';
        });
    };
}

export default remarkCenter;
