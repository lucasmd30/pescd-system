import { useEffect, useState } from 'react';
import { api } from '../api/client.js';
import { formatDate } from '../utils/format.js';
import { ErrorMessage, Loading } from '../components/Feedback.jsx';

// V.01 - Visualização pública de ofertas (GET /api/offers, sem autenticação).
export default function PublicOffersPage() {
  const [offers, setOffers] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/api/offers').then(setOffers).catch((e) => setError(e.message));
  }, []);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Ofertas públicas</h1>
          <p>Ofertas do programa disponíveis para consulta.</p>
        </div>
      </div>
      <ErrorMessage>{error}</ErrorMessage>
      {!offers && !error && <Loading />}
      {offers && (
        <div className="card">
          {offers.length === 0 ? (
            <p className="muted">Nenhuma oferta disponível.</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Oferta</th>
                  <th>Semestre</th>
                  <th>Período</th>
                  <th>Responsável</th>
                  <th>Inscritos</th>
                </tr>
              </thead>
              <tbody>
                {offers.map((offer, index) => (
                  <tr key={index}>
                    <td>{offer.name}</td>
                    <td>{offer.semester}</td>
                    <td>{formatDate(offer.startDate)} – {formatDate(offer.endDate)}</td>
                    <td>{offer.responsibleProfessor}</td>
                    <td>{offer.enrolledStudents}</td>
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
