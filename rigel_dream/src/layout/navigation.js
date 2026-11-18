import {
    Home,
    ShoppingCart,
    Box,
    Users,
    FileText,
    Settings,
    ShoppingBag,
    User,
    PackageOpenIcon,
    BottleWine,
} from 'lucide-react'

/** Single source of truth for the sections of the app (sidebar, topbar and page headers). */
export const NAV_ITEMS = [
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
].map((item, index) => ({...item, index: String(index + 1).padStart(2, '0')}))

export function findNavItem(pathname) {
    return NAV_ITEMS.find((item) => item.to === pathname)
        || NAV_ITEMS.find((item) => item.to !== '/' && pathname.startsWith(item.to))
        || null
}
