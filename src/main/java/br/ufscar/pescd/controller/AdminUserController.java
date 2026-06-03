package br.ufscar.pescd.controller;

import br.ufscar.pescd.dto.UserForm;
import br.ufscar.pescd.enums.UserRole;
import br.ufscar.pescd.service.UserService;
import jakarta.validation.Valid;
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
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("roles")
    public UserRole[] roles() {
        return new UserRole[]{UserRole.ADMIN, UserRole.SECRETARIO, UserRole.PROFESSOR};
    }

    @GetMapping
    public String list(Model model, Authentication authentication) {
        model.addAttribute("users", userService.findAll());
        model.addAttribute("loggedUsername", authentication.getName());
        return "admin/users/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("userForm", new UserForm());
        model.addAttribute("isEdit", false);
        return "admin/users/form";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("userForm") UserForm userForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (!userService.validateBusinessRules(userForm, bindingResult, false)) {
            model.addAttribute("isEdit", false);
            return "admin/users/form";
        }

        userService.create(userForm);
        redirectAttributes.addFlashAttribute("successMessage", "Usuário cadastrado com sucesso.");
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("userForm", userService.toForm(userService.findById(id)));
        model.addAttribute("isEdit", true);
        return "admin/users/form";
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("userForm") UserForm userForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        userForm.setId(id);

        if (!userService.validateBusinessRules(userForm, bindingResult, true)) {
            model.addAttribute("isEdit", true);
            return "admin/users/form";
        }

        userService.update(userForm);
        redirectAttributes.addFlashAttribute("successMessage", "Usuário atualizado com sucesso.");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.delete(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Usuário removido com sucesso.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/users";
    }
}
