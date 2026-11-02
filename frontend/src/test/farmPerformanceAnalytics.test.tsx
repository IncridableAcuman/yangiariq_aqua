import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import {
  DataConfidenceBanner,
  FarmPerformanceKpis,
  WaterPerformanceCard,
  PeriodComparisonCard,
  FieldPerformanceTable,
  FarmInsightsCard,
  PeriodFilterBar,
} from '@/features/analytics'
import { AnalyticsPage } from '@/pages/analytics'
import {
  AnalyticsOverviewResponse,
  FieldPerformanceDto,
  FarmInsightDto,
  DataConfidenceDto,
} from '@/entities/analytics'
import * as analyticsApiModule from '@/entities/analytics/api'

// Mock Recharts ResponsiveContainer to render in jsdom
vi.mock('recharts', async () => {
  const OriginalModule = await vi.importActual<any>('recharts')
  return {
    ...OriginalModule,
    ResponsiveContainer: ({ children }: any) => (
      <div className="recharts-responsive-container" style={{ width: 800, height: 300 }}>
        {children}
      </div>
    ),
  }
})

// Mock AuthContext
vi.mock('@/features/auth/context/AuthContext', () => ({
  useAuth: () => ({
    user: {
      id: 'farmer-101',
      firstName: 'Anvar',
      lastName: 'Sobirov',
      phoneNumber: '+998901234567',
      role: 'FARMER',
    },
    isAuthenticated: true,
  }),
}))

// Mock Fields query
vi.mock('@/features/field/queries/useFieldQueries', () => ({
  useFieldsQuery: () => ({
    data: {
      content: [
        { id: 'field-1', name: 'Paxtakor 1' },
        { id: 'field-2', name: 'Bogʻdor 2' },
      ],
    },
    isLoading: false,
  }),
}))

