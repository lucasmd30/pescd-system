package br.ufscar.pescd.service;

import br.ufscar.pescd.dto.AnalyzeDocumentationFormDTO;
import br.ufscar.pescd.dto.ConcludeReportFormDTO;
import br.ufscar.pescd.dto.ResponsavelDashboardDTO;
import br.ufscar.pescd.entity.*;
import br.ufscar.pescd.enums.OfferStatus;
import br.ufscar.pescd.enums.StudentOfferStatus;
import br.ufscar.pescd.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProfessorResponsavelService {

    private final OfferRepository offerRepository;
    private final OfferStudentRepository offerStudentRepository;
    private final WorkPlanRepository workPlanRepository;
    private final ReportRepository reportRepository;
    private final DocumentationRepository documentationRepository;
    private final StatusChangeLogRepository statusChangeLogRepository;

    public ProfessorResponsavelService(
            OfferStudentRepository offerStudentRepository,
            WorkPlanRepository workPlanRepository,
            ReportRepository reportRepository,
            DocumentationRepository documentationRepository,
            StatusChangeLogRepository statusChangeLogRepository,
            OfferRepository offerRepository
    ) {
        this.offerStudentRepository = offerStudentRepository;
        this.workPlanRepository = workPlanRepository;
        this.reportRepository = reportRepository;
        this.documentationRepository = documentationRepository;
        this.statusChangeLogRepository = statusChangeLogRepository;
        this.offerRepository = offerRepository;
    }

    // -------------------------------------------------------------------------
    // Dashboard — lista todas as ofertas onde é responsável com seus alunos
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ResponsavelDashboardDTO> getDashboard(User professor) {

        List<OfferStudent> allEnrollments =
                offerStudentRepository.findByOffer_ResponsibleProfessor(professor);

        Map<Offer, List<OfferStudent>> byOffer = allEnrollments.stream()
                .collect(Collectors.groupingBy(OfferStudent::getOffer));

        return byOffer.entrySet().stream()
                .map(e -> new ResponsavelDashboardDTO(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(
                        dto -> dto.getOffer().getSemester(),
                        Comparator.reverseOrder()
                ))
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Utilitários de acesso — compartilhados por PR.01 e PR.02
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public OfferStudent getEnrollmentForResponsavel(Long offerId, Long studentId, User professor) {

        OfferStudent os = offerStudentRepository
                .findByOffer_IdAndStudent_Id(offerId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("Inscrição não encontrada."));

        if (!professor.equals(os.getOffer().getResponsibleProfessor())) {
            throw new AccessDeniedException(
                    "Você não é o professor responsável desta oferta.");
        }

        return os;
    }

    @Transactional(readOnly = true)
    public WorkPlan getWorkPlan(OfferStudent offerStudent) {
        return workPlanRepository.findByOfferStudent(offerStudent)
                .orElseThrow(() -> new IllegalArgumentException("Plano de trabalho não encontrado."));
    }

    @Transactional(readOnly = true)
    public Report getReport(OfferStudent offerStudent) {
        return reportRepository.findByOfferStudent(offerStudent)
                .orElseThrow(() -> new IllegalArgumentException("Relatório não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<StatusChangeLog> getStatusLogs(OfferStudent offerStudent) {
        return statusChangeLogRepository.findByOfferStudentOrderByChangedAtAsc(offerStudent);
    }

    // -------------------------------------------------------------------------
    // PR.01 — Concluir Relatório
    // -------------------------------------------------------------------------

    @Transactional
    public void concludeReport(Long offerId, Long studentId, User professor, ConcludeReportFormDTO form) {

        OfferStudent os = getEnrollmentForResponsavel(offerId, studentId, professor);

        if (os.getStatus() != StudentOfferStatus.RELATORIO_APROVADO_SUPERVISOR) {
            throw new IllegalArgumentException(
                    "Ação indisponível: o status atual é \""
                    + os.getStatus().getDisplayName() + "\".");
        }

        Report report = getReport(os);
        report.setResponsavelParecer(form.getParecer());
        report.setResponsavelFrequencia(form.getFrequencia());
        report.setResponsavelNota(form.getNota());
        report.setResponsavelApprovedAt(LocalDateTime.now());
        reportRepository.save(report);

        changeStatus(os, StudentOfferStatus.CONCLUIDO_RESPONSAVEL,
                "Relatório concluído pelo professor responsável");
    }

    // -------------------------------------------------------------------------
    // PR.02 — Analisar Documentação
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Documentation getDocumentation(OfferStudent offerStudent) {
        return documentationRepository.findByOfferStudent(offerStudent)
                .orElseThrow(() -> new IllegalArgumentException("Documentação não encontrada."));
    }

    @Transactional(readOnly = true)
    public Offer getOffer(Long offerId) {

        return offerRepository.findById(offerId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Oferta não encontrada."));
    }

    @Transactional
    public void closeOffer(
            Long offerId,
            User professor,
            String lessonsLearned
    ) {

        Offer offer = getOffer(offerId);

        if (!offer.getResponsibleProfessor().getId().equals(professor.getId())) {
            throw new IllegalArgumentException(
                    "Você não é o responsável por esta oferta."
            );
        }

        List<OfferStudent> students =
                offerStudentRepository.findByOffer(offer);

        boolean allFinished = students.stream()
                .allMatch(s ->
                        s.getStatus() == StudentOfferStatus.CONCLUIDO_RESPONSAVEL);

        if (!allFinished) {
            throw new IllegalArgumentException(
                    "Todos os alunos devem estar concluídos antes do encerramento."
            );
        }

        offer.setLessonsLearned(lessonsLearned);
        offer.setStatus(OfferStatus.AGUARDANDO_ENCERRAMENTO_SECRETARIO);

        offerRepository.save(offer);
    }

    @Transactional
    public void analyzeDocumentation(Long offerId, Long studentId, User professor,
                                     AnalyzeDocumentationFormDTO form) {

        OfferStudent os = getEnrollmentForResponsavel(offerId, studentId, professor);

        if (os.getStatus() != StudentOfferStatus.DOCUMENTACAO_ENVIADA) {
            throw new IllegalArgumentException(
                    "Ação indisponível: o status atual é \""
                    + os.getStatus().getDisplayName() + "\".");
        }

        Documentation doc = getDocumentation(os);
        doc.setResponsavelParecer(form.getParecer());
        doc.setResponsavelFrequencia(form.getFrequencia());
        doc.setResponsavelNota(form.getNota());
        doc.setResponsavelApprovedAt(LocalDateTime.now());
        documentationRepository.save(doc);

        changeStatus(os, StudentOfferStatus.CONCLUIDO_RESPONSAVEL,
                "Documentação analisada pelo professor responsável");
    }

    // -------------------------------------------------------------------------
    // Utilitário — log de status
    // -------------------------------------------------------------------------

    private void changeStatus(OfferStudent os, StudentOfferStatus newStatus, String description) {
        StatusChangeLog log = new StatusChangeLog();
        log.setOfferStudent(os);
        log.setPreviousStatus(os.getStatus());
        log.setNewStatus(newStatus);
        log.setDescription(description);
        statusChangeLogRepository.save(log);

        os.setStatus(newStatus);
        offerStudentRepository.save(os);
    }
}
