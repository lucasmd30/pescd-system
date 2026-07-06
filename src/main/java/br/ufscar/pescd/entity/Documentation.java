package br.ufscar.pescd.entity;

import br.ufscar.pescd.enums.GradeOption;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "documentations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Documentation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "offer_student_id", nullable = false, unique = true)
    private OfferStudent offerStudent;

    @Column(nullable = false)
    private String institutionName;

    @Column(nullable = false)
    private String disciplineName;

    @Column(nullable = false)
    private String disciplineCourse;

    @Column(nullable = false)
    private Integer workloadHours;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    /** Identificador do arquivo no SeaweedFS (fid), ex.: "3,01637037d6". */
    @Column(name = "file_fid")
    private String fileFid;

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    // -------------------------------------------------------------------------
    // Campos preenchidos pelo Professor Responsável (PR.02)
    // -------------------------------------------------------------------------

    @Column(columnDefinition = "TEXT")
    private String responsavelParecer;

    private Integer responsavelFrequencia;

    @Enumerated(EnumType.STRING)
    private GradeOption responsavelNota;

    private LocalDateTime responsavelApprovedAt;

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
    }
}
