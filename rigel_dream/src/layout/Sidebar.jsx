import React from 'react'
import { NavLink } from 'react-router-dom'
import { Home, Box, Users, ShoppingCart, Settings } from 'lucide-react'

const items = [
  { to: '/', label: 'Dashboard', icon: Home },
  { to: '/products', label: 'Productos', icon: Box },
  { to: '/suppliers', label: 'Proveedores', icon: Users },
  { to: '/pos', label: 'Punto de venta', icon: ShoppingCart },
  { to: '/settings', label: 'Configuración', icon: Settings },
]

export default function Sidebar() {
  return (
    <aside className="w-64 bg-gray-900 text-gray-100 min-h-screen p-4 flex flex-col">
      <div className="mb-6 px-2">
        <h2 className="text-2xl font-bold">Rigel</h2>
      </div>
      <nav className="flex-1">
        {items.map((it) => {
          const Icon = it.icon
          return (
            <NavLink
              key={it.to}
              to={it.to}
              end={it.to === '/'}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2 rounded-md mb-1 transition-colors duration-150 ${
                  isActive ? 'bg-gray-800 text-white' : 'text-gray-300 hover:bg-gray-800'
                }`
              }
            >
              <Icon className="w-5 h-5" />
              <span className="text-sm">{it.label}</span>
            </NavLink>
          )
        })}
      </nav>
      <div className="text-xs text-gray-500 mt-4 px-3">v0.1.0</div>
    </aside>
  )
}
