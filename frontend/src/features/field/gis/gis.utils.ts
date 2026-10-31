import { FieldBoundary } from '@/entities/field'
import { LatLngPoint, GisValidationResult } from './gis.types'

export const UZBEKISTAN_BOUNDS = {
  minLat: 37.0,
  maxLat: 45.6,
  minLng: 56.0,
  maxLng: 73.2,
} as const

// Yangiariq district center coordinates (Khorezm region)
export const YANGIARIQ_DEFAULT_CENTER: LatLngPoint = {
  lat: 41.3414,
  lng: 60.6094,
}

const EPSILON = 1e-6
const EARTH_RADIUS_METERS = 6378137

/**
 * Validates whether the given coordinate point falls within the territory of Uzbekistan.
 */
export function isInsideUzbekistan(lat: number, lng: number): boolean {
  if (typeof lat !== 'number' || typeof lng !== 'number') return false
  if (isNaN(lat) || isNaN(lng) || !isFinite(lat) || !isFinite(lng)) return false

  return (
    lat >= UZBEKISTAN_BOUNDS.minLat &&
    lat <= UZBEKISTAN_BOUNDS.maxLat &&
    lng >= UZBEKISTAN_BOUNDS.minLng &&
    lng <= UZBEKISTAN_BOUNDS.maxLng
  )
}

/**
 * Converts a GeoJSON Polygon boundary (which uses [longitude, latitude] order)
 * into a Leaflet-compatible array of [latitude, longitude] pairs.
 */
export function geoJsonToLeafletLatLngs(
  boundary?: FieldBoundary | null
): [number, number][] {
  if (!boundary || !boundary.coordinates || !boundary.coordinates[0]) {
    return []
  }

  const exteriorRing = boundary.coordinates[0]
  return exteriorRing.map(([lon, lat]) => [lat, lon])
}

/**
 * Converts an array of Leaflet { lat, lng } points into a GeoJSON Polygon FieldBoundary.
 * Ensures the exterior ring is properly closed (first point equals last point)
 * and stored in canonical GeoJSON [longitude, latitude] order.
 */
export function leafletLatLngsToGeoJson(points: LatLngPoint[]): FieldBoundary {
  if (points.length < 3) {
    throw new Error('At least 3 distinct vertices are required to define a boundary')
  }

  const ring: number[][] = points.map((p) => [p.lng, p.lat])

  // Ensure closure
  const first = points[0]
  const last = points[points.length - 1]
  const isClosed =
    Math.abs(first.lat - last.lat) < EPSILON &&
    Math.abs(first.lng - last.lng) < EPSILON

  if (!isClosed) {
    ring.push([first.lng, first.lat])
  }

  return {
    type: 'Polygon',
    coordinates: [ring],
  }
}

/**
 * Validates a GeoJSON Polygon boundary against domain invariants:
 * - Must have type 'Polygon'
 * - Exterior ring must have at least 4 coordinates (3 unique points + closing point)
 * - First point must match last point (closed loop)
 * - All coordinates must lie within Uzbekistan bounding box
 */
export function validateBoundary(
  boundary?: FieldBoundary | null
): GisValidationResult {
  if (!boundary) {
    return { valid: true } // Boundary is optional
  }

  if (boundary.type !== 'Polygon') {
    return { valid: false, error: 'Boundary type must be Polygon' }
  }

  if (!boundary.coordinates || boundary.coordinates.length === 0) {
    return { valid: false, error: 'Boundary coordinates cannot be empty' }
  }

  const ring = boundary.coordinates[0]
  if (!ring || ring.length < 4) {
    return {
      valid: false,
      error: 'Boundary polygon must contain at least 4 coordinates (closed ring)',
    }
  }

  for (let i = 0; i < ring.length; i++) {
    const pt = ring[i]
    if (!pt || pt.length < 2) {
      return { valid: false, error: `Invalid coordinate pair at index ${i}` }
    }
    const [lon, lat] = pt
    if (!isInsideUzbekistan(lat, lon)) {
      return {
        valid: false,
        error: `Coordinate at index ${i} (${lat}, ${lon}) is outside Uzbekistan bounds`,
      }
    }
  }

  const first = ring[0]
  const last = ring[ring.length - 1]
  const isClosed =
    Math.abs(first[0] - last[0]) < EPSILON &&
    Math.abs(first[1] - last[1]) < EPSILON

  if (!isClosed) {
    return {
      valid: false,
      error: 'Boundary polygon exterior ring is not closed (first point != last point)',
    }
  }

  return { valid: true }
}

/**
 * Calculates the geodesic surface area of a polygon ring in canonical Hectares (ha)
 * using the spherical excess algorithm.
 */
export function calculatePolygonAreaHectares(points: LatLngPoint[]): number {
  if (points.length < 3) return 0

  let totalAngle = 0
  const n = points.length

  const toRad = (deg: number) => (deg * Math.PI) / 180

  for (let i = 0; i < n; i++) {
    const p1 = points[i]
    const p2 = points[(i + 1) % n]

    const lat1 = toRad(p1.lat)
    const lat2 = toRad(p2.lat)
    const lonDiff = toRad(p2.lng - p1.lng)

    totalAngle += lonDiff * (Math.sin(lat1) + Math.sin(lat2))
  }

  const areaSqMeters = Math.abs((totalAngle * EARTH_RADIUS_METERS * EARTH_RADIUS_METERS) / 2)
  const areaHectares = areaSqMeters / 10000

  return Math.round(areaHectares * 100) / 100
}
