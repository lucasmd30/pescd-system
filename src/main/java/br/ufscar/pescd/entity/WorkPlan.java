package br.ufscar.pescd.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "work_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WorkPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "offer_student_id", nullable = false, unique = true)
    private OfferStudent offerStudent;

    @Column(nullable = false)
    private String disciplineCode;

    @Column(nullable = false)
    private String disciplineName;

    @Column(nullable = false)
    private String disciplineCourse;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    /** Identificador do arquivo no SeaweedFS (fid), ex.: "3,01637037d6". */
    @Column(name = "file_fid")
    private String fileFid;

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    /** Preenchido pelo professor supervisor ao aprovar o plano (PS.02). */
    @Column(columnDefinition = "TEXT")
    private String supervisorParecer;

    /** Timestamp da aprovação pelo supervisor (PS.02). */
    private LocalDateTime supervisorApprovedAt;

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
    }
}
