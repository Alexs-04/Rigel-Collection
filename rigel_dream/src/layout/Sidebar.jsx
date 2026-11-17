import React, {useContext} from 'react'
import {NavLink} from 'react-router-dom'
import {PanelLeftClose, PanelLeftOpen} from 'lucide-react'
import {AuthContext} from '../context/AuthContext'
import RigelMark from '../components/ui/RigelMark'
import {NAV_ITEMS} from './navigation'

export default function Sidebar({
                                    collapsed = false, onToggle = () => {
    }
                                }) {
    const {user} = useContext(AuthContext)
    const visibleItems = NAV_ITEMS.filter((it) => !user?.role || it.roles.includes(user.role))
    const ToggleIcon = collapsed ? PanelLeftOpen : PanelLeftClose

    return (
        <aside
            className={`sidebar sticky top-0 flex h-screen shrink-0 flex-col border-r border-ink-800 bg-ink-950 text-ink-100 transition-[width] duration-200 ${collapsed ? 'w-[72px]' : 'w-60'}`}
        >
            <div className={`flex h-14 items-center border-b border-ink-800 ${collapsed ? 'justify-center' : 'justify-between pl-5 pr-3'}`}>
                {!collapsed && (
                    <div className="flex items-center gap-2.5">
                        <RigelMark className="h-5 w-5 text-brand-400"/>
                        <span className="font-display text-xl font-semibold tracking-tight">Rigel</span>
                    </div>
                )}
                <button
                    onClick={onToggle}
                    aria-label={collapsed ? 'Expandir menú' : 'Contraer menú'}
                    className="grid h-9 w-9 place-items-center rounded-md text-ink-400 transition hover:bg-ink-800 hover:text-ink-50"
                >
                    <ToggleIcon className="h-[18px] w-[18px]"/>
                </button>
            </div>

            <nav className="flex flex-1 flex-col gap-0.5 overflow-y-auto px-3 py-4">
                {!collapsed && <div className="mb-2 px-2 font-mono text-[0.65rem] uppercase tracking-[0.14em] text-ink-500">Secciones</div>}
                {visibleItems.map((it) => {
                    const Icon = it.icon
                    return (
                        <NavLink
                            key={it.to}
                            to={it.to}
                            end={it.to === '/'}
                            title={collapsed ? it.label : undefined}
                            className={({isActive}) =>
                                [
                                    'group flex items-center gap-3 rounded-md py-2 text-sm no-underline transition',
                                    collapsed ? 'justify-center px-0' : 'px-2',
                                    isActive
                                        ? 'bg-ink-50 font-semibold text-ink-950'
                                        : 'text-ink-300 hover:bg-ink-800 hover:text-ink-50',
                                ].join(' ')
                            }
                        >
                            {({isActive}) => (
                                <>
                                    {!collapsed && (
                                        <span className={`w-5 font-mono text-[0.68rem] font-normal ${isActive ? 'text-brand-600' : 'text-ink-500 group-hover:text-ink-300'}`}>
                                            {it.index}
                                        </span>
                                    )}
                                    <Icon className="h-[17px] w-[17px] shrink-0" strokeWidth={isActive ? 2.2 : 1.8}/>
                                    {!collapsed && <span className="truncate">{it.label}</span>}
                                </>
                            )}
                        </NavLink>
                    )
                })}
            </nav>

            <div className={`border-t border-ink-800 py-3 font-mono text-[0.65rem] tracking-[0.12em] text-ink-500 ${collapsed ? 'text-center' : 'px-5'}`}>
                {collapsed ? 'β' : 'β ORIONIS · V0.1.0'}
            </div>
        </aside>
    )
}
