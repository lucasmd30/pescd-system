package br.ufscar.pescd.controller;

import br.ufscar.pescd.dto.AnalyzeDocumentationFormDTO;
import br.ufscar.pescd.dto.ConcludeReportFormDTO;
import br.ufscar.pescd.dto.ResponsavelDashboardDTO;
import br.ufscar.pescd.entity.*;
import br.ufscar.pescd.enums.GradeOption;
import br.ufscar.pescd.repository.UserRepository;
import br.ufscar.pescd.service.ProfessorResponsavelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import br.ufscar.pescd.dto.CloseOfferFormDTO;
import br.ufscar.pescd.entity.Offer;

import java.util.List;

@Controller
@RequestMapping("/professor/responsavel")
public class ProfessorResponsavelController {

    private final ProfessorResponsavelService professorResponsavelService;
    private final UserRepository userRepository;

    public ProfessorResponsavelController(
            ProfessorResponsavelService professorResponsavelService,
            UserRepository userRepository
    ) {
        this.professorResponsavelService = professorResponsavelService;
        this.userRepository = userRepository;
    }

    // -------------------------------------------------------------------------
    // Dashboard
    // -------------------------------------------------------------------------

    @GetMapping("/ofertas/{offerId}")
    public String offerDetails(
            @PathVariable Long offerId,
            Authentication authentication,
            Model model
    ) {

        User professor = resolveUser(authentication);

        Offer offer = professorResponsavelService.getOffer(offerId);

        if (!offer.getResponsibleProfessor().getId().equals(professor.getId())) {
            throw new IllegalArgumentException(
                    "Você não é responsável por esta oferta."
            );
        }

        model.addAttribute("offer", offer);

        model.addAttribute(
                "students",
                professorResponsavelService.getOfferStudents(offerId)
        );

        return "professor/responsavel/offer-details";
    }



    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {

        User professor = resolveUser(authentication);

        List<ResponsavelDashboardDTO> dashboard =
                professorResponsavelService.getDashboard(professor);

        model.addAttribute("dashboard", dashboard);
        model.addAttribute("professor", professor);

        return "professor/responsavel/dashboard";
    }

    // -------------------------------------------------------------------------
    // PR.01 — Concluir Relatório
    // -------------------------------------------------------------------------

    @GetMapping("/ofertas/{offerId}/alunos/{studentId}/concluir-relatorio")
    public String concludeReportForm(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            Model model,
            Authentication authentication
    ) {
        User professor = resolveUser(authentication);

        OfferStudent os = professorResponsavelService
                .getEnrollmentForResponsavel(offerId, studentId, professor);

        WorkPlan plan   = professorResponsavelService.getWorkPlan(os);
        Report   report = professorResponsavelService.getReport(os);

        ConcludeReportFormDTO form = new ConcludeReportFormDTO();
        form.setFrequencia(report.getSupervisorFrequencia());   // pré-preenche com valor do supervisor
        form.setNota(report.getSupervisorNotaSugestao());       // pré-preenche com sugestão do supervisor

        model.addAttribute("offerStudent", os);
        model.addAttribute("workPlan", plan);
        model.addAttribute("report", report);
        model.addAttribute("statusLogs", professorResponsavelService.getStatusLogs(os));
        model.addAttribute("gradeOptions", GradeOption.values());
        model.addAttribute("form", form);

        return "professor/responsavel/conclude-report";
    }



    @PostMapping("/ofertas/{offerId}/alunos/{studentId}/concluir-relatorio")
    public String concludeReport(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            @Valid @ModelAttribute("form") ConcludeReportFormDTO form,
            BindingResult bindingResult,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        User professor = resolveUser(authentication);

        if (bindingResult.hasErrors()) {
            OfferStudent os = professorResponsavelService
                    .getEnrollmentForResponsavel(offerId, studentId, professor);
            model.addAttribute("offerStudent", os);
            model.addAttribute("workPlan", professorResponsavelService.getWorkPlan(os));
            model.addAttribute("report", professorResponsavelService.getReport(os));
            model.addAttribute("statusLogs", professorResponsavelService.getStatusLogs(os));
            model.addAttribute("gradeOptions", GradeOption.values());
            return "professor/responsavel/conclude-report";
        }

        professorResponsavelService.concludeReport(offerId, studentId, professor, form);

        redirectAttributes.addFlashAttribute("successMessage",
                "Relatório concluído com sucesso.");

        return "redirect:/professor/responsavel/dashboard";
    }

