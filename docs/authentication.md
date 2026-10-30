# Authentication & User Management Blueprint

## 1. Overview & Architectural Principles

The **Authentication (`com.aquaai.auth`)** and **User (`com.aquaai.user`)** modules in **YANGIARIQ AQUA AI** establish a secure, production-grade identity, access control, and user management subsystem built within our modular monolith architecture.

### Architectural Segregation
- **`com.aquaai.auth`**: Responsible strictly for authentication lifecycle, credential verification, JWT issuance and signing, refresh token rotation, compromised session detection, logout session invalidation, and password reset workflows.
- **`com.aquaai.user`**: Responsible for user identity state, farmer and operator profiles, status management, contact information updates, and self-service domain operations.
- **Cross-Module Boundaries**: The `auth` module interacts with `user` via the `UserRepository` interface and domain entities. Auth does not duplicate user records. JPA entities are never exposed in API responses; all endpoints consume and produce validated DTOs wrapped in `ApiResponse<T>`.

---

## 2. Domain & Security Models

### User Status Model (`UserStatus`)
| Status | Description | Can Authenticate? |
| :--- | :--- | :--- |
| `ACTIVE` | Normal operating state; user is authorized to log in, rotate tokens, and perform operations. | Yes |
| `INACTIVE` | Account created or temporarily deactivated; cannot authenticate. | No (401 Bad Credentials) |
| `BLOCKED` | Account suspended due to security infraction or administrative sanction; cannot authenticate. | No (403 Forbidden with `USER_ACCOUNT_BLOCKED`) |

### Roles and Permissions (`Role`)
All role evaluations use type-safe enums rather than arbitrary strings:
- **`FARMER`**: Default self-registration role. Can view and update their own profile, register and manage their agricultural fields, submit irrigation logs, and report canal/water problems.
- **`OPERATOR`**: Yangiariq district water-resource management authority. Can view district dashboards, monitor canal and irrigation statuses across farmers, and manage problem report triage.
- **`ADMIN`**: Platform administrator with full override capabilities across user accounts, statuses, and system configurations.

### Ownership-Aware Authorization (`SecurityContextHelper`)
To enforce multi-tenant field data privacy and prevent Insecure Direct Object References (IDOR):
- `SecurityContextHelper.verifyOwnership(targetUserId, allowedOverrideRoles...)` verifies whether the currently authenticated principal is the resource owner.
- Privileged roles (such as `ADMIN` or `OPERATOR`) can be passed to bypass ownership checks where cross-farmer governance is required.
- Non-owners attempting unauthorized access are rejected with HTTP 403 Forbidden (`FORBIDDEN_OPERATION`).

---

## 3. Token Lifecycle & Session Architecture

```
+---------------+           +-------------------------+           +--------------------------+
|    Client     |           |       Spring Boot       |           |        PostgreSQL        |
+---------------+           +-------------------------+           +--------------------------+
        |                                |                                     |
        |--- 1. POST /auth/login ------->|                                     |
        |    (identifier + password)     |-- 2. Verify BCrypt Hash ----------> |
        |                                |-- 3. Generate Access Token (JWT)    |
        |                                |-- 4. Generate Refresh Token (UUID)  |
        |                                |-- 5. Store SHA-256 Hash of Refresh->|
        |<-- 6. Returns Access + Refresh-|                                     |
        |                                |                                     |
        |--- 7. POST /auth/refresh ----->|                                     |
        |    (plain refresh token)       |-- 8. Find session by SHA-256 hash ->|
        |                                |-- 9. Check revoked/reuse/expiration |
        |                                |-- 10. Revoke old, save new token -->|
        |<-- 11. New Access + Refresh ---|                                     |
```

### Access Tokens (Stateless JWT)
- **Format**: Signed HMAC-SHA256 (HS256) JSON Web Token.
- **Claims**: Subject (`userId`), `role`, `phoneNumber`, `email`, `iat`, `exp`.
- **Default Lifespan**: 24 hours (`JWT_ACCESS_TOKEN_EXPIRATION` configurable; recommended 15–60 minutes in hardened production).
- **Verification**: Fully stateless, verified on every incoming request by `JwtAuthenticationFilter` with zero database round-trips.

### Refresh Tokens (Stateful Multi-Device Sessions)
- **Format**: High-entropy cryptographically secure random UUID string (`raw token`).
- **Database Storage**: Raw refresh tokens are **never** stored in plaintext. Only the SHA-256 hex digest (`token_hash`) is persisted in `refresh_token_sessions`.
- **Lifespan**: 7 days (`JWT_REFRESH_TOKEN_EXPIRATION` configurable; default 604,800,000 ms).
- **Multi-Device Support**: Each login from a web browser, mobile device, or tablet generates an independent session row linked to `user_id`.

---

## 4. Refresh Token Rotation & Reuse Detection

