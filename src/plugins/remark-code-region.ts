import { visit } from 'unist-util-visit';
import { readFileSync } from 'fs';
import { resolve } from 'path';
import type { Root } from 'mdast';

export default function remarkCodeRegion() {
    return (tree: Root) => {
        const examplesDir = resolve(process.cwd(), 'examples');

        visit(tree, 'code', (node: any) => {
            const meta: string = node.meta || '';
            // Match file=path in meta string (non-whitespace value)
            const fileMatch = meta.match(/file=(\S+)/);
            if (!fileMatch) return;

            // Match optional region=name in meta
            const regionMatch = meta.match(/region=(\S+)/);

            // Strip our custom meta attributes, preserve everything else (e.g. title, Expressive Code attrs)
            node.meta = meta
                .replace(/file=\S+\s*/g, '')
                .replace(/region=\S+\s*/g, '')
                .replace(/title=\S+\s*/g, '')
                .trim();

            // Strip lines containing #region / #endregion markers regardless of comment-syntax prefix.
            // Catches // #region, # region, <!-- #region -->, -- #region, etc. for any language.
            const markerRE = /^\s*(?:\/\/|#|--|<!--|-->)?\s*#(?:end)?region\b/;

            const srcPath = resolve(examplesDir, fileMatch[1]);
            const content = readFileSync(srcPath, 'utf-8');
            const lines = content.split('\n');

            if (!regionMatch) {
                node.value = dedent(
                    lines.filter((l) => !markerRE.test(l)).join('\n'),
                );
                return;
            }

            const regionName = regionMatch[1];
            const regionLines: string[] = [];
            let inRegion = false;
            let found = false;

            // Matches #region <name> with optional comment-syntax prefix (//, #, --, <!--, -->)
            const regionRE = new RegExp(
                `^\\s*(?:\\/\\/|#|--|<!--|-->)?\\s*#region\\s+${escapeRegex(regionName)}\\s*$`,
            );
            // Matches #endregion <name> with the same optional comment-syntax prefix
            const endRE = new RegExp(
                `^\\s*(?:\\/\\/|#|--|<!--|-->)?\\s*#endregion\\s+${escapeRegex(regionName)}\\s*$`,
            );

            for (const line of lines) {
                if (!inRegion && regionRE.test(line)) {
                    if (found)
                        throw Error(
                            `Duplicate region "${regionName}" in ${srcPath}`,
                        );
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

            node.value = dedent(
                regionLines.filter((l) => !markerRE.test(l)).join('\n'),
            );
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
