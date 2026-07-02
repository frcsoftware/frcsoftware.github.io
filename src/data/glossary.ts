/**
 * Glossary of terms and abbreviations
 *
 * Add terms here to automatically highlight them across the site
 * with a dotted underline and hover tooltip.
 *
 * Format:
 * {
 *   term: "TERM",           // The word/abbreviation to match (case-insensitive)
 *   definition: "..."       // The explanation shown on hover
 * }
 */

export interface GlossaryTerm {
    term: string;
    definition: string;
}

export const glossaryTerms: GlossaryTerm[] = [
    // Electronics
    {
        term: 'PDH',
        definition: 'Power Distribution Hub',
    },
    {
        term: 'SparkMAX',
        definition: 'Motor controller for REV motors',
    },
    {
        term: 'PWM',
        definition: 'Pulse Width Modulation cable',
    },
    {
        term: 'SystemCore',
        definition: 'SystemCore - The brain of the robot',
    },
    {
        term: 'Blinkin',
        definition: 'LED controller for REV',
    },
    {
        term: 'Main Breaker',
        definition: 'Switch for the robot',
    },
    {
        term: 'Limit Switch',
        definition:
            'Type of sensor that acts as a soft limit when physically or magnetically hit',
    },
    {
        term: 'Throughbore Encoder',
        definition:
            'An encoder that allows shafts to pass through its center to record position',
    },
    {
        term: 'Magnetic Encoder',
        definition:
            'An encoder that uses magnetic fields to measure position or motion',
    },

    // Software
    {
        term: 'Repository',
        definition:
            'A storage location for software packages, often used in version control systems like Git. Repositories are just folders that contain files and subfolders, and they can be hosted on platforms like GitHub to facilitate collaboration and version tracking',
    },
];

/**
 * Get a glossary term by its name (case-insensitive)
 */
export function getGlossaryTerm(term: string): GlossaryTerm | undefined {
    return glossaryTerms.find(
        (g) => g.term.toLowerCase() === term.toLowerCase(),
    );
}

/**
 * Get all terms as a map for quick lookup
 */
export function getGlossaryMap(): Map<string, string> {
    const map = new Map<string, string>();
    glossaryTerms.forEach(({ term, definition }) => {
        map.set(term.toLowerCase(), definition);
    });
    return map;
}
