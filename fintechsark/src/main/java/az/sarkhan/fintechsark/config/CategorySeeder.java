package az.sarkhan.fintechsark.config;

import az.sarkhan.fintechsark.entity.Category;
import az.sarkhan.fintechsark.entity.Transaction;
import az.sarkhan.fintechsark.entity.User;
import az.sarkhan.fintechsark.enums.CategoryType;
import az.sarkhan.fintechsark.enums.TransactionType;
import az.sarkhan.fintechsark.repository.CategoryRepository;
import az.sarkhan.fintechsark.repository.TransactionRepository;
import az.sarkhan.fintechsark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategorySeeder implements ApplicationRunner {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User user = seedUser();
        seedCategories();
        seedTransactions(user);
    }

    // ── USER ─────────────────────────────────────────────────────────────
    private User seedUser() {
        if (userRepository.count() > 0) {
            log.info("İstifadəçi artıq mövcuddur.");
            return userRepository.findAll().get(0);
        }

        User user = User.builder()
                .name("Sarkhan Babayev")
                .email("serxan.babayev.06@gmail.com")
                .password(passwordEncoder.encode("salam123"))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);
        log.info("Demo istifadəçi yaradıldı: {}", user.getEmail());
        return user;
    }

    // ── KATEQORİYALAR ────────────────────────────────────────────────────
    private void seedCategories() {
        if (categoryRepository.countByUserIsNull() > 0) {
            log.info("Kateqoriyalar artıq mövcuddur.");
            return;
        }

        log.info("Sistem kateqoriyaları yaradılır...");

        Category gelir = save("Gəlir Mənbələri", CategoryType.INCOME, null);
        save("Maaş",        CategoryType.INCOME, gelir);
        save("Bonus",       CategoryType.INCOME, gelir);
        save("Freelance",   CategoryType.INCOME, gelir);
        save("Əlavə Gəlir", CategoryType.INCOME, gelir);
        save("Digər Gəlir", CategoryType.INCOME, gelir);

        Category yasayis = save("Yaşayış və Ev", CategoryType.EXPENSE, null);
        save("Mənzil İcarəsi",   CategoryType.EXPENSE, yasayis);
        save("İpoteka",          CategoryType.EXPENSE, yasayis);
        save("Ev Təmiri",        CategoryType.EXPENSE, yasayis);
        save("Mebel və Texnika", CategoryType.EXPENSE, yasayis);

        Category qida = save("Qida və Market", CategoryType.EXPENSE, null);
        save("Supermarket",      CategoryType.EXPENSE, qida);
        save("Restoran və Kafe", CategoryType.EXPENSE, qida);
        save("Fast Food",        CategoryType.EXPENSE, qida);
        save("Çatdırılma",       CategoryType.EXPENSE, qida);

        Category neqliyyat = save("Nəqliyyat", CategoryType.EXPENSE, null);
        save("Yanacaq",           CategoryType.EXPENSE, neqliyyat);
        save("Taksi",             CategoryType.EXPENSE, neqliyyat);
        save("İctimai Nəqliyyat", CategoryType.EXPENSE, neqliyyat);
        save("Avtomobil Xidməti", CategoryType.EXPENSE, neqliyyat);

        Category kommunal = save("Kommunal", CategoryType.EXPENSE, null);
        save("Elektrik", CategoryType.EXPENSE, kommunal);
        save("Qaz",      CategoryType.EXPENSE, kommunal);
        save("Su",       CategoryType.EXPENSE, kommunal);
        save("İnternet", CategoryType.EXPENSE, kommunal);
        save("Telefon",  CategoryType.EXPENSE, kommunal);

        Category saglamliq = save("Sağlamlıq", CategoryType.EXPENSE, null);
        save("Aptek",   CategoryType.EXPENSE, saglamliq);
        save("Həkim",   CategoryType.EXPENSE, saglamliq);
        save("Fitness", CategoryType.EXPENSE, saglamliq);
        save("Sığorta", CategoryType.EXPENSE, saglamliq);

        Category tehsil = save("Təhsil", CategoryType.EXPENSE, null);
        save("Kurs və Təlim", CategoryType.EXPENSE, tehsil);
        save("Kitab",         CategoryType.EXPENSE, tehsil);
        save("Universitet",   CategoryType.EXPENSE, tehsil);
        save("Məktəb",        CategoryType.EXPENSE, tehsil);

        Category eylence = save("Əyləncə", CategoryType.EXPENSE, null);
        save("Kino və Teatr", CategoryType.EXPENSE, eylence);
        save("Səyahət",       CategoryType.EXPENSE, eylence);
        save("Abunəlik",      CategoryType.EXPENSE, eylence);
        save("Oyun",          CategoryType.EXPENSE, eylence);

        Category geyim = save("Geyim və Şəxsi", CategoryType.EXPENSE, null);
        save("Geyim",         CategoryType.EXPENSE, geyim);
        save("Ayaqqabı",      CategoryType.EXPENSE, geyim);
        save("Hədiyyə",       CategoryType.EXPENSE, geyim);
        save("Gözəllik",      CategoryType.EXPENSE, geyim);
        save("Elektronika",   CategoryType.EXPENSE, geyim);
        save("Digər Xərclər", CategoryType.EXPENSE, geyim);

        Category bank = save("Bank", CategoryType.BOTH, null);
        save("Bank Çıxarışı", CategoryType.BOTH, bank);

        log.info("Kateqoriyalar uğurla yaradıldı.");
    }

    // ── TRANZAKSİYALAR ───────────────────────────────────────────────────
    private void seedTransactions(User user) {
        if (transactionRepository.count() > 0) {
            log.info("Tranzaksiyalar artıq mövcuddur.");
            return;
        }

        log.info("Demo tranzaksiyalar yaradılır...");

        // Kateqoriyaları ada görə yüklə
        Map<String, Category> cats = new java.util.HashMap<>();
        categoryRepository.findAll().forEach(c -> cats.put(c.getName(), c));

        Category maas        = cats.get("Maaş");
        Category bonus       = cats.get("Bonus");
        Category freelance   = cats.get("Freelance");
        Category elave       = cats.get("Əlavə Gəlir");
        Category icare       = cats.get("Mənzil İcarəsi");
        Category supermarket = cats.get("Supermarket");
        Category restoran    = cats.get("Restoran və Kafe");
        Category taksi       = cats.get("Taksi");
        Category yanacaq     = cats.get("Yanacaq");
        Category internet    = cats.get("İnternet");
        Category aptek       = cats.get("Aptek");
        Category fitness     = cats.get("Fitness");
        Category kurs        = cats.get("Kurs və Təlim");
        Category kitab       = cats.get("Kitab");
        Category kino        = cats.get("Kino və Teatr");
        Category seyahet     = cats.get("Səyahət");
        Category geyim       = cats.get("Geyim");
        Category ayaqqabi    = cats.get("Ayaqqabı");
        Category hediyye     = cats.get("Hədiyyə");
        Category elektron    = cats.get("Elektronika");
        Category mekteb      = cats.get("Məktəb");

        Object[][] data = {
                // Yanvar
                {2500.00, "Maaş - Yanvar",           "INCOME",  "2025-01-01", maas},
                {350.00,  "Mənzil icarəsi",           "EXPENSE", "2025-01-01", icare},
                {120.00,  "Bravo market",             "EXPENSE", "2025-01-02", supermarket},
                {45.00,   "Bolt taksi",               "EXPENSE", "2025-01-03", taksi},
                {28.00,   "Azərenerji yanvar",        "EXPENSE", "2025-01-04", internet},
                {15.00,   "Bakcell internet",         "EXPENSE", "2025-01-05", internet},
                {80.00,   "Restoran Firuza",          "EXPENSE", "2025-01-06", restoran},
                {200.00,  "Kurs ödənişi",             "EXPENSE", "2025-01-07", kurs},
                {55.00,   "Aptek dərman",             "EXPENSE", "2025-01-08", aptek},
                {30.00,   "Socar yanacaq",            "EXPENSE", "2025-01-09", yanacaq},
                {500.00,  "Freelance iş",             "INCOME",  "2025-01-10", freelance},
                {90.00,   "Köynək almaq",             "EXPENSE", "2025-01-11", geyim},
                {40.00,   "Kino bilet",               "EXPENSE", "2025-01-12", kino},
                // Fevral
                {2500.00, "Maaş - Fevral",            "INCOME",  "2025-02-01", maas},
                {350.00,  "Mənzil icarəsi",           "EXPENSE", "2025-02-01", icare},
                {145.00,  "Araz supermarket",         "EXPENSE", "2025-02-03", supermarket},
                {35.00,   "Metro bilet",              "EXPENSE", "2025-02-04", taksi},
                {28.00,   "Azərenerji fevral",        "EXPENSE", "2025-02-05", internet},
                {120.00,  "Sevgililer günü restoran", "EXPENSE", "2025-02-14", restoran},
                {60.00,   "Aptek dərman",             "EXPENSE", "2025-02-15", aptek},
                {300.00,  "Əlavə gəlir",              "INCOME",  "2025-02-16", elave},
                {75.00,   "Netflix, Spotify",         "EXPENSE", "2025-02-17", kino},
                {200.00,  "Ayaqqabı almaq",           "EXPENSE", "2025-02-20", ayaqqabi},
                {25.00,   "Bolt taksi",               "EXPENSE", "2025-02-22", taksi},
                {15.00,   "Bakcell internet",         "EXPENSE", "2025-02-25", internet},
                // Mart
                {2500.00, "Maaş - Mart",              "INCOME",  "2025-03-01", maas},
                {350.00,  "Mənzil icarəsi",           "EXPENSE", "2025-03-01", icare},
                {130.00,  "Bravo market",             "EXPENSE", "2025-03-02", supermarket},
                {50.00,   "Socar yanacaq",            "EXPENSE", "2025-03-05", yanacaq},
                {700.00,  "Bonus",                    "INCOME",  "2025-03-08", bonus},
                {85.00,   "Restoran Sahil",           "EXPENSE", "2025-03-09", restoran},
                {28.00,   "Azərenerji mart",          "EXPENSE", "2025-03-10", internet},
                {450.00,  "Telefon alışı",            "EXPENSE", "2025-03-12", elektron},
                {15.00,   "Bakcell internet",         "EXPENSE", "2025-03-15", internet},
                {40.00,   "Fitness club",             "EXPENSE", "2025-03-18", fitness},
                {100.00,  "Kitab alışı",              "EXPENSE", "2025-03-20", kitab},
                {30.00,   "Bolt taksi",               "EXPENSE", "2025-03-25", taksi},
                // Aprel
                {2500.00, "Maaş - Aprel",             "INCOME",  "2025-04-01", maas},
                {350.00,  "Mənzil icarəsi",           "EXPENSE", "2025-04-01", icare},
                {160.00,  "Araz supermarket",         "EXPENSE", "2025-04-03", supermarket},
                {28.00,   "Azərenerji aprel",         "EXPENSE", "2025-04-05", internet},
                {55.00,   "Restoran Pizza",           "EXPENSE", "2025-04-07", restoran},
                {400.00,  "Freelance iş",             "INCOME",  "2025-04-10", freelance},
                {15.00,   "Bakcell internet",         "EXPENSE", "2025-04-12", internet},
                {250.00,  "Paltar almaq",             "EXPENSE", "2025-04-15", geyim},
                {40.00,   "Fitness club",             "EXPENSE", "2025-04-18", fitness},
                {60.00,   "Bolt taksi",               "EXPENSE", "2025-04-20", taksi},
                {35.00,   "Kino bilet",               "EXPENSE", "2025-04-22", kino},
                {20.00,   "Socar yanacaq",            "EXPENSE", "2025-04-25", yanacaq},
                // May
                {2500.00, "Maaş - May",               "INCOME",  "2025-05-01", maas},
                {350.00,  "Mənzil icarəsi",           "EXPENSE", "2025-05-01", icare},
                {140.00,  "Bravo market",             "EXPENSE", "2025-05-02", supermarket},
                {28.00,   "Azərenerji may",           "EXPENSE", "2025-05-05", internet},
                {15.00,   "Bakcell internet",         "EXPENSE", "2025-05-08", internet},
                {90.00,   "Restoran Nəriman",         "EXPENSE", "2025-05-10", restoran},
                {600.00,  "Bonus may",                "INCOME",  "2025-05-12", bonus},
                {300.00,  "Səyahət xərci",            "EXPENSE", "2025-05-15", seyahet},
                {40.00,   "Fitness club",             "EXPENSE", "2025-05-18", fitness},
                {75.00,   "Aptek dərman",             "EXPENSE", "2025-05-20", aptek},
                {45.00,   "Bolt taksi",               "EXPENSE", "2025-05-22", taksi},
                {120.00,  "Elektronika alışı",        "EXPENSE", "2025-05-25", elektron},
                // İyun
                {2500.00, "Maaş - İyun",              "INCOME",  "2025-06-01", maas},
                {350.00,  "Mənzil icarəsi",           "EXPENSE", "2025-06-01", icare},
                {155.00,  "Araz supermarket",         "EXPENSE", "2025-06-03", supermarket},
                {28.00,   "Azərenerji iyun",          "EXPENSE", "2025-06-05", internet},
                {500.00,  "Freelance iş",             "INCOME",  "2025-06-08", freelance},
                {15.00,   "Bakcell internet",         "EXPENSE", "2025-06-10", internet},
                {200.00,  "Məzuniyyət səyahəti",      "EXPENSE", "2025-06-15", seyahet},
                {40.00,   "Fitness club",             "EXPENSE", "2025-06-18", fitness},
                {70.00,   "Restoran Sahil",           "EXPENSE", "2025-06-20", restoran},
                {35.00,   "Socar yanacaq",            "EXPENSE", "2025-06-22", yanacaq},
                {180.00,  "Geyim almaq",              "EXPENSE", "2025-06-25", geyim},
                {50.00,   "Kino və əyləncə",          "EXPENSE", "2025-06-28", kino},
                // İyul
                {2500.00, "Maaş - İyul",              "INCOME",  "2025-07-01", maas},
                {350.00,  "Mənzil icarəsi",           "EXPENSE", "2025-07-01", icare},
                {170.00,  "Bravo market",             "EXPENSE", "2025-07-02", supermarket},
                {28.00,   "Azərenerji iyul",          "EXPENSE", "2025-07-05", internet},
                {15.00,   "Bakcell internet",         "EXPENSE", "2025-07-08", internet},
                {400.00,  "Əlavə gəlir",              "INCOME",  "2025-07-10", elave},
                {500.00,  "Məzuniyyət xərcləri",      "EXPENSE", "2025-07-12", seyahet},
                {40.00,   "Fitness club",             "EXPENSE", "2025-07-15", fitness},
                {95.00,   "Restoran Pizza",           "EXPENSE", "2025-07-18", restoran},
                {60.00,   "Bolt taksi",               "EXPENSE", "2025-07-20", taksi},
                {150.00,  "Ayaqqabı almaq",           "EXPENSE", "2025-07-22", ayaqqabi},
                {45.00,   "Aptek dərman",             "EXPENSE", "2025-07-25", aptek},
                // Avqust
                {2500.00, "Maaş - Avqust",            "INCOME",  "2025-08-01", maas},
                {350.00,  "Mənzil icarəsi",           "EXPENSE", "2025-08-01", icare},
                {160.00,  "Araz supermarket",         "EXPENSE", "2025-08-03", supermarket},
                {28.00,   "Azərenerji avqust",        "EXPENSE", "2025-08-05", internet},
                {800.00,  "Bonus avqust",             "INCOME",  "2025-08-08", bonus},
                {15.00,   "Bakcell internet",         "EXPENSE", "2025-08-10", internet},
                {40.00,   "Fitness club",             "EXPENSE", "2025-08-12", fitness},
                {80.00,   "Restoran Nəriman",         "EXPENSE", "2025-08-15", restoran},
                {55.00,   "Socar yanacaq",            "EXPENSE", "2025-08-18", yanacaq},
                {300.00,  "Məktəb ləvazimatı",        "EXPENSE", "2025-08-20", mekteb},
                {200.00,  "Paltar almaq",             "EXPENSE", "2025-08-22", geyim},
                {35.00,   "Kino bilet",               "EXPENSE", "2025-08-25", kino},
                // Sentyabr
                {2500.00, "Maaş - Sentyabr",          "INCOME",  "2025-09-01", maas},
                {350.00,  "Mənzil icarəsi",           "EXPENSE", "2025-09-01", icare},
                {140.00,  "Bravo market",             "EXPENSE", "2025-09-02", supermarket},
                {28.00,   "Azərenerji sentyabr",      "EXPENSE", "2025-09-05", internet},
                {15.00,   "Bakcell internet",         "EXPENSE", "2025-09-08", internet},
                {500.00,  "Freelance iş",             "INCOME",  "2025-09-10", freelance},
                {250.00,  "Kurs ödənişi",             "EXPENSE", "2025-09-12", kurs},
                {40.00,   "Fitness club",             "EXPENSE", "2025-09-15", fitness},
                {75.00,   "Restoran Firuza",          "EXPENSE", "2025-09-18", restoran},
                {45.00,   "Bolt taksi",               "EXPENSE", "2025-09-20", taksi},
                {90.00,   "Aptek dərman",             "EXPENSE", "2025-09-22", aptek},
                {180.00,  "Elektronika alışı",        "EXPENSE", "2025-09-25", elektron},
                // Oktyabr
                {2500.00, "Maaş - Oktyabr",           "INCOME",  "2025-10-01", maas},
                {350.00,  "Mənzil icarəsi",           "EXPENSE", "2025-10-01", icare},
                {155.00,  "Araz supermarket",         "EXPENSE", "2025-10-03", supermarket},
                {28.00,   "Azərenerji oktyabr",       "EXPENSE", "2025-10-05", internet},
                {600.00,  "Bonus oktyabr",            "INCOME",  "2025-10-08", bonus},
                {15.00,   "Bakcell internet",         "EXPENSE", "2025-10-10", internet},
                {40.00,   "Fitness club",             "EXPENSE", "2025-10-12", fitness},
                {85.00,   "Restoran Sahil",           "EXPENSE", "2025-10-15", restoran},
                {50.00,   "Socar yanacaq",            "EXPENSE", "2025-10-18", yanacaq},
                {300.00,  "Paltar almaq",             "EXPENSE", "2025-10-20", geyim},
                {60.00,   "Kino və əyləncə",          "EXPENSE", "2025-10-22", kino},
                {70.00,   "Aptek dərman",             "EXPENSE", "2025-10-25", aptek},
                // Noyabr
                {2500.00, "Maaş - Noyabr",            "INCOME",  "2025-11-01", maas},
                {350.00,  "Mənzil icarəsi",           "EXPENSE", "2025-11-01", icare},
                {165.00,  "Bravo market",             "EXPENSE", "2025-11-02", supermarket},
                {28.00,   "Azərenerji noyabr",        "EXPENSE", "2025-11-05", internet},
                {15.00,   "Bakcell internet",         "EXPENSE", "2025-11-08", internet},
                {400.00,  "Freelance iş",             "INCOME",  "2025-11-10", freelance},
                {40.00,   "Fitness club",             "EXPENSE", "2025-11-12", fitness},
                {95.00,   "Restoran Nəriman",         "EXPENSE", "2025-11-15", restoran},
                {200.00,  "Kurs ödənişi",             "EXPENSE", "2025-11-18", kurs},
                {55.00,   "Bolt taksi",               "EXPENSE", "2025-11-20", taksi},
                {150.00,  "Hədiyyə almaq",            "EXPENSE", "2025-11-22", hediyye},
                {45.00,   "Socar yanacaq",            "EXPENSE", "2025-11-25", yanacaq},
                // Dekabr
                {2500.00, "Maaş - Dekabr",            "INCOME",  "2025-12-01", maas},
                {350.00,  "Mənzil icarəsi",           "EXPENSE", "2025-12-01", icare},
                {200.00,  "Araz supermarket",         "EXPENSE", "2025-12-03", supermarket},
                {28.00,   "Azərenerji dekabr",        "EXPENSE", "2025-12-05", internet},
                {1000.00, "İllik bonus",              "INCOME",  "2025-12-08", bonus},
                {15.00,   "Bakcell internet",         "EXPENSE", "2025-12-10", internet},
                {400.00,  "Yeni il hədiyyələri",      "EXPENSE", "2025-12-15", hediyye},
                {40.00,   "Fitness club",             "EXPENSE", "2025-12-18", fitness},
                {150.00,  "Restoran Yeni il",         "EXPENSE", "2025-12-20", restoran},
                {60.00,   "Socar yanacaq",            "EXPENSE", "2025-12-22", yanacaq},
                {250.00,  "Paltar almaq",             "EXPENSE", "2025-12-24", geyim},
                {80.00,   "Kino və əyləncə",          "EXPENSE", "2025-12-28", kino},
                {500.00,  "Freelance iş",             "INCOME",  "2025-12-30", freelance},
        };

        for (Object[] row : data) {
            Transaction t = Transaction.builder()
                    .amount(BigDecimal.valueOf((Double) row[0]))
                    .description((String) row[1])
                    .type(TransactionType.valueOf((String) row[2]))
                    .date(LocalDate.parse((String) row[3]))
                    .user(user)
                    .category((Category) row[4])
                    .isDeleted(false)
                    .build();
            transactionRepository.save(t);
        }

        log.info("Demo tranzaksiyalar uğurla yaradıldı.");
    }

    private Category save(String name, CategoryType type, Category parent) {
        Category category = Category.builder()
                .name(name)
                .type(type)
                .parent(parent)
                .user(null)
                .isActive(true)
                .build();
        return categoryRepository.save(category);
    }
}