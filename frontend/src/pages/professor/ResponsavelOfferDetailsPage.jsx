import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../../api/client.js';
import { formatDate } from '../../utils/format.js';
import { ErrorMessage, Loading, StatusBadge } from '../../components/Feedback.jsx';

// PR.04 - acompanhamento de uma oferta pelo responsável, com busca de alunos por
// nome (espelha professor/responsavel/offer-details.html).
export default function ResponsavelOfferDetailsPage() {
  const { offerId } = useParams();
  const base = `/api/professor/responsavel/offers/${offerId}`;

  const [offer, setOffer] = useState(null);
  const [students, setStudents] = useState([]);
  const [term, setTerm] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    api.get(base)
      .then((d) => { setOffer(d.offer); setStudents(d.students); })
      .catch((e) => setError(e.message));
  }, [offerId]);

  async function search(event) {
    event.preventDefault();
    setError('');
    try {
      const query = term ? `?name=${encodeURIComponent(term)}` : '';
      setStudents(await api.get(`${base}/students${query}`));
    } catch (e) {
      setError(e.message);
    }
  }

  if (!offer && !error) return <Loading />;
  if (!offer) return <ErrorMessage>{error}</ErrorMessage>;

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>{offer.name}</h1>
          <p>{offer.semester} · {formatDate(offer.startDate)} – {formatDate(offer.endDate)}</p>
        </div>
        <Link className="btn btn-ghost" to="/professor/responsavel">Voltar</Link>
      </div>

      <ErrorMessage>{error}</ErrorMessage>

      <div className="card">
        <h2 style={{ marginTop: 0, fontSize: 18 }}>Informações da oferta</h2>
        <dl className="detail-grid">
          <dt>Status</dt><dd><StatusBadge label={offer.statusLabel} status={offer.status} /></dd>
          <dt>Responsável</dt><dd>{offer.responsibleProfessor?.fullName || '—'}</dd>
          <dt>Inscritos</dt><dd>{offer.enrolledStudents}</dd>
        </dl>
      </div>

      <div className="card">
        <h2 style={{ marginTop: 0, fontSize: 18 }}>Alunos inscritos</h2>
        <form onSubmit={search} className="form-actions" style={{ marginBottom: 16 }}>
          <input
            value={term}
            onChange={(e) => setTerm(e.target.value)}
            placeholder="Buscar aluno pelo nome"
            style={{ flex: 1, padding: '9px 11px', border: '1px solid var(--border)', borderRadius: 8 }}
          />
          <button className="btn" type="submit">Buscar</button>
        </form>
        {students.length === 0 ? (
          <p className="muted">Nenhum aluno encontrado.</p>
        ) : (
          <table>
            <thead>
              <tr><th>Aluno</th><th>E-mail</th><th>Status</th><th></th></tr>
            </thead>
            <tbody>
              {students.map((s) => (
                <tr key={s.enrollmentId}>
                  <td>{s.student.fullName}</td>
                  <td>{s.student.email}</td>
                  <td><StatusBadge label={s.statusLabel} status={s.status} /></td>
                  <td>
                    <Link to={`/professor/responsavel/ofertas/${offerId}/alunos/${s.student.id}`}>
                      Ver detalhes
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
