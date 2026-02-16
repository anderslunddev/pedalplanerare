# Render Deployment Guide

This guide covers deploying the Pedalboard Planner application to Render.

## Prerequisites

1. **Render Account** - Sign up at https://render.com
2. **GitHub Repository** - Push your code to GitHub (Render deploys from Git)
3. **PostgreSQL Database** - Render provides managed PostgreSQL

---

## ✅ Changes Made for Render Deployment

### 1. Added PostgreSQL Dependency
- ✅ Added `postgresql` driver to `pom.xml`

### 2. Updated Configuration for Environment Variables
- ✅ Updated `application.yml` to use environment variables:
  - `DATABASE_URL` - PostgreSQL connection (parsed automatically)
  - `PORT` - Server port (Render sets this)
  - `JWT_SECRET` - JWT secret key (required in production)
  - `H2_CONSOLE_ENABLED` - Disabled in production
  - `SHOW_SQL` - Disabled in production

### 3. Created Database Configuration
- ✅ Created `DatabaseConfig.java` to parse `DATABASE_URL` from Render
- ✅ Automatically detects PostgreSQL vs H2 based on environment

### 4. Added JWT Secret Validation
- ✅ Added validation to ensure JWT secret is at least 32 characters

### 5. Created Render Configuration
- ✅ Created `render.yaml` with:
  - PostgreSQL database service
  - Web service configuration
  - Build and start commands
  - Environment variables

---

## Step 4: Set Environment Variables in Render Dashboard

After creating the service, configure these environment variables:
- `DATABASE_URL` - Automatically provided by Render when you link a PostgreSQL database
- `JWT_SECRET` - Generate a secure random string (at least 32 characters)
- `PORT` - Automatically set by Render (don't override)

---

## Step 5: Update CORS Configuration

Update CORS settings to allow your Render frontend URL.

---

## Deployment Steps

### Option A: Using render.yaml (Recommended)

1. Push code to GitHub
2. In Render Dashboard:
   - New → Blueprint
   - Connect your GitHub repository
   - Render will read `render.yaml` and create services automatically

### Option B: Manual Setup

1. **Create PostgreSQL Database:**
   - New → PostgreSQL
   - Name: `pedalboard-db`
   - Note the connection details

2. **Create Web Service:**
   - New → Web Service
   - Connect GitHub repository
   - Root Directory: `backend`
   - Build Command: `mvn clean package -DskipTests`
   - Start Command: `java -jar target/pedalboard-backend-0.0.1-SNAPSHOT.jar`
   - Environment Variables:
     - `DATABASE_URL` (from PostgreSQL service)
     - `JWT_SECRET` (generate secure random string)
     - `SPRING_PROFILES_ACTIVE=production`

---

## Environment Variables Reference

| Variable | Description | Example |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection string | `postgresql://user:pass@host:5432/dbname` |
| `JWT_SECRET` | Secret key for JWT tokens | `your-secure-random-string-min-32-chars` |
| `PORT` | Server port (auto-set by Render) | `10000` |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `production` |

---

## Post-Deployment Checklist

- [ ] Verify database connection works
- [ ] Test user registration
- [ ] Test login and JWT token generation
- [ ] Test board creation
- [ ] Verify CORS allows frontend domain
- [ ] Check logs for any errors
- [ ] Verify static files (frontend) are served correctly

---

## Troubleshooting

### Build Fails
- Ensure Node.js is available during build (Render provides it)
- Check Maven logs for dependency issues

### Database Connection Fails
- Verify `DATABASE_URL` is correctly set
- Check PostgreSQL service is running
- Ensure database credentials are correct

### Port Issues
- Render sets `PORT` automatically - don't hardcode port 8080
- Use `${PORT:8080}` in application.yml for local fallback

### Frontend Not Loading
- Verify frontend build succeeded (`frontend/dist` exists)
- Check static resource mapping in Spring Boot
- Verify no CORS issues in browser console

---

## Security Notes

- ✅ Never commit `JWT_SECRET` to Git
- ✅ Use Render's environment variables for secrets
- ✅ Disable H2 console in production
- ✅ Use HTTPS (Render provides this automatically)
- ✅ Keep database credentials secure
