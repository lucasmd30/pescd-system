package br.ufscar.pescd.entity;

import br.ufscar.pescd.enums.StudentOfferStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "offer_students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OfferStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "supervisor_professor_id")
    private User supervisor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentOfferStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = StudentOfferStatus.NAO_ENVIADO;
        }
    }
}
