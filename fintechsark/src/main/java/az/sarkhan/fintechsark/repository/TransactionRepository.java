package az.sarkhan.fintechsark.repository;

import az.sarkhan.fintechsark.entity.Transaction;
import az.sarkhan.fintechsark.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query(value = """
    SELECT t.* FROM transactions t
    JOIN categories c ON c.id = t.category_id
    LEFT JOIN categories p ON p.id = c.parent_id
    WHERE t.user_id = :userId
      AND t.is_deleted = false
      AND (CAST(:type AS varchar) IS NULL OR t.type = CAST(:type AS varchar))
      AND (CAST(:categoryId AS bigint) IS NULL OR t.category_id = CAST(:categoryId AS bigint))
      AND (CAST(:startDate AS date) IS NULL OR t.date >= CAST(:startDate AS date))
      AND (CAST(:endDate AS date) IS NULL OR t.date <= CAST(:endDate AS date))
    ORDER BY t.date DESC, t.created_at DESC
    """, nativeQuery = true,
            countQuery = """
    SELECT COUNT(*) FROM transactions t
    WHERE t.user_id = :userId
      AND t.is_deleted = false
      AND (CAST(:type AS varchar) IS NULL OR t.type = CAST(:type AS varchar))
      AND (CAST(:categoryId AS bigint) IS NULL OR t.category_id = CAST(:categoryId AS bigint))
      AND (CAST(:startDate AS date) IS NULL OR t.date >= CAST(:startDate AS date))
      AND (CAST(:endDate AS date) IS NULL OR t.date <= CAST(:endDate AS date))
    """)
    Page<Transaction> findAllByFilters(
            @Param("userId") Long userId,
            @Param("type") String type,
            @Param("categoryId") Long categoryId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            Pageable pageable);

    @Query("""
        SELECT t FROM Transaction t
        JOIN FETCH t.category c
        LEFT JOIN FETCH c.parent
        WHERE t.id = :id AND t.isDeleted = false
        """)
    Optional<Transaction> findByIdWithCategory(@Param("id") Long id);

    @Query("""
        SELECT t FROM Transaction t
        JOIN FETCH t.category c
        LEFT JOIN FETCH c.parent
        WHERE t.user.id = :userId AND t.isDeleted = false
        ORDER BY t.date DESC, t.createdAt DESC
        """)
    List<Transaction> findTop10ByUserId(@Param("userId") Long userId, Pageable pageable);

    // ✅ Native query — enum problem yoxdur
    @Query(value = """
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE user_id = :userId
          AND type = 'INCOME'
          AND is_deleted = false
        """, nativeQuery = true)
    BigDecimal sumIncomeByUser(@Param("userId") Long userId);

    @Query(value = """
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE user_id = :userId
          AND type = 'EXPENSE'
          AND is_deleted = false
        """, nativeQuery = true)
    BigDecimal sumExpenseByUser(@Param("userId") Long userId);

    @Query(value = """
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE user_id = :userId
          AND type = 'EXPENSE'
          AND is_deleted = false
          AND EXTRACT(MONTH FROM date) = EXTRACT(MONTH FROM CURRENT_DATE)
          AND EXTRACT(YEAR FROM date) = EXTRACT(YEAR FROM CURRENT_DATE)
        """, nativeQuery = true)
    BigDecimal sumCurrentMonthExpense(@Param("userId") Long userId);

    @Query(value = """
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE user_id = :userId
          AND type = 'INCOME'
          AND is_deleted = false
          AND date >= :startDate
          AND date <= :endDate
        """, nativeQuery = true)
    BigDecimal sumIncomeByPeriod(@Param("userId") Long userId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

    @Query(value = """
        SELECT COALESCE(SUM(amount), 0)
        FROM transactions
        WHERE user_id = :userId
          AND type = 'EXPENSE'
          AND is_deleted = false
          AND date >= :startDate
          AND date <= :endDate
        """, nativeQuery = true)
    BigDecimal sumExpenseByPeriod(@Param("userId") Long userId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    @Query(value = """
        SELECT
            TO_CHAR(date, 'YYYY-MM') AS month,
            type,
            COALESCE(SUM(amount), 0) AS total
        FROM transactions
        WHERE user_id = :userId
          AND is_deleted = false
          AND date >= DATE_TRUNC('month', CURRENT_DATE) - INTERVAL '5 months'
        GROUP BY TO_CHAR(date, 'YYYY-MM'), type
        ORDER BY month ASC
        """, nativeQuery = true)
    List<Object[]> findMonthlyTrend(@Param("userId") Long userId);

    @Query(value = """
        SELECT
            TO_CHAR(date, 'YYYY-MM') AS month,
            type,
            COALESCE(SUM(amount), 0) AS total
        FROM transactions
        WHERE user_id = :userId
          AND is_deleted = false
          AND date >= :startDate
        GROUP BY TO_CHAR(date, 'YYYY-MM'), type
        ORDER BY month ASC
        """, nativeQuery = true)
    List<Object[]> findMonthlyTrendByPeriod(@Param("userId") Long userId,
                                            @Param("startDate") LocalDate startDate);

    @Query(value = """
        SELECT
            COALESCE(c.parent_id, c.id)   AS parentId,
            COALESCE(p.name, c.name)       AS parentName,
            SUM(t.amount)                  AS total
        FROM transactions t
        JOIN categories c ON c.id = t.category_id
        LEFT JOIN categories p ON p.id = c.parent_id
        WHERE t.user_id = :userId
          AND t.type = 'EXPENSE'
          AND t.is_deleted = false
          AND (CAST(:startDate AS date) IS NULL OR t.date >= CAST(:startDate AS date))
          AND (CAST(:endDate AS date) IS NULL OR t.date <= CAST(:endDate AS date))
        GROUP BY COALESCE(c.parent_id, c.id), COALESCE(p.name, c.name)
        ORDER BY total DESC
        """, nativeQuery = true)
    List<Object[]> findExpenseByParentCategory(
            @Param("userId") Long userId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);

    @Query(value = """
        SELECT
            c.id          AS catId,
            c.name        AS catName,
            SUM(t.amount) AS total
        FROM transactions t
        JOIN categories c ON c.id = t.category_id
        WHERE t.user_id = :userId
          AND t.type = 'EXPENSE'
          AND t.is_deleted = false
          AND c.parent_id = :parentId
          AND t.date >= :startDate
          AND t.date <= :endDate
        GROUP BY c.id, c.name
        ORDER BY total DESC
        """, nativeQuery = true)
    List<Object[]> findExpenseBySubcategory(
            @Param("userId") Long userId,
            @Param("parentId") Long parentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("""
    SELECT t FROM Transaction t
    JOIN FETCH t.category c
    LEFT JOIN FETCH c.parent
    WHERE t.user.id = :userId
      AND t.isDeleted = false
      AND t.date >= :startDate
      AND t.date <= :endDate
    ORDER BY t.date DESC
    """)
    List<Transaction> findCurrentMonthTransactions(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query(value = """
    SELECT t.* FROM transactions t
    JOIN categories c ON c.id = t.category_id
    LEFT JOIN categories p ON p.id = c.parent_id
    WHERE t.user_id = :userId
      AND t.is_deleted = false
      AND (
          LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))
          OR CAST(t.amount AS varchar) LIKE CONCAT('%', :query, '%')
          OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))
          OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%'))
      )
    ORDER BY t.date DESC, t.created_at DESC
    """, nativeQuery = true)
    List<Transaction> searchTransactions(
            @Param("userId") Long userId,
            @Param("query") String query);
}