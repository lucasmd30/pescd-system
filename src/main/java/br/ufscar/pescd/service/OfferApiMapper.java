package br.ufscar.pescd.service;

import br.ufscar.pescd.dto.OfferDetailsResponse;
import br.ufscar.pescd.dto.OfferStudentDetailsResponse;
import br.ufscar.pescd.dto.OfferStudentSummaryResponse;
import br.ufscar.pescd.dto.OfferSummaryResponse;
import br.ufscar.pescd.dto.ResponsibleCloseOfferSummaryResponse;
import br.ufscar.pescd.dto.StatusLogResponse;
import br.ufscar.pescd.dto.UserSummaryResponse;
import br.ufscar.pescd.entity.Documentation;
import br.ufscar.pescd.entity.Offer;
import br.ufscar.pescd.entity.OfferStudent;
import br.ufscar.pescd.entity.Report;
import br.ufscar.pescd.entity.StatusChangeLog;
import br.ufscar.pescd.entity.User;
import br.ufscar.pescd.enums.GradeOption;
import br.ufscar.pescd.repository.DocumentationRepository;
import br.ufscar.pescd.repository.OfferStudentRepository;
import br.ufscar.pescd.repository.ReportRepository;
import br.ufscar.pescd.repository.StatusChangeLogRepository;
import br.ufscar.pescd.repository.WorkPlanRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class OfferApiMapper {

    private final OfferStudentRepository offerStudentRepository;
    private final WorkPlanRepository workPlanRepository;
    private final DocumentationRepository documentationRepository;
    private final ReportRepository reportRepository;
    private final StatusChangeLogRepository statusChangeLogRepository;

    public OfferApiMapper(
            OfferStudentRepository offerStudentRepository,
            WorkPlanRepository workPlanRepository,
            DocumentationRepository documentationRepository,
            ReportRepository reportRepository,
            StatusChangeLogRepository statusChangeLogRepository
    ) {
        this.offerStudentRepository = offerStudentRepository;
        this.workPlanRepository = workPlanRepository;
        this.documentationRepository = documentationRepository;
        this.reportRepository = reportRepository;
        this.statusChangeLogRepository = statusChangeLogRepository;
    }

    public OfferSummaryResponse toOfferSummary(Offer offer) {
        return new OfferSummaryResponse(
                offer.getId(),
                offer.getName(),
                offer.getSemester(),
                offer.getStartDate(),
                offer.getEndDate(),
                offer.getStatus(),
                offer.getStatus().getDisplayName(),
                offer.getCreatedAt(),
                offer.getClosureRequestedAt(),
                offer.getClosedAt(),
                offer.getLessonsLearned(),
                toUserSummary(offer.getResponsibleProfessor()),
                toUserSummary(offer.getClosureRequestedBy()),
                toUserSummary(offer.getClosedBy()),
                offerStudentRepository.countByOffer(offer)
        );
    }

    public OfferDetailsResponse toOfferDetails(Offer offer, List<OfferStudent> students) {
        return new OfferDetailsResponse(
                toOfferSummary(offer),
                students.stream().map(this::toOfferStudentSummary).toList()
        );
    }

    public OfferStudentDetailsResponse toOfferStudentDetails(OfferStudent enrollment) {
        return new OfferStudentDetailsResponse(
                toOfferSummary(enrollment.getOffer()),
                toOfferStudentSummary(enrollment),
                toStatusLogs(enrollment)
        );
    }

    public ResponsibleCloseOfferSummaryResponse toResponsibleCloseSummary(
            Offer offer,
            List<OfferStudent> students,
            boolean canClose
    ) {
        List<OfferStudentSummaryResponse> studentResponses = students.stream()
                .map(this::toOfferStudentSummary)
                .toList();

        List<Integer> frequencies = studentResponses.stream()
                .map(OfferStudentSummaryResponse::finalFrequency)
                .filter(Objects::nonNull)
                .toList();

        BigDecimal averageFrequency = frequencies.isEmpty()
                ? null
                : BigDecimal.valueOf(frequencies.stream().mapToInt(Integer::intValue).average().orElse(0))
                        .setScale(2, RoundingMode.HALF_UP);

        Map<String, Long> gradeDistribution = studentResponses.stream()
                .map(OfferStudentSummaryResponse::finalGrade)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        GradeOption::name,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        return new ResponsibleCloseOfferSummaryResponse(
                toOfferSummary(offer),
                studentResponses,
                averageFrequency,
                studentResponses.stream().filter(s -> "DOCUMENTATION".equals(s.completionSource())).count(),
                studentResponses.stream().filter(s -> "REPORT".equals(s.completionSource())).count(),
                gradeDistribution,
                canClose
        );
    }

    public OfferStudentSummaryResponse toOfferStudentSummary(OfferStudent enrollment) {
        Optional<Documentation> documentation = documentationRepository.findByOfferStudent(enrollment);
        Optional<Report> report = reportRepository.findByOfferStudent(enrollment);

        Integer finalFrequency = report.map(Report::getResponsavelFrequencia)
                .or(() -> documentation.map(Documentation::getResponsavelFrequencia))
                .orElse(null);
        GradeOption finalGrade = report.map(Report::getResponsavelNota)
                .or(() -> documentation.map(Documentation::getResponsavelNota))
                .orElse(null);

        String completionSource = null;
        if (report.map(Report::getResponsavelApprovedAt).isPresent()) {
            completionSource = "REPORT";
        } else if (documentation.map(Documentation::getResponsavelApprovedAt).isPresent()) {
            completionSource = "DOCUMENTATION";
        }

        return new OfferStudentSummaryResponse(
                enrollment.getId(),
                toUserSummary(enrollment.getStudent()),
                toUserSummary(enrollment.getSupervisor()),
                enrollment.getStatus(),
                enrollment.getStatus().getDisplayName(),
                enrollment.getCreatedAt(),
                workPlanRepository.findByOfferStudent(enrollment).isPresent(),
                documentation.isPresent(),
                report.isPresent(),
                completionSource,
                finalFrequency,
                finalGrade,
                finalGrade != null ? finalGrade.getDisplayName() : null
        );
    }

    public List<StatusLogResponse> toStatusLogs(OfferStudent enrollment) {
        return statusChangeLogRepository.findByOfferStudentOrderByChangedAtAsc(enrollment)
                .stream()
                .map(this::toStatusLog)
                .toList();
    }

    public StatusLogResponse toStatusLog(StatusChangeLog log) {
        return new StatusLogResponse(
                log.getId(),
                log.getPreviousStatus(),
                log.getPreviousStatus() != null ? log.getPreviousStatus().getDisplayName() : null,
                log.getNewStatus(),
                log.getNewStatus().getDisplayName(),
                log.getDescription(),
                log.getChangedAt()
        );
    }

    public UserSummaryResponse toUserSummary(User user) {
        if (user == null) {
            return null;
        }

        return new UserSummaryResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getUsername()
        );
    }
}
