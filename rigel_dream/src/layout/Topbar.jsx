import React, {useContext} from 'react'
import {AuthContext} from '../context/AuthContext'
import {LogOut} from 'lucide-react'
import {useNavigate} from 'react-router-dom'

export default function Topbar() {
    const {user, logout} = useContext(AuthContext)
    const navigate = useNavigate()

    const doLogout = () => {
        logout()
        navigate('/login')
    }

    return (
        <header className="topbar justify-between px-4">
            {/* Left: brand */}
            <div className="flex items-center gap-3">
                <div
                    className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand-500 text-white"
                >
                    ⭐
                </div>
                <div className="ui-title font-bold">Rigel</div>
            </div>

            {/* Right area: user + logout */}
            <div className="ml-auto flex items-center gap-3">
                <div className="ui-title text-sm font-medium">{user?.username || 'Invitado'}</div>
                <button
                    onClick={doLogout}
                    className="rounded-md p-2 text-slate-700 transition hover:bg-brand-50 hover:text-brand-700 dark:text-slate-100 dark:hover:bg-slate-800 dark:hover:text-brand-300"
                    aria-label="Cerrar sesión"
                >
                    <LogOut className="h-[18px] w-[18px]"/>
                </button>
            </div>
        </header>
    )
}
