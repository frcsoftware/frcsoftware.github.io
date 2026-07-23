import puppeteer, { type Page } from 'puppeteer';
import {
    PDFDocument,
    PDFName,
    PDFNull,
    PDFArray,
    PDFDict,
    PDFString,
    PDFHexString,
    StandardFonts,
    rgb,
} from 'pdf-lib';
import { spawn } from 'node:child_process';
import { createServer } from 'node:net';
import type { AddressInfo } from 'node:net';
import { mkdir, readFile, writeFile } from 'node:fs/promises';
import {
    sidebarSections,
    flattenSidebarItems,
} from '../src/config/sidebarConfig.ts';

const MARGIN = 42.52; // 1.5cm in points (matches Puppeteer margin setting)
const FOOTER_Y = 20;
const FOOTER_SEPARATOR_Y = 36;
const FOOTER_SIZE = 9;
const FOOTER_COLOR = rgb(0.45, 0.45, 0.45);

// Forces lazy images to load eagerly so hidden slides render before printing.
function domForceEagerImages() {
    document
        .querySelectorAll<HTMLImageElement>('img[loading="lazy"]')
        .forEach((img) => {
            img.removeAttribute('loading');
            const src = img.getAttribute('src');
            if (src) img.src = src;
        });
}

// Injects the FRCSoftware logo + stage label as a header above the page title
function domInjectLogoHeader(svgSource: string, stageLabel: string) {
    const styles = {
        header: 'display:flex;align-items:center;gap:0.8rem;padding-bottom:1.25rem;margin-bottom:1.75rem;border-bottom:2px solid #924dc3;',
        icon: 'width:48px;height:48px;flex-shrink:0;display:flex;align-items:center;',
        siteLabel:
            'font-size:1.6rem;font-weight:700;color:#924dc3;letter-spacing:-0.02em;line-height:1;',
        stageBadge:
            'margin-left:auto;font-size:1rem;font-weight:600;color:#924dc3;letter-spacing:0.01em;line-height:1;',
    };

    const container =
        document.querySelector('.sl-markdown-content') ||
        document.querySelector('article') ||
        document.querySelector('main');
    if (!container) return;

    const header = document.createElement('div');
    header.style.cssText = styles.header;

    const icon = document.createElement('div');
    icon.style.cssText = styles.icon;
    icon.innerHTML = svgSource;

    const siteLabel = document.createElement('span');
    siteLabel.textContent = 'FRCSoftware.org';
    siteLabel.style.cssText = styles.siteLabel;

    const stageBadge = document.createElement('span');
    stageBadge.textContent = stageLabel;
    stageBadge.style.cssText = styles.stageBadge;

    header.appendChild(icon);
    header.appendChild(siteLabel);
    header.appendChild(stageBadge);

    // The page <h1> lives outside .sl-markdown-content in Starlight, so
    // insert above it rather than at the top of the content area.
    const h1 = document.querySelector('h1');
    if (h1?.parentNode) {
        h1.parentNode.insertBefore(header, h1);
    } else {
        container.insertBefore(header, container.firstChild);
    }
}

// Replaces glossary tooltip spans with inline [N] superscripts and appends a
// numbered glossary section at the bottom of the article content.
function domInjectGlossaryFootnotes() {
    const styles = {
        sup: 'font-size:0.7em;color:#555;margin-left:1px;vertical-align:super;line-height:0;',
        section:
            'margin-top:2rem;padding-top:1rem;border-top:1px solid #ccc;font-size:0.85rem;color:#333;',
        heading: 'font-weight:600;margin:0 0 0.5rem;font-size:0.9rem;',
        ol: 'margin:0;padding-left:1.5rem;',
        li: 'margin-bottom:0.2rem;',
    };

    const terms = [...document.querySelectorAll<HTMLElement>('.glossary-term')];
    if (terms.length === 0) return;

    const defToEntry = new Map<string, { term: string; index: number }>();
    let nextIndex = 1;

    for (const el of terms) {
        const displayText = el.textContent || '';
        const canonicalTerm = el.getAttribute('data-term') || displayText;
        const def = el.getAttribute('data-tooltip') || '';
        if (!defToEntry.has(def)) {
            defToEntry.set(def, { term: canonicalTerm, index: nextIndex++ });
        }
        const { index } = defToEntry.get(def)!;

        const wrapper = document.createElement('span');
        wrapper.textContent = displayText;
        const sup = document.createElement('sup');
        sup.textContent = `[${index}]`;
        sup.style.cssText = styles.sup;
        wrapper.appendChild(sup);
        el.replaceWith(wrapper);
    }

    const container =
        document.querySelector('.sl-markdown-content') ||
        document.querySelector('article') ||
        document.querySelector('main');
    if (!container) return;

    const section = document.createElement('div');
    section.style.cssText = styles.section;

    const heading = document.createElement('p');
    heading.textContent = 'Glossary';
    heading.style.cssText = styles.heading;
    section.appendChild(heading);

    const ol = document.createElement('ol');
    ol.style.cssText = styles.ol;
    [...defToEntry.entries()]
        .sort((a, b) => a[1].index - b[1].index)
        .forEach(([def, { term }]) => {
            const li = document.createElement('li');
            li.style.cssText = styles.li;
            const b = document.createElement('b');
            b.textContent = term + ': ';
            li.appendChild(b);
            li.appendChild(document.createTextNode(def));
            ol.appendChild(li);
        });

    section.appendChild(ol);
    container.appendChild(section);
}

