package br.ufscar.pescd.repository;

import br.ufscar.pescd.entity.Documentation;
import br.ufscar.pescd.entity.OfferStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentationRepository extends JpaRepository<Documentation, Long> {

    Optional<Documentation> findByOfferStudent(OfferStudent offerStudent);
}
