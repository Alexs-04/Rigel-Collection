module.exports = {
  darkMode: 'class',
  content: [
    './index.html',
    './src/**/*.{js,jsx,ts,tsx}'
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#eef2ff',
          100: '#e0e7ff',
          200: '#c7d2fe',
          300: '#a5b4fc',
          400: '#818cf8',
          500: '#6366f1',
          600: '#4f46e5',
          700: '#4338ca',
        },
        app: {
          bg: '#f8fafc',
          surface: '#ffffff',
          muted: '#64748b',
          border: '#e2e8f0',
          text: '#0f172a',
        },
      },
      borderRadius: {
        app: '12px',
      },
      boxShadow: {
        card: '0 10px 30px rgba(2, 6, 23, 0.08)',
        soft: '0 2px 10px rgba(2, 6, 23, 0.06)',
      },
    },
  },
  plugins: [],
};

