package br.ufscar.pescd.controller;

import br.ufscar.pescd.dto.DocumentationForm;
import br.ufscar.pescd.dto.ReportForm;
import br.ufscar.pescd.dto.StudentEnrollmentDetailsResponse;
import br.ufscar.pescd.dto.StudentOfferSummaryResponse;
import br.ufscar.pescd.dto.UserSummaryResponse;
import br.ufscar.pescd.dto.WorkPlanForm;
import br.ufscar.pescd.entity.Documentation;
import br.ufscar.pescd.entity.OfferStudent;
import br.ufscar.pescd.entity.Report;
import br.ufscar.pescd.entity.WorkPlan;
import br.ufscar.pescd.service.StudentOfferService;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/aluno/ofertas")
@Tag(name = "Aluno", description = "Fluxo REST do aluno para ofertas, plano, documentação e relatório.")
@SecurityRequirement(name = "sessionAuth")
public class StudentOfferApiController {

    private final StudentOfferService studentOfferService;
    private final SeaweedFsStorageService storageService;

    public StudentOfferApiController(
            StudentOfferService studentOfferService,
            SeaweedFsStorageService storageService
    ) {
        this.studentOfferService = studentOfferService;
        this.storageService = storageService;
    }

    @GetMapping
    @Operation(summary = "Listar inscrições do aluno", description = "Retorna as ofertas em que o aluno autenticado está inscrito.")
    public List<StudentOfferSummaryResponse> listEnrollments(Authentication authentication) {
        return studentOfferService.listEnrollmentsForApi(authentication.getName());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhar inscrição", description = "Retorna os detalhes de uma inscrição específica do aluno.")
    public StudentEnrollmentDetailsResponse enrollmentDetails(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return studentOfferService.getEnrollmentDetailsForApi(id, authentication.getName());
    }

    @GetMapping("/professors")
    @Operation(summary = "Listar professores", description = "Retorna os professores disponíveis para seleção como supervisores.")
    public List<UserSummaryResponse> listProfessors() {
        return studentOfferService.listProfessorsForApi();
    }

    @PostMapping("/{id}/plano")
    @Operation(summary = "Enviar plano de trabalho", description = "Realiza o envio multipart do plano de trabalho em PDF e define o professor supervisor.")
    public StudentEnrollmentDetailsResponse submitWorkPlan(
            @PathVariable Long id,
            @Valid @ModelAttribute WorkPlanForm form,
            Authentication authentication
    ) {
        return studentOfferService.submitWorkPlanForApi(id, authentication.getName(), form);
    }

    @PostMapping("/{id}/documentacao")
    @Operation(summary = "Enviar documentação", description = "Realiza o envio multipart da documentação comprobatória em PDF.")
    public StudentEnrollmentDetailsResponse submitDocumentation(
            @PathVariable Long id,
            @Valid @ModelAttribute DocumentationForm form,
            Authentication authentication
    ) {
        return studentOfferService.submitDocumentationForApi(id, authentication.getName(), form);
    }

    @PostMapping("/{id}/relatorio")
    @Operation(summary = "Enviar relatório final", description = "Realiza o envio multipart do relatório final do aluno em PDF com indicador de frequência.")
    public StudentEnrollmentDetailsResponse submitReport(
            @PathVariable Long id,
            @Valid @ModelAttribute ReportForm form,
            Authentication authentication
    ) {
        return studentOfferService.submitReportForApi(id, authentication.getName(), form);
    }

    @GetMapping("/{id}/plano/arquivo")
    @Operation(summary = "Baixar plano de trabalho", description = "Retorna o PDF do plano de trabalho associado à inscrição.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo retornado com sucesso.",
                    content = @Content(mediaType = "application/pdf",
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Plano não encontrado.")
    })
    public ResponseEntity<byte[]> downloadWorkPlan(@PathVariable Long id, Authentication authentication) {
        OfferStudent enrollment = studentOfferService.getEnrollment(id, authentication.getName());
        WorkPlan workPlan = studentOfferService.getWorkPlan(enrollment);
        if (workPlan == null) {
            return ResponseEntity.notFound().build();
        }
        return pdfResponse(workPlan.getFileName(), storageService.read(workPlan.getFileFid()));
    }

    @GetMapping("/{id}/documentacao/arquivo")
    @Operation(summary = "Baixar documentação", description = "Retorna o PDF da documentação comprobatória associada à inscrição.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo retornado com sucesso.",
                    content = @Content(mediaType = "application/pdf",
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Documentação não encontrada.")
    })
    public ResponseEntity<byte[]> downloadDocumentation(@PathVariable Long id, Authentication authentication) {
        OfferStudent enrollment = studentOfferService.getEnrollment(id, authentication.getName());
        Documentation documentation = studentOfferService.getDocumentation(enrollment);
        if (documentation == null) {
            return ResponseEntity.notFound().build();
        }
        return pdfResponse(documentation.getFileName(), storageService.read(documentation.getFileFid()));
    }

    @GetMapping("/{id}/relatorio/arquivo")
    @Operation(summary = "Baixar relatório", description = "Retorna o PDF do relatório final associado à inscrição.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Arquivo retornado com sucesso.",
                    content = @Content(mediaType = "application/pdf",
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Relatório não encontrado.")
    })
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long id, Authentication authentication) {
        OfferStudent enrollment = studentOfferService.getEnrollment(id, authentication.getName());
        Report report = studentOfferService.getReport(enrollment);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        return pdfResponse(report.getFileName(), storageService.read(report.getFileFid()));
    }

    private ResponseEntity<byte[]> pdfResponse(String fileName, byte[] content) {
        String safeName = fileName != null ? fileName : "arquivo.pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeName + "\"")
                .body(content);
    }
}
