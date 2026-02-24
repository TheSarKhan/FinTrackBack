package az.sarkhan.fintechsark.service.impl;

import az.sarkhan.fintechsark.dto.response.BankStatementRow;
import az.sarkhan.fintechsark.entity.Category;
import az.sarkhan.fintechsark.enums.BankType;
import az.sarkhan.fintechsark.enums.TransactionType;
import az.sarkhan.fintechsark.exception.BusinessException;
import az.sarkhan.fintechsark.repository.CategoryRepository;
import az.sarkhan.fintechsark.security.SecurityUtils;
import az.sarkhan.fintechsark.service.BankStatementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BankStatementServiceImpl implements BankStatementService {

    private final CategoryRepository categoryRepository;
    private final SecurityUtils securityUtils;


    @Override
    public List<BankStatementRow> parse(BankType bankType, MultipartFile file) {
        try {
            ZipSecureFile.setMinInflateRatio(0.001);
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);
            List<Category> categories = categoryRepository.findAllVisibleToUser(
                    securityUtils.getCurrentUserId()
            );
            return switch (bankType) {
                case ABB        -> parseAbb(sheet, categories);
                case KAPITAL    -> parseKapital(sheet, categories);
                case RABITABANK -> parseRabitabank(sheet, categories);
                case XALQBANK   -> parseXalqbank(sheet, categories);
                case LEOBANK    -> parseLeobank(sheet, categories);
                case DOSTBANK -> parseDostbank(sheet, categories);

            };
        } catch (IOException e) {
            throw new BusinessException("Excel faylı oxunarkən xəta baş verdi: " + e.getMessage());
        } catch (Exception e) {
            throw new BusinessException("Fayl formatı dəstəklənmir: " + e.getMessage());
        }
    }

    private List<BankStatementRow> parseAbb(Sheet sheet, List<Category> categories) {
        List<BankStatementRow> rows = new ArrayList<>();

        int dataStartRow = -1;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            String cell1 = stringVal(row.getCell(1));
            if ("Tarix".equalsIgnoreCase(cell1.trim())) {
                dataStartRow = i + 1;
                break;
            }
        }

        if (dataStartRow == -1) return rows;

        for (int i = dataStartRow; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String dateStr   = stringVal(row.getCell(1));
            String fullDesc  = stringVal(row.getCell(2));
            BigDecimal income  = decimalVal(row.getCell(3));
            BigDecimal expense = decimalVal(row.getCell(4));

            if (dateStr.isBlank() && fullDesc.isBlank()) continue;

            LocalDate date = parseDate(row.getCell(1));
            if (date == null) continue;

            // Təsvirin ilk hissəsini götür (~-dən əvvəl)
            String cleanDesc = fullDesc.contains("~")
                    ? fullDesc.split("~")[0].trim()
                    : fullDesc.trim();

            TransactionType type;
            BigDecimal amount;

            if (expense.compareTo(BigDecimal.ZERO) > 0) {
                type   = TransactionType.EXPENSE;
                amount = expense;
            } else {
                type   = TransactionType.INCOME;
                amount = income;
            }

            // Kateqoriya təyin et
            Category cat = detectAbbCategory(cleanDesc, type, categories);
            Category parent = cat != null ? cat.getParent() : null;

            rows.add(new BankStatementRow(
                    date,
                    cleanDesc,
                    cat != null ? cat.getId() : null,
                    cat != null ? cat.getName() : "Bank Çıxarışı",
                    parent != null ? parent.getId()   : null,
                    parent != null ? parent.getName() : null,
                    type,
                    amount
            ));
        }

        return rows;
    }

    private Category detectAbbCategory(String desc, TransactionType type, List<Category> categories) {
        String d = desc.toUpperCase();

        String categoryName = detectAbbCategoryName(d, type);

        return categories.stream()
                .filter(c -> c.getName().equalsIgnoreCase(categoryName))
                .findFirst()
                .orElse(
                        categories.stream()
                                .filter(c -> c.getName().equalsIgnoreCase("Bank Çıxarışı"))
                                .findFirst()
                                .orElse(null)
                );
    }

    private String detectAbbCategoryName(String d, TransactionType type) {

        // ── Nəqliyyat ────────────────────────────────────────────────────────
        if (d.contains("TP METRO") || d.contains("TP BUS") ||
                d.contains("ADY RAILWAY") || d.contains("M10 TOP UP")) {
            return "İctimai Nəqliyyat";
        }

        // ── Restoran və Kafe ─────────────────────────────────────────────────
        if (d.contains("TURCO BURITTO") || d.contains("MA DESSERTS") ||
                d.contains("KUDO KUDO")     || d.contains("KFC") ||
                d.contains("BORANI")        || d.contains("KOMAGENE") ||
                d.contains("PIDEM")         || d.contains("KAFE") ||
                d.contains("RESTORAN")      || d.contains("MIX POINT") ||
                d.contains("KOFE EVI")      || d.contains("9 BAR") ||
                d.contains("OVEN 2")        || d.contains("TELMANIN KOZ") ||
                d.contains("WOLT")          || d.contains("WINGZ")) {
            return "Restoran və Kafe";
        }

        // ── Qida və Market ───────────────────────────────────────────────────
        if (d.contains("ERZAQ")   || d.contains("EMINEM MARKET") ||
                d.contains("FERMER")  || d.contains("BRAVO") ||
                d.contains("RAHAT")   || d.contains("OBA MARKET") ||
                d.contains("OBA NEF") || d.contains("NUR MARKET") ||
                d.contains("NURAY")   || d.contains("SAHIL MARKET") ||
                d.contains("BIRMARKET")|| d.contains("KOROGLU") ||
                d.contains("BOL BLUE")|| d.contains("BOL REBUS") ||
                d.contains("BAZARSTORE")|| d.contains("YATMARKA") ||
                d.contains("YARMARKA") || d.contains("MAGAZASI") ||
                d.contains("RAHАТ AILA")) {
            return "Supermarket";
        }

        // ── Geyim və Şəxsi ───────────────────────────────────────────────────
        if (d.contains("BERSHKA")    || d.contains("LC WAIKIKI") ||
                d.contains("NEW YORKER") || d.contains("GLORIA JEANS") ||
                d.contains("SKECHERS")   || d.contains("GUL MAGAZASI") ||
                d.contains("PARFUMERIYA")|| d.contains("AYSEL PERFUMES") ||
                d.contains("TRENDYOL")   || d.contains("TEMU") ||
                d.contains("DUAKS")      || d.contains("MPOS VVULZ")) {
            return "Geyim";
        }

        // ── Əyləncə ──────────────────────────────────────────────────────────
        if (d.contains("CINEMA")     || d.contains("KINOZALI") ||
                d.contains("ITICKET")    || d.contains("EPOINT") ||
                d.contains("SU IDMAN")   || d.contains("SPOTIFY") ||
                d.contains("STEAM")      || d.contains("GOOGLE") ||
                d.contains("APPLE.COM")  || d.contains("UDEMY") ||
                d.contains("PARKCINEMA") || d.contains("KINGSMART")) {
            return "Kino və Teatr";
        }

        // ── Kommunal / İnternet ──────────────────────────────────────────────
        if (d.contains("AZERCELL")  || d.contains("BAKCELL") ||
                d.contains("AZLINK")    || d.contains("IBA MOBILE") ||
                d.contains("HOSTINGER") || d.contains("PAYTР") ||
                d.contains("PAYTР O*")  || d.contains("OFISAIT") ||
                d.contains("JETSHR")) {
            return "İnternet";
        }

        // ── Kitab və Təhsil ──────────────────────────────────────────────────
        if (d.contains("KITAB EVI") || d.contains("ALI VE NINO") ||
                d.contains("ALI VA NINO")) {
            return "Kitab";
        }

        // ── Maliyyə Köçürməsi / Gəlir ────────────────────────────────────────
        if (d.contains("BIRBANK")     || d.contains("ASB P2P") ||
                d.contains("P2P SEND")    || d.contains("KARTA M") ||
                d.contains("IPS ILE C2C") || d.contains("CARDTOCARD") ||
                d.contains("C2C TO")      || d.contains("YELO BKM") ||
                d.contains("UFX C2C")     || d.contains("PULZ") ||
                d.contains("ANIPAY")      || d.contains("UNIBANK") ||
                d.contains("TERMINAL PAYMENT")) {
            return type == TransactionType.INCOME ? "Əlavə Gəlir" : "Bank Çıxarışı";
        }

        // ── Cashback / Faiz ──────────────────────────────────────────────────
        if (d.contains("FAIZ") || d.contains("CASHBACK") || d.contains("FAYDALI")) {
            return "Əlavə Gəlir";
        }

        // ── Default ──────────────────────────────────────────────────────────
        return "Bank Çıxarışı";
    }
    // ── LeoBank ───────────────────────────────────────────────────────────
