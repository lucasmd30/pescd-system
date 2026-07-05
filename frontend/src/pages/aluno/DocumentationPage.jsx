import { useRef, useState } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { api } from '../../api/client.js';
import { ErrorMessage } from '../../components/Feedback.jsx';

// AL.03 - Envio da documentação comprobatória (PDF <= 5MB).
export default function DocumentationPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const fileRef = useRef(null);
  const [form, setForm] = useState({
    institutionName: '',
    disciplineName: '',
    disciplineCourse: '',
    workloadHours: '',
  });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  function update(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const file = fileRef.current.files[0];
      if (!file) throw new Error('Selecione o arquivo PDF da documentação.');
      const data = new FormData();
      data.append('institutionName', form.institutionName);
      data.append('disciplineName', form.disciplineName);
      data.append('disciplineCourse', form.disciplineCourse);
      data.append('workloadHours', form.workloadHours);
      data.append('file', file);
      await api.postForm(`/api/aluno/ofertas/${id}/documentacao`, data);
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
          <h1>Enviar documentação</h1>
          <p>AL.03 — PDF de até 5 MB.</p>
        </div>
        <Link className="btn btn-ghost" to={`/aluno/ofertas/${id}`}>Voltar</Link>
      </div>
      <div className="card" style={{ maxWidth: 620 }}>
        <form onSubmit={handleSubmit}>
          <ErrorMessage>{error}</ErrorMessage>
          <div className="form-group">
            <label>Instituição *</label>
            <input value={form.institutionName} onChange={(e) => update('institutionName', e.target.value)} required />
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
            <label>Carga horária (h) *</label>
            <input
              type="number"
              min="1"
              value={form.workloadHours}
              onChange={(e) => update('workloadHours', e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label>Arquivo (PDF) *</label>
            <input type="file" accept="application/pdf,.pdf" ref={fileRef} required />
          </div>
          <div className="form-actions">
            <button className="btn" type="submit" disabled={submitting}>
              {submitting ? 'Enviando…' : 'Enviar documentação'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
