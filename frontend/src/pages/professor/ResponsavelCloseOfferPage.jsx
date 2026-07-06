import { useEffect, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { api } from '../../api/client.js';
import { ErrorMessage, Loading, StatusBadge } from '../../components/Feedback.jsx';
import { SectionTitle } from '../../components/DataGrid.jsx';

// Encerramento de oferta pelo professor responsável, com resumo consolidado e
// registro das lições aprendidas (espelha professor/responsavel/close-offer.html).
export default function ResponsavelCloseOfferPage() {
  const { offerId } = useParams();
  const navigate = useNavigate();
  const base = `/api/professor/responsavel/offers/${offerId}`;

  const [summary, setSummary] = useState(null);
  const [lessonsLearned, setLessonsLearned] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    api.get(`${base}/close`).then(setSummary).catch((e) => setError(e.message));
  }, [offerId]);

  async function submit(event) {
    event.preventDefault();
    setError(''); setBusy(true);
    try {
      await api.post(`${base}/close`, { lessonsLearned });
      navigate('/professor/responsavel');
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  if (!summary && !error) return <Loading />;
  if (!summary) return <ErrorMessage>{error}</ErrorMessage>;

  const { offer } = summary;
  const grades = Object.entries(summary.gradeDistribution || {});

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Encerrar oferta</h1>
          <p>{offer.name}</p>
        </div>
        <Link className="btn btn-ghost" to="/professor/responsavel">Voltar</Link>
      </div>

      <ErrorMessage>{error}</ErrorMessage>

      <div className="card">
        <dl className="detail-grid">
          <dt>Oferta</dt><dd>{offer.name}</dd>
          <dt>Semestre</dt><dd>{offer.semester}</dd>
          <dt>Status</dt><dd><StatusBadge label={offer.statusLabel} status={offer.status} /></dd>
          <dt>Alunos inscritos</dt><dd>{offer.enrolledStudents}</dd>
          <dt>Frequência média</dt><dd>{summary.averageFrequency != null ? `${summary.averageFrequency}%` : '—'}</dd>
          <dt>Concluídos por documentação</dt><dd>{summary.documentationCompletions}</dd>
          <dt>Concluídos por relatório</dt><dd>{summary.reportCompletions}</dd>
        </dl>
        {grades.length > 0 && (
          <>
            <SectionTitle>Distribuição de notas</SectionTitle>
            <div className="form-actions">
              {grades.map(([grade, count]) => (
                <span key={grade} className="badge">{grade}: {count}</span>
              ))}
            </div>
          </>
        )}
      </div>

      <div className="card" style={{ maxWidth: 720 }}>
        <form onSubmit={submit}>
          <div className="form-group">
            <label>Lições aprendidas *</label>
            <textarea rows={6} value={lessonsLearned}
              onChange={(e) => setLessonsLearned(e.target.value)} required />
          </div>
          {!summary.canClose && (
            <p className="muted" style={{ fontSize: 13 }}>
              Atenção: a oferta ainda pode não estar pronta para encerramento — confira os status dos alunos.
            </p>
          )}
          <button className="btn" type="submit" disabled={busy}>Encerrar oferta</button>
        </form>
      </div>
    </div>
  );
}