// Format: Tarix | Təyinat | Məbləğ | Komissiya | Balans
// Tarix: dd-MM-yyyy HH:mm:ss  |  Məbləğ: mənfi → XƏRC, müsbət → GƏLİR
    private List<BankStatementRow> parseLeobank(Sheet sheet, List<Category> categories) {
        List<BankStatementRow> rows = new ArrayList<>();

        int dataStartRow = -1;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            String cell = stringVal(row.getCell(0));
            if ("Tarix".equalsIgnoreCase(cell.trim())) {
                dataStartRow = i + 1;
                break;
            }
        }

        if (dataStartRow == -1) return rows;

        for (int i = dataStartRow; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String dateStr    = stringVal(row.getCell(0));
            String description = stringVal(row.getCell(1)).trim();
            String amountStr  = stringVal(row.getCell(2));

            if (dateStr.isBlank()) continue;

            LocalDate date = parsLeobankDate(dateStr);
            if (date == null) continue;

            BigDecimal amount;
            try {
                amount = new BigDecimal(amountStr.replace(",", ".").replace(" ", ""));
            } catch (Exception e) {
                continue;
            }

            if (amount.compareTo(BigDecimal.ZERO) == 0) continue;

            TransactionType type;
            BigDecimal absAmount;

            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                type      = TransactionType.EXPENSE;
                absAmount = amount.abs();
            } else {
                type      = TransactionType.INCOME;
                absAmount = amount;
            }

            // Kateqoriya təyin et
            Category cat = detectLeobankCategory(description, type, categories);
            Category parent = cat != null ? cat.getParent() : null;

            rows.add(new BankStatementRow(
                    date,
                    description,
                    cat != null ? cat.getId() : null,
                    cat != null ? cat.getName() : "Bank Çıxarışı",
                    parent != null ? parent.getId()   : null,
                    parent != null ? parent.getName() : null,
                    type,
                    absAmount
            ));
        }

        return rows;
    }

    private Category detectLeobankCategory(String desc, TransactionType type, List<Category> categories) {
        String categoryName = detectLeobankCategoryName(desc.toUpperCase(), type);
        return categories.stream()
                .filter(c -> c.getName().equalsIgnoreCase(categoryName))
                .findFirst()
                .orElse(
                        categories.stream()
                                .filter(c -> c.getName().equalsIgnoreCase("Bank Çıxarışı"))
                                .findFirst()
                                .orElse(null)
                );
    }

    private String detectLeobankCategoryName(String d, TransactionType type) {

        // ── Nəqliyyat ────────────────────────────────────────────────────────
        if (d.contains("TP METRO") || d.contains("TP BUS") || d.contains("M10 TOP UP")) {
            return "İctimai Nəqliyyat";
        }

        // ── Kommunal / Telefon ───────────────────────────────────────────────
        if (d.contains("EMANAT")) {
            return "Telefon";
        }

        // ── Əyləncə / Abunəlik ───────────────────────────────────────────────
        if (d.contains("GOOGLE")) {
            return "Abunəlik";
        }

        // ── Maliyyə Köçürməsi / Gəlir ────────────────────────────────────────
        if (d.contains("BIRBANK")   || d.contains("DOSTBANK") ||
                d.contains("ANIPAY")    || d.contains("416973") ||
                d.contains("552209")) {
            return type == TransactionType.INCOME ? "Əlavə Gəlir" : "Bank Çıxarışı";
        }

        // ── Qənaət / Faiz ────────────────────────────────────────────────────
        if (d.contains("ARTIM") || d.contains("PROSTA") || d.contains("XƏZİNƏ")) {
            return type == TransactionType.INCOME ? "Əlavə Gəlir" : "Bank Çıxarışı";
        }

        // ── Şəxsdən gələn köçürmə (adlar) ────────────────────────────────────
        if (d.contains("KAMRAN")  || d.contains("FAZIL") ||
                d.contains("TOFIQ")   || d.contains("RƏVAN") ||
                d.contains("REVAN")) {
            return type == TransactionType.INCOME ? "Əlavə Gəlir" : "Bank Çıxarışı";
        }

        // ── Default ──────────────────────────────────────────────────────────
        return "Bank Çıxarışı";
    }

    private LocalDate parsLeobankDate(String dateStr) {
        try {
            // "23-01-2026 14:33:23" formatı
            String datePart = dateStr.trim().split(" ")[0]; // "23-01-2026"
            String[] parts  = datePart.split("-");
            return LocalDate.of(
                    Integer.parseInt(parts[2]), // il
                    Integer.parseInt(parts[1]), // ay
                    Integer.parseInt(parts[0])  // gün
            );
        } catch (Exception e) {
            log.warn("LeoBank tarix parse edilə bilmədi: {}", dateStr);
            return null;
        }
    }
    // ── DostBank ──────────────────────────────────────────────────────────
