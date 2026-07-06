import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../../api/client.js';
import { formatDateTime } from '../../utils/format.js';
import { ErrorMessage, Loading, StatusBadge } from '../../components/Feedback.jsx';

// Detalhes de um aluno da oferta vistos pelo secretário (espelha
// secretary/offers/student-details.html): dados do aluno + histórico de status.
export default function StudentDetailsPage() {
  const { offerId, offerStudentId } = useParams();
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get(`/api/secretary/offers/${offerId}/students/${offerStudentId}`)
      .then(setData)
      .catch((e) => setError(e.message));
  }, [offerId, offerStudentId]);

  if (!data && !error) return <Loading />;
  if (!data) return <ErrorMessage>{error}</ErrorMessage>;

  const { enrollment, statusLogs } = data;

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Detalhes do aluno</h1>
          <p>{data.offer.name}</p>
        </div>
        <Link className="btn btn-ghost" to={`/secretaria/ofertas/${offerId}`}>Voltar</Link>
      </div>

      <div className="card">
        <h2 style={{ marginTop: 0, fontSize: 18 }}>Informações do aluno</h2>
        <dl className="detail-grid">
          <dt>Nome</dt><dd>{enrollment.student.fullName}</dd>
          <dt>E-mail</dt><dd>{enrollment.student.email}</dd>
          <dt>Status atual</dt><dd><StatusBadge label={enrollment.statusLabel} status={enrollment.status} /></dd>
          <dt>Supervisor</dt><dd>{enrollment.supervisor?.fullName || '—'}</dd>
        </dl>
      </div>

      <div className="card">
        <h2 style={{ marginTop: 0, fontSize: 18 }}>Histórico de status</h2>
        {statusLogs.length === 0 ? (
          <p className="muted">Nenhuma alteração de status registrada.</p>
        ) : (
          <table>
            <thead>
              <tr><th>Status anterior</th><th>Novo status</th><th>Descrição</th><th>Data</th></tr>
            </thead>
            <tbody>
              {statusLogs.map((log) => (
                <tr key={log.id}>
                  <td>{log.previousStatusLabel || '—'}</td>
                  <td>{log.newStatusLabel}</td>
                  <td>{log.description}</td>
                  <td>{formatDateTime(log.changedAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
