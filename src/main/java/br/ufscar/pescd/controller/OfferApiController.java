package br.ufscar.pescd.controller;

import br.ufscar.pescd.dto.OfferView;
import br.ufscar.pescd.service.OfferService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/offers")
public class OfferApiController {

    private final OfferService offerService;

    public OfferApiController(OfferService offerService) {
        this.offerService = offerService;
    }

    @GetMapping
    public List<OfferView> listPublicOffers() {
        return offerService.findPublicOffers();
    }
}