// Format: Əməliyyat tarixi | Tranzaksiya № | Məbləğ | Kateqoriya | Kart № | Təsvir | Komissiya | Əməliyyat növü
// Header: Row 6 (0-based), Data: Row 7-dən başlayır
// Tarix: "22.02.2026 19:04:42" → dd.MM.yyyy
// Əməliyyat növü: "Mədaxil" → GƏLİR, "Məxaric" → XƏRC
    private List<BankStatementRow> parseDostbank(Sheet sheet, List<Category> categories) {
        List<BankStatementRow> rows = new ArrayList<>();

        // Header sətirini dinamik tap
        int dataStartRow = -1;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            String cell = stringVal(row.getCell(1));
            if ("Əməliyyat tarixi".equalsIgnoreCase(cell.trim())) {
                dataStartRow = i + 1;
                break;
            }
        }

        if (dataStartRow == -1) return rows;

        Category cat = categories.stream()
                .filter(c -> c.getName().equalsIgnoreCase("Bank Çıxarışı"))
                .findFirst()
                .orElse(null);

        for (int i = dataStartRow; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String dateStr     = stringVal(row.getCell(1)); // "22.02.2026 19:04:42"
            String amountStr   = stringVal(row.getCell(3)); // "344.17 AZN"
            String description = stringVal(row.getCell(6)); // "www.birbank.az"
            String typeStr     = stringVal(row.getCell(8)); // "Mədaxil" / "Məxaric"

            if (dateStr.isBlank()) continue;

            // Tarix: "22.02.2026 19:04:42" → dd.MM.yyyy
            LocalDate date = parseDostbankDate(dateStr);
            if (date == null) continue;

            // Məbləğ: "344.17 AZN" → 344.17
            BigDecimal amount = parseDostbankAmount(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) == 0) continue;

            // Növ: "Mədaxil" → INCOME, "Məxaric" → EXPENSE
            TransactionType type = "Mədaxil".equalsIgnoreCase(typeStr.trim())
                    ? TransactionType.INCOME
                    : TransactionType.EXPENSE;

            Category parent = cat != null ? cat.getParent() : null;
            rows.add(new BankStatementRow(
                    date,
                    description.trim(),
                    cat != null ? cat.getId() : null,
                    "Bank Çıxarışı",
                    parent != null ? parent.getId()   : null,
                    parent != null ? parent.getName() : null,
                    type,
                    amount
            ));
        }

        return rows;
    }

    private LocalDate parseDostbankDate(String dateStr) {
        try {
            // "22.02.2026 19:04:42" → ilk hissəni götür
            String datePart = dateStr.trim().split(" ")[0]; // "22.02.2026"
            String[] parts  = datePart.split("\\.");
            return LocalDate.of(
                    Integer.parseInt(parts[2]), // il
                    Integer.parseInt(parts[1]), // ay
                    Integer.parseInt(parts[0])  // gün
            );
        } catch (Exception e) {
            log.warn("DostBank tarix parse edilə bilmədi: {}", dateStr);
            return null;
        }
    }

    private BigDecimal parseDostbankAmount(String amountStr) {
        try {
            // "344.17 AZN" → "344.17"  |  "2.99 USD" → "2.99"
            String cleaned = amountStr.trim().split(" ")[0].replace(",", ".");
            return new BigDecimal(cleaned);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
    // ── Kapital Bank ──────────────────────────────────────────────────────
    // Format: Tarix | Əməliyyat | Məbləğ | Valyuta | Növ | Açıqlama
    private List<BankStatementRow> parseKapital(Sheet sheet, List<Category> categories) {
        List<BankStatementRow> rows = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            LocalDate date     = parseDate(row.getCell(0));
            String description = stringVal(row.getCell(5));
            BigDecimal amount  = decimalVal(row.getCell(2)).abs();
            String typeStr     = stringVal(row.getCell(4)).toUpperCase();
            if (date == null || amount.compareTo(BigDecimal.ZERO) == 0) continue;
            TransactionType type = typeStr.contains("MƏXARİC") || typeStr.contains("DEBET")
                    ? TransactionType.EXPENSE : TransactionType.INCOME;
            rows.add(buildRow(date, description, amount, type, autoAssignCategory(description, type, categories)));
        }
        return rows;
    }

    // ── Rabitəbank ────────────────────────────────────────────────────────
    // Format: Tarix | Sənəd № | Açıqlama | Mədaxil | Məxaric | Qalıq
    private List<BankStatementRow> parseRabitabank(Sheet sheet, List<Category> categories) {
        List<BankStatementRow> rows = new ArrayList<>();
        for (int i = 2; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            LocalDate date     = parseDate(row.getCell(0));
            String description = stringVal(row.getCell(2));
            BigDecimal income  = decimalVal(row.getCell(3));
            BigDecimal expense = decimalVal(row.getCell(4));
            if (date == null || (income.compareTo(BigDecimal.ZERO) == 0 && expense.compareTo(BigDecimal.ZERO) == 0)) continue;
            TransactionType type;
            BigDecimal amount;
            if (income.compareTo(BigDecimal.ZERO) > 0) {
                type = TransactionType.INCOME;  amount = income;
            } else {
                type = TransactionType.EXPENSE; amount = expense;
            }
            rows.add(buildRow(date, description, amount, type, autoAssignCategory(description, type, categories)));
        }
        return rows;
    }

    // ── Xalq Bank ─────────────────────────────────────────────────────────
    // Format: Tarix | Açıqlama | Növ | Məbləğ
    private List<BankStatementRow> parseXalqbank(Sheet sheet, List<Category> categories) {
        List<BankStatementRow> rows = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            LocalDate date     = parseDate(row.getCell(0));
            String description = stringVal(row.getCell(1));
            String typeStr     = stringVal(row.getCell(2)).toUpperCase();
            BigDecimal amount  = decimalVal(row.getCell(3)).abs();
            if (date == null || amount.compareTo(BigDecimal.ZERO) == 0) continue;
            TransactionType type = typeStr.contains("MƏXARİC") || typeStr.contains("ÇIXIŞ")
                    ? TransactionType.EXPENSE : TransactionType.INCOME;
            rows.add(buildRow(date, description, amount, type, autoAssignCategory(description, type, categories)));
        }
        return rows;
    }

    // ── Azərpoçt ──────────────────────────────────────────────────────────
    // Format: Tarix | Məbləğ | Növ | Açıqlama
    private List<BankStatementRow> parseAzerpost(Sheet sheet, List<Category> categories) {
        List<BankStatementRow> rows = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            LocalDate date     = parseDate(row.getCell(0));
            BigDecimal amount  = decimalVal(row.getCell(1)).abs();
            String typeStr     = stringVal(row.getCell(2)).toUpperCase();
            String description = stringVal(row.getCell(3));
            if (date == null || amount.compareTo(BigDecimal.ZERO) == 0) continue;
            TransactionType type = typeStr.contains("DEBET") || typeStr.contains("MƏXARİC")
                    ? TransactionType.EXPENSE : TransactionType.INCOME;
            rows.add(buildRow(date, description, amount, type, autoAssignCategory(description, type, categories)));
        }
        return rows;
    }

    // ── Avtomatik kateqoriya təyini ───────────────────────────────────────
    private static final Map<String, List<String>> CATEGORY_KEYWORDS = new LinkedHashMap<>() {{
        put("Supermarket",     List.of("bravo", "bolmart", "araz", "bizim", "market", "supermarket", "ərzaq", "erzaq"));
        put("Restoran & Kafe", List.of("restoran", "kafe", "pizza", "burger", "çay", "coffee", "restaurant", "cafe"));
        put("Yanacaq",         List.of("socar", "bp", "lukoil", "azpetrol", "yanacaq", "fuel"));
        put("İnternet & TV",   List.of("internet", "aztelekom", "bakcell", "nar ", "azercell", "tv", "kabel"));
        put("Kommunal",        List.of("işıq", "qaz ", "su ", "kommunal", "azerenerji", "azərsu", "baku gas"));
        put("Maaş",            List.of("maaş", "əmək haqqı", "salary", "əmh"));
        put("Kredit Ödənişi",  List.of("kredit", "loan", "ipoteka", "borc"));
        put("Taksi",           List.of("uber", "bolt", "taksi", "taxi"));
        put("Aptek",           List.of("aptek", "dərman", "apteka", "pharmacy"));
        put("Abunəliklər",     List.of("netflix", "spotify", "youtube", "abunə", "subscription"));
        put("Hədiyyələr",      List.of("hədiyyə", "gift", "present"));
        put("Tibbi xərclər",   List.of("klinika", "xəstəxana", "hospital", "doctor", "həkim"));
        put("Təhsil",          List.of("kurs", "universit", "məktəb", "school", "education", "tədris"));
        put("Kirayə Gəliri",   List.of("kirayə", "rent", "icarə"));
    }};

    private Category autoAssignCategory(String description, TransactionType type, List<Category> categories) {
        if (description == null || description.isBlank()) return findFallback(type, categories);
        String lower = description.toLowerCase();
        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            boolean matches = entry.getValue().stream().anyMatch(lower::contains);
            if (!matches) continue;
            Optional<Category> found = categories.stream()
                    .filter(c -> c.getName().equalsIgnoreCase(entry.getKey()))
                    .findFirst();
            if (found.isPresent()) return found.get();
        }
        return findFallback(type, categories);
    }

    private Category findFallback(TransactionType type, List<Category> categories) {
        String name = type == TransactionType.INCOME ? "Digər Gəlir" : "Digər Xərclər";
        return categories.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(categories.isEmpty() ? null : categories.get(0));
    }

    // ── Köməkçi metodlar ─────────────────────────────────────────────────
    private BankStatementRow buildRow(LocalDate date, String description,
                                      BigDecimal amount, TransactionType type, Category cat) {
        if (cat == null) {
            return new BankStatementRow(date, description, null, "Bank Çıxarışı", null, null, type, amount);
        }
        Category parent = cat.getParent();
        return new BankStatementRow(date, description,
                cat.getId(), cat.getName(),
                parent != null ? parent.getId()   : null,
                parent != null ? parent.getName() : null,
                type, amount);
    }

    private LocalDate parseDate(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate();
            }
            String val = stringVal(cell).trim();
            if (val.isBlank()) return null;

            // yyyy-MM-dd formatı (ABB-nin formatı)
            if (val.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(val);
            }
            // dd.MM.yyyy formatı
            if (val.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
                String[] p = val.split("\\.");
                return LocalDate.of(Integer.parseInt(p[2]),
                        Integer.parseInt(p[1]),
                        Integer.parseInt(p[0]));
            }
            return null;
        } catch (Exception e) {
            log.warn("Tarix parse edilə bilmədi: {}", cell);
            return null;
        }
    }

    private BigDecimal decimalVal(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;
        try {
            if (cell.getCellType() == CellType.NUMERIC) return BigDecimal.valueOf(cell.getNumericCellValue());
            String val = cell.getStringCellValue().trim().replace(",", ".").replace(" ", "");
            return val.isBlank() ? BigDecimal.ZERO : new BigDecimal(val);
        } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private String stringVal(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default      -> "";
        };
    }
}
