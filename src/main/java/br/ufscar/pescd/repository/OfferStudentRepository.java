package br.ufscar.pescd.repository;

import br.ufscar.pescd.entity.Offer;
import br.ufscar.pescd.entity.OfferStudent;
import br.ufscar.pescd.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OfferStudentRepository extends JpaRepository<OfferStudent, Long> {

    List<OfferStudent> findByOffer(Offer offer);

    List<OfferStudent> findByStudent(User student);

    Optional<OfferStudent> findByOfferAndStudent(Offer offer, User student);

    long countByOffer(Offer offer);

    boolean existsByOfferAndStudent(Offer offer, User student);
}
