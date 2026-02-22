package az.sarkhan.fintechsark.controller;

import az.sarkhan.fintechsark.dto.request.BulkTransactionRequest;
import az.sarkhan.fintechsark.dto.response.BankStatementRow;
import az.sarkhan.fintechsark.dto.response.TransactionResponse;
import az.sarkhan.fintechsark.enums.BankType;
import az.sarkhan.fintechsark.service.BankStatementService;
import az.sarkhan.fintechsark.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/bank-statement")
@RequiredArgsConstructor
public class BankStatementController {

    private final BankStatementService bankStatementService;
    private final TransactionService   transactionService;

    /**
     * 1-ci addım: Excel yüklə → parse et → preview qaytar
     * POST /api/bank-statement/parse?bank=ABB
     * Body: multipart/form-data  →  file = <excel.xlsx>
     */
    @PostMapping(value = "/parse", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<BankStatementRow>> parse(
            @RequestParam BankType bank,
            @RequestPart("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(bankStatementService.parse(bank, file));
    }

    /**
     * 2-ci addım: İstifadəçi popup-da təsdiqləyir → bulk save
     * POST /api/bank-statement/confirm
     * Body: { "transactions": [...] }
     * Response: { "saved": 12, "total": 12, "transactions": [...] }
     */
    @PostMapping("/confirm")
    public ResponseEntity<BulkSaveResponse> confirm(
            @Valid @RequestBody BulkTransactionRequest request) {
        List<TransactionResponse> saved = transactionService.bulkCreate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BulkSaveResponse(saved.size(), request.transactions().size(), saved));
    }

    public record BulkSaveResponse(
            int saved,
            int total,
            List<TransactionResponse> transactions
    ) {}
}
