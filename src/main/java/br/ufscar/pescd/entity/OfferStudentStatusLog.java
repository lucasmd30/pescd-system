package br.ufscar.pescd.entity;

import br.ufscar.pescd.enums.StudentOfferStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "offer_student_status_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OfferStudentStatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "offer_student_id", nullable = false)
    private OfferStudent offerStudent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentOfferStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentOfferStatus newStatus;

    @ManyToOne
    @JoinColumn(name = "changed_by_id", nullable = false)
    private User changedBy;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        this.changedAt = LocalDateTime.now();
    }
}

