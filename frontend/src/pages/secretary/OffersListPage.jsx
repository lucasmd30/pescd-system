import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/client.js';
import { formatDate } from '../../utils/format.js';
import { ErrorMessage, Loading, StatusBadge } from '../../components/Feedback.jsx';

export default function OffersListPage() {
  const [offers, setOffers] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/api/secretary/offers').then(setOffers).catch((e) => setError(e.message));
  }, []);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Ofertas</h1>
          <p>Gestão das ofertas do programa.</p>
        </div>
        <Link className="btn" to="/secretaria/ofertas/nova">Nova oferta</Link>
      </div>
      <ErrorMessage>{error}</ErrorMessage>
      {!offers && !error && <Loading />}
      {offers && (
        <div className="card">
          {offers.length === 0 ? (
            <p className="muted">Nenhuma oferta cadastrada.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Oferta</th>
                  <th>Semestre</th>
                  <th>Período</th>
                  <th>Status</th>
                  <th>Inscritos</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {offers.map((offer) => (
                  <tr key={offer.id}>
                    <td>{offer.name}</td>
                    <td>{offer.semester}</td>
                    <td>{formatDate(offer.startDate)} – {formatDate(offer.endDate)}</td>
                    <td><StatusBadge label={offer.statusLabel} /></td>
                    <td>{offer.enrolledStudents}</td>
                    <td><Link to={`/secretaria/ofertas/${offer.id}`}>Detalhes</Link></td>
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
