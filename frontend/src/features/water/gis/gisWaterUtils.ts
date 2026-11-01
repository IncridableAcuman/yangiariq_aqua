import { GisField, GisWaterSource, WaterStatusCategory } from '@/entities/gis'
import {
  geoJsonToLeafletLatLngs,
  YANGIARIQ_DEFAULT_CENTER,
  isInsideUzbekistan,
} from '@/features/field/gis/gis.utils'

export { YANGIARIQ_DEFAULT_CENTER, isInsideUzbekistan }

export interface WaterStatusStyle {
  color: string
  fillColor: string
  label: string
}

export const WATER_STATUS_STYLES: Record<WaterStatusCategory, WaterStatusStyle> = {
  NORMAL: {
    color: '#059669', // emerald-600
    fillColor: '#10b981', // emerald-500
    label: 'Normal',
  },
  WARNING: {
    color: '#d97706', // amber-600
    fillColor: '#f59e0b', // amber-500
    label: 'Warning (≥80%)',
  },
  OVER_CONSUMED: {
    color: '#e11d48', // rose-600
    fillColor: '#f43f5e', // rose-500
    label: 'Over Consumed (>100%)',
  },
  NO_ALLOCATION: {
    color: '#64748b', // slate-500
    fillColor: '#94a3b8', // slate-400
    label: 'No Allocation',
  },
}

export const WATER_SOURCE_COLORS: Record<string, string> = {
  CANAL: '#0284c7', // sky-600
  DITCH: '#0891b2', // cyan-600
  RESERVOIR: '#1d4ed8', // blue-700
  WELL: '#4f46e5', // indigo-600
  RIVER: '#0ea5e9', // sky-500
  OTHER: '#64748b', // slate-500
}

/**
 * Creates SVG markup for water source map pins.
 */
export function getWaterSourceSvgPin(type: string, status: string = 'ACTIVE'): string {
  const color = status === 'INACTIVE' ? '#9ca3af' : (WATER_SOURCE_COLORS[type] || '#0284c7')

  // Inner icon according to type
  let iconPath = '<path d="M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5 5-2.24 5-5-2.24-5-5-5z" fill="#ffffff"/>'
  if (type === 'CANAL' || type === 'RIVER') {
    iconPath = '<path d="M6 12c2-2 4-2 6 0s4 2 6 0M6 15c2-2 4-2 6 0s4 2 6 0" stroke="#ffffff" stroke-width="2" stroke-linecap="round"/>'
  } else if (type === 'RESERVOIR') {
    iconPath = '<circle cx="12" cy="12" r="4.5" fill="#ffffff"/>'
  } else if (type === 'WELL') {
    iconPath = '<path d="M12 7v10M8 11h8" stroke="#ffffff" stroke-width="2" stroke-linecap="round"/>'
  }

  return `
    <div style="position: relative; width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; filter: drop-shadow(0 2px 4px rgba(0,0,0,0.3)); cursor: pointer;">
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="32" height="32" fill="${color}" stroke="#ffffff" stroke-width="1.5">
        <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"/>
      </svg>
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="16" height="16" style="position: absolute; top: 6px; left: 8px;">
        ${iconPath}
      </svg>
    </div>
  `
}

/**
 * Calculates a representative coordinate for a field (from location or boundary centroid).
 */
export function getFieldCentroid(field: GisField): [number, number] | null {
  if (
    typeof field.latitude === 'number' &&
    typeof field.longitude === 'number' &&
    !isNaN(field.latitude) &&
    !isNaN(field.longitude)
  ) {
    return [field.latitude, field.longitude]
  }

  const latlngs = geoJsonToLeafletLatLngs(field.boundary)
  if (latlngs.length >= 3) {
    let sumLat = 0
    let sumLng = 0
    // Skip the duplicate closing point in average
    const count = latlngs.length - 1
    for (let i = 0; i < count; i++) {
      sumLat += latlngs[i][0]
      sumLng += latlngs[i][1]
    }
    return [sumLat / count, sumLng / count]
  }

  return null
}

/**
 * Computes bounding limits enclosing all provided fields and water sources.
 */
export function calculateCombinedBounds(
  fields: GisField[],
  waterSources: GisWaterSource[]
): [[number, number], [number, number]] | null {
  const allPoints: [number, number][] = []

  for (const field of fields) {
    const latlngs = geoJsonToLeafletLatLngs(field.boundary)
    if (latlngs.length > 0) {
      allPoints.push(...latlngs)
    } else if (field.latitude != null && field.longitude != null) {
      allPoints.push([field.latitude, field.longitude])
    }
  }

  for (const source of waterSources) {
    if (source.latitude != null && source.longitude != null) {
      allPoints.push([source.latitude, source.longitude])
    }
  }

  if (allPoints.length === 0) {
    return null
  }

  let minLat = 90
  let maxLat = -90
  let minLng = 180
  let maxLng = -180

  for (const [lat, lng] of allPoints) {
    if (lat < minLat) minLat = lat
    if (lat > maxLat) maxLat = lat
    if (lng < minLng) minLng = lng
    if (lng > maxLng) maxLng = lng
  }

  // Ensure minimum bounding area to avoid zero-size bounds
  if (maxLat - minLat < 0.005) {
    minLat -= 0.005
    maxLat += 0.005
  }
  if (maxLng - minLng < 0.005) {
    minLng -= 0.005
    maxLng += 0.005
  }

  return [
    [minLat, minLng],
    [maxLat, maxLng],
  ]
}

export type DateRangePreset = 'all' | 'today' | '7days' | '30days' | 'season' | 'custom'

export function getDateRangeFromPreset(preset: DateRangePreset): { from?: string; to?: string } {
  const now = new Date()

  switch (preset) {
    case 'today': {
      const start = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0)
      return { from: start.toISOString(), to: now.toISOString() }
    }
    case '7days': {
      const past = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
      return { from: past.toISOString(), to: now.toISOString() }
    }
    case '30days': {
      const past = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
      return { from: past.toISOString(), to: now.toISOString() }
    }
    case 'season': {
      // Yangiariq cotton/wheat main irrigation season: May 1 - Oct 31 of current year
      const year = now.getFullYear()
      const seasonStart = new Date(Date.UTC(year, 4, 1, 0, 0, 0)) // May 1
      const seasonEnd = new Date(Date.UTC(year, 9, 31, 23, 59, 59)) // Oct 31
      return { from: seasonStart.toISOString(), to: seasonEnd.toISOString() }
    }
    case 'all':
    default:
      return {}
  }
}
