# Field Security, Ownership Hardening & Multi-Tenant Authorization

## 1. Executive Summary & Security Objectives

In **YANGIARIQ AQUA AI**, the **Field (`com.aquaai.field`)** module represents the primary physical boundary for agricultural land tenure, water distribution, telemetry sensing, and incident reporting. Because farmers operating in the Yangiariq district cultivate separate plots, ensuring multi-tenant isolation, protecting against Insecure Direct Object References (IDOR), and preventing unauthorized field enumeration are fundamental security requirements.

The core security invariant is:
> **A `FARMER` user can access, list, update, and deactivate only their own parcels. The backend enforces this strictly and authoritatively, independent of client or frontend declarations.**

---

## 2. Threat Model & Attack Vectors Considered

| Threat Vector | Attack Scenario | Mitigation & Defense |
| :--- | :--- | :--- |
| **Horizontal Privilege Escalation (IDOR)** | Farmer A discovers or guesses the UUID of Field B (owned by Farmer B) and calls `GET /api/v1/fields/{fieldB}`, `PATCH /api/v1/fields/{fieldB}`, or `DELETE /api/v1/fields/{fieldB}`. | **Query-Level Ownership**: Database queries enforce `owner_id = currentUserId`. Non-owned parcels return `404 Not Found` without loading or modifying data. |
| **Resource Existence Enumeration** | Malicious actor iterates UUIDs against `GET /api/v1/fields/{uuid}` looking for 403 vs 404 responses to confirm active field IDs. | **Uniform 404 Anti-Enumeration Strategy**: A non-existent UUID and another farmer's field UUID return the exact same `404 Not Found` envelope (`RESOURCE_NOT_FOUND`). |
| **Owner ID Injection** | Malicious client includes `"ownerId": "victim-user-id"` in `POST /api/v1/fields` to register parcels under another farmer's name. | **Server-Derived Identity**: Request DTOs do not bind `ownerId`. The backend authoritatively resolves `ownerId = currentUserProvider.getRequiredUserId()`. |
| **Immutable Attribute Tampering** | Attacker passes `"id"`, `"ownerId"`, `"status"`, or `"createdAt"` in `PATCH /api/v1/fields/{id}` to hijack parcel or manipulate lifecycle. | **Explicit Update DTO**: `UpdateFieldRequest` maps only mutable agricultural fields (`name`, `area`, `crop`, `soil`, `irrigation`, `boundary`). System identifiers and lifecycle status remain immutable. |
| **Filter Parameter Bypass** | Malicious farmer calls `GET /api/v1/fields?ownerId={victimId}` hoping to view another farmer's parcel portfolio. | **Query Filter Override**: For `FARMER` role, `ListMyFieldsUseCase` ignores client-passed `ownerId` and forces `effectiveOwnerId = currentUserId`. |
| **Sorting SQL / JPA Injection** | Attacker supplies arbitrary column names or injection vectors via `?sort=passwordHash` or `?sort=malicious_sql`. | **Strict Sort Whitelisting**: `FieldRepositoryImpl` validates against `ALLOWED_SORT_PROPERTIES = {createdAt, updatedAt, name, areaHectares, status}`. Unsupported fields fall back safely to `createdAt`. |
| **Role Escalation via Field API** | Farmer attempts to assign administrator privileges or call user status modification endpoints. | **Spring Security RBAC**: Administrative endpoints require `@PreAuthorize("hasRole('ADMIN')")`. Public registration defaults strictly to `FARMER`. |

---

## 3. Ownership Architecture: Query-Level Ownership

Rather than loading arbitrary entities into application memory and evaluating ownership post-fetch, YANGIARIQ AQUA AI employs **Query-Level Ownership**:

```
Client (FARMER)
      │
      ▼
FieldController (Validates Bearer JWT)
      │
      ▼
GetFieldUseCase / UpdateFieldUseCase / DeactivateFieldUseCase
      │
      ▼
FieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole)
      │
      ▼
FieldRepository.findByIdAndOwnerId(fieldId, currentUserId)
      │
      ▼
SELECT * FROM fields WHERE id = :fieldId AND owner_id = :currentUserId
      │
      ├──> [Row Found]   ──> Reconstitute Aggregate Root & Return (200 OK)
      └──> [0 Rows]      ──> Throw ResourceNotFoundException (404 Not Found)
```

### Architectural Benefits
1. **Zero Data Leakage**: Non-owned parcel data is never transferred from PostgreSQL to the JVM.
2. **Defeat of Timing & Enumeration Attacks**: Database index scan on `(id, owner_id)` takes equivalent execution time regardless of whether `id` exists for another owner or not.
3. **Transaction Safety**: Authorization is guaranteed at query resolution time before any mutation, entity state changes, or dirty checks can occur.

---

## 4. Role Authorization vs. Resource Ownership

The system strictly decouples **Role Authorization** (*What can this type of user do?*) from **Resource Ownership** (*Does this resource belong to this user?*):

```
+-----------------------------------------------------------------------------------------+
|                                    ACCESS EVALUATION                                    |
|                                                                                         |
|  1. ROLE CHECK (@PreAuthorize / UseCase)                                                |
|     - Is the user a FARMER? Can create own parcel, view own parcel, list own parcels.  |
|     - Is the user an OPERATOR? Can view district parcels for canal monitoring.          |
|     - Is the user an ADMIN? Can manage system-wide parcels.                             |
|                                                                                         |
|  2. OWNERSHIP CHECK (FieldOwnershipChecker)                                             |
|     - If FARMER: Must match field.ownerId == currentUserId                              |
|     - If OPERATOR: Read-only access permitted; mutation strictly forbidden (403)        |
|     - If ADMIN: Override permitted                                                      |
+-----------------------------------------------------------------------------------------+
```

