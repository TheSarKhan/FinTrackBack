package az.sarkhan.fintechsark.service.impl;

import az.sarkhan.fintechsark.dto.request.BulkTransactionRequest;
import az.sarkhan.fintechsark.dto.request.TransactionRequest;
import az.sarkhan.fintechsark.dto.response.PageResponse;
import az.sarkhan.fintechsark.dto.response.TransactionResponse;
import az.sarkhan.fintechsark.entity.Category;
import az.sarkhan.fintechsark.entity.Transaction;
import az.sarkhan.fintechsark.entity.User;
import az.sarkhan.fintechsark.enums.TransactionType;
import az.sarkhan.fintechsark.exception.BusinessException;
import az.sarkhan.fintechsark.exception.ResourceNotFoundException;
import az.sarkhan.fintechsark.repository.CategoryRepository;
import az.sarkhan.fintechsark.repository.TransactionRepository;
import az.sarkhan.fintechsark.security.SecurityUtils;
import az.sarkhan.fintechsark.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        User user = securityUtils.getCurrentUser();

        Category category;

        if (request.categoryId() != null) {
            category = findAndValidateCategory(request.categoryId(), user.getId());
        } else {
            // ← BURA dəyiş
            category = categoryRepository.findAllVisibleToUser(user.getId())
                    .stream()
                    .filter(c -> c.getName().equalsIgnoreCase("Bank Çıxarışı"))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Bank Çıxarışı kateqoriyası tapılmadı"));
        }

        String description = (request.description() != null && !request.description().isBlank())
                ? request.description()
                : buildDefaultDescription(category);

        Transaction transaction = Transaction.builder()
                .amount(request.amount())
                .description(description)
                .type(request.type())
                .date(request.date())
                .user(user)
                .category(category)
                .isDeleted(false)
                .build();

        return toResponse(transactionRepository.save(transaction));
    }

    private String buildDefaultDescription(Category category) {
        if (category.getParent() != null) {
            return category.getParent().getName() + " + " + category.getName();
        }
        return category.getName();
    }
    @Override
    @Transactional
    public List<TransactionResponse> bulkCreate(BulkTransactionRequest request) {
        User user = securityUtils.getCurrentUser();
        List<Transaction> transactions = request.transactions().stream()
                .map(item -> {
                    Category category = findAndValidateCategory(item.categoryId(), user.getId());
                    return Transaction.builder()
                            .amount(item.amount())
                            .description(item.description())
                            .type(item.type())
                            .date(item.date())
                            .user(user)
                            .category(category)
                            .isDeleted(false)
                            .build();
                })
                .toList();
        return transactionRepository.saveAll(transactions).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        User user = securityUtils.getCurrentUser();
        Transaction transaction = findOwnedTransaction(id, user.getId());
        Category category = findAndValidateCategory(request.categoryId(), user.getId());
        transaction.setAmount(request.amount());
        transaction.setDescription(request.description());
        transaction.setType(request.type());
        transaction.setDate(request.date());
        transaction.setCategory(category);
        transaction.setUpdatedAt(LocalDateTime.now());
        return toResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = securityUtils.getCurrentUser();
        Transaction transaction = findOwnedTransaction(id, user.getId());
        transaction.setIsDeleted(true);
        transaction.setUpdatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getById(Long id) {
        Long userId = securityUtils.getCurrentUserId();
        return toResponse(findOwnedTransaction(id, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> getAll(
            TransactionType type, Long categoryId,
            LocalDate startDate, LocalDate endDate,
            int page, int size) {
        Long userId = securityUtils.getCurrentUserId();
        Page<Transaction> pageResult = transactionRepository.findAllByFilters(
                userId,
                type != null ? type.name() : null,
                categoryId,
                startDate != null ? startDate.toString() : null,
                endDate   != null ? endDate.toString()   : null,
                PageRequest.of(page, size)
        );
        return new PageResponse<>(
                pageResult.getContent().stream().map(this::toResponse).toList(),
                pageResult.getNumber(), pageResult.getSize(),
                pageResult.getTotalElements(), pageResult.getTotalPages(), pageResult.isLast()
        );
    }

    private Transaction findOwnedTransaction(Long id, Long userId) {
        Transaction t = transactionRepository.findByIdWithCategory(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));
        if (!t.getUser().getId().equals(userId)) throw new BusinessException("Access denied to this transaction");
        if (Boolean.TRUE.equals(t.getIsDeleted())) throw new ResourceNotFoundException("Transaction not found: " + id);
        return t;
    }

    private Category findAndValidateCategory(Long categoryId, Long userId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));
        if (!category.getIsActive()) throw new BusinessException("Category is not active");
        if (category.getUser() != null && !category.getUser().getId().equals(userId))
            throw new BusinessException("You don't have access to this category");
        return category;
    }

    public TransactionResponse toResponse(Transaction t) {
        Category cat = t.getCategory();
        Category parent = cat.getParent();
        return new TransactionResponse(
                t.getId(), t.getAmount(), t.getDescription(), t.getType(), t.getDate(),
                cat.getId(), cat.getName(),
                parent != null ? parent.getId()   : null,
                parent != null ? parent.getName() : null,
                t.getCreatedAt()
        );
    }
}