To neutralize token theft and replay attacks, YANGIARIQ AQUA AI implements strict **Refresh Token Rotation (RTR)**:

1. **Normal Rotation**:
   - The client submits `POST /api/v1/auth/refresh` with `{ "refreshToken": "<raw_token>" }`.
   - The service hashes the incoming token using SHA-256 and searches `refresh_token_sessions`.
   - If the session is active and unexpired:
     - A new refresh token is generated and saved as a new session row.
     - The old session is immediately marked `revoked = true`, with `revoked_at = now()` and `replaced_by_token_id = <new_session_id>`.
     - A newly minted JWT access token and new plain refresh token are returned to the client.

2. **Reuse Detection (Compromise Response)**:
   - If an incoming refresh token's hash matches an **already revoked session**, a token reuse/theft event is detected (indicating an attacker or victim attempted to reuse a spent token).
   - **Mitigation Action**: `AuthService` immediately issues a high-priority security alert and executes `revokeAllActiveSessionsForUser(userId)`.
   - All active devices for that user are logged out instantly, preventing further unauthorized access.
   - The request is rejected with HTTP 403 Forbidden (`INVALID_REFRESH_TOKEN`).

---

## 5. Password Reset Workflow

```
+--------+                      +-------------------+                    +-------------------------+
| Client |                      |    AuthService    |                    | Notification Provider   |
+--------+                      +-------------------+                    +-------------------------+
    |                                     |                                           |
    |--- 1. POST /password-reset/request->|                                           |
    |    (email or phone)                 |-- 2. Lookup user (no error if missing)   |
    |                                     |-- 3. Generate raw reset token (UUID)      |
    |                                     |-- 4. Persist SHA-256 token hash & expiry  |
    |<-- 5. Always 200 OK (Generic) ------|-- 6. Dispatch raw token asynchronously -->|
    |                                                                                 |
    |--- 7. POST /password-reset/confirm (token + newPassword) ---------------------->|
    |    Verifies hash, expiration, and used status                                   |
    |    BCrypt-hashes new password, marks token used, and revokes all active sessions|
    |<-- 8. 200 OK (Password Reset Successful) ---------------------------------------|
```

### Key Security Invariants
- **Non-Enumerating Response**: `POST /api/v1/auth/password-reset/request` always returns HTTP 200 OK with a generic message (`"If the account exists, instructions have been sent."`), regardless of whether the email/phone exists. This prevents user enumeration.
- **Hashed Token Storage**: The database only stores `SHA-256(rawResetToken)`. If the database is compromised, reset tokens cannot be reconstructed.
- **Single-Use & Expiration**: Tokens expire in 1 hour (`DEFAULT_RESET_TOKEN_EXPIRATION_MINUTES = 60`) and are invalidated with `used_at = now()` immediately upon first confirmation.
- **Session Revocation on Reset**: When a password is reset, all existing active refresh token sessions across all devices are immediately revoked.
- **Notification Abstraction**: `PasswordResetNotificationProvider` provides a decoupled port (`sendPasswordResetToken(identifier, rawToken)`). In testing and local dev, `MockPasswordResetNotificationProvider` records tokens in-memory for deterministic assertion; in production, an SMS or SMTP adapter can be plugged in without domain changes.

---

## 6. Endpoints & API Specifications

All endpoints use `/api/v1` prefixes and return standard `ApiResponse<T>` envelopes.

### Authentication Endpoints (`com.aquaai.auth.api.AuthController`)

#### 1. Registration: `POST /api/v1/auth/register`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "firstName": "Rustam",
    "lastName": "Karimov",
    "phoneNumber": "+998901234567",
    "email": "rustam@yangiariq.uz",
    "password": "SecurePassword123!",
    "district": "Yangiariq"
  }
  ```
- **Behavior**: Normalizes phone (E.164) and email (trimmed lowercase). Verifies uniqueness (returns 409 Conflict if taken). Assigns role `FARMER` and status `ACTIVE`.
- **Response**: `201 Created` with `AuthResponse` containing `accessToken`, `refreshToken`, and user summary.

#### 2. Login: `POST /api/v1/auth/login`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "identifier": "+998901234567",
    "password": "SecurePassword123!"
  }
  ```
- **Behavior**: Matches `identifier` against either `phoneNumber` or `email`. Verifies BCrypt hash. Rejects non-active accounts. Issues new access and refresh tokens.
- **Response**: `200 OK` with `AuthResponse`.

#### 3. Refresh Token Rotation: `POST /api/v1/auth/refresh`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "refreshToken": "7c9e6679-7425-40de-944b-e07fc1f90ae7"
  }
  ```
- **Behavior**: Validates hash against `refresh_token_sessions`. Triggers reuse detection if token was already revoked. Returns new access and rotated refresh token.
- **Response**: `200 OK` with `AuthResponse`.

#### 4. Logout: `POST /api/v1/auth/logout`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "refreshToken": "7c9e6679-7425-40de-944b-e07fc1f90ae7"
  }
  ```
