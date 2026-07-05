import { Link } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

const ROLE_LINKS = {
  ADMIN: { to: '/admin/usuarios', label: 'Gerenciar usuários' },
  SECRETARIO: { to: '/secretaria/ofertas', label: 'Gerenciar ofertas' },
  ALUNO: { to: '/aluno/ofertas', label: 'Ver minhas ofertas' },
  PROFESSOR: { to: '/professor/supervisor', label: 'Área do professor' },
};

export default function HomePage() {
  const { user } = useAuth();
  const shortcut = user && ROLE_LINKS[user.role];

  return (
    <div className="card">
      <h1>Programa de Estágio Supervisionado de Capacitação Docente</h1>
      <p className="muted">
        Front-end React consumindo a REST API do PESCD.
      </p>
      <div className="form-actions">
        <Link className="btn btn-ghost" to="/ofertas">Ofertas públicas</Link>
        {shortcut && <Link className="btn" to={shortcut.to}>{shortcut.label}</Link>}
        {!user && <Link className="btn" to="/login">Entrar</Link>}
      </div>
    </div>
  );
}
