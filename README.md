# Financial Dashboard Backend API

Welcome to the backend API for the Financial Dashboard application. This project is a Spring Boot application designed to manage users, process financial records, and aggregate dashboard analytics.

It goes beyond basic CRUD operations by implementing advanced architectural patterns like Soft Deletion, granular Role-Based Access Control (RBAC), dynamic database filtering, and API Rate Limiting.

---

##  Key Features

* **Advanced RBAC:** Strict separation of concerns using Spring Security.
  * `VIEWER`: Can only view dashboard summaries.
  * `ANALYST`: Can view dashboard summaries, access deep-dive trends, and read records.
  * `ADMIN`: Full CRUD access over users and financial records.
* **Stateless Authentication:** Secured via JSON Web Tokens (JWT).
* **Enterprise Data Integrity:** Hibernate-level Soft Deletion (`@SQLDelete`) ensures financial data is never permanently destroyed, preserving audit trails.
* **Dynamic Search & Filtering:** Utilizes Spring Data JPA `Specification` to build dynamic queries for filtering records by date range, type, category, and text search.
* **Rate Limiting:** Integrated `Bucket4j` token-bucket algorithm to protect endpoints from brute-force and DDoS attacks (Max 50 requests/minute per IP).
* **Automated API Documentation:** Integrated Swagger/OpenAPI UI for easy endpoint testing.

---

## 🛠️ Setup & Installation

### Prerequisites
* **Java 21** or higher
* **Maven** 4.0+

### 1. Clone & Configure
Clone the repository to your local machine.

In the `src/main/resources` folder, create a file named `application.properties` and paste the following template.

**Note on Database:** This project uses a file-based H2 database (`jdbc:h2:file:./finance-db`). This provides the lightweight simplicity of H2 without losing our data every time the server restarts!

```properties
spring.application.name=dashboard
spring.datasource.url=jdbc:h2:file:./finance-db;DB_CLOSE_ON_EXIT=FALSE;AUTO_RECONNECT=TRUE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=temp@12345

spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.show-sql=true

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=36000000

# Default Admin Credentials seeded on startup
app.admin.name=System Admin
app.admin.email=admin@finance.com
app.admin.password=secureAdminPass123
```

### 2. Run the Application
You can run the application directly using Maven:
```bash
mvn spring-boot:run
```
Upon startup, the `DatabaseSeeder` will automatically detect if the database is empty and inject an Admin, Analyst, and Viewer, along with several months of dummy financial records to populate the dashboard graphs.

---

## 📖 API Documentation & Testing

Once the server is running on `http://localhost:8080`, you do not need Postman to test the API.

Navigate to the automated Swagger UI:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

**To use the UI:**
1. Scroll down to `POST /auth/login`.
2. Enter the Admin credentials (`admin@finance.com` / `password123`) and execute.
3. Copy the returned JWT token.
4. Scroll to the top, click the green **Authorize** button, and paste your token. You can now execute any endpoint directly from the browser!


---

## 🧠 Architecture & Tradeoffs Considered

1. **File-Based H2 vs. In-Memory vs. PostgreSQL:** I chose a file-based H2 database for this assignment. An in-memory database wipes data on every restart, which slows down testing. A full PostgreSQL setup requires Docker or local installation, which makes reviewing the code harder for evaluators. File-based H2 offers the best of both worlds: zero-setup for the reviewer, but true data persistence across sessions.
2. **Soft Deletion (`@SQLDelete`):** In financial systems, hard-deleting records is a compliance risk. Instead of writing manual `WHERE is_deleted = false` checks in every service, I overrode the Hibernate entity annotations. Deletes are automatically intercepted and converted to updates, and read queries automatically filter them out.
3. **Database-Level Aggregations:** Instead of pulling thousands of records into the Java JVM to calculate the Monthly Trends, I utilized raw SQL/JPQL `GROUP BY YEAR(), MONTH()` queries. This pushes the heavy mathematical lifting to the database, drastically reducing memory consumption and API response times.
4. **Rate Limiting (Bucket4j):** Implemented an Interceptor to protect the API from spam. I opted for a strict Interval refill strategy (50 requests per minute) rather than a greedy drip to ensure complete lockouts during simulated load attacks.