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

/**
 * AL.01 a AL.04 - fluxo do aluno como REST API.
 *
 * AL.01 - visualiza suas ofertas e o status de cada uma.
 * AL.02 - envio do plano de trabalho (PDF, define supervisor).
 * AL.03 - envio da documentação comprobatória (PDF).
 * AL.04 - envio do relatório final (frequência 0-100, PDF).
 */
@RestController
@RequestMapping("/api/aluno/ofertas")
public class StudentOfferApiController {

    private final StudentOfferService studentOfferService;

    public StudentOfferApiController(StudentOfferService studentOfferService) {
        this.studentOfferService = studentOfferService;
    }

    // AL.01 - lista de ofertas do aluno com o status de cada inscrição.
    @GetMapping
    public List<StudentOfferSummaryResponse> listEnrollments(Authentication authentication) {
        return studentOfferService.listEnrollmentsForApi(authentication.getName());
    }

    // AL.01/AL.04 - detalhes de uma inscrição, com envios e histórico de status.
    @GetMapping("/{id}")
    public StudentEnrollmentDetailsResponse enrollmentDetails(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return studentOfferService.getEnrollmentDetailsForApi(id, authentication.getName());
    }

    // AL.02 - lista de professores para escolha do supervisor.
    @GetMapping("/professors")
    public List<UserSummaryResponse> listProfessors() {
        return studentOfferService.listProfessorsForApi();
    }

    // AL.02 - envio do plano de trabalho (multipart: campos + PDF).
    @PostMapping("/{id}/plano")
    public StudentEnrollmentDetailsResponse submitWorkPlan(
            @PathVariable Long id,
            @Valid @ModelAttribute WorkPlanForm form,
            Authentication authentication
    ) {
        return studentOfferService.submitWorkPlanForApi(id, authentication.getName(), form);
    }

    // AL.03 - envio da documentação comprobatória (multipart: campos + PDF).
    @PostMapping("/{id}/documentacao")
    public StudentEnrollmentDetailsResponse submitDocumentation(
            @PathVariable Long id,
            @Valid @ModelAttribute DocumentationForm form,
            Authentication authentication
    ) {
        return studentOfferService.submitDocumentationForApi(id, authentication.getName(), form);
    }

    // AL.04 - envio do relatório final (multipart: frequência + PDF).
    @PostMapping("/{id}/relatorio")
    public StudentEnrollmentDetailsResponse submitReport(
            @PathVariable Long id,
            @Valid @ModelAttribute ReportForm form,
            Authentication authentication
    ) {
        return studentOfferService.submitReportForApi(id, authentication.getName(), form);
    }

    // Downloads dos arquivos enviados (PDF inline).
    @GetMapping("/{id}/plano/arquivo")
    public ResponseEntity<byte[]> downloadWorkPlan(@PathVariable Long id, Authentication authentication) {
        OfferStudent enrollment = studentOfferService.getEnrollment(id, authentication.getName());
        WorkPlan workPlan = studentOfferService.getWorkPlan(enrollment);
        if (workPlan == null) {
            return ResponseEntity.notFound().build();
        }
        return pdfResponse(workPlan.getFileName(), workPlan.getFileContent());
    }

    @GetMapping("/{id}/documentacao/arquivo")
    public ResponseEntity<byte[]> downloadDocumentation(@PathVariable Long id, Authentication authentication) {
        OfferStudent enrollment = studentOfferService.getEnrollment(id, authentication.getName());
        Documentation documentation = studentOfferService.getDocumentation(enrollment);
        if (documentation == null) {
            return ResponseEntity.notFound().build();
        }
        return pdfResponse(documentation.getFileName(), documentation.getFileContent());
    }

    @GetMapping("/{id}/relatorio/arquivo")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long id, Authentication authentication) {
        OfferStudent enrollment = studentOfferService.getEnrollment(id, authentication.getName());
        Report report = studentOfferService.getReport(enrollment);
        if (report == null) {
            return ResponseEntity.notFound().build();
        }
        return pdfResponse(report.getFileName(), report.getFileContent());
    }

    private ResponseEntity<byte[]> pdfResponse(String fileName, byte[] content) {
        String safeName = fileName != null ? fileName : "arquivo.pdf";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeName + "\"")
                .body(content);
    }
}
