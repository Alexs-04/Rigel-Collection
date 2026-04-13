import React from 'react'
import {useAppSettings} from '../context/AppSettingsContext'

function ToggleRow({title, description, checked, onChange}) {
    return (
        <div className="flex items-start justify-between gap-4 rounded-lg border border-app-border bg-app-surface p-4 dark:border-slate-700 dark:bg-slate-900">
            <div>
                <h3 className="text-sm font-semibold text-slate-900 dark:text-slate-100">{title}</h3>
                <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">{description}</p>
            </div>
            <button
                type="button"
                role="switch"
                aria-checked={checked}
                onClick={() => onChange(!checked)}
                className={`relative mt-1 inline-flex h-6 w-11 items-center rounded-full transition ${checked ? 'bg-brand-600' : 'bg-slate-300'}`}
            >
                <span className={`inline-block h-5 w-5 transform rounded-full bg-white transition ${checked ? 'translate-x-5' : 'translate-x-1'}`}/>
            </button>
        </div>
    )
}

export default function Settings() {
    const {
        settings,
        setShowFrontendErrors,
        setDebugEnabled,
        setTheme,
        resetSettings,
    } = useAppSettings()

    return (
        <div className="mx-auto max-w-4xl space-y-5 p-6">
            <header className="ui-card p-5">
                <h1 className="text-2xl font-bold text-slate-900 dark:text-slate-100">Configuracion</h1>
                <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">
                    Ajusta el comportamiento visual y de diagnostico de la aplicacion.
                </p>
            </header>

            <section className="space-y-3">
                <ToggleRow
                    title="Mostrar errores tecnicos en frontend"
                    description="Cuando esta activo se muestra el detalle tecnico capturado por el ErrorBoundary."
                    checked={settings.showFrontendErrors}
                    onChange={setShowFrontendErrors}
                />

                <ToggleRow
                    title="Habilitar panel de depuracion"
                    description="Muestra un panel flotante con datos de ruta, usuario y rol para diagnostico rapido."
                    checked={settings.debugEnabled}
                    onChange={setDebugEnabled}
                />

                <div className="rounded-lg border border-app-border bg-app-surface p-4 dark:border-slate-700 dark:bg-slate-900">
                    <h3 className="text-sm font-semibold text-slate-900 dark:text-slate-100">Apariencia</h3>
                    <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">Selecciona el tema visual de la aplicacion.</p>

                    <div className="mt-3 flex flex-wrap gap-2">
                        <button
                            type="button"
                            onClick={() => setTheme('light')}
                            className={settings.theme === 'light' ? 'ui-btn-primary' : 'ui-btn-ghost'}
                        >
                            Claro
                        </button>
                        <button
                            type="button"
                            onClick={() => setTheme('dark')}
                            className={settings.theme === 'dark' ? 'ui-btn-primary' : 'ui-btn-ghost'}
                        >
                            Oscuro
                        </button>
                    </div>
                </div>
            </section>

            <footer className="flex justify-end">
                <button type="button" className="ui-btn-ghost" onClick={resetSettings}>
                    Restablecer valores
                </button>
            </footer>
        </div>
    )
}

