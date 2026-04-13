import React, {createContext, useContext, useEffect, useMemo, useState} from 'react';

const APP_SETTINGS_KEY = 'rigel:app_settings';

const DEFAULT_SETTINGS = {
    showFrontendErrors: true,
    debugEnabled: false,
    theme: 'light',
};

function readStoredSettings() {
    try {
        const raw = localStorage.getItem(APP_SETTINGS_KEY);
        if (!raw) return DEFAULT_SETTINGS;
        const parsed = JSON.parse(raw);
        return {
            ...DEFAULT_SETTINGS,
            ...parsed,
            theme: parsed?.theme === 'dark' ? 'dark' : 'light',
        };
    } catch {
        return DEFAULT_SETTINGS;
    }
}

const AppSettingsContext = createContext(null);

export function AppSettingsProvider({children}) {
    const [settings, setSettings] = useState(readStoredSettings);

    useEffect(() => {
        localStorage.setItem(APP_SETTINGS_KEY, JSON.stringify(settings));
    }, [settings]);

    useEffect(() => {
        const root = document.documentElement;
        root.classList.toggle('dark', settings.theme === 'dark');
    }, [settings.theme]);

    const actions = useMemo(() => ({
        setShowFrontendErrors(value) {
            setSettings((prev) => ({...prev, showFrontendErrors: Boolean(value)}));
        },
        setDebugEnabled(value) {
            setSettings((prev) => ({...prev, debugEnabled: Boolean(value)}));
        },
        setTheme(theme) {
            setSettings((prev) => ({...prev, theme: theme === 'dark' ? 'dark' : 'light'}));
        },
        resetSettings() {
            setSettings(DEFAULT_SETTINGS);
        },
    }), []);

    const value = useMemo(() => ({settings, ...actions}), [settings, actions]);

    return (
        <AppSettingsContext.Provider value={value}>
            {children}
        </AppSettingsContext.Provider>
    );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAppSettings() {
    const context = useContext(AppSettingsContext);
    if (!context) {
        throw new Error('useAppSettings must be used inside AppSettingsProvider');
    }
    return context;
}

