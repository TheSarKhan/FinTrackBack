-- ============================================================
-- Financial Tracking System - Database Schema & Seed Data
-- ============================================================

-- Categories table
CREATE TABLE IF NOT EXISTS categories (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    type       VARCHAR(20) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE', 'BOTH')),
    parent_id  BIGINT REFERENCES categories(id) ON DELETE CASCADE,
    user_id    BIGINT,
    is_active  BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    is_active  BOOLEAN DEFAULT TRUE NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Add FK after users table exists
ALTER TABLE categories DROP CONSTRAINT IF EXISTS fk_category_user;
ALTER TABLE categories ADD CONSTRAINT fk_category_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id          BIGSERIAL PRIMARY KEY,
    amount      DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    description TEXT,
    type        VARCHAR(20) NOT NULL CHECK (type IN ('INCOME','EXPENSE')),
    date        DATE NOT NULL,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id),
    is_deleted  BOOLEAN DEFAULT FALSE NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW(),
    updated_at  TIMESTAMP DEFAULT NOW()
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_transactions_user_id   ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_date       ON transactions(date);
CREATE INDEX IF NOT EXISTS idx_transactions_type       ON transactions(type);
CREATE INDEX IF NOT EXISTS idx_transactions_category   ON transactions(category_id);
CREATE INDEX IF NOT EXISTS idx_categories_parent       ON categories(parent_id);
CREATE INDEX IF NOT EXISTS idx_categories_user         ON categories(user_id);

-- ============================================================
-- System Parent Categories (user_id = NULL, parent_id = NULL)
-- ============================================================
INSERT INTO categories (id, name, type, parent_id, user_id) VALUES
  (1,  'Yaşayış və Ev',       'EXPENSE', NULL, NULL),
  (2,  'Kommunal Xidmətlər',  'EXPENSE', NULL, NULL),
  (3,  'Qida və Market',      'EXPENSE', NULL, NULL),
  (4,  'Nəqliyyat',           'EXPENSE', NULL, NULL),
  (5,  'Təhsil və İnkişaf',   'EXPENSE', NULL, NULL),
  (6,  'Sağlamlıq',           'EXPENSE', NULL, NULL),
  (7,  'Əyləncə',             'EXPENSE', NULL, NULL),
  (8,  'Şəxsi Xərclər',       'EXPENSE', NULL, NULL),
  (9,  'Maliyyə',             'BOTH',    NULL, NULL),
  (10, 'Digər',               'BOTH',    NULL, NULL),
  (11, 'Gəlir',               'INCOME',  NULL, NULL)
ON CONFLICT (id) DO NOTHING;

-- ============================================================
-- System Subcategories
-- ============================================================

-- 1. Yaşayış və Ev
INSERT INTO categories (name, type, parent_id, user_id) VALUES
  ('İcarə',               'EXPENSE', 1, NULL),
  ('İpoteka',             'EXPENSE', 1, NULL),
  ('Ev Avadanlıqları',    'EXPENSE', 1, NULL),
  ('Təmir & Baxım',       'EXPENSE', 1, NULL)
ON CONFLICT DO NOTHING;

-- 2. Kommunal Xidmətlər
INSERT INTO categories (name, type, parent_id, user_id) VALUES
  ('Elektrik',            'EXPENSE', 2, NULL),
  ('Qaz',                 'EXPENSE', 2, NULL),
  ('Su',                  'EXPENSE', 2, NULL),
  ('İnternet & TV',       'EXPENSE', 2, NULL),
  ('Mobil Telefon',       'EXPENSE', 2, NULL)
ON CONFLICT DO NOTHING;

-- 3. Qida və Market
INSERT INTO categories (name, type, parent_id, user_id) VALUES
  ('Supermarket',         'EXPENSE', 3, NULL),
  ('Restoran & Kafe',     'EXPENSE', 3, NULL),
  ('Fast Food',           'EXPENSE', 3, NULL),
  ('Çatdırılma',          'EXPENSE', 3, NULL),
  ('İçkilər',             'EXPENSE', 3, NULL)
ON CONFLICT DO NOTHING;

