package br.ufscar.pescd.controller;

import br.ufscar.pescd.security.RoleRedirectService;
import br.ufscar.pescd.service.OfferService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    private final OfferService offerService;
    private final RoleRedirectService roleRedirectService;

    public AuthController(
            OfferService offerService,
            RoleRedirectService roleRedirectService
    ) {
        this.offerService = offerService;
        this.roleRedirectService = roleRedirectService;
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

        return "redirect:" + roleRedirectService.resolveRedirectPath(authentication.getAuthorities());
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
