# SmishGuard – Bank SMS Scam Detector

> Detect fake (smishing / scam) bank SMS messages for Indian users.
> Paste a message → get **GENUINE / SUSPICIOUS / FAKE** verdict with reasons.

---

## Architecture

```
User browser
    │
    ▼
React + Vite (frontend/)
    │  POST /api/check
    ▼
Spring Boot (src/)
    │
    ├─ Step 1: MaskingService     – hides OTP, account#, phone before anything
    ├─ Step 2: RuleCheckService   – 8 cheap rule checks, score 0–60
    ├─ Step 3: UrlScanService     – Google Safe Browsing (optional, needs key)
    ├─ Step 4: LlmService         – Gemini AI (optional, only for uncertain msgs)
    └─ Step 5: VerdictService     – combines scores → GENUINE / SUSPICIOUS / FAKE
         │
         └─ H2 (local) or PostgreSQL (prod) via Spring Data JPA
```

### Score bands
| Score | Verdict     |
|-------|-------------|
| 0–29  | GENUINE     |
| 30–59 | SUSPICIOUS  |
| 60+   | FAKE        |

---

## How to Run Locally

### Backend
```bash
# Java 21 must be installed and JAVA_HOME set
cd smishguard
./mvnw spring-boot:run
# API available at http://localhost:8080
# H2 console at http://localhost:8080/h2-console (local profile)
```

### Frontend
```bash
cd smishguard/frontend
npm install
npm run dev
# App at http://localhost:5173
```

---

## Running Tests

```bash
cd smishguard
./mvnw test
```

This runs:
- `SmishguardApplicationTests` – context + API smoke tests
- `MaskingServiceTest` – masking unit tests
- `RuleCheckServiceTest` – rule engine integration tests
- `VerdictServiceTest` – score band unit tests
- `PipelineEvalTest` – 40 test messages with accuracy/precision/recall/F1 report

> **Note**: `test_messages.csv` contains example test data.
> Replace it with your own real, masked messages for a meaningful evaluation.
> See `src/main/resources/data/README.md` for CSV format.

---

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `SPRING_PROFILES_ACTIVE` | No | `local` (H2, default) or `prod` (PostgreSQL) |
| `GEMINI_API_KEY` | No | Enables AI explanations (Gemini 1.5 Flash) |
| `SAFE_BROWSING_API_KEY` | No | Enables Google Safe Browsing link checks |
| `DATABASE_URL` | Prod only | PostgreSQL JDBC URL |
| `DB_USERNAME` | Prod only | DB username |
| `DB_PASSWORD` | Prod only | DB password |
| `ALLOWED_ORIGINS` | No | CORS origins (comma-separated) |
| `ADMIN_KEY` | No | Header key for `GET /api/reports` |
| `VITE_API_URL` | No | Backend URL for frontend build |

Copy `.env.example` → `.env` and fill in the values.

---

## Adding Your Own Seed Messages

See `src/main/resources/data/README.md` for full instructions.

**Short version:**
1. Mask personal numbers (account → `XXXXXX1234`, OTP → `<OTP>`, phone → `<PHONE>`)
2. Add a row to the appropriate CSV file
3. Restart the app (H2 is in-memory so the table is empty each restart)

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/check` | Run detection pipeline |
| GET | `/api/history` | Last 20 checks |
| GET | `/api/stats` | Aggregated statistics |
| GET | `/api/sender/{code}` | Lookup bank sender code |
| POST | `/api/report` | Submit user feedback |
| GET | `/api/reports` | Admin: unreviewed reports (needs ADMIN_KEY header) |
| GET | `/api/health` | Health check |

---

## Limitations

- **Not 100% accurate.** Rule-based scoring is heuristic.
- **The LLM can be wrong.** Gemini is used only for borderline messages.
- **Small seed data.** The bundled examples are illustrative, not exhaustive.
- **Sender IDs can be spoofed.** A known sender header does not guarantee the message is genuine.
- **Language coverage.** Hindi/Telugu detection is basic keyword matching.
- **No real-time blocklist.** Without Safe Browsing API key, links are not scanned.

---

## Project Structure

```
smishguard/
├── src/main/java/com/smishguard/smishguard/
│   ├── config/          CorsConfig, RateLimitFilter, RestClientConfig
│   ├── controller/      CheckController, HistoryController, StatsController,
│   │                    SenderController, ReportController, HealthController
│   ├── entity/          JPA entities (6 tables)
│   ├── exception/       GlobalExceptionHandler
│   ├── model/           CheckRequest, CheckResponse (Java records)
│   ├── repository/      Spring Data JPA repositories
│   └── service/         DataSeederService, MaskingService, RuleCheckService,
│                        UrlScanService, LlmService, VerdictService
├── src/main/resources/
│   ├── application*.properties
│   └── data/            CSV seed files + README
├── src/test/            JUnit tests + PipelineEvalTest
└── frontend/            React + Vite app
```
