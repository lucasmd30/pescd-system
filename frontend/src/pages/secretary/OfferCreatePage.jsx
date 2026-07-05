import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { api } from '../../api/client.js';
import { ErrorMessage } from '../../components/Feedback.jsx';

// S.01 - Criar oferta. Nome é opcional (gerado do semestre se vazio); professor
// responsável vem da lista do BD; a data de fim deve ser depois da de início.
export default function OfferCreatePage() {
  const navigate = useNavigate();
  const [professors, setProfessors] = useState([]);
  const [form, setForm] = useState({
    name: '',
    semester: '',
    startDate: '',
    endDate: '',
    responsibleProfessorId: '',
  });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    api.get('/api/secretary/offers/professors')
      .then(setProfessors)
      .catch((e) => setError(e.message));
  }, []);

  function update(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const created = await api.post('/api/secretary/offers', {
        name: form.name || null,
        semester: form.semester,
        startDate: form.startDate,
        endDate: form.endDate,
        responsibleProfessorId: Number(form.responsibleProfessorId),
      });
      navigate(`/secretaria/ofertas/${created.id}`);
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <div className="page-header">
        <div>
          <h1>Nova oferta</h1>
          <p>S.01 — criação de oferta.</p>
        </div>
        <Link className="btn btn-ghost" to="/secretaria/ofertas">Voltar</Link>
      </div>
      <div className="card" style={{ maxWidth: 620 }}>
        <form onSubmit={handleSubmit}>
          <ErrorMessage>{error}</ErrorMessage>
          <div className="form-group">
            <label>Nome (opcional)</label>
            <input
              value={form.name}
              onChange={(e) => update('name', e.target.value)}
              placeholder="Gerado a partir do semestre se em branco"
            />
          </div>
          <div className="form-group">
            <label>Semestre *</label>
            <input
              value={form.semester}
              onChange={(e) => update('semester', e.target.value)}
              placeholder="2026/1"
              required
            />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Início *</label>
              <input
                type="date"
                value={form.startDate}
                onChange={(e) => update('startDate', e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label>Fim *</label>
              <input
                type="date"
                value={form.endDate}
                onChange={(e) => update('endDate', e.target.value)}
                required
              />
            </div>
          </div>
          <div className="form-group">
            <label>Professor responsável *</label>
            <select
              value={form.responsibleProfessorId}
              onChange={(e) => update('responsibleProfessorId', e.target.value)}
              required
            >
              <option value="">Selecione…</option>
              {professors.map((p) => (
                <option key={p.id} value={p.id}>{p.fullName}</option>
              ))}
            </select>
          </div>
          <div className="form-actions">
            <button className="btn" type="submit" disabled={submitting}>
              {submitting ? 'Salvando…' : 'Criar oferta'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
