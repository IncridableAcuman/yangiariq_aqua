# Real Sensor Data Connectivity & Gateway Architecture

## 1. Overview
The platform supports real-world IoT sensor connectivity through the `SensorProvider` SPI, extended in Milestone 08.19 with the `HttpSensorGatewayProvider`.

## 2. Sensor Provider Implementations

| Provider | Provider Name | Default Status | Evidence Classification | Hardware Present |
| :--- | :--- | :--- | :--- | :--- |
| `SimulatedSensorProvider` | `SimulatedSensorProvider` | `ACTIVE` | `SIMULATED` | No (Simulated edge math) |
| `HttpSensorGatewayProvider`| `HttpSensorGatewayProvider`| `AWAITING_HARDWARE` | `OBSERVED` | No (Contract ready for physical deployment) |

## 3. Hardware Truthfulness & Status Policy
- **No Physical Devices Currently Deployed**: Yangiariq water district fields currently lack physical automated digital gateway hardware.
- **Strict Prohibition Against Fabricated Telemetry**: The platform does **not** simulate fake IoT gateway responses or claim real sensor observations are streaming when hardware is absent.
- **Explicit Status**: `HttpSensorGatewayProvider` is explicitly assigned `DataSourceStatus.AWAITING_HARDWARE`.
- **Primary Bean Annotation**: `SimulatedSensorProvider` is designated as `@Primary` to allow uninterrupted synthetic testing while preventing ambiguous Spring dependency injection.

## 4. HTTP Gateway Specification
When physical edge concentrators or LoRaWAN gateways are deployed:
- **Endpoint**: `POST /api/v1/data-ingestion/telemetry`
- **Authentication**: Bearer JWT token issued to a dedicated `SENSOR_GATEWAY` service account or pre-shared HMAC gateway key in `X-Gateway-Signature`.
- **Payload Schema**:
```json
{
  "sourceCode": "HTTP_SENSOR_GATEWAY",
  "measurements": [
    {
      "fieldId": "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d",
      "sensorType": "SOIL_MOISTURE",
      "value": 48.5,
      "unit": "%",
      "measuredAt": "2026-11-09T08:00:00Z",
      "externalRecordId": "GW-KHZ-01-SN-8821"
    }
  ]
}
```
- **Responses**:
  - `201 Created`: All readings accepted and persisted.
  - `207 Multi-Status` / `Partial`: Duplicate or out-of-range rows reported individually with validation error messages.
  - `422 Unprocessable Content`: Unconfigured or inactive gateway source code.

## 5. MQTT Evaluation
An MQTT broker (e.g., Mosquitto or EMQX) was evaluated during the Phase 0 audit. Because no physical field devices are currently in place, introducing an external MQTT broker daemon would create unnecessary operational overhead without real hardware producers. The `HttpSensorGatewayProvider` contract provides immediate compatibility with HTTP-to-MQTT edge gateways (such as ThingsBoard, ChirpStack, or Teltonika) whenever field hardware is installed.
