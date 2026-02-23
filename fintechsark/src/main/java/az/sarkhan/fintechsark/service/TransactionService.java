package az.sarkhan.fintechsark.service;

import az.sarkhan.fintechsark.dto.request.BulkTransactionRequest;
import az.sarkhan.fintechsark.dto.request.TransactionRequest;
import az.sarkhan.fintechsark.dto.response.PageResponse;
import az.sarkhan.fintechsark.dto.response.TransactionResponse;
import az.sarkhan.fintechsark.enums.TransactionType;

import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    TransactionResponse create(TransactionRequest request);
    List<TransactionResponse> bulkCreate(BulkTransactionRequest request);
    TransactionResponse update(Long id, TransactionRequest request);
    void delete(Long id);
    TransactionResponse getById(Long id);
    PageResponse<TransactionResponse> getAll(
            TransactionType type,
            Long categoryId,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size
    );
    List<TransactionResponse> search(String query);
}
