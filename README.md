# Gym Membership and Attendance Tracker (IronPulse)

A complete full-stack web application designed for gym management, member registrations, flexible membership plans, renewal tracking, live entrance check-in attendance verification, multi-page UI, and secure role-based authentication.

Built with **Java 17**, **Spring Boot 3.3.5**, **Spring Security**, **Spring Data JPA**, **MySQL**, **H2 (Test)**, and a clean, high-contrast white-theme multi-page frontend served via vanilla HTML, CSS, and JavaScript (Fetch API).

---

## 🔐 Demo Login Credentials

The application seeds two accounts on initial startup:

| Username | Password | Role | Description |
|---|---|---|---|
| `admin` | `admin123` | **ADMIN** | Full administrative access, user management, plan editing, and deletion privileges |
| `staff` | `staff123` | **STAFF** | Front-desk operations: member registrations, memberships, attendance, quick check-in |

---

## 🚀 Features

- **Multi-Page Web UI with Clean URLs**:
  - `/login`: Dedicated secure authentication page.
  - `/dashboard`: Real-time gym KPIs and recent check-ins stream.
  - `/members`, `/members/new`, `/members/{id}`, `/members/{id}/edit`: Member registry, profile, and history.
  - `/plans`, `/plans/{id}`: Membership plans catalog and editor.
  - `/memberships`, `/memberships/new`, `/memberships/{id}`: Subscriptions and instant renewals.
  - `/checkin`: Quick live attendance check-in terminal with instant feedback.
  - `/attendance`, `/attendance/member/{id}`: Daily log and individual attendance history.
  - `/users`: Role-based user administration (ADMIN only).
- **Session-based Authentication & Role Security**:
  - Secure passwords hashed with BCrypt.
  - Role-based authorization (`ADMIN` and `STAFF`).
  - Unauthenticated page access automatically redirects to `/login`.
  - Unauthenticated API access returns clean `401 Unauthorized` JSON.
  - Destructive actions (`DELETE`) and `/api/users` restricted strictly to `ADMIN`.
- **Smart Renewal & Expiry Calculations**:
  - Active check: `today <= expiryDate`.
  - Active renewal: extends from existing expiry date (`expiryDate + duration`).
  - Expired renewal: extends from today (`today + duration`).
- **Clean White Theme & High Contrast**:
  - Clean white `#FFFFFF` aesthetic accented with IronPulse Blue (`#1E4FD8`) and Red (`#D62828`).


---

## 🛠️ Prerequisites

- **Java Development Kit (JDK)**: Version 17 or higher
- **MySQL Database Server**: Version 8.0+ (running locally on port 3306)
- **Maven**: Bundled Maven Wrapper (`mvnw` / `mvnw.cmd`) is included in the project repository.

---

## ⚙️ Database Configuration

By default, the application is pre-configured to connect to MySQL on `localhost:3306` with credentials:
- **Database**: `gym_db` (created automatically if not present via `createDatabaseIfNotExist=true`)
- **Username**: `root`
- **Password**: `root`

To change the database password or host, edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gym_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
# Change to your own password:
spring.datasource.password=your_mysql_password
```

---

## 🧪 Running Automated Tests

The automated test suite runs on an in-memory **H2 database** with the `test` profile. It executes unit and full HTTP integration tests without requiring an active MySQL connection.

On Windows (Command Prompt / PowerShell):
```powershell
.\mvnw.cmd clean test
```

On Linux / macOS:
```bash
./mvnw clean test
```

### Automated Tests Covered:
- Register a valid member -> `201 Created`
- Register a member with an empty name -> `400 Bad Request`
- Duplicate phone registration -> `409 Conflict`
- Create a membership -> `expiryDate` matches `startDate + duration`
- Check-in with active membership -> `201 Created` with saved timestamp
- Check-in with expired membership -> `400 Bad Request` (`Membership expired. Please renew.`)
- Check-in with invalid member ID -> `404 Not Found`
- Renew an expired membership -> new expiry date = `today + plan duration`
- Renew an active membership -> expiry extended from existing `expiryDate`
- Search member by name and by phone query

---

## ▶️ Running the Application

### Option A: Standard Run with MySQL
```powershell
.\mvnw.cmd spring-boot:run
```

### Option B: In-Memory H2 Fallback (No MySQL required)
If MySQL is not installed or the service is stopped:
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=test
```

