package az.sarkhan.fintechsark.service.impl;

import az.sarkhan.fintechsark.dto.request.RecurringTransactionRequest;
import az.sarkhan.fintechsark.dto.response.RecurringTransactionResponse;
import az.sarkhan.fintechsark.entity.*;
import az.sarkhan.fintechsark.enums.RecurringFrequency;
import az.sarkhan.fintechsark.exception.BusinessException;
import az.sarkhan.fintechsark.exception.ResourceNotFoundException;
import az.sarkhan.fintechsark.repository.*;
import az.sarkhan.fintechsark.security.SecurityUtils;
import az.sarkhan.fintechsark.service.RecurringTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    private final RecurringTransactionRepository recurringRepo;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public RecurringTransactionResponse create(RecurringTransactionRequest request) {
        User user = securityUtils.getCurrentUser();
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Kateqoriya tapılmadı"));

        // İlk icra tarixi hesabla
        LocalDate nextExecution = calculateNextDate(request.startDate(), request);

        RecurringTransaction recurring = RecurringTransaction.builder()
                .user(user)
                .category(category)
                .description(request.description())
                .amount(request.amount())
                .type(request.type())
                .frequency(request.frequency())
                .dayOfMonth(request.dayOfMonth())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .nextExecutionDate(nextExecution)
                .isActive(true)
                .build();

        return toResponse(recurringRepo.save(recurring));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecurringTransactionResponse> getAll() {
        Long userId = securityUtils.getCurrentUserId();
        return recurringRepo.findByUserId(userId)
                .stream().map(this::toResponse).toList();
    }
    @Override
    @Transactional
    public RecurringTransactionResponse toggleActive(Long id) {
        RecurringTransaction r = findOwned(id);
        r.setIsActive(!r.getIsActive());
        return toResponse(recurringRepo.save(r));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        RecurringTransaction r = findOwned(id);
        recurringRepo.delete(r);
    }

    // ── Hər gün saat 08:00-da işləyir ───────────────────────────────────
    @Override
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void executeAllDue() {
        LocalDate today = LocalDate.now();
        List<RecurringTransaction> due = recurringRepo.findDueToday(today);

        log.info("Recurring transactions: {} ədəd icra ediləcək", due.size());

        for (RecurringTransaction r : due) {
            try {
                // Tranzaksiya yarat
                Transaction transaction = Transaction.builder()
                        .user(r.getUser())
                        .category(r.getCategory())
                        .description(r.getDescription())
                        .amount(r.getAmount())
                        .type(r.getType())
                        .date(today)
                        .isDeleted(false)
                        .build();

                transactionRepository.save(transaction);

                // Növbəti icra tarixini yenilə
                r.setLastExecutedDate(today);
                r.setNextExecutionDate(calculateNextDate(today, r));
                recurringRepo.save(r);

                log.info("Recurring icra edildi: {} - {} AZN", r.getDescription(), r.getAmount());

            } catch (Exception e) {
                log.error("Recurring icra xətası: id={}, error={}", r.getId(), e.getMessage());
            }
        }
    }

    // ── Köməkçi metodlar ─────────────────────────────────────────────────
    private LocalDate calculateNextDate(LocalDate from, RecurringTransactionRequest req) {
        return switch (req.frequency()) {
            case MONTHLY -> from.withDayOfMonth(req.dayOfMonth()).isBefore(from) ||
                    from.withDayOfMonth(req.dayOfMonth()).isEqual(from)
                    ? from.plusMonths(1).withDayOfMonth(req.dayOfMonth())
                    : from.withDayOfMonth(req.dayOfMonth());
            case WEEKLY  -> from.plusWeeks(1);
            case YEARLY  -> from.plusYears(1);
        };
    }

    private LocalDate calculateNextDate(LocalDate from, RecurringTransaction r) {
        return switch (r.getFrequency()) {
            case MONTHLY -> from.plusMonths(1).withDayOfMonth(r.getDayOfMonth());
            case WEEKLY  -> from.plusWeeks(1);
            case YEARLY  -> from.plusYears(1);
        };
    }

    private RecurringTransaction findOwned(Long id) {
        Long userId = securityUtils.getCurrentUserId();
        RecurringTransaction r = recurringRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tapılmadı: " + id));
        if (!r.getUser().getId().equals(userId))
            throw new BusinessException("Giriş qadağandır");
        return r;
    }

    private RecurringTransactionResponse toResponse(RecurringTransaction r) {
        return new RecurringTransactionResponse(
                r.getId(), r.getDescription(), r.getAmount(), r.getType(),
                r.getFrequency(), r.getDayOfMonth(), r.getStartDate(), r.getEndDate(),
                r.getLastExecutedDate(), r.getNextExecutionDate(), r.getIsActive(),
                r.getCategory().getId(), r.getCategory().getName(), r.getCreatedAt()
        );
    }
}