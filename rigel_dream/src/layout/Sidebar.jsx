import React, {useContext} from 'react'
import {NavLink} from 'react-router-dom'
import {
    Home,
    ShoppingCart,
    Box,
    Users,
    FileText,
    Settings,
    Menu,
    ShoppingBag,
    User,
    PackageOpenIcon,
    BottleWine
} from 'lucide-react'
import {AuthContext} from '../context/AuthContext'

const items = [
    {to: '/', label: 'Dashboard', icon: Home, roles: ['ROOT', 'ADMIN', 'USER', 'SUPPLIER']},
    {to: '/pos', label: 'Punto de venta', icon: ShoppingCart, roles: ['ROOT', 'ADMIN', 'USER']},
    {to: '/amounts', label: 'Importes', icon: BottleWine, roles: ['ROOT', 'ADMIN', 'USER']},
    {to: '/products', label: 'Productos', icon: Box, roles: ['ROOT', 'ADMIN', 'USER', 'SUPPLIER']},
    {to: '/suppliers', label: 'Proveedores', icon: Users, roles: ['ROOT', 'ADMIN', 'USER', 'SUPPLIER']},
    {to: '/purchases', label: 'Compras', icon: ShoppingBag, roles: ['ROOT', 'ADMIN']},
    {to: '/logs', label: 'Bitácora', icon: FileText, roles: ['ROOT', 'ADMIN']},
    {to: '/inventory', label: 'Inventario', icon: PackageOpenIcon, roles: ['ROOT', 'ADMIN', 'USER']},
    {to: '/users', label: 'Usuarios', icon: User, roles: ['ROOT']},
    {to: '/settings', label: 'Configuración', icon: Settings, roles: ['ROOT', 'ADMIN']},
]

export default function Sidebar({
                                    collapsed = false, onToggle = () => {
    }
                                }) {
    const {user} = useContext(AuthContext)
    const width = collapsed ? 80 : 256
    const visibleItems = items.filter((it) => !user?.role || it.roles.includes(user.role))

    return (
        <aside
            className="sidebar sticky top-0 flex h-screen flex-col border-r border-app-border bg-white px-4 pb-4 pt-2 dark:border-slate-700 dark:bg-slate-900"
            style={{
                width,
                boxSizing: 'border-box',
            }}
        >
            <div className={`mb-4 flex h-14 items-center ${collapsed ? 'justify-center' : 'justify-start'}`}>
                <button
                    onClick={onToggle}
                    aria-label="Toggle sidebar"
                    className="flex items-center justify-center rounded-lg p-2 text-slate-900 transition hover:bg-brand-50 dark:text-slate-100 dark:hover:bg-slate-800"
                >
                    <Menu className="h-5 w-5"/>
                </button>
            </div>

            <nav className="flex flex-1 flex-col">
                {visibleItems.map((it) => {
                    const Icon = it.icon
                    return (
                        <NavLink
                            key={it.to + it.label}
                            to={it.to}
                            end={it.to === '/'}
                            className={({isActive}) =>
                                [
                                    'mb-1.5 flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm no-underline transition',
                                    isActive
                                        ? 'bg-brand-50 text-slate-900 dark:bg-brand-500/20 dark:text-slate-100'
                                        : 'text-slate-500 hover:bg-brand-50 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-slate-800 dark:hover:text-slate-100',
                                ].join(' ')
                            }
                        >
                            {({isActive}) => (
                                <>
                                    <Icon className={`h-[18px] w-[18px] ${isActive ? 'text-brand-500 dark:text-brand-300' : 'text-slate-500 dark:text-slate-400'}`}/>
                                    {!collapsed && <span>{it.label}</span>}
                                </>
                            )}
                        </NavLink>
                    )
                })}
            </nav>

            {!collapsed && (
                <div className="ui-muted mt-4 text-xs">
                    v0.1.0
                </div>
            )}
        </aside>
    )
}
