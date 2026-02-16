# Pedalboard Planner – Full-Stack Project

This project is a full-stack **Spring Boot + React** application for planning pedalboard layouts.

- **Backend**: Java 17, Spring Boot 3, JPA, H2 in-memory DB  
- **Frontend**: React + TypeScript, Vite, Tailwind CSS  
- **Structure**: Backend and frontend live side by side in this repo. The backend Maven build can still bundle the frontend into the same JAR for production.

---

## Prerequisites

- **Java** 17+ (JDK)
- **Node.js** 18+ and **npm**
- **Maven** 3.8+ (optional if using IntelliJ’s built-in Maven support)

Paths used below assume this repo root:

```text
pedalplanerare/
  backend/
    pom.xml
    src/...
  frontend/
    package.json
    src/...
```

---

## 1. Running in Development

In development, you typically run **backend** and **frontend dev server** separately:

### 1.1 Start the backend (Spring Boot + H2)

From the `backend` directory:

```bash
cd backend
mvn spring-boot:run
```

This will:
- Start Spring Boot on `http://localhost:8080`
- Use an **in-memory H2** database (data resets on restart)

### 1.2 Start the frontend (Vite dev server)

In another terminal, from `frontend`:

```bash
cd frontend
npm install        # first time only
npm run dev
```

Vite will start on `http://localhost:5173`.

The dev server is configured (via `vite.config.mts`) to **proxy `/api` calls to `http://localhost:8080`**, so the frontend can call the Spring Boot API without CORS issues.

Open:
- Frontend: `http://localhost:5173`
- Backend API (for testing): `http://localhost:8080/api/...`

---

## 2. Building a Single JAR (Backend + Frontend)

The Maven build is configured to:

1. Run `npm install` and `npm run build` inside `frontend`
2. Copy the built static files (`frontend/dist`) into the Spring Boot jar under `static/`

From `backend`:

```bash
cd backend
mvn clean package
```

This produces a single runnable JAR in `backend/target`, e.g.:

```text
backend/target/pedalboard-backend-0.0.1-SNAPSHOT.jar
```

Run it:

```bash
java -jar target/pedalboard-backend-0.0.1-SNAPSHOT.jar
```

Then open:

- `http://localhost:8080` – serves the built React app
- `http://localhost:8080/api/...` – REST API endpoints

---

## 3. Key Endpoints (Backend)

All endpoints are prefixed with `/api`.

- **Boards**
  - `POST /api/boards`  
    Create a board. Body:
    ```json
    {
      "name": "My Board",
      "width": 60.0,
      "height": 30.0
    }
    ```
  - `GET /api/boards/{id}`  
    Get a board by UUID.

- **Pedals**
  - `POST /api/boards/{boardId}/pedals`  
    Add a pedal to a board. Body:
    ```json
    {
      "name": "Overdrive",
      "width": 10.0,
      "height": 12.0,
      "color": "#4f46e5",
      "x": 5.0,
      "y": 5.0
    }
    ```
  - `PUT /api/pedals/{id}`  
    Update a pedal’s position. Body:
    ```json
    {
      "x": 15.0,
      "y": 8.0
    }
    ```

---

## 4. Frontend Behavior (Overview)

- **Create Board**:  
  - Inputs for board width/height (in units, e.g. cm).  
  - Calls `POST /api/boards`, displays the board as a **gray rectangle**.

- **Add Pedal**:  
  - “Add Pedal” button opens a form for name, width, height, and color.  
  - On save, calls `POST /api/boards/{boardId}/pedals` and renders the pedal on the board.

- **Scaling & Coordinates**:
  - A constant scale is used: **1 unit = 5px**.  
  - Board and pedal positions/sizes are all calculated from the units stored in the database.  
  - Pedal `x`/`y` are relative to the **top-left** of the board (0,0).

- **Drag & Drop**:
  - Pedals are draggable within the board area.  
  - On drag end, the app calls `PUT /api/pedals/{id}` to persist the new `x`/`y` in the database.

---

## 5. Notes / Troubleshooting

- **H2 in-memory DB**:  
  - Data is lost when you stop the backend.  
  - For a persistent DB, you can switch the datasource in `backend/src/main/resources/application.yml` to PostgreSQL or another database.

- **Build errors related to npm**:  
  - Ensure Node.js and npm are installed and available on your PATH.  
  - You can also manually run:
    ```bash
    cd backend/frontend
    npm install
    npm run build
    ```
    before running `mvn package`.

