-- KATEQORIYALAR


INSERT INTO categories (name, type, parent_id, user_id, is_active, created_at) VALUES

-- ── Ana kateqoriyalar (parent_id = NULL, user_id = NULL = system) ──────
('Gəlir Mənbələri',    'INCOME',  NULL, NULL, true, NOW()),  -- 1
('Yaşayış və Ev',      'EXPENSE', NULL, NULL, true, NOW()),  -- 2
('Qida və Market',     'EXPENSE', NULL, NULL, true, NOW()),  -- 3
('Nəqliyyat',          'EXPENSE', NULL, NULL, true, NOW()),  -- 4
('Kommunal',           'EXPENSE', NULL, NULL, true, NOW()),  -- 5
('Sağlamlıq',          'EXPENSE', NULL, NULL, true, NOW()),  -- 6
('Təhsil',             'EXPENSE', NULL, NULL, true, NOW()),  -- 7
('Əyləncə',            'EXPENSE', NULL, NULL, true, NOW()),  -- 8
('Geyim və Şəxsi',     'EXPENSE', NULL, NULL, true, NOW()),  -- 9
('Bank',               'BOTH',    NULL, NULL, true, NOW()),  -- 10

-- ── Gəlir Mənbələri alt kateqoriyaları ───────────────────────────────
('Maaş',               'INCOME',  1, NULL, true, NOW()),     -- 11
('Bonus',              'INCOME',  1, NULL, true, NOW()),     -- 12
('Freelance',          'INCOME',  1, NULL, true, NOW()),     -- 13
('Əlavə Gəlir',        'INCOME',  1, NULL, true, NOW()),     -- 14
('Digər Gəlir',        'INCOME',  1, NULL, true, NOW()),     -- 15

-- ── Yaşayış və Ev alt kateqoriyaları ─────────────────────────────────
('Mənzil İcarəsi',     'EXPENSE', 2, NULL, true, NOW()),     -- 16
('İpoteka',            'EXPENSE', 2, NULL, true, NOW()),     -- 17
('Ev Təmiri',          'EXPENSE', 2, NULL, true, NOW()),     -- 18
('Mebel və Texnika',   'EXPENSE', 2, NULL, true, NOW()),     -- 19

-- ── Qida və Market alt kateqoriyaları ────────────────────────────────
('Supermarket',        'EXPENSE', 3, NULL, true, NOW()),     -- 20
('Restoran və Kafe',   'EXPENSE', 3, NULL, true, NOW()),     -- 21
('Fast Food',          'EXPENSE', 3, NULL, true, NOW()),     -- 22
('Çatdırılma',         'EXPENSE', 3, NULL, true, NOW()),     -- 23

-- ── Nəqliyyat alt kateqoriyaları ─────────────────────────────────────
('Yanacaq',            'EXPENSE', 4, NULL, true, NOW()),     -- 24
('Taksi',              'EXPENSE', 4, NULL, true, NOW()),     -- 25
('İctimai Nəqliyyat',  'EXPENSE', 4, NULL, true, NOW()),     -- 26
('Avtomobil Xidməti',  'EXPENSE', 4, NULL, true, NOW()),     -- 27

-- ── Kommunal alt kateqoriyaları ───────────────────────────────────────
('Elektrik',           'EXPENSE', 5, NULL, true, NOW()),     -- 28
('Qaz',                'EXPENSE', 5, NULL, true, NOW()),     -- 29
('Su',                 'EXPENSE', 5, NULL, true, NOW()),     -- 30
('İnternet',           'EXPENSE', 5, NULL, true, NOW()),     -- 31
('Telefon',            'EXPENSE', 5, NULL, true, NOW()),     -- 32

-- ── Sağlamlıq alt kateqoriyaları ─────────────────────────────────────
('Aptek',              'EXPENSE', 6, NULL, true, NOW()),     -- 33
('Həkim',              'EXPENSE', 6, NULL, true, NOW()),     -- 34
('Fitness',            'EXPENSE', 6, NULL, true, NOW()),     -- 35
('Sığorta',            'EXPENSE', 6, NULL, true, NOW()),     -- 36

