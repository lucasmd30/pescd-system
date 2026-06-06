package br.ufscar.pescd.service;

import br.ufscar.pescd.dto.SupervisorDashboardDTO;
import br.ufscar.pescd.entity.*;
import br.ufscar.pescd.enums.StudentOfferStatus;
import br.ufscar.pescd.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SupervisorProfessorService {

    private final OfferStudentRepository offerStudentRepository;
    private final WorkPlanRepository workPlanRepository;
    private final StatusChangeLogRepository statusChangeLogRepository;

    public SupervisorProfessorService(
            OfferStudentRepository offerStudentRepository,
            WorkPlanRepository workPlanRepository,
            StatusChangeLogRepository statusChangeLogRepository
    ) {
        this.offerStudentRepository = offerStudentRepository;
        this.workPlanRepository = workPlanRepository;
        this.statusChangeLogRepository = statusChangeLogRepository;
    }

    // -------------------------------------------------------------------------
    // PS.01 — Dashboard
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // PS.02 — Aprovar Plano de Trabalho
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public OfferStudent getEnrollmentForSupervisor(Long offerId, Long studentId, User professor) {

        OfferStudent os = offerStudentRepository
                .findByOffer_IdAndStudent_Id(offerId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("Inscrição não encontrada."));

        if (!professor.equals(os.getSupervisor())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Você não é o supervisor deste aluno nesta oferta.");
        }

        return os;
    }

    @Transactional(readOnly = true)
    public WorkPlan getWorkPlan(OfferStudent offerStudent) {
        return workPlanRepository.findByOfferStudent(offerStudent)
                .orElseThrow(() -> new IllegalArgumentException("Plano de trabalho não encontrado."));
    }

    @Transactional
    public void approvePlan(Long offerId, Long studentId, User professor, String parecer) {

        OfferStudent os = getEnrollmentForSupervisor(offerId, studentId, professor);

        if (os.getStatus() != StudentOfferStatus.PLANO_ENVIADO) {
            throw new IllegalArgumentException(
                    "Ação indisponível: o status atual é \""
                    + os.getStatus().getDisplayName() + "\".");
        }

        WorkPlan plan = getWorkPlan(os);
        plan.setSupervisorParecer(parecer);
        plan.setSupervisorApprovedAt(LocalDateTime.now());
        workPlanRepository.save(plan);

        changeStatus(os, StudentOfferStatus.PLANO_APROVADO, "Plano de trabalho aprovado pelo professor supervisor");
    }

    // -------------------------------------------------------------------------
    // Utilitário — log de status (mesmo padrão do StudentOfferService)
    // -------------------------------------------------------------------------

    private void changeStatus(OfferStudent os, StudentOfferStatus newStatus, String description) {
        StatusChangeLog log = new StatusChangeLog();
        log.setOfferStudent(os);
        log.setPreviousStatus(os.getStatus());
        log.setNewStatus(newStatus);
        log.setDescription(description);
        statusChangeLogRepository.save(log);

        os.setStatus(newStatus);
        offerStudentRepository.save(os);
    }
}
