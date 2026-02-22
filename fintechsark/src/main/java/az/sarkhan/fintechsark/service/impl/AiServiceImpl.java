package az.sarkhan.fintechsark.service.impl;

import az.sarkhan.fintechsark.dto.response.AiChatResponse;
import az.sarkhan.fintechsark.entity.Category;
import az.sarkhan.fintechsark.entity.Transaction;
import az.sarkhan.fintechsark.enums.TransactionType;
import az.sarkhan.fintechsark.repository.TransactionRepository;
import az.sarkhan.fintechsark.security.SecurityUtils;
import az.sarkhan.fintechsark.service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private final TransactionRepository transactionRepository;
    private final SecurityUtils securityUtils;

    @Value("${ai.openai.api-key:disabled}")
    private String openAiApiKey;

    @Override
    @Transactional(readOnly = true)
    public AiChatResponse chat(String userMessage) {
        Long userId = securityUtils.getCurrentUserId();
        LocalDate start = LocalDate.now().minusMonths(12).withDayOfMonth(1);
        LocalDate end = LocalDate.now();
        List<Transaction> transactions = transactionRepository.findCurrentMonthTransactions(userId, start, end);
        String analysis = buildRuleBasedAnalysis(transactions, userMessage);
        return new AiChatResponse(analysis, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public AiChatResponse analyzeCurrentMonth() {
        Long userId = securityUtils.getCurrentUserId();
        LocalDate start = LocalDate.now().minusMonths(12).withDayOfMonth(1);
        LocalDate end = LocalDate.now();
        List<Transaction> transactions = transactionRepository.findCurrentMonthTransactions(userId, start, end);
        String analysis = buildFullMonthAnalysis(transactions);
        return new AiChatResponse(analysis, LocalDateTime.now());
    }

    // ── Rule-based AI analysis ───────────────────────────────────────────

    private String buildRuleBasedAnalysis(List<Transaction> transactions, String userMessage) {
        String lowerMsg = userMessage.toLowerCase();

        if (!isFinanceRelated(lowerMsg)) {
            return handleSmallTalk(lowerMsg);
        }

        if (transactions.isEmpty()) {
            return "Bu ay hələ heç bir tranzaksiya qeydə alınmayıb. Xərc və gəlirlərinizi əlavə etdikdən sonra analiz edə bilərəm.";
        }
        if (transactions.isEmpty()) {
            return "Bu ay hələ heç bir tranzaksiya qeydə alınmayıb. Xərc və gəlirlərinizi əlavə etdikdən sonra analiz edə bilərəm.";
        }


        // Specific category check first
        String specificCategory = detectCategory(lowerMsg);
        if (specificCategory != null) {
            return buildSpecificCategoryAnalysis(transactions, specificCategory);
        }

        if (lowerMsg.contains("xərc") && (lowerMsg.contains("nə qədər") || lowerMsg.contains("neçə") || lowerMsg.contains("nə qədərdir"))) {
            return buildExpenseAnalysis(transactions);
        } else if (lowerMsg.contains("gəlir") || lowerMsg.contains("qazanc") || lowerMsg.contains("maaş")) {
            return buildIncomeAnalysis(transactions);
        } else if (lowerMsg.contains("tövsiyə") || lowerMsg.contains("advice") || lowerMsg.contains("nə etməliyəm") || lowerMsg.contains("necə qənaət")) {
            return buildAdviceAnalysis(transactions);
        } else if (lowerMsg.contains("trend") || lowerMsg.contains("artım") || lowerMsg.contains("azalma") || lowerMsg.contains("dəyişib")) {
            return buildTrendAnalysis(transactions);
        } else if (lowerMsg.contains("kateqoriya") || lowerMsg.contains("bölgü") || lowerMsg.contains("hara xərclədim")) {
            return buildCategoryAnalysis(transactions);
        } else if (lowerMsg.contains("balans") || lowerMsg.contains("qalıq") || lowerMsg.contains("nə qaldı")) {
            return buildBalanceAnalysis(transactions);
        } else if (lowerMsg.contains("ən çox") || lowerMsg.contains("max") || lowerMsg.contains("biggest")) {
            return buildTopExpenseAnalysis(transactions);
        } else if (lowerMsg.contains("ən az") || lowerMsg.contains("min") || lowerMsg.contains("az xərc")) {
            return buildMinExpenseAnalysis(transactions);
        } else {
            return buildFullMonthAnalysis(transactions);
        }
    }
    private boolean isFinanceRelated(String msg) {
        List<String> financeKeywords = List.of(
                "xərc", "gəlir", "pul", "manat", "azn", "balans", "qalıq",
                "qənaət", "borc", "kredit", "ödəniş", "maaş", "qazanc",
                "büdcə", "investisiya", "bank", "kateqoriya", "tranzaksiya",
                "analiz", "tövsiyə", "trend", "artım", "azalma", "faiz",
                "market", "restoran", "icarə", "yanacaq", "kommunal",
                "nəqliyyat", "sağlamlıq", "təhsil", "əyləncə", "hara",
                "nə qədər", "neçə", "ən çox", "ən az", "niyə", "hansı"
        );
        return financeKeywords.stream().anyMatch(msg::contains);
    }

    private String handleSmallTalk(String msg) {
        if (msg.contains("salam") || msg.contains("hello") || msg.contains("hi")) {
            return "Salam! 👋 Mən maliyyə köməkçinizəm. Xərcləriniz, gəlirləriniz və büdcəniz haqqında suallar verə bilərsiniz.\n\n" +
                    "Məsələn:\n" +
                    "• \"Bu ay nə qədər xərclədim?\"\n" +
                    "• \"Ən çox nəyə xərcləyirəm?\"\n" +
                    "• \"Mənə tövsiyə ver\"";
        }
        if (msg.contains("necəsən") || msg.contains("neces") || msg.contains("how are you")) {
            return "Yaxşıyam, təşəkkür edirəm! 😊 Maliyyənizi analiz etməyə hazıram.\n\n" +
                    "Büdcəniz haqqında nə bilmək istəyirsiniz?";
        }
        if (msg.contains("kimsin") || msg.contains("nəsən") || msg.contains("who are you")) {
            return "Mən sizin şəxsi maliyyə köməkçinizəm! 🤖\n\n" +
                    "Bacardıqlarım:\n" +
                    "• Xərc və gəlir analizi\n" +
                    "• Kateqoriya üzrə bölgü\n" +
                    "• Maliyyə tövsiyələri\n" +
                    "• Trend analizi";
        }
        if (msg.contains("təşəkkür") || msg.contains("sağ ol") || msg.contains("thanks")) {
            return "Buyurun! 😊 Başqa sualınız olarsa, məmnuniyyətlə kömək edərəm.";
        }
        if (msg.contains("əla") || msg.contains("super") || msg.contains("gözəl")) {
            return "Çox şadam! 😊 Maliyyənizlə bağlı başqa sualınız varmı?";
        }

        // Tanınmayan sual
        return "Üzr istəyirəm, bu sualı başa düşmədim. 🤔\n\n" +
                "Mən yalnız maliyyə mövzularında kömək edə bilərəm:\n" +
                "• \"Xərclərim nə qədərdir?\"\n" +
                "• \"Qidaya nə qədər xərclədim?\"\n" +
                "• \"Mənə tövsiyə ver\"";
    }
    private String detectCategory(String msg) {
        Map<String, List<String>> categoryKeywords = new LinkedHashMap<>();
        categoryKeywords.put("Qida və Market",      List.of("qida", "market", "yemək", "restoran", "kafe", "fast food", "supermarket", "ərzaq"));
        categoryKeywords.put("Nəqliyyat",           List.of("nəqliyyat", "taksi", "yanacaq", "benzin", "avtobus", "metro", "uber", "bolt", "maşın"));
        categoryKeywords.put("Yaşayış və Ev",       List.of("icarə", "ev", "yaşayış", "kirə", "ipoteka", "mənzil"));
        categoryKeywords.put("Kommunal Xidmətlər",  List.of("kommunal", "işıq", "elektrik", "qaz", "su", "internet", "telefon"));
        categoryKeywords.put("Sağlamlıq",           List.of("sağlamlıq", "həkim", "dərman", "aptək", "klinika", "tibb", "idman", "fitnes"));
        categoryKeywords.put("Təhsil və İnkişaf",   List.of("təhsil", "kurs", "kitab", "universitet", "məktəb", "öyrənmə"));
        categoryKeywords.put("Əyləncə",             List.of("əyləncə", "kino", "teatr", "konsert", "oyun", "səyahət", "turizm", "abunəlik"));
        categoryKeywords.put("Şəxsi Xərclər",       List.of("geyim", "paltar", "hədiyyə", "gözəllik", "baxım", "elektronika"));
        categoryKeywords.put("Maliyyə",             List.of("kredit", "borc", "bank", "investisiya", "faiz"));

        for (Map.Entry<String, List<String>> entry : categoryKeywords.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (msg.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private String buildSpecificCategoryAnalysis(List<Transaction> transactions, String categoryName) {
        List<Transaction> expenses = filterByType(transactions, TransactionType.EXPENSE);
        BigDecimal totalExpense = sumAmounts(expenses);

        List<Transaction> categoryTxns = expenses.stream()
                .filter(t -> {
                    Category cat = t.getCategory();
                    String parentName = cat.getParent() != null ? cat.getParent().getName() : cat.getName();
                    return parentName.equalsIgnoreCase(categoryName);
                })
                .collect(Collectors.toList());

        if (categoryTxns.isEmpty()) {
            return String.format("Bu ay %s kateqoriyasında heç bir xərciniz yoxdur.", categoryName);
        }

        BigDecimal categoryTotal = sumAmounts(categoryTxns);
        double pct = totalExpense.compareTo(BigDecimal.ZERO) == 0 ? 0
                : categoryTotal.divide(totalExpense, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📂 %s xərcləriniz:\n\n", categoryName));
        sb.append(String.format("  💰 Cəmi: %s AZN\n", format(categoryTotal)));
        sb.append(String.format("  📊 Ümumi xərclərin: %.1f%%\n\n", pct));

        // Subcategory breakdown
        Map<String, BigDecimal> bySub = new LinkedHashMap<>();
        for (Transaction t : categoryTxns) {
            bySub.merge(t.getCategory().getName(), t.getAmount(), BigDecimal::add);
        }

        if (bySub.size() > 1) {
            sb.append("  Alt kateqoriyalar:\n");
            bySub.entrySet().stream()
                    .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                    .forEach(e -> {
                        double subPct = e.getValue().divide(categoryTotal, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100)).doubleValue();
                        sb.append(String.format("    • %s: %s AZN (%.1f%%)\n",
                                e.getKey(), format(e.getValue()), subPct));
                    });
        }

        // Transaction list
        sb.append("\n  Son əməliyyatlar:\n");
        categoryTxns.stream().limit(5).forEach(t ->
                sb.append(String.format("    • %s — %s AZN (%s)\n",
                        t.getCategory().getName(), format(t.getAmount()), t.getDate())));

        return sb.toString();
    }

    private String buildBalanceAnalysis(List<Transaction> transactions) {
        BigDecimal totalIncome = sumByType(transactions, TransactionType.INCOME);
        BigDecimal totalExpense = sumByType(transactions, TransactionType.EXPENSE);
        BigDecimal net = totalIncome.subtract(totalExpense);

        StringBuilder sb = new StringBuilder("💼 Bu ayın balansı:\n\n");
        sb.append(String.format("  💰 Gəlir:  %s AZN\n", format(totalIncome)));
        sb.append(String.format("  💸 Xərc:   %s AZN\n", format(totalExpense)));
        sb.append(String.format("  📊 Qalıq:  %s AZN\n\n", format(net)));

        if (net.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("  ✅ Bu ay %s AZN qənaət etdiniz.", format(net)));
        } else if (net.compareTo(BigDecimal.ZERO) < 0) {
            sb.append(String.format("  ❗ Bu ay %s AZN kəsirle işlədiniz.", format(net.abs())));
        } else {
            sb.append("  ⚖️ Gəlir və xərcləriniz bərabərdir.");
        }
        return sb.toString();
    }

    private String buildTopExpenseAnalysis(List<Transaction> transactions) {
        List<Transaction> expenses = filterByType(transactions, TransactionType.EXPENSE);
        if (expenses.isEmpty()) return "Bu ay heç bir xərc tapılmadı.";

        Map<String, BigDecimal> byCategory = groupByParentCategory(expenses);
        Map.Entry<String, BigDecimal> top = byCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue()).orElse(null);

        BigDecimal total = sumAmounts(expenses);
        StringBuilder sb = new StringBuilder("🔝 Ən çox xərc etdiyiniz sahə:\n\n");

        if (top != null) {
            double pct = top.getValue().divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
            sb.append(String.format("  📌 %s: %s AZN (%.1f%%)\n\n", top.getKey(), format(top.getValue()), pct));
        }

        sb.append("  Bütün kateqoriyalar:\n");
        byCategory.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .forEach(e -> {
                    double pct = e.getValue().divide(total, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();
                    sb.append(String.format("    %s. %s: %s AZN (%.1f%%)\n",
                            byCategory.keySet().stream().toList().indexOf(e.getKey()) + 1,
                            e.getKey(), format(e.getValue()), pct));
                });
        return sb.toString();
    }

    private String buildMinExpenseAnalysis(List<Transaction> transactions) {
        List<Transaction> expenses = filterByType(transactions, TransactionType.EXPENSE);
        if (expenses.isEmpty()) return "Bu ay heç bir xərc tapılmadı.";

        Map<String, BigDecimal> byCategory = groupByParentCategory(expenses);
        BigDecimal total = sumAmounts(expenses);

        StringBuilder sb = new StringBuilder("📉 Ən az xərc etdiyiniz sahələr:\n\n");
        byCategory.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(e -> {
                    double pct = e.getValue().divide(total, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();
                    sb.append(String.format("  • %s: %s AZN (%.1f%%)\n", e.getKey(), format(e.getValue()), pct));
                });
        return sb.toString();
    }

    private String buildFullMonthAnalysis(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return "Bu ay hələ heç bir tranzaksiya tapılmadı.";
        }

        StringBuilder sb = new StringBuilder();
        String currentMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        sb.append("📊 ").append(currentMonth).append(" - Maliyyə Analizi\n\n");

        // Income/Expense totals
        BigDecimal totalIncome = sumByType(transactions, TransactionType.INCOME);
        BigDecimal totalExpense = sumByType(transactions, TransactionType.EXPENSE);
        BigDecimal net = totalIncome.subtract(totalExpense);

        sb.append("💰 Ümumi Gəlir: ").append(format(totalIncome)).append(" AZN\n");
        sb.append("💸 Ümumi Xərc: ").append(format(totalExpense)).append(" AZN\n");
        sb.append("📈 Xalis Balans: ").append(format(net)).append(" AZN\n\n");

        // Category breakdown
        sb.append(buildExpenseAnalysis(transactions));
        sb.append("\n");
        sb.append(buildAdviceAnalysis(transactions));

        return sb.toString();
    }

    private String buildExpenseAnalysis(List<Transaction> transactions) {
        List<Transaction> expenses = filterByType(transactions, TransactionType.EXPENSE);
        if (expenses.isEmpty()) return "Bu ay heç bir xərc qeyd edilməyib.\n";

        BigDecimal totalExpense = sumAmounts(expenses);
        Map<String, BigDecimal> byParentCategory = groupByParentCategory(expenses);

        StringBuilder sb = new StringBuilder("📊 Kateqoriya üzrə xərc bölgüsü:\n");

        byParentCategory.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .forEach(entry -> {
                    double pct = entry.getValue()
                            .divide(totalExpense, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                    sb.append(String.format("  • %s: %.1f%% (%s AZN)\n",
                            entry.getKey(), pct, format(entry.getValue())));
                });

        // Highlight top category
        byParentCategory.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .ifPresent(top -> {
                    double pct = top.getValue()
                            .divide(totalExpense, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                    sb.append(String.format(
                            "\n⚠️ Ən çox xərc edilən sahə: %s (xərclərinizin %.1f%%)\n",
                            top.getKey(), pct));
                });

        return sb.toString();
    }

    private String buildIncomeAnalysis(List<Transaction> transactions) {
        List<Transaction> incomes = filterByType(transactions, TransactionType.INCOME);
        if (incomes.isEmpty()) return "Bu ay heç bir gəlir qeyd edilməyib.\n";

        BigDecimal total = sumAmounts(incomes);
        Map<String, BigDecimal> byCategory = groupByParentCategory(incomes);

        StringBuilder sb = new StringBuilder("💰 Gəlir analizi:\n");
        sb.append("  Ümumi gəlir: ").append(format(total)).append(" AZN\n");
        sb.append("  Gəlir mənbələri:\n");
        byCategory.forEach((k, v) ->
                sb.append(String.format("    • %s: %s AZN\n", k, format(v))));

        return sb.toString();
    }

    private String buildAdviceAnalysis(List<Transaction> transactions) {
        List<Transaction> expenses = filterByType(transactions, TransactionType.EXPENSE);
        List<Transaction> incomes = filterByType(transactions, TransactionType.INCOME);

        if (expenses.isEmpty()) return "Tövsiyə vermək üçün kifayət qədər data yoxdur.";

        BigDecimal totalExpense = sumAmounts(expenses);
        BigDecimal totalIncome = sumAmounts(incomes);
        Map<String, BigDecimal> byParent = groupByParentCategory(expenses);

        StringBuilder sb = new StringBuilder("💡 Maliyyə Tövsiyələri:\n");
        int adviceCount = 0;

        // Savings rate advice
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal savingsRate = BigDecimal.ONE.subtract(
                    totalExpense.divide(totalIncome, 4, RoundingMode.HALF_UP));
            double savingsPct = savingsRate.doubleValue() * 100;

            if (savingsPct < 0) {
                sb.append("  ❗ Xərcləriniz gəlirinizdən çoxdur! Bu ay ").append(format(totalExpense.subtract(totalIncome)))
                        .append(" AZN kəsirlə işlədiniz. Dərhal xərcləri azaltmağı tövsiyə edirəm.\n");
            } else if (savingsPct < 10) {
                sb.append(String.format("  ⚠️ Qənaət nisbətiniz çox aşağıdır (%.1f%%). Aylıq gəlirin ən azı 20%%ini qənaət etməyi hədəfləyin.\n", savingsPct));
            } else if (savingsPct >= 20) {
                sb.append(String.format("  ✅ Əla! Gəlirinizdən %.1f%% qənaət edirsiniz. Bu yaxşı bir nisbətdir.\n", savingsPct));
            }
            adviceCount++;
        }

        // Category-specific advice
        byParent.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> {
                    double pct = entry.getValue()
                            .divide(totalExpense, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();

                    if (pct > 40) {
                        sb.append(String.format("  ⚠️ %s xərcləriniz ümumi xərcin %.1f%%ini təşkil edir. Bu yüksək bir nisbətdir, azaltmağı düşünün.\n",
                                entry.getKey(), pct));
                    } else if (entry.getKey().toLowerCase().contains("əyləncə") && pct > 20) {
                        sb.append(String.format("  💡 Əyləncə xərcləriniz %.1f%%dir. Orta hesabla 10-15%% olması tövsiyə olunur.\n", pct));
                    }
                });

        if (adviceCount == 0 && sb.toString().equals("💡 Maliyyə Tövsiyələri:\n")) {
            sb.append("  ✅ Maliyyəniz stabil görünür. Qənaət hədəflərinizi artırmağı düşünün.\n");
        }

        return sb.toString();
    }

    private String buildTrendAnalysis(List<Transaction> transactions) {
        // Simple trend based on date split (first half vs second half of month)
        List<Transaction> expenses = filterByType(transactions, TransactionType.EXPENSE);
        if (expenses.size() < 3) return "Trend analizi üçün kifayət qədər data yoxdur. Daha çox tranzaksiya əlavə edin.";

        int dayOfMonth = LocalDateTime.now().getDayOfMonth();
        int midpoint = 15;

        BigDecimal firstHalf = expenses.stream()
                .filter(t -> t.getDate().getDayOfMonth() <= midpoint)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal secondHalf = expenses.stream()
                .filter(t -> t.getDate().getDayOfMonth() > midpoint)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder sb = new StringBuilder("📈 Trend Analizi:\n");

        if (firstHalf.compareTo(BigDecimal.ZERO) > 0 && secondHalf.compareTo(BigDecimal.ZERO) > 0) {
            if (secondHalf.compareTo(firstHalf) > 0) {
                BigDecimal increase = secondHalf.subtract(firstHalf)
                        .divide(firstHalf, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                sb.append(String.format("  ⬆️ Ayın ikinci yarısında xərcləriniz %.1f%% artmışdır.\n", increase.doubleValue()));
            } else {
                BigDecimal decrease = firstHalf.subtract(secondHalf)
                        .divide(firstHalf, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                sb.append(String.format("  ⬇️ Xərcləriniz ayın sonlarına yaxın %.1f%% azalmışdır. Yaxşı nəzarət!\n", decrease.doubleValue()));
            }
        }

        // Category trends
        Map<String, BigDecimal> byCategory = groupByParentCategory(expenses);
        sb.append("\n  Kateqoriya üzrə xərc trendi:\n");
        byCategory.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(5)
                .forEach(e -> sb.append(String.format("    • %s: %s AZN\n", e.getKey(), format(e.getValue()))));

        return sb.toString();
    }

    private String buildCategoryAnalysis(List<Transaction> transactions) {
        List<Transaction> expenses = filterByType(transactions, TransactionType.EXPENSE);
        if (expenses.isEmpty()) return "Bu ay kateqoriya analizi üçün xərc tapılmadı.";

        BigDecimal total = sumAmounts(expenses);
        Map<String, Map<String, BigDecimal>> byParentAndSub = new LinkedHashMap<>();

        for (Transaction t : expenses) {
            Category cat = t.getCategory();
            String parentName = cat.getParent() != null ? cat.getParent().getName() : cat.getName();
            String subName = cat.getParent() != null ? cat.getName() : "Digər";
            byParentAndSub
                    .computeIfAbsent(parentName, k -> new LinkedHashMap<>())
                    .merge(subName, t.getAmount(), BigDecimal::add);
        }

        StringBuilder sb = new StringBuilder("🗂️ Ətraflı Kateqoriya Analizi:\n\n");
        byParentAndSub.forEach((parent, subs) -> {
            BigDecimal parentTotal = subs.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            double pct = parentTotal.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
            sb.append(String.format("📁 %s: %s AZN (%.1f%%)\n", parent, format(parentTotal), pct));
            subs.entrySet().stream()
                    .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                    .forEach(sub -> sb.append(String.format("   └─ %s: %s AZN\n", sub.getKey(), format(sub.getValue()))));
        });

        return sb.toString();
    }

    // ── Utility methods ──────────────────────────────────────────────────

    private List<Transaction> filterByType(List<Transaction> list, TransactionType type) {
        return list.stream().filter(t -> t.getType() == type).collect(Collectors.toList());
    }

    private BigDecimal sumAmounts(List<Transaction> list) {
        return list.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumByType(List<Transaction> list, TransactionType type) {
        return sumAmounts(filterByType(list, type));
    }

    private Map<String, BigDecimal> groupByParentCategory(List<Transaction> list) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Transaction t : list) {
            Category cat = t.getCategory();
            String parentName = cat.getParent() != null ? cat.getParent().getName() : cat.getName();
            result.merge(parentName, t.getAmount(), BigDecimal::add);
        }
        return result.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
    }

    private String format(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
