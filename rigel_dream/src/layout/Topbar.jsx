import React, {useContext} from 'react'
import {AuthContext} from '../context/AuthContext'
import {LogOut, Moon, Sun} from 'lucide-react'
import {useLocation, useNavigate} from 'react-router-dom'
import {useAppSettings} from '../context/AppSettingsContext'
import {findNavItem} from './navigation'

const ROLE_LABELS = {ROOT: 'Root', ADMIN: 'Administrador', USER: 'Usuario', SUPPLIER: 'Proveedor'}

function initialsOf(name) {
    const parts = String(name || '').trim().split(/\s+/).filter(Boolean)
    if (!parts.length) return '·'
    return (parts[0][0] + (parts[1]?.[0] || '')).toUpperCase()
}

export default function Topbar() {
    const {user, logout} = useContext(AuthContext)
    const {settings, setTheme} = useAppSettings()
    const navigate = useNavigate()
    const {pathname} = useLocation()

    const section = findNavItem(pathname)
    const displayName = user?.name || user?.username || 'Invitado'
    const isDark = settings.theme === 'dark'
    const today = new Date().toLocaleDateString('es-MX', {weekday: 'short', day: '2-digit', month: 'short', year: 'numeric'})

    const doLogout = async () => {
        await logout()
        navigate('/login')
    }

    return (
        <header className="topbar justify-between gap-4">
            <div className="ui-eyebrow flex min-w-0 items-center gap-2">
                <span>Rigel</span>
                <span className="text-app-text/30">/</span>
                <span className="truncate text-app-text">{section?.label || 'Inicio'}</span>
            </div>

            <div className="flex items-center gap-1.5">
                <span className="ui-eyebrow mr-2 hidden md:inline">{today}</span>

                <button
                    onClick={() => setTheme(isDark ? 'light' : 'dark')}
                    className="ui-icon-btn"
                    aria-label={isDark ? 'Cambiar a tema claro' : 'Cambiar a tema oscuro'}
                    title={isDark ? 'Tema claro' : 'Tema oscuro'}
                >
                    {isDark ? <Sun className="h-[17px] w-[17px]"/> : <Moon className="h-[17px] w-[17px]"/>}
                </button>

                <div className="mx-1.5 flex items-center gap-2.5 border-l border-app-border pl-3.5">
                    <div className="grid h-8 w-8 place-items-center rounded-md bg-app-text font-mono text-xs font-semibold text-app-bg">
                        {initialsOf(displayName)}
                    </div>
                    <div className="hidden leading-tight sm:block">
                        <div className="text-sm font-semibold text-app-text">{displayName}</div>
                        {user?.role && <div className="ui-eyebrow !tracking-[0.08em]">{ROLE_LABELS[user.role] || user.role}</div>}
                    </div>
                </div>

                <button onClick={doLogout} className="ui-icon-btn" aria-label="Cerrar sesión" title="Cerrar sesión">
                    <LogOut className="h-[17px] w-[17px]"/>
                </button>
            </div>
        </header>
    )
}
