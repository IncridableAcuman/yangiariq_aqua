# Administrative Management & Operations Guide

## 1. Overview

Milestone **08.20.1** introduces dedicated administrative management capabilities to **YANGIARIQ AQUA AI**.

Administrative functionality is accessed via the **Administration** dashboard (`/admin` / `administration` section in navigation) and backed by the `admin` bounded context.

---

## 2. Administrative Capabilities

### 2.1 User Management
- **Administrative Account Provisioning**: Provision new accounts directly without public self-registration via `POST /api/v1/users`. Requires password, contact information, and role assignment.
- **Role Assignment & Transitions**: Assign platform roles (`FARMER`, `OPERATOR`, `ADMIN`, `SUPER_ADMIN`). Promoted accounts immediately inherit updated authorities upon next request.
- **Account Lifecycle & Status Management**: Transition accounts between `ACTIVE`, `INACTIVE`, and `BLOCKED` states. Transitioning to `INACTIVE` or `BLOCKED` immediately revokes active JWT sessions at the filter level (`JwtAuthenticationFilter`).
- **Privilege Escalation Defense**: Standard `ADMIN` users are strictly barred from viewing or mutating `SUPER_ADMIN` accounts. Any attempt to modify a `SUPER_ADMIN` returns `403 FORBIDDEN`.

### 2.2 Roles & Permissions Management
- **Matrix View**: Interactive matrix displaying all 30 platform permissions grouped into 8 functional categories.
- **System Role Protection**: System roles are protected against deletion.
- **Custom Role Provisioning**: Super Administrators can create organizational-specific custom roles (e.g. `AGRONOMIST_LEAD`, `CANAL_INSPECTOR`) with granular permission subsets.

### 2.3 Security Audit Trail
- **Immutable Log Store**: Backed by `security_audit_logs` table.
- **Event Capture**: Captures user creations, updates, role changes, status mutations, deletions, role configuration updates, login events, and access denials.
- **Audit Detail Inspection**: Audit events record client IP addresses, actor metadata, target entities, and sanitized JSON change payloads.

### 2.4 System Runtime Posture Inspection
- **Genuine Runtime State**: Exposes active runtime configurations via `GET /api/v1/admin/settings`.
- **Transparency**: Discloses environment (`production` / `development`), application version, token expiration window, sensor provider adapter status, weather adapter status, and strict prohibition of physical actuation.

---

## 3. Administrative REST Endpoints

| Method | Endpoint | Authority Required | Description |
|---|---|---|---|
| `GET` | `/api/v1/users` | `USER_READ` / `ROLE_ADMIN` | List users with pagination and search |
| `POST` | `/api/v1/users` | `USER_CREATE` / `ROLE_ADMIN` | Provision a new user account |
| `GET` | `/api/v1/users/summary` | `USER_READ` / `ROLE_ADMIN` | Retrieve user count summary statistics |
| `GET` | `/api/v1/users/{id}` | `USER_READ` / Ownership | Fetch user profile details |
| `PUT` | `/api/v1/users/{id}` | `USER_UPDATE` / `ROLE_ADMIN` | Update user profile details |
| `PATCH` | `/api/v1/users/{id}/role` | `USER_ROLE_CHANGE` / `ROLE_SUPER_ADMIN` | Change a user's platform role |
| `PATCH` | `/api/v1/users/{id}/status` | `USER_STATUS_CHANGE` / `ROLE_ADMIN` | Activate, deactivate, or block user |
| `DELETE` | `/api/v1/users/{id}` | `USER_DELETE` / `ROLE_SUPER_ADMIN` | Soft-delete a user account |
| `GET` | `/api/v1/admin/roles` | `ROLE_ADMIN` / `ROLE_SUPER_ADMIN` | List all platform roles with user counts |
| `POST` | `/api/v1/admin/roles` | `ROLE_MANAGE` / `ROLE_SUPER_ADMIN` | Create a new custom role |
| `PUT` | `/api/v1/admin/roles/{code}/permissions` | `ROLE_MANAGE` / `ROLE_SUPER_ADMIN` | Update permission bindings for a role |
| `DELETE` | `/api/v1/admin/roles/{code}` | `ROLE_MANAGE` / `ROLE_SUPER_ADMIN` | Delete a custom role |
| `GET` | `/api/v1/admin/roles/permissions` | `ROLE_ADMIN` / `ROLE_SUPER_ADMIN` | Catalog of all 30 platform permissions |
| `GET` | `/api/v1/admin/roles/permissions/grouped` | `ROLE_ADMIN` / `ROLE_SUPER_ADMIN` | Permissions grouped by category |
| `GET` | `/api/v1/admin/audit-logs` | `AUDIT_LOG_READ` / `ROLE_ADMIN` | Query security audit trail with filters |
| `GET` | `/api/v1/admin/settings` | `ROLE_ADMIN` / `ROLE_SUPER_ADMIN` | Inspect runtime environment parameters |
