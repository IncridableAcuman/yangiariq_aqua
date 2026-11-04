# Render va Neon PostgreSQL orqali Production Deployment Qo'llanmasi

Ushbu qo'llanma **Yangiariq Aqua AI (Agronexsus)** loyihasini **Render** platformasida va **Neon Serverless PostgreSQL** ma'lumotlar bazasida production rejimida ishga tushirish (deploy) bo'yicha to'liq ko'rsatmalarni o'z ichiga oladi.

---

## 🏗 Arxitektura

| Komponent | Texnologiya | Joylashuv | Tavsif |
| :--- | :--- | :--- | :--- |
| **Backend** | Spring Boot 3 (Java 21) | Render Web Service (Docker) | REST API, Liquibase migratsiyalari, xavfsizlik va biznes-mantiq |
| **Database** | PostgreSQL 16 (Serverless) | Neon.tech | Bulutli ma'lumotlar bazasi (`backend/.env` dagi URL) |
| **Frontend** | React 18 + Vite + TS | Render Static Site (Global CDN) | SPA interfeysi, tezkor va bepul CDN hosting |

---

## 🚀 1-usul: Render Blueprint orqali 1 marta bosish bilan deploy qilish (Tavsiya etiladi)

Loyiha ildizida barcha sozlamalarni o'z ichiga olgan [render.yaml](file:///home/izzatbek/Desktop/agronexsus/render.yaml) blueprint fayli tayyorlangan.

### Bosqichlar:
1. O'zgarishlarni GitHub repositoriyangizga push qiling:
   ```bash
   git add .
   git commit -m "feat: prepare production deployment for Render and Neon"
   git push origin main
   ```

2. [Render Dashboard](https://dashboard.render.com/) ga kiring.
3. Yuqori o'ng burchakdagi **"New +"** tugmasini bosing va **"Blueprint"** ni tanlang.
4. GitHub repositoriyangizni ulang (`agronexsus`).
5. Render `render.yaml` faylini avtomatik tahlil qiladi va 2 ta xizmatni ko'rsatadi:
   - `yangiariq-aqua-backend` (Docker Web Service)
   - `yangiariq-aqua-frontend` (Static Site)
6. So'ralgan o'zgaruvchilar:
   - `DATABASE_URL`: `backend/.env` faylidagi Neon URL manzilini kiriting:
     ```text
     postgresql://neondb_owner:npg_OFdN0pikAuf2@ep-fragrant-mouse-begewk9z-pooler.c-9.us-east-2.aws.neon.tech/neondb?sslmode=require&channel_binding=require
     ```
   - `GEMINI_API_KEY`: Gemini API kalitingiz (agar mavjud bo'lsa).
7. **"Apply"** tugmasini bosing. Render avtomatik ravishda backend'ni qura boshlaydi, Liquibase orqali Neon'da 32 ta jadvalni yaratadi va frontend'ni deploy qiladi!

---

## 🛠 2-usul: Render Dashboard orqali qo'lda (Manual) sozlash

Agar har bir xizmatni alohida yaratmoqchi bo'lsangiz:

### A. Backend xizmatini sozlash (Web Service)
1. Render Dashboard -> **"New +"** -> **"Web Service"**.
2. GitHub repositoriyangizni tanlang.
3. Quyidagi parametrlarni kiriting:
   - **Name**: `yangiariq-aqua-backend`
   - **Region**: `Ohio (US East)` (Neon ham `us-east-2` da bo'lgani uchun eng past kechikish - latency)
   - **Branch**: `main`
   - **Root Directory**: `backend`
   - **Runtime**: `Docker`
   - **Dockerfile Path**: `Dockerfile` (yoki ildizdan bo'lsa: `backend/Dockerfile`)
   - **Plan**: `Free`
4. **Health Check Path** (Advanced bo'limida):
   - `/actuator/health`
5. **Environment Variables**:
   | Kalit (Key) | Qiymat (Value) |
   | :--- | :--- |
   | `SPRING_PROFILES_ACTIVE` | `prod` |
   | `DATABASE_URL` | *Neon URL manzili (`backend/.env` dan)* |
   | `JWT_SECRET` | *(Render avtomatik yaratishi yoki 64+ belgili tasodifiy string)* |
   | `CORS_ALLOWED_ORIGINS` | `https://yangiariq-aqua-frontend.onrender.com,http://localhost:3000` |
   | `AI_PROVIDER` | `mock` (yoki `gemini`) |
   | `GEMINI_API_KEY` | *(ixtiyoriy)* |
   | `SENSOR_PROVIDER` | `simulated` |
6. **"Create Web Service"** tugmasini bosing.

---

### B. Frontend xizmatini sozlash (Static Site)
1. Render Dashboard -> **"New +"** -> **"Static Site"**.
2. GitHub repositoriyangizni tanlang.
3. Quyidagi parametrlarni kiriting:
   - **Name**: `yangiariq-aqua-frontend`
   - **Branch**: `main`
   - **Root Directory**: `frontend`
   - **Build Command**: `npm ci && npm run build`
   - **Publish Directory**: `dist`
4. **Environment Variables**:
   | Kalit (Key) | Qiymat (Value) |
   | :--- | :--- |
   | `VITE_API_BASE_URL` | `https://yangiariq-aqua-backend.onrender.com/api/v1` |
   | `VITE_DEFAULT_LANGUAGE` | `uz` |
5. **Redirects/Rewrites (SPA routing uchun muhim)**:
   - **Type**: `Rewrite`
   - **Source**: `/*`
   - **Destination**: `/index.html`
6. **"Create Static Site"** tugmasini bosing.

---

## 🔒 Neon PostgreSQL integratsiyasining avtomatik mexanizmi

Spring Boot ilovasiga [`DatabaseUrlEnvironmentPostProcessor`](file:///home/izzatbek/Desktop/agronexsus/backend/src/main/java/com/aquaai/shared/config/DatabaseUrlEnvironmentPostProcessor.java) qo'shildi:
- Neon yoki Render tomonidan beriladigan standart `postgresql://user:password@host/db?...` formatidagi URL'ni avtomatik ravishda tahlil qiladi (parse).
- Username va Parolni xavfsiz ajratib oladi va JDBC drayveri talab qiladigan toza `jdbc:postgresql://host:port/db?sslmode=require...` formatiga o'tkazadi.
- HikariCP connection pool parametrlarini serverless Neon arxitekturasi uchun optimallashtiradi (`keepalive-time: 300s`, `max-lifetime: 600s`).
- Render'ning dinamik `PORT` parametrini avtomatik taniydi.

---

## 🔍 Tekshirish (Verification)

Deploy tugagach, quyidagi havolalar orqali tizimni tekshirishingiz mumkin:

1. **Backend Salomatligi (Actuator Health)**:
   ```
   https://yangiariq-aqua-backend.onrender.com/actuator/health
   ```
   *Kutilgan javob: `{"status":"UP"}`*

2. **Swagger / OpenAPI Hujjatlari**:
   ```
   https://yangiariq-aqua-backend.onrender.com/swagger-ui.html
   ```

3. **Frontend Veb Ilovasi**:
   ```
   https://yangiariq-aqua-frontend.onrender.com
   ```
