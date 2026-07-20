/// <reference types="mdast-util-mdx-jsx" />
import { visit } from 'unist-util-visit';
import type { Root, RootContent, Text } from 'mdast';
import type { VFile } from 'vfile';
import { glossaryTerms } from '../data/glossary';

const sortedTerms = [...glossaryTerms].sort(
    (a, b) => b.term.length - a.term.length,
);

const termMap = new Map<string, string>();
glossaryTerms.forEach(({ term, definition }) => {
    termMap.set(term.toLowerCase(), definition);
});

const pattern = new RegExp(
    `\\b(${sortedTerms.map((t) => escapeRegex(t.term)).join('|')})\\b`,
    'gi',
);

function escapeRegex(str: string): string {
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

export function remarkGlossary() {
    return (tree: Root, file: VFile) => {
        if (file.path?.endsWith('glossary.mdx')) return;

        visit(tree, 'text', (node: Text, index, parent) => {
            if (!parent || index === undefined) return;

            if (parent.type === 'link') {
                return;
            }

            const text = node.value;
            const matches = [...text.matchAll(pattern)];

            if (matches.length === 0) return;

            const newNodes: RootContent[] = [];
            let lastIndex = 0;

            matches.forEach((match) => {
                const matchStart = match.index;
                const matchEnd = matchStart + match[0].length;
                const matchedTerm = match[0];
                const definition = termMap.get(matchedTerm.toLowerCase());

                if (matchStart > lastIndex) {
                    newNodes.push({
                        type: 'text',
                        value: text.slice(lastIndex, matchStart),
                    });
                }

                newNodes.push({
                    type: 'html',
                    value: `<abbr class="glossary-term" data-tooltip="${escapeHtml(definition || '')}">${escapeHtml(matchedTerm)}</abbr>`,
                });

                lastIndex = matchEnd;
            });

            if (lastIndex < text.length) {
                newNodes.push({
                    type: 'text',
                    value: text.slice(lastIndex),
                });
            }

            parent.children.splice(index, 1, ...newNodes);
        });
    };
}

function escapeHtml(str: string): string {
    return str
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

export default remarkGlossary;
