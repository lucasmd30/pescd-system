import OffersWithStudents from './OffersWithStudents.jsx';

// PS.01 - ofertas e alunos que o professor supervisiona.
export default function SupervisorDashboard() {
  return (
    <OffersWithStudents
      title="Professor supervisor"
      subtitle="Ofertas e alunos que você supervisiona."
      endpoint="/api/professor/supervisor/offers"
      studentLink={(offerId, studentId) =>
        `/professor/supervisor/ofertas/${offerId}/alunos/${studentId}`}
    />
  );
}
