# Render va Neon PostgreSQL orqali Production Deployment Qo'llanmasi

Ushbu qo'llanma **Yangiariq Aqua AI (Agronexsus)** loyihasini **Render** platformasida va **Neon Serverless PostgreSQL** ma'lumotlar bazasida production rejimida ishga tushirish (deploy) bo'yicha to'liq ko'rsatmalarni o'z ichiga oladi.

---

## 🏗 Arxitektura va Chiroyli Nomlar

| Komponent | Xizmat Nomi | Texnologiya | Jonli Havola (URL) | Tavsif |
| :--- | :--- | :--- | :--- | :--- |
| **Frontend** | `yangiariq-aqua` | React 18 + Vite + TS (Static CDN) | `https://yangiariq-aqua.onrender.com` | Foydalanuvchilar kiradigan asosiy veb-ilova |
| **Backend** | `yangiariq-aqua-api` | Spring Boot 3 (Java 21 Docker) | `https://yangiariq-aqua-api.onrender.com` | REST API, Liquibase migratsiyalari, RBAC va biznes-mantiq |
| **Database** | `Neon PostgreSQL` | PostgreSQL 16 (Serverless) | Neon.tech Cloud | Bulutli PostgreSQL ma'lumotlar bazasi |

---

## 🚀 1-usul: Render Blueprint orqali 1 marta bosish bilan deploy qilish (Eng qulay usul)

Loyiha ildizida barcha sozlamalarni o'z ichiga olgan [render.yaml](file:///home/izzatbek/Desktop/agronexsus/render.yaml) blueprint fayli tayyorlangan.

### Bosqichlar:
1. O'zgarishlarni GitHub repositoriyangizga yuboring (push qiling):
   ```bash
   git add .
   git commit -m "feat: configure render blueprint with clean names and production cors"
   git push origin main
   ```

2. [Render Dashboard](https://dashboard.render.com/) ga kiring.
3. Yuqori o'ng burchakdagi **"New +"** tugmasini bosing va **"Blueprint"** ni tanlang.
4. GitHub repositoriyangizni ulang (`yangiariq_aqua` yoki `agronexsus`).
5. Render `render.yaml` faylini avtomatik tahlil qiladi va 2 ta mos xizmatni ko'rsatadi:
   - **`yangiariq-aqua-api`** (Docker Web Service)
   - **`yangiariq-aqua`** (Static Site CDN)
6. So'ralgan o'zgaruvchilarga qiymat bering:
   - **`DATABASE_URL`**: Neon PostgreSQL ulanish satrini kiriting:
     ```text
     postgresql://neondb_owner:npg_OFdN0pikAuf2@ep-fragrant-mouse-begewk9z-pooler.c-9.us-east-2.aws.neon.tech/neondb?sslmode=require&channel_binding=require
     ```
   - **`GEMINI_API_KEY`**: Gemini API kalitingiz (ixtiyoriy, AI tavsiyalar uchun).
7. **"Apply"** tugmasini bosing!  
   Render avtomatik ravishda:
   - Backend konteynerini quradi.
   - Liquibase orqali barcha 51 ta migratsiyani va 67 ta jadvalni yaratadi.
   - Frontend'ni yig'adi (`npm run build`) va global CDN orqali tarqatadi.

---

## 🛠 2-usul: Render Dashboard orqali qo'lda (Manual) sozlash

Agar har bir xizmatni alohida yaratmoqchi bo'lsangiz:

### A. Backend xizmatini sozlash (Web Service)
1. Render Dashboard -> **"New +"** -> **"Web Service"**.
2. GitHub repositoriyangizni tanlang.
3. Quyidagi parametrlarni kiriting:
   - **Name**: `yangiariq-aqua-api`
   - **Region**: `Ohio (US East)` (Neon ham `us-east-2` da bo'lgani uchun eng past kechikish)
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
   | `CORS_ALLOWED_ORIGINS` | `https://yangiariq-aqua.onrender.com,https://yangiariq-aqua-api.onrender.com` |
   | `RATE_LIMIT_ENABLED` | `true` |
   | `ENABLE_SWAGGER_PROD` | `true` |
   | `AI_PROVIDER` | `mock` (yoki `gemini`) |
   | `GEMINI_API_KEY` | *(ixtiyoriy)* |
   | `SENSOR_PROVIDER` | `simulated` |
6. **"Create Web Service"** tugmasini bosing.

---

### B. Frontend xizmatini sozlash (Static Site)
1. Render Dashboard -> **"New +"** -> **"Static Site"**.
2. GitHub repositoriyangizni tanlang.
3. Quyidagi parametrlarni kiriting:
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
6. **"Create Static Site"** tugmasini bosing.

---

## 🔍 Deploy Natijalarini Tekshirish

Deploy tugagach, quyidagi havolalar orqali tizimni tekshirishingiz mumkin:

1. **Frontend Veb Ilovasi**:
   ```
   https://yangiariq-aqua.onrender.com
   ```

2. **Backend API Salomatligi (Actuator Health)**:
   ```
   https://yangiariq-aqua-api.onrender.com/actuator/health
   ```
   *Kutilgan javob: `{"status":"UP"}`*

3. **Swagger / OpenAPI Interaktiv Hujjatlari**:
   ```
   https://yangiariq-aqua-api.onrender.com/swagger-ui.html
   ```
