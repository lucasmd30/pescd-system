import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/client.js';
import { formatDate } from '../../utils/format.js';
import { ErrorMessage, Loading, StatusBadge } from '../../components/Feedback.jsx';

// AL.01 - Aluno visualiza suas ofertas e o status de cada uma.
export default function EnrollmentsPage() {
  const [items, setItems] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/api/aluno/ofertas').then(setItems).catch((e) => setError(e.message));
  }, []);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Minhas ofertas</h1>
          <p>Ofertas em que você está inscrito e o status de cada uma.</p>
        </div>
      </div>
      <ErrorMessage>{error}</ErrorMessage>
      {!items && !error && <Loading />}
      {items && (
        <div className="card">
          {items.length === 0 ? (
            <p className="muted">Você ainda não está inscrito em nenhuma oferta.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Oferta</th>
                  <th>Semestre</th>
                  <th>Período</th>
                  <th>Status</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {items.map((item) => (
                  <tr key={item.enrollment.enrollmentId}>
                    <td>{item.offer.name}</td>
                    <td>{item.offer.semester}</td>
                    <td>{formatDate(item.offer.startDate)} – {formatDate(item.offer.endDate)}</td>
                    <td><StatusBadge label={item.enrollment.statusLabel} status={item.enrollment.status} /></td>
                    <td>
                      <Link to={`/aluno/ofertas/${item.enrollment.enrollmentId}`}>Abrir</Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}
    </div>
  );
}