- **Behavior**: Identifies session by SHA-256 hash and sets `revoked = true`, `revoked_at = now()`. Idempotent operation.
- **Response**: `200 OK` with `success: true`.

#### 5. Change Password: `POST /api/v1/auth/change-password`
- **Access**: Authenticated (`Bearer <access-token>`)
- **Request Body**:
  ```json
  {
    "currentPassword": "SecurePassword123!",
    "newPassword": "NewStrongPassword456!"
  }
  ```
- **Behavior**: Validates current password against stored BCrypt hash. Updates password hash. Revokes all active refresh token sessions for the authenticated user.
- **Response**: `200 OK` with `success: true`.

#### 6. Request Password Reset: `POST /api/v1/auth/password-reset/request`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "identifier": "rustam@yangiariq.uz"
  }
  ```
- **Behavior**: Dispatches secure reset token if user exists. Always responds with generic non-enumerating message.
- **Response**: `200 OK` with message.

#### 7. Confirm Password Reset: `POST /api/v1/auth/password-reset/confirm`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "token": "4a7378d4-531e-450b-99f5-147bdde29d89",
    "newPassword": "BrandNewPassword789!"
  }
  ```
- **Behavior**: Verifies token hash, checks expiration and single-use status. Updates password and revokes all active sessions.
- **Response**: `200 OK` with message.

---

### User Endpoints (`com.aquaai.user.api.UserController`)

#### 1. Get Current User Profile: `GET /api/v1/user/profile`
- **Access**: Authenticated (`Bearer <access-token>`)
- **Response**: `200 OK` with `UserProfileResponse`:
  ```json
  {
    "success": true,
    "data": {
      "id": "b3e0c0a9-25f3-4d6b-b4a1-b6a88b5774a3",
      "firstName": "Rustam",
      "lastName": "Karimov",
      "fullName": "Rustam Karimov",
      "phoneNumber": "+998901234567",
      "email": "rustam@yangiariq.uz",
      "role": "FARMER",
      "status": "ACTIVE",
      "district": "Yangiariq",
      "createdAt": "2026-10-30T10:00:00Z",
      "updatedAt": "2026-10-30T10:00:00Z"
    }
  }
  ```

#### 2. Update Current User Profile: `PATCH /api/v1/user/profile`
- **Access**: Authenticated (`Bearer <access-token>`)
- **Request Body**:
  ```json
  {
    "firstName": "Rustambek",
    "lastName": "Karimov",
    "email": "new.email@yangiariq.uz",
    "district": "Yangiariq"
  }
  ```
- **Behavior**: Updates profile fields. If email is changed, uniqueness is validated against other users.
- **Response**: `200 OK` with updated `UserProfileResponse`.

---

## 7. Security Hardening & Best Practices

1. **Password Hashing**: BCrypt with default work factor (10). Upgrades transparently when configured via Spring Security's `PasswordEncoderFactories`.
2. **Timing Attack & Enumeration Resistance**:
   - Authentication rejects non-existent users and password mismatches with the exact same error code (`BAD_CREDENTIALS`).
   - Password reset requests never reveal whether an identifier exists in the system.
3. **Token Hash Storage**: Raw refresh and reset tokens are never exposed in log files or database snapshots. Only SHA-256 digests are persisted.
4. **CORS Policy**: Configured explicitly in `SecurityConfig` and restricted to `CORS_ALLOWED_ORIGINS`.
5. **Architectural Governance**: ArchUnit rules strictly enforce that `UserController` and `AuthController` never inject repositories directly, never return persistence entities, and remain separated according to package-by-feature conventions.

---

## 8. Future Production Recommendations

While the current foundation meets all MVP and production stability requirements, the following enhancements should be introduced as adoption scales:
- **Rate Limiting**: Implement IP and identifier-based rate limiting (via Spring Bucket4j or Redis token bucket) on `/login`, `/register`, and `/password-reset/request` to mitigate brute-force and credential-stuffing attacks.
- **Two-Factor Authentication (2FA / OTP)**: Integrate SMS OTP delivery via local Uzbekistan SMS aggregators (e.g., Eskiz SMS, PlayMobile) for phone number verification and multi-factor login.
- **Audit Logging**: Introduce an asynchronous audit event table recording critical security actions (`LOGIN_SUCCESS`, `LOGIN_FAILED`, `TOKEN_REUSE_DETECTED`, `PASSWORD_CHANGED`, `ROLE_CHANGED`) with client IP and User-Agent metadata.
- **Token Blacklisting / Short-Lived Access Tokens**: In high-security enterprise environments, keep access token duration to 15 minutes and maintain an in-memory revocation bloom filter for immediate access token invalidation before expiration.
