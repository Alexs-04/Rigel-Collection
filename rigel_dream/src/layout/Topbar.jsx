import React, { useContext } from 'react'
import { AuthContext } from '../context/AuthContext'
import { LogOut, Menu } from 'lucide-react'
import { useNavigate } from 'react-router-dom'

export default function Topbar({ onToggle }) {
  const { user, logout } = useContext(AuthContext)
  const navigate = useNavigate()

  const doLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <header className="topbar" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        <button onClick={onToggle} style={{ padding: 8, borderRadius: 6, background: 'transparent', border: 'none' }}>
          <Menu style={{ width: 18, height: 18 }} />
        </button>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <div style={{ height: 32, width: 32, background: '#6366f1', borderRadius: 6, display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', fontWeight: 700 }}>R</div>
          <div style={{ fontWeight: 700 }}>Rigel</div>
        </div>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        <div style={{ fontSize: 14 }}>{user?.username || 'Invitado'}</div>
        <button onClick={doLogout} style={{ padding: 8, borderRadius: 6, background: 'transparent', border: 'none' }}>
          <LogOut style={{ width: 18, height: 18 }} />
        </button>
      </div>
    </header>
  )
}