-- 4. Nəqliyyat
INSERT INTO categories (name, type, parent_id, user_id) VALUES
  ('Yanacaq',             'EXPENSE', 4, NULL),
  ('İctimai Nəqliyyat',   'EXPENSE', 4, NULL),
  ('Taksi',               'EXPENSE', 4, NULL),
  ('Avtomobil Baxımı',    'EXPENSE', 4, NULL),
  ('Parking',             'EXPENSE', 4, NULL)
ON CONFLICT DO NOTHING;

-- 5. Təhsil və İnkişaf
INSERT INTO categories (name, type, parent_id, user_id) VALUES
  ('Kurslar',             'EXPENSE', 5, NULL),
  ('Kitablar',            'EXPENSE', 5, NULL),
  ('Universitet',         'EXPENSE', 5, NULL),
  ('Sertifikatlar',       'EXPENSE', 5, NULL)
ON CONFLICT DO NOTHING;

-- 6. Sağlamlıq
INSERT INTO categories (name, type, parent_id, user_id) VALUES
  ('Həkim',               'EXPENSE', 6, NULL),
  ('Dərmanlar',           'EXPENSE', 6, NULL),
  ('İdman & Fitnes',      'EXPENSE', 6, NULL),
  ('Sığorta',             'EXPENSE', 6, NULL)
ON CONFLICT DO NOTHING;

-- 7. Əyləncə
INSERT INTO categories (name, type, parent_id, user_id) VALUES
  ('Kino & Teatr',        'EXPENSE', 7, NULL),
  ('Oyunlar',             'EXPENSE', 7, NULL),
  ('Konsert',             'EXPENSE', 7, NULL),
  ('Turizm & Səyahət',    'EXPENSE', 7, NULL),
  ('Abunəliklər',         'EXPENSE', 7, NULL)
ON CONFLICT DO NOTHING;

-- 8. Şəxsi Xərclər
INSERT INTO categories (name, type, parent_id, user_id) VALUES
  ('Geyim',               'EXPENSE', 8, NULL),
  ('Hədiyyələr',          'EXPENSE', 8, NULL),
  ('Baxım & Gözəllik',    'EXPENSE', 8, NULL),
  ('Elektronika',         'EXPENSE', 8, NULL)
ON CONFLICT DO NOTHING;

-- 9. Maliyyə
INSERT INTO categories (name, type, parent_id, user_id) VALUES
  ('Kredit Ödənişi',      'EXPENSE', 9, NULL),
  ('Qənaət',              'INCOME',  9, NULL),
  ('İnvestisiya',         'BOTH',    9, NULL),
  ('Bank Komissiyası',    'EXPENSE', 9, NULL)
ON CONFLICT DO NOTHING;

-- 10. Digər
INSERT INTO categories (name, type, parent_id, user_id) VALUES
  ('Ianə & Yardım',       'EXPENSE', 10, NULL),
  ('Digər Xərclər',       'EXPENSE', 10, NULL)
ON CONFLICT DO NOTHING;

-- 11. Gəlir
INSERT INTO categories (name, type, parent_id, user_id) VALUES
  ('Maaş',                'INCOME', 11, NULL),
  ('Freelance',           'INCOME', 11, NULL),
  ('Biznes Gəliri',       'INCOME', 11, NULL),
  ('Kirayə Gəliri',       'INCOME', 11, NULL),
  ('Dividend',            'INCOME', 11, NULL),
  ('Digər Gəlir',         'INCOME', 11, NULL)
ON CONFLICT DO NOTHING;

-- Reset sequences
SELECT setval('categories_id_seq', (SELECT MAX(id) FROM categories));
-- ============================================================
-- 150 rows of transaction seed data (1 year, user_id = 1)
-- Run AFTER registering a user (user_id=1)
-- ============================================================

INSERT INTO transactions (amount, description, type, date, user_id, category_id, is_deleted) VALUES

