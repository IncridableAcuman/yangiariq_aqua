import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { FieldTelemetryDashboard } from '@/features/sensor/analytics/FieldTelemetryDashboard'
import * as fieldQueries from '@/features/field/queries/useFieldQueries'
import * as sensorQueries from '@/features/sensor/queries/useSensorQueries'
import { FieldAnalyticsSummary, TelemetryAggregate, Sensor } from '@/entities/sensor'

// Mock recharts ResponsiveContainer and components for jsdom
vi.mock('recharts', async () => {
  const actual = await vi.importActual<Record<string, unknown>>('recharts')
  return {
    ...actual,
    ResponsiveContainer: ({ children }: { children: React.ReactNode }) => (
      <div data-testid="responsive-container" style={{ width: 800, height: 400 }}>
        {children}
      </div>
    ),
    AreaChart: ({ children }: { children: React.ReactNode }) => (
      <div data-testid="area-chart">{children}</div>
    ),
    Area: () => <div data-testid="area" />,
    XAxis: () => <div data-testid="xaxis" />,
    YAxis: () => <div data-testid="yaxis" />,
    Tooltip: () => <div data-testid="tooltip" />,
    CartesianGrid: () => <div data-testid="grid" />,
  }
})

describe('FieldTelemetryDashboard Component', () => {
  const mockFields = [
    {
      id: 'field-1',
      name: 'Paxtakor 1',
      cropType: 'COTTON',
      areaHectares: 12.5,
      soilType: 'LOAM',
      irrigationMethod: 'DRIP',
      status: 'ACTIVE',
      ownerId: 'farmer-1',
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z',
    },
    {
      id: 'field-2',
      name: 'Bogʻdorchilik 2',
      cropType: 'ORCHARD',
      areaHectares: 5.0,
      soilType: 'SANDY',
      irrigationMethod: 'SPRINKLER',
      status: 'ACTIVE',
      ownerId: 'farmer-1',
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z',
    },
  ]

  const mockSensors: Sensor[] = [
    {
      id: 'sensor-1',
      fieldId: 'field-1',
      name: 'AquaProbe 400',
      deviceIdentifier: 'SN-001',
      type: 'SOIL_MOISTURE',
      status: 'ACTIVE',
      provider: 'SIMULATED',
      installedAt: '2026-01-01T00:00:00Z',
      lastSeenAt: '2026-11-01T09:00:00Z',
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-11-01T09:00:00Z',
    },
    {
      id: 'sensor-2',
      fieldId: 'field-1',
      name: 'FlowSense 90',
      deviceIdentifier: 'SN-002',
      type: 'WATER_FLOW',
      status: 'ACTIVE',
      provider: 'SIMULATED',
      installedAt: '2026-01-01T00:00:00Z',
      lastSeenAt: '2026-11-01T05:00:00Z',
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-11-01T05:00:00Z',
    },
  ]

  const mockSummary: FieldAnalyticsSummary = {
    fieldId: 'field-1',
    generatedAt: '2026-11-01T09:30:00Z',
    latestReadings: {
      SOIL_MOISTURE: {
        id: 'r-1',
        sensorId: 'sensor-1',
        fieldId: 'field-1',
        type: 'SOIL_MOISTURE',
        value: 38.4,
        unit: 'PERCENT',
        unitSymbol: '%',
        quality: 'VALID',
        source: 'SIMULATED',
        measuredAt: '2026-11-01T09:00:00Z',
        createdAt: '2026-11-01T09:00:01Z',
      },
      TEMPERATURE: {
        id: 'r-2',
        sensorId: 'sensor-temp',
        fieldId: 'field-1',
        type: 'TEMPERATURE',
        value: 24.6,
        unit: 'CELSIUS',
        unitSymbol: '°C',
        quality: 'VALID',
        source: 'SIMULATED',
        measuredAt: '2026-11-01T09:00:00Z',
        createdAt: '2026-11-01T09:00:01Z',
      },
      WATER_FLOW: {
        id: 'r-3',
        sensorId: 'sensor-2',
        fieldId: 'field-1',
        type: 'WATER_FLOW',
        value: 14.2,
        unit: 'CUBIC_METER_PER_HOUR',
        unitSymbol: 'm³/h',
        quality: 'VALID',
        source: 'SIMULATED',
        measuredAt: '2026-11-01T09:00:00Z',
        createdAt: '2026-11-01T09:00:01Z',
      },
      SALINITY: {
        id: 'r-4',
        sensorId: 'sensor-ec',
        fieldId: 'field-1',
        type: 'SALINITY',
        value: 1.85,
        unit: 'DECISIEMENS_PER_METER',
        unitSymbol: 'dS/m',
        quality: 'VALID',
        source: 'SIMULATED',
        measuredAt: '2026-11-01T09:00:00Z',
        createdAt: '2026-11-01T09:00:01Z',
      },
    },
    past24hAggregates: {
      SOIL_MOISTURE: {
        fieldId: 'field-1',
        sensorId: 'sensor-1',
        sensorType: 'SOIL_MOISTURE',
        interval: '1d',
        unit: 'PERCENT',
        bucketStartTime: '2026-10-31T09:30:00Z',
        bucketEndTime: '2026-11-01T09:30:00Z',
        readingCount: 96,
        average: 37.8,
        minimum: 34.2,
        maximum: 41.5,
        sum: null,
      },
      TEMPERATURE: {
        fieldId: 'field-1',
        sensorId: 'sensor-temp',
        sensorType: 'TEMPERATURE',
        interval: '1d',
        unit: 'CELSIUS',
        bucketStartTime: '2026-10-31T09:30:00Z',
        bucketEndTime: '2026-11-01T09:30:00Z',
        readingCount: 96,
        average: 22.1,
        minimum: 16.0,
        maximum: 28.0,
        sum: null,
      },
    },
    past24hDataQuality: {
      totalReadings: 288,
      validReadings: 280,
      suspectReadings: 6,
      invalidReadings: 2,
      validPercentage: 97.2,
      missingIntervals: 0,
      delayedReadings: 1,
    },
    availability: {
      totalSensors: 4,
      activeCount: 4,
      inactiveCount: 0,
      maintenanceCount: 0,
      offlineCount: 0,
      communicatingCount: 3,
      unresponsiveCount: 1,
    },
  }

  const mockAggregates: TelemetryAggregate[] = [
    {
      sensorId: 'sensor-1',
      fieldId: 'field-1',
      sensorType: 'SOIL_MOISTURE',
      interval: '1h',
      unit: 'PERCENT',
      bucketStartTime: '2026-11-01T08:00:00Z',
      bucketEndTime: '2026-11-01T09:00:00Z',
      readingCount: 12,
      average: 38.2,
      minimum: 37.9,
      maximum: 38.5,
      sum: null,
      firstReadingValue: 37.9,
      firstReadingAt: '2026-11-01T08:02:00Z',
      lastReadingValue: 38.4,
      lastReadingAt: '2026-11-01T08:58:00Z',
    },
  ]

  const mockMutate = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()

    vi.spyOn(fieldQueries, 'useFieldsQuery').mockReturnValue({
      data: {
        content: mockFields,
        page: 0,
        size: 10,
        totalElements: 2,
        totalPages: 1,
      },
      isLoading: false,
    } as unknown as ReturnType<typeof fieldQueries.useFieldsQuery>)

    vi.spyOn(sensorQueries, 'useFieldSensorsQuery').mockReturnValue({
      data: mockSensors,
      isLoading: false,
    } as unknown as ReturnType<typeof sensorQueries.useFieldSensorsQuery>)

    vi.spyOn(sensorQueries, 'useFieldAnalyticsSummaryQuery').mockReturnValue({
      data: mockSummary,
      isLoading: false,
      refetch: vi.fn(),
    } as unknown as ReturnType<typeof sensorQueries.useFieldAnalyticsSummaryQuery>)

    vi.spyOn(sensorQueries, 'useFieldAnalyticsQuery').mockReturnValue({
      data: mockAggregates,
      isLoading: false,
      refetch: vi.fn(),
    } as unknown as ReturnType<typeof sensorQueries.useFieldAnalyticsQuery>)

    vi.spyOn(sensorQueries, 'useSimulateReadingMutation').mockReturnValue({
      mutate: mockMutate,
      isPending: false,
    } as unknown as ReturnType<typeof sensorQueries.useSimulateReadingMutation>)
  })

  it('renders field selector and summary KPI cards correctly', () => {
    render(<FieldTelemetryDashboard lang="uz" initialFieldId="field-1" />)

    // Field selector contains field options
    expect(screen.getByText(/Paxtakor 1 \(COTTON - 12.5 ga\)/i)).toBeInTheDocument()

    // KPI values
    expect(screen.getByText('38.4')).toBeInTheDocument() // Soil moisture
    expect(screen.getByText('24.6')).toBeInTheDocument() // Temperature
    expect(screen.getByText('14.2')).toBeInTheDocument() // Water flow
    expect(screen.getByText('1.85')).toBeInTheDocument() // Salinity

    // 24h average text
    expect(screen.getByText('37.8%')).toBeInTheDocument()
    // Temperature min/max
    expect(screen.getByText('16° / 28°')).toBeInTheDocument()
  })

  it('renders hardware fleet availability and data quality indicators', () => {
    render(<FieldTelemetryDashboard lang="uz" initialFieldId="field-1" />)

    // Availability counts
    expect(screen.getByText('3')).toBeInTheDocument() // communicating
    expect(screen.getByText('1')).toBeInTheDocument() // unresponsive

    // Data quality
    expect(screen.getByText('97.2%')).toBeInTheDocument() // valid %
    expect(screen.getByText('6')).toBeInTheDocument() // suspect readings
    expect(screen.getByText('2')).toBeInTheDocument() // invalid readings
  })

  it('renders sensors list with identifier and simulation action', () => {
    render(<FieldTelemetryDashboard lang="uz" initialFieldId="field-1" />)

    expect(screen.getByText('SN-001')).toBeInTheDocument()
    expect(screen.getByText('AquaProbe 400')).toBeInTheDocument()

    // Find simulation button
    const simButtons = screen.getAllByRole('button', { name: /Simulyatsiya/i })
    expect(simButtons.length).toBeGreaterThan(0)

    // Trigger simulation
    fireEvent.click(simButtons[0])
    expect(mockMutate).toHaveBeenCalledWith(
      { sensorId: 'sensor-1', fieldId: 'field-1' },
      expect.any(Object)
    )
  })

  it('switches modality when modality button is clicked', () => {
    render(<FieldTelemetryDashboard lang="uz" initialFieldId="field-1" />)

    // Modality button for Harorat (Temperature)
    const tempButton = screen.getByRole('button', { name: /Harorat/i })
    fireEvent.click(tempButton)

    // Should re-render with Temperature active
    expect(tempButton).toHaveClass('shadow-sm')
  })

  it('renders correctly in Russian language', () => {
    render(<FieldTelemetryDashboard lang="ru" initialFieldId="field-1" />)

    expect(screen.getByText(/Выберите земельный участок/i)).toBeInTheDocument()
    expect(screen.getByText(/Статус датчиков/i)).toBeInTheDocument()
    expect(screen.getByText(/Качество данных/i)).toBeInTheDocument()
  })

  it('renders correctly in English language', () => {
    render(<FieldTelemetryDashboard lang="en" initialFieldId="field-1" />)

    expect(screen.getByText(/Select agricultural parcel/i)).toBeInTheDocument()
    expect(screen.getByText(/Hardware Availability/i)).toBeInTheDocument()
    expect(screen.getByText(/Data Quality/i)).toBeInTheDocument()
  })
})