// Replaces each Slides carousel with a vertical stack of image+caption pairs
// so all slides render in print rather than only the active one.
function domRestructureSlides() {
    const styles = {
        stack: 'display:flex;flex-direction:column;align-items:center;gap:2rem;width:100%;margin:1.5rem 0;',
        entry: 'display:flex;flex-direction:column;align-items:center;gap:0.5rem;width:100%;',
        media: 'display:flex;justify-content:center;width:100%;',
        caption: 'opacity:1;pointer-events:auto;display:block;',
    };

    document.querySelectorAll('.slides-component').forEach((component) => {
        const slides = [...component.querySelectorAll('.slide')];
        const captions = [...component.querySelectorAll('.slides-caption')];

        const stack = document.createElement('div');
        stack.style.cssText = styles.stack;

        slides.forEach((slide, i) => {
            const entry = document.createElement('div');
            entry.style.cssText = styles.entry;

            const media = slide.querySelector('.slide-media');
            if (media) {
                const img = media.querySelector<HTMLImageElement>('img');
                if (img) {
                    img.style.width = 'auto';
                    img.style.height = 'auto';
                    img.style.maxWidth = '100%';
                    img.style.maxHeight = 'none';
                }
                media.querySelector('.slide-loader')?.remove();
                media.classList.remove('loading');
                const mediaClone = media.cloneNode(true) as HTMLElement;
                mediaClone.style.cssText = styles.media;
                entry.appendChild(mediaClone);
            }

            const caption = captions[i];
            const text = caption?.textContent?.trim();
            if (caption && text && text !== ' ') {
                const captionClone = caption.cloneNode(true) as HTMLElement;
                captionClone.style.cssText = styles.caption;
                entry.appendChild(captionClone);
            }

            stack.appendChild(entry);
        });

        component.replaceWith(stack);
    });
}

type PageMeta = { stageLabel: string; pageTitle: string };

// Draws a footer (separator line, "Stage N — Title" left, page number right)
// on every page of the merged document.
async function addPageDecorations(
    doc: PDFDocument,
    pageMetadata: PageMeta[],
): Promise<void> {
    const font = await doc.embedFont(StandardFonts.Helvetica);

    for (let i = 0; i < doc.getPageCount(); i++) {
        const page = doc.getPage(i);
        const meta = pageMetadata[i];
        if (!meta) continue;

        const { width } = page.getSize();

        page.drawLine({
            start: { x: MARGIN, y: FOOTER_SEPARATOR_Y },
            end: { x: width - MARGIN, y: FOOTER_SEPARATOR_Y },
            thickness: 0.5,
            color: rgb(0.75, 0.75, 0.75),
        });

        page.drawText(`${meta.stageLabel} — ${meta.pageTitle}`, {
            x: MARGIN,
            y: FOOTER_Y,
            size: FOOTER_SIZE,
            font,
            color: FOOTER_COLOR,
        });

        const rightText = String(i + 1);
        page.drawText(rightText, {
            x: width - MARGIN - font.widthOfTextAtSize(rightText, FOOTER_SIZE),
            y: FOOTER_Y,
            size: FOOTER_SIZE,
            font,
            color: FOOTER_COLOR,
        });
    }
}

// Converts localhost link annotations to in-PDF GoTo destinations for pages
// within the same stage, and to frcsoftware.org URLs for cross-stage links.
function fixLinks(
    doc: PDFDocument,
    pathToPageOffset: Map<string, number>,
    port: number,
): void {
    const localhostOrigin = `http://localhost:${port}`;
    const pages = doc.getPages();

    for (const page of pages) {
        const annotsRaw = page.node.get(PDFName.of('Annots'));
        if (!annotsRaw) continue;
        const annots = doc.context.lookup(annotsRaw);
        if (!(annots instanceof PDFArray)) continue;

        for (let i = 0; i < annots.size(); i++) {
            const annotObj = doc.context.lookup(annots.get(i));
            if (!(annotObj instanceof PDFDict)) continue;
            if (annotObj.get(PDFName.of('Subtype'))?.toString() !== '/Link')
                continue;

            const action = doc.context.lookup(annotObj.get(PDFName.of('A')));
            if (!(action instanceof PDFDict)) continue;
            if (action.get(PDFName.of('S'))?.toString() !== '/URI') continue;

            const uriRaw = action.get(PDFName.of('URI'));
            let uri: string | null = null;
            if (uriRaw instanceof PDFString) uri = uriRaw.decodeText();
            else if (uriRaw instanceof PDFHexString) uri = uriRaw.decodeText();
            if (!uri?.startsWith(localhostOrigin)) continue;

            const { pathname } = new URL(uri);
            const targetOffset = pathToPageOffset.get(pathname);

            if (targetOffset !== undefined) {
                annotObj.delete(PDFName.of('A'));
                annotObj.set(
                    PDFName.of('Dest'),
                    doc.context.obj([
                        pages[targetOffset].ref,
                        PDFName.of('XYZ'),
                        PDFNull,
                        PDFNull,
                        PDFNull,
                    ]),
                );
            } else {
                action.set(
                    PDFName.of('URI'),
                    PDFString.of('https://frcsoftware.org' + pathname),
                );
            }
        }
    }
}