-- ===== YANVAR =====
(1200.00, 'Yanvar maaşı',               'INCOME',  '2025-01-05', 1, (SELECT id FROM categories WHERE name='Maaş'),              false),
(150.00,  'Freelance iş',               'INCOME',  '2025-01-12', 1, (SELECT id FROM categories WHERE name='Freelance'),          false),
(400.00,  'Yanvar icarəsi',             'EXPENSE', '2025-01-01', 1, (SELECT id FROM categories WHERE name='İcarə'),              false),
(45.00,   'Elektrik yanvar',            'EXPENSE', '2025-01-03', 1, (SELECT id FROM categories WHERE name='Elektrik'),           false),
(28.00,   'Qaz yanvar',                 'EXPENSE', '2025-01-03', 1, (SELECT id FROM categories WHERE name='Qaz'),                false),
(180.00,  'Supermarket alış-veriş',     'EXPENSE', '2025-01-07', 1, (SELECT id FROM categories WHERE name='Supermarket'),        false),
(55.00,   'Restoran - dostlarla',       'EXPENSE', '2025-01-09', 1, (SELECT id FROM categories WHERE name='Restoran & Kafe'),    false),
(40.00,   'Yanacaq yanvar',             'EXPENSE', '2025-01-10', 1, (SELECT id FROM categories WHERE name='Yanacaq'),            false),
(25.00,   'Mobil telefon yanvar',       'EXPENSE', '2025-01-11', 1, (SELECT id FROM categories WHERE name='Mobil Telefon'),      false),
(35.00,   'Dərman - aptək',             'EXPENSE', '2025-01-15', 1, (SELECT id FROM categories WHERE name='Dərmanlar'),          false),
(120.00,  'Online kurs - Python',       'EXPENSE', '2025-01-18', 1, (SELECT id FROM categories WHERE name='Kurslar'),            false),
(60.00,   'Geyim - köynək',             'EXPENSE', '2025-01-22', 1, (SELECT id FROM categories WHERE name='Geyim'),              false),
(30.00,   'Kino biletləri',             'EXPENSE', '2025-01-25', 1, (SELECT id FROM categories WHERE name='Kino & Teatr'),       false),
(15.00,   'Fast food - McDonalds',      'EXPENSE', '2025-01-28', 1, (SELECT id FROM categories WHERE name='Fast Food'),          false),

-- ===== FEVRAL =====
(1200.00, 'Fevral maaşı',               'INCOME',  '2025-02-05', 1, (SELECT id FROM categories WHERE name='Maaş'),              false),
(400.00,  'Fevral icarəsi',             'EXPENSE', '2025-02-01', 1, (SELECT id FROM categories WHERE name='İcarə'),              false),
(42.00,   'Elektrik fevral',            'EXPENSE', '2025-02-03', 1, (SELECT id FROM categories WHERE name='Elektrik'),           false),
(30.00,   'Qaz fevral',                 'EXPENSE', '2025-02-03', 1, (SELECT id FROM categories WHERE name='Qaz'),                false),
(200.00,  'Supermarket fevral',         'EXPENSE', '2025-02-08', 1, (SELECT id FROM categories WHERE name='Supermarket'),        false),
(75.00,   'Sevgililer günü restoran',   'EXPENSE', '2025-02-14', 1, (SELECT id FROM categories WHERE name='Restoran & Kafe'),    false),
(45.00,   'Yanacaq fevral',             'EXPENSE', '2025-02-12', 1, (SELECT id FROM categories WHERE name='Yanacaq'),            false),
(25.00,   'İnternet fevral',            'EXPENSE', '2025-02-15', 1, (SELECT id FROM categories WHERE name='İnternet & TV'),      false),
(80.00,   'Həkim müayinəsi',            'EXPENSE', '2025-02-17', 1, (SELECT id FROM categories WHERE name='Həkim'),              false),
(50.00,   'Kitablar - proqramlaşdırma', 'EXPENSE', '2025-02-20', 1, (SELECT id FROM categories WHERE name='Kitablar'),           false),
(200.00,  'Hədiyyə - doğumgünü',        'EXPENSE', '2025-02-22', 1, (SELECT id FROM categories WHERE name='Hədiyyələr'),         false),
(35.00,   'Taksi fevral',               'EXPENSE', '2025-02-25', 1, (SELECT id FROM categories WHERE name='Taksi'),              false),

