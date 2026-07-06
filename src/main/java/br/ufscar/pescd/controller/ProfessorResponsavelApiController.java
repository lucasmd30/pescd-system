package br.ufscar.pescd.controller;

import br.ufscar.pescd.dto.AnalyzeDocumentationFormDTO;
import br.ufscar.pescd.dto.CloseOfferFormDTO;
import br.ufscar.pescd.dto.ConcludeReportFormDTO;
import br.ufscar.pescd.dto.DocumentationDetailsResponse;
import br.ufscar.pescd.dto.OfferDetailsResponse;
import br.ufscar.pescd.dto.OfferStudentDetailsResponse;
import br.ufscar.pescd.dto.OfferStudentSummaryResponse;
import br.ufscar.pescd.dto.OfferSummaryResponse;
import br.ufscar.pescd.dto.ReportDetailsResponse;
import br.ufscar.pescd.dto.ResponsibleCloseOfferSummaryResponse;
import br.ufscar.pescd.entity.Documentation;
import br.ufscar.pescd.entity.OfferStudent;
import br.ufscar.pescd.entity.Report;
import br.ufscar.pescd.entity.User;
import br.ufscar.pescd.entity.WorkPlan;
import br.ufscar.pescd.repository.UserRepository;
import br.ufscar.pescd.service.ProfessorResponsavelService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/professor/responsavel/offers")
@Tag(name = "Professor responsável", description = "Operações REST do professor responsável.")
@SecurityRequirement(name = "sessionAuth")
public class ProfessorResponsavelApiController {

    private final ProfessorResponsavelService professorResponsavelService;
    private final UserRepository userRepository;
    private final SeaweedFsStorageService storageService;

    public ProfessorResponsavelApiController(
            ProfessorResponsavelService professorResponsavelService,
            UserRepository userRepository,
            SeaweedFsStorageService storageService
    ) {
        this.professorResponsavelService = professorResponsavelService;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    @GetMapping
    @Operation(summary = "Listar ofertas do responsável", description = "Retorna as ofertas em que o professor autenticado é responsável.")
    public List<OfferDetailsResponse> listResponsibleOffers(Authentication authentication) {
        return professorResponsavelService.getDashboardForApi(resolveUser(authentication));
    }

    @GetMapping("/{offerId}")
    @Operation(summary = "Detalhar oferta", description = "Retorna os detalhes completos de uma oferta do professor responsável.")
    public OfferDetailsResponse offerDetails(
            @PathVariable Long offerId,
            Authentication authentication
    ) {
        return professorResponsavelService.getOfferDetailsForApi(offerId, resolveUser(authentication));
    }

    @GetMapping("/{offerId}/students")
    @Operation(summary = "Buscar alunos da oferta", description = "Lista os alunos da oferta, com filtro opcional por nome.")
    public List<OfferStudentSummaryResponse> searchStudents(
            @PathVariable Long offerId,
            @RequestParam(required = false) String name,
            Authentication authentication
    ) {
        return professorResponsavelService.searchStudentsByNameForApi(
                offerId,
                resolveUser(authentication),
                name
        );
    }

    @GetMapping("/{offerId}/close")
    @Operation(summary = "Resumo para encerramento", description = "Retorna o resumo necessário para o encerramento da oferta pelo professor responsável.")
    public ResponsibleCloseOfferSummaryResponse closeSummary(
            @PathVariable Long offerId,
            Authentication authentication
    ) {
        return professorResponsavelService.getCloseOfferSummaryForApi(offerId, resolveUser(authentication));
    }

    @PostMapping("/{offerId}/close")
    @Operation(summary = "Encerrar oferta", description = "Encaminha as lições aprendidas e conclui o encerramento da oferta.")
    public OfferSummaryResponse closeOffer(
            @PathVariable Long offerId,
            @Valid @RequestBody CloseOfferFormDTO form,
            Authentication authentication
    ) {
        return professorResponsavelService.closeOfferForApi(
                offerId,
                resolveUser(authentication),
                form.getLessonsLearned()
        );
    }

    @GetMapping("/{offerId}/students/{studentId}")
    @Operation(summary = "Detalhar inscrição do aluno", description = "Retorna os detalhes da inscrição do aluno em uma oferta do professor responsável.")
    public OfferStudentDetailsResponse enrollmentDetails(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            Authentication authentication
    ) {
        return professorResponsavelService.getEnrollmentDetailsForApi(
                offerId, studentId, resolveUser(authentication));
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
        OfferStudent os = professorResponsavelService
                .getEnrollmentForResponsavel(offerId, studentId, professor);
        WorkPlan plan = professorResponsavelService.getWorkPlan(os);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(plan.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + plan.getFileName() + "\"")
                .body(storageService.read(plan.getFileFid()));
    }

    @GetMapping("/{offerId}/students/{studentId}/report")
    @Operation(summary = "Detalhar relatório", description = "Retorna os detalhes do relatório do aluno.")
    public ReportDetailsResponse reportDetails(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            Authentication authentication
    ) {
        return professorResponsavelService.getReportDetailsForApi(
                offerId, studentId, resolveUser(authentication));
    }

    @PostMapping("/{offerId}/students/{studentId}/report/conclude")
    @Operation(summary = "Concluir relatório", description = "Registra o parecer final, a frequência final e a nota do aluno.")
    public OfferStudentSummaryResponse concludeReport(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            @Valid @RequestBody ConcludeReportFormDTO form,
            Authentication authentication
    ) {
        return professorResponsavelService.concludeReportForApi(
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
        OfferStudent os = professorResponsavelService
                .getEnrollmentForResponsavel(offerId, studentId, professor);
        Report report = professorResponsavelService.getReport(os);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(report.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + report.getFileName() + "\"")
                .body(storageService.read(report.getFileFid()));
    }

    @GetMapping("/{offerId}/students/{studentId}/documentation")
    @Operation(summary = "Detalhar documentação", description = "Retorna os detalhes da documentação comprobatória enviada pelo aluno.")
    public DocumentationDetailsResponse documentationDetails(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            Authentication authentication
    ) {
        return professorResponsavelService.getDocumentationDetailsForApi(
                offerId, studentId, resolveUser(authentication));
    }

    @PostMapping("/{offerId}/students/{studentId}/documentation/analyze")
    @Operation(summary = "Analisar documentação", description = "Registra a análise, frequência e nota final da documentação enviada pelo aluno.")
    public OfferStudentSummaryResponse analyzeDocumentation(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            @Valid @RequestBody AnalyzeDocumentationFormDTO form,
            Authentication authentication
    ) {
        return professorResponsavelService.analyzeDocumentationForApi(
                offerId, studentId, resolveUser(authentication), form);
    }

    @GetMapping("/{offerId}/students/{studentId}/documentation/download")
    @Operation(summary = "Baixar documentação", description = "Retorna o PDF da documentação comprobatória do aluno.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo retornado com sucesso.",
                    content = @Content(mediaType = "application/pdf",
                            schema = @Schema(type = "string", format = "binary")))
    })
    public ResponseEntity<byte[]> downloadDocumentation(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            Authentication authentication
    ) {
        User professor = resolveUser(authentication);
        OfferStudent os = professorResponsavelService
                .getEnrollmentForResponsavel(offerId, studentId, professor);
        Documentation doc = professorResponsavelService.getDocumentation(os);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(doc.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + doc.getFileName() + "\"")
                .body(storageService.read(doc.getFileFid()));
    }

    private User resolveUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário autenticado não encontrado."));
    }
}
