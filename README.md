# Employee Management REST API

A secured RESTful backend for managing employee records, built with **Spring Boot**, **Spring Security (JWT)**, **Spring Data JPA (Hibernate)** and **MySQL**. It supports full CRUD operations, filtering by state, user registration and login, and returns meaningful HTTP status codes for every case (created, not found, conflict, unauthorized, and so on). The code is covered by **64 automated tests** (JUnit 5, Mockito, MockMvc and H2).

## Tech Stack

- Java 17
- Spring Boot 4 (Spring Web, Spring Data JPA, Spring Security)
- JWT (jjwt 0.12.6) for stateless authentication
- BCrypt for password hashing
- Hibernate ORM
- MySQL 8
- Maven
- JUnit 5, Mockito, MockMvc and H2 (in-memory, tests only)
- Tested manually with Postman

- ## Live demo

- **App:** https://employee-management-ui-delta.vercel.app
- **API:** https://employee-management-api-zp0h.onrender.com
- Create an account on the Register page to try it.

> Free-tier hosting: the backend sleeps when idle, so the first request can take about a minute, and the free database may need to be powered on after a long pause.

## Deployment

| Part | Host |
| --- | --- |
| React frontend | Vercel |
| Spring Boot API (Docker, multi-stage build) | Render |
| MySQL | Aiven |

