# REST API Conventions & Standards

## 1. URI & Versioning

- Base URL pattern: `/api/v1/{resource}`
- Plural nouns for resource endpoints (e.g. `/api/v1/fields`, `/api/v1/irrigation-records`, `/api/v1/problem-reports`).
- Explicit Sub-resources: `/api/v1/fields/{fieldId}/sensors`

## 2. Standardized Envelope: `ApiResponse<T>`

All endpoints respond with the structured `ApiResponse` envelope:

```json
{
  "success": true,
  "data": {
    "id": "e2a07c91-95dc-42a0-ba12-f04c6e3b567d",
    "name": "Yangiariq Plot A-12",
    "cropType": "Cotton",
    "areaHectares": 12.50
  },
  "timestamp": "2026-10-30T10:15:30.125Z",
  "correlationId": "8b8fcff7-66a9-4e78-9e19-0f2b380dbd63"
}
```

In the event of an error:

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed for request",
    "details": [
      "areaHectares: must be greater than 0",
      "name: cannot be blank"
    ]
  },
  "timestamp": "2026-10-30T10:15:30.125Z",
  "correlationId": "8b8fcff7-66a9-4e78-9e19-0f2b380dbd63"
}
```

## 3. HTTP Status Codes
- `200 OK`: Successful read, update, or action.
- `201 Created`: Resource successfully created.
- `204 No Content`: Successful deletion.
- `400 Bad Request`: Syntax error or request validation failure.
- `401 Unauthorized`: Missing or invalid JWT authentication.
- `403 Forbidden`: Authenticated user lacks permission.
- `404 Not Found`: Resource does not exist.
- `422 Unprocessable Entity`: Business invariant violated.
- `500 Internal Server Error`: Unhandled server condition (details masked from client).

## 4. Pagination
Endpoints returning list collections accept standard query parameters:
`GET /api/v1/fields?page=0&size=20&sort=createdAt&direction=desc`

Returns `ApiResponse<PageResponse<T>>` containing metadata: `totalElements`, `totalPages`, `page`, `size`, `first`, `last`.
