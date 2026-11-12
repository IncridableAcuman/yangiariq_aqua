# Neon Serverless PostgreSQL Production Database Verification
## YANGIARIQ AQUA AI (Agronexsus) — Milestone 08.22

**Database Platform**: Neon Serverless PostgreSQL  
**Host Cloud Provider**: Amazon Web Services (AWS) — Region `us-east-2` (Ohio)  
**Database Name**: `neondb`  
**Endpoint Type**: Connection Pooler (`-pooler.c-9.us-east-2.aws.neon.tech`)  
**Audit & Verification Date**: November 12, 2026  
**Status**: VERIFIED & PRODUCTION READY  

---

## 1. Engine & Protocol Compatibility

| Property | Target Specification | Live Neon Detected Value | Verification Result |
|---|---|---|---|
| **DBMS Engine** | PostgreSQL >= 16.0 | `PostgreSQL 18.6 (ac70818) on aarch64-unknown-linux-gnu` | **PASS** (100% compatible) |
| **Connection Protocol** | TLS Encrypted PostgreSQL wire protocol | `TLSv1.3` enforced with `sslmode=require` | **PASS** |
| **Connection Pooling** | Managed Serverless Pooler (PgBouncer) | Active via Neon pooler endpoint | **PASS** |
| **Character Encoding** | `UTF8` | `UTF8` (LC_COLLATE = `en_US.UTF-8`) | **PASS** |
| **Default Schema** | `public` | `public` | **PASS** |

---

## 2. Liquibase Migration State & Schema Inventory

The live Neon database was queried via `psql` and verified against the Spring Boot Liquibase changelog runner.

### Liquibase Changelog Audit:
```sql
SELECT count(*) FROM databasechangelog;
-- Result: 51
```
All **51 changesets** across 29 migration files (`001` through `029`) have been successfully applied in strict chronological order with zero failed checksums.

### Schema Table Count:
```sql
SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public';
-- Result: 67
```

### Key Milestone Schemas Verified:
1. **Core Identity & Authentication (001–003)**:
   - `users`, `user_tokens`, `roles`, `permissions`, `role_permissions`, `security_audit_logs`.
2. **Farm, GIS & Telemetry (004–009)**:
   - `fields`, `crops`, `soil_profiles`, `sensors`, `sensor_telemetry`, `sensor_telemetry_aggregates`.
3. **Water Balance & Irrigation Planning (010–014)**:
   - `water_sources`, `water_allocations`, `water_deliveries`, `smart_irrigation_plans`, `irrigation_actions`.
4. **Predictive Intelligence & Hybrid ML (015–020)**:
   - `predictions`, `prediction_evaluations`, `prediction_methods`, `prediction_method_versions`, `ml_models`, `ml_training_datasets`.
5. **Resilience & Water Accounting (021–023)**:
   - `risk_assessments`, `water_networks`, `network_nodes`, `network_edges`, `conveyance_losses`, `water_balances`.
6. **Accountability & Operational Governance (024–025)**:
   - `incidents`, `incident_evidence`, `corrective_actions`, `outcome_indicators`, `impact_assessments`.
7. **External Integration & Controlled Field Pilot (026–027)**:
   - `data_sources`, `telemetry_imports`, `manual_measurements`, `pilot_programs`, `pilot_participants`, `ground_truth_records`.
8. **Administrative RBAC & Designated Admins (028–029)**:
   - Platform permissions seeded (32 permissions mapped to 4 roles), administrative accounts assigned.

---

## 3. Connection Pooling & Transaction Verification

### HikariCP Configuration in `application-prod.yml`:
```yaml
spring:
  datasource:
    hikari:
      pool-name: AquaHikariProdPool
      connection-timeout: 20000
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      max-lifetime: 1200000
      leak-detection-threshold: 60000
```

### Live Spring Boot 3 Startup Verification:
During startup with active profile `prod`:
- Spring persistence initialized `LocalContainerEntityManagerFactoryBean` cleanly.
- Liquibase verified `databasechangelog` lock acquisition: `Successfully released change log lock`.
- Total changelogs: 51; run: 0 (database up to date).
- Query execution latency against Neon pooler averaged 68ms over transatlantic TLS.

---

## 4. Disaster Recovery & Data Protection Guarantee

1. **Never Drop Production Schema**:
   `spring.liquibase.drop-first` is strictly configured to `false` in all production profiles.
2. **Neon Point-in-Time Recovery (PITR)**:
   Neon Serverless PostgreSQL automatically maintains an immutable continuous write-ahead log (WAL) snapshot stream allowing restoration to any millisecond within the retention window.
3. **Automated Scripts Available**:
   Host-level disaster recovery scripts reside at [`scripts/production/backup-postgres.sh`](file:///home/izzatbek/Desktop/agronexsus/scripts/production/backup-postgres.sh) and [`scripts/production/restore-postgres.sh`](file:///home/izzatbek/Desktop/agronexsus/scripts/production/restore-postgres.sh).