-- ── Təhsil alt kateqoriyaları ─────────────────────────────────────────
('Kurs və Təlim',      'EXPENSE', 7, NULL, true, NOW()),     -- 37
('Kitab',              'EXPENSE', 7, NULL, true, NOW()),     -- 38
('Universitet',        'EXPENSE', 7, NULL, true, NOW()),     -- 39
('Məktəb',             'EXPENSE', 7, NULL, true, NOW()),     -- 40

-- ── Əyləncə alt kateqoriyaları ───────────────────────────────────────
('Kino və Teatr',      'EXPENSE', 8, NULL, true, NOW()),     -- 41
('Səyahət',            'EXPENSE', 8, NULL, true, NOW()),     -- 42
('Abunəlik',           'EXPENSE', 8, NULL, true, NOW()),     -- 43
('Oyun',               'EXPENSE', 8, NULL, true, NOW()),     -- 44

-- ── Geyim və Şəxsi alt kateqoriyaları ────────────────────────────────
('Geyim',              'EXPENSE', 9, NULL, true, NOW()),     -- 45
('Ayaqqabı',           'EXPENSE', 9, NULL, true, NOW()),     -- 46
('Hədiyyə',            'EXPENSE', 9, NULL, true, NOW()),     -- 47
('Gözəllik',           'EXPENSE', 9, NULL, true, NOW()),     -- 48
('Elektronika',        'EXPENSE', 9, NULL, true, NOW()),     -- 49
('Digər Xərclər',      'EXPENSE', 9, NULL, true, NOW()),     -- 50

-- ── Bank alt kateqoriyası ─────────────────────────────────────────────
('Bank Çıxarışı',      'BOTH',    10, NULL, true, NOW());    -- 51
-- Reset sequences
SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));


-- ============================================================
-- 150 rows of transaction seed data (1 year, user_id = 1)
-- Run AFTER registering a user (user_id=1)
-- ============================================================
-- Bütün user_id = 1 olanları real ID ilə əvəz et
-- Aşağıdakı sorğunu işlətməzdən əvvəl öz ID-ni yaz

DO $$
DECLARE
    real_user_id BIGINT;
