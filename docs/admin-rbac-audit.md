# Security & RBAC Baseline Audit (Milestone 08.20.1)

## 1. Executive Summary

This audit evaluates the platform identity, access control, administrative governance, and role-based access control (RBAC) across **YANGIARIQ AQUA AI** prior to the implementation of **Milestone 08.20.1: Admin Management, Centralized RBAC & Security Governance**.

The audit covers:
- Backend authentication, authorization models, token lifecycles, and security filters.
- Method-level authorization and domain-level tenancy checks.
- Administrative user APIs, role structures, and security audit logging.
- Frontend navigation, protected routing, user management, and UI permission awareness.
- Database schemas and Liquibase migrations (changesets `001`–`027`).

---

## 2. Audit Matrix by Subsystem & Capability

Each capability is classified into one of four states:
- **`IMPLEMENTED`**: Fully functioning in code, persistent in database, verified with automated tests.
- **`PARTIALLY_IMPLEMENTED`**: Basic logic exists, but lacks granularity, administrative controls, or full integration.
- **`MISSING`**: Does not exist in backend or frontend; required for milestone completion.
- **`NOT_APPLICABLE`**: Out of scope for monolithic RBAC architecture or explicitly disallowed.

### 2.1 Backend Identity, Authentication & Token Management

| Capability | Status | Evidence & Audit Observations | Required Action |
| :--- | :---: | :--- | :--- |
| **Password Hashing (BCrypt)** | `IMPLEMENTED` | `BCryptPasswordEncoder` bean in `SecurityConfig.java`. Salted hashing enforced on registration and password change. | Preserve existing password hashing mechanism. |
| **Stateless JWT Issuance** | `IMPLEMENTED` | `JwtTokenProvider.java` issues HMAC-SHA256 signed tokens containing `sub` (userId), `username`, and `role`. | Extend claims / authorities to support granular permissions. |
| **Refresh Token Persistence & Rotation** | `IMPLEMENTED` | `refresh_token_sessions` table (changeset `002`), single-use hash checking, reuse detection, and session revocation. | Preserve session revocation on account status changes. |
| **Security Event Invalidation** | `PARTIALLY_IMPLEMENTED` | `UserSecurityInvalidationEvent` triggers `refreshTokenSessionRepository.revokeAllActiveSessionsForUser()`. However, `JwtAuthenticationFilter` does not check if the user is active/blocked on each request. | Invalidate active JWT verification if user is marked `BLOCKED` or `INACTIVE`. |
| **Public vs Protected Endpoints** | `IMPLEMENTED` | `SecurityConfig.PUBLIC_ENDPOINTS` permits `/auth/**`, `/actuator/**`, and Swagger. All other routes require authentication. | Maintain default-deny policy. |
| **Platform Role Structure** | `PARTIALLY_IMPLEMENTED` | `UserRole` enum contains `FARMER`, `OPERATOR`, `ADMIN`. Missing `SUPER_ADMIN` role required for platform-wide governance. | Introduce `SUPER_ADMIN` platform role while preserving existing user assignments. |
| **Granular Permissions Catalog** | `MISSING` | `UserPrincipal.getAuthorities()` only emits `"ROLE_" + role.name()`. No fine-grained authorities (e.g. `USER_VIEW`, `ROLE_MANAGE`, `AUDIT_LOG_VIEW`). | Introduce database-backed/domain-backed permissions catalog and assign to roles. |
| **Role & Permission Management API** | `MISSING` | No endpoints or services exist to list permissions, inspect role-permission matrices, or manage custom roles. | Create centralized Role & Permission management backend (`RoleService`, `RoleController`). |
| **Platform-Wide Security Audit Logging** | `MISSING` | Model governance has `GovernanceAuditLog`, but no security audit ledger tracks user creation, role modification, or status change. | Introduce `security_audit_logs` table and transactional `SecurityAuditService`. |
| **Real System Settings API** | `MISSING` | No administrative endpoint to inspect genuine backend runtime configurations (JWT TTL, rate limits, active providers). | Implement `/api/v1/admin/settings` exposing real application environment metadata. |

### 2.2 User Management Backend

