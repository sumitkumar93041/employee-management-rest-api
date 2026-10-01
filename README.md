# Employee Management REST API

A RESTful backend for managing employee records, built with **Spring Boot**, **Spring Data JPA (Hibernate)** and **MySQL**. It supports full CRUD operations, filtering by state, and returns meaningful HTTP status codes for every case (created, not found, conflict, and so on).

## Tech Stack

- Java 17
- Spring Boot 4 (Spring Web, Spring Data JPA)
- Hibernate ORM
- MySQL 8
- Maven
- Tested with Postman

## Features

- CRUD endpoints under `/employees`
- Optional query-parameter filtering: `/employees?state=Bihar`
- Layered architecture: **Controller → Service → Repository**
- Proper HTTP status codes: `200`, `201`, `204`, `404`, `409`
- Duplicate-id protection on create (`409 Conflict`)
- Missing-id handling on get, update and delete (`404 Not Found`)
- Database credentials read from environment variables (nothing sensitive in source code)

## API Endpoints

| Method | Endpoint                 | Description                          | Success          | Errors                   |
|--------|--------------------------|--------------------------------------|------------------|--------------------------|
| GET    | `/employees`             | Get all employees                    | `200 OK`         |                          |
| GET    | `/employees?state=Bihar` | Get employees filtered by state      | `200 OK`         |                          |
| GET    | `/employees/{id}`        | Get one employee by id               | `200 OK`         | `404 Not Found`          |
| POST   | `/employees`             | Add a new employee                   | `201 Created`    | `409 Conflict` (id exists) |
| PUT    | `/employees/{id}`        | Update an existing employee          | `200 OK`         | `404 Not Found`          |
| DELETE | `/employees/{id}`        | Delete an employee                   | `204 No Content` | `404 Not Found`          |

## Sample Request

`POST /employees`

```json
{
  "id": 175,
  "first_name": "Vaibhav",
  "last_name": "Gupta",
  "age": 23,
  "salary": 50000.00,
  "city": "Lucknow",
  "state": "Uttar Pradesh"
}
```

The `id` is supplied by the client on purpose. Posting an id that already exists returns `409 Conflict` instead of overwriting the record.

## Project Structure

```
src/main/java/com/example
├── Employee.java                 # JPA entity
├── EmployeeRepo.java             # Spring Data repository (includes findByState)
├── EmployeeService.java          # Business logic and error handling
├── EmployeeController.java       # REST endpoints
└── Resttry1Application.java      # Application entry point
```

## Getting Started

### Prerequisites

- JDK 17
- MySQL 8 running on `localhost:3306`
- Maven (or use the included Maven wrapper)

### 1. Clone the repository

```bash
git clone https://github.com/sumitkumar93041/employee-management-rest-api.git
cd employee-management-rest-api
```

### 2. Create the database

```sql
CREATE DATABASE resttry2;
```

The `employee` table is created automatically on first run (`spring.jpa.hibernate.ddl-auto=update`).

### 3. Set the environment variables

The app reads the MySQL username and password from environment variables:

| Variable            | Value                  |
|---------------------|------------------------|
| `DB_USER_WORKBENCH` | your MySQL username    |
| `DB_PASS_WORKBENCH` | your MySQL password    |

**Windows (PowerShell):**

```powershell
setx DB_USER_WORKBENCH "root"
setx DB_PASS_WORKBENCH "your_password"
```

Close and reopen your terminal or IDE afterwards so it picks up the new values.

**Linux / macOS:**

```bash
export DB_USER_WORKBENCH=root
export DB_PASS_WORKBENCH=your_password
```

### 4. Run the application

```bash
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd spring-boot:run`. The API starts at `http://localhost:8080`.

### 5. Try it

```bash
curl http://localhost:8080/employees
curl "http://localhost:8080/employees?state=Bihar"
```

## Configuration

`src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/resttry2
spring.datasource.username=${DB_USER_WORKBENCH}
spring.datasource.password=${DB_PASS_WORKBENCH}
spring.jpa.hibernate.ddl-auto=update
```

If your MySQL runs on a different port or you want another schema name, change the URL here.

## Testing

All endpoints were tested manually in Postman, including the error cases:

- `GET /employees/{id}` with a missing id returns `404`
- `POST /employees` with an existing id returns `409`
- `PUT /employees/{id}` with a missing id returns `404`
- `DELETE /employees/{id}` with a missing id returns `404`

<!--
Add screenshots after creating a /screenshots folder in the repo:

![GET by state](screenshots/get-by-state.png)
![409 Conflict](screenshots/post-conflict.png)
-->

## Possible Improvements

- Request validation (`@Valid`) and a global exception handler
- DTOs instead of exposing the entity directly
- Pagination and sorting on `GET /employees`
- Unit and integration tests
- Swagger / OpenAPI documentation

## Author

**Sumit Kumar**
GitHub: [sumitkumar93041](https://github.com/sumitkumar93041)
