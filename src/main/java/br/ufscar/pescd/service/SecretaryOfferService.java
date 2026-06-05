package br.ufscar.pescd.service;

import br.ufscar.pescd.entity.Offer;
import br.ufscar.pescd.entity.OfferStudent;
import br.ufscar.pescd.repository.OfferRepository;
import br.ufscar.pescd.repository.OfferStudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SecretaryOfferService {

    private final OfferRepository offerRepository;
    private final OfferStudentRepository offerStudentRepository;

    public SecretaryOfferService(
            OfferRepository offerRepository,
            OfferStudentRepository offerStudentRepository
    ) {
        this.offerRepository = offerRepository;
        this.offerStudentRepository = offerStudentRepository;
    }

    @Transactional(readOnly = true)
    public List<Offer> listOffers() {
        return offerRepository.findAllByOrderBySemesterDesc();
    }

    @Transactional(readOnly = true)
    public Offer getOffer(Long id) {
        return offerRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Oferta não encontrada"));
    }

    @Transactional(readOnly = true)
    public List<OfferStudent> getOfferStudents(Long offerId) {

        Offer offer = getOffer(offerId);

        return offerStudentRepository.findByOffer(offer);
    }
}