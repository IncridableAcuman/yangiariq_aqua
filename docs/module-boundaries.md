# Module Boundaries & Ownership

In Yangiariq Aqua AI, every backend module has a strictly bounded context. Modules must never bypass public contracts or directly manipulate database tables belonging to other modules.

| Module | Core Responsibilities | Owned Entities | External Ports / Dependencies |
| :--- | :--- | :--- | :--- |
| **`auth`** | Token issuing, login, registration, password hashing | - | Interacts with `user` via `UserRepository` or `UserService` |
| **`user`** | Farmer profiles, operator credentials, roles | `User` | Shared security context |
| **`field`** | Parcels, crop types, soil characteristics, boundaries | `Field` | References `User` (farmerId) |
| **`water`** | Irrigation logs, water delivery, volumetric intake | `IrrigationRecord` | References `Field` (fieldId), `User` (recordedBy) |
| **`problem`**| Canal blockage, water shortage, leak reports, status | `ProblemReport` | References `User` (reportedBy), `Field` (optional fieldId) |
| **`ai`** | Recommendation orchestration, prompt generation | - | Defines `AiProvider`, depends on AI adapter |
| **`sensor`** | Ingestion, telemetry values, mock/real sensors | `SensorReading` | Defines `SensorProvider`, depends on simulated/hardware adapter |
| **`dashboard`**| Read-model aggregation, high-level metrics | - | Read-only queries across `user`, `field`, `problem` |
| **`notification`**| User alerts, push notifications, read states | `Notification` | References `User` (recipientId) |
| **`shared`** | Exception handlers, JWT filter, base entity, pagination | `BaseAuditableEntity` | Injected across all modules |

### Inter-Module Communication Rules
1. **Synchronous Internal Calls**: Modules invoke Application Service interfaces of another module if needed.
2. **Data Aggregations**: The `dashboard` module is explicitly designated as a read-model aggregator; it is allowed to query module repositories in read-only mode to prevent write-service pollution.
3. **Future Microservice Transition**: Modules communicate strictly using UUIDs rather than direct object references (`@ManyToOne` across different module entities is intentionally avoided). This allows any module to be moved to an external microservice without breaking database integrity.
