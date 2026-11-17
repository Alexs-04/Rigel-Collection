/*
 * Rigel — "Orion" theme.
 *
 * Night-blue neutrals with Rigel (blue supergiant) as the action colour.
 * Danger uses a clean red; Betelgeuse's warm tone survives only as the sister
 * star in the login constellation.
 * `slate` and `red` are remapped on purpose so every existing utility in the
 * app picks up the palette without touching each component.
 */
const ink = {
    50: '#f5f7fb',
    100: '#ebeff6',
    200: '#dbe2ee',
    300: '#bfc9db',
    400: '#8b97b0',
    500: '#5f6b85',
    600: '#455069',
    700: '#2f3850',
    800: '#1b2236',
    900: '#111728',
    950: '#0a0f1e',
};

const rigel = {
    50: '#eef4ff',
    100: '#dce8ff',
    200: '#bfd4ff',
    300: '#93b6ff',
    400: '#5f8fff',
    500: '#2f6bff',
    600: '#1a52eb',
    700: '#153fc0',
    800: '#153699',
    900: '#142f77',
};

const danger = {
    50: '#fff1f1',
    100: '#ffe0e0',
    200: '#fdc5c6',
    300: '#f99a9c',
    400: '#f06a6e',
    500: '#e5484d',
    600: '#cd2b31',
    700: '#a82328',
    800: '#862024',
    900: '#6a1d20',
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
                red: danger,
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
                sm: '4px',
                DEFAULT: '5px',
                md: '6px',
                lg: '8px',
                xl: '10px',
                '2xl': '14px',
                app: '10px',
            },
            boxShadow: {
                card: '0 1px 2px rgb(13 19 36 / 0.04), 0 4px 16px -6px rgb(13 19 36 / 0.08)',
                soft: '0 1px 2px rgb(13 19 36 / 0.06)',
                raised: '0 2px 4px rgb(13 19 36 / 0.04), 0 18px 40px -16px rgb(13 19 36 / 0.22)',
                glow: '0 1px 2px rgb(13 19 36 / 0.12), 0 8px 22px -8px rgb(47 107 255 / 0.6)',
            },
        },
    },
    plugins: [],
};
