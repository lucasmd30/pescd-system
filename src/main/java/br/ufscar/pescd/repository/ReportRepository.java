package br.ufscar.pescd.repository;

import br.ufscar.pescd.entity.OfferStudent;
import br.ufscar.pescd.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findByOfferStudent(OfferStudent offerStudent);
}
