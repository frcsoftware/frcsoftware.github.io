import { visit } from 'unist-util-visit';
import { readFileSync } from 'fs';
import { resolve } from 'path';
import type { Root } from 'mdast';
import { VFile } from 'vfile';

export default function remarkCodeRegion() {
    return (tree: Root, file: VFile) => {
        const examplesDir = resolve(process.cwd(), 'examples');
        let codeRegionSources = file.data.astro?.frontmatter?.codeRegionSources;
        if (
            codeRegionSources == null ||
            typeof codeRegionSources !== 'object'
        ) {
            codeRegionSources = {};
        }

        visit(tree, 'code', (node) => {
            const meta: string = node.meta || '';

            const token = meta.match(/^(\S+)/);
            if (!token) return;

            const raw = token[1];
            const hashIdx = raw.indexOf('#');
            let filePath = hashIdx === -1 ? raw : raw.slice(0, hashIdx);
            if (filePath === '' && codeRegionSources.default) {
                filePath = codeRegionSources.default;
            } else {
                const entries = Object.entries(codeRegionSources);
                for (const [source, pathAlias] of entries) {
                    if (
                        typeof source === 'string' &&
                        typeof pathAlias === 'string' &&
                        filePath === '{' + source + '}'
                    ) {
                        filePath = pathAlias;
                        break;
                    }
                }
            }
            const regionName = hashIdx !== -1 ? raw.slice(hashIdx + 1) : null;

            node.meta = meta.slice(raw.length).trim();

            const srcPath = resolve(examplesDir, filePath);
            const content = readFileSync(srcPath, 'utf-8');
            const lines = content.split('\n');

            if (!regionName) {
                const markerRE = /^\s*(?:\/\/|#|--|<!--|-->)?\s*\[\/?\w+\]\s*$/;
                node.value = dedent(
                    lines.filter((l) => !markerRE.test(l)).join('\n'),
                );
                return;
            }

            const escapedName = escapeRegex(regionName);
            const startRE = new RegExp(
                `^\\s*(?:\\/\\/|#|--|<!--|-->)?\\s*\\[${escapedName}\\]\\s*$`,
            );
            const endRE = new RegExp(
                `^\\s*(?:\\/\\/|#|--|<!--|-->)?\\s*\\[\\/${escapedName}\\]\\s*$`,
            );

            const regionLines: string[] = [];
            let inRegion = false;
            let found = false;

            for (const line of lines) {
                if (!inRegion && startRE.test(line)) {
                    inRegion = true;
                    found = true;
                    continue;
                }
                if (inRegion && endRE.test(line)) {
                    inRegion = false;
                    continue;
                }
                if (inRegion) regionLines.push(line);
            }

            if (!found)
                throw Error(`Region "${regionName}" not found in ${srcPath}`);

            if (inRegion)
                throw Error(
                    `Unclosed region "${regionName}" in ${srcPath} — missing [/${regionName}]`,
                );

            node.value = dedent(regionLines.join('\n'));
        });
    };
}

function escapeRegex(str: string): string {
    return str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

function dedent(str: string): string {
    const lines = str.split('\n');
    const indent = lines
        .filter((l) => l.trim().length > 0)
        .reduce(
            (min, l) =>
                Math.min(min, l.match(/^[ \t]*/)?.[0].length ?? Infinity),
            Infinity,
        );
    if (indent === 0 || !isFinite(indent)) return str;
    return lines.map((l) => l.slice(indent)).join('\n');
}
