package br.ufscar.pescd.entity;

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

    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] fileContent;

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
    }
}
