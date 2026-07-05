package br.ufscar.pescd.controller;

import br.ufscar.pescd.dto.OfferDetailsResponse;
import br.ufscar.pescd.dto.OfferStudentDetailsResponse;
import br.ufscar.pescd.dto.OfferSummaryResponse;
import br.ufscar.pescd.dto.SecretaryCloseOfferPreviewResponse;
import br.ufscar.pescd.dto.SecretaryCloseOfferRequest;
import br.ufscar.pescd.service.SecretaryOfferService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/secretary/offers")
public class SecretaryOfferApiController {

    private final SecretaryOfferService secretaryOfferService;

    public SecretaryOfferApiController(SecretaryOfferService secretaryOfferService) {
        this.secretaryOfferService = secretaryOfferService;
    }

    @GetMapping
    public List<OfferSummaryResponse> listOffers() {
        return secretaryOfferService.listOffersForApi();
    }

    @GetMapping("/{offerId}")
    public OfferDetailsResponse offerDetails(@PathVariable Long offerId) {
        return secretaryOfferService.getOfferDetailsForApi(offerId);
    }

    @GetMapping("/{offerId}/students/{offerStudentId}")
    public OfferStudentDetailsResponse studentDetails(
            @PathVariable Long offerId,
            @PathVariable Long offerStudentId
    ) {
        return secretaryOfferService.getStudentDetailsForApi(offerId, offerStudentId);
    }

    @GetMapping("/{offerId}/close")
    public SecretaryCloseOfferPreviewResponse closePreview(@PathVariable Long offerId) {
        return secretaryOfferService.getCloseOfferPreviewForApi(offerId);
    }

    @PostMapping("/{offerId}/close")
    public OfferSummaryResponse closeOffer(
            @PathVariable Long offerId,
            @Valid @RequestBody SecretaryCloseOfferRequest request,
            Authentication authentication
    ) {
        return secretaryOfferService.closeOfferForApi(offerId, authentication.getName());
    }
}
