import React, {useState, useContext} from 'react';
import {AuthContext} from '../context/AuthContext';
import {useNavigate} from 'react-router-dom';
import {User, Lock, ArrowRight} from 'lucide-react';
import AuthShell from './ui/AuthShell';

export default function LoginForm() {
    const {login} = useContext(AuthContext);
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setLoading(true);
        try {
            const result = await login(email, password);
            if (!result?.success) {
                setError('Credenciales inválidas');
            } else {
                navigate('/');
            }
        } catch (err) {
            const status = err?.response?.status
            if (status === 401 || status === 403) {
                setError(err?.response?.data?.message || 'Tu cuenta no puede iniciar sesión en este momento')
            } else {
                setError(err?.response?.data?.message || 'Error al iniciar sesión')
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <AuthShell>
            <div className="ui-eyebrow mb-3">Acceso</div>
            <h1 className="mb-2 mt-0 text-3xl font-semibold text-app-text">Bienvenido de vuelta</h1>
            <p className="mb-6 mt-0 text-sm text-app-muted">Ingresa con tu cuenta para continuar.</p>

            <form onSubmit={handleSubmit} className="grid gap-4">
                <div>
                    <label htmlFor="login-email">Email</label>
                    <div className="relative">
                        <User className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-app-muted"/>
                        <input
                            id="login-email"
                            className="ui-input pl-9"
                            type="email"
                            autoComplete="username"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>
                </div>

                <div>
                    <label htmlFor="login-password">Contraseña</label>
                    <div className="relative">
                        <Lock className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-app-muted"/>
                        <input
                            id="login-password"
                            className="ui-input pl-9"
                            type="password"
                            autoComplete="current-password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>
                </div>

                {error && (
                    <div role="alert" className="border-l-2 border-red-500 bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-500/10">
                        {error}
                    </div>
                )}

                <div className="mt-1 flex items-center gap-3">
                    <button type="submit" disabled={loading} className="ui-btn-primary flex-1">
                        {loading ? 'Ingresando...' : 'Ingresar'}
                        {!loading && <ArrowRight className="h-4 w-4"/>}
                    </button>
                    <button type="button" className="ui-btn-ghost" onClick={() => {
                        setEmail('');
                        setPassword('')
                    }}>
                        Limpiar
                    </button>
                </div>

                <a className="text-sm text-app-text underline decoration-brand-500 decoration-2 underline-offset-4 hover:text-brand-600 dark:hover:text-brand-300"
                   href="/reset-password">
                    ¿Olvidaste tu contraseña?
                </a>
            </form>
        </AuthShell>
    );
}
