package br.ufscar.pescd.repository;

import br.ufscar.pescd.entity.OfferStudent;
import br.ufscar.pescd.entity.WorkPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkPlanRepository extends JpaRepository<WorkPlan, Long> {

    Optional<WorkPlan> findByOfferStudent(OfferStudent offerStudent);
}
