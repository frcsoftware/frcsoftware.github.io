import { visit } from 'unist-util-visit';
import type { BlockContent, PhrasingContent, Root } from 'mdast';

export function remarkFigure() {
    return (tree: Root) => {
        visit(tree, 'containerDirective', (node) => {
            if (node.name !== 'figure') return;

            const attrs = node.attributes || {};

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

            node.data = node.data || {};
            node.data.hName = 'figure';
            node.data.hProperties = node.data.hProperties || {};
            node.data.hProperties.class =
                'md-figure' + ('border' in attrs ? ' md-figure-border' : '');

            if (style) {
                node.data.hProperties.style = style.trim();
            }

            const newChildren = [];

            for (const child of node.children) {
                const c = child;
                if (c.type === 'paragraph' && c.children) {
                    const images = [];
                    const textNodes: PhrasingContent[] = [];

                    for (const subChild of c.children) {
                        const sc = subChild;
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
                        } as BlockContent);
                    }

                    if (textNodes.length > 0) {
                        newChildren.push({
                            type: 'paragraph',
                            children: textNodes,
                            data: {
                                hName: 'figcaption',
                                hProperties: { class: 'md-figcaption' },
                            },
                        } as BlockContent);
                    }
                } else {
                    newChildren.push(child);
                }
            }

            node.children = newChildren;
        });
    };
}

export default remarkFigure;
