export type DataSourceType =
  | 'SENSOR_DEVICE'
  | 'WEATHER_PROVIDER'
  | 'OPERATOR_INPUT'
  | 'FARMER_INPUT'
  | 'CSV_IMPORT'
  | 'EXTERNAL_API'
  | 'MANUAL_MEASUREMENT'
  | 'SIMULATED_PROVIDER'

export type DataSourceStatus =
  | 'ACTIVE'
  | 'INACTIVE'
  | 'DEGRADED'
  | 'NOT_CONFIGURED'
  | 'DISCONNECTED'
  | 'ERROR'

export type DataQualityStatus =
  | 'VALID'
  | 'INVALID'
  | 'STALE'
  | 'DUPLICATE'
  | 'INCOMPLETE'
  | 'OUT_OF_RANGE'
  | 'UNVERIFIED'

export type EvidenceClassification =
  | 'OBSERVED'
  | 'VERIFIED'
  | 'ESTIMATED'
  | 'CALCULATED'
  | 'SIMULATED'
  | 'MISSING'

export interface DataSource {
  id: string
  sourceCode: string
  name: string
  sourceType: DataSourceType
  providerName: string
  description?: string
  status: DataSourceStatus
  endpointUrl?: string
  configurationRef?: string
  lastSyncAt?: string
  lastSuccessAt?: string
  lastErrorAt?: string
  lastErrorMessage?: string
  totalIngestedCount: number
  totalRejectedCount: number
  reliabilityRate: number
  createdAt: string
  updatedAt: string
}

export interface DataQualitySummary {
  totalIngested: number
  validCount: number
  invalidCount: number
  duplicateCount: number
  staleCount: number
  outOfRangeCount: number
  unverifiedCount: number
  overallQualityScore: number
}

export interface IngestedProvenance {
  id: string
  dataSourceId: string
  externalRecordId?: string
  idempotencyKey: string
  sensorReadingId?: string
  fieldId: string
  sensorId?: string
  sensorType: string
  rawValue: number
  rawUnit: string
  normalizedValue: number
  canonicalUnit: string
  qualityStatus: DataQualityStatus
  evidenceClassification: EvidenceClassification
  validationMessage?: string
  measuredAt: string
  ingestedAt: string
}

export interface ImportRowResult {
  rowNumber: number
  success: boolean
  externalRecordId?: string
  errorMessage?: string
}

export interface ImportBatchSummary {
  batchId: string
  fileName: string
  fileSizeBytes: number
  totalRows: number
  successfulRows: number
  rejectedRows: number
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'PARTIAL'
  startedAt: string
  completedAt?: string
  sampleErrors?: ImportRowResult[]
}

export interface CreateDataSourceRequest {
  sourceCode: string
  name: string
  sourceType: DataSourceType
  providerName: string
  description?: string
  endpointUrl?: string
  configurationRef?: string
  initialStatus?: DataSourceStatus
}

export interface UpdateDataSourceRequest {
  name?: string
  description?: string
  endpointUrl?: string
  configurationRef?: string
}

export interface RecordManualMeasurementRequest {
  fieldId: string
  sensorId?: string
  sensorType: string
  value: number
  unit?: string
  measuredAt?: string
  verified: boolean
  notes?: string
}

export interface IngestionResult {
  totalReceived: number
  validCount: number
  rejectedCount: number
  duplicateCount: number
  errors: string[]
}
