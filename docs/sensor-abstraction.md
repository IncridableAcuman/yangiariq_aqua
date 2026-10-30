# IoT Sensor Abstraction & Simulation Strategy

## 1. Context & Problem Statement

In the initial MVP rollout for Yangiariq district, physical hardware sensors (e.g. capacitive soil moisture probes, ultrasonic canal flow meters) are not yet deployed in every field. However, building the platform without telemetry models would necessitate an expensive redesign once sensors are installed.

## 2. Architecture Solution

A clean domain port `SensorProvider` defines the contract for reading and querying telemetry data.

```java
public interface SensorProvider {
    String getProviderType();
    SensorReading getLatestReading(UUID sensorId);
    List<SensorReading> getReadingsForField(UUID fieldId);
    boolean isAvailable();
}
```

### Implementations

1. **`SimulatedSensorProvider` (MVP Implementation)**:
   - Emits realistic agronomic telemetry based on Yangiariq regional soil profiles (silt-loam, sandy loam) and seasonal Khorezm evapotranspiration rates.
   - Generates metrics:
     - Soil Moisture Percentage (`40% - 75%`)
     - Secondary canal flow rate (`liters / second`)
     - Battery health and signal strength
   - Enables full end-to-end frontend chart rendering, threshold alert triggering, and AI irrigation calculations without physical hardware.

2. **Future Hardware Adapters (`MqttSensorProvider`, `HttpSensorProvider`)**:
   - When field sensor gateways are provisioned, an MQTT or Webhook adapter will implement `SensorProvider` to ingest real-time sensor packets.
   - The application layer (`SensorService`, `AiService`, `DashboardService`) will consume the telemetry identically without modification.
