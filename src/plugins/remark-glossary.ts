import { visit, SKIP } from 'unist-util-visit';
import type { Root, RootContent, Text } from 'mdast';
import type { VFile } from 'vfile';
import type { MdxJsxTextElement } from 'mdast-util-mdx-jsx';

import { glossaryTerms } from '../data/glossary';

const sortedTerms = [...glossaryTerms].sort(
    (a, b) => b.term.length - a.term.length,
);

function escapeRegex(str: string): string {
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function buildPattern(terms: typeof sortedTerms, flags: string): RegExp | null {
    if (terms.length === 0) return null;
    return new RegExp(
        `(?<![\\p{L}\\p{N}_])(${terms.map((t) => escapeRegex(t.term)).join('|')})(?![\\p{L}\\p{N}_])`,
        flags,
    );
}

const caseSensitivePattern = buildPattern(
    sortedTerms.filter((t) => t.caseSensitive),
    'gu',
);
const caseInsensitivePattern = buildPattern(
    sortedTerms.filter((t) => !t.caseSensitive),
    'giu',
);

interface TermMatch {
    index: number;
    text: string;
}

function findMatches(text: string): TermMatch[] {
    const raw: TermMatch[] = [];
    for (const p of [caseSensitivePattern, caseInsensitivePattern]) {
        if (!p) continue;
        for (const m of text.matchAll(p)) {
            raw.push({ index: m.index, text: m[0] });
        }
    }

    raw.sort((a, b) => a.index - b.index || b.text.length - a.text.length);

    const matches: TermMatch[] = [];
    let lastEnd = -1;
    for (const m of raw) {
        if (m.index < lastEnd) continue;
        matches.push(m);
        lastEnd = m.index + m.text.length;
    }
    return matches;
}

export function remarkGlossary() {
    return (tree: Root, file: VFile) => {
        if (file.path?.endsWith('glossary.mdx')) return;

        visit(tree, 'text', (node: Text, index, parent) => {
            if (!parent || index === undefined) return;

            if (parent.type === 'link' || parent.type === 'mdxJsxTextElement') {
                return;
            }

            const text = node.value;
            const matches = findMatches(text);

            if (matches.length === 0) return;

            const newNodes: RootContent[] = [];
            let lastIndex = 0;

            matches.forEach((match) => {
                const matchStart = match.index;
                const matchEnd = matchStart + match.text.length;
                const matchedTerm = match.text;

                if (matchStart > lastIndex) {
                    newNodes.push({
                        type: 'text',
                        value: text.slice(lastIndex, matchStart),
                    });
                }

                const glossaryNode: MdxJsxTextElement = {
                    type: 'mdxJsxTextElement',
                    name: 'Glossary',
                    attributes: [
                        {
                            type: 'mdxJsxAttribute',
                            name: 'term',
                            value: matchedTerm,
                        },
                    ],
                    children: [],
                };
                newNodes.push(glossaryNode);

                lastIndex = matchEnd;
            });

            if (lastIndex < text.length) {
                newNodes.push({
                    type: 'text',
                    value: text.slice(lastIndex),
                });
            }

            parent.children.splice(index, 1, ...newNodes);
            return [SKIP, index + newNodes.length];
        });
    };
}

export default remarkGlossary;