-- ===== MART =====
(1200.00, 'Mart maaşı',                 'INCOME',  '2025-03-05', 1, (SELECT id FROM categories WHERE name='Maaş'),              false),
(300.00,  'Biznes gəliri mart',         'INCOME',  '2025-03-15', 1, (SELECT id FROM categories WHERE name='Biznes Gəliri'),      false),
(400.00,  'Mart icarəsi',               'EXPENSE', '2025-03-01', 1, (SELECT id FROM categories WHERE name='İcarə'),              false),
(38.00,   'Elektrik mart',              'EXPENSE', '2025-03-03', 1, (SELECT id FROM categories WHERE name='Elektrik'),           false),
(160.00,  'Supermarket mart',           'EXPENSE', '2025-03-06', 1, (SELECT id FROM categories WHERE name='Supermarket'),        false),
(90.00,   'Novruz şirniyyatları',       'EXPENSE', '2025-03-20', 1, (SELECT id FROM categories WHERE name='Supermarket'),        false),
(55.00,   'Restoran - Novruz',          'EXPENSE', '2025-03-21', 1, (SELECT id FROM categories WHERE name='Restoran & Kafe'),    false),
(50.00,   'Yanacaq mart',               'EXPENSE', '2025-03-10', 1, (SELECT id FROM categories WHERE name='Yanacaq'),            false),
(25.00,   'Mobil mart',                 'EXPENSE', '2025-03-11', 1, (SELECT id FROM categories WHERE name='Mobil Telefon'),      false),
(150.00,  'İdman zalı - 3 aylıq',       'EXPENSE', '2025-03-15', 1, (SELECT id FROM categories WHERE name='İdman & Fitnes'),     false),
(70.00,   'Geyim - şalvar',             'EXPENSE', '2025-03-18', 1, (SELECT id FROM categories WHERE name='Geyim'),              false),
(45.00,   'Konsert biletləri',          'EXPENSE', '2025-03-28', 1, (SELECT id FROM categories WHERE name='Konsert'),            false),

-- ===== APRELFEBRUARİ =====
(1200.00, 'Aprel maaşı',                'INCOME',  '2025-04-05', 1, (SELECT id FROM categories WHERE name='Maaş'),              false),
(400.00,  'Aprel icarəsi',              'EXPENSE', '2025-04-01', 1, (SELECT id FROM categories WHERE name='İcarə'),              false),
(35.00,   'Elektrik aprel',             'EXPENSE', '2025-04-03', 1, (SELECT id FROM categories WHERE name='Elektrik'),           false),
(170.00,  'Supermarket aprel',          'EXPENSE', '2025-04-07', 1, (SELECT id FROM categories WHERE name='Supermarket'),        false),
(65.00,   'Restoran aprel',             'EXPENSE', '2025-04-12', 1, (SELECT id FROM categories WHERE name='Restoran & Kafe'),    false),
(40.00,   'Yanacaq aprel',              'EXPENSE', '2025-04-08', 1, (SELECT id FROM categories WHERE name='Yanacaq'),            false),
(300.00,  'Avtomobil texniki baxım',    'EXPENSE', '2025-04-15', 1, (SELECT id FROM categories WHERE name='Avtomobil Baxımı'),   false),
(25.00,   'İnternet aprel',             'EXPENSE', '2025-04-15', 1, (SELECT id FROM categories WHERE name='İnternet & TV'),      false),
(60.00,   'Baxım & gözəllik',           'EXPENSE', '2025-04-18', 1, (SELECT id FROM categories WHERE name='Baxım & Gözəllik'),   false),
(35.00,   'Fast food aprel',            'EXPENSE', '2025-04-22', 1, (SELECT id FROM categories WHERE name='Fast Food'),          false),
(200.00,  'Kredit ödənişi aprel',       'EXPENSE', '2025-04-25', 1, (SELECT id FROM categories WHERE name='Kredit Ödənişi'),     false),

