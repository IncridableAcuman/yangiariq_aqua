# Render va Neon PostgreSQL orqali Production Deployment Qo'llanmasi
## YANGIARIQ AQUA AI (Agronexsus) — Milestone 08.22

Ushbu qo'llanma **Yangiariq Aqua AI (Agronexsus)** platformasini **Render** bulutli platformasida va **Neon Serverless PostgreSQL** ma'lumotlar bazasida ishlab chiqarish (production) rejimida ishga tushirish, xavfsizlikni ta'minlash va operatsion boshqaruv bo'yicha to'liq qo'llanmadir.

---

## 🏗 Arxitektura va Jonli Havolalar

| Komponent | Xizmat Nomi | Texnologiya | Jonli Havola (URL) | Holati | Tavsif |
| :--- | :--- | :--- | :--- | :---: | :--- |
| **Frontend** | `yangiariq-aqua` | React 18 + Vite + TS (Static CDN) | `https://yangiariq-aqua.onrender.com` | **DEPLOYED & VERIFIED** | Foydalanuvchilar kiradigan asosiy SPA veb-ilovasi |
| **Backend** | `yangiariq-aqua-api` | Spring Boot 3 (Java 21 Docker) | `https://yangiariq-aqua-api.onrender.com` | **CONFIGURED** | REST API, 51 ta Liquibase migratsiyasi, 32 ta ruxsatnoma va RBAC |
| **Database** | `Neon PostgreSQL` | PostgreSQL 18.6 (Serverless) | Neon.tech Cloud (AWS Ohio `us-east-2`) | **VERIFIED & ACTIVE** | 67 ta relyatsion jadval, PgBouncer pooler ulanishi |

---

## 🚀 1-usul: Render Blueprint orqali 1 marta bosish bilan deploy qilish (Tavsiya etiladi)

Loyiha ildizida barcha sozlamalarni o'z ichiga olgan [render.yaml](file:///home/izzatbek/Desktop/agronexsus/render.yaml) blueprint fayli tayyorlangan.

### Bosqichlar:
1. O'zgarishlarni GitHub repositoriyangizga yuboring:
   ```bash
   git add .
   git commit -m "feat(deploy): update render blueprint and production environment specification"
   git push origin main
   ```

