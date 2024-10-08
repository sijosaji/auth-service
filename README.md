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
    git clone https://github.com/sijosaji/auth-service.git
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
     spring.data.mongodb.uri=mongodb://localhost:27017/mongo_migration
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

The application will start and be accessible at `http://localhost:9000`.

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

- **Example Request**:
    ```bash
    curl -X POST http://localhost:9000/auth/register \
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

- **Example Request**:
    ```bash
    curl -X POST http://localhost:9000/auth/token \
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
      "token": "access-token-value",
      "roles": ["ROLE_USER"]
    }
    ```

- **Example Request**:
    ```bash
    curl -X POST http://localhost:9000/auth/validate \
    -H "Content-Type: application/json" \
    -d '{
          "token": "access-token-value",
          "roles": ["ROLE_USER"]
        }'
    ```

- **Response**:
  - **200 OK**: Returns userId if the token is valid.
  ```json
    {
      "userId": "user-id"
    }
    ```
- **401 UNAUTHORIZED**: If token validation fails.
- **403 FORBIDDEN**: If a user has insufficient roles.
### `PUT /auth/refresh`

Refreshes the access token using the provided refresh token.

- **Request**:
  - **Body**: `RefreshRequest` - JSON object containing refresh token to generate new access token:
    ```json
    {
      "refreshToken": "refresh-token-value"
    }
    ```

- **Example Request**:
    ```bash
    curl -X PUT http://localhost:9000/auth/refresh \
    -H "Content-Type: application/json"
    -d '{
          "refreshToken": "refresh-token-value"
        }'
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
  - **401 UNAUTHORIZED**: If refresh token expires or not present.
