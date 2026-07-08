import {BrowserRouter, Routes, Route, Navigate} from 'react-router-dom'
import {useContext, useState} from 'react'
import LoginForm from './components/LoginForm'
import Sidebar from './layout/Sidebar'
import Topbar from './layout/Topbar'
import Dashboard from './pages/Dashboard.tsx'
import Products from './pages/Products'
import Suppliers from './pages/Suppliers.tsx'
import POS from './pages/POS'
import Settings from './pages/Settings'
import Logs from './pages/Logs'
import Users from './pages/Users'
import Purchases from "./pages/Purchases.tsx";
import Inventory from "./pages/Inventory.tsx";
import Amounts from "./pages/Amounts.tsx";
import {AuthContext} from './context/AuthContext'
import DebugPanel from './components/DebugPanel'
import ResetPasswordPage from "./pages/ResetPassword.tsx";
import ActivateAccountPage from "./pages/ActivateAccount.tsx";

function AppLayout({children}) {
    const [collapsed, setCollapsed] = useState(false)

    const toggle = () => setCollapsed((c) => !c)

    return (
        <div className="app-flex">
            <Sidebar collapsed={collapsed} onToggle={toggle}/>
            <div className="main">
                <Topbar/>
                <main className="content">{children}</main>
            </div>
        </div>
    )
}

function RoleRoute({children, allowedRoles}) {
    const {user} = useContext(AuthContext)

    if (!user) return <Navigate to="/login" replace/>
    if (!allowedRoles.includes(user.role)) return <Navigate to="/" replace/>

    return children
}

function AppRoutes() {
    const {user, logout} = useContext(AuthContext)

    if (!user) {
        return <Navigate to="/login" replace/>
    }

    if (user.active === false) {
        logout()
        return <Navigate to="/login" replace/>
    }

    return (
        <AppLayout>
            <Routes>
                <Route path="/" element={<Dashboard/>}/>
                <Route path="/products" element={<Products/>}/>
                <Route path="/suppliers" element={<Suppliers/>}/>
                <Route path="/pos" element={<RoleRoute allowedRoles={['ROOT', 'ADMIN', 'USER']}><POS/></RoleRoute>}/>
                <Route path="/logs" element={<RoleRoute allowedRoles={['ROOT', 'ADMIN']}><Logs/></RoleRoute>}/>
                <Route path="/settings" element={<RoleRoute allowedRoles={['ROOT', 'ADMIN']}><Settings/></RoleRoute>}/>
                <Route path="/users" element={<RoleRoute allowedRoles={['ROOT']}><Users/></RoleRoute>}/>
                <Route path="/purchases"
                       element={<RoleRoute allowedRoles={['ROOT', 'ADMIN']}><Purchases/></RoleRoute>}/>
                <Route path="/inventory"
                       element={<RoleRoute allowedRoles={['ROOT', 'ADMIN', 'USER']}><Inventory/></RoleRoute>}/>
                <Route path="/amounts" element={<RoleRoute allowedRoles={['ROOT', 'ADMIN', 'USER']}><Amounts /></RoleRoute>}/>
                <Route path="/activate" element={<ActivateAccountPage />} />
                <Route path="/reset-password" element={<ResetPasswordPage />} />
            </Routes>
        </AppLayout>
    )
}

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/login" element={<LoginForm/>}/>
                <Route path="/*" element={<AppRoutes/>}/>
            </Routes>
            <DebugPanel/>
        </BrowserRouter>
    )
}

export default App
