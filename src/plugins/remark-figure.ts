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

export function remarkFigure() {
    return (tree: Root) => {
        visit(
            tree,
            'containerDirective',
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            (node: any) => {
                const dir = node as ContainerDirective;
                if (dir.name !== 'figure') return;

                const attrs = dir.attributes || {};

                let style = '';

                if (attrs.width) {
                    style += `width: ${attrs.width};`;
                } else if (attrs.w) {
                    style += `width: ${attrs.w}%;`;
                }

                if ('border' in attrs) {
                    const borderValue = attrs.border || '5px solid #ADADAD';
                    style += ` --figure-border: ${borderValue.replace(/_/g, ' ')};`;
                }

                dir.data = dir.data || {};
                dir.data.hName = 'figure';
                dir.data.hProperties = dir.data.hProperties || {};
                dir.data.hProperties.class =
                    'md-figure' +
                    ('border' in attrs ? ' md-figure-border' : '');

                if (style) {
                    dir.data.hProperties.style = style.trim();
                }

                const newChildren: unknown[] = [];

                for (const child of dir.children) {
                    const c = child as {
                        type: string;
                        children?: unknown[];
                        data?: unknown;
                        value?: string;
                    };
                    if (c.type === 'paragraph' && c.children) {
                        const images: unknown[] = [];
                        const textNodes: unknown[] = [];

                        for (const subChild of c.children) {
                            const sc = subChild as {
                                type: string;
                                value?: string;
                            };
                            if (sc.type === 'image') {
                                images.push(subChild);
                            } else if (sc.type === 'text' && sc.value?.trim()) {
                                textNodes.push(subChild);
                            } else if (sc.type !== 'text' || sc.value?.trim()) {
                                textNodes.push(subChild);
                            }
                        }

                        if (images.length > 0) {
                            newChildren.push({
                                type: 'paragraph',
                                children: images,
                                data: c.data,
                            });
                        }

                        if (textNodes.length > 0) {
                            newChildren.push({
                                type: 'paragraph',
                                children: textNodes,
                                data: {
                                    hName: 'figcaption',
                                    hProperties: { class: 'md-figcaption' },
                                },
                            });
                        }
                    } else {
                        newChildren.push(child);
                    }
                }

                dir.children = newChildren;
            },
        );
    };
}

export default remarkFigure;
