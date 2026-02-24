package az.sarkhan.fintechsark.config;

import az.sarkhan.fintechsark.entity.Category;
import az.sarkhan.fintechsark.enums.CategoryType;
import az.sarkhan.fintechsark.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategorySeeder implements ApplicationRunner {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (categoryRepository.countByUserIsNull() > 0) {
            log.info("Kateqoriyalar artıq mövcuddur, seeding atlanır.");
            return;
        }

        log.info("Sistem kateqoriyaları yaradılır...");
        seedCategories();
        log.info("Kateqoriyalar uğurla yaradıldı.");
    }

    private void seedCategories() {

        // ── GƏLİR ANA KATEQORİYALARI ────────────────────────────────────
        Category gelirMenbeleri = save("Gəlir Mənbələri", CategoryType.INCOME, null);
        save("Maaş",          CategoryType.INCOME, gelirMenbeleri);
        save("Bonus",         CategoryType.INCOME, gelirMenbeleri);
        save("Freelance",     CategoryType.INCOME, gelirMenbeleri);
        save("Əlavə Gəlir",   CategoryType.INCOME, gelirMenbeleri);
        save("Digər Gəlir",   CategoryType.INCOME, gelirMenbeleri);

        // ── XƏRCLƏr ANA KATEQORİYALARI ──────────────────────────────────
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
        save("Yanacaq",            CategoryType.EXPENSE, neqliyyat);
        save("Taksi",              CategoryType.EXPENSE, neqliyyat);
        save("İctimai Nəqliyyat",  CategoryType.EXPENSE, neqliyyat);
        save("Avtomobil Xidməti",  CategoryType.EXPENSE, neqliyyat);

        Category kommunal = save("Kommunal", CategoryType.EXPENSE, null);
        save("Elektrik",  CategoryType.EXPENSE, kommunal);
        save("Qaz",       CategoryType.EXPENSE, kommunal);
        save("Su",        CategoryType.EXPENSE, kommunal);
        save("İnternet",  CategoryType.EXPENSE, kommunal);
        save("Telefon",   CategoryType.EXPENSE, kommunal);

        Category saglamliq = save("Sağlamlıq", CategoryType.EXPENSE, null);
        save("Aptek",    CategoryType.EXPENSE, saglamliq);
        save("Həkim",    CategoryType.EXPENSE, saglamliq);
        save("Fitness",  CategoryType.EXPENSE, saglamliq);
        save("Sığorta",  CategoryType.EXPENSE, saglamliq);

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
        save("Geyim",          CategoryType.EXPENSE, geyim);
        save("Ayaqqabı",       CategoryType.EXPENSE, geyim);
        save("Hədiyyə",        CategoryType.EXPENSE, geyim);
        save("Gözəllik",       CategoryType.EXPENSE, geyim);
        save("Elektronika",    CategoryType.EXPENSE, geyim);
        save("Digər Xərclər",  CategoryType.EXPENSE, geyim);

        // ── BANK ANA KATEQORİYASI ────────────────────────────────────────
        Category bank = save("Bank", CategoryType.BOTH, null);
        save("Bank Çıxarışı", CategoryType.BOTH, bank);
    }

    private Category save(String name, CategoryType type, Category parent) {
        Category category = Category.builder()
                .name(name)
                .type(type)
                .parent(parent)
                .user(null) // system category
                .isActive(true)
                .build();
        return categoryRepository.save(category);
    }
}