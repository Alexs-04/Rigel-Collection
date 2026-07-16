import React, {useState, useContext} from 'react';
import {AuthContext} from '../context/AuthContext';
import {useNavigate} from 'react-router-dom';
import {User, Lock} from 'lucide-react';

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
        <div className="center-screen bg-gradient">
            <div className="w-full max-w-[420px]">
                <div className="ui-card p-7">
                    <div className="mb-3 flex items-center gap-2">
                        <div className="grid h-8 w-8 place-items-center rounded-lg bg-brand-600 text-sm font-bold text-white">R</div>
                        <div className="text-lg font-semibold text-slate-900">Rigel</div>
                    </div>

                    <h2 className="mb-1.5 text-xl font-semibold text-slate-900">Bienvenido de vuelta</h2>
                    <p className="mb-4 text-sm text-slate-500">Ingresa con tu cuenta para continuar</p>

                    <form onSubmit={handleSubmit} className="grid gap-3">
                        <div>
                            <label>Email</label>
                            <div className="flex items-center gap-2">
                                <div className="inline-flex items-center rounded-lg bg-slate-100 p-2">
                                    <User className="h-4 w-4 text-slate-500"/>
                                </div>
                                <input
                                    className="ui-input"
                                    type="email"
                                    value={email}
                                    onChange={(e) => setEmail(e.target.value)}
                                    required
                                />
                            </div>
                        </div>

                        <div>
                            <label>Contraseña</label>
                            <div className="flex items-center gap-2">
                                <div className="inline-flex items-center rounded-lg bg-slate-100 p-2">
                                    <Lock className="h-4 w-4 text-slate-500"/>
                                </div>
                                <input
                                    className="ui-input"
                                    type="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                />
                            </div>
                        </div>

                        {error && <div className="text-sm text-red-500">{error}</div>}

                        <div className="flex items-center gap-3">
                            <button type="submit" disabled={loading} className="ui-btn-primary flex-1">
                                {loading ? 'Ingresando...' : 'Ingresar'}
                            </button>
                            <button type="button" className="ui-btn-ghost" onClick={() => {
                                setEmail('');
                                setPassword('')
                            }}>
                                Limpiar
                            </button>
                        </div>

                        <div className="flex justify-between text-xs text-slate-500">
                            <div></div>
                            <a className="text-brand-600 no-underline hover:text-brand-700" href="/reset-password">¿Olvidaste tu
                                contraseña?</a>
                        </div>
                    </form>
                </div>

                <div className="mt-3 text-center text-xs text-slate-500">
                    © {new Date().getFullYear()} Rigel
                </div>
            </div>
        </div>
    );
}