---

## 5. Security Decision Matrix

| User Principal | HTTP Operation | Target Resource | Response Status | Security Behavior |
| :--- | :--- | :--- | :--- | :--- |
| **Anonymous** | Any endpoint | Any Field | `401 Unauthorized` | Rejected at `JwtAuthenticationFilter` |
| **Farmer A** | `POST /api/v1/fields` | New Field | `201 Created` | Owned strictly by Farmer A |
| **Farmer A** | `GET /api/v1/fields/{idA}` | Own Field A | `200 OK` | FieldResponse returned |
| **Farmer A** | `GET /api/v1/fields/{idB}` | Field B (Farmer B) | `404 Not Found` | Anti-enumeration: Treated as non-existent |
| **Farmer A** | `PATCH /api/v1/fields/{idB}`| Field B (Farmer B) | `404 Not Found` | Anti-enumeration: No mutation executed |
| **Farmer A** | `DELETE /api/v1/fields/{idB}`| Field B (Farmer B) | `404 Not Found` | Anti-enumeration: No status change |
| **Farmer A** | `GET /api/v1/fields?ownerId={idB}` | Field Query | `200 OK` | `ownerId` parameter ignored; returns Farmer A fields only |
| **Operator** | `POST /api/v1/fields` | New Field | `403 Forbidden` | Only farmers cultivate parcels |
| **Operator** | `GET /api/v1/fields/{idA}` | Any District Field | `200 OK` | District operational monitoring permitted |
| **Operator** | `GET /api/v1/fields` | District Fields | `200 OK` | Full district list / filter by owner permitted |
| **Operator** | `PATCH /api/v1/fields/{idA}`| Any Field | `403 Forbidden` | Read-only; cannot alter farmer boundaries |
| **Operator** | `DELETE /api/v1/fields/{idA}`| Any Field | `403 Forbidden` | Read-only; cannot deactivate parcels |
| **Admin** | `POST /api/v1/fields` | New Field | `403 Forbidden` | Admins manage users/system, parcels belong to farmers |
| **Admin** | `GET /api/v1/fields/{idA}` | Any Field | `200 OK` | Administrative audit permitted |
| **Admin** | `PATCH /api/v1/fields/{idA}`| Any Field | `200 OK` | Administrative parcel correction permitted |
| **Admin** | `DELETE /api/v1/fields/{idA}`| Any Field | `200 OK` | Administrative deactivation permitted |

---

## 6. Centralized Ownership Abstraction: `FieldOwnershipChecker`

Located in `com.aquaai.field.application.FieldOwnershipChecker`, this component is the sole entry point for ownership and role-access evaluations across the modular monolith:

- `getFieldForRead(UUID fieldId, UUID currentUserId, UserRole currentRole)`:
  - For `ADMIN` and `OPERATOR`: Executes `fieldRepository.findById(fieldId)`.
  - For `FARMER`: Executes `fieldRepository.findByIdAndOwnerId(fieldId, currentUserId)`.
- `getFieldForMutation(UUID fieldId, UUID currentUserId, UserRole currentRole)`:
  - For `ADMIN`: Executes `fieldRepository.findById(fieldId)`.
  - For `OPERATOR`: Throws `UnauthorizedOperationException` (403 Forbidden).
  - For `FARMER`: Executes `fieldRepository.findByIdAndOwnerId(fieldId, currentUserId)`.
- `isOwner(UUID fieldId, UUID userId)`: Returns boolean existence check without throwing exceptions.

### Reusability by Future Modules
Downstream modules (`water`, `problem`, `sensor`, `ai`) reuse `FieldOwnershipChecker` to establish root parcel authority before creating telemetry bindings or logging water deliveries:

```
WaterRecord / IncidentReport
          │
          ▼
    field_id (UUID)
          │
          ▼
FieldOwnershipChecker.getFieldForRead(fieldId, currentUserId, currentRole)
```

---

## 7. Automated Security Test Suite

The security policies are validated via automated tests in [`FieldIntegrationTest`](file:///home/izzatbek/Desktop/agronexsus/backend/src/test/java/com/aquaai/field/api/FieldIntegrationTest.java) and [`FieldPostgresDatabaseTest`](file:///home/izzatbek/Desktop/agronexsus/backend/src/test/java/com/aquaai/field/infrastructure/persistence/FieldPostgresDatabaseTest.java):

1. **Unauthenticated Access**: `401 Unauthorized` verified across all endpoints.
2. **Invalid & Malformed Tokens**: Missing Bearer prefix and corrupted signatures rejected with `401 Unauthorized`.
3. **IDOR GET Defense**: Farmer B accessing Field A returns `404 Not Found` (`RESOURCE_NOT_FOUND`).
4. **IDOR PATCH Defense**: Farmer B modifying Field A returns `404 Not Found`; Field A attributes remain identical.
5. **IDOR DELETE Defense**: Farmer B deactivating Field A returns `404 Not Found`; Field A status remains `ACTIVE`.
6. **Owner Injection Defense**: Request bodies with forged `"ownerId"` are ignored; parcel is registered strictly to authenticated user.
7. **Immutable Property Defense**: Tampering attempts on `"id"`, `"ownerId"`, `"status"`, `"createdAt"` via PATCH are discarded.
8. **Operator Mutation Defense**: Operator attempting PATCH/DELETE receives `403 Forbidden` (`UNAUTHORIZED_OPERATION`).
9. **Role Escalation Defense**: Farmer calling user role/status endpoints receives `403 Forbidden`.
10. **PostgreSQL Anti-Enumeration Query**: `findByIdAndOwnerId` verified against PostgreSQL 16 container.
