package br.ufscar.pescd.config;

import br.ufscar.pescd.entity.Documentation;
import br.ufscar.pescd.entity.Offer;
import br.ufscar.pescd.entity.OfferStudent;
import br.ufscar.pescd.entity.User;
import br.ufscar.pescd.entity.Report;
import br.ufscar.pescd.entity.WorkPlan;
import br.ufscar.pescd.enums.GradeOption;
import br.ufscar.pescd.enums.OfferStatus;
import br.ufscar.pescd.enums.StudentOfferStatus;
import br.ufscar.pescd.enums.UserRole;
import br.ufscar.pescd.repository.DocumentationRepository;
import br.ufscar.pescd.repository.OfferRepository;
import br.ufscar.pescd.repository.OfferStudentRepository;
import br.ufscar.pescd.repository.UserRepository;
import br.ufscar.pescd.repository.ReportRepository;
import br.ufscar.pescd.repository.WorkPlanRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OfferRepository offerRepository;
    private final OfferStudentRepository offerStudentRepository;
    private final WorkPlanRepository workPlanRepository;
    private final ReportRepository reportRepository;
    private final DocumentationRepository documentationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            OfferRepository offerRepository,
            OfferStudentRepository offerStudentRepository,
            WorkPlanRepository workPlanRepository,
            ReportRepository reportRepository,
            DocumentationRepository documentationRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.offerRepository = offerRepository;
        this.offerStudentRepository = offerStudentRepository;
        this.workPlanRepository = workPlanRepository;
        this.reportRepository = reportRepository;
        this.documentationRepository = documentationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        User admin = upsertUser("Administrador do Sistema", "admin@pescd.com", "admin", "admin123", UserRole.ADMIN);
        upsertUser("Secretário do Programa", "secretario@pescd.com", "secretario", "123456", UserRole.SECRETARIO);

        User professorLevada = upsertUser("Alexandre Levada", "alexandre.levada@ufscar.br", "alevada", "123456", UserRole.PROFESSOR);
        User professorNaldi = upsertUser("Murilo Naldi", "murilo.naldi@ufscar.br", "mnaldi", "123456", UserRole.PROFESSOR);
        User professorComin = upsertUser("César Comin", "cesar.comin@ufscar.br", "ccomin", "123456", UserRole.PROFESSOR);
        User professorEndo = upsertUser("André Endo", "andre.endo@ufscar.br", "aendo", "123456", UserRole.PROFESSOR);

        User aluno1 = upsertUser("Lucas Ferreira", "lucas.ferreira@estudante.ufscar.br", "lferreira", "123456", UserRole.ALUNO);
        User aluno2 = upsertUser("Marina Souza", "marina.souza@estudante.ufscar.br", "msouza", "123456", UserRole.ALUNO);
        User aluno3 = upsertUser("João Lima", "joao.lima@estudante.ufscar.br", "jlima", "123456", UserRole.ALUNO);
        User aluno4 = upsertUser("Beatriz Costa", "beatriz.costa@estudante.ufscar.br", "bcosta", "123456", UserRole.ALUNO);

        removeLegacyMockOffers();

        Offer offer1 = upsertOffer(
                "Algoritmo e Estrutura de Dados I",
                "2026/1",
                LocalDate.of(2026, 3, 3),
                LocalDate.of(2026, 7, 10),
                professorLevada,
                admin
        );
        Offer offer2 = upsertOffer(
                "Aprendizado de Máquina I",
                "2025/2",
                LocalDate.of(2025, 8, 4),
                LocalDate.of(2025, 12, 5),
                professorNaldi,
                admin
        );
        Offer offer3 = upsertOffer(
                "Processamento Digital de Imagens",
                "2025/1",
                LocalDate.of(2025, 3, 10),
                LocalDate.of(2025, 7, 18),
                professorComin,
                admin
        );
        Offer offer4 = upsertOffer(
                "Desenvolvimento Web I",
                "2024/2",
                LocalDate.of(2024, 8, 12),
                LocalDate.of(2024, 12, 6),
                professorEndo,
                admin
        );

        enroll(aluno1, offer1, StudentOfferStatus.NAO_ENVIADO);
        enroll(aluno2, offer1, StudentOfferStatus.NAO_ENVIADO);
        enroll(aluno3, offer2, StudentOfferStatus.NAO_ENVIADO);
        enroll(aluno4, offer2, StudentOfferStatus.NAO_ENVIADO);
        enroll(aluno1, offer3, StudentOfferStatus.NAO_ENVIADO);
        enroll(aluno4, offer3, StudentOfferStatus.NAO_ENVIADO);
        enroll(aluno2, offer4, StudentOfferStatus.NAO_ENVIADO);

        // --- Seed PS.01 / PS.02: João Lima em offer1 com PLANO_ENVIADO sob supervisão de Prof. Levada ---
        OfferStudent seedPS02 = enrollWithSupervisor(
                aluno3, offer1, professorLevada, StudentOfferStatus.PLANO_ENVIADO);
        seedWorkPlan(seedPS02);

        // --- Seed PS.03: Beatriz Costa em offer4 com RELATORIO_ENVIADO sob supervisão de Prof. Endo ---
        OfferStudent seedPS03 = enrollWithSupervisor(
                aluno4, offer4, professorEndo, StudentOfferStatus.RELATORIO_ENVIADO);
        seedWorkPlan(seedPS03);
        seedReport(seedPS03);

        // --- Seed PR.01: Lucas Ferreira em offer4 com RELATORIO_APROVADO_SUPERVISOR (Prof. Endo é responsável) ---
        OfferStudent seedPR01 = enrollWithSupervisor(
                aluno1, offer4, professorComin, StudentOfferStatus.RELATORIO_APROVADO_SUPERVISOR);
        seedWorkPlan(seedPR01);
        seedReportWithSupervisorApproval(seedPR01);

        // --- Seed PR.02: João Lima em offer2 com DOCUMENTACAO_ENVIADA (Prof. Naldi é responsável) ---
        OfferStudent seedPR02 = enroll(aluno3, offer2, StudentOfferStatus.DOCUMENTACAO_ENVIADA);
        seedDocumentation(seedPR02);
    }

    private User upsertUser(
            String fullName,
            String email,
            String username,
            String rawPassword,
            UserRole role
    ) {
        User user = userRepository.findByUsername(username).orElseGet(User::new);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setUsername(username);
        user.setRole(role);
        user.setEnabled(true);

        if (user.getId() == null) {
            user.setPassword(passwordEncoder.encode(rawPassword));
        }

        return userRepository.save(user);
    }

    private Offer upsertOffer(
            String name,
            String semester,
            LocalDate startDate,
            LocalDate endDate,
            User responsibleProfessor,
            User createdBy
    ) {
        Offer offer = offerRepository.findByName(name).orElseGet(Offer::new);
        offer.setName(name);
        offer.setSemester(semester);
        offer.setStartDate(startDate);
        offer.setEndDate(endDate);
        offer.setStatus(OfferStatus.AGUARDANDO_ENCERRAMENTO_SECRETARIO);
        offer.setResponsibleProfessor(responsibleProfessor);
        offer.setCreatedBy(createdBy);
        return offerRepository.save(offer);
    }

    private OfferStudent enroll(User student, Offer offer, StudentOfferStatus status) {
        OfferStudent offerStudent = offerStudentRepository
                .findByOfferAndStudent(offer, student)
                .orElseGet(OfferStudent::new);
        offerStudent.setStudent(student);
        offerStudent.setOffer(offer);
        offerStudent.setStatus(status);
        return offerStudentRepository.save(offerStudent);
    }

    private void seedWorkPlan(OfferStudent offerStudent) {
        if (workPlanRepository.findByOfferStudent(offerStudent).isPresent()) {
            return;
        }

        WorkPlan workPlan = new WorkPlan();
        workPlan.setOfferStudent(offerStudent);
        workPlan.setDisciplineCode("1000123");
        workPlan.setDisciplineName("Algoritmo e Estrutura de Dados I");
        workPlan.setDisciplineCourse("Bacharelado em Ciência da Computação");
        workPlan.setFileName("plano-exemplo.pdf");
        workPlan.setContentType("application/pdf");
        workPlan.setFileContent(
                "%PDF-1.4\n% Plano de trabalho de exemplo (PESCD)\n".getBytes(StandardCharsets.UTF_8));
        workPlanRepository.save(workPlan);
    }

    private void seedReport(OfferStudent offerStudent) {
        if (reportRepository.findByOfferStudent(offerStudent).isPresent()) {
            return;
        }

        Report report = new Report();
        report.setOfferStudent(offerStudent);
        report.setFrequency(85);
        report.setFileName("relatorio-exemplo.pdf");
        report.setContentType("application/pdf");
        report.setFileContent(
                "%PDF-1.4\n% Relatório de exemplo (PESCD)\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        reportRepository.save(report);
    }

    private void seedReportWithSupervisorApproval(OfferStudent offerStudent) {
        if (reportRepository.findByOfferStudent(offerStudent).isPresent()) {
            return;
        }

        Report report = new Report();
        report.setOfferStudent(offerStudent);
        report.setFrequency(90);
        report.setFileName("relatorio-exemplo.pdf");
        report.setContentType("application/pdf");
        report.setFileContent(
                "%PDF-1.4\n% Relatório de exemplo (PESCD)\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        // Dados do supervisor (simulando PS.03 já concluído)
        report.setSupervisorParecer("Relatório bem elaborado. Aluno demonstrou domínio do conteúdo.");
        report.setSupervisorFrequencia(88);
        report.setSupervisorNotaSugestao(GradeOption.B);
        report.setSupervisorApprovedAt(java.time.LocalDateTime.now().minusDays(3));
        reportRepository.save(report);
    }

    private OfferStudent enrollWithSupervisor(
            User student,
            Offer offer,
            User supervisor,
            StudentOfferStatus status
    ) {
        OfferStudent os = offerStudentRepository
                .findByOfferAndStudent(offer, student)
                .orElseGet(OfferStudent::new);
        os.setStudent(student);
        os.setOffer(offer);
        os.setSupervisor(supervisor);
        os.setStatus(status);
        return offerStudentRepository.save(os);
    }

    private void seedDocumentation(OfferStudent offerStudent) {
        if (documentationRepository.findByOfferStudent(offerStudent).isPresent()) {
            return;
        }

        Documentation doc = new Documentation();
        doc.setOfferStudent(offerStudent);
        doc.setInstitutionName("Universidade Federal de São Carlos");
        doc.setDisciplineName("Aprendizado de Máquina I");
        doc.setDisciplineCourse("Bacharelado em Ciência da Computação");
        doc.setWorkloadHours(60);
        doc.setFileName("documentacao-exemplo.pdf");
        doc.setContentType("application/pdf");
        doc.setFileContent(
                "%PDF-1.4\n% Documentação comprobatória de exemplo (PESCD)\n"
                        .getBytes(StandardCharsets.UTF_8));
        documentationRepository.save(doc);
    }

    private void removeLegacyMockOffers() {
        List<String> legacyNames = List.of(
                "Estágio Docência em Algoritmos",
                "Monitoria Avançada em Banco de Dados",
                "Prática Supervisionada em Engenharia de Software"
        );

        offerRepository.findAll().stream()
                .filter(offer -> legacyNames.contains(offer.getName()))
                .forEach(offer -> {
                    offerStudentRepository.deleteAll(offerStudentRepository.findByOffer(offer));
                    offerRepository.delete(offer);
                });
    }
}