Configuration comes from environment variables (`DB_URL`, `DB_USER_WORKBENCH`, `DB_PASS_WORKBENCH`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`, `CORS_ORIGIN`). No secrets are stored in the repository.

## Features

- CRUD endpoints under `/employees`
- Optional query-parameter filtering: `/employees?state=Bihar`
- User registration (`POST /register`) and login (`POST /login`) backed by a MySQL `users` table
- **JWT bearer-token authentication**: every `/employees` call needs a valid token
- Passwords stored only as **BCrypt hashes**; the password is never returned in any response
- `USER` and `ADMIN` roles stored in the database; an admin account is created at startup from environment variables
- An expired, tampered or malformed token returns `401`, not a server error
- Layered architecture: **Controller → Service → Repository**
- Proper HTTP status codes: `200`, `201`, `204`, `401`, `404`, `409`
- Duplicate-id protection on create and duplicate-username protection on register (`409 Conflict`)
- Missing-id handling on get, update and delete (`404 Not Found`)
- Database and admin credentials read from environment variables (nothing sensitive in source code)
- 64 automated tests across the service, controller, repository and full login-flow layers

## API Endpoints

### Authentication (public)

| Method | Endpoint    | Description                 | Success                                              | Errors                                |
|--------|-------------|-----------------------------|------------------------------------------------------|---------------------------------------|
| POST   | `/register` | Create a normal user (`USER`) | `200 OK` with the user JSON (id, username, role)   | `409 Conflict` (username exists)      |
| POST   | `/login`    | Log in and get a JWT        | `200 OK` with the token as plain text                | `401 Unauthorized` (wrong credentials) |

### Employees (token required)

Send the token from `/login` in the header `Authorization: Bearer <token>`.

| Method | Endpoint                 | Description                     | Success          | Errors                                  |
|--------|--------------------------|---------------------------------|------------------|-----------------------------------------|
| GET    | `/employees`             | Get all employees               | `200 OK`         | `401` (no or invalid token)             |
| GET    | `/employees?state=Bihar` | Get employees filtered by state | `200 OK`         | `401`                                   |
| GET    | `/employees/{id}`        | Get one employee by id          | `200 OK`         | `401`, `404 Not Found`                  |
| POST   | `/employees`             | Add a new employee              | `201 Created`    | `401`, `409 Conflict` (id exists)       |
| PUT    | `/employees/{id}`        | Update an existing employee     | `200 OK`         | `401`, `404 Not Found`                  |
| DELETE | `/employees/{id}`        | Delete an employee              | `204 No Content` | `401`, `404 Not Found`                  |

## How Authentication Works

1. **Register:** `POST /register` hashes the password with BCrypt, forces the role to `USER` (a role sent by the client is ignored) and saves the user in MySQL.
2. **Login:** `POST /login` passes the username and password to Spring Security's `AuthenticationManager`. It loads the user from MySQL, compares the password with the stored BCrypt hash and, on success, returns a signed JWT (subject = username, valid for 30 minutes).
3. **Protected calls:** `JwtFilter` reads `Authorization: Bearer <token>` on every request, verifies the signature and expiry, loads the user and puts the authentication into the security context. Without a valid token the API answers `401`.
4. **Stateless:** the server keeps no session. CSRF protection is disabled because the API uses a header token rather than cookies.

## Sample Requests

`POST /register`

```
{
  "username": "testuser",
  "password": "test123"
}
```

Response (`200 OK`):

```
{
  "id": 2,
  "username": "testuser",
  "role": "USER"
}
```

`POST /login` (same body) returns a long text starting with `eyJ...`. That is the JWT.

`POST /employees` with header `Authorization: Bearer <token>`:

```
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

The employee `id` is supplied by the client on purpose. Posting an id that already exists returns `409 Conflict` instead of overwriting the record.

## Project Structure

```
src/main/java/com/example
├── Resttry1Application.java        # Application entry point
├── controller
│   ├── EmployeeController.java     # /employees endpoints
│   └── UserController.java         # /register and /login
├── service
│   ├── EmployeeService.java        # Business logic and error handling
│   ├── UserService.java            # Register, login, admin creation
│   ├── JWTService.java             # Creates and validates JWTs
│   └── MyUserDetailsService.java   # Loads a user from MySQL for Spring Security
├── repo
│   ├── EmployeeRepo.java           # Spring Data repository (includes findByState)
│   └── UserRepo.java               # findByUsername
├── model
│   ├── Employee.java              # JPA entity
│   ├── Users.java                 # JPA entity for the users table
│   └── UserPrincipal.java         # UserDetails wrapper
└── config
    ├── SecurityConfig.java         # Security filter chain, BCrypt, AuthenticationManager
    ├── JwtFilter.java              # Reads the Bearer token on every request
    └── AdminSeeder.java            # Creates the admin user at startup

src/test/java/com/example
├── controller   # EmployeeControllerTest, UserControllerTest
├── service      # EmployeeServiceTest, UserServiceTest, JWTServiceTest, MyUserDetailsServiceTest
├── repo         # EmployeeRepoTest, UserRepoTest
└── security     # AuthFlowIntegrationTest
```

## Getting Started

### Prerequisites

- JDK 17
- MySQL 8 running on `localhost:3306`
- Maven (or use the included Maven wrapper)

### 1. Clone the repository

```
git clone https://github.com/sumitkumar93041/employee-management-rest-api.git
cd employee-management-rest-api
```

### 2. Create the database

```
CREATE DATABASE resttry2;
```

The `employee` and `users` tables are created automatically on first run (`spring.jpa.hibernate.ddl-auto=update`).

### 3. Set the environment variables

| Variable            | Value                                                        |
|---------------------|--------------------------------------------------------------|
| `DB_USER_WORKBENCH` | your MySQL username                                          |
| `DB_PASS_WORKBENCH` | your MySQL password                                          |
| `ADMIN_USERNAME`    | username of the admin account (default `admin` if not set)   |
| `ADMIN_PASSWORD`    | password of the admin account (no admin is created if unset) |

**Windows (PowerShell):**

```
setx DB_USER_WORKBENCH "root"
setx DB_PASS_WORKBENCH "your_password"
setx ADMIN_USERNAME "admin"
setx ADMIN_PASSWORD "your_admin_password"
```

Close and reopen your terminal or IDE afterwards so it picks up the new values.

**Linux / macOS:**

```
export DB_USER_WORKBENCH=root
export DB_PASS_WORKBENCH=your_password
export ADMIN_USERNAME=admin
export ADMIN_PASSWORD=your_admin_password
```

At startup, `AdminSeeder` creates the admin row (role `ADMIN`, BCrypt-hashed password) only if that username does not exist yet. Changing `ADMIN_PASSWORD` later does not change an existing admin's stored password.

**Alternative without the admin variables:** run the app, register a user with `POST /register`, then promote it in MySQL:

```
UPDATE users SET role = 'ADMIN' WHERE username = 'admin';
```

### 4. Run the application

```
./mvnw spring-boot:run
```

On Windows, use `mvnw.cmd spring-boot:run`. The API starts at `http://localhost:8080`.

### 5. Try it

```
# 1. No token: 401
curl -i http://localhost:8080/employees

# 2. Log in and copy the token from the response
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"your_admin_password"}'

# 3. Call the API with the token
curl http://localhost:8080/employees -H "Authorization: Bearer <token>"
curl "http://localhost:8080/employees?state=Bihar" -H "Authorization: Bearer <token>"
```

In Windows PowerShell use `curl.exe` instead of `curl`, or use Postman (Authorization tab, type Bearer Token).

## Configuration

`src/main/resources/application.properties`:

```
spring.application.name=resttry1
spring.datasource.name=test
spring.datasource.url=jdbc:mysql://localhost:3306/resttry2
spring.datasource.username=${DB_USER_WORKBENCH}
spring.datasource.password=${DB_PASS_WORKBENCH}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
```

If your MySQL runs on a different port or you want another schema name, change the URL here.

## Testing

The project has **64 automated tests**. They use H2 in memory (`src/test/resources/application.properties`), so **no MySQL and no environment variables are needed** to run them.

```
./mvnw test
```

In Eclipse/STS: right-click `src/test/java` > Run As > JUnit Test.

| Test class                 | Type                                   | Tests |
|----------------------------|----------------------------------------|-------|
| `EmployeeServiceTest`      | Mockito unit test                      | 12    |
| `UserServiceTest`          | Mockito unit test                      | 8     |
| `JWTServiceTest`           | Plain unit test                        | 7     |
| `MyUserDetailsServiceTest` | Mockito unit test                      | 3     |
| `EmployeeControllerTest`   | `@WebMvcTest` + MockMvc                | 10    |
| `UserControllerTest`       | `@WebMvcTest` + MockMvc                | 4     |
| `EmployeeRepoTest`         | `@DataJpaTest` on H2                   | 6     |
| `UserRepoTest`             | `@DataJpaTest` on H2                   | 4     |
| `AuthFlowIntegrationTest`  | `@SpringBootTest` + MockMvc on H2      | 10    |

What they cover:

- Service logic and every error path (`404`, `409`), with the repository mocked
- JWT creation, validation, expiry and tampered tokens
- Controller status codes and JSON (the password is never in a response)
- Repository queries, including `findByState` and the unique-username rule
- The real security chain end to end: no token gives `401`, a fake token gives `401`, register then login then `/employees` works, a wrong password gives `401`, an `ADMIN` role in the register body is ignored, the startup admin can log in, and a full employee create, read, update and delete runs with a token

All endpoints were also tested manually in Postman, including the error cases.

## Limitations

- There is no logout endpoint. With stateless JWT the client simply discards the token, which stays valid until it expires (30 minutes) or the app restarts.
- The JWT signing key is generated randomly at every startup, so tokens do not survive a restart.
- Any logged-in user (`USER` or `ADMIN`) can currently use all employee endpoints. The role is stored and carried as the user's authority, so admin-only rules can be added with `hasAuthority("ADMIN")`.
- There are no refresh tokens and no rate limiting on login.

## Possible Improvements

- Logout with a token blacklist
- Admin-only write endpoints (POST, PUT, DELETE) with matching tests
- Load the JWT secret from configuration so tokens survive a restart
- Request validation (`@Valid`) and a global exception handler
- DTOs instead of exposing the entity directly
- Pagination and sorting on `GET /employees`
- Swagger / OpenAPI documentation

## Author

**Sumit Kumar** GitHub: [sumitkumar93041](https://github.com/sumitkumar93041)
