import { visit } from 'unist-util-visit';

const IGNORE_RE = /rli:\s*ignore/;

export default function remarkNoInlineCodeFences() {
    return (tree, file) => {
        visit(tree, 'code', (node, index, parent) => {
            if (index !== undefined && parent) {
                const prev = parent.children[index - 1];
                if (
                    prev?.type === 'mdxFlowExpression' &&
                    IGNORE_RE.test(prev.value)
                ) {
                    return;
                }
            }

            if (node.meta?.trim()) return;

            if (node.value?.trim()) {
                const msg = file.message(
                    'Code fences must use the RLI system: ```language path/to/file#regionName',
                    node,
                );
                msg.fatal = true;
            }
        });
    };
}
