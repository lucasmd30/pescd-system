package br.ufscar.pescd.controller;

import br.ufscar.pescd.dto.CloseOfferFormDTO;
import br.ufscar.pescd.dto.OfferDetailsResponse;
import br.ufscar.pescd.dto.OfferStudentSummaryResponse;
import br.ufscar.pescd.dto.OfferSummaryResponse;
import br.ufscar.pescd.dto.ResponsibleCloseOfferSummaryResponse;
import br.ufscar.pescd.entity.User;
import br.ufscar.pescd.repository.UserRepository;
import br.ufscar.pescd.service.ProfessorResponsavelService;
import jakarta.validation.Valid;
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

    public ProfessorResponsavelApiController(
            ProfessorResponsavelService professorResponsavelService,
            UserRepository userRepository
    ) {
        this.professorResponsavelService = professorResponsavelService;
        this.userRepository = userRepository;
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

    private User resolveUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Usuário autenticado não encontrado."));
    }
}
