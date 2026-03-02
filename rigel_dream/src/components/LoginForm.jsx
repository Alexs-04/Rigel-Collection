import React, { useState, useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { User, Lock } from 'lucide-react';

export default function LoginForm() {
  const { login } = useContext(AuthContext);
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
      setError(err?.response?.data?.message || 'Error al iniciar sesión');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="center-screen bg-gradient">
      <div style={{ width: '100%', maxWidth: 420 }}>
        <div className="card">
          <div className="brand" style={{ marginBottom: 12 }}>
            <div className="logo">R</div>
            <div className="title">Rigel</div>
          </div>

          <h2 style={{ fontSize: 20, marginBottom: 6 }}>Bienvenido de vuelta</h2>
          <p style={{ color: 'var(--muted)', marginBottom: 16 }}>Ingresa con tu cuenta para continuar</p>

          <form onSubmit={handleSubmit} style={{ display: 'grid', gap: 12 }}>
            <div>
              <label>Email</label>
              <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <div style={{ display: 'inline-flex', alignItems: 'center', padding: 8, borderRadius: 8, background: '#f3f4f6' }}>
                  <User style={{ width: 16, height: 16, color: 'var(--muted)' }} />
                </div>
                <input
                  className="input"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
            </div>

            <div>
              <label>Contraseña</label>
              <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                <div style={{ display: 'inline-flex', alignItems: 'center', padding: 8, borderRadius: 8, background: '#f3f4f6' }}>
                  <Lock style={{ width: 16, height: 16, color: 'var(--muted)' }} />
                </div>
                <input
                  className="input"
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>
            </div>

            {error && <div style={{ color: '#ef4444', fontSize: 13 }}>{error}</div>}

            <div style={{ display: 'flex', gap: 12, alignItems: 'center' }}>
              <button type="submit" disabled={loading} className="btn-primary" style={{ flex: 1 }}>
                {loading ? 'Ingresando...' : 'Ingresar'}
              </button>
              <button type="button" className="btn-ghost" onClick={() => { setEmail(''); setPassword('') }}>
                Limpiar
              </button>
            </div>

            <div style={{ display: 'flex', justifyContent: 'space-between', color: 'var(--muted)', fontSize: 13 }}>
              <div></div>
              <a style={{ color: 'var(--accent-600)', textDecoration: 'none' }} href="#">¿Olvidaste tu contraseña?</a>
            </div>
          </form>
        </div>

        <div style={{ marginTop: 12, textAlign: 'center', color: 'var(--muted)', fontSize: 13 }}>
          © {new Date().getFullYear()} Rigel
        </div>
      </div>
    </div>
  );
}
