/**
 * Remark plugin that auto-injects common component imports into every MDX file.
 * Contributors no longer need to manually import ContentFigure, Aside, Slides,
 * or LinkButton. They are available in all MDX pages automatically.
 *
 * Runs in the remark pipeline (same as remarkCenter and remarkGlossary), which
 * Astro applies to all MDX files. Skips injection for any component already
 * imported to avoid duplicate identifier errors. No-ops on plain .md files.
 */

import type { Root } from 'mdast';
import type { VFile } from 'vfile';

declare module 'mdast' {
    interface RootContentMap {
        mdxjsEsm: {
            type: 'mdxjsEsm';
            value: string;
            data?: { estree?: unknown };
        };
    }
}

const GLOBAL_IMPORTS = [
    { name: 'ContentFigure', path: '@components/ContentFigure.astro' },
    { name: 'Aside', path: '@components/Aside.astro' },
    { name: 'Slides', path: '@components/Slides.astro' },
    { name: 'LinkButton', path: '@components/LinkButton.astro' },
    { name: 'Glossary', path: '@components/Glossary.astro' },
    //   { name: 'ImageTable',    path: '@components/ImageTable.astro' },
];

function makeImportNode(name: string, importPath: string) {
    return {
        type: 'mdxjsEsm' as const,
        value: `import ${name} from '${importPath}';`,
        data: {
            estree: {
                type: 'Program',
                body: [
                    {
                        type: 'ImportDeclaration',
                        specifiers: [
                            {
                                type: 'ImportDefaultSpecifier',
                                local: { type: 'Identifier', name },
                            },
                        ],
                        source: {
                            type: 'Literal',
                            value: importPath,
                            raw: `'${importPath}'`,
                        },
                    },
                ],
                sourceType: 'module',
            },
        },
    };
}

export function remarkMdxGlobalImports() {
    return (tree: Root, file: VFile) => {
        if (!file.path?.endsWith('.mdx')) return;

        const existingNames = new Set<string>();
        for (const node of tree.children) {
            if (node.type === 'mdxjsEsm') {
                const match = node.value?.match(/\bimport\s+(\w+)\s+from\b/);
                if (match) existingNames.add(match[1]);
            }
        }

        const toInsert = GLOBAL_IMPORTS.filter(
            ({ name }) => !existingNames.has(name),
        ).map(({ name, path }) => makeImportNode(name, path));

        if (toInsert.length > 0) {
            tree.children.unshift(...toInsert);
        }
    };
}

export default remarkMdxGlobalImports;
