package br.ufscar.pescd.repository;

import br.ufscar.pescd.entity.OfferStudent;
import br.ufscar.pescd.entity.StatusChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StatusChangeLogRepository extends JpaRepository<StatusChangeLog, Long> {

    List<StatusChangeLog> findByOfferStudentOrderByChangedAtAsc(OfferStudent offerStudent);
}
