import { readFileSync, readdirSync } from 'fs';
import { join, relative } from 'path';
import { fileURLToPath } from 'url';

const ROOT = fileURLToPath(new URL('..', import.meta.url));
const EXAMPLES_DIR = join(ROOT, 'examples');
const DOCS_DIR = join(ROOT, 'src', 'content', 'docs');

const START_RE = /^\s*(?:\/\/|#|--|<!--|-->)?\s*\[(\w+)\]\s*$/;
const END_RE = /^\s*(?:\/\/|#|--|<!--|-->)?\s*\[\/(\w+)\]\s*$/;
const CODEBLOCK_RE = /^\s*```\w+\s+(\S+)#(\w+)/;

const errors: string[] = [];

// ── Collect region references from MDX files ──

const referencedRegions = new Map<string, Set<string>>();

function walkMdx(dir: string) {
    for (const entry of readdirSync(dir, { withFileTypes: true })) {
        const full = join(dir, entry.name);
        if (entry.isDirectory()) {
            walkMdx(full);
        } else if (entry.name.endsWith('.mdx')) {
            const content = readFileSync(full, 'utf-8');
            for (const line of content.split('\n')) {
                const m = line.match(CODEBLOCK_RE);
                if (m) {
                    const [, filePath, regionName] = m;
                    if (!referencedRegions.has(filePath)) {
                        referencedRegions.set(filePath, new Set());
                    }
                    referencedRegions.get(filePath)!.add(regionName);
                }
            }
        }
    }
}

// ── Validate region markers in source files ──

const definedRegions = new Map<string, Map<string, number>>();

function validateSource(filePath: string) {
    const content = readFileSync(filePath, 'utf-8');
    const lines = content.split('\n');
    const regions = new Map<string, number>();
    const stack: { name: string; lineNum: number }[] = [];
    const rel = relative(EXAMPLES_DIR, filePath).replace(/\\/g, '/');

    for (let i = 0; i < lines.length; i++) {
        let m = lines[i].match(START_RE);
        if (m) {
            const name = m[1];
            if (regions.has(name)) {
                errors.push(`${rel}:${i + 1}: Duplicate region "${name}"`);
            }
            regions.set(name, i + 1);
            stack.push({ name, lineNum: i + 1 });
            continue;
        }

        m = lines[i].match(END_RE);
        if (m) {
            const name = m[1];
            if (stack.length === 0) {
                errors.push(
                    `${rel}:${i + 1}: Unmatched closing tag [/${name}] — no region opened`,
                );
            } else if (stack[stack.length - 1].name !== name) {
                const expected = stack[stack.length - 1].name;
                errors.push(
                    `${rel}:${i + 1}: Region mismatch — expected [/${expected}] but found [/${name}]`,
                );
            } else {
                stack.pop();
            }
        }
    }

    for (const { name, lineNum } of stack) {
        errors.push(
            `${rel}:${lineNum}: Unclosed region "${name}" — missing [/${name}]`,
        );
    }

    definedRegions.set(rel, regions);
}

function walkExamples(dir: string) {
    for (const entry of readdirSync(dir, { withFileTypes: true })) {
        const full = join(dir, entry.name);
        if (entry.isDirectory()) {
            walkExamples(full);
        } else {
            validateSource(full);
        }
    }
}

// ── Cross-reference ──

walkMdx(DOCS_DIR);
walkExamples(EXAMPLES_DIR);

for (const [filePath, names] of referencedRegions) {
    const defs = definedRegions.get(filePath);
    for (const name of names) {
        if (!defs || !defs.has(name)) {
            errors.push(
                `${filePath}: Region "${name}" referenced in MDX but not defined in examples/${filePath}`,
            );
        }
    }
}

for (const [filePath, names] of definedRegions) {
    const refs = referencedRegions.get(filePath);
    for (const [name, lineNum] of names) {
        if (!refs || !refs.has(name)) {
            errors.push(
                `${filePath}:${lineNum}: Orphaned region "${name}" — defined but never referenced in any .mdx file`,
            );
        }
    }
}

// ── Report ──

if (errors.length > 0) {
    for (const e of errors) process.stderr.write(e + '\n');
    process.exit(1);
}
