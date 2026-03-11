import React, {useContext} from 'react'
import {NavLink} from 'react-router-dom'
import {Home, ShoppingCart, Box, Users, FileText, Settings, Menu, ShoppingBag, User, PackageOpenIcon} from 'lucide-react'
import {AuthContext} from '../context/AuthContext'

const items = [
    {to: '/', label: 'Dashboard', icon: Home, roles: ['ROOT', 'ADMIN', 'USER', 'SUPPLIER']},
    {to: '/pos', label: 'Punto de venta', icon: ShoppingCart, roles: ['ROOT', 'ADMIN', 'USER']},
    {to: '/products', label: 'Productos', icon: Box, roles: ['ROOT', 'ADMIN', 'USER', 'SUPPLIER']},
    {to: '/suppliers', label: 'Proveedores', icon: Users, roles: ['ROOT', 'ADMIN', 'USER', 'SUPPLIER']},
    {to: '/purchases', label: 'Compras', icon: ShoppingBag, roles: ['ROOT', 'ADMIN']},
    {to: '/logs', label: 'Bitácora', icon: FileText, roles: ['ROOT', 'ADMIN']},
    {to : '/inventory', label: 'Inventario', icon: PackageOpenIcon, roles: ['ROOT', 'ADMIN', 'USER']},
    {to: '/users', label: 'Usuarios', icon: User, roles: ['ROOT']},
    {to: '/settings', label: 'Configuración', icon: Settings, roles: ['ROOT', 'ADMIN']},
]

export default function Sidebar({collapsed = false, onToggle = () => {}}) {
    const {user} = useContext(AuthContext)
    const width = collapsed ? 80 : 256 // px
    const textColor = '#111827' // slate-900
    const mutedColor = '#6b7280' // slate-500
    const activeBg = '#eef2ff' // light purple-ish
    const accent = '#6366f1' // indigo-500
    const visibleItems = items.filter((it) => !user?.role || it.roles.includes(user.role))

    return (
        <aside
            className="sidebar"
            style={{
                width,
                display: 'flex',
                flexDirection: 'column',
                padding: '8px 16px 16px',
                position: 'sticky',
                top: 0,
                height: '100vh',
                boxSizing: 'border-box',
                background: 'white',
                borderRight: '1px solid rgba(2,6,23,0.04)',
            }}
        >
            {/* Header: alineado con la altura y el eje vertical del topbar */}
            <div
                style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: collapsed ? 'center' : 'flex-start',
                    height: 56,
                    marginBottom: 16,
                }}
            >
                <button
                    onClick={onToggle}
                    aria-label="Toggle sidebar"
                    style={{
                        background: 'transparent',
                        border: 'none',
                        padding: 8,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: textColor,
                        cursor: 'pointer',
                        marginLeft: 0,
                        marginRight: 0,
                        borderRadius: 8,
                    }}
                >
                    <Menu style={{width: 20, height: 20}}/>
                </button>
            </div>

            <nav style={{flex: 1, display: 'flex', flexDirection: 'column'}}>
                {visibleItems.map((it) => {
                    const Icon = it.icon
                    return (
                        <NavLink
                            key={it.to + it.label}
                            to={it.to}
                            end={it.to === '/'}
                            style={({isActive}) => ({
                                display: 'flex',
                                alignItems: 'center',
                                gap: 12,
                                padding: '10px 12px',
                                borderRadius: 8,
                                marginBottom: 6,
                                color: isActive ? textColor : mutedColor,
                                background: isActive ? activeBg : 'transparent',
                                textDecoration: 'none',
                            })}
                        >
                            {({isActive}) => (
                                <>
                                    <Icon style={{width: 18, height: 18, color: isActive ? accent : mutedColor}}/>
                                    {!collapsed && <span style={{fontSize: 14}}>{it.label}</span>}
                                </>
                            )}
                        </NavLink>
                    )
                })}
            </nav>

            {!collapsed && (
                <div
                    style={{
                        fontSize: 12,
                        color: mutedColor,
                        marginTop: 16,
                    }}
                >
                    v0.1.0
                </div>
            )}
        </aside>
    )
}
