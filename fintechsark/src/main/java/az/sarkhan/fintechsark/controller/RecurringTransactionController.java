package az.sarkhan.fintechsark.controller;

import az.sarkhan.fintechsark.dto.request.RecurringTransactionRequest;
import az.sarkhan.fintechsark.dto.response.RecurringTransactionResponse;
import az.sarkhan.fintechsark.service.RecurringTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringService;

    @PostMapping
    public ResponseEntity<RecurringTransactionResponse> create(
            @Valid @RequestBody RecurringTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recurringService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<RecurringTransactionResponse>> getAll() {
        return ResponseEntity.ok(recurringService.getAll());
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<RecurringTransactionResponse> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(recurringService.toggleActive(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recurringService.delete(id);
        return ResponseEntity.noContent().build();
    }
}