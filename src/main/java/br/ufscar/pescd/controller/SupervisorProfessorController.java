package br.ufscar.pescd.controller;

import br.ufscar.pescd.dto.ApprovePlanFormDTO;
import br.ufscar.pescd.dto.ApproveReportFormDTO;
import br.ufscar.pescd.dto.SupervisorDashboardDTO;
import br.ufscar.pescd.entity.*;
import br.ufscar.pescd.enums.GradeOption;
import br.ufscar.pescd.repository.UserRepository;
import br.ufscar.pescd.service.SupervisorProfessorService;
import br.ufscar.pescd.storage.SeaweedFsStorageService;
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

import java.util.List;

@Controller
@RequestMapping("/professor/supervisor")
public class SupervisorProfessorController {

    private final SupervisorProfessorService supervisorProfessorService;
    private final UserRepository userRepository;
    private final SeaweedFsStorageService storageService;

    public SupervisorProfessorController(
            SupervisorProfessorService supervisorProfessorService,
            UserRepository userRepository,
            SeaweedFsStorageService storageService
    ) {
        this.supervisorProfessorService = supervisorProfessorService;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    // -------------------------------------------------------------------------
    // PS.01 — Dashboard
    // -------------------------------------------------------------------------

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {

        User professor = resolveUser(authentication);

        List<SupervisorDashboardDTO> dashboard =
                supervisorProfessorService.getDashboard(professor);

        model.addAttribute("dashboard", dashboard);
        model.addAttribute("professor", professor);

        return "professor/supervisor/dashboard";
    }

    // -------------------------------------------------------------------------
    // PS.02 — Aprovar Plano de Trabalho
    // -------------------------------------------------------------------------

    @GetMapping("/ofertas/{offerId}/alunos/{studentId}/aprovar-plano")
    public String approvePlanForm(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            Model model,
            Authentication authentication
    ) {
        User professor = resolveUser(authentication);

        OfferStudent os = supervisorProfessorService
                .getEnrollmentForSupervisor(offerId, studentId, professor);

        WorkPlan plan = supervisorProfessorService.getWorkPlan(os);

        model.addAttribute("offerStudent", os);
        model.addAttribute("workPlan", plan);
        model.addAttribute("form", new ApprovePlanFormDTO());

        return "professor/supervisor/approve-plan";
    }

    @PostMapping("/ofertas/{offerId}/alunos/{studentId}/aprovar-plano")
    public String approvePlan(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            @Valid @ModelAttribute("form") ApprovePlanFormDTO form,
            BindingResult bindingResult,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        User professor = resolveUser(authentication);

        if (bindingResult.hasErrors()) {
            OfferStudent os = supervisorProfessorService
                    .getEnrollmentForSupervisor(offerId, studentId, professor);
            WorkPlan plan = supervisorProfessorService.getWorkPlan(os);
            model.addAttribute("offerStudent", os);
            model.addAttribute("workPlan", plan);
            return "professor/supervisor/approve-plan";
        }

        supervisorProfessorService.approvePlan(offerId, studentId, professor, form.getParecer());

        redirectAttributes.addFlashAttribute("successMessage",
                "Plano de trabalho aprovado com sucesso.");

        return "redirect:/professor/supervisor/dashboard";
    }

    // -------------------------------------------------------------------------
    // Download do PDF do plano (usado nas telas PS.02 e PS.03)
    // -------------------------------------------------------------------------

    @GetMapping("/ofertas/{offerId}/alunos/{studentId}/plano/download")
    @ResponseBody
    public ResponseEntity<byte[]> downloadPlan(
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

    // -------------------------------------------------------------------------
    // PS.03 — Aprovar Relatório
    // -------------------------------------------------------------------------

    @GetMapping("/ofertas/{offerId}/alunos/{studentId}/aprovar-relatorio")
    public String approveReportForm(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            Model model,
            Authentication authentication
    ) {
        User professor = resolveUser(authentication);

        OfferStudent os = supervisorProfessorService
                .getEnrollmentForSupervisor(offerId, studentId, professor);

        WorkPlan plan = supervisorProfessorService.getWorkPlan(os);
        Report report  = supervisorProfessorService.getReport(os);

        ApproveReportFormDTO form = new ApproveReportFormDTO();
        form.setFrequencia(report.getFrequency());   // pré-preenchido com valor do aluno

        model.addAttribute("offerStudent", os);
        model.addAttribute("workPlan", plan);
        model.addAttribute("report", report);
        model.addAttribute("statusLogs", supervisorProfessorService.getStatusLogs(os));
        model.addAttribute("gradeOptions", GradeOption.values());
        model.addAttribute("form", form);

        return "professor/supervisor/approve-report";
    }

    @PostMapping("/ofertas/{offerId}/alunos/{studentId}/aprovar-relatorio")
    public String approveReport(
            @PathVariable Long offerId,
            @PathVariable Long studentId,
            @Valid @ModelAttribute("form") ApproveReportFormDTO form,
            BindingResult bindingResult,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        User professor = resolveUser(authentication);

        if (bindingResult.hasErrors()) {
            OfferStudent os = supervisorProfessorService
                    .getEnrollmentForSupervisor(offerId, studentId, professor);
            model.addAttribute("offerStudent", os);
            model.addAttribute("workPlan", supervisorProfessorService.getWorkPlan(os));
            model.addAttribute("report", supervisorProfessorService.getReport(os));
            model.addAttribute("statusLogs", supervisorProfessorService.getStatusLogs(os));
            model.addAttribute("gradeOptions", GradeOption.values());
            return "professor/supervisor/approve-report";
        }

        supervisorProfessorService.approveReport(offerId, studentId, professor, form);

        redirectAttributes.addFlashAttribute("successMessage",
                "Relatório aprovado com sucesso.");

        return "redirect:/professor/supervisor/dashboard";
    }

    // -------------------------------------------------------------------------
    // Download do PDF do relatório (PS.03)
    // -------------------------------------------------------------------------

    @GetMapping("/ofertas/{offerId}/alunos/{studentId}/relatorio/download")
    @ResponseBody
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

    // -------------------------------------------------------------------------
    // Utilitário
    // -------------------------------------------------------------------------

    private User resolveUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
    }
}
