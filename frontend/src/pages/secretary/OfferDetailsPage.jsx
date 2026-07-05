import { useEffect, useRef, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../../api/client.js';
import { formatDate } from '../../utils/format.js';
import { ErrorMessage, Loading, SuccessMessage, StatusBadge } from '../../components/Feedback.jsx';

export default function OfferDetailsPage() {
  const { offerId } = useParams();
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [studentForm, setStudentForm] = useState({ fullName: '', email: '', ra: '' });
  const [busy, setBusy] = useState(false);
  const fileRef = useRef(null);

  const base = `/api/secretary/offers/${offerId}`;

  function load() {
    api.get(base).then(setData).catch((e) => setError(e.message));
  }

  useEffect(load, [offerId]);

  async function addStudent(event) {
    event.preventDefault();
    setError(''); setMessage(''); setBusy(true);
    try {
      const updated = await api.post(`${base}/students`, studentForm);
      setData(updated);
      setStudentForm({ fullName: '', email: '', ra: '' });
      setMessage('Aluno adicionado à oferta.');
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function importCsv(event) {
    event.preventDefault();
    setError(''); setMessage(''); setBusy(true);
    try {
      const file = fileRef.current.files[0];
      if (!file) throw new Error('Selecione um arquivo CSV.');
      const formData = new FormData();
      formData.append('file', file);
      const result = await api.postForm(`${base}/students/import`, formData);
      setData(result.offer);
      fileRef.current.value = '';
      setMessage(`${result.enrolled} aluno(s) importado(s) do CSV.`);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  async function closeOffer() {
    setError(''); setMessage(''); setBusy(true);
    try {
      await api.post(`${base}/close`, {});
      setMessage('Oferta encerrada com sucesso.');
      load();
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  if (!data && !error) return <Loading />;
  if (!data) return <ErrorMessage>{error}</ErrorMessage>;

  const { offer, students } = data;

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>{offer.name}</h1>
          <p>{offer.semester} · {formatDate(offer.startDate)} – {formatDate(offer.endDate)}</p>
        </div>
        <Link className="btn btn-ghost" to="/secretaria/ofertas">Voltar</Link>
      </div>

      <ErrorMessage>{error}</ErrorMessage>
      <SuccessMessage>{message}</SuccessMessage>

      <div className="card">
        <div className="row-between">
          <div>
            <StatusBadge label={offer.statusLabel} />{' '}
            <span className="muted">Responsável: {offer.responsibleProfessor?.fullName || '—'}</span>
          </div>
          <button className="btn btn-danger btn-sm" onClick={closeOffer} disabled={busy}>
            Encerrar oferta
          </button>
        </div>
      </div>

      <div className="card">
        <h2 style={{ marginTop: 0, fontSize: 18 }}>Alunos inscritos ({students.length})</h2>
        {students.length === 0 ? (
          <p className="muted">Nenhum aluno inscrito ainda.</p>
        ) : (
          <table>
            <thead>
              <tr><th>Aluno</th><th>E-mail</th><th>Status</th><th></th></tr>
            </thead>
            <tbody>
              {students.map((s) => (
                <tr key={s.enrollmentId}>
                  <td>{s.student.fullName}</td>
                  <td>{s.student.email}</td>
                  <td><StatusBadge label={s.statusLabel} /></td>
                  <td>
                    <Link to={`/secretaria/ofertas/${offerId}?aluno=${s.enrollmentId}`} className="muted">
                      #{s.enrollmentId}
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className="form-row">
        <div className="card" style={{ flex: 1 }}>
          <h2 style={{ marginTop: 0, fontSize: 18 }}>Adicionar aluno (S.02)</h2>
          <form onSubmit={addStudent}>
            <div className="form-group">
              <label>Nome completo</label>
              <input
                value={studentForm.fullName}
                onChange={(e) => setStudentForm((p) => ({ ...p, fullName: e.target.value }))}
                required
              />
            </div>
            <div className="form-group">
              <label>E-mail</label>
              <input
                type="email"
                value={studentForm.email}
                onChange={(e) => setStudentForm((p) => ({ ...p, email: e.target.value }))}
                required
              />
            </div>
            <div className="form-group">
              <label>RA</label>
              <input
                value={studentForm.ra}
                onChange={(e) => setStudentForm((p) => ({ ...p, ra: e.target.value }))}
                required
              />
            </div>
            <button className="btn" type="submit" disabled={busy}>Adicionar</button>
          </form>
        </div>

        <div className="card" style={{ flex: 1 }}>
          <h2 style={{ marginTop: 0, fontSize: 18 }}>Importar via CSV (S.02)</h2>
          <p className="muted" style={{ fontSize: 13 }}>
            Formato: <code>RA,NOME_COMPLETO,EMAIL</code> (uma linha de cabeçalho + dados).
          </p>
          <form onSubmit={importCsv}>
            <div className="form-group">
              <input type="file" accept=".csv" ref={fileRef} required />
            </div>
            <button className="btn" type="submit" disabled={busy}>Importar</button>
          </form>
        </div>
      </div>
    </div>
  );
}
