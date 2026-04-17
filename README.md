<h1>Salon Service Management API</h1>

This is a robust backend REST API developed for Studio Gui, a professional salon management system. It provides a complete solution for scheduling services, managing professionals, and handling user authentication.
<h2>Technologies</h2>

This project utilizes modern software engineering practices and the following technology stack:

    Java 17: Core language.

    Spring Boot 4.0.1: Framework for building the RESTful API.

    Spring Security: Implementation of authentication and authorization with JWT.

    Spring Data JPA: Abstraction for data persistence.

    PostgreSQL: Relational database.

    Flyway: Database versioning and migrations.

    Lombok: Reducing boilerplate code for models and DTOs.

    JUnit 5 & Mockito: Unit testing and service layer validation.

    Docker: Containerization using docker-compose for the database environment.

    Maven Wrapper: Ensuring consistent builds across different environments.

🛠️ Key Features

    Secure Authentication: JWT-based login and user registration.

    Role-Based Access Control (RBAC): Specific permissions for USER, PROFESSIONAL, and ADMIN.

    Smart Scheduling:

        Validation of business hours (Tuesday to Saturday, 08:00 - 18:00).

        Automated conflict detection to prevent double-booking for both professionals and clients.

        Minimum lead time of 30 minutes for new appointments.

    Service Management: Soft delete (inactivation) of services to preserve historical data integrity.

    Optimized Performance: Database indexes created specifically for frequent calendar and history queries.

⚙️ How to Run
Prerequisites

    Java 17 installed.

    Docker and Docker Compose (recommended for the database).

1. Database Setup

The easiest way to start the database is using the provided Docker configuration. In the project root, run:
Bash

docker-compose -f Docker/docker-compose.yml up -d

This will start a PostgreSQL instance on port 5432 with the database securitydb.

### 2. Configuration

Following security best practices, the database connection is configured using environment variables to prevent sensitive data exposure.

1. Create a `.env` file in the root directory of the project.
2. Add the following environment variables with your local database credentials:

```env
DB_URL=jdbc:postgresql://localhost:5432/securitydb
DB_USERNAME=postgres
DB_PASSWORD=manager
```

#### 2.1 The database connection is pre-configured in src/main/resources/application.properties:

    Username: ${DB_USERNAME}
    Password: ${DB_PASSWORD}
    URL: ${DB_URL}

3. Running the Application

Use the Maven Wrapper to build and run the project:
Bash

# On Linux/macOS
./mvnw spring-boot:run

# On Windows
mvnw.cmd spring-boot:run

The API will be available at http://localhost:8080.
🧪 Testing

The project includes a suite of unit tests for core services. To run them:
Bash

./mvnw test
<div>
  <h3>API Endpoints (Quick Reference)</h3>
  Method	Endpoint	Access	Description:</br> </br>
  
  POST	/api/auth/register	Public	Register a new user</br>
  
  POST	/api/auth/login	Public	Authenticate and receive JWT</br>
  
  GET	/api/services	Authenticated	List all active services</br>
  
  POST	/api/services	Admin	Create a new salon service</br>
  
  POST	/api/appointments	Authenticated	Schedule a service</br>
  
  GET	/api/appointments/today	Authenticated	View daily agenda</br>
  
  PATCH	/api/users/{id}/role	Admin	Change user permissions</br>
</div>