BEGIN
    SELECT id INTO real_user_id FROM users LIMIT 1;

    INSERT INTO transactions (amount, description, type, date, user_id, category_id, is_deleted, created_at) VALUES
    (2500.00, 'Maaş - Yanvar',           'INCOME',  '2025-01-01', real_user_id, 1,  false, '2025-01-01 09:00:00'),
    (350.00,  'Mənzil icarəsi',           'EXPENSE', '2025-01-01', real_user_id, 5,  false, '2025-01-01 10:00:00'),
    (120.00,  'Bravo market',             'EXPENSE', '2025-01-02', real_user_id, 8,  false, '2025-01-02 11:00:00'),
    (45.00,   'Bolt taksi',               'EXPENSE', '2025-01-03', real_user_id, 12, false, '2025-01-03 08:30:00'),
    (28.00,   'Azərenerji yanvar',        'EXPENSE', '2025-01-04', real_user_id, 15, false, '2025-01-04 12:00:00'),
    (15.00,   'Bakcell internet',         'EXPENSE', '2025-01-05', real_user_id, 15, false, '2025-01-05 09:00:00'),
    (80.00,   'Restoran Firuza',          'EXPENSE', '2025-01-06', real_user_id, 8,  false, '2025-01-06 19:00:00'),
    (200.00,  'Kurs ödənişi',             'EXPENSE', '2025-01-07', real_user_id, 20, false, '2025-01-07 10:00:00'),
    (55.00,   'Aptek dərman',             'EXPENSE', '2025-01-08', real_user_id, 18, false, '2025-01-08 11:00:00'),
    (30.00,   'Socar yanacaq',            'EXPENSE', '2025-01-09', real_user_id, 12, false, '2025-01-09 07:30:00'),
    (500.00,  'Freelance iş',             'INCOME',  '2025-01-10', real_user_id, 2,  false, '2025-01-10 14:00:00'),
    (90.00,   'Köynək almaq',             'EXPENSE', '2025-01-11', real_user_id, 22, false, '2025-01-11 15:00:00'),
    (40.00,   'Kino bilet',               'EXPENSE', '2025-01-12', real_user_id, 25, false, '2025-01-12 17:00:00'),
    (2500.00, 'Maaş - Fevral',            'INCOME',  '2025-02-01', real_user_id, 1,  false, '2025-02-01 09:00:00'),
    (350.00,  'Mənzil icarəsi',           'EXPENSE', '2025-02-01', real_user_id, 5,  false, '2025-02-01 10:00:00'),
    (145.00,  'Araz supermarket',         'EXPENSE', '2025-02-03', real_user_id, 8,  false, '2025-02-03 11:00:00'),
    (35.00,   'Metro bilet',              'EXPENSE', '2025-02-04', real_user_id, 12, false, '2025-02-04 08:00:00'),
    (28.00,   'Azərenerji fevral',        'EXPENSE', '2025-02-05', real_user_id, 15, false, '2025-02-05 12:00:00'),
    (120.00,  'Sevgililer günü restoran', 'EXPENSE', '2025-02-14', real_user_id, 8,  false, '2025-02-14 20:00:00'),
    (60.00,   'Aptey dərman',             'EXPENSE', '2025-02-15', real_user_id, 18, false, '2025-02-15 11:00:00'),
    (300.00,  'Əlavə gəlir',              'INCOME',  '2025-02-16', real_user_id, 2,  false, '2025-02-16 14:00:00'),
    (75.00,   'Netflix, Spotify',         'EXPENSE', '2025-02-17', real_user_id, 25, false, '2025-02-17 10:00:00'),
    (200.00,  'Ayaqqabı almaq',           'EXPENSE', '2025-02-20', real_user_id, 22, false, '2025-02-20 15:00:00'),
    (25.00,   'Bolt taksi',               'EXPENSE', '2025-02-22', real_user_id, 12, false, '2025-02-22 18:00:00'),
    (15.00,   'Bakcell internet',         'EXPENSE', '2025-02-25', real_user_id, 15, false, '2025-02-25 09:00:00'),
    (2500.00, 'Maaş - Mart',              'INCOME',  '2025-03-01', real_user_id, 1,  false, '2025-03-01 09:00:00'),
    (350.00,  'Mənzil icarəsi',           'EXPENSE', '2025-03-01', real_user_id, 5,  false, '2025-03-01 10:00:00'),
    (130.00,  'Bravo market',             'EXPENSE', '2025-03-02', real_user_id, 8,  false, '2025-03-02 11:00:00'),
    (50.00,   'Socar yanacaq',            'EXPENSE', '2025-03-05', real_user_id, 12, false, '2025-03-05 07:30:00'),
    (700.00,  'Bonus',                    'INCOME',  '2025-03-08', real_user_id, 2,  false, '2025-03-08 09:00:00'),
    (85.00,   'Restoran Sahil',           'EXPENSE', '2025-03-09', real_user_id, 8,  false, '2025-03-09 19:30:00'),
    (28.00,   'Azərenerji mart',          'EXPENSE', '2025-03-10', real_user_id, 15, false, '2025-03-10 12:00:00'),
    (450.00,  'Telefon alışı',            'EXPENSE', '2025-03-12', real_user_id, 22, false, '2025-03-12 14:00:00'),
    (15.00,   'Bakcell internet',         'EXPENSE', '2025-03-15', real_user_id, 15, false, '2025-03-15 09:00:00'),
    (40.00,   'Fitness club',             'EXPENSE', '2025-03-18', real_user_id, 18, false, '2025-03-18 07:00:00'),
    (100.00,  'Kitab alışı',              'EXPENSE', '2025-03-20', real_user_id, 20, false, '2025-03-20 15:00:00'),
    (30.00,   'Bolt taksi',               'EXPENSE', '2025-03-25', real_user_id, 12, false, '2025-03-25 18:00:00'),
    (2500.00, 'Maaş - Aprel',             'INCOME',  '2025-04-01', real_user_id, 1,  false, '2025-04-01 09:00:00'),
    (350.00,  'Mənzil icarəsi',           'EXPENSE', '2025-04-01', real_user_id, 5,  false, '2025-04-01 10:00:00'),
    (160.00,  'Araz supermarket',         'EXPENSE', '2025-04-03', real_user_id, 8,  false, '2025-04-03 11:00:00'),
    (28.00,   'Azərenerji aprel',         'EXPENSE', '2025-04-05', real_user_id, 15, false, '2025-04-05 12:00:00'),
    (55.00,   'Restoran Pizza',           'EXPENSE', '2025-04-07', real_user_id, 8,  false, '2025-04-07 19:00:00'),
    (400.00,  'Freelance iş',             'INCOME',  '2025-04-10', real_user_id, 2,  false, '2025-04-10 14:00:00'),
    (15.00,   'Bakcell internet',         'EXPENSE', '2025-04-12', real_user_id, 15, false, '2025-04-12 09:00:00'),
    (250.00,  'Paltar almaq',             'EXPENSE', '2025-04-15', real_user_id, 22, false, '2025-04-15 15:00:00'),
    (40.00,   'Fitness club',             'EXPENSE', '2025-04-18', real_user_id, 18, false, '2025-04-18 07:00:00'),
    (60.00,   'Bolt taksi',               'EXPENSE', '2025-04-20', real_user_id, 12, false, '2025-04-20 18:00:00'),
    (35.00,   'Kino bilet',               'EXPENSE', '2025-04-22', real_user_id, 25, false, '2025-04-22 17:00:00'),
    (20.00,   'Socar yanacaq',            'EXPENSE', '2025-04-25', real_user_id, 12, false, '2025-04-25 07:30:00'),
    (2500.00, 'Maaş - May',               'INCOME',  '2025-05-01', real_user_id, 1,  false, '2025-05-01 09:00:00'),
    (350.00,  'Mənzil icarəsi',           'EXPENSE', '2025-05-01', real_user_id, 5,  false, '2025-05-01 10:00:00'),
    (140.00,  'Bravo market',             'EXPENSE', '2025-05-02', real_user_id, 8,  false, '2025-05-02 11:00:00'),
    (28.00,   'Azərenerji may',           'EXPENSE', '2025-05-05', real_user_id, 15, false, '2025-05-05 12:00:00'),
    (15.00,   'Bakcell internet',         'EXPENSE', '2025-05-08', real_user_id, 15, false, '2025-05-08 09:00:00'),
    (90.00,   'Restoran Nəriman',         'EXPENSE', '2025-05-10', real_user_id, 8,  false, '2025-05-10 19:30:00'),
    (600.00,  'Bonus may',                'INCOME',  '2025-05-12', real_user_id, 2,  false, '2025-05-12 09:00:00'),
    (300.00,  'Səyahət xərci',            'EXPENSE', '2025-05-15', real_user_id, 25, false, '2025-05-15 10:00:00'),
    (40.00,   'Fitness club',             'EXPENSE', '2025-05-18', real_user_id, 18, false, '2025-05-18 07:00:00'),
    (75.00,   'Aptek dərman',             'EXPENSE', '2025-05-20', real_user_id, 18, false, '2025-05-20 11:00:00'),
    (45.00,   'Bolt taksi',               'EXPENSE', '2025-05-22', real_user_id, 12, false, '2025-05-22 08:00:00'),
    (120.00,  'Elektronika alışı',        'EXPENSE', '2025-05-25', real_user_id, 22, false, '2025-05-25 15:00:00'),
    (2500.00, 'Maaş - İyun',              'INCOME',  '2025-06-01', real_user_id, 1,  false, '2025-06-01 09:00:00'),
    (350.00,  'Mənzil icarəsi',           'EXPENSE', '2025-06-01', real_user_id, 5,  false, '2025-06-01 10:00:00'),
    (155.00,  'Araz supermarket',         'EXPENSE', '2025-06-03', real_user_id, 8,  false, '2025-06-03 11:00:00'),
    (28.00,   'Azərenerji iyun',          'EXPENSE', '2025-06-05', real_user_id, 15, false, '2025-06-05 12:00:00'),
    (500.00,  'Freelance iş',             'INCOME',  '2025-06-08', real_user_id, 2,  false, '2025-06-08 14:00:00'),
    (15.00,   'Bakcell internet',         'EXPENSE', '2025-06-10', real_user_id, 15, false, '2025-06-10 09:00:00'),
    (200.00,  'Məzuniyyət səyahəti',      'EXPENSE', '2025-06-15', real_user_id, 25, false, '2025-06-15 08:00:00'),
    (40.00,   'Fitness club',             'EXPENSE', '2025-06-18', real_user_id, 18, false, '2025-06-18 07:00:00'),
    (70.00,   'Restoran Sahil',           'EXPENSE', '2025-06-20', real_user_id, 8,  false, '2025-06-20 19:00:00'),
    (35.00,   'Socar yanacaq',            'EXPENSE', '2025-06-22', real_user_id, 12, false, '2025-06-22 07:30:00'),
    (180.00,  'Geyim almaq',              'EXPENSE', '2025-06-25', real_user_id, 22, false, '2025-06-25 15:00:00'),
    (50.00,   'Kino və əyləncə',          'EXPENSE', '2025-06-28', real_user_id, 25, false, '2025-06-28 17:00:00'),
    (2500.00, 'Maaş - İyul',              'INCOME',  '2025-07-01', real_user_id, 1,  false, '2025-07-01 09:00:00'),
    (350.00,  'Mənzil icarəsi',           'EXPENSE', '2025-07-01', real_user_id, 5,  false, '2025-07-01 10:00:00'),
    (170.00,  'Bravo market',             'EXPENSE', '2025-07-02', real_user_id, 8,  false, '2025-07-02 11:00:00'),
    (28.00,   'Azərenerji iyul',          'EXPENSE', '2025-07-05', real_user_id, 15, false, '2025-07-05 12:00:00'),
    (15.00,   'Bakcell internet',         'EXPENSE', '2025-07-08', real_user_id, 15, false, '2025-07-08 09:00:00'),
    (400.00,  'Əlavə gəlir',              'INCOME',  '2025-07-10', real_user_id, 2,  false, '2025-07-10 14:00:00'),
    (500.00,  'Məzuniyyət xərcləri',      'EXPENSE', '2025-07-12', real_user_id, 25, false, '2025-07-12 10:00:00'),
    (40.00,   'Fitness club',             'EXPENSE', '2025-07-15', real_user_id, 18, false, '2025-07-15 07:00:00'),
    (95.00,   'Restoran Pizza',           'EXPENSE', '2025-07-18', real_user_id, 8,  false, '2025-07-18 19:30:00'),
    (60.00,   'Bolt taksi',               'EXPENSE', '2025-07-20', real_user_id, 12, false, '2025-07-20 18:00:00'),
    (150.00,  'Ayaqqabı almaq',           'EXPENSE', '2025-07-22', real_user_id, 22, false, '2025-07-22 15:00:00'),
    (45.00,   'Aptek dərman',             'EXPENSE', '2025-07-25', real_user_id, 18, false, '2025-07-25 11:00:00'),
    (2500.00, 'Maaş - Avqust',            'INCOME',  '2025-08-01', real_user_id, 1,  false, '2025-08-01 09:00:00'),
    (350.00,  'Mənzil icarəsi',           'EXPENSE', '2025-08-01', real_user_id, 5,  false, '2025-08-01 10:00:00'),
    (160.00,  'Araz supermarket',         'EXPENSE', '2025-08-03', real_user_id, 8,  false, '2025-08-03 11:00:00'),
    (28.00,   'Azərenerji avqust',        'EXPENSE', '2025-08-05', real_user_id, 15, false, '2025-08-05 12:00:00'),
    (800.00,  'Bonus avqust',             'INCOME',  '2025-08-08', real_user_id, 2,  false, '2025-08-08 09:00:00'),
    (15.00,   'Bakcell internet',         'EXPENSE', '2025-08-10', real_user_id, 15, false, '2025-08-10 09:00:00'),
    (40.00,   'Fitness club',             'EXPENSE', '2025-08-12', real_user_id, 18, false, '2025-08-12 07:00:00'),
    (80.00,   'Restoran Nəriman',         'EXPENSE', '2025-08-15', real_user_id, 8,  false, '2025-08-15 19:00:00'),
    (55.00,   'Socar yanacaq',            'EXPENSE', '2025-08-18', real_user_id, 12, false, '2025-08-18 07:30:00'),
    (300.00,  'Məktəb ləvazimatı',        'EXPENSE', '2025-08-20', real_user_id, 20, false, '2025-08-20 10:00:00'),
    (200.00,  'Paltar almaq',             'EXPENSE', '2025-08-22', real_user_id, 22, false, '2025-08-22 15:00:00'),
    (35.00,   'Kino bilet',               'EXPENSE', '2025-08-25', real_user_id, 25, false, '2025-08-25 17:00:00'),
    (2500.00, 'Maaş - Sentyabr',          'INCOME',  '2025-09-01', real_user_id, 1,  false, '2025-09-01 09:00:00'),
    (350.00,  'Mənzil icarəsi',           'EXPENSE', '2025-09-01', real_user_id, 5,  false, '2025-09-01 10:00:00'),
    (140.00,  'Bravo market',             'EXPENSE', '2025-09-02', real_user_id, 8,  false, '2025-09-02 11:00:00'),
    (28.00,   'Azərenerji sentyabr',      'EXPENSE', '2025-09-05', real_user_id, 15, false, '2025-09-05 12:00:00'),
    (15.00,   'Bakcell internet',         'EXPENSE', '2025-09-08', real_user_id, 15, false, '2025-09-08 09:00:00'),
    (500.00,  'Freelance iş',             'INCOME',  '2025-09-10', real_user_id, 2,  false, '2025-09-10 14:00:00'),
    (250.00,  'Kurs ödənişi',             'EXPENSE', '2025-09-12', real_user_id, 20, false, '2025-09-12 10:00:00'),
    (40.00,   'Fitness club',             'EXPENSE', '2025-09-15', real_user_id, 18, false, '2025-09-15 07:00:00'),
    (75.00,   'Restoran Firuza',          'EXPENSE', '2025-09-18', real_user_id, 8,  false, '2025-09-18 19:00:00'),
    (45.00,   'Bolt taksi',               'EXPENSE', '2025-09-20', real_user_id, 12, false, '2025-09-20 18:00:00'),
    (90.00,   'Aptek dərman',             'EXPENSE', '2025-09-22', real_user_id, 18, false, '2025-09-22 11:00:00'),
    (180.00,  'Elektronika alışı',        'EXPENSE', '2025-09-25', real_user_id, 22, false, '2025-09-25 15:00:00'),
    (2500.00, 'Maaş - Oktyabr',           'INCOME',  '2025-10-01', real_user_id, 1,  false, '2025-10-01 09:00:00'),
    (350.00,  'Mənzil icarəsi',           'EXPENSE', '2025-10-01', real_user_id, 5,  false, '2025-10-01 10:00:00'),
    (155.00,  'Araz supermarket',         'EXPENSE', '2025-10-03', real_user_id, 8,  false, '2025-10-03 11:00:00'),
    (28.00,   'Azərenerji oktyabr',       'EXPENSE', '2025-10-05', real_user_id, 15, false, '2025-10-05 12:00:00'),
    (600.00,  'Bonus oktyabr',            'INCOME',  '2025-10-08', real_user_id, 2,  false, '2025-10-08 09:00:00'),
    (15.00,   'Bakcell internet',         'EXPENSE', '2025-10-10', real_user_id, 15, false, '2025-10-10 09:00:00'),
    (40.00,   'Fitness club',             'EXPENSE', '2025-10-12', real_user_id, 18, false, '2025-10-12 07:00:00'),
    (85.00,   'Restoran Sahil',           'EXPENSE', '2025-10-15', real_user_id, 8,  false, '2025-10-15 19:30:00'),
    (50.00,   'Socar yanacaq',            'EXPENSE', '2025-10-18', real_user_id, 12, false, '2025-10-18 07:30:00'),
    (300.00,  'Paltar almaq',             'EXPENSE', '2025-10-20', real_user_id, 22, false, '2025-10-20 15:00:00'),
    (60.00,   'Kino və əyləncə',          'EXPENSE', '2025-10-22', real_user_id, 25, false, '2025-10-22 17:00:00'),
    (70.00,   'Aptek dərman',             'EXPENSE', '2025-10-25', real_user_id, 18, false, '2025-10-25 11:00:00'),
    (2500.00, 'Maaş - Noyabr',            'INCOME',  '2025-11-01', real_user_id, 1,  false, '2025-11-01 09:00:00'),
    (350.00,  'Mənzil icarəsi',           'EXPENSE', '2025-11-01', real_user_id, 5,  false, '2025-11-01 10:00:00'),
    (165.00,  'Bravo market',             'EXPENSE', '2025-11-02', real_user_id, 8,  false, '2025-11-02 11:00:00'),
    (28.00,   'Azərenerji noyabr',        'EXPENSE', '2025-11-05', real_user_id, 15, false, '2025-11-05 12:00:00'),
    (15.00,   'Bakcell internet',         'EXPENSE', '2025-11-08', real_user_id, 15, false, '2025-11-08 09:00:00'),
    (400.00,  'Freelance iş',             'INCOME',  '2025-11-10', real_user_id, 2,  false, '2025-11-10 14:00:00'),
    (40.00,   'Fitness club',             'EXPENSE', '2025-11-12', real_user_id, 18, false, '2025-11-12 07:00:00'),
    (95.00,   'Restoran Nəriman',         'EXPENSE', '2025-11-15', real_user_id, 8,  false, '2025-11-15 19:00:00'),
    (200.00,  'Kurs ödənişi',             'EXPENSE', '2025-11-18', real_user_id, 20, false, '2025-11-18 10:00:00'),
    (55.00,   'Bolt taksi',               'EXPENSE', '2025-11-20', real_user_id, 12, false, '2025-11-20 18:00:00'),
    (150.00,  'Hədiyyə almaq',            'EXPENSE', '2025-11-22', real_user_id, 22, false, '2025-11-22 15:00:00'),
    (45.00,   'Socar yanacaq',            'EXPENSE', '2025-11-25', real_user_id, 12, false, '2025-11-25 07:30:00'),
    (2500.00, 'Maaş - Dekabr',            'INCOME',  '2025-12-01', real_user_id, 1,  false, '2025-12-01 09:00:00'),
    (350.00,  'Mənzil icarəsi',           'EXPENSE', '2025-12-01', real_user_id, 5,  false, '2025-12-01 10:00:00'),
    (200.00,  'Araz supermarket',         'EXPENSE', '2025-12-03', real_user_id, 8,  false, '2025-12-03 11:00:00'),
    (28.00,   'Azərenerji dekabr',        'EXPENSE', '2025-12-05', real_user_id, 15, false, '2025-12-05 12:00:00'),
    (1000.00, 'İllik bonus',              'INCOME',  '2025-12-08', real_user_id, 2,  false, '2025-12-08 09:00:00'),
    (15.00,   'Bakcell internet',         'EXPENSE', '2025-12-10', real_user_id, 15, false, '2025-12-10 09:00:00'),
    (400.00,  'Yeni il hədiyyələri',      'EXPENSE', '2025-12-15', real_user_id, 22, false, '2025-12-15 14:00:00'),
    (40.00,   'Fitness club',             'EXPENSE', '2025-12-18', real_user_id, 18, false, '2025-12-18 07:00:00'),
    (150.00,  'Restoran Yeni il',         'EXPENSE', '2025-12-20', real_user_id, 8,  false, '2025-12-20 19:30:00'),
    (60.00,   'Socar yanacaq',            'EXPENSE', '2025-12-22', real_user_id, 12, false, '2025-12-22 07:30:00'),
    (250.00,  'Paltar almaq',             'EXPENSE', '2025-12-24', real_user_id, 22, false, '2025-12-24 15:00:00'),
    (80.00,   'Kino və əyləncə',          'EXPENSE', '2025-12-28', real_user_id, 25, false, '2025-12-28 17:00:00'),
    (500.00,  'Freelance iş',             'INCOME',  '2025-12-30', real_user_id, 2,  false, '2025-12-30 14:00:00');
END $$;