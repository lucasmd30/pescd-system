package br.ufscar.pescd.controller;

import br.ufscar.pescd.dto.OfferForm;
import br.ufscar.pescd.dto.StudentForm;
import br.ufscar.pescd.entity.Offer;
import br.ufscar.pescd.entity.OfferStudent;
import br.ufscar.pescd.service.SecretaryOfferService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/secretary/offers")
public class SecretaryOfferController {

    private final SecretaryOfferService secretaryOfferService;

    public SecretaryOfferController(SecretaryOfferService secretaryOfferService) {
        this.secretaryOfferService = secretaryOfferService;
    }

    @GetMapping
    public String listOffers(Model model) {
        List<Offer> offers = secretaryOfferService.listOffers();
        model.addAttribute("offers", offers);
        return "secretary/offers/list";
    }


    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("offerForm", new OfferForm());
        model.addAttribute("professors", secretaryOfferService.listProfessors());
        return "secretary/offers/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("offerForm") OfferForm offerForm,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (offerForm.getStartDate() != null
                && offerForm.getEndDate() != null
                && !offerForm.getEndDate().isAfter(offerForm.getStartDate())) {
            bindingResult.rejectValue("endDate", "invalid", "A data de fim deve ser depois da data de início.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("professors", secretaryOfferService.listProfessors());
            return "secretary/offers/form";
        }

        secretaryOfferService.createOffer(offerForm, authentication.getName());
        redirectAttributes.addFlashAttribute("successMessage", "Oferta criada com sucesso.");
        return "redirect:/secretary/offers";
    }


    @GetMapping("/{id}")
    public String offerDetails(@PathVariable Long id, Model model) {
        Offer offer = secretaryOfferService.getOffer(id);
        List<OfferStudent> students = secretaryOfferService.getOfferStudents(id);
        model.addAttribute("offer", offer);
        model.addAttribute("students", students);
        return "secretary/offers/details";
    }


    @GetMapping("/{id}/students")
    public String manageStudents(@PathVariable Long id, Model model) {
        Offer offer = secretaryOfferService.getOffer(id);
        model.addAttribute("offer", offer);
        model.addAttribute("students", secretaryOfferService.getOfferStudents(id));
        if (!model.containsAttribute("studentForm")) {
            model.addAttribute("studentForm", new StudentForm());
        }
        return "secretary/offers/students";
    }


    @PostMapping("/{id}/students")
    public String addStudent(
            @PathVariable Long id,
            @Valid @ModelAttribute("studentForm") StudentForm studentForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            Offer offer = secretaryOfferService.getOffer(id);
            model.addAttribute("offer", offer);
            model.addAttribute("students", secretaryOfferService.getOfferStudents(id));
            return "secretary/offers/students";
        }

        try {
            secretaryOfferService.addStudent(id, studentForm);
            redirectAttributes.addFlashAttribute("successMessage", "Aluno adicionado à oferta.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/secretary/offers/" + id + "/students";
    }

    @PostMapping("/{id}/students/import")
    public String importStudents(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {
        try {
            int enrolled = secretaryOfferService.importStudentsFromCsv(id, file);
            redirectAttributes.addFlashAttribute("successMessage",
                    enrolled + " aluno(s) adicionado(s) à oferta a partir do CSV.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/secretary/offers/" + id + "/students";
    }

    @GetMapping("/{offerId}/students/{offerStudentId}")
    public String studentDetails(
            @PathVariable Long offerId,
            @PathVariable Long offerStudentId,
            Model model
    ) {

        OfferStudent offerStudent =
                secretaryOfferService.getOfferStudent(offerStudentId);

        model.addAttribute("offerStudent", offerStudent);

        model.addAttribute(
                "logs",
                secretaryOfferService.getStudentLogs(offerStudentId)
        );

        return "secretary/offers/student-details";
    }

    @PostMapping("/{id}/close")
    public String closeOffer(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        try {
            secretaryOfferService.closeOffer(id);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Oferta encerrada com sucesso."
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return "redirect:/secretary/offers";
    }
}
