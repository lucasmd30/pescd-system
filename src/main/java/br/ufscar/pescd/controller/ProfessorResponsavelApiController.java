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
    public List<OfferDetailsResponse> listResponsibleOffers(Authentication authentication) {
        return professorResponsavelService.getDashboardForApi(resolveUser(authentication));
    }

    @GetMapping("/{offerId}")
    public OfferDetailsResponse offerDetails(
            @PathVariable Long offerId,
            Authentication authentication
    ) {
        return professorResponsavelService.getOfferDetailsForApi(offerId, resolveUser(authentication));
    }

    @GetMapping("/{offerId}/students")
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
    public ResponsibleCloseOfferSummaryResponse closeSummary(
            @PathVariable Long offerId,
            Authentication authentication
    ) {
        return professorResponsavelService.getCloseOfferSummaryForApi(offerId, resolveUser(authentication));
    }

    @PostMapping("/{offerId}/close")
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
    public OfferStudentDetailsResponse enrollmentDetails(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            Authentication authentication
    ) {
        return professorResponsavelService.getEnrollmentDetailsForApi(
                offerId, studentId, resolveUser(authentication));
    }

    @GetMapping("/{offerId}/students/{studentId}/work-plan/download")
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

    // -------------------------------------------------------------------------
    // PR.01 — Conclusão do relatório de estágio de um aluno
    // -------------------------------------------------------------------------

    @GetMapping("/{offerId}/students/{studentId}/report")
    public ReportDetailsResponse reportDetails(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            Authentication authentication
    ) {
        return professorResponsavelService.getReportDetailsForApi(
                offerId, studentId, resolveUser(authentication));
    }

    @PostMapping("/{offerId}/students/{studentId}/report/conclude")
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

    // -------------------------------------------------------------------------
    // PR.02 — Análise e aprovação de documentação de aula
    // -------------------------------------------------------------------------

    @GetMapping("/{offerId}/students/{studentId}/documentation")
    public DocumentationDetailsResponse documentationDetails(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            Authentication authentication
    ) {
        return professorResponsavelService.getDocumentationDetailsForApi(
                offerId, studentId, resolveUser(authentication));
    }

    @PostMapping("/{offerId}/students/{studentId}/documentation/analyze")
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