Once started, open your browser and navigate to:
👉 **http://localhost:8080**

---

## 📡 REST API Reference

### 1. Members Endpoints (`/api/members`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/members` | Register a new member |
| `GET` | `/api/members` | Get all members (supports `?search=query`) |
| `GET` | `/api/members/{id}` | Get member by ID |
| `PUT` | `/api/members/{id}` | Update member profile |
| `DELETE` | `/api/members/{id}` | Delete member and related data |

### 2. Plans Endpoints (`/api/plans`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/plans` | Create a new membership plan |
| `GET` | `/api/plans` | Get all plans (seeded with Monthly, Quarterly, Yearly) |
| `GET` | `/api/plans/{id}` | Get plan by ID |
| `PUT` | `/api/plans/{id}` | Update existing plan |

### 3. Memberships Endpoints (`/api/memberships`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/memberships` | Assign plan to a member |
| `GET` | `/api/memberships` | Get all memberships |
| `GET` | `/api/memberships/{id}` | Get membership by ID |
| `PUT` | `/api/memberships/{id}/renew` | Renew membership (optional `planId`) |
| `GET` | `/api/memberships/member/{memberId}` | Get all memberships for a specific member |

### 4. Check-in & Attendance Endpoints (`/api/checkins`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/checkins` | Check-in with `memberId` OR `phone` |
| `GET` | `/api/checkins` | Get check-in logs (supports `?date=YYYY-MM-DD`) |
| `GET` | `/api/checkins/member/{memberId}` | Get attendance history for a member |
| `GET` | `/api/checkins/member/{memberId}/month` | Get current month attendance for a member |
| `GET` | `/api/checkins/today-count` | Get count of check-ins today |

### 5. Dashboard Metrics (`/api/dashboard`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/api/dashboard/stats` | Total members, active memberships, today checkins |

---

## 📋 Sample JSON Request Bodies

### 1. Register Member (`POST /api/members`)
```json
{
  "name": "Sarah Connor",
  "phone": "9876543210",
  "email": "sarah@resistance.org",
  "joinDate": "2026-09-28"
}
```

### 2. Assign Membership (`POST /api/memberships`)
```json
{
  "memberId": 1,
  "planId": 1,
  "startDate": "2026-09-28"
}
```

### 3. Renew Membership (`PUT /api/memberships/1/renew`)
```json
{
  "planId": 2
}
```
*(Leave body empty `{}` or omit `planId` to renew with current plan)*

### 4. Check-In by Member ID (`POST /api/checkins`)
```json
{
  "memberId": 1
}
```

### 5. Check-In by Phone (`POST /api/checkins`)
```json
{
  "phone": "9876543210"
}
```

---

## 📂 Project Architecture

```
gym-tracker/
├── pom.xml
├── mvnw, mvnw.cmd, .mvn/
├── README.md
├── src/main/java/com/gym/tracker/
│   ├── GymTrackerApplication.java
│   ├── config/       DataLoader (seeds default plans), CorsConfig
│   ├── controller/   MemberController, PlanController, MembershipController, CheckinController, DashboardController
│   ├── dto/          MemberRequest, PlanRequest, MembershipRequest, RenewMembershipRequest, CheckinRequest, ErrorResponse, MembershipResponse, CheckinResponse
│   ├── entity/       Member, Plan, Membership, Checkin
│   ├── exception/    ResourceNotFoundException, MembershipExpiredException, DuplicateResourceException, GlobalExceptionHandler
│   ├── repository/   MemberRepository, PlanRepository, MembershipRepository, CheckinRepository
│   └── service/      MemberService, PlanService, MembershipService, CheckinService
├── src/main/resources/
│   ├── application.properties
│   ├── application-test.properties (H2 in-memory)
│   ├── schema.sql (table reference)
│   └── static/
│       ├── index.html
│       ├── css/style.css
│       └── js/app.js
└── src/test/java/com/gym/tracker/
    ├── service/CheckinServiceTest.java
    ├── service/MembershipServiceTest.java
    └── controller/ApiIntegrationTest.java
```
