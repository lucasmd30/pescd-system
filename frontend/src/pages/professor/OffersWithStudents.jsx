import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/client.js';
import { formatDate } from '../../utils/format.js';
import { ErrorMessage, Loading, StatusBadge } from '../../components/Feedback.jsx';

// Lista de ofertas com seus alunos, reutilizada pelos dashboards do professor
// (supervisor e responsável). `studentLink(offerId, studentId)` define o destino.
export default function OffersWithStudents({ title, subtitle, endpoint, studentLink }) {
  const [offers, setOffers] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get(endpoint).then(setOffers).catch((e) => setError(e.message));
  }, [endpoint]);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>{title}</h1>
          <p>{subtitle}</p>
        </div>
      </div>
      <ErrorMessage>{error}</ErrorMessage>
      {!offers && !error && <Loading />}
      {offers && offers.length === 0 && (
        <div className="card"><p className="muted">Nenhuma oferta associada a você.</p></div>
      )}
      {offers && offers.map((item) => (
        <div className="card" key={item.offer.id}>
          <div className="row-between">
            <h2 style={{ margin: 0, fontSize: 18 }}>{item.offer.name}</h2>
            <span className="muted">
              {item.offer.semester} · {formatDate(item.offer.startDate)} – {formatDate(item.offer.endDate)}
            </span>
          </div>
          {item.students.length === 0 ? (
            <p className="muted" style={{ marginBottom: 0 }}>Sem alunos inscritos.</p>
          ) : (
            <table>
              <thead>
                <tr><th>Aluno</th><th>Status</th><th></th></tr>
              </thead>
              <tbody>
                {item.students.map((s) => (
                  <tr key={s.enrollmentId}>
                    <td>{s.student.fullName}</td>
                    <td><StatusBadge label={s.statusLabel} /></td>
                    <td><Link to={studentLink(item.offer.id, s.student.id)}>Abrir</Link></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      ))}
    </div>
  );
}
