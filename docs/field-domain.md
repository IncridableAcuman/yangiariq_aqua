# Field Domain Blueprint & Data Architecture

## 1. Overview & Architectural Role

The **Field (`com.aquaai.field`)** module is the foundational domain context for agricultural parcel governance in **YANGIARIQ AQUA AI**. It establishes the data structures, domain invariants, ownership boundaries, and persistence mechanisms that underpin downstream water scheduling, incident tracking, sensor telemetry, and AI advisory workflows.

In adherence to Clean Architecture and the Modular Monolith blueprint, the `field` module strictly segregates domain business rules from infrastructure persistence, Spring Data JPA, and web delivery concerns.

```
com.aquaai.field/
├── domain/
│   ├── model/                  # Pure Domain Aggregate Root & Value Objects
│   │   ├── Field.java
│   │   ├── CropType.java
│   │   ├── SoilType.java
│   │   ├── IrrigationMethod.java
│   │   ├── FieldStatus.java
│   │   ├── FieldLocation.java
│   │   ├── FieldBoundary.java
│   │   └── CoordinatePoint.java
│   └── repository/             # Domain Repository Port (Clean Interface)
│       └── FieldRepository.java
├── application/                # Use Case Orchestration & Authorization
│   ├── FieldService.java
│   ├── FieldOwnershipChecker.java
│   └── dto/
│       ├── CreateFieldCommand.java
│       ├── UpdateFieldCommand.java
│       └── FieldResult.java
└── infrastructure/             # Persistence Adapter & ORM Entities
    └── persistence/
        ├── FieldJpaEntity.java
        ├── SpringDataFieldRepository.java
        ├── FieldJpaMapper.java
        └── FieldRepositoryImpl.java
```

---

## 2. Domain Concept & Identity

### User to Farmer to Field Relationship
In YANGIARIQ AQUA AI, farmers are represented directly by `User` accounts having `role = FARMER`. There is **no duplicate farmer table**. A Field parcel belongs to exactly one farmer (`owner_id` referencing `users.id`).

```
+--------------------+
|    User (FARMER)   |
|   id: UUID         |
+--------------------+
          | 1
          | owns
          | *
+--------------------+
|       Field        |
|   id: UUID         |
|   ownerId: UUID    |
+--------------------+
```

### Identifier Strategy
- **Field IDs** are Universally Unique Identifiers (`UUID v4`).
- Exposed as immutable domain identifiers, preventing enumeration and IDOR attacks.
- Supported in the database via `gen_random_uuid()` default generation.

---

## 3. Field Aggregate Model (`Field`)

The `Field` entity is the root of the Field Aggregate. It encapsulates state mutations and invariant enforcement:

| Property | Type | Constraints / Invariants |
| :--- | :--- | :--- |
| `id` | `UUID` | Immutable identifier |
| `ownerId` | `UUID` | Immutable foreign reference; must not be null |
| `name` | `String` | Non-blank, trimmed, max length 128 chars |
| `description` | `String` | Optional, max length 500 chars |
| `areaHectares` | `BigDecimal` | Mandatory, canonical unit, strictly `> 0` |
| `cropType` | `CropType` | Mandatory type-safe enum |
| `soilType` | `SoilType` | Mandatory type-safe enum |
| `irrigationMethod` | `IrrigationMethod` | Mandatory type-safe enum |
| `location` | `FieldLocation` | Optional center point; latitude `[-90, 90]`, longitude `[-180, 180]` |
| `boundary` | `FieldBoundary` | Optional GeoJSON RFC 7946 Polygon |
| `status` | `FieldStatus` | `ACTIVE` or `INACTIVE` (default: `ACTIVE`) |
| `createdAt` | `Instant` | Audited creation timestamp |
| `updatedAt` | `Instant` | Audited last modification timestamp |
| `version` | `Long` | Optimistic concurrency version |

### Aggregate Invariants
1. **Area Positive Requirement**: Field parcel area cannot be negative or zero.
2. **Mandatory Agricultural Types**: Crop, soil, and irrigation methods cannot be null.
3. **Owner Association**: Every field must have an explicit owner pointing to a registered user.
4. **Lifecycle Control**: Fields can transition between `ACTIVE` and `INACTIVE` states; hard deletion is avoided to protect historical irrigation records.

---

## 4. Agricultural Reference Classifications

