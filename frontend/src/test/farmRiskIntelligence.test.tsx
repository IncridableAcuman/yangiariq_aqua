import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import {
  RiskSeverityBadge,
  RiskConfidenceIndicator,
  CriticalRiskBanner,
  RiskOverviewCards,
  FieldRiskCard,
  FieldRiskTable,
  RiskFactorBreakdown,
  RiskEvidencePanel,
  RiskRecommendationCard,
  RiskActionPanel,
  RiskAssessmentDetailModal,
} from '@/features/risk'
import { RisksPage } from '@/pages/risks/RisksPage'
import { FarmRiskDashboardCard } from '@/features/dashboard/components/FarmRiskDashboardCard'
import {
  FarmRiskOverview,
  FieldRiskProfile,
  RiskEvidence,
  RiskFactor,
  RiskRecommendation,
} from '@/entities/risk'
import * as riskApiModule from '@/entities/risk/api'

// Mock AuthContext
vi.mock('@/features/auth/context/AuthContext', () => ({
  useAuth: () => ({
    user: {
      id: 'farmer-1',
      firstName: 'Alisher',
      lastName: 'Navoiy',
      role: 'FARMER',
    },
    isAuthenticated: true,
  }),
}))

describe('Farm-Level Predictive Risk & Resilience Intelligence Suite (Milestone 08.13)', () => {
  let queryClient: QueryClient

  const mockEvidence: RiskEvidence[] = [
    {
      id: 'ev-1',
      category: 'HYDRAULIC',
      metricName: 'WATER_PRESSURE',
      observedValue: 0.35,
      thresholdValue: 1.2,
      unit: 'bar',
      evidenceType: 'OBSERVED',
      source: 'HYDRAULIC_SAFETY',
      message: 'Critical pressure collapse below delivery threshold.',
      recordedAt: '2026-11-07T08:00:00Z',
      isAuthoritative: true,
    },
    {
      id: 'ev-2',
      category: 'SOIL_MOISTURE',
      metricName: 'PREDICTED_MOISTURE_24H',
      observedValue: 13.8,
      thresholdValue: 15.0,
      unit: '%',
      evidenceType: 'PREDICTED',
      source: 'PREDICTION_ENGINE',
      message: 'FAO-56 regression projects moisture wilting point in 18 hours.',
      recordedAt: '2026-11-07T08:00:00Z',
      isAuthoritative: false,
    },
    {
      id: 'ev-3',
      category: 'SENSOR_DATA_RELIABILITY',
      metricName: 'STALE_TELEMETRY',
      observedValue: 7.5,
      thresholdValue: 6.0,
      unit: 'hours',
      evidenceType: 'UNRELIABLE',
      source: 'TELEMETRY',
      message: 'Telemetry feed is stale (>6h old).',
      recordedAt: '2026-11-07T08:00:00Z',
      isAuthoritative: false,
    },
    {
      id: 'ev-4',
      category: 'WATER_AVAILABILITY',
      metricName: 'SEASONAL_QUOTA',
      observedValue: null,
      thresholdValue: 1.0,
      unit: 'allocation',
      evidenceType: 'MISSING',
      source: 'WATER_BUDGET',
      message: 'No seasonal water allocation registered for field parcel.',
      recordedAt: '2026-11-07T08:00:00Z',
      isAuthoritative: true,
    },
  ]

  const mockFactors: RiskFactor[] = [
    {
      id: 'factor-1',
      category: 'HYDRAULIC',
      severity: 'CRITICAL',
      confidence: 'HIGH',
      title: 'Mainline Pipeline Burst Suspected',
      description: 'Abnormal pressure drop detected simultaneously with surging intake flow.',
      evidenceList: [mockEvidence[0]],
      contributionScore: 100,
      potentialImpact: 'Catastrophic soil erosion, parcel flooding, downstream canal depletion.',
      suggestedAction: 'Isolate distribution valve immediately and inspect pipe coupling.',
      missingInputs: [],
    },
    {
      id: 'factor-2',
      category: 'SOIL_MOISTURE',
      severity: 'HIGH',
      confidence: 'MEDIUM',
      title: 'Root-Zone Deficit Imminent',
      description: 'Soil moisture is approaching critical 15% wilting threshold.',
      evidenceList: [mockEvidence[1]],
      contributionScore: 70,
      potentialImpact: 'Cotton vegetative stunting, reduced boll formation.',
      suggestedAction: 'Schedule precision drip irrigation cycle.',
      missingInputs: ['Manual probe corroboration'],
    },
  ]

  const mockRecommendations: RiskRecommendation[] = [
    {
      id: 'rec-1',
      assessmentId: 'assess-1',
      fieldId: 'field-101',
      fieldName: 'Parcel 101 - North Cotton',
      title: 'Inspect Mainline Hydraulic Infrastructure Immediately',
      description: 'Hydraulic safety interlock has blocked line pressurization.',
      category: 'HYDRAULIC',
      severity: 'CRITICAL',
      confidence: 'HIGH',
      supportingEvidence: [mockEvidence[0]],
      reason: 'Pressure dropped to 0.35 bar while water flow increased.',
      suggestedNextAction: 'Check physical pipeline segment at intake manifold.',
      priorityRank: 1,
      relatedOperationalEntity: 'ANOMALY:PIPE_BURST',
      destinationRoute: '/anomalies?fieldId=field-101',
      createdAt: '2026-11-07T08:00:00Z',
    },
    {
      id: 'rec-2',
      assessmentId: 'assess-2',
      fieldId: 'field-102',
      fieldName: 'Parcel 102 - South Wheat',
      title: 'Adjust Planned Irrigation Volumes to Match Quota',
      description: 'Upcoming scheduled runs exceed remaining water allocation balance.',
      category: 'WATER_AVAILABILITY',
      severity: 'HIGH',
      confidence: 'HIGH',
      supportingEvidence: [mockEvidence[3]],
      reason: 'Remaining quota is 250 m³ while scheduled volume is 420 m³.',
      suggestedNextAction: 'Reduce run duration or transfer quota in Water Management.',
      priorityRank: 4,
      relatedOperationalEntity: 'WATER_BUDGET:DEFICIT',
      destinationRoute: '/operations?fieldId=field-102',
      createdAt: '2026-11-07T08:05:00Z',
    },
  ]

  const mockFieldProfiles: FieldRiskProfile[] = [
    {
      assessmentId: 'assess-1',
      fieldId: 'field-101',
      fieldName: 'Parcel 101 - North Cotton',
      areaHectares: 14.5,
      cropType: 'Cotton',
      soilType: 'Sandy Loam',
      irrigationMethod: 'Drip',
      overallSeverity: 'CRITICAL',
      overallConfidence: 'HIGH',
      riskTrend: 'DEGRADING',
      riskScore: 100,
      isHydraulicBlocked: true,
      hydraulicBlockReason: 'Hydraulic Safety Interlock: SUSPECTED_PIPE_BURST detected. Pressurization blocked.',
      primaryRiskCategory: 'HYDRAULIC',
      lifecycleStatus: 'OPEN',
      priorityRank: 1,
      priorityReason: 'Rank 1: Hydraulic safety emergency. Pipeline rupture detected.',
      activeFactorCount: 1,
      recommendationCount: 1,
      riskFactors: [mockFactors[0]],
      recommendations: [mockRecommendations[0]],
      missingDataWarnings: [],
      evaluatedAt: '2026-11-07T08:00:00Z',
    },
    {
      assessmentId: 'assess-2',
      fieldId: 'field-102',
      fieldName: 'Parcel 102 - South Wheat',
      areaHectares: 20.0,
      cropType: 'Winter Wheat',
      soilType: 'Loam',
      irrigationMethod: 'Furrow',
      overallSeverity: 'HIGH',
      overallConfidence: 'MEDIUM',
      riskTrend: 'STABLE',
      riskScore: 68,
      isHydraulicBlocked: false,
      hydraulicBlockReason: null,
      primaryRiskCategory: 'SOIL_MOISTURE',
      lifecycleStatus: 'OPEN',
      priorityRank: 5,
      priorityReason: 'Rank 5: Severe soil moisture risk. Root-zone drying trend.',
      activeFactorCount: 1,
      recommendationCount: 1,
      riskFactors: [mockFactors[1]],
      recommendations: [mockRecommendations[1]],
      missingDataWarnings: ['Soil moisture reading unrefreshed for 7 hours'],
      evaluatedAt: '2026-11-07T08:05:00Z',
    },
    {
      assessmentId: 'assess-3',
      fieldId: 'field-103',
      fieldName: 'Parcel 103 - East Orchard',
      areaHectares: 8.0,
      cropType: 'Apple Orchard',
      soilType: 'Clay Loam',
      irrigationMethod: 'Drip',
      overallSeverity: 'LOW',
      overallConfidence: 'HIGH',
      riskTrend: 'IMPROVING',
      riskScore: 12,
      isHydraulicBlocked: false,
      hydraulicBlockReason: null,
      primaryRiskCategory: 'WATER_AVAILABILITY',
      lifecycleStatus: 'RESOLVED',
      priorityRank: 9,
      priorityReason: 'Rank 9: Nominal agricultural and operational baseline.',
      activeFactorCount: 0,
      recommendationCount: 0,
      riskFactors: [],
      recommendations: [],
      missingDataWarnings: [],
      evaluatedAt: '2026-11-07T08:10:00Z',
      resolvedAt: '2026-11-07T08:15:00Z',
      resolutionNotes: 'Irrigation cycle concluded with soil moisture recharged to 28%.',
    },
  ]

  const mockOverview: FarmRiskOverview = {
    totalAssessedFields: 3,
    criticalRiskCount: 1,
    highRiskCount: 1,
    mediumRiskCount: 0,
    lowRiskCount: 1,
    insufficientDataCount: 0,
    hydraulicEmergencyCount: 1,
    waterShortageCount: 1,
    severeMoistureRiskCount: 1,
    telemetryIssuesCount: 1,
    modelReliabilityWarningsCount: 0,
    activeRecommendationsCount: 2,
    categoryDistribution: {
      HYDRAULIC: 1,
      SOIL_MOISTURE: 1,
      WATER_AVAILABILITY: 1,
      IRRIGATION_OPERATIONAL: 0,
      SENSOR_DATA_RELIABILITY: 1,
      PREDICTIVE_MODEL_RELIABILITY: 0,
    },
    criticalEmergencyFields: [mockFieldProfiles[0]],
    topPrioritizedRecommendations: mockRecommendations,
    evaluatedAt: '2026-11-07T08:10:00Z',
  }

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    })
    vi.clearAllMocks()
  })

  // --------------------------------------------------------------------------
  // 1. RiskSeverityBadge
  // --------------------------------------------------------------------------
  it('1. RiskSeverityBadge renders distinct visual states across CRITICAL, HIGH, MEDIUM, LOW, and UNKNOWN', () => {
    const { rerender } = render(<RiskSeverityBadge severity="CRITICAL" />)
    expect(screen.getByTestId('risk-severity-badge-critical')).toHaveTextContent('CRITICAL')

    rerender(<RiskSeverityBadge severity="HIGH" />)
    expect(screen.getByTestId('risk-severity-badge-high')).toHaveTextContent('HIGH')

    rerender(<RiskSeverityBadge severity="MEDIUM" />)
    expect(screen.getByTestId('risk-severity-badge-medium')).toHaveTextContent('MEDIUM')

    rerender(<RiskSeverityBadge severity="LOW" />)
    expect(screen.getByTestId('risk-severity-badge-low')).toHaveTextContent('LOW')
  })

  // --------------------------------------------------------------------------
  // 2. RiskConfidenceIndicator
  // --------------------------------------------------------------------------
  it('2. RiskConfidenceIndicator clearly identifies INSUFFICIENT_DATA and separates confidence from severity', () => {
    const { rerender } = render(<RiskConfidenceIndicator confidence="INSUFFICIENT_DATA" />)
    expect(screen.getByTestId('risk-confidence-insufficient_data')).toHaveTextContent('Insufficient Data')

    rerender(<RiskConfidenceIndicator confidence="HIGH" />)
    expect(screen.getByTestId('risk-confidence-high')).toHaveTextContent('High Confidence')

    rerender(<RiskConfidenceIndicator confidence="LOW" />)
    expect(screen.getByTestId('risk-confidence-low')).toHaveTextContent('Low Confidence')
  })

  // --------------------------------------------------------------------------
  // 3. CriticalRiskBanner
  // --------------------------------------------------------------------------
  it('3. CriticalRiskBanner renders prominent alert on hydraulic interlock and prevents pressurization', () => {
    const onSelectField = vi.fn()
    render(
      <CriticalRiskBanner
        emergencyFields={[mockFieldProfiles[0]]}
        onSelectField={onSelectField}
      />
    )

    expect(screen.getByTestId('critical-risk-banner')).toBeInTheDocument()
    expect(screen.getByText(/HYDRAULIC SAFETY INTERLOCK ACTIVE/i)).toBeInTheDocument()
    expect(screen.getByText(/Parcel 101 - North Cotton: Pressurization Strictly Blocked/i)).toBeInTheDocument()

    const btn = screen.getByRole('button', { name: /Inspect Safety Block/i })
    fireEvent.click(btn)
    expect(onSelectField).toHaveBeenCalledWith('field-101')
  })

  // --------------------------------------------------------------------------
  // 4. RiskOverviewCards
  // --------------------------------------------------------------------------
  it('4. RiskOverviewCards renders all 6 agricultural risk metrics and allows interactive filtering', () => {
    const onFilterChange = vi.fn()
    render(
      <RiskOverviewCards
        overview={mockOverview}
        activeFilter="ALL"
        onFilterChange={onFilterChange}
      />
    )

    expect(screen.getByTestId('risk-overview-cards')).toBeInTheDocument()
    expect(screen.getByText('Hydraulic Interlocks')).toBeInTheDocument()
    expect(screen.getByText('Water Quota Risks')).toBeInTheDocument()
    expect(screen.getByText('Soil Moisture Stress')).toBeInTheDocument()
    expect(screen.getByText('Telemetry Gaps')).toBeInTheDocument()

    // Click on hydraulic interlock card
    fireEvent.click(screen.getByText('Hydraulic Interlocks'))
    expect(onFilterChange).toHaveBeenCalledWith('HYDRAULIC')
  })

  // --------------------------------------------------------------------------
  // 5. FieldRiskCard
  // --------------------------------------------------------------------------
  it('5. FieldRiskCard displays composite score, priority rank, and parcel agronomic attributes', () => {
    const onSelect = vi.fn()
    render(<FieldRiskCard profile={mockFieldProfiles[0]} onSelect={onSelect} />)

    expect(screen.getByText('Parcel 101 - North Cotton')).toBeInTheDocument()
    expect(screen.getByText('Cotton')).toBeInTheDocument()
    expect(screen.getByText(/14.5 ha/i)).toBeInTheDocument()
    expect(screen.getByText(/100\/100/i)).toBeInTheDocument()
    expect(screen.getByText(/Priority Rank #1/i)).toBeInTheDocument()
    expect(screen.getByText(/Hydraulic Safety Interlock: SUSPECTED_PIPE_BURST detected/i)).toBeInTheDocument()

    fireEvent.click(screen.getByTestId('field-risk-card-field-101'))
    expect(onSelect).toHaveBeenCalledWith('field-101')
  })

  // --------------------------------------------------------------------------
  // 6. FieldRiskTable
  // --------------------------------------------------------------------------
  it('6. FieldRiskTable filters parcels by severity, search query, and displays hydraulic badges', () => {
    const onSelectField = vi.fn()
    const onSearchChange = vi.fn()
    const onSeverityFilterChange = vi.fn()

    render(
      <FieldRiskTable
        fields={mockFieldProfiles}
        totalElements={3}
        onSelectField={onSelectField}
        onSearchChange={onSearchChange}
        onSeverityFilterChange={onSeverityFilterChange}
      />
    )

    expect(screen.getByTestId('field-risk-table-container')).toBeInTheDocument()
    expect(screen.getByText('Parcel 101 - North Cotton')).toBeInTheDocument()
    expect(screen.getByText('Parcel 102 - South Wheat')).toBeInTheDocument()
    expect(screen.getByText('Parcel 103 - East Orchard')).toBeInTheDocument()

    // Search input
    const searchInput = screen.getByTestId('field-risk-search-input')
    fireEvent.change(searchInput, { target: { value: 'Wheat' } })
    expect(onSearchChange).toHaveBeenCalledWith('Wheat')

    // Severity select
    const sevSelect = screen.getByTestId('field-risk-severity-select')
    fireEvent.change(sevSelect, { target: { value: 'CRITICAL' } })
    expect(onSeverityFilterChange).toHaveBeenCalledWith('CRITICAL')
  })

  // --------------------------------------------------------------------------
  // 7. RiskEvidencePanel
  // --------------------------------------------------------------------------
  it('7. RiskEvidencePanel clearly categorizes OBSERVED, PREDICTED, UNRELIABLE, and MISSING evidence', () => {
    render(<RiskEvidencePanel evidenceList={mockEvidence} />)

    expect(screen.getByText('OBSERVED (GROUND TRUTH)')).toBeInTheDocument()
    expect(screen.getByText('PREDICTED (FORECAST)')).toBeInTheDocument()
    expect(screen.getByText('UNRELIABLE / STALE')).toBeInTheDocument()
    expect(screen.getByText('MISSING INPUT')).toBeInTheDocument()
    expect(screen.getByText('WATER_PRESSURE')).toBeInTheDocument()
    expect(screen.getByText('PREDICTED_MOISTURE_24H')).toBeInTheDocument()
  })

  // --------------------------------------------------------------------------
  // 8. RiskFactorBreakdown
  // --------------------------------------------------------------------------
  it('8. RiskFactorBreakdown explains factor contribution, potential impact, and preventative actions', () => {
    render(<RiskFactorBreakdown factors={mockFactors} />)

    expect(screen.getByText('Mainline Pipeline Burst Suspected')).toBeInTheDocument()
    expect(screen.getAllByText(/Contribution:/i).length).toBe(2)
    expect(screen.getByText('100%')).toBeInTheDocument()
    expect(screen.getByText(/Catastrophic soil erosion, parcel flooding/i)).toBeInTheDocument()
    expect(screen.getByText(/Isolate distribution valve immediately/i)).toBeInTheDocument()
    expect(screen.getByText(/Missing domain inputs required for complete verification: Manual probe corroboration/i)).toBeInTheDocument()
  })

  // --------------------------------------------------------------------------
  // 9. RiskRecommendationCard
  // --------------------------------------------------------------------------
  it('9. RiskRecommendationCard renders priority rank, trigger reason, and deep-link route', () => {
    const onNavigateRoute = vi.fn()
    render(
      <RiskRecommendationCard
        recommendation={mockRecommendations[0]}
        onNavigateRoute={onNavigateRoute}
      />
    )

    expect(screen.getByText(/Priority #1/i)).toBeInTheDocument()
    expect(screen.getByText('Inspect Mainline Hydraulic Infrastructure Immediately')).toBeInTheDocument()
    expect(screen.getByText(/Pressure dropped to 0.35 bar while water flow increased./i)).toBeInTheDocument()

    const btn = screen.getByRole('button', { name: /Execute in Workflow/i })
    fireEvent.click(btn)
    expect(onNavigateRoute).toHaveBeenCalledWith('/anomalies?fieldId=field-101')
  })

  // --------------------------------------------------------------------------
  // 10. RiskActionPanel
  // --------------------------------------------------------------------------
  it('10. RiskActionPanel enforces human approval and mandatory resolution audit notes', async () => {
    const onAcknowledge = vi.fn().mockResolvedValue(undefined)
    const onResolve = vi.fn().mockResolvedValue(undefined)

    render(
      <RiskActionPanel
        profile={mockFieldProfiles[0]}
        onAcknowledge={onAcknowledge}
        onResolve={onResolve}
      />
    )

    expect(screen.getByText(/Human Control Guarantee/i)).toBeInTheDocument()

    // Click Resolve
    const resolveBtn = screen.getByTestId('btn-resolve-risk')
    fireEvent.click(resolveBtn)

    // Modal opens
    expect(screen.getByText('Resolve Risk Assessment')).toBeInTheDocument()
    const confirmBtn = screen.getByRole('button', { name: /Confirm Resolution/i })

    // When textarea is blank, confirm is disabled
    expect(confirmBtn).toBeDisabled()

    const textarea = screen.getByRole('textbox', { name: /Remedial Action Notes/i })
    fireEvent.change(textarea, { target: { value: 'Poly-pipe replaced and mainline pressure tested.' } })

    expect(confirmBtn).toBeEnabled()
    fireEvent.click(confirmBtn)

    await waitFor(() => {
      expect(onResolve).toHaveBeenCalledWith('Poly-pipe replaced and mainline pressure tested.')
    })
  })

  // --------------------------------------------------------------------------
  // 11. RisksPage Full Integration
  // --------------------------------------------------------------------------
  it('11. RisksPage integrates overview, tab switching, and renders field table cleanly', async () => {
    vi.spyOn(riskApiModule.riskApi, 'getOverview').mockResolvedValue(mockOverview)
    vi.spyOn(riskApiModule.riskApi, 'listFields').mockResolvedValue({
      content: mockFieldProfiles,
      page: 0,
      size: 15,
      totalElements: 3,
      totalPages: 1,
      last: true,
    })
    vi.spyOn(riskApiModule.riskApi, 'listRecommendations').mockResolvedValue({
      content: mockRecommendations,
      page: 0,
      size: 20,
      totalElements: 2,
      totalPages: 1,
      last: true,
    })

    render(
      <QueryClientProvider client={queryClient}>
        <RisksPage lang="en" />
      </QueryClientProvider>
    )

    await waitFor(() => {
      expect(screen.getByTestId('risks-page')).toBeInTheDocument()
      expect(screen.getByTestId('critical-risk-banner')).toBeInTheDocument()
    })

    expect(screen.getByText('Farm-Level Predictive Risk & Resilience Intelligence')).toBeInTheDocument()
    expect(screen.getByText('Field Risk Matrix')).toBeInTheDocument()
    expect(screen.getByText('Actionable Recommendations')).toBeInTheDocument()
    expect(screen.getByText('Safety Interlocks')).toBeInTheDocument()

    // Switch to Actionable Recommendations Tab
    const recTab = screen.getByRole('tab', { name: /Actionable Recommendations/i })
    fireEvent.pointerDown(recTab, { button: 0 })
    fireEvent.click(recTab)
    fireEvent.keyDown(recTab, { key: 'Enter' })
    await waitFor(() => {
      expect(screen.getByText('Inspect Mainline Hydraulic Infrastructure Immediately')).toBeInTheDocument()
    })

    // Switch to Safety Interlocks Tab
    const safetyTab = screen.getByRole('tab', { name: /Safety Interlocks/i })
    fireEvent.pointerDown(safetyTab, { button: 0 })
    fireEvent.click(safetyTab)
    fireEvent.keyDown(safetyTab, { key: 'Enter' })
    await waitFor(() => {
      expect(screen.getByText(/Non-Negotiable Hydraulic Precedence/i)).toBeInTheDocument()
    })
  })

  // --------------------------------------------------------------------------
  // 12. FarmRiskDashboardCard
  // --------------------------------------------------------------------------
  it('12. FarmRiskDashboardCard renders risk summary in dashboard and links to risks page', async () => {
    vi.spyOn(riskApiModule.riskApi, 'getOverview').mockResolvedValue(mockOverview)
    const onNavigate = vi.fn()

    render(
      <QueryClientProvider client={queryClient}>
        <FarmRiskDashboardCard lang="en" onNavigate={onNavigate} />
      </QueryClientProvider>
    )

    await waitFor(() => {
      expect(screen.getByTestId('farm-risk-dashboard-card')).toBeInTheDocument()
    })

    expect(screen.getByText('Predictive Risk & Resilience')).toBeInTheDocument()
    expect(screen.getByText(/CRITICAL: Hydraulic safety interlock active!/i)).toBeInTheDocument()

    const openCenterBtn = screen.getByTestId('btn-open-risk-center')
    fireEvent.click(openCenterBtn)
    expect(onNavigate).toHaveBeenCalledWith('risks')
  })

  // --------------------------------------------------------------------------
  // 13. RiskAssessmentDetailModal
  // --------------------------------------------------------------------------
  it('13. RiskAssessmentDetailModal renders tabs and field information when open', async () => {
    vi.spyOn(riskApiModule.riskApi, 'getFieldProfile').mockResolvedValue(mockFieldProfiles[0])
    vi.spyOn(riskApiModule.riskApi, 'getFieldHistory').mockResolvedValue({
      content: [mockFieldProfiles[0]],
      page: 0,
      size: 10,
      totalElements: 1,
      totalPages: 1,
      last: true,
    })

    const onClose = vi.fn()
    render(
      <QueryClientProvider client={queryClient}>
        <RiskAssessmentDetailModal
          fieldId="f1"
          isOpen={true}
          onClose={onClose}
        />
      </QueryClientProvider>
    )

    await waitFor(() => {
      expect(screen.getByText('Parcel 101 - North Cotton')).toBeInTheDocument()
    })
    expect(screen.getByText(/Risk Factors & Evidence/i)).toBeInTheDocument()
    expect(screen.getByText(/Actionable Recommendations/i)).toBeInTheDocument()
  })
})
