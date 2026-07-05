import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext.jsx';

// Menu por perfil: cada role vê apenas os seus links.
const NAV_BY_ROLE = {
  ADMIN: [{ to: '/admin/usuarios', label: 'Usuários' }],
  SECRETARIO: [{ to: '/secretaria/ofertas', label: 'Ofertas' }],
  ALUNO: [{ to: '/aluno/ofertas', label: 'Minhas ofertas' }],
  PROFESSOR: [
    { to: '/professor/supervisor', label: 'Supervisor' },
    { to: '/professor/responsavel', label: 'Responsável' },
  ],
};

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  async function handleLogout() {
    await logout();
    navigate('/login');
  }

  const navLinks = (user && NAV_BY_ROLE[user.role]) || [];

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="topbar-inner">
          <Link to="/" className="brand">PESCD</Link>
          <nav className="nav">
            <NavLink to="/ofertas">Ofertas públicas</NavLink>
            {navLinks.map((link) => (
              <NavLink key={link.to} to={link.to}>{link.label}</NavLink>
            ))}
          </nav>
          <div className="topbar-right">
            {user ? (
              <>
                <span className="user-chip">{user.fullName} · {user.role}</span>
                <button className="btn btn-ghost" onClick={handleLogout}>Sair</button>
              </>
            ) : (
              <Link to="/login" className="btn">Entrar</Link>
            )}
          </div>
        </div>
      </header>
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