describe('Farm Performance & Historical Intelligence Analytics Suite', () => {
  let queryClient: QueryClient

  const mockConfidenceComplete: DataConfidenceDto = {
    overall: 'COMPLETE',
    waterConfidence: 'COMPLETE',
    telemetryConfidence: 'COMPLETE',
    verificationConfidence: 'COMPLETE',
    hasIncompleteData: false,
    warningMessage: undefined,
  }

  const mockConfidencePartial: DataConfidenceDto = {
    overall: 'PARTIAL',
    waterConfidence: 'COMPLETE',
    telemetryConfidence: 'PARTIAL',
    verificationConfidence: 'PARTIAL',
    hasIncompleteData: true,
    warningMessage: 'Telemetriya qamrovi 72% boʻlgani sababli tahliliy hisob-kitoblar qisman ishonchli.',
  }

  const mockFieldPerformances: FieldPerformanceDto[] = [
    {
      fieldId: 'field-1',
      fieldName: 'Paxtakor Dala 1',
      cropType: 'COTTON',
      soilType: 'LOAM',
      areaHectares: 12.5,
      allocatedM3: 10000,
      consumedM3: 7500,
      remainingM3: 2500,
      utilizationPercent: 75.0,
      waterStatus: 'NORMAL',
      plansCount: 5,
      completedOperationsCount: 4,
      planAdherencePercent: 80.0,
      telemetryCoveragePercent: 92.0,
      sensorHealth: 'ONLINE',
      safetyEventCount: 0,
      criticalAnomalyCount: 0,
      verificationCoveragePercent: 100.0,
      performanceScore: 88,
      performanceStatus: 'NORMAL',
      scoreBreakdown: {
        waterCompliance: 100,
        planAdherence: 80,
        telemetryReliability: 92,
        safetyRecord: 100,
        verificationCoverage: 100,
      },
    },
    {
      fieldId: 'field-2',
      fieldName: 'Bogʻdor Dala 2',
      cropType: 'WHEAT',
      soilType: 'SANDY_LOAM',
      areaHectares: 8.0,
      allocatedM3: 5000,
      consumedM3: 5800,
      remainingM3: -800,
      utilizationPercent: 116.0,
      waterStatus: 'OVER_CONSUMED',
      plansCount: 4,
      completedOperationsCount: 4,
      planAdherencePercent: 100.0,
      telemetryCoveragePercent: 65.0,
      sensorHealth: 'DEGRADED',
      safetyEventCount: 2,
      criticalAnomalyCount: 1,
      verificationCoveragePercent: 50.0,
      performanceScore: 52,
      performanceStatus: 'ATTENTION',
      scoreBreakdown: {
        waterCompliance: 10,
        planAdherence: 100,
        telemetryReliability: 65,
        safetyRecord: 0,
        verificationCoverage: 50,
      },
    },
  ]

  const mockInsights: FarmInsightDto[] = [
    {
      id: 'ins-1',
      category: 'SAFETY',
      severity: 'CRITICAL',
      fieldId: 'field-2',
      fieldName: 'Bogʻdor Dala 2',
      title: 'Takroriy gidravlik anomaliya aniqlangan',
      explanation: 'Ushbu dalada kritik bosim tushishi yoki quvur yorilishi xavfi mavjud.',
      evidence: ['Kritik anomaliyalar soni: 1', 'Gidro-xavfsizlik holati: ATTENTION'],
      recommendedAction: 'Gidravlik xavfni tekshirish',
      targetSection: 'anomalies',
      targetPayload: { fieldId: 'field-2' },
      createdAt: '2026-11-01T10:00:00Z',
    },
    {
      id: 'ins-2',
      category: 'WATER',
      severity: 'HIGH',
      fieldId: 'field-2',
      fieldName: 'Bogʻdor Dala 2',
      title: 'Mavsumiy suv limiti oshirilgan',
      explanation: 'Dala uchun ajratilgan kvotadan 800 m³ ortiqcha suv sarflangan.',
      evidence: ['Limit: 5000 m³', 'Haqiqiy sarf: 5800 m³'],
      recommendedAction: 'Suv kvotasini qayta koʻrib chiqish',
      targetSection: 'water',
      targetPayload: { fieldId: 'field-2' },
      createdAt: '2026-11-01T10:00:00Z',
    },
  ]

  const mockOverviewResponse: AnalyticsOverviewResponse = {
    period: {
      type: 'LAST_30_DAYS',
      from: '2026-10-02T00:00:00Z',
      to: '2026-11-01T00:00:00Z',
      label: '02 Oct 2026 — 01 Nov 2026',
      durationDays: 30,
    },
    farmOverview: {
      totalFields: 2,
      activeFields: 2,
      totalAreaHectares: 20.5,
      aggregatePerformanceScore: 70,
      overallStatus: 'WARNING',
      totalSafetyEvents: 2,
      criticalAnomaliesCount: 1,
    },
    waterPerformance: {
      totalAllocatedM3: 15000,
      totalConsumedM3: 13300,
      totalRemainingM3: 1700,
      utilizationPercent: 88.7,
      variancePercent: 5.2,
      overConsumedFieldsCount: 1,
      overConsumedTotalM3: 800,
      waterStatus: 'WARNING',
      statusExplanation: 'Suv sarfi kvotaning 88.7% qismini tashkil etmoqda. 1 ta dalada limit oshirilgan.',
    },
    irrigationPerformance: {
      totalPlansCreated: 9,
      totalPlansApproved: 9,
      totalOperationsScheduled: 8,
      totalOperationsCompleted: 8,
      executionRatePercent: 88.9,
      planAdherencePercent: 88.9,
      averageDelayMinutes: 14,
      averageActualDurationMinutes: 125,
    },
    telemetryQuality: {
      totalSensors: 6,
      onlineSensors: 5,
      offlineSensors: 0,
      degradedSensors: 1,
      totalReadingsCount: 1420,
      validReadingsCount: 1280,
      validReadingPercent: 90.1,
      sensorAvailabilityPercent: 83.3,
      dataConfidence: 'COMPLETE',
    },
    verificationPerformance: {
      totalOperationsToVerify: 8,
      verifiedOperationsCount: 6,
      verificationCoveragePercent: 75.0,
      unverifiedCompletedOperationsCount: 2,
      automatedVerificationPercent: 75.0,
    },
    safetyMetrics: {
      totalAnomaliesDetected: 2,
      criticalAnomaliesCount: 1,
      warningAnomaliesCount: 1,
      activeHydraulicBlocksCount: 0,
      safetyIncidentRatePer100Ha: 9.75,
    },
    periodComparison: {
      hasComparisonData: true,
      previousPeriodLabel: '02 Sep 2026 — 02 Oct 2026',
      waterConsumedDeltaPercent: -4.5,
      waterConsumedDeltaM3: -630,
      operationsCompletedDelta: 1,
      planAdherenceDeltaPercent: 0,
      telemetryCoverageDeltaPercent: 0,
      safetyEventsDelta: 0,
      comparisonSummary: 'Oldingi davrga nisbatan suv sarfi 630 m³ (4.5%) tejaldi.',
    },
    dataConfidence: mockConfidencePartial,
    topInsights: mockInsights,
    generatedAt: '2026-11-01T12:00:00Z',
    tenantScope: 'Farmer Own Parcels',
  }

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    })
    vi.clearAllMocks()
  })

  it('1. DataConfidenceBanner: Hides when data confidence is COMPLETE without incomplete flags', () => {
    const { container } = render(
      <DataConfidenceBanner confidence={mockConfidenceComplete} lang="uz" />
    )
    expect(container.firstChild).toBeNull()
  })

  it('2. DataConfidenceBanner: Renders high-visibility warning when confidence is PARTIAL', () => {
    render(<DataConfidenceBanner confidence={mockConfidencePartial} lang="uz" />)
    expect(screen.getByText(/Tahliliy maʼlumotlar toʻliqligi va ishonchliligi holati/i)).toBeInTheDocument()
    expect(screen.getByText(/Qisman toʻliq/i)).toBeInTheDocument()
    expect(screen.getByText(/Telemetriya qamrovi 72%/i)).toBeInTheDocument()
  })

  it('3. FarmPerformanceKpis: Renders all 5 core metric factors with correct values', () => {
    render(
      <FarmPerformanceKpis
        lang="uz"
        water={mockOverviewResponse.waterPerformance}
        irrigation={mockOverviewResponse.irrigationPerformance}
        telemetry={mockOverviewResponse.telemetryQuality}
        verification={mockOverviewResponse.verificationPerformance}
        safety={mockOverviewResponse.safetyMetrics}
      />
    )

    // Water Utilization %
    expect(screen.getByText('88.7%')).toBeInTheDocument()
    // Plan Adherence %
    expect(screen.getByText('88.9%')).toBeInTheDocument()
    // Telemetry Quality %
    expect(screen.getByText('90.1%')).toBeInTheDocument()
    // Verification %
    expect(screen.getByText('75%')).toBeInTheDocument()
    // Critical anomaly tag
    expect(screen.getByText('1 Kritik')).toBeInTheDocument()
  })

  it('4. WaterPerformanceCard: Clearly distinguishes between Quota Utilization and Plan Variance', () => {
    render(<WaterPerformanceCard water={mockOverviewResponse.waterPerformance} lang="uz" />)
    expect(screen.getByText(/Suv sarfi va kvota tahlili/i)).toBeInTheDocument()
    // Utilization
    expect(screen.getAllByText(/88.7%/).length).toBeGreaterThanOrEqual(1)
    // Variance (+5.2%)
    expect(screen.getByText('+5.2%')).toBeInTheDocument()
    // Quota values
    expect(screen.getByText(/13,300/)).toBeInTheDocument()
    expect(screen.getByText(/1,700/)).toBeInTheDocument()
    // Governance notice
    expect(screen.getByText(/Izoh: "Limit sarfi" \(utilization\)/i)).toBeInTheDocument()
  })

  it('5. PeriodComparisonCard: Renders period comparison delta and summary', () => {
    render(
      <PeriodComparisonCard
        comparison={mockOverviewResponse.periodComparison}
        lang="uz"
      />
    )
    expect(screen.getByText(/Oldingi davr bilan qiyosiy tahlil/i)).toBeInTheDocument()
    expect(screen.getByText(/02 Sep 2026 — 02 Oct 2026/)).toBeInTheDocument()
    expect(screen.getByText(/-630 m³/)).toBeInTheDocument()
    expect(screen.getByText(/Oldingi davrga nisbatan suv sarfi 630 m³ \(4.5%\) tejaldi/i)).toBeInTheDocument()
  })

  it('6. PeriodComparisonCard: Renders insufficient data fallback message gracefully', () => {
    const insufficientComparison = {
      hasComparisonData: false,
      previousPeriodLabel: 'Oldingi davr',
      waterConsumedDeltaPercent: 0,
      waterConsumedDeltaM3: 0,
      operationsCompletedDelta: 0,
      planAdherenceDeltaPercent: 0,
      telemetryCoverageDeltaPercent: 0,
      safetyEventsDelta: 0,
      comparisonSummary: 'Taqqoslash uchun oldingi davr maʼlumotlari yetarli emas.',
    }
    render(<PeriodComparisonCard comparison={insufficientComparison} lang="uz" />)
    expect(screen.getByText(/Tarixiy maʼlumot yetarli emas/i)).toBeInTheDocument()
  })

  it('7. FieldPerformanceTable: Renders ranked parcels and opens explainable score breakdown', async () => {
    const onSortMock = vi.fn()
    const onPageMock = vi.fn()

    render(
      <FieldPerformanceTable
        fields={mockFieldPerformances}
        totalElements={2}
        page={0}
        size={10}
        totalPages={1}
        onPageChange={onPageMock}
        onSortChange={onSortMock}
        currentSortBy="score"
        currentSortDir="desc"
        lang="uz"
      />
    )

    // Verify parcel names
    expect(screen.getByText('Paxtakor Dala 1')).toBeInTheDocument()
    expect(screen.getByText('Bogʻdor Dala 2')).toBeInTheDocument()

    // Status badges
    expect(screen.getByText('NORMAL')).toBeInTheDocument()
    expect(screen.getByText('ATTENTION')).toBeInTheDocument()

    // Click score breakdown button for Field 1
    const scoreBtn = screen.getByText('88 / 100')
    fireEvent.click(scoreBtn)

    // Verify Explainable Score Modal opens
    expect(screen.getByText(/Samaradorlik indeksi hisob-kitobi/i)).toBeInTheDocument()
    expect(screen.getByText(/Suv kvotasiga rioya qilish \(25%\)/i)).toBeInTheDocument()
    expect(screen.getAllByText(/100 \/ 100/).length).toBeGreaterThanOrEqual(1)

    // Close modal
    const closeBtn = screen.getByText('Yopish')
    fireEvent.click(closeBtn)
    expect(screen.queryByText(/Samaradorlik indeksi hisob-kitobi/i)).not.toBeInTheDocument()
  })

  it('8. FarmInsightsCard: Deep-links to action when button clicked', () => {
    const onNavigateMock = vi.fn()
    render(
      <FarmInsightsCard
        insights={mockInsights}
        lang="uz"
        onNavigateToAction={onNavigateMock}
      />
    )

    expect(screen.getByText(/Takroriy gidravlik anomaliya aniqlangan/i)).toBeInTheDocument()
    expect(screen.getByText(/Mavsumiy suv limiti oshirilgan/i)).toBeInTheDocument()

    const actionButtons = screen.getAllByText('Amalni bajarish')
    expect(actionButtons.length).toBe(2)

    fireEvent.click(actionButtons[0])
    expect(onNavigateMock).toHaveBeenCalledWith('anomalies', { fieldId: 'field-2' })
  })

  it('9. PeriodFilterBar: Changes period when clicking preset buttons', () => {
    const onPeriodSelect = vi.fn()
    render(
      <PeriodFilterBar
        lang="uz"
        selectedPeriod="LAST_30_DAYS"
        onSelectPeriod={onPeriodSelect}
        dateRangeLabel="02 Oct 2026 — 01 Nov 2026"
        fields={[{ id: 'f-1', name: 'Dala 1' }]}
        selectedFieldId={null}
        onSelectField={vi.fn()}
        selectedCrop={null}
        onSelectCrop={vi.fn()}
        customFrom=""
        customTo=""
        onCustomDatesChange={vi.fn()}
        onRefresh={vi.fn()}
      />
    )

    expect(screen.getByText('02 Oct 2026 — 01 Nov 2026')).toBeInTheDocument()
    const last7DaysBtn = screen.getByText('7 kun')
    fireEvent.click(last7DaysBtn)
    expect(onPeriodSelect).toHaveBeenCalledWith('LAST_7_DAYS')
  })

  it('10. AnalyticsPage: Complete integration rendering with API mocks', async () => {
    vi.spyOn(analyticsApiModule.analyticsApi, 'fetchOverview').mockResolvedValue(mockOverviewResponse)
    vi.spyOn(analyticsApiModule.analyticsApi, 'fetchTrends').mockResolvedValue({
      granularity: 'DAILY',
      trendPoints: [
        {
          timestamp: '2026-10-31T00:00:00Z',
          dateLabel: '31 Oct',
          waterConsumedM3: 450,
          waterPlannedM3: 420,
          utilizationPercent: 107.1,
          completedOperationsCount: 1,
          telemetryCoveragePercent: 95.0,
          safetyEventsCount: 0,
        },
      ],
    })
    vi.spyOn(analyticsApiModule.analyticsApi, 'fetchFieldRankings').mockResolvedValue({
      fields: mockFieldPerformances,
      totalElements: 2,
      page: 0,
      size: 10,
      totalPages: 1,
    })
    vi.spyOn(analyticsApiModule.analyticsApi, 'fetchInsights').mockResolvedValue(mockInsights)

    const onNavigateMock = vi.fn()

    render(
      <QueryClientProvider client={queryClient}>
        <AnalyticsPage lang="uz" onNavigate={onNavigateMock} />
      </QueryClientProvider>
    )

    // Wait for async query data to render
    await waitFor(() => {
      expect(screen.getByText('02 Oct 2026 — 01 Nov 2026')).toBeInTheDocument()
    })

    // Verify key sections render
    expect(screen.getAllByText('Paxtakor Dala 1').length).toBeGreaterThanOrEqual(1)
    expect(screen.getAllByText('Bogʻdor Dala 2').length).toBeGreaterThanOrEqual(1)
    expect(screen.getByText(/Takroriy gidravlik anomaliya aniqlangan/i)).toBeInTheDocument()
  })

  it('11. FarmPerformanceSummaryCard: Renders concise summary card with 1-click CTA to detailed analytics', async () => {
    vi.spyOn(analyticsApiModule.analyticsApi, 'fetchOverview').mockResolvedValue(mockOverviewResponse)
    const onNavigateMock = vi.fn()

    const { FarmPerformanceSummaryCard } = await import('@/features/dashboard/components/FarmPerformanceSummaryCard')

    render(
      <QueryClientProvider client={queryClient}>
        <FarmPerformanceSummaryCard lang="uz" onNavigate={onNavigateMock} />
      </QueryClientProvider>
    )

    await waitFor(() => {
      expect(screen.getByText('Fermer xoʻjaligi samaradorligi')).toBeInTheDocument()
    })

    // Check KPIs
    expect(screen.getByText('88.7%')).toBeInTheDocument()
    expect(screen.getByText('88.9%')).toBeInTheDocument()
    expect(screen.getByText('90.1%')).toBeInTheDocument()
    expect(screen.getByText('75%')).toBeInTheDocument()

    // Click CTA
    const ctaBtn = screen.getByText('Batafsil tahlilni koʻrish')
    fireEvent.click(ctaBtn)
    expect(onNavigateMock).toHaveBeenCalledWith('analytics')
  })
})
