# Security Architecture & Governance

## 1. Overview

**YANGIARIQ AQUA AI** enforces defense-in-depth across the entire stack, adhering to:
- **Zero Trust & Default-Deny**: Every endpoint requires explicit authentication and authorization authorities.
- **Hierarchical Centralized RBAC**: Four platform roles (`SUPER_ADMIN`, `ADMIN`, `OPERATOR`, `FARMER`) with 30 granular platform permissions.
- **Tenant & Resource Isolation (Anti-IDOR)**: Ownership checks on farms, fields, sensors, and water quotas.
- **Privilege Escalation Immunity**: Role modification, account deactivation, and Super Admin protections.
- **Immutable Security Auditing**: All administrative actions logged to `security_audit_logs`.
- **Physical Actuation Prohibited**: Platform remains advisory/decision-support only; physical pumps and gates are not actuated.

---

## 2. Authentication & Session Security

- **JWT Tokens**: Signed using HMAC-SHA256 (`HS256`) with a cryptographically secure 256-bit secret.
- **Active Status Check on Request**: In `JwtAuthenticationFilter`, every request resolves the user against the database. If the user's status is `INACTIVE` or `BLOCKED`, the request is rejected with `401 UNAUTHORIZED`, immediately terminating active sessions.
- **Password Hashing**: BCrypt hashing with configurable work factor (default 12 rounds).
- **Rate Limiting**: Configured bucket-token rate limiter guarding authentication endpoints against brute force attacks.

---

## 3. Authorization & RBAC

See [`docs/authorization-model.md`](file:///home/izzatbek/Desktop/agronexsus/docs/authorization-model.md) and [`docs/role-permission-matrix.md`](file:///home/izzatbek/Desktop/agronexsus/docs/role-permission-matrix.md) for full specifications.

---

## 4. Operational & Pilot Security

- **Four-Eyes Governance**: Critical data approvals (e.g. pilot ground-truth validation) require a distinct reviewer different from the submitter.
- **Data Provenance**: Ingested measurements retain full provenance (source type, batch ID, checksum, reliability rate).
- **No Mock or Simulated Data in Production Verification**: Simulated data is explicitly tagged `SIMULATED` and segregated from ground truth.
