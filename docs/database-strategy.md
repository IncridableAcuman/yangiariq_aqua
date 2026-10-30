# Database Strategy & Migrations

## 1. Engine & Configuration

- **Database**: PostgreSQL 16
- **Schema Management**: Liquibase (`classpath:db/changelog/db.changelog-master.xml`)
- **Isolation & Concurrency**: Read Committed with optimistic concurrency control via JPA `@Version`.
- **Primary Keys**: Universally Unique Identifiers (UUID v4) generated via `gen_random_uuid()` to prevent enumeration attacks and simplify distributed replication.

## 2. Changelog Hierarchy

All schema migrations are version-controlled inside `backend/src/main/resources/db/changelog/`:

```
db/changelog/
├── db.changelog-master.xml          # Root orchestrator
└── changes/
    ├── 001-initial-schema.sql       # Users, Fields, Irrigation, Problems, Notifications
    └── 002-initial-seed.sql         # Seed admin & operator accounts
```

## 3. Engineering Rules

1. **No Manual Production Mutations**: Direct execution of DDL on production or staging databases is prohibited. All DDL must pass code review as a Liquibase changeset.
2. **Reversibility / Rollback**: Where possible, changesets should include a rollback statement or be designed for forward-only compatibility.
3. **Auditing by Default**: Every domain entity inherits `BaseAuditableEntity`, ensuring automatic recording of `created_at`, `updated_at`, and optimistic lock `version`.
4. **Foreign Key Restraints**: Cascade rules must be explicit:
   - User account deletion cascades to personal field boundaries (`ON DELETE CASCADE`).
   - Incident reports preserve history; deleting a field sets `field_id` to null (`ON DELETE SET NULL`) while preserving the report description.
