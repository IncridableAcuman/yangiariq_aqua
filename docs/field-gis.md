# Field GIS & Map Visualization Architecture (Milestone 05.5)

## 1. Executive Summary & Geospatial Vision
In **YANGIARIQ AQUA AI**, the **GIS / Map Field Visualization** subsystem provides agricultural producers, water operators, and platform administrators with geospatial visibility over irrigated land parcels across the Yangiariq district (Khorezm region).

The GIS subsystem enables:
1. **Interactive Cartography**: High-performance OpenStreetMap (OSM) tile rendering without proprietary API keys or external token dependencies.
2. **Centroid Coordinates Resolution**: Intuitive point-and-click or marker drag positioning within Uzbekistan geographic bounding limits.
3. **GeoJSON Boundary Polygon Management**: Drawing, vertex manipulation, and validation of field perimeter contours conforming strictly to RFC 7946.
4. **Agronomic Area Estimation**: Real-time geodesic spherical polygon area computation assisting farmers while maintaining authoritative registered area integrity.

---

## 2. Geospatial Data Model & RFC 7946 Conventions

### 2.1 GeoJSON Canonical Structure
All field boundaries are modeled as standard GeoJSON `Polygon` value objects:

```json
{
  "type": "Polygon",
  "coordinates": [
    [
      [60.510000, 41.410000],
      [60.520000, 41.410000],
      [60.520000, 41.420000],
      [60.510000, 41.420000],
      [60.510000, 41.410000]
    ]
  ]
}
```

