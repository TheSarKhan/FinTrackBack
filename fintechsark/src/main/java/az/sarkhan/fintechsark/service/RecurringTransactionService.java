package az.sarkhan.fintechsark.service;

import az.sarkhan.fintechsark.dto.request.RecurringTransactionRequest;
import az.sarkhan.fintechsark.dto.response.RecurringTransactionResponse;

import java.util.List;

public interface RecurringTransactionService {
    RecurringTransactionResponse create(RecurringTransactionRequest request);
    List<RecurringTransactionResponse> getAll();
    RecurringTransactionResponse toggleActive(Long id);
    void delete(Long id);
    void executeAllDue(); // Scheduler çağırır
}