2. [Render Dashboard](https://dashboard.render.com/) ga kiring.
3. Yuqori o'ng burchakdagi **"New +"** tugmasini bosing va **"Blueprint"** ni tanlang.
4. GitHub repositoriyangizni ulang (`agronexsus` yoki `yangiariq_aqua`).
5. Render `render.yaml` faylini avtomatik tahlil qiladi va 2 ta xizmatni ko'rsatadi:
   - **`yangiariq-aqua-api`** (Docker Web Service)
   - **`yangiariq-aqua`** (Static Site CDN)
6. So'ralgan o'zgaruvchilarga qiymat bering:
   - **`DATABASE_URL`**: Neon PostgreSQL ulanish satrini kiriting:
     ```text
     postgresql://neondb_owner:npg_OFdN0pikAuf2@ep-fragrant-mouse-begewk9z-pooler.c-9.us-east-2.aws.neon.tech/neondb?sslmode=require&channel_binding=require
     ```
   - **`GEMINI_API_KEY`**: *(ixtiyoriy)* Google Gemini API kalitingiz.
7. **"Apply"** tugmasini bosing.
   Render avtomatik ravishda:
   - Backend konteynerini Java 21 da yig'adi.
   - Barcha 51 ta Liquibase migratsiyasini tekshiradi (baza to'liq tayyor).
   - Frontend'ni yig'adi (`npm run build`) va global CDN orqali tarqatadi.

---

## 🛠 2-usul: Render Dashboard orqali qo'lda (Manual) sozlash

Agar Blueprint o'rniga har bir xizmatni alohida boshqarmoqchi bo'lsangiz:

### A. Backend xizmatini sozlash (Web Service)
1. Render Dashboard -> **"New +"** -> **"Web Service"**.
2. GitHub repositoriyangizni tanlang.
3. Quyidagi parametrlarni kiriting:
   - **Name**: `yangiariq-aqua-api`
   - **Region**: `Ohio (US East)` (Neon ham `us-east-2` da bo'lgani uchun eng kam kechikish)
   - **Branch**: `main`
   - **Root Directory**: `backend`
   - **Runtime**: `Docker`
   - **Dockerfile Path**: `Dockerfile`
   - **Plan**: `Free`
4. **Health Check Path** (Advanced bo'limida):
   - `/actuator/health`
5. **Environment Variables**:
   | Kalit (Key) | Qiymat (Value) |
   | :--- | :--- |
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `DATABASE_URL` | *Neon URL manzili* |
   | `JWT_SECRET` | *(Render avtomatik yaratishi yoki 64+ belgili tasodifiy string)* |
   | `JWT_ACCESS_TOKEN_EXPIRATION` | `3600000` (1 soat) |
   | `JWT_REFRESH_TOKEN_EXPIRATION` | `604800000` (7 kun) |
   | `CORS_ALLOWED_ORIGINS` | `https://yangiariq-aqua.onrender.com,https://yangiariq-aqua-6hfh.onrender.com,https://yangiariq-aqua-api.onrender.com` |
   | `RATE_LIMIT_ENABLED` | `true` |
   | `ENABLE_SWAGGER_PROD` | `true` |
   | `AI_PROVIDER` | `mock` (yoki `gemini`) |
   | `GEMINI_API_KEY` | *(ixtiyoriy)* |
   | `SENSOR_PROVIDER` | `simulated` |
   | `JAVA_OPTS` | `-XX:+UseG1GC -XX:MaxRAMPercentage=60.0 -Xss256k -XX:MaxMetaspaceSize=128m -XX:+ExitOnOutOfMemoryError` |

---

### B. Frontend xizmatini sozlash (Static Site)
1. Render Dashboard -> **"New +"** -> **"Static Site"**.
2. GitHub repositoriyangizni tanlang.
3. Parametrlar:
   - **Name**: `yangiariq-aqua`
   - **Branch**: `main`
   - **Root Directory**: `frontend`
   - **Build Command**: `npm ci && npm run build`
   - **Publish Directory**: `dist`
4. **Environment Variables**:
   | Kalit (Key) | Qiymat (Value) |
   | :--- | :--- |
   | `VITE_API_BASE_URL` | `https://yangiariq-aqua-api.onrender.com/api/v1` |
   | `VITE_DEFAULT_LANGUAGE` | `uz` |
   | `VITE_APP_NAME` | `Yangiariq Aqua AI` |
5. **Redirects/Rewrites (SPA routing uchun muhim)**:
   - **Type**: `Rewrite`
   - **Source**: `/*`
   - **Destination**: `/index.html`

---

## 🔑 Bir Martalik SuperAdmin Akkauntini Ochish (Bootstrap)

Tizimda birinchi marta `SUPER_ADMIN` rolidagi foydalanuvchini xavfsiz yaratish uchun:
1. Render Backend Dashboard -> **Environment** bo'limiga kiring.
2. Quyidagi 3 ta o'zgaruvchini qo'shing:
   - `SUPER_ADMIN_BOOTSTRAP_ENABLED` = `true`
   - `SUPER_ADMIN_BOOTSTRAP_EMAIL` = `admin@aqua.yangiariq.uz`
   - `SUPER_ADMIN_BOOTSTRAP_PASSWORD` = `[KuchliParol12+Belgili!]` (kamida 12 belgi, bosh/kichik harf, raqam va maxsus belgi)
3. Backend xizmatini qayta ishga tushiring (Manual Deploy -> Clear build cache & deploy).
4. Tizim ishga tushgach, SuperAdmin yaratiladi va xavfsizlik audit jurnaliga yoziladi.
5. **Muhim**: SuperAdmin yaratilgach, `SUPER_ADMIN_BOOTSTRAP_ENABLED` qiymatini yana `false` ga o'zgartiring. Tizim allaqachon bitta faol SuperAdmin mavjud bo'lganda bootstrapni avtomatik bloklaydi.