-- ===== MAY =====
(1200.00, 'May maaşı',                  'INCOME',  '2025-05-05', 1, (SELECT id FROM categories WHERE name='Maaş'),              false),
(250.00,  'Freelance may',              'INCOME',  '2025-05-20', 1, (SELECT id FROM categories WHERE name='Freelance'),          false),
(400.00,  'May icarəsi',                'EXPENSE', '2025-05-01', 1, (SELECT id FROM categories WHERE name='İcarə'),              false),
(30.00,   'Elektrik may',               'EXPENSE', '2025-05-03', 1, (SELECT id FROM categories WHERE name='Elektrik'),           false),
(190.00,  'Supermarket may',            'EXPENSE', '2025-05-09', 1, (SELECT id FROM categories WHERE name='Supermarket'),        false),
(80.00,   'Restoran may',               'EXPENSE', '2025-05-14', 1, (SELECT id FROM categories WHERE name='Restoran & Kafe'),    false),
(55.00,   'Yanacaq may',                'EXPENSE', '2025-05-11', 1, (SELECT id FROM categories WHERE name='Yanacaq'),            false),
(25.00,   'Mobil may',                  'EXPENSE', '2025-05-11', 1, (SELECT id FROM categories WHERE name='Mobil Telefon'),      false),
(180.00,  'Sertifikat kursu - AWS',     'EXPENSE', '2025-05-16', 1, (SELECT id FROM categories WHERE name='Sertifikatlar'),      false),
(50.00,   'Taksi may',                  'EXPENSE', '2025-05-20', 1, (SELECT id FROM categories WHERE name='Taksi'),              false),
(90.00,   'Elektronika - qulaqlıq',     'EXPENSE', '2025-05-23', 1, (SELECT id FROM categories WHERE name='Elektronika'),        false),
(200.00,  'Kredit ödənişi may',         'EXPENSE', '2025-05-25', 1, (SELECT id FROM categories WHERE name='Kredit Ödənişi'),     false),
(40.00,   'Abunəlik - Netflix/Spotify', 'EXPENSE', '2025-05-28', 1, (SELECT id FROM categories WHERE name='Abunəliklər'),        false),

-- ===== İYUN =====
(1200.00, 'İyun maaşı',                 'INCOME',  '2025-06-05', 1, (SELECT id FROM categories WHERE name='Maaş'),              false),
(400.00,  'İyun icarəsi',               'EXPENSE', '2025-06-01', 1, (SELECT id FROM categories WHERE name='İcarə'),              false),
(28.00,   'Elektrik iyun',              'EXPENSE', '2025-06-03', 1, (SELECT id FROM categories WHERE name='Elektrik'),           false),
(160.00,  'Supermarket iyun',           'EXPENSE', '2025-06-08', 1, (SELECT id FROM categories WHERE name='Supermarket'),        false),
(110.00,  'Restoran - ailə yeməyi',     'EXPENSE', '2025-06-15', 1, (SELECT id FROM categories WHERE name='Restoran & Kafe'),    false),
(60.00,   'Yanacaq iyun',               'EXPENSE', '2025-06-10', 1, (SELECT id FROM categories WHERE name='Yanacaq'),            false),
(25.00,   'İnternet iyun',              'EXPENSE', '2025-06-15', 1, (SELECT id FROM categories WHERE name='İnternet & TV'),      false),
(45.00,   'Həkim - diş müayinəsi',      'EXPENSE', '2025-06-17', 1, (SELECT id FROM categories WHERE name='Həkim'),              false),
(200.00,  'Kredit ödənişi iyun',        'EXPENSE', '2025-06-25', 1, (SELECT id FROM categories WHERE name='Kredit Ödənişi'),     false),
(350.00,  'Məzuniyyət hazırlığı',       'EXPENSE', '2025-06-28', 1, (SELECT id FROM categories WHERE name='Turizm & Səyahət'),   false),