| Capability | Status | Evidence & Audit Observations | Required Action |
| :--- | :---: | :--- | :--- |
| **User Directory Query & Search** | `IMPLEMENTED` | `UserController.getUsers` provides pagination, search across phone/email/name, and filtering by role/status/district. | Guard with `USER_VIEW` permission and ensure `SUPER_ADMIN` visibility. |
| **Administrative User Creation** | `MISSING` | Only public self-registration (`/api/v1/auth/register`) exists, which forces `role = FARMER`. Admin cannot provision operators/admins. | Implement `POST /api/v1/admin/users` allowing authorized administrators to provision users. |
| **User Status Management** | `IMPLEMENTED` | `UserController.changeUserStatus` allows `ADMIN` to set `ACTIVE`, `INACTIVE`, `BLOCKED`. | Add protections: prevent deactivating the last active `SUPER_ADMIN`; prevent `ADMIN` from deactivating `SUPER_ADMIN`. |
| **User Role Assignment** | `PARTIALLY_IMPLEMENTED` | `UserController.changeUserRole` exists but has no hierarchy defense: `ADMIN` can assign any role without restriction. | Enforce: only `SUPER_ADMIN` can assign `SUPER_ADMIN` or `ADMIN`; prevent privilege escalation. |
| **Self-Privilege Escalation Prevention** | `PARTIALLY_IMPLEMENTED` | `UserService` prevents admins from changing their own role to non-admin, but doesn't prevent non-super-admins from escalating. | Implement strict hierarchy checks and audit logging on role mutations. |
| **Passwords Excluded from DTOs** | `IMPLEMENTED` | `UserProfileResponse` explicitly omits `passwordHash` and sensitive tokens. | Verify in new DTOs. |

### 2.3 Frontend Route, Navigation & Admin UX

| Capability | Status | Evidence & Audit Observations | Required Action |
| :--- | :---: | :--- | :--- |
| **Auth State & Token Storage** | `IMPLEMENTED` | `AuthContext.tsx` and `authStorage.ts` manage JWT and refresh tokens in `localStorage` with reactive 401 broadcast events. | Maintain existing token storage. |
| **Admin Navigation in Sidebar** | `MISSING` | `Sidebar.tsx` has no navigation item or submenu for administration. All navigation items are hardcoded. | Add permission-aware `Administration` section in `Sidebar` and `MobileSidebar`. |
| **Admin Overview Dashboard** | `MISSING` | No administrative overview page exists. | Create `AdminPage` with tabs: Overview, Users, Roles & Permissions, Audit Logs, Settings. |
| **User Management UI** | `MISSING` | No frontend user management interface exists (no user directory, status toggle, or role change dialogs). | Build responsive user management table with pagination, search, filter, and edit/create modals. |
| **Role & Permission Matrix UI** | `MISSING` | No visual matrix displaying roles and their granular capabilities. | Build interactive permission matrix component with system-role protection indicators. |
| **Security Audit Logs Viewer** | `MISSING` | No frontend interface to review security audit events. | Build searchable, paginated audit log table with metadata inspection modal. |
| **Permission-Aware UI Components** | `MISSING` | Components check `user.role` ad-hoc rather than granular permissions. | Add permission helper hooks (`usePermissions()`) to show/hide controls cleanly. |

---

## 3. Important Distinctions: Platform Roles vs. Pilot Participant Roles

A critical observation from Milestone 08.20:
- **Platform Security Roles (`UserRole`)**: Define system-level access to APIs, databases, and global administrative actions.
  - Recommended set: `SUPER_ADMIN`, `ADMIN`, `OPERATOR`, `FARMER`.
- **Pilot Participant Roles (`PilotParticipant.roleInPilot`)**: Define operational field duties within a specific bounded pilot program:
  - `FIELD_LEAD`: Senior agronomist coordinating sampling.
  - `OBSERVER`: Independent academic or auditor.
  - `OPERATOR`: Canal gatekeeper or ditch rider.
  - `FARMER`: Local landowner or cooperative tenant.

**Architectural Rule**: Pilot participant roles must **never** be merged with platform-level security roles. A user can be a `FARMER` on the platform and act as a `FIELD_LEAD` within a specific pilot parcel.

---

## 4. Required Technical Stack Extensions

1. **Liquibase Migration `028-admin-rbac-and-audit-tables.sql`**:
   - `roles` table: System roles (`SUPER_ADMIN`, `ADMIN`, `OPERATOR`, `FARMER`) and metadata.
   - `permissions` table: Granular authorities across all platform modules.
   - `role_permissions` join table: Pre-seeded mapping of roles to authorized permissions.
   - `security_audit_logs` table: Transactional audit ledger with actor, action, target, metadata, and IP.
2. **Backend Security Enhancements**:
   - Update `UserRole` enum to include `SUPER_ADMIN`.
   - Update `UserPrincipal` and `JwtAuthenticationFilter` to map role-based permissions into Spring Security authorities.
   - Implement `SecurityAuditService` to automatically record sensitive admin actions.
   - Add `RoleService` and `RoleController` for role and permission inspection.
   - Add `AdminUserController` and `AdminSettingsController`.
3. **Frontend Admin Experience**:
   - Create `AdminPage.tsx` with tabs: Overview, Users, Roles & Permissions, Audit Logs, System Settings.
   - Connect to real backend APIs with TanStack Query.
   - Ensure complete i18n in Uzbek (`uz`), Russian (`ru`), and English (`en`).
