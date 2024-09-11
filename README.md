# Auth Service

This Spring Boot application provides an authentication service that supports user registration, token generation, token validation, and access token refreshing.

## Table of Contents

- [Requirements](#requirements)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)

## Requirements

- **Java 21**
- **Maven 3.6+**
- **MongoDB** (running locally or remotely)

## Installation

1. **Clone the Repository**:
    ```bash
    git clone https://github.com/yourusername/auth-service.git
    cd auth-service
    ```

2. **Build the Project**:
    ```bash
    mvn clean install
    ```

## Configuration

1. **MongoDB Setup**:
   - Ensure MongoDB is running.
   - Update the `application.properties` file located in the `src/main/resources` directory with your MongoDB connection details:
     ```properties
     spring.data.mongodb.uri=mongodb://localhost:27017/auth_service
     ```

## Running the Application

You can start the application using one of the following methods:

1. **Using Maven**:
    ```bash
    mvn spring-boot:run
    ```

2. **Using the Executable JAR**:
    ```bash
    java -jar target/auth-service-0.0.1-SNAPSHOT.jar
    ```

The application will start and be accessible at `http://localhost:8080`.

## API Endpoints

### `POST /auth/register`

Registers a new user.

- **Request**:
  - **Body**: `UserCredentialCreateRequestDTO` - JSON object containing the user credentials:
    ```json
    {
      "username": "user@example.com",
      "password": "password123",
      "roles": ["ROLE_USER"]
    }
    ```

- **cURL Example**:
    ```bash
    curl -X POST http://localhost:8080/auth/register \
    -H "Content-Type: application/json" \
    -d '{
          "username": "user@example.com",
          "password": "password123",
          "roles": ["ROLE_USER"]
        }'
    ```

- **Response**:
  - **200 OK**: If the user is successfully registered.

### `POST /auth/token`

Generates an authentication token for the user.

- **Request**:
  - **Body**: `AuthRequest` - JSON object containing the username and password:
    ```json
    {
      "username": "user@example.com",
      "password": "password123"
    }
    ```

- **cURL Example**:
    ```bash
    curl -X POST http://localhost:8080/auth/token \
    -H "Content-Type: application/json" \
    -d '{
          "username": "user@example.com",
          "password": "password123"
        }'
    ```

- **Response**:
  - **200 OK**: If the authentication is successful, returns the generated token:
    ```json
    {
      "accessToken": "access-token-value",
      "refreshToken": "refresh-token-value",
      "userId": "user-id",
      "roles": ["ROLE_USER"]
    }
    ```
  - **401 UNAUTHORIZED**: If the authentication fails.

### `POST /auth/validate`

Validates the provided authentication token.

- **Request**:
  - **Body**: `AuthValidationRequest` - JSON object containing the token to validate:
    ```json
    {
      "token": "access-token-value"
    }
    ```

- **cURL Example**:
    ```bash
    curl -X POST http://localhost:8080/auth/validate \
    -H "Content-Type: application/json" \
    -d '{
          "token": "access-token-value"
        }'
    ```

- **Response**:
  - **200 OK**: If the token is valid.

### `PUT /auth/{refreshToken}/refresh`

Refreshes the access token using the provided refresh token.

- **Request**:
  - **Path Parameter**: `refreshToken` - The refresh token used to generate a new access token.

- **cURL Example**:
    ```bash
    curl -X PUT http://localhost:8080/auth/{refreshToken}/refresh \
    -H "Content-Type: application/json"
    ```

- **Response**:
  - **200 OK**: Returns the new access token:
    ```json
    {
      "accessToken": "new-access-token-value",
      "refreshToken": "new-refresh-token-value",
      "userId": "user-id",
      "roles": ["ROLE_USER"]
    }
    ```

## Exception Handling

- **DuplicateUserException**: Thrown when attempting to register a user that already exists.
- **ResponseStatusException**: Thrown when authentication fails due to invalid credentials.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
