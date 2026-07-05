import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api, openAuthenticatedFile } from '../../api/client.js';
import { formatDateTime, GRADE_OPTIONS } from '../../utils/format.js';
import { ErrorMessage, Loading, SuccessMessage, StatusBadge } from '../../components/Feedback.jsx';

// PS.02 (aprovar plano) e PS.03 (aprovar relatório) do professor supervisor.
export default function SupervisorEnrollmentPage() {
  const { offerId, studentId } = useParams();
  const base = `/api/professor/supervisor/offers/${offerId}/students/${studentId}`;

  const [detail, setDetail] = useState(null);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [planParecer, setPlanParecer] = useState('');
  const [reportForm, setReportForm] = useState({ parecer: '', frequencia: '', notaSugestao: '' });
  const [busy, setBusy] = useState(false);

  function load() {
    api.get(base).then(setDetail).catch((e) => setError(e.message));
  }
  useEffect(load, [offerId, studentId]);

  async function approvePlan(event) {
    event.preventDefault();
    setError(''); setMessage(''); setBusy(true);
    try {
      await api.post(`${base}/work-plan/approve`, { parecer: planParecer });
      setMessage('Plano de trabalho aprovado.');
      setPlanParecer('');
      load();
    } catch (err) { setError(err.message); } finally { setBusy(false); }
  }

  async function approveReport(event) {
    event.preventDefault();
    setError(''); setMessage(''); setBusy(true);
    try {
      await api.post(`${base}/report/approve`, {
        parecer: reportForm.parecer,
        frequencia: Number(reportForm.frequencia),
        notaSugestao: reportForm.notaSugestao,
      });
      setMessage('Relatório aprovado.');
      load();
    } catch (err) { setError(err.message); } finally { setBusy(false); }
  }

  if (!detail && !error) return <Loading />;
  if (!detail) return <ErrorMessage>{error}</ErrorMessage>;

  const { offer, enrollment } = detail;
  const status = enrollment.status;

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>{enrollment.student.fullName}</h1>
          <p>{offer.name} · <StatusBadge label={enrollment.statusLabel} /></p>
        </div>
        <Link className="btn btn-ghost" to="/professor/supervisor">Voltar</Link>
      </div>

      <ErrorMessage>{error}</ErrorMessage>
      <SuccessMessage>{message}</SuccessMessage>

      <div className="card">
        <div className="form-actions">
          {enrollment.hasWorkPlan && (
            <button className="btn btn-ghost btn-sm" onClick={() => openAuthenticatedFile(`${base}/work-plan/download`)}>
              Abrir plano (PDF)
            </button>
          )}
          {enrollment.hasReport && (
            <button className="btn btn-ghost btn-sm" onClick={() => openAuthenticatedFile(`${base}/report/download`)}>
              Abrir relatório (PDF)
            </button>
          )}
        </div>
      </div>

      {status === 'PLANO_ENVIADO' && (
        <div className="card" style={{ maxWidth: 620 }}>
          <h2 style={{ marginTop: 0, fontSize: 18 }}>Aprovar plano de trabalho (PS.02)</h2>
          <form onSubmit={approvePlan}>
            <div className="form-group">
              <label>Parecer</label>
              <textarea rows={4} value={planParecer} onChange={(e) => setPlanParecer(e.target.value)} required />
            </div>
            <button className="btn" type="submit" disabled={busy}>Aprovar plano</button>
          </form>
        </div>
      )}

      {status === 'RELATORIO_ENVIADO' && (
        <div className="card" style={{ maxWidth: 620 }}>
          <h2 style={{ marginTop: 0, fontSize: 18 }}>Aprovar relatório (PS.03)</h2>
          <form onSubmit={approveReport}>
            <div className="form-group">
              <label>Parecer</label>
              <textarea
                rows={4}
                value={reportForm.parecer}
                onChange={(e) => setReportForm((p) => ({ ...p, parecer: e.target.value }))}
                required
              />
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Frequência confirmada (%)</label>
                <input
                  type="number" min="0" max="100"
                  value={reportForm.frequencia}
                  onChange={(e) => setReportForm((p) => ({ ...p, frequencia: e.target.value }))}
                  required
                />
              </div>
              <div className="form-group">
                <label>Sugestão de nota</label>
                <select
                  value={reportForm.notaSugestao}
                  onChange={(e) => setReportForm((p) => ({ ...p, notaSugestao: e.target.value }))}
                  required
                >
                  <option value="">Selecione…</option>
                  {GRADE_OPTIONS.map((g) => <option key={g.value} value={g.value}>{g.label}</option>)}
                </select>
              </div>
            </div>
            <button className="btn" type="submit" disabled={busy}>Aprovar relatório</button>
          </form>
        </div>
      )}

      <StatusLogsCard logs={detail.statusLogs} />
    </div>
  );
}

function StatusLogsCard({ logs }) {
  return (
    <div className="card">
      <h2 style={{ marginTop: 0, fontSize: 18 }}>Histórico de status</h2>
      {logs.length === 0 ? (
        <p className="muted">Sem alterações registradas.</p>
      ) : (
        <table>
          <thead><tr><th>Quando</th><th>De</th><th>Para</th><th>Descrição</th></tr></thead>
          <tbody>
            {logs.map((log) => (
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
  );
}
