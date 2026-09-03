# AI Freelance Engineering OS

> A full-stack, enterprise-ready SaaS operating system for software freelancers, engineering consultants, and boutique agencies to manage clients, project budgets, technical tasks, and an interactive **AI Assistant** powered by **RAG (Retrieval-Augmented Generation)**, vector search, and user-confirmed AI Actions.

---

## 🌟 Key Features

### 🏢 Core Domain Management
- **User Authentication**: Secure JWT access tokens, HTTP-only refresh tokens, BCrypt password hashing, and role-based access control (`ROLE_FREELANCER`, `ROLE_CLIENT`).
- **Client Management**: Track company records, contact emails, phone numbers, and notes.
- **Project Tracking**: Manage project budgets, start/end dates, client ownership, and status lifecycles (`PLANNING`, `IN_PROGRESS`, `COMPLETED`, `ON_HOLD`).
- **Task Management**: Break projects into technical tasks with priority levels (`LOW`, `MEDIUM`, `HIGH`), estimated hours, due dates, and statuses (`TODO`, `IN_PROGRESS`, `COMPLETED`).
- **Strict Data Isolation**: Enforces multi-tenant user scoping on every database query. User A can never view or modify User B's resources.

### 🤖 AI Assistant & Intelligence Layer (Google Gemini & Vector RAG)
- **Persistent Multi-Turn Conversations**: Database-backed chat threads with history preservation.
- **Semantic Vector Retrieval (RAG)**: Integrates Google GenAI `text-embedding-004` with PostgreSQL `pgvector` and Cosine Similarity search to inject *only* relevant workspace context into prompts.
- **AI Action Extraction & Execution**:
  - Safely extracts action intent for `CREATE_CLIENT`, `CREATE_PROJECT`, and `CREATE_TASK`.
  - Displays an interactive **Action Proposal Card** in the chat UI.
  - Requires explicit user confirmation (`confirmed: true`) before creating database records.
- **Anti-Hallucination Guardrails**: Strictly grounded in authenticated user workspace data.

### 💻 Modern Responsive Frontend & Dashboard
- **React SPA**: Built with React Router v6, Axios interceptors, and modern dark-mode UI.
- **AI Workspace Control Center Dashboard**: Live metric counters, quick AI command bar, active projects list, and real-time AI capability status indicators.
- **Mobile Responsive Layout**: Sliding chat drawer and responsive navbar with hamburger navigation for viewports $\le 768\text{px}$.

---

## 🏗️ Tech Stack

### Backend
- **Java 21** & **Spring Boot 3.3.4**
- **Spring Security & JWT** (Access Tokens + Token Rotation)
- **Spring Data JPA & Hibernate**
- **Flyway Database Migrations** (v1 to v5)
- **Google GenAI Java SDK** (`gemini-2.5-flash`, `text-embedding-004`)
- **OpenAPI 3.0 / Swagger UI** & **Spring Boot Actuator**

### Database & Storage
- **PostgreSQL 16** with **`pgvector`** extension (In-Memory H2 fallback for testing)

### Frontend
- **React 18** & **React Router v6**
- **Axios** (with automatic token refreshing)
- **CSS3 / Modern Flex & Grid** (Zero heavy UI framework bloat)

---

## 📁 Repository Architecture

```text
ai-freelance-engineering-os/
├── backend/                  # Spring Boot backend application
│   ├── src/main/java/com/freelance/os/
│   │   ├── ai/               # AI Service, Context Builder, RAG, Action Executor/Extractor
│   │   ├── auth/             # Authentication & Registration Controllers/Services
│   │   ├── client/           # Client Management Module
│   │   ├── config/           # Security, OpenAPI, & App Configuration
│   │   ├── project/          # Project Management Module
│   │   ├── security/         # JwtAuthenticationFilter & UserDetails
│   │   ├── task/             # Task Management Module
│   │   └── user/             # User Domain Entities & Repositories
│   └── src/main/resources/
│       └── db/migration/     # Flyway SQL migration scripts (V1..V5)
├── frontend/                 # React SPA Single Page Application
│   ├── src/
│   │   ├── api/              # Axios API clients
│   │   ├── components/       # Layout, Navbar, ProtectedRoute components
│   │   ├── context/          # AuthContext Provider
│   │   ├── pages/            # Dashboard, Clients, Projects, Tasks, AI Chat, Auth Pages
│   │   └── styles/           # CSS3 Responsive Design System
├── docker/                   # Docker Compose setup for PostgreSQL + pgvector
├── .env.example              # Environment variable template
└── README.md                 # System documentation
```

---

## ⚙️ Environment Variables

Copy `.env.example` to create your `.env` file:

| Variable | Description | Default Value |
| :--- | :--- | :--- |
| `PORT` | Backend server port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile (`dev` / `test`) | `dev` |
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/freelance_os_db` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `postgrespassword` |
| `GEMINI_API_KEY` | Google Gemini API Key | *(Your Gemini API Key)* |
| `JWT_SECRET` | Secret key for signing JWTs | *(256-bit secret key)* |
| `JWT_EXPIRATION_MS` | Access token expiration (ms) | `900000` (15 mins) |
| `JWT_REFRESH_EXPIRATION_MS` | Refresh token expiration (ms) | `604800000` (7 days) |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | `http://localhost:3000` |

---

## 🚀 Getting Started

### 1. Prerequisites
- **Java 21 LTS**
- **Maven 3.9+**
- **Node.js 18+** & **npm 10+**
- **Docker & Docker Compose** (for PostgreSQL)

---

### 2. Start Database (PostgreSQL + pgvector)

```bash
cd docker
docker-compose up -d
```

---

### 3. Start Backend Application

```bash
cd backend
mvn spring-boot:run
```

- **Backend Base URL**: `http://localhost:8080/api`
- **Swagger UI Docs**: [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)
- **Actuator Health**: [http://localhost:8080/api/actuator/health](http://localhost:8080/api/actuator/health)

---

### 4. Start Frontend Application

```bash
cd frontend
npm install
npm start
```

- **Frontend Application URL**: [http://localhost:3000](http://localhost:3000)

---

## 🧪 Testing & Verification

Run the full automated backend test suite (67+ unit and integration tests):

```bash
cd backend
mvn clean test
```

### Print Registered Users List (Helper Command)

To view all registered users in your local dev database directly in the terminal:

```bash
cd backend
mvn test -Dtest=PrintDevUsersTest
```

---

## 🔗 Key API Endpoints Summary

| Endpoint | Method | Description | Auth Required |
| :--- | :--- | :--- | :--- |
| `/api/auth/register` | `POST` | Register a new user | ❌ No |
| `/api/auth/login` | `POST` | Authenticate user & get JWT tokens | ❌ No |
| `/api/auth/refresh` | `POST` | Refresh access token using refresh token | ❌ No |
| `/api/clients` | `GET`/`POST` | List & create user clients | ✅ Yes |
| `/api/projects` | `GET`/`POST` | List & create user projects | ✅ Yes |
| `/api/projects/{id}/tasks` | `GET`/`POST` | List & create tasks for a project | ✅ Yes |
| `/api/ai/conversations` | `GET`/`POST` | List & create AI chat threads | ✅ Yes |
| `/api/ai/conversations/{id}/messages` | `POST` | Send message to AI assistant | ✅ Yes |
| `/api/ai/actions/extract` | `POST` | Extract action proposal DTO from prompt | ✅ Yes |
| `/api/ai/actions/execute` | `POST` | Execute confirmed AI action | ✅ Yes |

---

## 🛡️ License

This project is open-source and available under the **MIT License**.