function findFreePort(): Promise<number> {
    return new Promise((resolve, reject) => {
        const server = createServer();
        server.listen(0, () => {
            const port = (server.address() as AddressInfo).port;
            server.close(() => resolve(port));
        });
        server.on('error', reject);
    });
}

async function waitForServer(baseUrl: string, maxMs = 15_000): Promise<void> {
    const deadline = Date.now() + maxMs;
    while (Date.now() < deadline) {
        try {
            await fetch(`${baseUrl}/`);
            return;
        } catch {
            await new Promise((r) => setTimeout(r, 300));
        }
    }
    throw new Error(`Preview server did not start within ${maxMs}ms`);
}

async function renderPage(
    tab: Page,
    baseUrl: string,
    href: string,
    isFirstPage: boolean,
    stageLabel: string,
    logoSvg: string,
) {
    await tab.goto(`${baseUrl}${href}`, { waitUntil: 'domcontentloaded' });
    await tab.evaluate(domForceEagerImages);
    await tab.waitForNetworkIdle({ idleTime: 500 });
    await tab.evaluate(() =>
        document.documentElement.setAttribute('data-theme', 'light'),
    );
    if (isFirstPage) {
        await tab.evaluate(domInjectLogoHeader, logoSvg, stageLabel);
    }
    await tab.evaluate(domInjectGlossaryFootnotes);
    await tab.evaluate(domRestructureSlides);
    return tab.pdf({
        format: 'A4',
        printBackground: true,
        margin: {
            top: '1.5cm',
            bottom: '1.5cm',
            left: '1.5cm',
            right: '1.5cm',
        },
    });
}

async function main() {
    const port = await findFreePort();
    const baseUrl = `http://localhost:${port}`;
    const preview = spawn('pnpm', ['preview', '--port', String(port)], {
        stdio: 'ignore',
    });

    try {
        console.log('Waiting for preview server...');
        await waitForServer(baseUrl);
        console.log('Server ready.');

        const browser = await puppeteer.launch({
            headless: true,
            args: process.env.CI ? ['--no-sandbox'] : [],
        });
        await mkdir('dist/pdfs', { recursive: true });

        const logoSvg = await readFile('public/favicon.svg', 'utf-8');
        const stageBuffers: Uint8Array[] = [];
        const learningItems =
            sidebarSections['/learning-course']?.[0]?.items ?? [];
        const stageGroups = learningItems.filter(
            (item) =>
                item.label?.startsWith('Stage') && Array.isArray(item.items),
        );

        for (const group of stageGroups) {
            const pages = flattenSidebarItems(group.items!);
            const fileName =
                group.label.toLowerCase().replace(/\s+/g, '') + '.pdf';
            console.log(`Generating ${fileName} (${pages.length} pages)...`);

            const merged = await PDFDocument.create();
            const pathToPageOffset = new Map<string, number>();
            const pageMetadata: PageMeta[] = [];
            let pageOffset = 0;

            for (const { href, label } of pages) {
                process.stdout.write(`  ${label}...`);
                const tab = await browser.newPage();
                const buffer = await renderPage(
                    tab,
                    baseUrl,
                    href,
                    pageOffset === 0,
                    group.label,
                    logoSvg,
                );
                await tab.close();

                const doc = await PDFDocument.load(buffer);
                pathToPageOffset.set(href, pageOffset);
                const count = doc.getPageCount();
                for (let p = 0; p < count; p++) {
                    pageMetadata.push({
                        stageLabel: group.label,
                        pageTitle: label,
                    });
                }
                pageOffset += count;
                const copied = await merged.copyPages(
                    doc,
                    doc.getPageIndices(),
                );
                copied.forEach((p) => merged.addPage(p));
                process.stdout.write(' done\n');
            }

            fixLinks(merged, pathToPageOffset, port);
            await addPageDecorations(merged, pageMetadata);

            const bytes = await merged.save();
            stageBuffers.push(bytes);
            await writeFile(`dist/pdfs/${fileName}`, bytes);
            console.log(`→ dist/pdfs/${fileName}`);
        }

        if (stageBuffers.length > 1) {
            console.log('Generating all.pdf...');
            const all = await PDFDocument.create();
            for (const buf of stageBuffers) {
                const doc = await PDFDocument.load(buf);
                const copied = await all.copyPages(doc, doc.getPageIndices());
                copied.forEach((p) => all.addPage(p));
            }
            await writeFile('dist/pdfs/all.pdf', await all.save());
            console.log('→ dist/pdfs/all.pdf');
        }

        await browser.close();
    } finally {
        preview.kill();
    }
}

main().catch((err) => {
    console.error(err);
    process.exit(1);
});
