package br.ufscar.pescd.repository;

import br.ufscar.pescd.entity.OfferStudent;
import br.ufscar.pescd.entity.OfferStudentStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferStudentStatusLogRepository extends JpaRepository<OfferStudentStatusLog, Long> {

    List<OfferStudentStatusLog> findByOfferStudentOrderByChangedAtAsc(OfferStudent offerStudent);
}
