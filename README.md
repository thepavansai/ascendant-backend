# ⚡ Ascendant Initiative — Backend

Gamified cognitive training platform for children aged 9–12.  
**Stack:** Java 21 · Spring Boot 3.2 · PostgreSQL 16 · Redis 7 · Anthropic Claude API

---

## Quick Start (5 minutes)

### 1. Prerequisites
- Java 21+
- Docker + Docker Compose
- Maven 3.9+

### 2. Clone & Configure
```bash
git clone https://github.com/your-org/ascendant-backend.git
cd ascendant-backend
cp .env.example .env
# Edit .env — add your CLAUDE_API_KEY and generate a JWT_SECRET
```

### 3. Start Infrastructure
```bash
docker-compose up -d
# Starts PostgreSQL 5432 + Redis 6379
# Schema + seed data auto-loaded from src/main/resources/db/
```

### 4. Run the Application
```bash
./mvnw spring-boot:run
# API running at http://localhost:8080
```

### 5. Verify Everything Works
```bash
# Register a child user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Priya","email":"priya@test.com","password":"Test@1234","role":"CHILD","parent_email":"dad@test.com"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"priya@test.com","password":"Test@1234"}'

# List missions (use token from login)
curl http://localhost:8080/api/missions \
  -H "Authorization: Bearer <token>"
```

---

## Project Structure

```
src/main/java/com/ascendant/initiative/
├── AscendantInitiativeApplication.java   # Entry point
├── config/          SecurityConfig · AsyncConfig · RedisConfig
├── controller/      AuthController · MissionController · ResponseController
│                    PlayerController · ParentController · AdminController
├── service/         AuthService · MissionService · EvaluationService
│                    ProgressService · GamificationService · PlayerService · ParentService
├── engine/
│   ├── rule/        LengthScorer · KeywordDensityScorer · LogicalConnectorScorer
│   │                HypothesisDetector · ScoreNormalizer · RuleEngine
│   └── ai/          PromptBuilder · LLMClient · ResponseParser
│                    CostController · AIScoreResult
├── model/           User · PlayerProfile · Mission · Scenario · Response
│                    Evaluation · ProgressionLog · ParentChildLink · AiCostLog
├── repository/      (one per entity)
├── dto/             auth/ · mission/ · response/ · evaluation/ · player/ · parent/
├── security/        JwtUtil · JwtAuthFilter
├── util/            XpCalculator · MissionMapper
└── exception/       AppException · GlobalExceptionHandler

src/main/resources/
├── application.yml
└── db/
    ├── schema.sql   (9 tables + indexes — auto-run by Docker)
    └── data.sql     (1 admin user + 5 seed missions — auto-run by Docker)

src/test/java/com/ascendant/initiative/
├── engine/rule/     LengthScorerTest · KeywordDensityScorerTest
│                    LogicalConnectorScorerTest · HypothesisDetectorTest · ScoreNormalizerTest
└── service/         EvaluationScoreMergeTest · XpCalculatorTest
```

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/auth/register | Register child or parent |
| POST | /api/auth/login | Login + get JWT |
| POST | /api/auth/refresh | Refresh JWT |
| GET  | /api/missions | List all missions |
| GET  | /api/missions/next | Next mission for user |
| GET  | /api/missions/{id} | Mission detail + scenario |
| POST | /api/responses | Submit response (async eval) |
| GET  | /api/responses/{id}/evaluation | Poll evaluation result |
| GET  | /api/player/{id}/profile | Player XP, level, attributes |
| GET  | /api/player/{id}/progression | Progression history |
| GET  | /api/player/{id}/stats/weekly | Weekly stats |
| GET  | /api/parent/{id}/dashboard | Parent dashboard |
| POST | /api/parent/{id}/approve/{childId} | Approve child account |
| POST | /api/admin/missions | Create mission (ADMIN) |
| PUT  | /api/admin/missions/{id} | Update mission (ADMIN) |
| DELETE | /api/admin/missions/{id} | Deactivate mission (ADMIN) |

---

## Hybrid Evaluation Engine

```
POST /responses  →  202 PENDING  →  Async thread pool
                                        ├── RuleEngine (sync, <5ms)
                                        │   ├── LengthScorer
                                        │   ├── KeywordDensityScorer
                                        │   ├── LogicalConnectorScorer
                                        │   └── HypothesisDetector
                                        └── AI Engine (async, <8s)
                                                └── Claude API
                                        ↓
                              Dynamic merge: (rule × weight) + (ai × weight)
                              FACTUAL: 0.6/0.4 | ANALYTICAL: 0.3/0.7 | OPEN_ENDED: 0.15/0.85
                                        ↓
                              Save Evaluation + Update Player XP + Log Progression
```

Poll: `GET /responses/{id}/evaluation` every 2s until status = DONE

---

## Running Tests

```bash
./mvnw test
```

---

## Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| DB_URL | PostgreSQL JDBC URL | jdbc:postgresql://localhost:5432/ascendant |
| DB_USER | DB username | ascendant_user |
| DB_PASSWORD | DB password | ascendant_pass |
| REDIS_HOST | Redis host | localhost |
| REDIS_PASSWORD | Redis password | ascendant_redis |
| JWT_SECRET | 64+ char secret | (generate with openssl rand -base64 64) |
| CLAUDE_API_KEY | Anthropic API key | sk-ant-... |

---

## AI Cost Control

- **Daily limit:** 10 AI calls per user per day
- **Token cap:** 300 output tokens per call
- **Fallback:** Rule-only evaluation when limit reached (`aiLimited: true` in response)
- **Tracking:** Every AI call logged in `ai_cost_log` table