### Crop Type (`CropType`)
Tailored to the Khorezm oasis and Aral Sea basin agricultural profile:
- `COTTON` (*Paxta* / *Хлопчатник*) - Dominant regional commercial crop with high evapotranspiration.
- `WHEAT` (*Bug'doy* / *Пшеница*) - Winter/spring grain crop.
- `RICE` (*Sholi* / *Рис*) - Intensive water basin crop common along the lower Amudarya.
- `MAIZE` (*Makkajo'xori* / *Кукуруза*) - Fodder and grain crop.
- `VEGETABLE` (*Sabzavotlar* / *Овощи*) - District horticulture crops.
- `MELON` (*Poliz ekinlari* / *Бахчевые*) - Khorezm melons and gourds.
- `OTHER` (*Boshqa* / *Другое*) - Extensible fallback.

### Soil Type (`SoilType`)
Represents soil hydrological properties influencing water infiltration and retention:
- `SANDY` (*Qumli*) - High drainage, low retention.
- `LOAM` (*Qumoq*) - Optimal moisture retention.
- `CLAY` (*Loyli*) - Heavy soil, slow infiltration, susceptible to waterlogging.
- `SANDY_LOAM` (*Qumloq*) - Moderate infiltration.
- `CLAY_LOAM` (*Og'ir qumoq*) - Heavy textured loam.
- `OTHER` (*Boshqa*) - Miscellaneous classifications.

### Irrigation Method (`IrrigationMethod`)
Represents the water delivery infrastructure:
- `CANAL` (*Kanal orqali*) - Direct gravity off-take from regional primary/secondary canals.
- `DITCH` (*Ariq orqali*) - Internal tertiary ditch distribution.
- `FURROW` (*Egatlab sug'orish*) - Traditional surface furrow irrigation.
- `DRIP` (*Tomchilatib sug'orish*) - Water-saving micro-irrigation.
- `SPRINKLER` (*Yomg'irlatib sug'orish*) - Overhead mechanical application.
- `OTHER` (*Boshqa*) - Alternative methods.

### Enum Persistence Decision
All reference types are persisted as PostgreSQL `VARCHAR(32)` strings (e.g. `'COTTON'`, `'LOAM'`, `'CANAL'`) rather than PostgreSQL native enum types (`CREATE TYPE`).
- **Rationale**: PostgreSQL native enum types require explicit transactional DDL (`ALTER TYPE ADD VALUE`) for schema migrations and introduce compatibility complications with replication, backup tools, and test environments. Application-controlled `VARCHAR(32)` provides clean future extensibility while maintaining database flexibility.

---

## 5. Area & Coordinate Models

### Canonical Measurement Unit: Hectares
All surface areas are captured and stored in **Hectares (`ha`)** as `BigDecimal` (`NUMERIC(10, 4)`).
- Eliminates floating-point rounding errors.
- Prohibits implicit conversions throughout the domain (no square meters or acres conversion pollution).

### Field Location (`FieldLocation`)
Represents the representative centroid or entrance point:
- `latitude`: `NUMERIC(9, 6)` bounded to `[-90.0, 90.0]`.
- `longitude`: `NUMERIC(9, 6)` bounded to `[-180.0, 180.0]`.
- Precision: 6 decimal places yields ~0.11 meters precision, ideal for parcel localization.

---

## 6. Field Boundary & GeoJSON Strategy

### Boundary Representation (`FieldBoundary`)
Stored as PostgreSQL `JSONB` adhering to the **GeoJSON RFC 7946** Polygon specification:

```json
{
  "type": "Polygon",
  "coordinates": [
    [
      [60.5900, 41.4500],
      [60.5950, 41.4500],
      [60.5950, 41.4550],
      [60.5900, 41.4550],
      [60.5900, 41.4500]
    ]
  ]
}
```

> [!IMPORTANT]
> In GeoJSON, coordinates are ordered `[longitude, latitude]` (X, Y), opposite to colloquial `(lat, lon)` order.

### Structural Validation
The `FieldBoundary` Value Object validates:
1. `type` must strictly equal `"Polygon"`.
2. Exterior ring must contain at least 4 coordinate pairs.
3. The exterior ring must be closed: the first point must match the last point within `1e-7` tolerance.
4. Longitude values must reside in `[-180.0, 180.0]`, latitude in `[-90.0, 90.0]`.

### PostGIS Tradeoff Analysis
- **Decision**: PostGIS is intentionally deferred to later GIS milestones.
- **Rationale**: For MVP parcel display on web maps (Leaflet / Mapbox), GeoJSON stored in `JSONB` fulfills all rendering and basic serialization requirements without introducing PostGIS extensions, GDAL dependencies, or heavyweight geometry engines.
- **Future Migration Path**: When spatial indexing (R-Tree / GIST), spatial containment queries (`ST_Contains`), or distance calculations are required, a Liquibase migration can add a generated geometry column:
  ```sql
  ALTER TABLE fields ADD COLUMN geom geometry(Polygon, 4326)
    GENERATED ALWAYS AS (ST_GeomFromGeoJSON(boundary)) STORED;
  CREATE INDEX idx_fields_geom ON fields USING GIST(geom);
  ```

---

## 7. Field Ownership & Multi-Tenant Authorization

### Ownership Model
A field belongs to exactly one farmer (`owner_id`). Client requests must never be trusted to declare ownership.

### `FieldOwnershipChecker`
To provide centralized, reusable authorization across the monolith, `FieldOwnershipChecker` encapsulates ownership evaluations:
- `verifyOwnership(UUID fieldId, UUID userId)`: Throws `ResourceNotFoundException` if the field does not exist; throws `UnauthorizedOperationException` if `userId` is not the owner.
- `verifyOwnershipOrRole(UUID fieldId, UUID userId, UserRole role, UserRole... overrides)`: Permits `ADMIN` or `OPERATOR` roles to govern fields across farmers while protecting farmer privacy against other farmers.
- Reusable across future `water`, `problem`, `sensor`, and `ai` modules.

### Current User Provider (`CurrentUserProvider`)
Decouples business code from static Spring Security context calls (`SecurityContextHolder`). Application services receive the authenticated user identity via `CurrentUserProvider`.

---

## 8. Field Name Uniqueness Business Rule

A farmer should not have two active fields with the identical name:
- **Scope**: Owner-scoped (Farmer A and Farmer B can both have `"Bog' 1"`).
- **Status Sensitivity**: Deactivated (`INACTIVE`) fields do not block creating an `ACTIVE` field with that name.
- **Enforcement Mechanism**:
  1. **Application Level**: `FieldService` queries `fieldRepository.existsByOwnerIdAndNameIgnoreCaseAndStatus(ownerId, name, FieldStatus.ACTIVE)` before creation or activation.
  2. **Database Level (PostgreSQL)**: Enforced via partial unique index:
     ```sql
     CREATE UNIQUE INDEX idx_fields_owner_active_name
     ON fields(owner_id, LOWER(name))
     WHERE status = 'ACTIVE';
     ```

---

## 9. Database Design & Liquibase Migration

Migration file: `backend/src/main/resources/db/changelog/changes/004-create-fields-table.sql`

```sql
CREATE TABLE IF NOT EXISTS fields (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    owner_id UUID NOT NULL,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(500),
    area_hectares NUMERIC(10, 4) NOT NULL,
    crop_type VARCHAR(32) NOT NULL,
    soil_type VARCHAR(32) NOT NULL,
    irrigation_method VARCHAR(32) NOT NULL,
    latitude NUMERIC(9, 6),
    longitude NUMERIC(9, 6),
    boundary JSONB,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    CONSTRAINT fk_fields_owner FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_field_area_positive CHECK (area_hectares > 0),
    CONSTRAINT chk_field_latitude CHECK (latitude IS NULL OR (latitude >= -90.0 AND latitude <= 90.0)),
    CONSTRAINT chk_field_longitude CHECK (longitude IS NULL OR (longitude >= -180.0 AND longitude <= 180.0))
);

CREATE INDEX IF NOT EXISTS idx_fields_owner_id ON fields(owner_id);
CREATE INDEX IF NOT EXISTS idx_fields_status ON fields(status);
CREATE INDEX IF NOT EXISTS idx_fields_crop_type ON fields(crop_type);
CREATE INDEX IF NOT EXISTS idx_fields_owner_status ON fields(owner_id, status);

CREATE UNIQUE INDEX IF NOT EXISTS idx_fields_owner_active_name
ON fields(owner_id, LOWER(name)) WHERE status = 'ACTIVE';
```

### Foreign Key & Referential Integrity
- `fk_fields_owner` links `fields.owner_id` to `users.id`.
- `ON DELETE CASCADE`: If a user account is deleted, their associated field parcel records are removed automatically.
- No redundant user details (`first_name`, `phone_number`) are duplicated into `fields`.
