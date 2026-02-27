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

  return (
    <header className="topbar">
      <div />
      <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
        <div style={{ fontSize: 14 }}>{user?.username || 'Invitado'}</div>
        <button onClick={doLogout} style={{ padding: 8, borderRadius: 6 }}>
          <LogOut style={{ width: 18, height: 18 }} />
        </button>
      </div>
    </header>
  )
}
