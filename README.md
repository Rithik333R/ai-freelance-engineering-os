# AI Freelance Engineering OS

A production-grade SaaS operating system designed for software freelancers, technical consultants, and boutique engineering agencies to manage software delivery lifecycles, requirement extraction, project planning, grounded RAG project context, and risk audits.

## 📋 Prerequisites
- **Java 21** LTS
- **Apache Maven 3.9+**
- **Node.js 22+** & **npm 10+**
- **Docker** & **Docker Compose** (for PostgreSQL + pgvector)

## 📁 Repository Structure

```text
ai-freelance-engineering-os/
├── frontend/           # React SPA (React Router, Axios, Interceptors)
├── backend/            # Spring Boot Modular Monolith (Java 21, Spring Security, JWT, OpenAPI)
├── docs/               # System architecture & production hardening specifications
├── docker/             # Docker compose setup for PostgreSQL + pgvector
├── .env.example        # Environment variable template
├── README.md           # Project documentation
└── .gitignore          # Git exclusion rules
```

## ⚙️ Environment Variables

Copy `.env.example` to create your local `.env` file or export the following variables:

| Variable | Description | Default Value |
| :--- | :--- | :--- |
| `PORT` | Backend server port | `8080` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` |
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/freelance_os_db` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `postgrespassword` |
| `JWT_SECRET` | Secret key for signing JWTs | `404E635266556A586E3272357538...` |
| `JWT_EXPIRATION_MS` | Access token expiration (ms) | `900000` (15 mins) |
| `JWT_REFRESH_EXPIRATION_MS` | Refresh token expiration (ms) | `604800000` (7 days) |
| `CORS_ALLOWED_ORIGINS` | Allowed CORS origins | `http://localhost:3000` |

## 🚀 Running the Backend

1. **Start PostgreSQL via Docker**:
   ```bash
   cd docker
   docker-compose up -d
   ```

2. **Run Spring Boot Application**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

   The API will start at: `http://localhost:8080/api`

3. **OpenAPI / Swagger UI**:
   Interactive API documentation is available at:
   - **Swagger UI**: [http://localhost:8080/api/swagger-ui.html](http://localhost:8080/api/swagger-ui.html)
   - **OpenAPI JSON**: [http://localhost:8080/api/v3/api-docs](http://localhost:8080/api/v3/api-docs)

4. **Health Check Endpoint**:
   - Actuator Health: `http://localhost:8080/api/actuator/health`

## 🧪 Running Tests

Execute the automated test suite (includes authentication, JWT, token rotation, resource authorization, and validation tests):

```bash
cd backend
mvn clean test
```
