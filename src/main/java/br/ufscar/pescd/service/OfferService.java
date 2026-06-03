package br.ufscar.pescd.service;

import br.ufscar.pescd.dto.OfferView;
import br.ufscar.pescd.entity.Offer;
import br.ufscar.pescd.repository.OfferRepository;
import br.ufscar.pescd.repository.OfferStudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final OfferStudentRepository offerStudentRepository;

    public OfferService(
            OfferRepository offerRepository,
            OfferStudentRepository offerStudentRepository
    ) {
        this.offerRepository = offerRepository;
        this.offerStudentRepository = offerStudentRepository;
    }

    @Transactional(readOnly = true)
    public List<OfferView> findPublicOffers() {
        return offerRepository.findAllByOrderBySemesterDesc()
                .stream()
                .map(this::toView)
                .toList();
    }

    private OfferView toView(Offer offer) {
        String professor = offer.getResponsibleProfessor() != null
                ? offer.getResponsibleProfessor().getFullName()
                : "Não definido";

        return new OfferView(
                offer.getName(),
                offer.getSemester(),
                offer.getStartDate(),
                offer.getEndDate(),
                professor,
                offerStudentRepository.countByOffer(offer)
        );
    }
}
