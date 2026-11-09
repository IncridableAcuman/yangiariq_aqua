import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import React from 'react'
import { ToastProvider } from '@/shared/ui'
import { DataIntegrationPage } from '@/pages/data-integration'
import { DataSourcesSummaryCards } from '@/features/data-integration/components/DataSourcesSummaryCards'
import { DataSourcesTable } from '@/features/data-integration/components/DataSourcesTable'
import { DataSourceDetailModal } from '@/features/data-integration/components/DataSourceDetailModal'
import { RegisterDataSourceDialog } from '@/features/data-integration/components/RegisterDataSourceDialog'
import { CsvImportDialog } from '@/features/data-integration/components/CsvImportDialog'
import { ManualMeasurementDialog } from '@/features/data-integration/components/ManualMeasurementDialog'
import { DataQualityMetricsView } from '@/features/data-integration/components/DataQualityMetricsView'
import { ProvenanceHistoryTable } from '@/features/data-integration/components/ProvenanceHistoryTable'
import { DataSource, DataQualitySummary, IngestedProvenance, ImportBatchSummary } from '@/entities/integration'
import * as clientModule from '@/shared/api/client'

// Mock AuthContext
vi.mock('@/features/auth/context/AuthContext', () => ({
  useAuth: () => ({
    user: {
      id: 'op-101',
      firstName: 'Operator',
      lastName: 'Yangiariq',
      role: 'OPERATOR',
    },
    isAuthenticated: true,
  }),
}))

const mockSources: DataSource[] = [
  {
    id: 'src-1',
    sourceCode: 'SIMULATED_SENSOR_PROVIDER',
    name: 'Simulated Edge Telemetry',
    sourceType: 'SIMULATED_PROVIDER',
    providerName: 'SimulatedSensorProvider',
    description: 'Internal telemetry generator',
    status: 'ACTIVE',
    totalIngestedCount: 1250,
    totalRejectedCount: 0,
    reliabilityRate: 100.0,
    createdAt: '2026-11-01T00:00:00Z',
    updatedAt: '2026-11-09T08:00:00Z',
    lastSyncAt: '2026-11-09T08:00:00Z',
  },
  {
    id: 'src-2',
    sourceCode: 'OPEN_METEO_WEATHER',
    name: 'Open-Meteo Regional Weather',
    sourceType: 'WEATHER_PROVIDER',
    providerName: 'OpenMeteoWeatherAdapter',
    description: 'Automated weather forecast and observation',
    status: 'NOT_CONFIGURED',
    totalIngestedCount: 0,
    totalRejectedCount: 0,
    reliabilityRate: 100.0,
    createdAt: '2026-11-01T00:00:00Z',
    updatedAt: '2026-11-09T08:00:00Z',
  },
  {
    id: 'src-3',
    sourceCode: 'CSV_TELEMETRY_IMPORT',
    name: 'CSV Telemetry Ingestion Gateway',
    sourceType: 'CSV_IMPORT',
    providerName: 'CsvTelemetryImporter',
    status: 'ACTIVE',
    totalIngestedCount: 450,
    totalRejectedCount: 12,
    reliabilityRate: 97.4,
    createdAt: '2026-11-02T00:00:00Z',
    updatedAt: '2026-11-09T08:00:00Z',
    lastSyncAt: '2026-11-09T07:30:00Z',
  },
]

const mockQualitySummary: DataQualitySummary = {
  totalIngested: 1712,
  validCount: 1680,
  invalidCount: 12,
  duplicateCount: 8,
  staleCount: 4,
  outOfRangeCount: 8,
  unverifiedCount: 0,
  overallQualityScore: 98.1,
}

const mockProvenanceRecords: IngestedProvenance[] = [
  {
    id: 'prov-1',
    dataSourceId: 'src-3',
    externalRecordId: 'CSV-ROW-1',
    idempotencyKey: 'CSV_TELEMETRY_IMPORT:CSV-ROW-1',
    fieldId: 'field-001',
    sensorType: 'SOIL_MOISTURE',
    rawValue: 52.4,
    rawUnit: '%',
    normalizedValue: 52.4,
    canonicalUnit: '%',
    qualityStatus: 'VALID',
    evidenceClassification: 'OBSERVED',
    measuredAt: '2026-11-09T08:00:00Z',
    ingestedAt: '2026-11-09T08:01:00Z',
  },
  {
    id: 'prov-2',
    dataSourceId: 'src-1',
    idempotencyKey: 'SIMULATED:field-001:SOIL_MOISTURE:12345',
    fieldId: 'field-001',
    sensorType: 'SOIL_MOISTURE',
    rawValue: 45.0,
    rawUnit: '%',
    normalizedValue: 45.0,
    canonicalUnit: '%',
    qualityStatus: 'VALID',
    evidenceClassification: 'SIMULATED',
    measuredAt: '2026-11-09T07:55:00Z',
    ingestedAt: '2026-11-09T07:55:01Z',
  },
]

