import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api, openAuthenticatedFile } from '../../api/client.js';
import { formatDateTime, GRADE_OPTIONS } from '../../utils/format.js';
import { ErrorMessage, Loading, SuccessMessage, StatusBadge } from '../../components/Feedback.jsx';

// PR.02 (analisar documentação) e PR.01 (concluir relatório) do responsável.
export default function ResponsavelEnrollmentPage() {
  const { offerId, studentId } = useParams();
  const base = `/api/professor/responsavel/offers/${offerId}/students/${studentId}`;

  const [detail, setDetail] = useState(null);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [docForm, setDocForm] = useState({ parecer: '', frequencia: '', nota: '' });
  const [reportForm, setReportForm] = useState({ parecer: '', frequencia: '', nota: '' });
  const [busy, setBusy] = useState(false);

  function load() {
    api.get(base).then(setDetail).catch((e) => setError(e.message));
  }
  useEffect(load, [offerId, studentId]);

  async function analyzeDoc(event) {
    event.preventDefault();
    setError(''); setMessage(''); setBusy(true);
    try {
      await api.post(`${base}/documentation/analyze`, {
        parecer: docForm.parecer,
        frequencia: Number(docForm.frequencia),
        nota: docForm.nota,
      });
      setMessage('Documentação analisada e aprovada.');
      load();
    } catch (err) { setError(err.message); } finally { setBusy(false); }
  }

  async function concludeReport(event) {
    event.preventDefault();
    setError(''); setMessage(''); setBusy(true);
    try {
      await api.post(`${base}/report/conclude`, {
        parecer: reportForm.parecer,
        frequencia: Number(reportForm.frequencia),
        nota: reportForm.nota,
      });
      setMessage('Relatório concluído.');
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
        <Link className="btn btn-ghost" to="/professor/responsavel">Voltar</Link>
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
          {enrollment.hasDocumentation && (
            <button className="btn btn-ghost btn-sm" onClick={() => openAuthenticatedFile(`${base}/documentation/download`)}>
              Abrir documentação (PDF)
            </button>
          )}
          {enrollment.hasReport && (
            <button className="btn btn-ghost btn-sm" onClick={() => openAuthenticatedFile(`${base}/report/download`)}>
              Abrir relatório (PDF)
            </button>
          )}
        </div>
      </div>

      {status === 'DOCUMENTACAO_ENVIADA' && (
        <div className="card" style={{ maxWidth: 620 }}>
          <h2 style={{ marginTop: 0, fontSize: 18 }}>Analisar documentação (PR.02)</h2>
          <ParecerNotaForm
            state={docForm}
            setState={setDocForm}
            onSubmit={analyzeDoc}
            busy={busy}
            submitLabel="Aprovar documentação"
          />
        </div>
      )}

      {status === 'RELATORIO_APROVADO_SUPERVISOR' && (
        <div className="card" style={{ maxWidth: 620 }}>
          <h2 style={{ marginTop: 0, fontSize: 18 }}>Concluir relatório (PR.01)</h2>
          <ParecerNotaForm
            state={reportForm}
            setState={setReportForm}
            onSubmit={concludeReport}
            busy={busy}
            submitLabel="Concluir relatório"
          />
        </div>
      )}

      <div className="card">
        <h2 style={{ marginTop: 0, fontSize: 18 }}>Histórico de status</h2>
        {detail.statusLogs.length === 0 ? (
          <p className="muted">Sem alterações registradas.</p>
        ) : (
          <table>
            <thead><tr><th>Quando</th><th>De</th><th>Para</th><th>Descrição</th></tr></thead>
            <tbody>
              {detail.statusLogs.map((log) => (
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

function ParecerNotaForm({ state, setState, onSubmit, busy, submitLabel }) {
  return (
    <form onSubmit={onSubmit}>
      <div className="form-group">
        <label>Parecer</label>
        <textarea
          rows={4}
          value={state.parecer}
          onChange={(e) => setState((p) => ({ ...p, parecer: e.target.value }))}
          required
        />
      </div>
      <div className="form-row">
        <div className="form-group">
          <label>Frequência (%)</label>
          <input
            type="number" min="0" max="100"
            value={state.frequencia}
            onChange={(e) => setState((p) => ({ ...p, frequencia: e.target.value }))}
            required
          />
        </div>
        <div className="form-group">
          <label>Nota</label>
          <select
            value={state.nota}
            onChange={(e) => setState((p) => ({ ...p, nota: e.target.value }))}
            required
          >
            <option value="">Selecione…</option>
            {GRADE_OPTIONS.map((g) => <option key={g.value} value={g.value}>{g.label}</option>)}
          </select>
        </div>
      </div>
      <button className="btn" type="submit" disabled={busy}>{submitLabel}</button>
    </form>
  );
}
