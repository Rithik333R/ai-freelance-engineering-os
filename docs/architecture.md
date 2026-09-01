# System Architecture & Production Hardening Specification

## Overview
AI Freelance Engineering OS is built as a Spring Boot Modular Monolith paired with a React SPA frontend and PostgreSQL (`pgvector`) database.

---

## 🎨 Frontend Architecture (`frontend/`)

```text
frontend/src/
├── api/
│   ├── axiosClient.js     # Axios instance with Bearer JWT interceptor, 401 refresh rotation, cold-start detection
│   ├── authApi.js         # Auth endpoints (login, register, refreshToken, health)
│   ├── clientApi.js       # Client CRUD API calls
│   ├── projectApi.js      # Project CRUD API calls
│   └── taskApi.js         # Task CRUD API calls
├── context/
│   └── AuthContext.jsx    # Session state provider (user, login, register, logout, cold-start signal)
├── components/
│   ├── common/
│   │   ├── Navbar.jsx           # Top header navigation bar with user badge and logout
│   │   ├── ColdStartNotice.jsx  # Cold start notice banner for free-tier server wakeup
│   │   ├── ProtectedRoute.jsx   # Auth guard redirecting unauthenticated users to /login
│   │   └── PublicRoute.jsx      # Auth guard redirecting authenticated users to /dashboard
│   └── layout/
│       └── MainLayout.jsx       # Layout shell wrapping Navbar, ColdStartNotice, and content container
├── pages/
│   ├── LoginPage.jsx       # User authentication page
│   ├── RegisterPage.jsx    # User signup page
│   ├── DashboardPage.jsx   # Workspace metric overview (Active clients, active projects, system status)
│   ├── ClientsPage.jsx     # Client directory management & CRUD modal
│   ├── ProjectsPage.jsx    # Project lifecycle management & CRUD modal
│   ├── TasksPage.jsx       # Project-scoped task board & CRUD modal
│   └── NotFoundPage.jsx    # 404 page
├── styles/
│   └── index.css           # Clean modern SaaS stylesheet
├── App.jsx                 # React Router v6 route hierarchy
└── index.js                # React root mounting App inside BrowserRouter & AuthProvider
```

---

## ⚙️ Backend Configuration Management
Environment-specific properties and secrets are strictly externalized from source code:
- Default values are defined in `backend/src/main/resources/application.yml`.
- Production values are overridden via system environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS`, `PORT`).
- A template template `.env.example` is maintained at the repository root.

---

## 🌐 CORS Configuration
CORS allowed origins are configurable via `app.cors.allowed-origins` (default: `http://localhost:3000`).
- Multi-origin comma-separated lists are supported.
- Unrestricted `*` wildcard origins are forbidden.
- Credentials (`allowCredentials(true)`) are enabled for secure cross-origin API interaction.

---

## 🗄️ Database Indexing Strategy
Flyway manages database migrations (`V1__init.sql`, `V2__make_client_id_optional_in_projects.sql`, `V3__add_performance_indexes.sql`).

| Index Name | Table & Columns | Purpose / Justification |
| :--- | :--- | :--- |
| `users_email_key` | `users(email)` | Enforces unique email constraint and speeds up login queries (`findByEmail`). |
| `idx_clients_user_id` | `clients(user_id)` | Speeds up user-isolated client list and lookup queries (`findByUserId`). |
| `idx_projects_user_id` | `projects(user_id)` | Speeds up user-isolated project list and lookup queries (`findByUserId`). |
| `idx_projects_client_id` | `projects(client_id)` | Accelerates client-project relational lookups and foreign key constraints. |
| `idx_tasks_project_id` | `tasks(project_id)` | Speeds up fetching tasks belonging to a specific project (`findByProjectId`). |
| `idx_tasks_due_date` | `tasks(due_date)` | Speeds up due-date range queries, deadline tracking, and task sorting. |
| `idx_refresh_tokens_token` | `refresh_tokens(token)` | Accelerates refresh token lookups during token renewal calls. |
| `idx_refresh_tokens_user_id` | `refresh_tokens(user_id)` | Accelerates user-based refresh token revocation and cascading user deletions. |

---

## 🛡️ Transaction Boundaries
Service layer methods enforce clear transactional boundaries using Spring's `@Transactional`:
- **Read-Only Transactions**: `@Transactional(readOnly = true)` applied to list and fetch operations. Enables Hibernate read-only optimizations.
- **Mutation Transactions**: `@Transactional` applied to creation, update, and deletion operations. Ensures atomic database operations.

---

## 📄 Pagination & Sorting
Collection GET endpoints (`/api/clients`, `/api/projects`, `/api/projects/{projectId}/tasks`) accept Spring Data `Pageable` parameters:
- Query Parameters: `?page=0&size=20&sort=createdAt,desc`
- Max Page Size Cap: Hard-capped at $100$ items per page in service layer (`Math.min(size, 100)`).
- Standardized Response: Output wrapped in `PagedResponse<T>`.

---

## 🔐 Security Hardening & Refresh Token Rotation
1. **Password Hashing**: Passwords stored as BCrypt hashes (`BCryptPasswordEncoder`).
2. **Stateless Security**: Spring Security configured with `SessionCreationPolicy.STATELESS`.
3. **Short-Lived Access Tokens**: Signed JWT access tokens expire after 15 minutes.
4. **Refresh Token Rotation**:
   When `POST /api/auth/refresh` is invoked:
   - Existing refresh token is validated and deleted.
   - A new 15-minute access token and new 7-day refresh token pair are generated.

---

## 🩺 Health Check Monitoring
Spring Boot Actuator is configured with minimal exposure:
- Endpoint: `/api/actuator/health` returning `{"status":"UP"}`.
