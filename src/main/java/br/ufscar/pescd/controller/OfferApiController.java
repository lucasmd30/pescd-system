package br.ufscar.pescd.controller;

import br.ufscar.pescd.dto.OfferView;
import br.ufscar.pescd.service.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
@Tag(name = "Ofertas públicas", description = "Consulta pública das ofertas do programa.")
public class OfferApiController {

    private final OfferService offerService;

    public OfferApiController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping
    @Operation(
            summary = "Listar ofertas públicas",
            description = "Retorna a lista pública de ofertas ordenada por semestre em ordem decrescente."
    )
    public List<OfferView> listPublicOffers() {
        return offerService.findPublicOffers();
    }
}
