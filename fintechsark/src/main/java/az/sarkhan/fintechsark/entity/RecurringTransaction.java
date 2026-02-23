package az.sarkhan.fintechsark.entity;

import az.sarkhan.fintechsark.enums.RecurringFrequency;
import az.sarkhan.fintechsark.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "recurring_transactions")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecurringTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurringFrequency frequency; // MONTHLY, WEEKLY, YEARLY

    // Hər ayın neçəsində icra edilsin (MONTHLY üçün)
    private Integer dayOfMonth;

    // Başlama tarixi
    @Column(nullable = false)
    private LocalDate startDate;

    // Bitmə tarixi (null = sonsuz)
    private LocalDate endDate;

    // Son icra tarixi
    private LocalDate lastExecutedDate;

    // Növbəti icra tarixi
    @Column(nullable = false)
    private LocalDate nextExecutionDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}