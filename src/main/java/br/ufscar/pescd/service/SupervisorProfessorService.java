package br.ufscar.pescd.service;

import br.ufscar.pescd.dto.SupervisorDashboardDTO;
import br.ufscar.pescd.entity.Offer;
import br.ufscar.pescd.entity.OfferStudent;
import br.ufscar.pescd.entity.User;
import br.ufscar.pescd.repository.OfferStudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SupervisorProfessorService {

    private final OfferStudentRepository offerStudentRepository;

    public SupervisorProfessorService(OfferStudentRepository offerStudentRepository) {
        this.offerStudentRepository = offerStudentRepository;
    }

    @Transactional(readOnly = true)
    public List<SupervisorDashboardDTO> getDashboard(User professor) {

        List<OfferStudent> supervised =
                offerStudentRepository.findBySupervisor(professor);

        Map<Offer, List<OfferStudent>> byOffer = supervised.stream()
                .collect(Collectors.groupingBy(OfferStudent::getOffer));

        return byOffer.entrySet().stream()
                .map(entry -> new SupervisorDashboardDTO(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(
                        dto -> dto.getOffer().getSemester(),
                        Comparator.reverseOrder()
                ))
                .collect(Collectors.toList());
    }
}
