import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import {
  AdaptiveHealthStrip,
  ContextPerformanceTable,
  AdaptiveSelectionsLedger,
  ExplainableSelectionModal,
} from '@/features/prediction/components'
import { PredictionsPage } from '@/pages/predictions/PredictionsPage'
import {
  AdaptiveOverviewResponse,
  AdaptiveSelectionDecision,
  ContextPerformanceProfile,
} from '@/entities/prediction'
import * as predictionApiModule from '@/entities/prediction/api'

// Mock AuthContext
vi.mock('@/features/auth/context/AuthContext', () => ({
  useAuth: () => ({
    user: {
      id: 'admin-001',
      firstName: 'Admin',
      lastName: 'Director',
      phoneNumber: '+998901234567',
      role: 'ADMIN',
    },
    isAuthenticated: true,
  }),
}))

// Mock Fields query
vi.mock('@/features/field/queries/useFieldQueries', () => ({
  useFieldsQuery: () => ({
    data: {
      content: [
        { id: 'field-1', name: 'Paxtakor 1', cropType: 'COTTON', areaHectares: 10.0 },
      ],
    },
    isLoading: false,
  }),
}))

describe('Milestone 08.10: Context-Aware Adaptive Intelligence Engine Suite', () => {
  let queryClient: QueryClient

  const mockAdaptiveOverview: AdaptiveOverviewResponse = {
    totalAdaptiveMethodsCount: 6,
    activeVersionsCount: 4,
    contextAwareSelectionsCount: 42,
    baselineFallbackCount: 8,
    insufficientContextCount: 2,
    contextualDriftWarningActive: false,
    recentSelections: [],
    contextualProfiles: [],
  }

  const mockProfiles: ContextPerformanceProfile[] = [
    {
      predictionType: 'SOIL_MOISTURE_FORECAST',
      versionTag: 'SOIL_V1',
      cropType: 'COTTON',
      soilType: 'SANDY_LOAM',
      irrigationMethod: 'DRIP',
      telemetryQuality: 'HIGH',
      season: 'SUMMER',
      sampleSize: 24,
      hasSufficientSample: true,
      meanAbsoluteError: 8.4,
      rootMeanSquaredError: 10.1,
      meanBiasError: 0.8,
      accuracyPercent: 91.5,
      globalModelHealth: 'HEALTHY',
      contextualModelHealth: 'HEALTHY',
      contextDriftDetected: false,
      driftDiagnosis: null,
    },
    {
      predictionType: 'WATER_DEMAND_FORECAST',
      versionTag: 'WATER_V1',
      cropType: 'WHEAT',
      soilType: 'LOAM',
      irrigationMethod: 'FLOOD',
      telemetryQuality: 'MEDIUM',
      season: 'SPRING',
      sampleSize: 6,
      hasSufficientSample: false, // n < 10
      meanAbsoluteError: 16.2,
      rootMeanSquaredError: 18.5,
      meanBiasError: -2.4,
      accuracyPercent: 74.0,
      globalModelHealth: 'HEALTHY',
      contextualModelHealth: 'INSUFFICIENT_DATA',
      contextDriftDetected: false,
      driftDiagnosis: null,
    },
  ]

  const mockDecision: AdaptiveSelectionDecision = {
    id: 'dec-101',
    predictionId: 'pred-202',
    fieldId: 'field-1',
    fieldName: 'Paxtakor 1',
    predictionType: 'SOIL_MOISTURE_FORECAST',
    selectedVersionTag: 'SOIL_V1',
    selectionMode: 'EXACT_CONTEXT',
    selectionReason: 'Method SOIL_V1 selected based on exact context with n=24 evaluations (MAE: 8.4%, bias: +0.8%).',
    evidenceSummary: 'Validated for COTTON + DRIP under SUMMER season.',
    contextSnapshotJson: JSON.stringify({
      cropType: 'COTTON',
      soilType: 'SANDY_LOAM',
      irrigationMethod: 'DRIP',
      telemetryQuality: 'HIGH',
      season: 'SUMMER',
      sensorAvailability: 'COMPLETE',
      waterAvailability: 'NORMAL',
      recentAnomalySeverity: 'NONE',
      temperatureContext: 'HOT',
      rainfallContext: 'DRY',
    }),
    alternativesJson: JSON.stringify([
      {
        versionTag: 'SOIL_V1',
        status: 'ACTIVE',
        contextMatchLevel: 'EXACT_CONTEXT',
        meanAbsoluteError: 8.4,
        selectionScore: 88.5,
      },
      {
        versionTag: 'SOIL_V2',
        status: 'APPROVED',
        contextMatchLevel: 'PARTIAL_CONTEXT',
        meanAbsoluteError: 11.2,
        selectionScore: 74.0,
        rejectionReason: 'Lower context match precedence',
      },
    ]),
    confidencePercent: 92,
    createdAt: new Date().toISOString(),
  }

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    })
    vi.clearAllMocks()
  })

  const renderWithClient = (ui: React.ReactElement) => {
    return render(<QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>)
  }

  it('renders AdaptiveHealthStrip with product principle and KPI metrics', () => {
    renderWithClient(
      <AdaptiveHealthStrip
        overview={mockAdaptiveOverview}
        isLoading={false}
        lang="en"
      />
    )

    expect(screen.getByText('Context-Aware Adaptive Intelligence Engine')).toBeInTheDocument()
    expect(screen.getByText(/YANGIARIQ AQUA AI does not autonomously self-modify/i)).toBeInTheDocument()
    expect(screen.getByText('42')).toBeInTheDocument() // contextAwareSelectionsCount
    expect(screen.getByText('8')).toBeInTheDocument()  // baselineFallbackCount
    expect(screen.getByText('2')).toBeInTheDocument()  // insufficientContextCount
  })

  it('renders ContextPerformanceTable with sample gating badges (n >= 10 vs n < 10)', () => {
    renderWithClient(
      <ContextPerformanceTable
        profiles={mockProfiles}
        isLoading={false}
        lang="en"
      />
    )

    expect(screen.getByText('Contextual Performance Matrix')).toBeInTheDocument()
    expect(screen.getAllByText('COTTON').length).toBeGreaterThan(0)
    expect(screen.getAllByText('WHEAT').length).toBeGreaterThan(0)
    expect(screen.getByText('n ≥ 10')).toBeInTheDocument()
    expect(screen.getByText('n < 10')).toBeInTheDocument()
    expect(screen.getByText('8.4%')).toBeInTheDocument()
    expect(screen.getByText('HEALTHY')).toBeInTheDocument()
  })

  it('renders AdaptiveSelectionsLedger and triggers explain callback', () => {
    const handleExplain = vi.fn()

    renderWithClient(
      <AdaptiveSelectionsLedger
        decisions={[mockDecision]}
        isLoading={false}
        onExplainDecision={handleExplain}
        lang="en"
      />
    )

    expect(screen.getByText('Adaptive Selections Audit Ledger')).toBeInTheDocument()
    expect(screen.getByText('Paxtakor 1')).toBeInTheDocument()
    expect(screen.getByText('Exact Context')).toBeInTheDocument()
    expect(screen.getByText('92%')).toBeInTheDocument()

    const explainBtn = screen.getByText('Why this method?')
    fireEvent.click(explainBtn)

    expect(handleExplain).toHaveBeenCalledWith(mockDecision)
  })

  it('renders ExplainableSelectionModal with 9-level deterministic hierarchy audit', () => {
    const handleClose = vi.fn()

    renderWithClient(
      <ExplainableSelectionModal
        decision={mockDecision}
        isOpen={true}
        onClose={handleClose}
        lang="en"
      />
    )

    expect(screen.getByText('Why Was This Method Selected? (Deterministic Explanation)')).toBeInTheDocument()
    expect(screen.getAllByText(/SOIL_V1/i).length).toBeGreaterThan(0)
    expect(screen.getByText(/Level 1: Hydraulic Safety Priority Check/i)).toBeInTheDocument()
    expect(screen.getByText(/Level 2 & 3: Governed Eligibility Filter/i)).toBeInTheDocument()
    expect(screen.getByText(/Level 4: Physical Sensor Availability Gate/i)).toBeInTheDocument()
    expect(screen.getByText(/Level 5: Context Matching Precedence/i)).toBeInTheDocument()
    expect(screen.getByText(/Level 6 & 7: Empirical Historical Ranking & Sample Gating/i)).toBeInTheDocument()
    expect(screen.getByText(/Level 8: Contextual Health & Local Drift Guard/i)).toBeInTheDocument()
    expect(screen.getByText(/Level 9: Production Safe Fallback Guarantee/i)).toBeInTheDocument()
  })

  it('renders PredictionsPage and switches to Adaptive Intelligence tab', async () => {
    vi.spyOn(predictionApiModule, 'getAdaptiveOverview').mockResolvedValue(mockAdaptiveOverview)
    vi.spyOn(predictionApiModule, 'getContextPerformanceProfiles').mockResolvedValue(mockProfiles)
    vi.spyOn(predictionApiModule, 'getAdaptiveSelectionHistory').mockResolvedValue([mockDecision])

    renderWithClient(
      <PredictionsPage
        lang="en"
        onNavigate={() => {}}
      />
    )

    const adaptiveTabBtn = screen.getByTestId('tab-adaptive')
    expect(adaptiveTabBtn).toBeInTheDocument()

    fireEvent.click(adaptiveTabBtn)

    await waitFor(() => {
      expect(screen.getByTestId('adaptive-dashboard-view')).toBeInTheDocument()
    })
  })
})
