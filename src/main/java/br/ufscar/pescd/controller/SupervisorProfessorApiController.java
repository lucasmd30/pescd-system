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
import br.ufscar.pescd.storage.SeaweedFsStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Professor supervisor", description = "Operações REST do professor supervisor.")
@SecurityRequirement(name = "sessionAuth")
public class SupervisorProfessorApiController {

    private final SupervisorProfessorService supervisorProfessorService;
    private final UserRepository userRepository;
    private final SeaweedFsStorageService storageService;

    public SupervisorProfessorApiController(
            SupervisorProfessorService supervisorProfessorService,
            UserRepository userRepository,
            SeaweedFsStorageService storageService
    ) {
        this.supervisorProfessorService = supervisorProfessorService;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    @GetMapping
    @Operation(summary = "Listar ofertas supervisionadas", description = "Retorna as ofertas em que o professor atua como supervisor.")
    public List<OfferDetailsResponse> listSupervisedOffers(Authentication authentication) {
        return supervisorProfessorService.getDashboardForApi(resolveUser(authentication));
    }

    @GetMapping("/{offerId}/students/{studentId}")
    @Operation(summary = "Detalhar inscrição supervisionada", description = "Retorna os detalhes da inscrição de um aluno em uma oferta supervisionada.")
    public OfferStudentDetailsResponse enrollmentDetails(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            Authentication authentication
    ) {
        return supervisorProfessorService.getEnrollmentDetailsForApi(
                offerId, studentId, resolveUser(authentication));
    }

    @GetMapping("/{offerId}/students/{studentId}/work-plan")
    @Operation(summary = "Detalhar plano de trabalho", description = "Retorna os detalhes do plano de trabalho enviado pelo aluno.")
    public WorkPlanDetailsResponse workPlanDetails(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            Authentication authentication
    ) {
        return supervisorProfessorService.getWorkPlanDetailsForApi(
                offerId, studentId, resolveUser(authentication));
    }

    @PostMapping("/{offerId}/students/{studentId}/work-plan/approve")
    @Operation(summary = "Aprovar plano de trabalho", description = "Registra o parecer do professor supervisor para o plano de trabalho.")
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
    @Operation(summary = "Baixar plano de trabalho", description = "Retorna o PDF do plano de trabalho do aluno.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo retornado com sucesso.",
                    content = @Content(mediaType = "application/pdf",
                            schema = @Schema(type = "string", format = "binary")))
    })
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
                .body(storageService.read(plan.getFileFid()));
    }

    @GetMapping("/{offerId}/students/{studentId}/report")
    @Operation(summary = "Detalhar relatório", description = "Retorna os detalhes do relatório enviado pelo aluno.")
    public ReportDetailsResponse reportDetails(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            Authentication authentication
    ) {
        return supervisorProfessorService.getReportDetailsForApi(
                offerId, studentId, resolveUser(authentication));
    }

    @PostMapping("/{offerId}/students/{studentId}/report/approve")
    @Operation(summary = "Aprovar relatório", description = "Registra o parecer do professor supervisor para o relatório do aluno.")
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
    @Operation(summary = "Baixar relatório", description = "Retorna o PDF do relatório do aluno.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo retornado com sucesso.",
                    content = @Content(mediaType = "application/pdf",
                            schema = @Schema(type = "string", format = "binary")))
    })
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
                .body(storageService.read(report.getFileFid()));
    }

    private User resolveUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário autenticado não encontrado."));
    }
}
