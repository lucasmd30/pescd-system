import { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { api, openAuthenticatedFile } from '../../api/client.js';
import { formatDateTime } from '../../utils/format.js';
import { ErrorMessage, Loading } from '../../components/Feedback.jsx';

// AL.04 - Envio do relatório final (frequência 0-100, PDF <= 5MB), com leitura
// do plano de trabalho e do histórico de status.
export default function ReportPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const fileRef = useRef(null);
  const [data, setData] = useState(null);
  const [frequency, setFrequency] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const base = `/api/aluno/ofertas/${id}`;

  useEffect(() => {
    api.get(base).then(setData).catch((e) => setError(e.message));
  }, [id]);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const file = fileRef.current.files[0];
      if (!file) throw new Error('Selecione o arquivo PDF do relatório.');
      const form = new FormData();
      form.append('frequency', frequency);
      form.append('file', file);
      await api.postForm(`${base}/relatorio`, form);
      navigate(`/aluno/ofertas/${id}`);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  if (!data && !error) return <Loading />;
  if (!data) return <ErrorMessage>{error}</ErrorMessage>;

  const { workPlan, statusLogs } = data;

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Enviar relatório final</h1>
          <p>AL.04 — frequência (0–100) e PDF de até 5 MB.</p>
        </div>
        <Link className="btn btn-ghost" to={`/aluno/ofertas/${id}`}>Voltar</Link>
      </div>

      {workPlan && (
        <div className="card">
          <div className="row-between">
            <h2 style={{ margin: 0, fontSize: 18 }}>Plano de trabalho aprovado</h2>
            <button className="btn btn-ghost btn-sm" onClick={() => openAuthenticatedFile(`${base}/plano/arquivo`)}>
              Abrir PDF
            </button>
          </div>
          <p className="muted" style={{ marginBottom: 0 }}>
            {workPlan.disciplineCode} — {workPlan.disciplineName} · {workPlan.disciplineCourse}
          </p>
        </div>
      )}

      <div className="card" style={{ maxWidth: 620 }}>
        <form onSubmit={handleSubmit}>
          <ErrorMessage>{error}</ErrorMessage>
          <div className="form-group">
            <label>Frequência (%) *</label>
            <input
              type="number"
              min="0"
              max="100"
              value={frequency}
              onChange={(e) => setFrequency(e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label>Arquivo (PDF) *</label>
            <input type="file" accept="application/pdf,.pdf" ref={fileRef} required />
          </div>
          <div className="form-actions">
            <button className="btn" type="submit" disabled={submitting}>
              {submitting ? 'Enviando…' : 'Enviar relatório'}
            </button>
          </div>
        </form>
      </div>

      <div className="card">
        <h2 style={{ marginTop: 0, fontSize: 18 }}>Histórico de status</h2>
        {statusLogs.length === 0 ? (
          <p className="muted">Sem alterações registradas.</p>
        ) : (
          <table>
            <thead>
              <tr><th>Quando</th><th>De</th><th>Para</th><th>Descrição</th></tr>
            </thead>
            <tbody>
              {statusLogs.map((log) => (
                <tr key={log.id}>
                  <td>{formatDateTime(log.changedAt)}</td>
                  <td>{log.previousStatusLabel || '—'}</td>
                  <td>{log.newStatusLabel}</td>
                  <td>{log.description}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
