import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api, openAuthenticatedFile } from '../../api/client.js';
import { formatDateTime, GRADE_OPTIONS } from '../../utils/format.js';
import { ErrorMessage, Loading, SuccessMessage, StatusBadge } from '../../components/Feedback.jsx';
import { InfoGrid, StatusTimeline, SectionTitle } from '../../components/DataGrid.jsx';

// PR.02 (analisar documentação) e PR.01 (concluir relatório) do responsável.
// Espelha analyze-documentation.html e conclude-report.html.
export default function ResponsavelEnrollmentPage() {
  const { offerId, studentId } = useParams();
  const base = `/api/professor/responsavel/offers/${offerId}/students/${studentId}`;

  const [detail, setDetail] = useState(null);
  const [documentation, setDocumentation] = useState(null);
  const [report, setReport] = useState(null);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [docForm, setDocForm] = useState({ parecer: '', frequencia: '', nota: '' });
  const [reportForm, setReportForm] = useState({ parecer: '', frequencia: '', nota: '' });
  const [busy, setBusy] = useState(false);

  async function load() {
    try {
      const d = await api.get(base);
      setDetail(d);
      if (d.enrollment.hasDocumentation) {
        setDocumentation(await api.get(`${base}/documentation`).catch(() => null));
      }
      if (d.enrollment.hasReport) {
        setReport(await api.get(`${base}/report`).catch(() => null));
      }
    } catch (e) {
      setError(e.message);
    }
  }
  useEffect(() => { load(); }, [offerId, studentId]);

  async function analyzeDoc(event) {
    event.preventDefault();
    setError(''); setMessage(''); setBusy(true);
    try {
      await api.post(`${base}/documentation/analyze`, {
        parecer: docForm.parecer, frequencia: Number(docForm.frequencia), nota: docForm.nota,
      });
      setMessage('Documentação analisada e aprovada.');
      await load();
    } catch (err) { setError(err.message); } finally { setBusy(false); }
  }

  async function concludeReport(event) {
    event.preventDefault();
    setError(''); setMessage(''); setBusy(true);
    try {
      await api.post(`${base}/report/conclude`, {
        parecer: reportForm.parecer, frequencia: Number(reportForm.frequencia), nota: reportForm.nota,
      });
      setMessage('Relatório concluído.');
      await load();
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
          <p>{offer.name} · <StatusBadge label={enrollment.statusLabel} status={status} /></p>
        </div>
        <Link className="btn btn-ghost" to="/professor/responsavel">Voltar</Link>
      </div>

      <ErrorMessage>{error}</ErrorMessage>
      <SuccessMessage>{message}</SuccessMessage>

      {/* Documentação comprobatória */}
      {documentation && (
        <div className="card">
          <SectionTitle>Documentação comprobatória</SectionTitle>
          <InfoGrid items={[
            { label: 'Instituição', value: documentation.institutionName },
            { label: 'Carga horária', value: `${documentation.workloadHours}h` },
            { label: 'Disciplina', value: documentation.disciplineName },
            { label: 'Curso', value: documentation.disciplineCourse },
            { label: 'Enviado em', value: formatDateTime(documentation.submittedAt), span2: true },
          ]} />
          <button className="btn btn-ghost btn-sm" style={{ marginTop: 12 }}
            onClick={() => openAuthenticatedFile(`${base}/documentation/download`)}>
            📄 Visualizar PDF da Documentação
          </button>
        </div>
      )}

      {/* Relatório + avaliação do supervisor */}
      {report && (
        <div className="card">
          <SectionTitle>Relatório e avaliação do supervisor</SectionTitle>
          <InfoGrid items={[
            { label: 'Frequência indicada pelo aluno', value: `${report.frequency}%` },
            { label: 'Enviado em', value: formatDateTime(report.submittedAt) },
          ]} />
          <div className="form-actions" style={{ marginTop: 12 }}>
            {enrollment.hasWorkPlan && (
              <button className="btn btn-ghost btn-sm" onClick={() => openAuthenticatedFile(`${base}/work-plan/download`)}>
                📄 PDF do Plano
              </button>
            )}
            <button className="btn btn-ghost btn-sm" onClick={() => openAuthenticatedFile(`${base}/report/download`)}>
              📄 PDF do Relatório
            </button>
          </div>
          {report.supervisorApprovedAt && (
            <div className="supervisor-box">
              <div className="supervisor-label">Avaliação do professor supervisor</div>
              <div className="supervisor-row">
                <span><strong>Frequência:</strong> {report.supervisorFrequencia}%</span>
                <span><strong>Sugestão de nota:</strong> {report.supervisorNotaSugestaoLabel || '—'}</span>
                <span><strong>Aprovado em:</strong> {formatDateTime(report.supervisorApprovedAt)}</span>
              </div>
              <div className="supervisor-parecer">{report.supervisorParecer}</div>
            </div>
          )}
        </div>
      )}

      {/* PR.02 — analisar documentação */}
      {status === 'DOCUMENTACAO_ENVIADA' && (
        <div className="card" style={{ maxWidth: 720 }}>
          <SectionTitle>Parecer do responsável (PR.02 — analisar documentação)</SectionTitle>
          <ParecerNotaForm state={docForm} setState={setDocForm} onSubmit={analyzeDoc} busy={busy} submitLabel="✓ Concluir análise" />
        </div>
      )}

      {/* PR.01 — concluir relatório */}
      {status === 'RELATORIO_APROVADO_SUPERVISOR' && (
        <div className="card" style={{ maxWidth: 720 }}>
          <SectionTitle>Parecer do responsável (PR.01 — concluir relatório)</SectionTitle>
          <ParecerNotaForm state={reportForm} setState={setReportForm} onSubmit={concludeReport} busy={busy} submitLabel="✓ Concluir relatório" />
        </div>
      )}

      <div className="card">
        <SectionTitle>Histórico de status</SectionTitle>
        <StatusTimeline logs={detail.statusLogs} />
      </div>
    </div>
  );
}

function ParecerNotaForm({ state, setState, onSubmit, busy, submitLabel }) {
  return (
    <form onSubmit={onSubmit}>
      <div className="form-group">
        <label>Parecer *</label>
        <textarea rows={4} value={state.parecer}
          onChange={(e) => setState((p) => ({ ...p, parecer: e.target.value }))} required />
      </div>
      <div className="form-row">
        <div className="form-group">
          <label>Frequência (%) *</label>
          <input type="number" min="0" max="100" value={state.frequencia}
            onChange={(e) => setState((p) => ({ ...p, frequencia: e.target.value }))} required />
        </div>
        <div className="form-group">
          <label>Nota final *</label>
          <select value={state.nota} onChange={(e) => setState((p) => ({ ...p, nota: e.target.value }))} required>
            <option value="">— Selecione —</option>
            {GRADE_OPTIONS.map((g) => <option key={g.value} value={g.value}>{g.label}</option>)}
          </select>
        </div>
      </div>
      <button className="btn" type="submit" disabled={busy}>{submitLabel}</button>
    </form>
  );
}
