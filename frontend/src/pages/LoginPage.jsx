import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';
import { ErrorMessage } from '../components/Feedback.jsx';

// Destinos por perfil após o login (a API também devolve redirectPath).
const HOME_BY_ROLE = {
  ADMIN: '/admin/usuarios',
  SECRETARIO: '/secretaria/ofertas',
  ALUNO: '/aluno/ofertas',
  PROFESSOR: '/professor/supervisor',
};

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const user = await login(username, password);
      navigate(HOME_BY_ROLE[user.role] || '/');
    } catch (err) {
      setError(err.message || 'Falha ao entrar.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="login-wrap">
      <div className="card login-card">
        <h1>PESCD</h1>
        <p className="muted">Entre com seu usuário e senha.</p>
        <form onSubmit={handleSubmit}>
          <ErrorMessage>{error}</ErrorMessage>
          <div className="form-group">
            <label htmlFor="username">Usuário</label>
            <input
              id="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoFocus
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="password">Senha</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <button className="btn" type="submit" disabled={submitting} style={{ width: '100%' }}>
            {submitting ? 'Entrando…' : 'Entrar'}
          </button>
        </form>
        <div className="login-hint">
          Exemplos de acesso (seed): <br />
          <b>secretario</b> / 123456 · <b>lferreira</b> / 123456 · <b>admin</b> / admin123
        </div>
      </div>
    </div>
  );
}
