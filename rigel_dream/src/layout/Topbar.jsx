import React, { useContext } from 'react'
import { AuthContext } from '../context/AuthContext'
import { LogOut } from 'lucide-react'
import { useNavigate } from 'react-router-dom'

export default function Topbar() {
  const { user, logout } = useContext(AuthContext)
  const navigate = useNavigate()

  const doLogout = () => {
    logout()
    navigate('/login')
  }

  const accent = '#6366f1'
  const textColor = '#111827'

  return (
    <header
      className="topbar"
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '8px 16px',
        height: 56,
        borderBottom: '1px solid rgba(0,0,0,0.06)',
        background: 'white',
      }}
    >
      {/* Left: brand */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        <div
          style={{
            height: 36,
            width: 36,
            background: accent,
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
        <div style={{ fontWeight: 700, color: textColor }}>Rigel</div>
      </div>

      {/* Right area: user + logout */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginLeft: 'auto' }}>
        <div style={{ fontSize: 14, fontWeight: 500, color: textColor }}>{user?.username || 'Invitado'}</div>
        <button
          onClick={doLogout}
          style={{ padding: 8, borderRadius: 6, background: 'transparent', border: 'none', cursor: 'pointer' }}
          aria-label="Cerrar sesión"
        >
          <LogOut style={{ width: 18, height: 18, color: textColor }} />
        </button>
      </div>
    </header>
  )
}
