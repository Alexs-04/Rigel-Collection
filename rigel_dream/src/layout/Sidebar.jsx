import React from 'react'
import { NavLink } from 'react-router-dom'
import { Home, ShoppingCart, Box, Users, FileText, Settings, Menu, ShoppingBag } from 'lucide-react'

const items = [
	{ to: '/', label: 'Dashboard', icon: Home },
	{ to: '/pos', label: 'Punto de venta', icon: ShoppingCart },
	{ to: '/products', label: 'Productos', icon: Box },
	{ to: '/suppliers', label: 'Proveedores', icon: Users },
	{ to: '/purchases', label: 'Compras', icon: ShoppingBag },
	{ to: '/logs', label: 'Bitácora', icon: FileText },
	{ to: '/settings', label: 'Configuración', icon: Settings },
]

export default function Sidebar({ collapsed = false, onToggle = () => {} }) {
	const width = collapsed ? 80 : 256 // px
	const textColor = '#111827' // slate-900
	const mutedColor = '#6b7280' // slate-500
	const activeBg = '#eef2ff' // light purple-ish
	const accent = '#6366f1' // indigo-500

	return (
		<aside
			className="sidebar"
			style={{
				width,
				display: 'flex',
				flexDirection: 'column',
				padding: 16,
				position: 'sticky',
				top: 0,
				height: '100vh',
				boxSizing: 'border-box',
				background: 'white',
				borderRight: '1px solid rgba(2,6,23,0.04)',
			}}
		>
			{/* Header: only the hamburguesa button, no logo/text */}
			<div
				className={`mb-6 px-2 flex items-center`}
				style={{
					display: 'flex',
					alignItems: 'center',
					gap: 18,
					justifyContent: collapsed ? 'center' : 'flex-start',
					paddingTop: 8,
				}}
			>
				<button
					onClick={onToggle}
					aria-label="Toggle sidebar"
					style={{
						background: 'transparent',
						border: 'none',
						padding: 10,
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
					<Menu style={{ width: 20, height: 20 }} />
				</button>
			</div>

			<nav style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
				{items.map((it) => {
					const Icon = it.icon
					return (
						<NavLink
							key={it.to + it.label}
							to={it.to}
							end={it.to === '/'}
							style={({ isActive }) => ({
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
							{({ isActive }) => (
								<>
									<Icon style={{ width: 18, height: 18, color: isActive ? accent : mutedColor }} />
									{!collapsed && <span style={{ fontSize: 14 }}>{it.label}</span>}
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