    // -------------------------------------------------------------------------
    // PR.02 — Analisar Documentação
    // -------------------------------------------------------------------------

    @GetMapping("/ofertas/{offerId}/alunos/{studentId}/analisar-documentacao")
    public String analyzeDocumentationForm(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            Model model,
            Authentication authentication
    ) {
        User professor = resolveUser(authentication);

        OfferStudent os = professorResponsavelService
                .getEnrollmentForResponsavel(offerId, studentId, professor);

        Documentation doc = professorResponsavelService.getDocumentation(os);

        model.addAttribute("offerStudent", os);
        model.addAttribute("documentation", doc);
        model.addAttribute("statusLogs", professorResponsavelService.getStatusLogs(os));
        model.addAttribute("gradeOptions", GradeOption.values());
        model.addAttribute("form", new AnalyzeDocumentationFormDTO());

        return "professor/responsavel/analyze-documentation";
    }

    @PostMapping("/ofertas/{offerId}/alunos/{studentId}/analisar-documentacao")
    public String analyzeDocumentation(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            @Valid @ModelAttribute("form") AnalyzeDocumentationFormDTO form,
            BindingResult bindingResult,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        User professor = resolveUser(authentication);

        if (bindingResult.hasErrors()) {
            OfferStudent os = professorResponsavelService
                    .getEnrollmentForResponsavel(offerId, studentId, professor);
            model.addAttribute("offerStudent", os);
            model.addAttribute("documentation", professorResponsavelService.getDocumentation(os));
            model.addAttribute("statusLogs", professorResponsavelService.getStatusLogs(os));
            model.addAttribute("gradeOptions", GradeOption.values());
            return "professor/responsavel/analyze-documentation";
        }

        professorResponsavelService.analyzeDocumentation(offerId, studentId, professor, form);

        redirectAttributes.addFlashAttribute("successMessage",
                "Documentação analisada com sucesso.");

        return "redirect:/professor/responsavel/dashboard";
    }

    @GetMapping("/ofertas/{offerId}/encerrar")
    public String closeOfferForm(
            @PathVariable Long offerId,
            Model model,
            Authentication authentication
    ) {

        User professor = resolveUser(authentication);

        Offer offer = professorResponsavelService.getOffer(offerId);

        model.addAttribute("offer", offer);
        model.addAttribute("form", new CloseOfferFormDTO());

        return "professor/responsavel/close-offer";
    }

    @PostMapping("/ofertas/{offerId}/encerrar")
    public String closeOffer(
            @PathVariable Long offerId,
            @Valid @ModelAttribute("form") CloseOfferFormDTO form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        User professor = resolveUser(authentication);

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "offer",
                    professorResponsavelService.getOffer(offerId)
            );

            return "professor/responsavel/close-offer";
        }

        try {

            professorResponsavelService.closeOffer(
                    offerId,
                    professor,
                    form.getLessonsLearned()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Oferta enviada para encerramento."
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );

        }

        return "redirect:/professor/responsavel/dashboard";
    }

    // -------------------------------------------------------------------------
    // Downloads
    // -------------------------------------------------------------------------

    @GetMapping("/ofertas/{offerId}/alunos/{studentId}/plano/download")
    @ResponseBody
    public ResponseEntity<byte[]> downloadPlan(
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
                .body(plan.getFileContent());
    }

    @GetMapping("/ofertas/{offerId}/alunos/{studentId}/relatorio/download")
    @ResponseBody
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
                .body(report.getFileContent());
    }

    @GetMapping("/ofertas/{offerId}/alunos/{studentId}/documentacao/download")
    @ResponseBody
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
                .body(doc.getFileContent());
    }

    // -------------------------------------------------------------------------
    // Utilitário
    // -------------------------------------------------------------------------

    private User resolveUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }
}
