import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api/client.js';
import { formatDate } from '../../utils/format.js';
import { ErrorMessage, Loading, StatusBadge } from '../../components/Feedback.jsx';

// Ação contextual disponível para cada aluno, conforme o status e o papel.
function contextualAction(role, status) {
  if (role === 'supervisor') {
    if (status === 'PLANO_ENVIADO') return 'Aprovar plano';
    if (status === 'RELATORIO_ENVIADO') return 'Aprovar relatório';
  }
  if (role === 'responsavel') {
    if (status === 'DOCUMENTACAO_ENVIADA') return 'Analisar documentação';
    if (status === 'RELATORIO_APROVADO_SUPERVISOR') return 'Concluir relatório';
  }
  return null;
}

// Lista de ofertas com seus alunos, reutilizada pelos dashboards do professor.
// role: 'supervisor' | 'responsavel'. `studentLink(offerId, studentId)` define o
// destino da linha. Para o responsável, `offerLink`/`closeLink` habilitam os
// botões por oferta (PR.04 e encerramento).
export default function OffersWithStudents({
  title, subtitle, endpoint, role, studentLink, offerLink, closeLink,
}) {
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
              {item.offer.semester} · {item.offer.statusLabel}
            </span>
          </div>

          {(offerLink || closeLink) && (
            <div className="form-actions" style={{ marginTop: 12 }}>
              {offerLink && <Link className="btn btn-ghost btn-sm" to={offerLink(item.offer.id)}>Ver oferta</Link>}
              {closeLink && <Link className="btn btn-sm" to={closeLink(item.offer.id)}>Encerrar oferta</Link>}
            </div>
          )}

          {item.students.length === 0 ? (
            <p className="muted" style={{ marginBottom: 0, marginTop: 12 }}>Sem alunos inscritos.</p>
          ) : (
            <table style={{ marginTop: 12 }}>
              <thead>
                <tr><th>Aluno</th><th>E-mail</th><th>Status</th><th>Ações</th></tr>
              </thead>
              <tbody>
                {item.students.map((s) => {
                  const action = contextualAction(role, s.status);
                  const to = studentLink(item.offer.id, s.student.id);
                  return (
                    <tr key={s.enrollmentId}>
                      <td>{s.student.fullName}</td>
                      <td>{s.student.email}</td>
                      <td><StatusBadge label={s.statusLabel} status={s.status} /></td>
                      <td>
                        {action
                          ? <Link className="btn btn-sm" to={to}>{action}</Link>
                          : <Link className="muted" to={to}>Ver detalhes</Link>}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          )}
        </div>
      ))}
    </div>
  );
}
