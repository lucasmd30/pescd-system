import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api, openAuthenticatedFile } from '../../api/client.js';
import { formatDate, formatDateTime } from '../../utils/format.js';
import { ErrorMessage, Loading, StatusBadge } from '../../components/Feedback.jsx';

// AL.01 - detalhes da inscrição, com envios já feitos e histórico de status.
export default function EnrollmentDetailsPage() {
  const { id } = useParams();
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  const base = `/api/aluno/ofertas/${id}`;

  useEffect(() => {
    api.get(base).then(setData).catch((e) => setError(e.message));
  }, [id]);

  if (!data && !error) return <Loading />;
  if (!data) return <ErrorMessage>{error}</ErrorMessage>;

  const { offer, enrollment, workPlan, documentation, report, statusLogs } = data;
  const status = enrollment.status;

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>{offer.name}</h1>
          <p>{offer.semester} · {formatDate(offer.startDate)} – {formatDate(offer.endDate)}</p>
        </div>
        <Link className="btn btn-ghost" to="/aluno/ofertas">Voltar</Link>
      </div>

      <ErrorMessage>{error}</ErrorMessage>

      <div className="card">
        <div className="row-between">
          <div><StatusBadge label={enrollment.statusLabel} status={status} /></div>
          <div className="form-actions">
            {status === 'NAO_ENVIADO' && (
              <>
                <Link className="btn" to={`/aluno/ofertas/${id}/plano`}>Enviar plano (AL.02)</Link>
                <Link className="btn btn-ghost" to={`/aluno/ofertas/${id}/documentacao`}>
                  Enviar documentação (AL.03)
                </Link>
              </>
            )}
            {status === 'PLANO_APROVADO' && (
              <Link className="btn" to={`/aluno/ofertas/${id}/relatorio`}>Enviar relatório (AL.04)</Link>
            )}
          </div>
        </div>
      </div>

      <FileCard
        title="Plano de trabalho"
        empty={!workPlan}
        onDownload={() => openAuthenticatedFile(`${base}/plano/arquivo`)}
      >
        {workPlan && (
          <dl className="detail-grid">
            <dt>Disciplina</dt><dd>{workPlan.disciplineCode} — {workPlan.disciplineName}</dd>
            <dt>Curso</dt><dd>{workPlan.disciplineCourse}</dd>
            <dt>Supervisor</dt><dd>{enrollment.supervisor?.fullName || '—'}</dd>
            <dt>Enviado em</dt><dd>{formatDateTime(workPlan.submittedAt)}</dd>
            <dt>Arquivo</dt><dd>{workPlan.fileName}</dd>
            {workPlan.supervisorApprovedAt && (
              <>
                <dt>Parecer supervisor</dt><dd>{workPlan.supervisorParecer}</dd>
                <dt>Aprovado em</dt><dd>{formatDateTime(workPlan.supervisorApprovedAt)}</dd>
              </>
            )}
          </dl>
        )}
      </FileCard>

      <FileCard
        title="Documentação comprobatória"
        empty={!documentation}
        onDownload={() => openAuthenticatedFile(`${base}/documentacao/arquivo`)}
      >
        {documentation && (
          <dl className="detail-grid">
            <dt>Instituição</dt><dd>{documentation.institutionName}</dd>
            <dt>Disciplina</dt><dd>{documentation.disciplineName}</dd>
            <dt>Carga horária</dt><dd>{documentation.workloadHours} h</dd>
            <dt>Enviado em</dt><dd>{formatDateTime(documentation.submittedAt)}</dd>
            <dt>Arquivo</dt><dd>{documentation.fileName}</dd>
          </dl>
        )}
      </FileCard>

      <FileCard
        title="Relatório final"
        empty={!report}
        onDownload={() => openAuthenticatedFile(`${base}/relatorio/arquivo`)}
      >
        {report && (
          <dl className="detail-grid">
            <dt>Frequência</dt><dd>{report.frequency}%</dd>
            <dt>Enviado em</dt><dd>{formatDateTime(report.submittedAt)}</dd>
            <dt>Arquivo</dt><dd>{report.fileName}</dd>
          </dl>
        )}
      </FileCard>

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

function FileCard({ title, empty, onDownload, children }) {
  return (
    <div className="card">
      <div className="row-between">
        <h2 style={{ margin: 0, fontSize: 18 }}>{title}</h2>
        {!empty && <button className="btn btn-ghost btn-sm" onClick={onDownload}>Abrir PDF</button>}
      </div>
      {empty ? <p className="muted" style={{ marginBottom: 0 }}>Ainda não enviado.</p> : children}
    </div>
  );
}
