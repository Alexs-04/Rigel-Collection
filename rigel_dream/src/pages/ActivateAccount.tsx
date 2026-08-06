import {useState} from 'react'
import {useSearchParams, useNavigate} from 'react-router-dom'
import api from '../services/api'

type Status = 'idle' | 'loading' | 'success' | 'error'

function normalizeError(error: unknown): string {
    const maybeError = error as {response?: {data?: {message?: string}}}
    return maybeError?.response?.data?.message || 'Enlace invalido o expirado. Contacta al administrador.'
}

export default function ActivateAccountPage() {
    const [searchParams] = useSearchParams()
    const navigate = useNavigate()
    const token = searchParams.get('token') ?? ''

    const [password, setPassword] = useState('')
    const [confirm, setConfirm] = useState('')
    const [status, setStatus] = useState<Status>('idle')
    const [error, setError] = useState('')

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        setError('')

        if (password.length < 8) {
            setError('La contraseña debe tener al menos 8 caracteres.')
            return
        }
        if (password !== confirm) {
            setError('Las contraseñas no coinciden.')
            return
        }
        if (!token) {
            setError('Token de activación no encontrado en el enlace.')
            return
        }

        setStatus('loading')
        try {
            await api.post('/account/activate', {token, password})
            setStatus('success')
        } catch (err) {
            setError(normalizeError(err))
            setStatus('error')
        }
    }

    if (status === 'success') {
        return (
            <div className="flex min-h-screen items-center justify-center bg-slate-50 p-4">
                <div className="ui-card w-full max-w-md p-8 text-center">
                    <div className="mb-4 text-4xl"></div>
                    <h1 className="ui-title mb-2 text-xl font-semibold">Cuenta activada</h1>
                    <p className="ui-muted mb-6 text-sm">Tu cuenta ha sido activada correctamente. Ya puedes iniciar sesion.</p>
                    <button className="ui-btn-primary w-full" onClick={() => navigate('/login')}>
                        Ir al inicio de sesion
                    </button>
                </div>
            </div>
        )
    }

    return (
        <div className="flex min-h-screen items-center justify-center bg-slate-50 p-4">
            <div className="ui-card w-full max-w-md p-8">
                <h1 className="ui-title mb-1 mt-0 text-xl font-semibold">Activar cuenta</h1>
                <p className="ui-muted mb-6 mt-0 text-sm">Establece una contrasena para completar el registro.</p>

                {!token && (
                    <p className="mb-4 text-sm text-red-600">
                        El enlace no contiene un token valido. Verifica que hayas abierto el correo correctamente.
                    </p>
                )}

                <form onSubmit={handleSubmit} className="grid gap-3">
                    <input
                        className="ui-input"
                        type="password"
                        placeholder="Nueva contrasena (min. 8 caracteres)"
                        value={password}
                        required
                        minLength={8}
                        disabled={status === 'loading' || !token}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                    <input
                        className="ui-input"
                        type="password"
                        placeholder="Confirmar contrasena"
                        value={confirm}
                        required
                        disabled={status === 'loading' || !token}
                        onChange={(e) => setConfirm(e.target.value)}
                    />

                    {error && <p className="text-sm text-red-600">{error}</p>}

                    <button
                        className="ui-btn-primary"
                        type="submit"
                        disabled={status === 'loading' || !token}
                    >
                        {status === 'loading' ? 'Activando...' : 'Activar cuenta'}
                    </button>
                </form>
            </div>
        </div>
    )
}