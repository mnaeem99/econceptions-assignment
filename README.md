# Assignment - Spring Boot Social Media Platform

A Spring Boot 3.4.5 application implementing a social media platform with user authentication, post management, commenting, liking, and following features. The application uses MySQL for persistence, JWT for authentication, and Swagger for API documentation.

## Table of Contents
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Setup Instructions](#setup-instructions)
- [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
    - [User Management APIs](#user-management-apis)
    - [Post Management APIs](#post-management-apis)
    - [Authentication](#authentication)
    - [Sample Payloads](#sample-payloads)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Features
- User registration and JWT-based authentication.
- Create, read, update, and delete posts.
- Comment on and like posts.
- Follow/unfollow users and view followers/following.
- Search posts and users by keywords with pagination.
- RESTful API with Swagger/OpenAPI documentation.
- Secure endpoints with role-based access control.
- MySQL database with JPA/Hibernate for persistence.

## Prerequisites
- **Java 17**: Ensure JDK 17 is installed.
- **Maven 3.8+**: For building and dependency management.
- **MySQL 8.0+**: Database for storing application data.
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code (optional).
- **cURL/Postman**: For testing API endpoints.

## Setup Instructions
1. **Clone the Repository**
   ```bash
   git clone https://github.com/mnaeem99/econceptions-assignment.git
   cd econceptions-assignment
   ```

2. **Configure MySQL**
    - Install MySQL and create a database named `socialmedia`.
    - Update `src/main/resources/application.properties` with your MySQL credentials:
      ```properties
      spring.datasource.url=jdbc:mysql://localhost:3306/socialmedia?createDatabaseIfNotExist=true
      spring.datasource.username=root
      spring.datasource.password=your-mysql-password
      ```
    - The provided `application.properties` uses `root`/`root`. Replace `root` with your actual MySQL password.

3. **Verify JWT Secret**
    - The `application.properties` includes a JWT secret:
      ```properties
      jwt.secret=ZmFzdGNvZGVhcHBfMTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkw
      ```
    - This is a base64-encoded secret. For production, generate a new secure key (at least 256 bits) and update this property.

4. **Install Dependencies**
    - Run the following to download dependencies:
      ```bash
      mvn clean install
      ```

## Running the Application
1. **Start the Application**
   ```bash
   mvn spring-boot:run
   ```
    - The application runs on `http://localhost:8080`.

2. **Access Swagger UI**
    - Open `http://localhost:8080/swagger-ui/index.html` in a browser to view and test API endpoints interactively.

3. **Verify Database**
    - Ensure MySQL is running. Hibernate will automatically create tables (`spring.jpa.hibernate.ddl-auto=update`).

## API Documentation
The application provides RESTful APIs for user and post management, documented via Swagger. Access the Swagger UI at `http://localhost:8080/swagger-ui/index.html` or the raw OpenAPI spec at `http://localhost:8080/api-docs`.

### User Management APIs
| Endpoint | Method | Description | Authentication Required | Request Body | Response |
|----------|--------|-------------|-------------------------|--------------|----------|
| `/users/register` | POST | Register a new user | No | `UserRegisterRequestDTO` | `UserResponseDTO` |
| `/users/login` | POST | Authenticate user and return JWT | No | `UserLoginRequestDTO` | JWT string |
| `/users/{id}` | GET | Get user profile by ID | No | None | `UserResponseDTO` |
| `/users/{id}/follow` | POST | Follow a user | Yes | None | 200 OK |
| `/users/{id}/followers` | GET | Get user's followers (paginated) | No | None | `Page<UserResponseDTO>` |
| `/users/{id}/following` | GET | Get users followed by a user (paginated) | No | None | `Page<UserResponseDTO>` |
| `/users/search` | POST | Search users by keyword (paginated) | No | `UserSearchRequestDTO` | `Page<UserResponseDTO>` |

**UserRegisterRequestDTO Example**:
```json
{
  "username": "mnaeem",
  "email": "naeem@example.com",
  "password": "securepassword",
  "bio": "Loves coding",
  "profilePicture": "http://example.com/profile.jpg"
}
```

**UserResponseDTO Example**:
```json
{
  "id": 1,
  "username": "mnaeem",
  "email": "naeem@example.com",
  "bio": "Loves coding",
  "profilePicture": "http://example.com/profile.jpg"
}
```

**UserLoginRequestDTO Example**:
```json
{
  "username": "mnaeem",
  "password": "securepassword"
}
```

**UserSearchRequestDTO Example**:
```json
{
  "keyword": "naeem"
}
```

### Post Management APIs
| Endpoint | Method | Description | Authentication Required | Request Body | Response |
|----------|--------|-------------|-------------------------|--------------|----------|
| `/posts` | POST | Create a new post | Yes | `PostCreateRequestDTO` | `PostResponseDTO` |
| `/posts` | GET | Get all posts (paginated, sorted) | No | None | `Page<PostResponseDTO>` |
| `/posts/{id}` | GET | Get post by ID | No | None | `PostResponseDTO` |
| `/posts/{id}` | PUT | Update a post (owner only) | Yes | `PostUpdateRequestDTO` | `PostResponseDTO` |
| `/posts/{id}` | DELETE | Delete a post (owner only) | Yes | None | 200 OK |
| `/posts/{id}/comments` | POST | Add comment to a post | Yes | `CommentRequestDTO` | `PostResponseDTO` |
| `/posts/{id}/like` | POST | Like a post | Yes | None | `PostResponseDTO` |
| `/posts/search` | POST | Search posts by keyword (paginated) | No | `PostSearchRequestDTO` | `Page<PostResponseDTO>` |

**PostCreateRequestDTO Example**:
```json
{
  "content": "Enjoying a sunny day at the park! 🌞"
}
```

**PostResponseDTO Example**:
```json
{
  "id": 1,
  "userId": 100,
  "content": "Enjoying a sunny day at the park! 🌞",
  "timestamp": "2025-04-25T14:30:00",
  "commentCount": 5,
  "likeCount": 42
}
```

**PostUpdateRequestDTO Example**:
```json
{
  "content": "Updated: Loving the sunny weather!"
}
```

**CommentRequestDTO Example**:
```json
{
  "content": "Looks like fun!"
}
```

**PostSearchRequestDTO Example**:
```json
{
  "keyword": "sunny"
}
```

**Query Parameters for Pagination/Sorting**:
- `page`: Page number (default: 0).
- `size`: Items per page (default: 10).
- `sortBy`: Field to sort by (e.g., `timestamp`, default: `timestamp`).
- `direction`: Sort direction (e.g., `asc`, `desc`, default: `desc`).

### Authentication
- **JWT Authentication**: Most endpoints (e.g., `/posts`, `/posts/{id}/like`, `/users/{id}/follow`) require a JWT token.
- **Obtaining a JWT**:
    - Call `POST /users/login` with a `UserLoginRequestDTO`.
    - Example using cURL:
      ```bash
      curl -X POST http://localhost:8080/users/login \
           -H "Content-Type: application/json" \
           -d '{"username":"mnaeem","password":"securepassword"}'
      ```
    - Response: A JWT string (e.g., `eyJhbGciOiJIUzUxMiJ9...`).
- **Using the JWT**:
    - Include the token in the `Authorization` header as `Bearer <token>` for protected endpoints.
    - Example:
      ```bash
      curl -X POST http://localhost:8080/posts \
           -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..." \
           -H "Content-Type: application/json" \
           -d '{"content":"New post!"}'
      ```

### Sample Payloads
- **Register a User**:
  ```bash
  curl -X POST http://localhost:8080/users/register \
       -H "Content-Type: application/json" \
       -d '{"username":"mnaeem","email":"naeem@example.com","password":"securepassword","bio":"Loves coding"}'
  ```

- **Create a Post**:
  ```bash
  curl -X POST http://localhost:8080/posts \
       -H "Authorization: Bearer <your-jwt-token>" \
       -H "Content-Type: application/json" \
       -d '{"content":"Hello world!"}'
  ```

- **Add a Comment**:
  ```bash
  curl -X POST http://localhost:8080/posts/1/comments \
       -H "Authorization: Bearer <your-jwt-token>" \
       -H "Content-Type: application/json" \
       -d '{"content":"Great post!"}'
  ```

- **Search Posts**:
  ```bash
  curl -X POST http://localhost:8080/posts/search?page=0&size=10 \
       -H "Content-Type: application/json" \
       -d '{"keyword":"sunny"}'
  ```

- **Search Users**:
  ```bash
  curl -X POST http://localhost:8080/users/search?page=0&size=10 \
       -H "Content-Type: application/json" \
       -d '{"keyword":"naeem"}'
  ```

## Project Structure
```
socialapp/
├── src/
│   ├── main/
│   │   ├── java/com/econceptions/socialapp/
│   │   │   ├── config/         # Security and JWT configuration
│   │   │   ├── controller/     # REST controllers (UserController, PostController)
│   │   │   ├── dto/           # Data Transfer Objects (UserRegisterRequestDTO, PostResponseDTO, etc.)
│   │   │   ├── entity/        # JPA entities (User, Post, Comment, Follow)
│   │   │   ├── repository/    # JPA repositories
│   │   │   ├── service/       # Business logic (UserService, PostService)
│   │   │   ├── util/          # Utility classes (JwtUtil)
│   │   │   └── SocialappApplication.java  # Main application class
│   │   └── resources/
│   │       └── application.properties  # Configuration file
│   └── test/                  # Unit and integration tests
├── pom.xml                    # Maven dependencies and build configuration
└── README.md                  # This file
```

## Testing
1. **Unit Tests**:
    - The project includes `spring-boot-starter-test` and `spring-security-test`.
    - Run tests with:
      ```bash
      mvn test
      ```

2. **Manual Testing**:
    - Use Swagger UI (`http://localhost:8080/swagger-ui/index.html`) for interactive testing.
    - Alternatively, use Postman or cURL to send requests (see [Sample Payloads](#sample-payloads)).

3. **Database Verification**:
    - Connect to MySQL and verify table creation:
      ```sql
      USE socialmedia;
      SHOW TABLES; -- Should list users, posts, comments, follows
      ```

## Troubleshooting
- **Application Fails to Start**:
    - Ensure MySQL is running and credentials in `application.properties` are correct.
    - Check for port conflicts (default: 8080). Change `server.port` if needed.
    - Verify Java 17 is used: `java -version`.

- **JWT Errors**:
    - Ensure `jwt.secret` is a valid base64-encoded string (minimum 256 bits).

## Contributing
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/YourFeature`).
3. Commit changes (`git commit -m "Add YourFeature"`).
4. Push to the branch (`git push origin feature/YourFeature`).
5. Open a pull request.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.