### 2.2 Crucial Coordinate Order Invariant: [Longitude, Latitude]
- **GeoJSON Standard (RFC 7946)** mandates `[X, Y]` which maps to `[longitude, latitude]`.
- **Leaflet Library API** expects `[Y, X]` which maps to `[latitude, longitude]`.
- **Conversion Utility**:
  - [`geoJsonToLeafletLatLngs(boundary)`](file:///home/izzatbek/Desktop/agronexsus/frontend/src/features/field/gis/gis.utils.ts#L41): Converts `[lon, lat]` into Leaflet `[lat, lon]`.
  - [`leafletLatLngsToGeoJson(points)`](file:///home/izzatbek/Desktop/agronexsus/frontend/src/features/field/gis/gis.utils.ts#L56): Converts `{ lat, lng }` into GeoJSON `[lng, lat]` and automatically enforces polygon closure.

---

## 3. Geographic Boundaries & Validation Rules

Field coordinates and perimeters are strictly validated on both client (Zod / GIS utilities) and backend (`FieldBoundary.java`):

| Rule | Specification | Behavior if Violated |
| :--- | :--- | :--- |
| **Uzbekistan Latitude Bounds** | `37.0°N` to `45.6°N` | Point is rejected with localized error. |
| **Uzbekistan Longitude Bounds** | `56.0°E` to `73.2°E` | Point is rejected with localized error. |
| **Minimum Vertex Count** | At least 4 coordinates (3 unique vertices + 1 closing point) | Form/API rejects with invariant exception. |
| **Polygon Closure** | First point equals last point (`\|P_1 - P_n\| < 1e-6`) | Automatically closed on client; verified by backend. |
| **Yangiariq District Centroid** | Default map center: `41.3414°N, 60.6094°E` | Used as initial viewport when no parcel coordinates exist. |

---

## 4. Frontend GIS Architecture & Components

Located in [`src/features/field/gis/`](file:///home/izzatbek/Desktop/agronexsus/frontend/src/features/field/gis):

```
frontend/src/features/field/gis/
├── gis.types.ts                  # LatLngPoint, GisValidationResult, FieldMapProps
├── gis.utils.ts                  # Transformations, area estimation, bounds checks
├── FieldMap.tsx                  # Interactive Leaflet viewer with OSM tiles & status styling
├── FieldLocationPickerModal.tsx  # Interactive map modal for centroid coordinate picking
├── FieldBoundaryEditorModal.tsx  # Drawing canvas with vertex handles & real-time area calculation
└── index.ts                      # Barrel exports
```

### 4.1 FieldMap Viewer ([`FieldMap.tsx`](file:///home/izzatbek/Desktop/agronexsus/frontend/src/features/field/gis/FieldMap.tsx))
- Renders directly in [`FieldDetailsModal`](file:///home/izzatbek/Desktop/agronexsus/frontend/src/features/field/components/FieldDetailsModal.tsx).
- Dynamically calls `map.fitBounds(polygon.getBounds())` when a boundary exists.
- Semantic Status Colors:
  - `ACTIVE`: Emerald green perimeter (`#059669`) and translucent fill.
  - `INACTIVE`: Slate/gray perimeter (`#6b7280`) and translucent fill.
- Popup card displaying Field Name, Crop Type, Area (ha), and Status.

### 4.2 Location Picker ([`FieldLocationPickerModal.tsx`](file:///home/izzatbek/Desktop/agronexsus/frontend/src/features/field/gis/FieldLocationPickerModal.tsx))
- Invoked from [`FieldFormDialog`](file:///home/izzatbek/Desktop/agronexsus/frontend/src/features/field/components/FieldFormDialog.tsx) via `[ Xaritada tanlash ]`.
- User clicks on map or drags the custom pin; updates form state with 6-decimal precision coordinates.

### 4.3 Boundary Editor & Drawing Canvas ([`FieldBoundaryEditorModal.tsx`](file:///home/izzatbek/Desktop/agronexsus/frontend/src/features/field/gis/FieldBoundaryEditorModal.tsx))
- **Drawing Mode**: Real-time dotted polyline connects clicked vertices. User clicks the first vertex or "Yakunlash" (Finish) to close the loop.
- **Editing Mode**: Renders draggable circular handles on every vertex, allowing interactive contour refinement.
- **Area Consistency Warning**: Computes estimated surface area in hectares. If difference between estimated area and registered `areaHectares` exceeds 35%, displays an advisory banner while preserving the authoritative registered area.

---

## 5. Security & Ownership Enforcement
1. **Server-Side Authority**: The frontend never assigns `ownerId`. Coordinates and boundaries submitted via `POST /api/v1/fields` or `PATCH /api/v1/fields/{id}` are assigned strictly to the authenticated farmer's parcel.
2. **Role Restrictions**:
   - `FARMER`: Can pick location and draw/edit boundary for own parcels.
   - `OPERATOR`: Has read-only map visibility across all district parcels for canal management. Mutation actions are hidden.
   - `ADMIN`: Has platform override authority to correct parcel boundaries.

---

## 6. Future PostGIS Strategy (Post-MVP Roadmap)

### Current Architecture: JSONB GeoJSON
In the current monolithic milestone, boundaries are stored as GeoJSON in PostgreSQL `JSONB` columns (`fields.boundary`). This provides:
- Zero extra container dependencies.
- Native JSON querying and schema agility.
- High performance for parcel-level lookups and CRUD.

### PostGIS Transition Trigger Points
A dedicated database migration to PostGIS geometry/geography types (`GEOMETRY(Polygon, 4326)`) will be introduced when the following domain capabilities are required:
1. **Canal Proximity Queries**: Spatial indexing to identify all parcels within $X$ meters of an irrigation branch.
2. **District Spatial Clustering**: District-wide spatial joins for aggregate water consumption heatmaps.
3. **Automated Overlap Detection**: ST_Intersects and ST_Overlaps for spatial conflict resolution across tenant boundaries.
4. **Satellite Raster Overlay**: Multi-spectral imagery alignment and NDVI computation.

---

## 7. Known Limitations

- **Internet Tile Dependency**: OpenStreetMap tiles require network connectivity. A vector tile caching worker will be evaluated in Milestone 07 for offline mobile field operation.
- **Topological Self-Intersection**: Complex self-intersecting "figure-8" polygons are flagged on the backend; full client-side Delaunay triangulation / planar validation is deferred until GIS advanced modules.
- **No Satellite Imagery Layer**: Aerial imagery is not bundled; standard OSM cartography is utilized.
