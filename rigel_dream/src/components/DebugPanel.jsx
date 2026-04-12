import {useContext} from 'react';
import {useLocation} from 'react-router-dom';
import {AuthContext} from '../context/AuthContext';
import {useAppSettings} from '../context/AppSettingsContext';

export default function DebugPanel() {
    const {settings} = useAppSettings();
    const {user} = useContext(AuthContext);
    const location = useLocation();

    if (!settings.debugEnabled) return null;

    return (
        <aside className="fixed bottom-4 right-4 z-50 w-80 rounded-app border border-amber-200 bg-amber-50 p-3 text-xs text-amber-900 shadow-soft">
            <h3 className="mb-2 text-sm font-semibold">Depuracion</h3>
            <div className="space-y-1">
                <p><span className="font-semibold">Ruta:</span> {location.pathname}</p>
                <p><span className="font-semibold">Usuario:</span> {user?.username || 'anonimo'}</p>
                <p><span className="font-semibold">Rol:</span> {user?.role || 'N/A'}</p>
                <p><span className="font-semibold">Tema:</span> {settings.theme === 'dark' ? 'Oscuro' : 'Claro'}</p>
            </div>
        </aside>
    );
}

