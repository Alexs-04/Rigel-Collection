import React from 'react'
import { NavLink } from 'react-router-dom'
import { Home, ShoppingCart, Box, Users, FileText, Settings } from 'lucide-react'

const items = [
	{ to: '/', label: 'Dashboard', icon: Home },
	{ to: '/pos', label: 'Punto de venta', icon: ShoppingCart },
	{ to: '/products', label: 'Productos', icon: Box },
	{ to: '/suppliers', label: 'Proveedores', icon: Users },
	{ to: '/logs', label: 'Bitácora', icon: FileText },
	{ to: '/settings', label: 'Configuración', icon: Settings },
]

export default function Sidebar({ collapsed = false }) {
	const width = collapsed ? 80 : 256 // px (approx 5rem / 16rem)
	return (
		<aside className="sidebar" style={{ width }}>
			<div
				className={`mb-6 px-2 flex items-center`}
				style={{
					display: 'flex',
					alignItems: 'center',
					gap: 12,
					justifyContent: collapsed ? 'center' : 'flex-start',
				}}
			>
				<div
					style={{
						height: 32,
						width: 32,
						background: '#6366f1',
						borderRadius: 8,
						display: 'flex',
						alignItems: 'center',
						justifyContent: 'center',
						color: 'white',
						fontWeight: 700,
					}}
				>
					R
				</div>
				{!collapsed && <h2 style={{ fontSize: 20, margin: 0 }}>Rigel</h2>}
			</div>

			<nav style={{ flex: 1 }}>
				{items.map((it) => {
					const Icon = it.icon
					return (
						<NavLink
							key={it.to}
							to={it.to}
							end={it.to === '/'}
							style={{
								display: 'flex',
								alignItems: 'center',
								gap: 12,
								padding: '8px 12px',
								borderRadius: 8,
								marginBottom: 6,
								color: '#e6eef8',
								textDecoration: 'none',
							}}
							className={({ isActive }) => (isActive ? 'active' : '')}
						>
							<Icon style={{ width: 18, height: 18 }} />
							{!collapsed && <span style={{ fontSize: 14 }}>{it.label}</span>}
						</NavLink>
					)
				})}
			</nav>

			{!collapsed && (
				<div
					style={{
						fontSize: 12,
						color: '#6b7280',
						marginTop: 16,
					}}
				>
					v0.1.0
				</div>
			)}
		</aside>
	)
}
