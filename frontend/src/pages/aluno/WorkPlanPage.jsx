import { useEffect, useRef, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { api } from '../../api/client.js';
import { ErrorMessage } from '../../components/Feedback.jsx';

// AL.02 - Envio do plano de trabalho (PDF <= 5MB) e escolha do supervisor.
export default function WorkPlanPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const fileRef = useRef(null);
  const [professors, setProfessors] = useState([]);
  const [form, setForm] = useState({
    disciplineCode: '',
    disciplineName: '',
    disciplineCourse: '',
    supervisorId: '',
  });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    api.get('/api/aluno/ofertas/professors').then(setProfessors).catch((e) => setError(e.message));
  }, []);

  function update(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const file = fileRef.current.files[0];
      if (!file) throw new Error('Selecione o arquivo PDF do plano.');
      const data = new FormData();
      data.append('disciplineCode', form.disciplineCode);
      data.append('disciplineName', form.disciplineName);
      data.append('disciplineCourse', form.disciplineCourse);
      data.append('supervisorId', form.supervisorId);
      data.append('file', file);
      await api.postForm(`/api/aluno/ofertas/${id}/plano`, data);
      navigate(`/aluno/ofertas/${id}`);
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
          <h1>Enviar plano de trabalho</h1>
          <p>AL.02 — PDF de até 5 MB.</p>
        </div>
        <Link className="btn btn-ghost" to={`/aluno/ofertas/${id}`}>Voltar</Link>
      </div>
      <div className="card" style={{ maxWidth: 620 }}>
        <form onSubmit={handleSubmit}>
          <ErrorMessage>{error}</ErrorMessage>
          <div className="form-group">
            <label>Código da disciplina *</label>
            <input value={form.disciplineCode} onChange={(e) => update('disciplineCode', e.target.value)} required />
          </div>
          <div className="form-group">
            <label>Nome da disciplina *</label>
            <input value={form.disciplineName} onChange={(e) => update('disciplineName', e.target.value)} required />
          </div>
          <div className="form-group">
            <label>Curso da disciplina *</label>
            <input value={form.disciplineCourse} onChange={(e) => update('disciplineCourse', e.target.value)} required />
          </div>
          <div className="form-group">
            <label>Professor supervisor *</label>
            <select value={form.supervisorId} onChange={(e) => update('supervisorId', e.target.value)} required>
              <option value="">Selecione…</option>
              {professors.map((p) => (
                <option key={p.id} value={p.id}>{p.fullName}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>Arquivo (PDF) *</label>
            <input type="file" accept="application/pdf,.pdf" ref={fileRef} required />
          </div>
          <div className="form-actions">
            <button className="btn" type="submit" disabled={submitting}>
              {submitting ? 'Enviando…' : 'Enviar plano'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