-- ===== İYUL =====
(1200.00, 'İyul maaşı',                 'INCOME',  '2025-07-05', 1, (SELECT id FROM categories WHERE name='Maaş'),              false),
(500.00,  'Məzuniyyət gəliri - bonus',  'INCOME',  '2025-07-01', 1, (SELECT id FROM categories WHERE name='Maaş'),              false),
(400.00,  'İyul icarəsi',               'EXPENSE', '2025-07-01', 1, (SELECT id FROM categories WHERE name='İcarə'),              false),
(32.00,   'Elektrik iyul',              'EXPENSE', '2025-07-03', 1, (SELECT id FROM categories WHERE name='Elektrik'),           false),
(140.00,  'Supermarket iyul',           'EXPENSE', '2025-07-07', 1, (SELECT id FROM categories WHERE name='Supermarket'),        false),
(800.00,  'Məzuniyyət - otel',          'EXPENSE', '2025-07-10', 1, (SELECT id FROM categories WHERE name='Turizm & Səyahət'),   false),
(250.00,  'Məzuniyyət - restoranlar',   'EXPENSE', '2025-07-12', 1, (SELECT id FROM categories WHERE name='Restoran & Kafe'),    false),
(70.00,   'Yanacaq iyul',               'EXPENSE', '2025-07-08', 1, (SELECT id FROM categories WHERE name='Yanacaq'),            false),
(25.00,   'Mobil iyul',                 'EXPENSE', '2025-07-11', 1, (SELECT id FROM categories WHERE name='Mobil Telefon'),      false),
(120.00,  'Geyim - yay paltarları',     'EXPENSE', '2025-07-20', 1, (SELECT id FROM categories WHERE name='Geyim'),              false),
(200.00,  'Kredit ödənişi iyul',        'EXPENSE', '2025-07-25', 1, (SELECT id FROM categories WHERE name='Kredit Ödənişi'),     false),

-- ===== AVQUST =====
(1200.00, 'Avqust maaşı',               'INCOME',  '2025-08-05', 1, (SELECT id FROM categories WHERE name='Maaş'),              false),
(400.00,  'Avqust icarəsi',             'EXPENSE', '2025-08-01', 1, (SELECT id FROM categories WHERE name='İcarə'),              false),
(55.00,   'Elektrik avqust',            'EXPENSE', '2025-08-03', 1, (SELECT id FROM categories WHERE name='Elektrik'),           false),
(175.00,  'Supermarket avqust',         'EXPENSE', '2025-08-08', 1, (SELECT id FROM categories WHERE name='Supermarket'),        false),
(70.00,   'Restoran avqust',            'EXPENSE', '2025-08-14', 1, (SELECT id FROM categories WHERE name='Restoran & Kafe'),    false),
(50.00,   'Yanacaq avqust',             'EXPENSE', '2025-08-10', 1, (SELECT id FROM categories WHERE name='Yanacaq'),            false),
(25.00,   'İnternet avqust',            'EXPENSE', '2025-08-15', 1, (SELECT id FROM categories WHERE name='İnternet & TV'),      false),
(200.00,  'Məktəb ləvazimatları',       'EXPENSE', '2025-08-20', 1, (SELECT id FROM categories WHERE name='Kitablar'),           false),
(85.00,   'Dərman - eczane',            'EXPENSE', '2025-08-22', 1, (SELECT id FROM categories WHERE name='Dərmanlar'),          false),
(200.00,  'Kredit ödənişi avqust',      'EXPENSE', '2025-08-25', 1, (SELECT id FROM categories WHERE name='Kredit Ödənişi'),     false),
(60.00,   'Oyun - PS Store',            'EXPENSE', '2025-08-28', 1, (SELECT id FROM categories WHERE name='Oyunlar'),            false),

