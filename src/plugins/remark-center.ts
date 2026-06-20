import { visit } from 'unist-util-visit';
import type { Root } from 'mdast';

interface ContainerDirective {
    type: 'containerDirective';
    name: string;
    attributes?: Record<string, string>;
    children: unknown[];
    data?: {
        hName?: string;
        hProperties?: Record<string, unknown>;
    };
}

export function remarkCenter() {
    return (tree: Root) => {
        visit(
            tree,
            'containerDirective',
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (node: any) => {
                const dir = node as ContainerDirective;
                if (dir.name !== 'center') return;

                dir.data = dir.data || {};
                dir.data.hName = 'div';
                dir.data.hProperties = dir.data.hProperties || {};
                dir.data.hProperties.class = 'centered-content';
            },
        );
    };
}

export default remarkCenter;
