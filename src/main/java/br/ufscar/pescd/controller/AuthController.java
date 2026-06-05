package br.ufscar.pescd.controller;

import br.ufscar.pescd.service.OfferService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    private final OfferService offerService;

    public AuthController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("offers", offerService.findPublicOffers());
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return "redirect:/admin/users";
        }

        boolean isSecretario = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_SECRETARIO"));
        if (isSecretario) {
            return "redirect:/dashboard/secretario";
        }

        boolean isProfessor = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_PROFESSOR"));
        if (isProfessor) {
            return "redirect:/dashboard/professor";
        }

        return "redirect:/aluno/ofertas";
    }

    @GetMapping("/dashboard/secretario")
    public String secretarioDashboard() {
        return "dashboard";
    }

    @GetMapping("/dashboard/professor")
    public String professorDashboard() {
        return "dashboard";
    }
}
