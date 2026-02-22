package az.sarkhan.fintechsark.service;

import az.sarkhan.fintechsark.dto.response.BankStatementRow;
import az.sarkhan.fintechsark.enums.BankType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BankStatementService {
    List<BankStatementRow> parse(BankType bankType, MultipartFile file);
}
