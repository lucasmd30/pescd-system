package br.ufscar.pescd.controller;

import br.ufscar.pescd.entity.Offer;
import br.ufscar.pescd.entity.OfferStudent;
import br.ufscar.pescd.service.SecretaryOfferService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;

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

    @GetMapping("/{id}")
    public String offerDetails(
            @PathVariable Long id,
            Model model
    ) {

        Offer offer = secretaryOfferService.getOffer(id);

        List<OfferStudent> students =
                secretaryOfferService.getOfferStudents(id);

        model.addAttribute("offer", offer);
        model.addAttribute("students", students);

        return "secretary/offers/details";
    }

    @PostMapping("/{id}/close")
    public String closeOffer(@PathVariable Long id) {

        secretaryOfferService.closeOffer(id);

        return "redirect:/secretary/offers";
    }
}