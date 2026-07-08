import {useState} from 'react'
import {useSearchParams, useNavigate} from 'react-router-dom'
import api from '../services/api'

type Status = 'idle' | 'loading' | 'success' | 'error'

function normalizeError(error: unknown): string {
    const maybeError = error as {response?: {data?: {message?: string}}}
    return maybeError?.response?.data?.message || 'Enlace invalido o expirado. Solicita uno nuevo.'
}

export default function ResetPasswordPage() {
    const [searchParams] = useSearchParams()
    const navigate = useNavigate()
    const token = searchParams.get('token') ?? ''

    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [confirm, setConfirm] = useState('')
    const [forgotStatus, setForgotStatus] = useState<Status>('idle')
    const [resetStatus, setResetStatus] = useState<Status>('idle')
    const [forgotMessage, setForgotMessage] = useState('')
    const [error, setError] = useState('')

    // ── With token: reset password ──────────────────────────────────────────
    const handleReset = async (e: React.FormEvent) => {
        e.preventDefault()
        setError('')

        if (password.length < 8) {
            setError('La contrasena debe tener al menos 8 caracteres.')
            return
        }
        if (password !== confirm) {
            setError('Las contrasenas no coinciden.')
            return
        }

        setResetStatus('loading')
        try {
            await api.post('/account/reset-password', {token, password})
            setResetStatus('success')
        } catch (err) {
            setError(normalizeError(err))
            setResetStatus('error')
        }
    }

    // ── Without token: request reset email ─────────────────────────────────
    const handleForgot = async (e: React.FormEvent) => {
        e.preventDefault()
        setForgotStatus('loading')
        setForgotMessage('')
        try {
            await api.post('/account/forgot-password', {email})
            // Backend always returns 200, message is informative
            setForgotMessage('Si el correo esta registrado, recibiras un enlace en breve.')
            setForgotStatus('success')
        } catch {
            setForgotMessage('No se pudo procesar la solicitud. Intenta de nuevo.')
            setForgotStatus('error')
        }
    }

    // ── Success screen ──────────────────────────────────────────────────────
    if (resetStatus === 'success') {
        return (
            <div className="flex min-h-screen items-center justify-center bg-slate-50 p-4">
                <div className="ui-card w-full max-w-md p-8 text-center">
                    <div className="mb-4 text-4xl">✅</div>
                    <h1 className="ui-title mb-2 text-xl font-semibold">Contrasena actualizada</h1>
                    <p className="ui-muted mb-6 text-sm">Tu contrasena ha sido cambiada correctamente.</p>
                    <button className="ui-btn-primary w-full" onClick={() => navigate('/login')}>
                        Ir al inicio de sesion
                    </button>
                </div>
            </div>
        )
    }

    // ── Reset form (has token) ──────────────────────────────────────────────
    if (token) {
        return (
            <div className="flex min-h-screen items-center justify-center bg-slate-50 p-4">
                <div className="ui-card w-full max-w-md p-8">
                    <h1 className="ui-title mb-1 mt-0 text-xl font-semibold">Nueva contrasena</h1>
                    <p className="ui-muted mb-6 mt-0 text-sm">Elige una contrasena segura para tu cuenta.</p>

                    <form onSubmit={handleReset} className="grid gap-3">
                        <input
                            className="ui-input"
                            type="password"
                            placeholder="Nueva contrasena (min. 8 caracteres)"
                            value={password}
                            required
                            minLength={8}
                            disabled={resetStatus === 'loading'}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                        <input
                            className="ui-input"
                            type="password"
                            placeholder="Confirmar contrasena"
                            value={confirm}
                            required
                            disabled={resetStatus === 'loading'}
                            onChange={(e) => setConfirm(e.target.value)}
                        />

                        {error && <p className="text-sm text-red-600">{error}</p>}

                        <button className="ui-btn-primary" type="submit" disabled={resetStatus === 'loading'}>
                            {resetStatus === 'loading' ? 'Guardando...' : 'Cambiar contrasena'}
                        </button>
                    </form>
                </div>
            </div>
        )
    }

    // ── Forgot form (no token) ──────────────────────────────────────────────
    return (
        <div className="flex min-h-screen items-center justify-center bg-slate-50 p-4">
            <div className="ui-card w-full max-w-md p-8">
                <h1 className="ui-title mb-1 mt-0 text-xl font-semibold">Recuperar contrasena</h1>
                <p className="ui-muted mb-6 mt-0 text-sm">
                    Ingresa tu correo y te enviaremos un enlace para restablecer tu contrasena.
                </p>

                <form onSubmit={handleForgot} className="grid gap-3">
                    <input
                        className="ui-input"
                        type="email"
                        placeholder="Tu correo electronico"
                        value={email}
                        required
                        disabled={forgotStatus === 'loading' || forgotStatus === 'success'}
                        onChange={(e) => setEmail(e.target.value)}
                    />

                    {forgotMessage && (
                        <p className={`text-sm ${forgotStatus === 'error' ? 'text-red-600' : 'ui-muted'}`}>
                            {forgotMessage}
                        </p>
                    )}

                    <button
                        className="ui-btn-primary"
                        type="submit"
                        disabled={forgotStatus === 'loading' || forgotStatus === 'success'}
                    >
                        {forgotStatus === 'loading' ? 'Enviando...' : 'Enviar enlace'}
                    </button>

                    <button
                        className="ui-btn-ghost text-sm"
                        type="button"
                        onClick={() => navigate('/login')}
                    >
                        Volver al inicio de sesion
                    </button>
                </form>
            </div>
        </div>
    )
}