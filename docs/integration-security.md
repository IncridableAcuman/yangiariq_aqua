# Integration Security, Access Control & Safeguards

## 1. Overview
The external data integration foundation handles data from untrusted network endpoints, external APIs, and user uploads. Milestone 08.19 implements multi-layered security controls to prevent credential leaks, SSRF attacks, injection vulnerabilities, and unauthorized data tampering.

## 2. Credential Security & Secret Protection
- **Zero Plain-Text Secrets in Database**: API tokens, private keys, or passwords are never stored in `data_sources.configuration_json` or any database table.
- **Environment Variable Binding**: External provider keys (such as `AQUAAI_WEATHER_API_KEY`) are bound exclusively via Spring Boot configuration properties (`aquaai.weather.api-key`).
- **Response Sanitization**: Controller DTOs (`DataSourceResponse`) scrub any sensitive tokens before emitting JSON payloads to clients.
- **Log Masking**: System logs never output authentication headers, Bearer tokens, or raw credential strings.

## 3. Server-Side Request Forgery (SSRF) Protection
In `ExternalWeatherClient`, before initiating outbound HTTP connections, target URLs undergo strict network resolution checks:
- Rejects loopback addresses (`127.0.0.1`, `localhost`, `::1`).
- Rejects RFC 1918 private subnets (`10.0.0.0/8`, `172.16.0.0/12`, `192.168.0.0/16`).
- Rejects cloud instance metadata endpoints (`169.254.169.254`).
- Restricts scheme to `https` (or `http` for explicitly configured test mock servers).

## 4. Role-Based Access Control (RBAC) Matrix

| Operation | Endpoint | ADMIN | OPERATOR | AGRONOMIST | FARMER |
| :--- | :--- | :---: | :---: | :---: | :---: |
| List Data Sources | `GET /api/v1/data-sources` | Yes | Yes | Yes | No |
| Register Source | `POST /api/v1/data-sources` | Yes | Yes | No | No |
| Update Source | `PUT /api/v1/data-sources/{id}` | Yes | Yes | No | No |
| Toggle Status | `POST /api/v1/data-sources/{id}/toggle-status` | Yes | Yes | No | No |
| Trigger Manual Sync| `POST /api/v1/data-sources/{id}/sync` | Yes | Yes | No | No |
| Ingest Telemetry | `POST /api/v1/data-ingestion/telemetry` | Yes | Yes | Yes | No |
| Manual Reading | `POST /api/v1/data-ingestion/manual` | Yes | Yes | Yes | Yes (unverified) |
| CSV Batch Import | `POST /api/v1/data-imports/csv` | Yes | Yes | Yes | No |
| View Quality & Provenance | `GET /api/v1/data-quality/**` | Yes | Yes | Yes | Yes |

## 5. Input Validation & Denial-of-Service Defense
- **Payload Limits**: Ingestion batch endpoints limit payload sizes to 500 records per transaction.
- **File Upload Limits**: Multipart CSV imports are capped at 10 MB.
- **Rate Limiting & Bounded Retries**: External weather sync utilizes a token bucket / rate limiter (60 calls/min) with a maximum of 3 retries and exponential backoff.
- **Timeout Isolation**: HTTP connection timeout of 3,000ms and read timeout of 5,000ms guarantee external network lags cannot lock Spring servlet threads.
- **No Physical Actuation Safeguard**: The integration module contains strictly unidirectional read/ingest mechanisms. No actuators, relays, pumps, or canal gates are reachable through any integration API.
