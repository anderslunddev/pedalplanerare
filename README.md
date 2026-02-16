# Pedalboard Planner

A full-stack web app for planning guitar pedalboard layouts: create boards, place pedals, and generate cable runs between them.

- **Backend:** Java 17, Spring Boot 3, JPA, JWT auth, rate limiting (Bucket4j)
- **Frontend:** React, TypeScript, Vite, Tailwind CSS
- **Data:** H2 (dev) or PostgreSQL (production)

---

## Prerequisites

- **Java** 17+ (JDK)
- **Node.js** 18+ and **npm**
- **Maven** 3.8+

---

## Scripts from repo root

From the project root you can run:

| Command | Description |
|--------|-------------|
| `npm run dev` | Start frontend dev server (Vite) |
| `npm run build` | Build frontend for production |
| `npm run preview` | Serve built frontend locally |
| `npm run backend:build` | Build frontend + backend into a single JAR |
| `npm run backend:run` | Run the built JAR |
| `npm run backend:test` | Run backend tests (Maven) |

All npm dependencies live in `frontend/`; run `npm install` inside `frontend/` when setting up. Backend is Maven-based (`backend/`).

---

## Quick start (development)

**Terminal 1 – backend:**

```bash
cd backend
mvn spring-boot:run
```

Backend runs at `http://localhost:8080` with an in-memory H2 database (data resets on restart).

**Terminal 2 – frontend:**

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at `http://localhost:5173` and proxies `/api` to the backend.

Open **http://localhost:5173** and sign up or log in to create boards and add pedals.

---

## Authentication

- **Register:** `POST /api/users` with `username`, `email`, `password` (min 8 chars).
- **Login:** `POST /api/users/login` with `username` and `password`. Returns a JWT and user object.
- **API calls:** Send `Authorization: Bearer <token>` for protected endpoints (boards, pedals, cables).

Login and registration are rate-limited (10 requests per minute per IP by default).

---

## Building for production

Build the frontend and package it with the backend into a single JAR:

```bash
cd backend
mvn clean package -Dskip.frontend=false
```

Run the JAR:

```bash
java -jar backend/target/pedalboard-backend-0.0.1-SNAPSHOT.jar
```

Then open `http://localhost:8080`. For production, set at least:

- `JWT_SECRET` – long random string (min 32 chars)
- `SPRING_DATASOURCE_*` or `DATABASE_URL` if using PostgreSQL

---

## Running tests

**Backend (from repo root):**

```bash
cd backend
mvn test -Dskip.frontend=true
```

**Frontend:** No test script in place; manual or add later.

---

## API overview

Base path: `/api`. Protected routes require `Authorization: Bearer <token>`.

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/users` | Register |
| POST | `/api/users/login` | Login (returns JWT + user) |
| GET | `/api/boards/user/{userId}` | List boards for user |
| POST | `/api/boards` | Create board (name, width, height, userId) |
| GET | `/api/boards/{id}` | Get board with pedals |
| DELETE | `/api/boards/{id}` | Delete board |
| POST | `/api/boards/{id}/pedals` | Add pedal |
| PUT | `/api/pedals/{id}` | Update pedal (e.g. position) |
| DELETE | `/api/pedals/{id}` | Remove pedal |
| POST | `/api/boards/{id}/generate-sequence` | Generate cable sequence between pedals |
| GET | `/api/boards/{id}/cables` | List cables |
| DELETE | `/api/boards/{id}/cables` | Remove all cables |

---

## Project structure

The app has a single frontend at the **repo root** (`frontend/`). The backend lives in `backend/` and, when building the production JAR, runs `npm install` and `npm run build` in `../frontend`, then bundles the result into the JAR. There is no duplicate frontend under `backend/`.

```text
pedalplanerare/
  package.json      # Root orchestrator (scripts only; no deps). Node app is in frontend/
  backend/          # Spring Boot app (build references ../frontend)
    src/
    pom.xml
  frontend/         # React app (used by both dev and backend JAR build)
    src/
    public/
    package.json    # All npm dependencies and frontend scripts
  render.yaml       # Optional: Render.com deployment
  docs/             # Deployment and development notes
  LICENSE           # MIT
```

---

## Deployment

- **Render:** A `render.yaml` blueprint is included. Configure a PostgreSQL database and set `JWT_SECRET` and database URL.
- **Other hosts:** Build the single JAR as above and run with Java; point `SPRING_DATASOURCE_*` or `DATABASE_URL` to your database.

---

## License

MIT. See [LICENSE](LICENSE).