-- ===== SENTYABR =====
(1200.00, 'Sentyabr maaşı',             'INCOME',  '2025-09-05', 1, (SELECT id FROM categories WHERE name='Maaş'),              false),
(350.00,  'Freelance sentyabr',         'INCOME',  '2025-09-18', 1, (SELECT id FROM categories WHERE name='Freelance'),          false),
(400.00,  'Sentyabr icarəsi',           'EXPENSE', '2025-09-01', 1, (SELECT id FROM categories WHERE name='İcarə'),              false),
(40.00,   'Elektrik sentyabr',          'EXPENSE', '2025-09-03', 1, (SELECT id FROM categories WHERE name='Elektrik'),           false),
(165.00,  'Supermarket sentyabr',       'EXPENSE', '2025-09-07', 1, (SELECT id FROM categories WHERE name='Supermarket'),        false),
(85.00,   'Restoran sentyabr',          'EXPENSE', '2025-09-13', 1, (SELECT id FROM categories WHERE name='Restoran & Kafe'),    false),
(55.00,   'Yanacaq sentyabr',           'EXPENSE', '2025-09-09', 1, (SELECT id FROM categories WHERE name='Yanacaq'),            false),
(25.00,   'Mobil sentyabr',             'EXPENSE', '2025-09-11', 1, (SELECT id FROM categories WHERE name='Mobil Telefon'),      false),
(250.00,  'Universitet - semestir',     'EXPENSE', '2025-09-15', 1, (SELECT id FROM categories WHERE name='Universitet'),        false),
(200.00,  'Kredit ödənişi sentyabr',    'EXPENSE', '2025-09-25', 1, (SELECT id FROM categories WHERE name='Kredit Ödənişi'),     false),
(75.00,   'Baxım & gözəllik sentyabr',  'EXPENSE', '2025-09-27', 1, (SELECT id FROM categories WHERE name='Baxım & Gözəllik'),   false),

-- ===== OKTYABR =====
(1300.00, 'Oktyabr maaşı - artım',      'INCOME',  '2025-10-05', 1, (SELECT id FROM categories WHERE name='Maaş'),              false),
(400.00,  'Oktyabr icarəsi',            'EXPENSE', '2025-10-01', 1, (SELECT id FROM categories WHERE name='İcarə'),              false),
(43.00,   'Elektrik oktyabr',           'EXPENSE', '2025-10-03', 1, (SELECT id FROM categories WHERE name='Elektrik'),           false),
(32.00,   'Qaz oktyabr',                'EXPENSE', '2025-10-03', 1, (SELECT id FROM categories WHERE name='Qaz'),                false),
(180.00,  'Supermarket oktyabr',        'EXPENSE', '2025-10-08', 1, (SELECT id FROM categories WHERE name='Supermarket'),        false),
(95.00,   'Restoran oktyabr',           'EXPENSE', '2025-10-15', 1, (SELECT id FROM categories WHERE name='Restoran & Kafe'),    false),
(60.00,   'Yanacaq oktyabr',            'EXPENSE', '2025-10-10', 1, (SELECT id FROM categories WHERE name='Yanacaq'),            false),
(25.00,   'İnternet oktyabr',           'EXPENSE', '2025-10-15', 1, (SELECT id FROM categories WHERE name='İnternet & TV'),      false),
(400.00,  'Elektronika - telefon ekran','EXPENSE', '2025-10-18', 1, (SELECT id FROM categories WHERE name='Elektronika'),        false),
(200.00,  'Kredit ödənişi oktyabr',     'EXPENSE', '2025-10-25', 1, (SELECT id FROM categories WHERE name='Kredit Ödənişi'),     false),
(55.00,   'Taksi oktyabr',              'EXPENSE', '2025-10-28', 1, (SELECT id FROM categories WHERE name='Taksi'),              false),
(40.00,   'Abunəlik oktyabr',           'EXPENSE', '2025-10-30', 1, (SELECT id FROM categories WHERE name='Abunəliklər'),        false),

