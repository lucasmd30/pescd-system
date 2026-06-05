package br.ufscar.pescd.entity;

import br.ufscar.pescd.enums.StudentOfferStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "status_change_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatusChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "offer_student_id", nullable = false)
    private OfferStudent offerStudent;

    @Enumerated(EnumType.STRING)
    private StudentOfferStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentOfferStatus newStatus;

    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        this.changedAt = LocalDateTime.now();
    }
}
