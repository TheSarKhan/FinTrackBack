package az.sarkhan.fintechsark.repository;

import az.sarkhan.fintechsark.entity.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {

    List<RecurringTransaction> findByUserIdAndIsActiveTrue(Long userId);
    List<RecurringTransaction> findByUserId(Long userId);

    // Bu gün icra edilməli olanlar
    @Query("""
        SELECT r FROM RecurringTransaction r
        WHERE r.isActive = true
          AND r.nextExecutionDate <= :today
          AND (r.endDate IS NULL OR r.endDate >= :today)
        """)
    List<RecurringTransaction> findDueToday(@Param("today") LocalDate today);
}