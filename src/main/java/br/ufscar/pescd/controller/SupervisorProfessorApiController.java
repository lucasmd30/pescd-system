package br.ufscar.pescd.controller;

import br.ufscar.pescd.dto.ApprovePlanFormDTO;
import br.ufscar.pescd.dto.ApproveReportFormDTO;
import br.ufscar.pescd.dto.OfferDetailsResponse;
import br.ufscar.pescd.dto.OfferStudentDetailsResponse;
import br.ufscar.pescd.dto.OfferStudentSummaryResponse;
import br.ufscar.pescd.dto.ReportDetailsResponse;
import br.ufscar.pescd.dto.WorkPlanDetailsResponse;
import br.ufscar.pescd.entity.OfferStudent;
import br.ufscar.pescd.entity.Report;
import br.ufscar.pescd.entity.User;
import br.ufscar.pescd.entity.WorkPlan;
import br.ufscar.pescd.repository.UserRepository;
import br.ufscar.pescd.service.SupervisorProfessorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/professor/supervisor/offers")
public class SupervisorProfessorApiController {

    private final SupervisorProfessorService supervisorProfessorService;
    private final UserRepository userRepository;

    public SupervisorProfessorApiController(
            SupervisorProfessorService supervisorProfessorService,
            UserRepository userRepository
    ) {
        this.supervisorProfessorService = supervisorProfessorService;
        this.userRepository = userRepository;
    }

    // -------------------------------------------------------------------------
    // PS.01 — Visualização das ofertas e alunos inscritos
    // -------------------------------------------------------------------------

    @GetMapping
    public List<OfferDetailsResponse> listSupervisedOffers(Authentication authentication) {
        return supervisorProfessorService.getDashboardForApi(resolveUser(authentication));
    }

    @GetMapping("/{offerId}/students/{studentId}")
    public OfferStudentDetailsResponse enrollmentDetails(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            Authentication authentication
    ) {
        return supervisorProfessorService.getEnrollmentDetailsForApi(
                offerId, studentId, resolveUser(authentication));
    }

    // -------------------------------------------------------------------------
    // PS.02 — Aprovação de plano de trabalho do aluno
    // -------------------------------------------------------------------------

    @GetMapping("/{offerId}/students/{studentId}/work-plan")
    public WorkPlanDetailsResponse workPlanDetails(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            Authentication authentication
    ) {
        return supervisorProfessorService.getWorkPlanDetailsForApi(
                offerId, studentId, resolveUser(authentication));
    }

    @PostMapping("/{offerId}/students/{studentId}/work-plan/approve")
    public OfferStudentSummaryResponse approvePlan(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            @Valid @RequestBody ApprovePlanFormDTO form,
            Authentication authentication
    ) {
        return supervisorProfessorService.approvePlanForApi(
                offerId, studentId, resolveUser(authentication), form);
    }

    @GetMapping("/{offerId}/students/{studentId}/work-plan/download")
    public ResponseEntity<byte[]> downloadWorkPlan(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            Authentication authentication
    ) {
        User professor = resolveUser(authentication);
        OfferStudent os = supervisorProfessorService
                .getEnrollmentForSupervisor(offerId, studentId, professor);
        WorkPlan plan = supervisorProfessorService.getWorkPlan(os);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(plan.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + plan.getFileName() + "\"")
                .body(plan.getFileContent());
    }

    // -------------------------------------------------------------------------
    // PS.03 — Aprovação de relatório de estágio
    // -------------------------------------------------------------------------

    @GetMapping("/{offerId}/students/{studentId}/report")
    public ReportDetailsResponse reportDetails(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            Authentication authentication
    ) {
        return supervisorProfessorService.getReportDetailsForApi(
                offerId, studentId, resolveUser(authentication));
    }

    @PostMapping("/{offerId}/students/{studentId}/report/approve")
    public OfferStudentSummaryResponse approveReport(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            @Valid @RequestBody ApproveReportFormDTO form,
            Authentication authentication
    ) {
        return supervisorProfessorService.approveReportForApi(
                offerId, studentId, resolveUser(authentication), form);
    }

    @GetMapping("/{offerId}/students/{studentId}/report/download")
    public ResponseEntity<byte[]> downloadReport(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            Authentication authentication
    ) {
        User professor = resolveUser(authentication);
        OfferStudent os = supervisorProfessorService
                .getEnrollmentForSupervisor(offerId, studentId, professor);
        Report report = supervisorProfessorService.getReport(os);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(report.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + report.getFileName() + "\"")
                .body(report.getFileContent());
    }

    private User resolveUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário autenticado não encontrado."));
    }
}