const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  })

const renderWithProviders = (ui: React.ReactElement) => {
  const queryClient = createTestQueryClient()
  return render(
    <QueryClientProvider client={queryClient}>
      <ToastProvider>{ui}</ToastProvider>
    </QueryClientProvider>
  )
}

describe('Milestone 08.19 — Real-World Data Integration Frontend Suite', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.spyOn(clientModule, 'apiClient').mockImplementation(async (endpoint) => {
      if (endpoint === '/data-sources') {
        return { success: true, data: mockSources, timestamp: new Date().toISOString() }
      }
      if (endpoint.startsWith('/data-quality/summary')) {
        return { success: true, data: mockQualitySummary, timestamp: new Date().toISOString() }
      }
      if (endpoint.startsWith('/data-ingestion/history')) {
        return { success: true, data: mockProvenanceRecords, timestamp: new Date().toISOString() }
      }
      if (endpoint === '/data-imports') {
        return { success: true, data: [], timestamp: new Date().toISOString() }
      }
      return { success: true, data: null, timestamp: new Date().toISOString() }
    })
  })

  it('1. Renders DataSourcesSummaryCards with calculated KPIs', () => {
    render(<DataSourcesSummaryCards sources={mockSources} lang="en" />)

    expect(screen.getByText('3')).toBeInTheDocument() // Total sources
    expect(screen.getByText('2')).toBeInTheDocument() // Active sources
    expect(screen.getByText('1')).toBeInTheDocument() // Not configured sources
    expect(screen.getByText('1,700')).toBeInTheDocument() // Total ingested
    expect(screen.getByText('99.3%')).toBeInTheDocument() // Overall reliability
  })

  it('2. Renders DataSourcesTable with source rows, status badges, and action triggers', () => {
    const handleSelect = vi.fn()
    const handleToggle = vi.fn()
    const handleSync = vi.fn()

    render(
      <DataSourcesTable
        sources={mockSources}
        lang="en"
        onSelectSource={handleSelect}
        onToggleStatus={handleToggle}
        onSyncSource={handleSync}
        canManage={true}
      />
    )

    expect(screen.getByText('Simulated Edge Telemetry')).toBeInTheDocument()
    expect(screen.getByText('SIMULATED_SENSOR_PROVIDER')).toBeInTheDocument()
    expect(screen.getByText('Open-Meteo Regional Weather')).toBeInTheDocument()
    expect(screen.getByText('Not Configured')).toBeInTheDocument()
    expect(screen.getByText('97.4%')).toBeInTheDocument()

    // Click details button for first source
    const detailButtons = screen.getAllByTitle('View Details')
    fireEvent.click(detailButtons[0])
    expect(handleSelect).toHaveBeenCalledWith(mockSources[0])
  })

  it('3. Renders DataSourceDetailModal with configuration details and allows editing', async () => {
    const handleUpdate = vi.fn().mockResolvedValue(undefined)
    const handleClose = vi.fn()

    render(
      <DataSourceDetailModal
        source={mockSources[0]}
        isOpen={true}
        onClose={handleClose}
        onUpdate={handleUpdate}
        canManage={true}
        lang="en"
      />
    )

    expect(screen.getByText('Simulated Edge Telemetry')).toBeInTheDocument()
    expect(screen.getByText('SIMULATED_PROVIDER')).toBeInTheDocument()

    // Click edit configuration
    const editBtn = screen.getByText('Edit Configuration')
    fireEvent.click(editBtn)

    expect(screen.getByText('Save Changes')).toBeInTheDocument()
    fireEvent.click(screen.getByText('Save Changes'))

    await waitFor(() => {
      expect(handleUpdate).toHaveBeenCalled()
    })
  })

  it('4. Renders RegisterDataSourceDialog and validates required fields', async () => {
    const handleRegister = vi.fn().mockResolvedValue(undefined)
    const handleClose = vi.fn()

    render(
      <RegisterDataSourceDialog
        isOpen={true}
        onClose={handleClose}
        onRegister={handleRegister}
        lang="en"
      />
    )

    expect(screen.getByText('Register New Data Source')).toBeInTheDocument()

    const submitBtn = screen.getByRole('button', { name: 'Register' })
    fireEvent.click(submitBtn)

    // Fill in required fields
    fireEvent.change(screen.getByPlaceholderText('KHOREZM_METEO_API'), {
      target: { value: 'NEW_IOT_GATEWAY' },
    })
    fireEvent.change(screen.getByPlaceholderText('Khorezm Regional Agro-Meteo Gateway'), {
      target: { value: 'Yangiariq New IoT Gateway' },
    })
    fireEvent.change(screen.getByPlaceholderText('RegionalAgroMeteoAdapter'), {
      target: { value: 'HttpGatewayAdapter' },
    })

    fireEvent.click(submitBtn)

    await waitFor(() => {
      expect(handleRegister).toHaveBeenCalledWith(
        expect.objectContaining({
          sourceCode: 'NEW_IOT_GATEWAY',
          name: 'Yangiariq New IoT Gateway',
          providerName: 'HttpGatewayAdapter',
        })
      )
    })
  })

  it('5. Renders CsvImportDialog and simulates dry run validation preview', async () => {
    const mockSummary: ImportBatchSummary = {
      batchId: 'batch-test-1',
      fileName: 'telemetry.csv (DRY-RUN)',
      fileSizeBytes: 2048,
      totalRows: 10,
      successfulRows: 9,
      rejectedRows: 1,
      status: 'PARTIAL',
      startedAt: '2026-11-09T08:00:00Z',
      sampleErrors: [{ rowNumber: 5, success: false, errorMessage: 'Out of physical bounds' }],
    }
    const handleUpload = vi.fn().mockResolvedValue(mockSummary)

    render(
      <CsvImportDialog
        isOpen={true}
        onClose={vi.fn()}
        onUpload={handleUpload}
        lang="en"
      />
    )

    expect(screen.getByText('CSV Telemetry Batch Import')).toBeInTheDocument()
    expect(screen.getByText('Select or drop CSV file here')).toBeInTheDocument()

    // Create mock file
    const file = new File(['field_id,type,value,unit\nf1,SOIL_MOISTURE,50,%'], 'test.csv', {
      type: 'text/csv',
    })
    const input = document.querySelector('input[type="file"]') as HTMLInputElement
    fireEvent.change(input, { target: { files: [file] } })

    const validateBtn = screen.getByText('Validate Preview')
    fireEvent.click(validateBtn)

    await waitFor(() => {
      expect(handleUpload).toHaveBeenCalledWith(file, true)
    })

    // Assert dry run preview cards appear
    await waitFor(() => {
      expect(screen.getByText(/Dry Run Preview Mode/i)).toBeInTheDocument()
      expect(screen.getByText('Valid Rows')).toBeInTheDocument()
      expect(screen.getByText('Confirm & Import')).toBeInTheDocument()
    })
  })

  it('6. Renders ManualMeasurementDialog with operator verification control', async () => {
    const handleSubmit = vi.fn().mockResolvedValue(undefined)

    render(
      <ManualMeasurementDialog
        isOpen={true}
        onClose={vi.fn()}
        onSubmit={handleSubmit}
        canVerify={true}
        lang="en"
      />
    )

    expect(screen.getByText('Manual Field Measurement Entry')).toBeInTheDocument()
    expect(screen.getByText('Physically verified by operator (VERIFIED)')).toBeInTheDocument()

    fireEvent.change(screen.getByPlaceholderText('00000000-0000-0000-0000-000000000000'), {
      target: { value: '11111111-2222-3333-4444-555555555555' },
    })
    fireEvent.change(screen.getByPlaceholderText('45.5'), {
      target: { value: '48.2' },
    })

    fireEvent.click(screen.getByText('Submit Measurement'))

    await waitFor(() => {
      expect(handleSubmit).toHaveBeenCalledWith(
        expect.objectContaining({
          fieldId: '11111111-2222-3333-4444-555555555555',
          value: 48.2,
          verified: true,
        })
      )
    })
  })

  it('7. Renders DataQualityMetricsView displaying health dimensions', () => {
    render(<DataQualityMetricsView summary={mockQualitySummary} lang="en" />)

    expect(screen.getByText('98.1%')).toBeInTheDocument()
    expect(screen.getByText('1,680')).toBeInTheDocument() // Valid readings
    expect(screen.getAllByText('8').length).toBeGreaterThanOrEqual(1) // Duplicate records / Out of range
  })

  it('8. Renders ProvenanceHistoryTable with distinct evidence classifications', () => {
    render(<ProvenanceHistoryTable records={mockProvenanceRecords} lang="en" />)

    expect(screen.getByText('CSV-ROW-1')).toBeInTheDocument()
    expect(screen.getByText('OBSERVED')).toBeInTheDocument()
    expect(screen.getByText('SIMULATED')).toBeInTheDocument()
    expect(screen.getAllByText('VALID').length).toBe(2)
  })

  it('9. Renders full DataIntegrationPage and handles tab switching', async () => {
    renderWithProviders(<DataIntegrationPage lang="en" />)

    expect(screen.getByText('Real-World Data Integration & External Connectivity')).toBeInTheDocument()
    expect(await screen.findByText('Simulated Edge Telemetry')).toBeInTheDocument()

    // Switch to Quality tab
    const qualityTab = screen.getByText('Quality & Provenance')
    fireEvent.click(qualityTab)

    expect(await screen.findByText('Overall Data Quality & Reliability Health')).toBeInTheDocument()
    expect(await screen.findByText('Recent Telemetry Provenance Trace')).toBeInTheDocument()

    // Switch to Imports tab
    const importsTab = screen.getByText('CSV Import Batches')
    fireEvent.click(importsTab)

    expect(await screen.findByText('File Name')).toBeInTheDocument()
  })
})
