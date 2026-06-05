package br.ufscar.pescd.service;

import br.ufscar.pescd.dto.DocumentationForm;
import br.ufscar.pescd.dto.ReportForm;
import br.ufscar.pescd.dto.WorkPlanForm;
import br.ufscar.pescd.entity.*;
import br.ufscar.pescd.enums.OfferStatus;
import br.ufscar.pescd.enums.StudentOfferStatus;
import br.ufscar.pescd.enums.UserRole;
import br.ufscar.pescd.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class StudentOfferService {

    private static final long MAX_FILE_SIZE = 5L * 1024 * 1024;

    private final OfferStudentRepository offerStudentRepository;
    private final UserRepository userRepository;
    private final WorkPlanRepository workPlanRepository;
    private final DocumentationRepository documentationRepository;
    private final ReportRepository reportRepository;
    private final StatusChangeLogRepository statusChangeLogRepository;

    public StudentOfferService(
            OfferStudentRepository offerStudentRepository,
            UserRepository userRepository,
            WorkPlanRepository workPlanRepository,
            DocumentationRepository documentationRepository,
            ReportRepository reportRepository,
            StatusChangeLogRepository statusChangeLogRepository
    ) {
        this.offerStudentRepository = offerStudentRepository;
        this.userRepository = userRepository;
        this.workPlanRepository = workPlanRepository;
        this.documentationRepository = documentationRepository;
        this.reportRepository = reportRepository;
        this.statusChangeLogRepository = statusChangeLogRepository;
    }

    @Transactional(readOnly = true)
    public List<OfferStudent> findStudentEnrollments(String username) {
        User student = getStudent(username);
        return offerStudentRepository.findByStudent(student);
    }

    @Transactional(readOnly = true)
    public OfferStudent getEnrollment(Long offerStudentId, String username) {
        OfferStudent enrollment = offerStudentRepository.findById(offerStudentId)
                .orElseThrow(() -> new IllegalArgumentException("Inscrição não encontrada."));

        if (!enrollment.getStudent().getUsername().equals(username)) {
            throw new IllegalArgumentException("Esta inscrição não pertence ao aluno logado.");
        }
        return enrollment;
    }

    @Transactional(readOnly = true)
    public List<User> listProfessors() {
        return userRepository.findByRole(UserRole.PROFESSOR);
    }

    @Transactional(readOnly = true)
    public WorkPlan getWorkPlan(OfferStudent enrollment) {
        return workPlanRepository.findByOfferStudent(enrollment).orElse(null);
    }

    @Transactional(readOnly = true)
    public Documentation getDocumentation(OfferStudent enrollment) {
        return documentationRepository.findByOfferStudent(enrollment).orElse(null);
    }

    @Transactional(readOnly = true)
    public Report getReport(OfferStudent enrollment) {
        return reportRepository.findByOfferStudent(enrollment).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<StatusChangeLog> getStatusLogs(OfferStudent enrollment) {
        return statusChangeLogRepository.findByOfferStudentOrderByChangedAtAsc(enrollment);
    }

    @Transactional
    public void submitWorkPlan(Long offerStudentId, String username, WorkPlanForm form) {
        OfferStudent enrollment = getEnrollment(offerStudentId, username);
        requireOfferInProgress(enrollment);
        requireStatus(enrollment, StudentOfferStatus.NAO_ENVIADO);

        User supervisor = userRepository.findById(form.getSupervisorId())
                .filter(user -> user.getRole() == UserRole.PROFESSOR)
                .orElseThrow(() -> new IllegalArgumentException("Professor supervisor inválido."));

        byte[] content = readPdf(form.getFile());

        WorkPlan workPlan = workPlanRepository.findByOfferStudent(enrollment).orElseGet(WorkPlan::new);
        workPlan.setOfferStudent(enrollment);
        workPlan.setDisciplineCode(form.getDisciplineCode());
        workPlan.setDisciplineName(form.getDisciplineName());
        workPlan.setDisciplineCourse(form.getDisciplineCourse());
        workPlan.setFileName(form.getFile().getOriginalFilename());
        workPlan.setContentType(form.getFile().getContentType());
        workPlan.setFileContent(content);
        workPlanRepository.save(workPlan);

        enrollment.setSupervisor(supervisor);
        changeStatus(enrollment, StudentOfferStatus.PLANO_ENVIADO, "Plano de trabalho enviado");
    }

    @Transactional
    public void submitDocumentation(Long offerStudentId, String username, DocumentationForm form) {
        OfferStudent enrollment = getEnrollment(offerStudentId, username);
        requireOfferInProgress(enrollment);
        requireStatus(enrollment, StudentOfferStatus.NAO_ENVIADO);

        byte[] content = readPdf(form.getFile());

        Documentation documentation = documentationRepository.findByOfferStudent(enrollment)
                .orElseGet(Documentation::new);
        documentation.setOfferStudent(enrollment);
        documentation.setInstitutionName(form.getInstitutionName());
        documentation.setDisciplineName(form.getDisciplineName());
        documentation.setDisciplineCourse(form.getDisciplineCourse());
        documentation.setWorkloadHours(form.getWorkloadHours());
        documentation.setFileName(form.getFile().getOriginalFilename());
        documentation.setContentType(form.getFile().getContentType());
        documentation.setFileContent(content);
        documentationRepository.save(documentation);

        changeStatus(enrollment, StudentOfferStatus.DOCUMENTACAO_ENVIADA, "Documentação enviada");
    }

    @Transactional
    public void submitReport(Long offerStudentId, String username, ReportForm form) {
        OfferStudent enrollment = getEnrollment(offerStudentId, username);
        requireOfferInProgress(enrollment);
        requireStatus(enrollment, StudentOfferStatus.PLANO_APROVADO);

        byte[] content = readPdf(form.getFile());

        Report report = reportRepository.findByOfferStudent(enrollment).orElseGet(Report::new);
        report.setOfferStudent(enrollment);
        report.setFrequency(form.getFrequency());
        report.setFileName(form.getFile().getOriginalFilename());
        report.setContentType(form.getFile().getContentType());
        report.setFileContent(content);
        reportRepository.save(report);

        changeStatus(enrollment, StudentOfferStatus.RELATORIO_ENVIADO, "Relatório final enviado");
    }

    private void changeStatus(OfferStudent enrollment, StudentOfferStatus newStatus, String description) {
        StatusChangeLog log = new StatusChangeLog();
        log.setOfferStudent(enrollment);
        log.setPreviousStatus(enrollment.getStatus());
        log.setNewStatus(newStatus);
        log.setDescription(description);
        statusChangeLogRepository.save(log);

        enrollment.setStatus(newStatus);
        offerStudentRepository.save(enrollment);
    }

    private void requireOfferInProgress(OfferStudent enrollment) {
        if (enrollment.getOffer().getStatus() != OfferStatus.EM_ANDAMENTO) {
            throw new IllegalArgumentException("Esta ação só é permitida em ofertas em andamento.");
        }
    }

    private void requireStatus(OfferStudent enrollment, StudentOfferStatus expected) {
        if (enrollment.getStatus() != expected) {
            throw new IllegalArgumentException(
                    "Ação indisponível: o status atual é \"" + enrollment.getStatus().getDisplayName() + "\".");
        }
    }

    private byte[] readPdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo é obrigatório.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("O arquivo deve ter no máximo 5 MB.");
        }
        String contentType = file.getContentType();
        String name = file.getOriginalFilename();
        boolean isPdf = "application/pdf".equalsIgnoreCase(contentType)
                || (name != null && name.toLowerCase().endsWith(".pdf"));
        if (!isPdf) {
            throw new IllegalArgumentException("O arquivo deve estar no formato PDF.");
        }
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Não foi possível ler o arquivo enviado.", ex);
        }
    }

    private User getStudent(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Aluno não encontrado."));
    }
}
