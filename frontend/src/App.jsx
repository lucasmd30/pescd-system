import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';

import LoginPage from './pages/LoginPage.jsx';
import HomePage from './pages/HomePage.jsx';
import PublicOffersPage from './pages/PublicOffersPage.jsx';

import UsersPage from './pages/admin/UsersPage.jsx';

import OffersListPage from './pages/secretary/OffersListPage.jsx';
import OfferCreatePage from './pages/secretary/OfferCreatePage.jsx';
import OfferDetailsPage from './pages/secretary/OfferDetailsPage.jsx';
import StudentDetailsPage from './pages/secretary/StudentDetailsPage.jsx';

import EnrollmentsPage from './pages/aluno/EnrollmentsPage.jsx';
import EnrollmentDetailsPage from './pages/aluno/EnrollmentDetailsPage.jsx';
import WorkPlanPage from './pages/aluno/WorkPlanPage.jsx';
import DocumentationPage from './pages/aluno/DocumentationPage.jsx';
import ReportPage from './pages/aluno/ReportPage.jsx';

import SupervisorDashboard from './pages/professor/SupervisorDashboard.jsx';
import SupervisorEnrollmentPage from './pages/professor/SupervisorEnrollmentPage.jsx';
import ResponsavelDashboard from './pages/professor/ResponsavelDashboard.jsx';
import ResponsavelEnrollmentPage from './pages/professor/ResponsavelEnrollmentPage.jsx';
import ResponsavelOfferDetailsPage from './pages/professor/ResponsavelOfferDetailsPage.jsx';
import ResponsavelCloseOfferPage from './pages/professor/ResponsavelCloseOfferPage.jsx';

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/" element={<Layout />}>
        <Route index element={<HomePage />} />
        <Route path="ofertas" element={<PublicOffersPage />} />

        {/* Administrador (AD.01) */}
        <Route
          path="admin/usuarios"
          element={<ProtectedRoute roles={['ADMIN']}><UsersPage /></ProtectedRoute>}
        />

        {/* Secretário (S.01, S.02, ...) */}
        <Route
          path="secretaria/ofertas"
          element={<ProtectedRoute roles={['SECRETARIO']}><OffersListPage /></ProtectedRoute>}
        />
        <Route
          path="secretaria/ofertas/nova"
          element={<ProtectedRoute roles={['SECRETARIO']}><OfferCreatePage /></ProtectedRoute>}
        />
        <Route
          path="secretaria/ofertas/:offerId"
          element={<ProtectedRoute roles={['SECRETARIO']}><OfferDetailsPage /></ProtectedRoute>}
        />
        <Route
          path="secretaria/ofertas/:offerId/alunos/:offerStudentId"
          element={<ProtectedRoute roles={['SECRETARIO']}><StudentDetailsPage /></ProtectedRoute>}
        />

        {/* Aluno (AL.01 a AL.04) */}
        <Route
          path="aluno/ofertas"
          element={<ProtectedRoute roles={['ALUNO']}><EnrollmentsPage /></ProtectedRoute>}
        />
        <Route
          path="aluno/ofertas/:id"
          element={<ProtectedRoute roles={['ALUNO']}><EnrollmentDetailsPage /></ProtectedRoute>}
        />
        <Route
          path="aluno/ofertas/:id/plano"
          element={<ProtectedRoute roles={['ALUNO']}><WorkPlanPage /></ProtectedRoute>}
        />
        <Route
          path="aluno/ofertas/:id/documentacao"
          element={<ProtectedRoute roles={['ALUNO']}><DocumentationPage /></ProtectedRoute>}
        />
        <Route
          path="aluno/ofertas/:id/relatorio"
          element={<ProtectedRoute roles={['ALUNO']}><ReportPage /></ProtectedRoute>}
        />

        {/* Professor supervisor (PS.*) */}
        <Route
          path="professor/supervisor"
          element={<ProtectedRoute roles={['PROFESSOR']}><SupervisorDashboard /></ProtectedRoute>}
        />
        <Route
          path="professor/supervisor/ofertas/:offerId/alunos/:studentId"
          element={<ProtectedRoute roles={['PROFESSOR']}><SupervisorEnrollmentPage /></ProtectedRoute>}
        />

        {/* Professor responsável (PR.*) */}
        <Route
          path="professor/responsavel"
          element={<ProtectedRoute roles={['PROFESSOR']}><ResponsavelDashboard /></ProtectedRoute>}
        />
        <Route
          path="professor/responsavel/ofertas/:offerId"
          element={<ProtectedRoute roles={['PROFESSOR']}><ResponsavelOfferDetailsPage /></ProtectedRoute>}
        />
        <Route
          path="professor/responsavel/ofertas/:offerId/encerrar"
          element={<ProtectedRoute roles={['PROFESSOR']}><ResponsavelCloseOfferPage /></ProtectedRoute>}
        />
        <Route
          path="professor/responsavel/ofertas/:offerId/alunos/:studentId"
          element={<ProtectedRoute roles={['PROFESSOR']}><ResponsavelEnrollmentPage /></ProtectedRoute>}
        />
      </Route>
    </Routes>
  );
}
