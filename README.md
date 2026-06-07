# ⚡ Ascendant Initiative — Backend

> *A gamified cognitive training platform that turns thinking into an adventure for kids aged 9–12.*

**🌍 Live Frontend:** [ascendantt.netlify.app](https://ascendantt.netlify.app)  
**⚙️ Live Backend:** [ascendant-backend.onrender.com](https://ascendant-backend.onrender.com)  

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-red?style=flat-square&logo=redis)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker_Hub-thepavansai%2Fascendant--backend-blue?style=flat-square&logo=docker)](https://hub.docker.com/repository/docker/thepavansai/ascendant-backend/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

---

## 🌟 What is Ascendant Initiative?

Imagine if solving real-world problems — like fixing a broken bridge, running a lemonade stand, or spotting AI misinformation — **earned you XP, levelled you up, and shaped your identity as a thinker**.

That's Ascendant Initiative. Kids read a scenario, write their reasoning, and get evaluated by a **hybrid AI + rule engine** that scores their logic, judgment, awareness, and clarity. Parents can track progress. Admins can manage missions. Everyone wins. 🧠🎮

---

## 🏗️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.2.5 |
| Database | PostgreSQL 16 |
| Cache | Redis 7 |
| AI Provider | Anthropic Claude / Groq / Any OpenAI-compatible API |
| Auth | JWT (Access + Refresh tokens) |
| HTTP Client | Spring WebFlux WebClient |
| Build Tool | Maven 3.9+ |
| Containerization | Docker + Docker Compose |

---

## 🎮 How the Game Works

```
Kid reads a mission scenario
         ↓
Kid writes their answer (min 10 words, max 2000)
         ↓
POST /api/responses → 202 ACCEPTED (instant!)
         ↓
         ┌─────────────────────────────────┐
         │       Async Evaluation          │
         │                                 │
         │  Rule Engine (~5ms)             │
         │  ├── Length Scorer              │
         │  ├── Keyword Density Scorer     │
         │  ├── Logical Connector Scorer   │
         │  └── Hypothesis Detector        │
         │                                 │
         │  AI Engine (~5s)                │
         │  └── Claude / Groq API          │
         │       ├── intellect score       │
         │       ├── judgment score        │
         │       ├── awareness score       │
         │       ├── clarity score         │
         │       └── friendly feedback     │
         └─────────────────────────────────┘
                        ↓
         Dynamic Score Merge by Mission Type:
         FACTUAL     → Rule 60% + AI 40%
         ANALYTICAL  → Rule 30% + AI 70%
         OPEN_ENDED  → Rule 15% + AI 85%
                        ↓
         XP Awarded + Level Updated + Streak Tracked
                        ↓
         Frontend polls GET /api/responses/{id}/evaluation
         until status = DONE 🎉
```

---

## 🧬 Cognitive Identity System

Every child develops a **unique cognitive identity** based on their strongest attribute:

| Identity | Strongest Attribute | What it means |
|----------|-------------------|---------------|
| 🔬 **ANALYST** | Intellect | Loves logic, evidence, and reasoning |
| ♟️ **STRATEGIST** | Judgment | Great at decisions and trade-offs |
| 🌍 **CREATOR** | Awareness | Sees the big picture and implications |
| 🏗️ **BUILDER** | Clarity | Communicates ideas simply and powerfully |

Attributes are updated using an **Exponential Moving Average** after every mission:
```
new_attribute = (old × 0.85) + (score × 10 × 0.15)
```
This prevents single-mission spikes and creates smooth, realistic progression. 📈

---

## 🚀 Quick Start (5 minutes)

### Prerequisites
- Java 21+
- Docker + Docker Compose
- Maven 3.9+

### 1. Clone the repo
```bash
git clone https://github.com/your-org/ascendant-backend.git
cd ascendant-backend
```

### 2. Set up environment
```bash
cp .env.example .env
```

Fill in all values — never leave secrets blank:
```env
DB_NAME=ascendant
DB_USER=ascendant_user
DB_PASSWORD=your_db_password
DB_PORT=5432
DB_URL=jdbc:postgresql://ep-your-db.us-east-1.aws.neon.tech/neondb?sslmode=require
DB_USER=neondb_owner
DB_PASSWORD=your_neon_password
REDIS_ENABLED=false
JWT_SECRET=<generate with: openssl rand -base64 64>
CLAUDE_API_KEY=<your API key>
AI_API_URL=https://api.groq.com/openai/v1/chat/completions
AI_MODEL=openai/gpt-oss-120b
FRONTEND_URL=https://ascendantt.netlify.app
```

### 3. Start infrastructure
```bash
docker-compose up -d
# Starts PostgreSQL on :5432 and Redis on :6379
# Schema + 5 seed missions auto-loaded!
```

### 4. Run the app
```bash
./mvnw spring-boot:run
# API live at http://localhost:8080 🚀
```

### 5. Verify it works
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@ascendant.app","password":"Admin@1234"}'
```

You should get back a JWT token. You're in! 🎉

---

## 📁 Project Structure

```
ascendant-backend/
├── src/main/java/com/ascendant/initiative/
│   ├── AscendantInitiativeApplication.java   # 🚀 Entry point
│   │
│   ├── config/
│   │   ├── SecurityConfig.java               # JWT + CORS + route rules
│   │   ├── AsyncConfig.java                  # Thread pool for eval engine
│   │   └── RedisConfig.java                  # Redis serialization setup
│   │
│   ├── controller/
│   │   ├── AuthController.java               # Register, Login, Refresh
│   │   ├── MissionController.java            # List, Detail, Next mission
│   │   ├── ResponseController.java           # Submit answer, Poll result
│   │   ├── PlayerController.java             # Profile, Progression, Stats
│   │   ├── ParentController.java             # Dashboard, Approve child
│   │   └── AdminController.java              # CRUD missions, List users
│   │
│   ├── service/
│   │   ├── AuthService.java                  # Registration + login logic
│   │   ├── EvaluationService.java            # 🧠 Core eval orchestration
│   │   ├── MissionService.java               # Mission fetch + Redis cache
│   │   ├── ProgressService.java              # XP + level + streak updates
│   │   ├── GamificationService.java          # Attribute blending + identity
│   │   ├── PlayerService.java                # Profile + stats queries
│   │   └── ParentService.java                # Parent dashboard + linking
│   │
│   ├── engine/
│   │   ├── rule/
│   │   │   ├── RuleEngine.java               # Orchestrates all rule scorers
│   │   │   ├── LengthScorer.java             # Word count scoring
│   │   │   ├── KeywordDensityScorer.java     # Reasoning keyword detection
│   │   │   ├── LogicalConnectorScorer.java   # Contrast + causation scoring
│   │   │   ├── HypothesisDetector.java       # If-then + what-if detection
│   │   │   └── ScoreNormalizer.java          # Weighted score aggregation
│   │   └── ai/
│   │       ├── LLMClient.java                # 🤖 Universal AI provider client
│   │       ├── PromptBuilder.java            # Builds evaluation prompts
│   │       ├── ResponseParser.java           # Parses AI JSON response
│   │       ├── CostController.java           # Daily quota enforcement
│   │       └── AIScoreResult.java            # AI score data model
│   │
│   ├── model/                                # JPA entities
│   ├── repository/                           # Spring Data repositories
│   ├── dto/                                  # Request/Response DTOs
│   ├── security/                             # JWT util + auth filter
│   ├── util/                                 # XpCalculator, MissionMapper
│   └── exception/                            # AppException + global handler
│
├── src/main/resources/
│   ├── application.yml                       # App configuration
│   └── db/
│       ├── schema.sql                        # 9 tables + indexes
│       └── data.sql                          # 1 admin + 5 seed missions
│
├── src/test/                                 # Unit tests
├── docker-compose.yml                        # PostgreSQL + Redis
├── .env.example                             
└── pom.xml                                   # Maven dependencies
```

---

## 🌐 API Reference

### 🔐 Auth
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/register` | Register child or parent | ❌ |
| POST | `/api/auth/login` | Login + get JWT | ❌ |
| POST | `/api/auth/refresh` | Refresh access token | ❌ |
| POST | `/api/auth/logout` | Logout (stateless) | ✅ |

### 🎯 Missions
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/missions` | List all active missions | ✅ |
| GET | `/api/missions/next` | Next incomplete mission | ✅ |
| GET | `/api/missions/{id}` | Mission detail + scenario | ✅ |

### 📝 Responses
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/responses` | Submit answer (async eval) | ✅ |
| GET | `/api/responses/{id}/evaluation` | Poll evaluation result | ✅ |

### 👤 Player
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/player/{id}/profile` | XP, level, attributes, identity | ✅ |
| GET | `/api/player/{id}/progression` | Mission history + XP log | ✅ |
| GET | `/api/player/{id}/stats/weekly` | Weekly performance summary | ✅ |

### 👨‍👩‍👧 Parent
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/parent/{id}/dashboard` | All children's stats | PARENT |
| POST | `/api/parent/{id}/approve/{childId}` | Approve child account | PARENT |
| POST | `/api/parent/link` | Link parent to child | PARENT |

### 🛡️ Admin
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/admin/users` | List all users | ADMIN |
| POST | `/api/admin/missions` | Create mission | ADMIN |
| PUT | `/api/admin/missions/{id}` | Update mission | ADMIN |
| DELETE | `/api/admin/missions/{id}` | Deactivate mission | ADMIN |

---

## 🌱 Seed Data

The app ships with **10 real-world thinking missions** out of the box, perfectly balanced between objective analysis and free-form logic:

| # | Mission | Type | Difficulty |
|---|---------|------|------------|
| 1 | 🚀 The Cosmic Dilemma | ANALYTICAL | ⭐⭐⭐ |
| 2 | 🦠 Cyber City Defender | FACTUAL | ⭐⭐ |
| 3 | ⚗️ Martian Botanist | ANALYTICAL | ⭐⭐⭐ |
| 4 | 🤖 The Robot Interpreter | FACTUAL | ⭐ |
| 5 | 🔐 Quantum Code Breaker | ANALYTICAL | ⭐⭐⭐⭐ |
| 6 | 🌊 Ocean Rescue Drone | OPEN_ENDED | ⭐⭐ |
| 7 | 🏛️ The Lost Pharaoh's Tomb | OPEN_ENDED | ⭐⭐⭐⭐ |
| 8 | ⏳ Time Traveler's Paradox | OPEN_ENDED | ⭐⭐⭐⭐⭐ |
| 9 | 🌴 Amazon Jungle Survival | OPEN_ENDED | ⭐⭐⭐ |
| 10 | 👽 The Galactic Council | OPEN_ENDED | ⭐⭐⭐⭐⭐ |

---

## 📊 XP & Levelling System

```
XP Earned = BASE_XP (100) × finalScore × difficultyMultiplier

Difficulty Multipliers:
  Level 1 → 0.5x  |  Level 2 → 1.0x  |  Level 3 → 1.5x
  Level 4 → 2.0x  |  Level 5 → 3.0x

Level Thresholds:
  Level 1  →      0 XP     Level 6  →  4,000 XP
  Level 2  →    200 XP     Level 7  →  5,500 XP
  Level 3  →    500 XP     Level 8  →  7,200 XP
  Level 4  →  1,000 XP     Level 9  →  9,000 XP
  Level 5  →  1,800 XP     Level 10 →  MAX 🏆
```

---

## 🤖 AI Cost Control

To keep API costs in check:

- **Daily limit:** 10 AI calls per user per day (configurable)
- **Token cap:** 300 output tokens per call
- **Fallback:** When limit is hit → rule-only evaluation with `aiLimited: true`
- **Tracking:** Every AI call logged in `ai_cost_log` table

---

## 🔌 AI Provider Support

The `LLMClient` auto-detects the provider from `AI_API_URL` and adjusts headers and response parsing automatically:

| Provider | API URL | Auth Header |
|----------|---------|-------------|
| Anthropic | `https://api.anthropic.com/v1/messages` | `x-api-key` |
| Groq | `https://api.groq.com/openai/v1/chat/completions` | `Authorization: Bearer` |
| OpenAI | `https://api.openai.com/v1/chat/completions` | `Authorization: Bearer` |
| OpenRouter | `https://openrouter.ai/api/v1/chat/completions` | `Authorization: Bearer` |

---

## 🧪 Running Tests

```bash
./mvnw test
```

Test coverage includes:
- `LengthScorerTest` — word count scoring edge cases
- `KeywordDensityScorerTest` — reasoning keyword detection
- `LogicalConnectorScorerTest` — contrast + causation scoring
- `HypothesisDetectorTest` — if-then + what-if detection
- `ScoreNormalizerTest` — weighted aggregation
- `EvaluationScoreMergeTest` — rule + AI merge logic
- `XpCalculatorTest` — XP and level calculations

---

## 🔒 Security Notes

- All passwords hashed with **BCrypt (cost factor 12)**
- JWT access tokens expire in **15 minutes**
- JWT refresh tokens expire in **7 days**
- Admin routes protected by **role-based access control**
- CORS restricted to `FRONTEND_URL` env variable
- **Never commit `.env`** — it's in `.gitignore`

---

## 🐳 Docker Services

### 🚀 Run Pre-built Image from Docker Hub
You can run the entire backend instantly without building the code by pulling the official image:
```bash
# Assuming you have a .env file locally
docker run -p 8080:8080 --env-file .env thepavansai/ascendant-backend:latest
```

### 🛠️ Local Development (Docker Compose)
```yaml
Services:
  postgres  → localhost:5432  (ascendant DB)
  redis     → localhost:6379  (mission cache + future token blacklist)
```

```bash
# Start
docker-compose up -d

# Stop
docker-compose down

# Pass env file explicitly (if needed)
docker-compose --env-file .env up -d

# Wipe data and start fresh
docker-compose down -v && docker-compose up -d
```

---

## 🤝 Contributing

1. Fork the repo
2. Create your feature branch: `git checkout -b feat/amazing-feature`
3. Commit your changes: `git commit -m 'feat: add amazing feature'`
4. Push to the branch: `git push origin feat/amazing-feature`
5. Open a Pull Request

---

## 📄 License

MIT License — see [LICENSE](LICENSE) for details.

---

<div align="center">

Built with ❤️ for curious kids everywhere 🌍

*"Every great thinker started by asking why."*

</div>