-- ===== NOYABR =====
(1300.00, 'Noyabr maaşı',               'INCOME',  '2025-11-05', 1, (SELECT id FROM categories WHERE name='Maaş'),              false),
(200.00,  'Kirayə gəliri',              'INCOME',  '2025-11-10', 1, (SELECT id FROM categories WHERE name='Kirayə Gəliri'),      false),
(400.00,  'Noyabr icarəsi',             'EXPENSE', '2025-11-01', 1, (SELECT id FROM categories WHERE name='İcarə'),              false),
(50.00,   'Elektrik noyabr',            'EXPENSE', '2025-11-03', 1, (SELECT id FROM categories WHERE name='Elektrik'),           false),
(38.00,   'Qaz noyabr',                 'EXPENSE', '2025-11-03', 1, (SELECT id FROM categories WHERE name='Qaz'),                false),
(170.00,  'Supermarket noyabr',         'EXPENSE', '2025-11-07', 1, (SELECT id FROM categories WHERE name='Supermarket'),        false),
(65.00,   'Restoran noyabr',            'EXPENSE', '2025-11-12', 1, (SELECT id FROM categories WHERE name='Restoran & Kafe'),    false),
(55.00,   'Yanacaq noyabr',             'EXPENSE', '2025-11-09', 1, (SELECT id FROM categories WHERE name='Yanacaq'),            false),
(25.00,   'Mobil noyabr',               'EXPENSE', '2025-11-11', 1, (SELECT id FROM categories WHERE name='Mobil Telefon'),      false),
(100.00,  'Tibbi sığorta',              'EXPENSE', '2025-11-15', 1, (SELECT id FROM categories WHERE name='Sığorta'),            false),
(200.00,  'Kredit ödənişi noyabr',      'EXPENSE', '2025-11-25', 1, (SELECT id FROM categories WHERE name='Kredit Ödənişi'),     false),
(150.00,  'Geyim - qış paltarları',     'EXPENSE', '2025-11-22', 1, (SELECT id FROM categories WHERE name='Geyim'),              false),
(80.00,   'Kino & əyləncə noyabr',      'EXPENSE', '2025-11-29', 1, (SELECT id FROM categories WHERE name='Kino & Teatr'),       false),

-- ===== DEKABR =====
(1300.00, 'Dekabr maaşı',               'INCOME',  '2025-12-05', 1, (SELECT id FROM categories WHERE name='Maaş'),              false),
(500.00,  'İlsonu bonusu',              'INCOME',  '2025-12-20', 1, (SELECT id FROM categories WHERE name='Maaş'),              false),
(200.00,  'Kirayə gəliri dekabr',       'INCOME',  '2025-12-10', 1, (SELECT id FROM categories WHERE name='Kirayə Gəliri'),      false),
(400.00,  'Dekabr icarəsi',             'EXPENSE', '2025-12-01', 1, (SELECT id FROM categories WHERE name='İcarə'),              false),
(65.00,   'Elektrik dekabr',            'EXPENSE', '2025-12-03', 1, (SELECT id FROM categories WHERE name='Elektrik'),           false),
(45.00,   'Qaz dekabr',                 'EXPENSE', '2025-12-03', 1, (SELECT id FROM categories WHERE name='Qaz'),                false),
(220.00,  'Supermarket dekabr',         'EXPENSE', '2025-12-08', 1, (SELECT id FROM categories WHERE name='Supermarket'),        false),
(150.00,  'Yeni il şirniyyatları',      'EXPENSE', '2025-12-25', 1, (SELECT id FROM categories WHERE name='Supermarket'),        false),
(120.00,  'Restoran - Yeni il şənliyi', 'EXPENSE', '2025-12-31', 1, (SELECT id FROM categories WHERE name='Restoran & Kafe'),    false),
(60.00,   'Yanacaq dekabr',             'EXPENSE', '2025-12-10', 1, (SELECT id FROM categories WHERE name='Yanacaq'),            false),
(25.00,   'İnternet dekabr',            'EXPENSE', '2025-12-15', 1, (SELECT id FROM categories WHERE name='İnternet & TV'),      false),
(350.00,  'Hədiyyələr - Yeni il',       'EXPENSE', '2025-12-22', 1, (SELECT id FROM categories WHERE name='Hədiyyələr'),         false),
(200.00,  'Kredit ödənişi dekabr',      'EXPENSE', '2025-12-25', 1, (SELECT id FROM categories WHERE name='Kredit Ödənişi'),     false),
(90.00,   'Baxım & gözəllik dekabr',    'EXPENSE', '2025-12-27', 1, (SELECT id FROM categories WHERE name='Baxım & Gözəllik'),   false),
(45.00,   'Abunəlik dekabr',            'EXPENSE', '2025-12-28', 1, (SELECT id FROM categories WHERE name='Abunəliklər'),        false);