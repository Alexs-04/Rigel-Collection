import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useContext, useState } from 'react'
import './App.css'
import LoginForm from './components/LoginForm'
import Sidebar from './layout/Sidebar'
import Topbar from './layout/Topbar'
import Dashboard from './pages/Dashboard'
import Products from './pages/Products'
import Suppliers from './pages/Suppliers'
import POS from './pages/POS'
import Settings from './pages/Settings'
import Logs from './pages/Logs'
import { AuthContext } from './context/AuthContext'

function AppLayout({ children }) {
  const [collapsed, setCollapsed] = useState(false)

  const toggle = () => setCollapsed((c) => !c)

  return (
    <div className="app-flex">
      <Sidebar collapsed={collapsed} />
      <div className="main">
        <Topbar onToggle={toggle} />
        <main className="content">{children}</main>
      </div>
    </div>
  )
}

function AppRoutes() {
  const { user } = useContext(AuthContext)

  if (!user) {
    return <Navigate to="/login" replace />
  }

  return (
    <AppLayout>
      <Routes>
        <Route path="/" element={<Dashboard />} />
        <Route path="/products" element={<Products />} />
        <Route path="/suppliers" element={<Suppliers />} />
        <Route path="/pos" element={<POS />} />
        <Route path="/logs" element={<Logs />} />
        <Route path="/settings" element={<Settings />} />
      </Routes>
    </AppLayout>
  )
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<LoginForm />} />
        <Route path="/*" element={<AppRoutes />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
