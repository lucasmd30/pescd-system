package br.ufscar.pescd.controller;

import br.ufscar.pescd.dto.DocumentationForm;
import br.ufscar.pescd.dto.ReportForm;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/aluno/ofertas")
public class StudentOfferController {

    private final StudentOfferService studentOfferService;

    public StudentOfferController(StudentOfferService studentOfferService) {
        this.studentOfferService = studentOfferService;
    }


    @GetMapping
    public String listEnrollments(Authentication authentication, Model model) {
        model.addAttribute("enrollments",
                studentOfferService.findStudentEnrollments(authentication.getName()));
        return "aluno/offers/list";
    }

    @GetMapping("/{id}")
    public String enrollmentDetails(@PathVariable Long id, Authentication authentication, Model model) {
        OfferStudent enrollment = studentOfferService.getEnrollment(id, authentication.getName());
        model.addAttribute("enrollment", enrollment);
        model.addAttribute("workPlan", studentOfferService.getWorkPlan(enrollment));
        model.addAttribute("documentation", studentOfferService.getDocumentation(enrollment));
        model.addAttribute("report", studentOfferService.getReport(enrollment));
        model.addAttribute("logs", studentOfferService.getStatusLogs(enrollment));
        return "aluno/offers/details";
    }


    @GetMapping("/{id}/plano")
    public String workPlanForm(@PathVariable Long id, Authentication authentication, Model model) {
        OfferStudent enrollment = studentOfferService.getEnrollment(id, authentication.getName());
        model.addAttribute("enrollment", enrollment);
        model.addAttribute("professors", studentOfferService.listProfessors());
        if (!model.containsAttribute("workPlanForm")) {
            model.addAttribute("workPlanForm", new WorkPlanForm());
        }
        return "aluno/offers/plano";
    }

    @PostMapping("/{id}/plano")
    public String submitWorkPlan(
            @PathVariable Long id,
            @Valid @ModelAttribute("workPlanForm") WorkPlanForm workPlanForm,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return renderWorkPlanForm(id, authentication, model);
        }
        try {
            studentOfferService.submitWorkPlan(id, authentication.getName(), workPlanForm);
            redirectAttributes.addFlashAttribute("successMessage", "Plano de trabalho enviado com sucesso.");
            return "redirect:/aluno/ofertas/" + id;
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return renderWorkPlanForm(id, authentication, model);
        }
    }

    private String renderWorkPlanForm(Long id, Authentication authentication, Model model) {
        OfferStudent enrollment = studentOfferService.getEnrollment(id, authentication.getName());
        model.addAttribute("enrollment", enrollment);
        model.addAttribute("professors", studentOfferService.listProfessors());
        return "aluno/offers/plano";
    }


    @GetMapping("/{id}/documentacao")
    public String documentationForm(@PathVariable Long id, Authentication authentication, Model model) {
        OfferStudent enrollment = studentOfferService.getEnrollment(id, authentication.getName());
        model.addAttribute("enrollment", enrollment);
        if (!model.containsAttribute("documentationForm")) {
            model.addAttribute("documentationForm", new DocumentationForm());
        }
        return "aluno/offers/documentacao";
    }

    @PostMapping("/{id}/documentacao")
    public String submitDocumentation(
            @PathVariable Long id,
            @Valid @ModelAttribute("documentationForm") DocumentationForm documentationForm,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("enrollment",
                    studentOfferService.getEnrollment(id, authentication.getName()));
            return "aluno/offers/documentacao";
        }
        try {
            studentOfferService.submitDocumentation(id, authentication.getName(), documentationForm);
            redirectAttributes.addFlashAttribute("successMessage", "Documentação enviada com sucesso.");
            return "redirect:/aluno/ofertas/" + id;
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("enrollment",
                    studentOfferService.getEnrollment(id, authentication.getName()));
            return "aluno/offers/documentacao";
        }
    }


    @GetMapping("/{id}/relatorio")
    public String reportForm(@PathVariable Long id, Authentication authentication, Model model) {
        OfferStudent enrollment = studentOfferService.getEnrollment(id, authentication.getName());
        model.addAttribute("enrollment", enrollment);
        model.addAttribute("workPlan", studentOfferService.getWorkPlan(enrollment));
        model.addAttribute("logs", studentOfferService.getStatusLogs(enrollment));
        if (!model.containsAttribute("reportForm")) {
            model.addAttribute("reportForm", new ReportForm());
        }
        return "aluno/offers/relatorio";
    }

    @PostMapping("/{id}/relatorio")
    public String submitReport(
            @PathVariable Long id,
            @Valid @ModelAttribute("reportForm") ReportForm reportForm,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return renderReportForm(id, authentication, model);
        }
        try {
            studentOfferService.submitReport(id, authentication.getName(), reportForm);
            redirectAttributes.addFlashAttribute("successMessage", "Relatório final enviado com sucesso.");
            return "redirect:/aluno/ofertas/" + id;
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return renderReportForm(id, authentication, model);
        }
    }

    private String renderReportForm(Long id, Authentication authentication, Model model) {
        OfferStudent enrollment = studentOfferService.getEnrollment(id, authentication.getName());
        model.addAttribute("enrollment", enrollment);
        model.addAttribute("workPlan", studentOfferService.getWorkPlan(enrollment));
        model.addAttribute("logs", studentOfferService.getStatusLogs(enrollment));
        return "aluno/offers/relatorio";
    }


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
