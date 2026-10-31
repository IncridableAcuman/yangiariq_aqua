import { FieldBoundary } from '@/entities/field'

export interface LatLngPoint {
  lat: number
  lng: number
}

export interface GisValidationResult {
  valid: boolean
  error?: string
}

export interface FieldMapProps {
  latitude?: number | null
  longitude?: number | null
  boundary?: FieldBoundary | null
  fieldName?: string
  cropType?: string
  areaHectares?: number
  status?: 'ACTIVE' | 'INACTIVE'
  className?: string
  height?: string
  interactive?: boolean
  showPopup?: boolean
}
