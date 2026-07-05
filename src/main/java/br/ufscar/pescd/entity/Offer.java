package br.ufscar.pescd.entity;

import br.ufscar.pescd.enums.OfferStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false)
    private String semester;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private OfferStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime closedAt;

    private LocalDateTime closureRequestedAt;

    @Column(columnDefinition = "TEXT")
    private String lessonsLearned;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "responsible_professor_id")
    private User responsibleProfessor;

    @ManyToOne
    @JoinColumn(name = "closure_requested_by_id")
    private User closureRequestedBy;

    @ManyToOne
    @JoinColumn(name = "closed_by_id")
    private User closedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
