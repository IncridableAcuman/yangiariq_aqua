import { describe, it, expect } from 'vitest'
import {
  isInsideUzbekistan,
  geoJsonToLeafletLatLngs,
  leafletLatLngsToGeoJson,
  validateBoundary,
  calculatePolygonAreaHectares,
  UZBEKISTAN_BOUNDS,
  YANGIARIQ_DEFAULT_CENTER,
} from '@/features/field/gis/gis.utils'
import { FieldBoundary } from '@/entities/field'

describe('GIS Utilities & Coordinate Transformation Tests', () => {
  it('isInsideUzbekistan should return true for valid Yangiariq coordinates', () => {
    expect(isInsideUzbekistan(YANGIARIQ_DEFAULT_CENTER.lat, YANGIARIQ_DEFAULT_CENTER.lng)).toBe(true)
    expect(isInsideUzbekistan(41.450123, 60.520456)).toBe(true)
    expect(isInsideUzbekistan(UZBEKISTAN_BOUNDS.minLat, UZBEKISTAN_BOUNDS.minLng)).toBe(true)
    expect(isInsideUzbekistan(UZBEKISTAN_BOUNDS.maxLat, UZBEKISTAN_BOUNDS.maxLng)).toBe(true)
  })

  it('isInsideUzbekistan should return false for coordinates outside Uzbekistan or invalid values', () => {
    expect(isInsideUzbekistan(35.0, 60.0)).toBe(false) // Too far south
    expect(isInsideUzbekistan(47.0, 60.0)).toBe(false) // Too far north
    expect(isInsideUzbekistan(41.0, 50.0)).toBe(false) // Too far west
    expect(isInsideUzbekistan(41.0, 75.0)).toBe(false) // Too far east
    expect(isInsideUzbekistan(NaN, 60.0)).toBe(false)
    expect(isInsideUzbekistan(41.0, NaN)).toBe(false)
  })

  it('geoJsonToLeafletLatLngs should transform [lon, lat] pairs into [lat, lon]', () => {
    const geoJsonBoundary: FieldBoundary = {
      type: 'Polygon',
      coordinates: [
        [
          [60.51, 41.41],
          [60.52, 41.41],
          [60.52, 41.42],
          [60.51, 41.42],
          [60.51, 41.41],
        ],
      ],
    }

    const leafletCoords = geoJsonToLeafletLatLngs(geoJsonBoundary)
    expect(leafletCoords.length).toBe(5)
    // First point [lon 60.51, lat 41.41] -> [lat 41.41, lon 60.51]
    expect(leafletCoords[0]).toEqual([41.41, 60.51])
    expect(leafletCoords[2]).toEqual([41.42, 60.52])
  })

  it('geoJsonToLeafletLatLngs should return empty array for null or undefined boundary', () => {
    expect(geoJsonToLeafletLatLngs(null)).toEqual([])
    expect(geoJsonToLeafletLatLngs(undefined)).toEqual([])
  })

  it('leafletLatLngsToGeoJson should convert {lat, lng} points into canonical GeoJSON [lon, lat] and close the ring', () => {
    const openPoints = [
      { lat: 41.41, lng: 60.51 },
      { lat: 41.41, lng: 60.52 },
      { lat: 41.42, lng: 60.52 },
    ]

    const result = leafletLatLngsToGeoJson(openPoints)
    expect(result.type).toBe('Polygon')
    expect(result.coordinates.length).toBe(1)
    const ring = result.coordinates[0]
    // 3 unique points + 1 closing point = 4 coordinates
    expect(ring.length).toBe(4)
    expect(ring[0]).toEqual([60.51, 41.41]) // [lon, lat]
    expect(ring[1]).toEqual([60.52, 41.41])
    expect(ring[2]).toEqual([60.52, 41.42])
    expect(ring[3]).toEqual([60.51, 41.41]) // Closed loop matches first
  })

  it('leafletLatLngsToGeoJson should throw error if fewer than 3 vertices provided', () => {
    const invalidPoints = [
      { lat: 41.41, lng: 60.51 },
      { lat: 41.42, lng: 60.52 },
    ]

    expect(() => leafletLatLngsToGeoJson(invalidPoints)).toThrow(
      'At least 3 distinct vertices are required to define a boundary'
    )
  })

  it('validateBoundary should return valid for properly closed polygon within bounds', () => {
    const validBoundary: FieldBoundary = {
      type: 'Polygon',
      coordinates: [
        [
          [60.51, 41.41],
          [60.52, 41.41],
          [60.52, 41.42],
          [60.51, 41.42],
          [60.51, 41.41],
        ],
      ],
    }

    const validation = validateBoundary(validBoundary)
    expect(validation.valid).toBe(true)
  })

  it('validateBoundary should reject open polygon (first != last)', () => {
    const openBoundary: FieldBoundary = {
      type: 'Polygon',
      coordinates: [
        [
          [60.51, 41.41],
          [60.52, 41.41],
          [60.52, 41.42],
          [60.51, 41.42], // Missing closing point [60.51, 41.41]
        ],
      ],
    }

    const validation = validateBoundary(openBoundary)
    expect(validation.valid).toBe(false)
    expect(validation.error).toContain('not closed')
  })

  it('validateBoundary should reject boundary with coordinates outside Uzbekistan', () => {
    const outsideBoundary: FieldBoundary = {
      type: 'Polygon',
      coordinates: [
        [
          [50.0, 41.0], // Lon 50 is outside Uzbekistan bounds (min 56.0)
          [50.1, 41.0],
          [50.1, 41.1],
          [50.0, 41.1],
          [50.0, 41.0],
        ],
      ],
    }

    const validation = validateBoundary(outsideBoundary)
    expect(validation.valid).toBe(false)
    expect(validation.error).toContain('outside Uzbekistan bounds')
  })

  it('calculatePolygonAreaHectares should calculate realistic area for agricultural parcel', () => {
    // Approx 200m x 200m square = 40,000 sq m = ~4.0 hectares
    // In Khorezm (~41.4°N): 1 deg lat ≈ 111,000 m (0.0018 deg ≈ 200m)
    // 1 deg lon ≈ 83,500 m (0.0024 deg ≈ 200m)
    const parcelPoints = [
      { lat: 41.41, lng: 60.51 },
      { lat: 41.41, lng: 60.5124 },
      { lat: 41.4118, lng: 60.5124 },
      { lat: 41.4118, lng: 60.51 },
    ]

    const area = calculatePolygonAreaHectares(parcelPoints)
    expect(area).toBeGreaterThan(3.5)
    expect(area).toBeLessThan(4.5)
  })
})
