package br.ufscar.pescd.entity;

import br.ufscar.pescd.enums.GradeOption;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "offer_student_id", nullable = false, unique = true)
    private OfferStudent offerStudent;

    @Column(nullable = false)
    private Integer frequency;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] fileContent;

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    // -------------------------------------------------------------------------
    // PS.03 — campos preenchidos pelo Professor Supervisor
    // -------------------------------------------------------------------------

    /** Parecer do supervisor sobre o relatório (PS.03). */
    @Column(columnDefinition = "TEXT")
    private String supervisorParecer;

    /** Frequência confirmada/ajustada pelo supervisor (PS.03). */
    private Integer supervisorFrequencia;

    /** Sugestão de nota do supervisor (PS.03). */
    @Enumerated(EnumType.STRING)
    private GradeOption supervisorNotaSugestao;

    /** Timestamp da aprovação pelo supervisor (PS.03). */
    private LocalDateTime supervisorApprovedAt;

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
    }
}
