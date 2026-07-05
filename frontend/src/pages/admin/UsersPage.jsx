import { useEffect, useState } from 'react';
import { api } from '../../api/client.js';
import { ErrorMessage, Loading, SuccessMessage } from '../../components/Feedback.jsx';

const ROLES = [
  { value: 'ADMIN', label: 'Administrador' },
  { value: 'SECRETARIO', label: 'Secretário' },
  { value: 'PROFESSOR', label: 'Professor' },
  { value: 'ALUNO', label: 'Aluno' },
];

const EMPTY = { id: null, fullName: '', email: '', username: '', password: '', role: 'ALUNO' };

// AD.01 - CRUD administrativo de usuários.
export default function UsersPage() {
  const [users, setUsers] = useState(null);
  const [form, setForm] = useState(EMPTY);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [busy, setBusy] = useState(false);

  function load() {
    api.get('/api/admin/users').then(setUsers).catch((e) => setError(e.message));
  }

  useEffect(load, []);

  function update(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  function edit(user) {
    setForm({ ...user, password: '' });
    setMessage(''); setError('');
  }

  async function submit(event) {
    event.preventDefault();
    setError(''); setMessage(''); setBusy(true);
    try {
      const payload = {
        fullName: form.fullName,
        email: form.email,
        username: form.username,
        password: form.password || null,
        role: form.role,
      };
      if (form.id) {
        await api.put(`/api/admin/users/${form.id}`, payload);
        setMessage('Usuário atualizado.');
      } else {
        await api.post('/api/admin/users', payload);
        setMessage('Usuário criado.');
      }
      setForm(EMPTY);
      load();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function remove(user) {
    if (!window.confirm(`Remover ${user.fullName}?`)) return;
    setError(''); setMessage('');
    try {
      await api.del(`/api/admin/users/${user.id}`);
      setMessage('Usuário removido.');
      if (form.id === user.id) setForm(EMPTY);
      load();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Usuários</h1>
          <p>AD.01 — administração de usuários do sistema.</p>
        </div>
      </div>

      <ErrorMessage>{error}</ErrorMessage>
      <SuccessMessage>{message}</SuccessMessage>

      <div className="form-row" style={{ alignItems: 'flex-start' }}>
        <div className="card" style={{ flex: 2 }}>
          {!users ? <Loading /> : (
            <table>
              <thead>
                <tr><th>Nome</th><th>Usuário</th><th>Perfil</th><th></th></tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td>{u.fullName}<br /><span className="muted" style={{ fontSize: 12 }}>{u.email}</span></td>
                    <td>{u.username}</td>
                    <td>{u.role}</td>
                    <td>
                      <button className="btn btn-ghost btn-sm" onClick={() => edit(u)}>Editar</button>{' '}
                      <button className="btn btn-danger btn-sm" onClick={() => remove(u)}>Remover</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        <div className="card" style={{ flex: 1 }}>
          <h2 style={{ marginTop: 0, fontSize: 18 }}>{form.id ? 'Editar usuário' : 'Novo usuário'}</h2>
          <form onSubmit={submit}>
            <div className="form-group">
              <label>Nome completo</label>
              <input value={form.fullName} onChange={(e) => update('fullName', e.target.value)} required />
            </div>
            <div className="form-group">
              <label>E-mail</label>
              <input type="email" value={form.email} onChange={(e) => update('email', e.target.value)} required />
            </div>
            <div className="form-group">
              <label>Usuário</label>
              <input value={form.username} onChange={(e) => update('username', e.target.value)} required />
            </div>
            <div className="form-group">
              <label>Senha {form.id && <span className="muted">(deixe em branco para manter)</span>}</label>
              <input
                type="password"
                value={form.password}
                onChange={(e) => update('password', e.target.value)}
                required={!form.id}
              />
            </div>
            <div className="form-group">
              <label>Perfil</label>
              <select value={form.role} onChange={(e) => update('role', e.target.value)}>
                {ROLES.map((r) => <option key={r.value} value={r.value}>{r.label}</option>)}
              </select>
            </div>
            <div className="form-actions">
              <button className="btn" type="submit" disabled={busy}>{form.id ? 'Salvar' : 'Criar'}</button>
              {form.id && (
                <button type="button" className="btn btn-ghost" onClick={() => setForm(EMPTY)}>Cancelar</button>
              )}
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
