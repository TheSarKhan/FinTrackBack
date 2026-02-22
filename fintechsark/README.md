# 💰 Financial Tracking System — Backend

Spring Boot REST API | JWT Auth | PostgreSQL | AI Chat Module

---

## 🏗️ Layihə Strukturu

```
fintechsark/
├── src/main/java/az/sarkhan/fintechsark/
│   ├── config/          # Security, JPA konfiqurasiyası
│   ├── controller/      # REST endpointlər
│   ├── dto/             # Request & Response DTO-lar
│   ├── entity/          # JPA Entity-lər
│   ├── enums/           # TransactionType, CategoryType
│   ├── exception/       # Global error handling
│   ├── repository/      # Spring Data JPA
│   ├── security/        # JWT filter & util
│   └── service/         # Business logic
├── src/main/resources/
│   ├── application.properties
│   └── schema.sql       # DB schema + seed data
├── compose.yaml         # Docker PostgreSQL
└── pom.xml
```

---

## 🚀 Başlatma

### 1. PostgreSQL (Docker)
```bash
docker compose up -d
```

### 2. Tətbiqi Başlat
```bash
./mvnw spring-boot:run
```
API: `http://localhost:8080`

---

## 🔐 Authentication API

| Method | Endpoint | Açıqlama |
|--------|----------|----------|
| POST | `/api/auth/register` | Qeydiyyat |
| POST | `/api/auth/login` | Giriş → JWT token |

**Register:**
```json
{
  "name": "Sarkhan",
  "email": "sarkhan@example.com",
  "password": "secret123"
}
```

**Response:**
```json
{
  "token": "eyJ...",
  "name": "Sarkhan",
  "email": "sarkhan@example.com",
  "userId": 1
}
```

Sonrakı sorğularda header əlavə et:
```
Authorization: Bearer <token>
```

---

## 📂 Category API

| Method | Endpoint | Açıqlama |
|--------|----------|----------|
| GET | `/api/categories/parents` | Bütün parent kateqoriyalar |
| GET | `/api/categories/parents/{id}/subcategories` | Parent-in subkateqoriyaları |
| GET | `/api/categories` | Tam ağac (dropdown üçün) |
| POST | `/api/categories` | Yeni user subkateqoriyası yarat |
| DELETE | `/api/categories/{id}` | User kateqoriyasını sil |

---

## 💸 Transaction API

| Method | Endpoint | Açıqlama |
|--------|----------|----------|
| GET | `/api/transactions` | Siyahı (filter + pagination) |
| GET | `/api/transactions/{id}` | Tək tranzaksiya |
| POST | `/api/transactions` | Yeni tranzaksiya |
| PUT | `/api/transactions/{id}` | Yenilə |
| DELETE | `/api/transactions/{id}` | Soft delete |

**Filter parametrləri:** `type`, `categoryId`, `startDate`, `endDate`, `page`, `size`

---

## 📊 Dashboard API

| Method | Endpoint | Açıqlama |
|--------|----------|----------|
| GET | `/api/dashboard` | Tam statistika |
| GET | `/api/dashboard/expense-by-category` | Pie chart məlumatı |
| GET | `/api/dashboard/expense-by-category/{parentId}/drilldown` | Subcategory breakdown |

---

## 🤖 AI Chat API

| Method | Endpoint | Açıqlama |
|--------|----------|----------|
| POST | `/api/ai/chat` | Sual ver, analiz al |
| GET | `/api/ai/analyze` | Avtomatik bu ay analizi |

**Chat Request:**
```json
{ "message": "Bu ay ən çox nəyə xərclədim?" }
```

**Nümunə cavablar:**
- "Bu ay xərclərinizin 38%-i Qida və Market kateqoriyasındadır."
- "Nəqliyyat xərclərinizdə artım trendi müşahidə olunur."
- "Xərcləriniz gəlirinizdən 15% çoxdur, qənaəti artırmağı tövsiyə edirəm."

---

## 🗄️ Database

**System kateqoriyaları** (schema.sql-dən avtomatik yüklənir):
- 10 parent kateqoriya (silinə bilməz)
- 40+ predefined subkateqoriya
- İstifadəçi öz subkateqoriyalarını əlavə edə bilər

---

## ⚙️ Mühit Dəyişənləri

| Dəyişən | Default | Açıqlama |
|---------|---------|----------|
| `OPENAI_API_KEY` | disabled | GPT inteqrasiyası üçün (optional) |
| `spring.datasource.url` | localhost:5432/fintechsark | DB URL |

AI default olaraq rule-based analiz edir. `OPENAI_API_KEY` set edilsə GPT-ə keçid edilə bilər.
