# SmishGuard – Deployment Guide

---

## Backend → Render or Railway (free tier)

### Option A: Render

1. Push the repository to GitHub.
2. Go to [render.com](https://render.com) → New → **Web Service**.
3. Connect your GitHub repo.
4. Settings:
   - **Build command**: `./mvnw package -DskipTests`
   - **Start command**: `java -jar target/smishguard-*.jar`
   - **Instance type**: Free
5. Under **Environment Variables**, add:
   ```
   SPRING_PROFILES_ACTIVE = prod
   DATABASE_URL = jdbc:postgresql://<host>/<dbname>
   DB_USERNAME  = <your-db-user>
   DB_PASSWORD  = <your-db-password>
   ALLOWED_ORIGINS = https://your-frontend.vercel.app
   ADMIN_KEY    = your-secret-admin-key
   GEMINI_API_KEY = (optional)
   SAFE_BROWSING_API_KEY = (optional)
   ```
6. Click **Deploy**. First deploy takes 3–5 minutes to download Maven deps.

> ⚠️ **Free servers sleep when idle.** The first request after inactivity takes 30–60 seconds.
> Upgrade to a paid plan to avoid this.

### Option B: Railway

1. Go to [railway.app](https://railway.app) → New Project → **Deploy from GitHub Repo**.
2. Railway auto-detects the Dockerfile. Add the same environment variables above.
3. Railway provides a free PostgreSQL plugin — use its connection string for `DATABASE_URL`.

---

## Free PostgreSQL options

| Service | Free tier | Notes |
|---------|-----------|-------|
| [Neon](https://neon.tech) | 512 MB | PostgreSQL 16, auto-suspend |
| [Supabase](https://supabase.com) | 500 MB | PostgreSQL 15, pauses after 1 week inactive |
| Railway Plugin | 1 GB | Only available on Railway |

Get the JDBC URL from the service dashboard. Format:
```
jdbc:postgresql://<host>:<port>/<dbname>?sslmode=require
```

---

## Frontend → Vercel or Netlify

### Vercel

1. Go to [vercel.com](https://vercel.com) → New Project → import your repo.
2. Set the **Root Directory** to `frontend`.
3. Framework preset: **Vite**.
4. Add environment variable:
   ```
   VITE_API_URL = https://your-backend.onrender.com
   ```
5. Deploy.

### Netlify

1. Go to [netlify.com](https://netlify.com) → Add new site → Import from Git.
2. Base directory: `frontend`
3. Build command: `npm run build`
4. Publish directory: `dist`
5. Add `VITE_API_URL` environment variable.

---

## Local PostgreSQL with Docker Compose

```bash
# Start PostgreSQL + backend together
docker compose up

# Backend at http://localhost:8080
# PostgreSQL at localhost:5432
```

---

## Checklist before going live

- [ ] `ADMIN_KEY` is a strong random string
- [ ] `ALLOWED_ORIGINS` points to your exact frontend URL (no trailing slash)
- [ ] `GEMINI_API_KEY` added for AI features (optional)
- [ ] `SAFE_BROWSING_API_KEY` added for link scanning (optional)
- [ ] `.env` file is NOT committed to git
- [ ] Test `POST /api/check` with one fake and one genuine message after deploy
