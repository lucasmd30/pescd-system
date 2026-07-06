import OffersWithStudents from './OffersWithStudents.jsx';

// PR.04 - ofertas pelas quais o professor é responsável.
export default function ResponsavelDashboard() {
  return (
    <OffersWithStudents
      title="Professor responsável"
      subtitle="Ofertas pelas quais você é responsável."
      endpoint="/api/professor/responsavel/offers"
      role="responsavel"
      studentLink={(offerId, studentId) =>
        `/professor/responsavel/ofertas/${offerId}/alunos/${studentId}`}
      offerLink={(offerId) => `/professor/responsavel/ofertas/${offerId}`}
      closeLink={(offerId) => `/professor/responsavel/ofertas/${offerId}/encerrar`}
    />
  );
}
