/*
 * Rigel — "Orion ledger" theme.
 *
 * Warm paper + ink neutrals, with the two stars of Orion as signal colours:
 * Rigel (blue supergiant) for actions, Betelgeuse (red supergiant) for danger.
 * `slate` and `red` are remapped on purpose so every existing utility in the
 * app picks up the new palette without touching each component.
 */
const ink = {
    50: '#f7f4ec',
    100: '#efebe0',
    200: '#e0dacb',
    300: '#c9c2b0',
    400: '#9c9585',
    500: '#716b5e',
    600: '#555045',
    700: '#3d3931',
    800: '#262420',
    900: '#191813',
    950: '#100f0c',
};

const rigel = {
    50: '#eef1ff',
    100: '#e0e5ff',
    200: '#c6cfff',
    300: '#a1afff',
    400: '#7084ff',
    500: '#3d5afe',
    600: '#2440e6',
    700: '#1b30b8',
    800: '#17288f',
    900: '#131f6b',
};

const betelgeuse = {
    50: '#fdf0eb',
    100: '#fbddd2',
    200: '#f6bba6',
    300: '#f09274',
    400: '#ea6e48',
    500: '#e5532a',
    600: '#c9411c',
    700: '#a33316',
    800: '#7f2913',
    900: '#5e2010',
};

module.exports = {
    darkMode: 'class',
    content: [
        './index.html',
        './src/**/*.{js,jsx,ts,tsx}'
    ],
    theme: {
        extend: {
            colors: {
                slate: ink,
                ink,
                brand: rigel,
                red: betelgeuse,
                app: {
                    bg: 'rgb(var(--paper) / <alpha-value>)',
                    surface: 'rgb(var(--surface) / <alpha-value>)',
                    muted: 'rgb(var(--muted) / <alpha-value>)',
                    border: 'rgb(var(--line) / <alpha-value>)',
                    text: 'rgb(var(--ink) / <alpha-value>)',
                },
            },
            fontFamily: {
                sans: ['"Instrument Sans Variable"', 'system-ui', '-apple-system', 'Segoe UI', 'sans-serif'],
                display: ['"Bricolage Grotesque Variable"', '"Instrument Sans Variable"', 'system-ui', 'sans-serif'],
                mono: ['"JetBrains Mono Variable"', 'ui-monospace', 'SFMono-Regular', 'Menlo', 'monospace'],
            },
            borderRadius: {
                sm: '2px',
                DEFAULT: '3px',
                md: '4px',
                lg: '5px',
                xl: '6px',
                '2xl': '8px',
                app: '6px',
            },
            boxShadow: {
                card: '0 1px 0 rgb(var(--ink) / 0.05)',
                soft: '0 1px 0 rgb(var(--ink) / 0.08)',
                hard: '3px 3px 0 rgb(var(--ink))',
                'hard-sm': '2px 2px 0 rgb(var(--ink))',
            },
        },
    },
    plugins: [],
};
