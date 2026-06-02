package br.ufscar.pescd.repository;

import br.ufscar.pescd.entity.Offer;
import br.ufscar.pescd.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    List<Offer> findAllByOrderBySemesterDesc();

    List<Offer> findByResponsibleProfessor(User professor);
}