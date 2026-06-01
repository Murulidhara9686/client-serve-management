# ClientServe — Client Service Management System v2.0

A full-stack Spring Boot + Thymeleaf multi-role platform for managing client service requests end-to-end.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2, Spring Security 6 |
| Persistence | Spring Data JPA, Hibernate, MySQL 8 |
| Frontend | Thymeleaf 3, HTML5, CSS3, Vanilla JS, Chart.js |
| Build | Maven 3.8+ |

---

## Quick Start

1. Ensure **MySQL 8** runs on `localhost:3306` with user `root` / password `root`
2. `mvn spring-boot:run`
3. Open [http://localhost:8080](http://localhost:8080)

Adjust DB credentials in `src/main/resources/application.properties` if needed.

---

## Demo Accounts (auto-seeded)

| Role | Email | Password |
|---|---|---|
| Manager | manager@csm.com | password |
| Assistant Manager | assistant@csm.com | password |
| Employee | employee@csm.com | password |
| Customer | customer@csm.com | password |

> Click any row on the login page to auto-fill credentials instantly.

---

## Features by Role

### Customer
- Submit service requests with title, category, priority, budget range, and deadline
- Live search + status filter tabs (All / Pending / Active / Done / Rejected)
- View full request details in a modal with 4-step progress tracker
- Edit or cancel PENDING requests
- See manager notes and employee updates
- Comment on own requests (thread visible to team)
- Rate completed requests (1–5 stars) with feedback
- 🔔 Notification bell showing real-time updates
- Edit profile (name, phone)

### Manager
- See all 5 stat cards: Total / Pending / In Progress / Completed / Rejected
- Assign PENDING requests to an assistant manager with a custom note
- Reject requests with a reason (customer is notified)
- Add inline comments on any active request
- View customer rating and employee updates per row
- Search + filter tabs across all requests
- 👥 Team page listing all assistants, employees, and customers
- 📊 Stats dashboard link
- 🔔 Notification bell

### Assistant Manager
- 4 stat cards: Assigned / Needs Employee / In Progress / Completed
- Assign tasks to employees (with notification sent automatically)
- Reassign if needed
- "Unassigned" filter tab to quickly spot tasks without an employee
- Add comments on tasks
- 🔔 Notification bell

### Employee
- 3 stat cards + search + filter (All / In Progress / Completed)
- See manager note (amber box) and previous updates (blue box)
- Submit progress updates and change status
- Fill completion summary when marking complete
- See customer rating once the customer rates (gold box)
- Add comments visible to customer and assistant
- 🔔 Notification bell

### Stats (Manager + Assistant access)
- 6 hero cards: Total, Pending, In Progress, Completed, Rejected, Avg Rating
- Animated completion rate ring
- Progress bars for all 4 statuses
- Doughnut charts: Request Status Distribution + Team Composition
- Team member counts (Managers / Assistants / Employees / Customers)

---

## Architecture

```
com.csm/
├── config/
│   ├── DataInitializer.java        — Seeds 4 demo users on first run
│   ├── GlobalExceptionHandler.java — Catches all exceptions → friendly error page
│   └── SecurityConfig.java         — Spring Security: role-based URL access, login/logout
├── controller/
│   ├── AuthController.java         — GET /, /register, POST /register, /access-denied
│   ├── CustomerController.java     — /customer/** (submit, edit, cancel, rate, comment, profile)
│   ├── ManagerController.java      — /manager/** (assign, reject, comment, users page)
│   ├── AssistantController.java    — /assistant/** (assign, reassign, comment)
│   ├── EmployeeController.java     — /employee/** (update, comment)
│   └── StatsController.java        — /stats
├── model/
│   ├── User.java                   — id, name, email, password, phone, role
│   ├── ServiceRequest.java         — full request entity with all fields
│   ├── Comment.java                — per-request comment thread
│   ├── Notification.java           — per-user notification with type + read flag
│   ├── Role.java                   — CUSTOMER, MANAGER, ASSISTANT_MANAGER, EMPLOYEE
│   ├── TaskStatus.java             — PENDING, IN_PROGRESS, COMPLETED, REJECTED
│   └── Priority.java               — LOW, MEDIUM, HIGH, URGENT
├── repository/                     — Spring Data JPA interfaces
└── service/
    ├── UserService.java
    ├── ServiceRequestService.java
    ├── NotificationService.java
    └── CommentService.java
```

---

## Bug Fixes (all carried from v1.x)

1. **HTTP 500 on Assistant login** — LazyInitializationException fixed with EAGER fetch + @Transactional
2. **Requests not showing after submit** — Thymeleaf `th:attr onclick` escaping issue fixed with `data-*` attributes
3. **Delete via GET** — changed to POST form
4. **Registered message not shown** — controller now reads param and sets model attribute
5. **No global error handler** — added @ControllerAdvice

---

*Built with Spring Boot 3 · Thymeleaf · MySQL · Chart.js*
