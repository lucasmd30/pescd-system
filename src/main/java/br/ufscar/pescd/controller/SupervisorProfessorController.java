package br.ufscar.pescd.controller;

import br.ufscar.pescd.dto.SupervisorDashboardDTO;
import br.ufscar.pescd.entity.User;
import br.ufscar.pescd.repository.UserRepository;
import br.ufscar.pescd.service.SupervisorProfessorService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/professor/supervisor")
public class SupervisorProfessorController {

    private final SupervisorProfessorService supervisorProfessorService;
    private final UserRepository userRepository;

    public SupervisorProfessorController(
            SupervisorProfessorService supervisorProfessorService,
            UserRepository userRepository
    ) {
        this.supervisorProfessorService = supervisorProfessorService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {

        User professor = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        List<SupervisorDashboardDTO> dashboard =
                supervisorProfessorService.getDashboard(professor);

        model.addAttribute("dashboard", dashboard);
        model.addAttribute("professor", professor);

        return "professor/supervisor/dashboard";
    }
}
