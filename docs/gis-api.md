# GIS Water Intelligence API Specification (Milestone 06.5)

## 1. Overview
The GIS API provides a high-performance, unified spatial read model for agricultural parcels, irrigation water sources, volumetric quotas, and actual water consumption across Yangiariq district.

- **Base Path**: `/api/v1/gis`
- **Security**: Bearer JWT required on all endpoints.
- **Tenant Scope**: Filtered automatically by caller credentials (Farmers see own parcels; Operators and Admins see district-wide records).

---

## 2. Endpoints

### 2.1 Get GIS Overview

Retrieve the complete spatial intelligence graph including parcels, water sources, aggregated volumetric balances, and infrastructure links for a specified observation period.

- **Method**: `GET`
- **URL**: `/api/v1/gis/overview`
- **Role Permissions**: `FARMER`, `OPERATOR`, `ADMIN`

#### Request Query Parameters

| Parameter | Type | Required | Description | Example |
| :--- | :--- | :--- | :--- | :--- |
| `from` | ISO-8601 Instant | Optional | Start timestamp of observation period (defaults to active season) | `2026-05-01T00:00:00Z` |
| `to` | ISO-8601 Instant | Optional | End timestamp of observation period (defaults to now) | `2026-10-31T23:59:59Z` |

#### Validation Constraints
- If both `from` and `to` are supplied, `from` must be less than or equal to `to`.
- Violations return `422 Unprocessable Entity` with localized error messages.

#### Response: `200 OK`

```json
{
  "fields": [
    {
      "id": "c1f7a810-72c2-4e9b-b530-9b33a5fa9701",
      "ownerId": "f04128b7-6b6c-4861-a083-d08e1a129ef3",
      "name": "Markaziy Paxtazor",
      "areaHectares": 12.50,
      "cropType": "COTTON",
      "soilType": "LOAM",
      "irrigationMethod": "CANAL",
      "latitude": 41.341400,
      "longitude": 60.609400,
      "boundary": {
        "type": "Polygon",
        "coordinates": [
          [
            [60.608000, 41.340000],
            [60.612000, 41.340000],
            [60.612000, 41.343000],
            [60.608000, 41.343000],
            [60.608000, 41.340000]
          ]
        ]
      },
      "status": "ACTIVE"
    }
  ],
  "waterSources": [
    {
      "id": "e45bbd09-b7b5-4b53-b09e-71649fcf9581",
      "name": "Shovot Magistral Kanali",
      "type": "CANAL",
      "description": "Yangiariq tumanining asosiy suv taʼminoti tarmogʻi",
      "status": "ACTIVE",
      "latitude": 41.350200,
      "longitude": 60.621000
    }
  ],
  "waterStatuses": [
    {
      "fieldId": "c1f7a810-72c2-4e9b-b530-9b33a5fa9701",
      "totalAllocatedVolumeM3": 12500.0,
      "totalConsumedVolumeM3": 13800.0,
      "totalRemainingVolumeM3": 0.0,
      "totalOverConsumedVolumeM3": 1300.0,
      "isOverConsumed": true,
      "consumptionPercentage": 110.4,
      "category": "OVER_CONSUMED",
      "allocationCount": 1,
      "irrigationEventCount": 4
    }
  ],
  "allocations": [
    {
      "id": "a90bb782-b7b5-4b53-b09e-71649fcf9581",
      "fieldId": "c1f7a810-72c2-4e9b-b530-9b33a5fa9701",
      "waterSourceId": "e45bbd09-b7b5-4b53-b09e-71649fcf9581",
      "allocatedVolumeM3": 12500.0,
      "status": "ACTIVE"
    }
  ]
}
```

---

## 3. Water Status Category Derivation

The field water status is deterministically categorized by [`WaterStatusCategory`](file:///home/izzatbek/Desktop/agronexsus/backend/src/main/java/com/aquaai/gis/api/dto/WaterStatusCategory.java):

```java
public enum WaterStatusCategory {
    NORMAL,        // Consumed <= 80% of allocated volume
    WARNING,       // Consumed > 80% and <= 100% of allocated volume
    OVER_CONSUMED, // Consumed > 100% of allocated volume (exceeded quota)
    NO_ALLOCATION  // Total allocated volume is zero or null
}
```

---

## 4. Error Responses

### 4.1 401 Unauthorized
Occurs when request lacks a valid JWT token.

```json
{
  "status": 401,
  "message": "Full authentication is required to access this resource"
}
```

### 4.2 422 Unprocessable Entity
Occurs when query parameters fail business validation (e.g. `from > to`).

```json
{
  "timestamp": "2026-11-01T07:40:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Start date must be before or equal to end date"
}
```
