import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import {
  PriorityBadge,
  FarmOverviewMetrics,
  ActionCenter,
  PriorityFieldsTable,
  WaterBudgetOverviewCard,
  IrrigationLifecycleTracker,
  RecentActivityFeed,
} from '@/features/dashboard'
import { DashboardPage } from '@/pages/dashboard/DashboardPage'
import {
  DashboardOverviewResponse,
  PriorityField,
  UrgentAction,
} from '@/entities/dashboard'
import * as dashboardApi from '@/entities/dashboard/api'

// Mock AuthContext
vi.mock('@/features/auth/context/AuthContext', () => ({
  useAuth: () => ({
    user: {
      id: 'user-1',
      firstName: 'Rustam',
      lastName: 'Karimov',
      phoneNumber: '+998901234567',
      role: 'FARMER',
    },
    isAuthenticated: true,
  }),
}))

// Mock Leaflet Map in WaterFieldMap to avoid DOM canvas errors in node/jsdom
vi.mock('@/features/water/gis/components/WaterFieldMap', () => ({
  WaterFieldMap: () => <div data-testid="mock-water-field-map">Mock Leaflet Map</div>,
}))

// Mock useGisOverviewQuery
vi.mock('@/features/water/gis', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/features/water/gis')>()
  return {
    ...actual,
    useGisOverviewQuery: () => ({
      data: {
        fields: [],
        waterSources: [],
        waterStatuses: [],
        allocations: [],
      },
      isLoading: false,
    }),
  }
})

describe('Farm Intelligence Dashboard UI Suite', () => {
  let queryClient: QueryClient

  const mockPriorityFields: PriorityField[] = [
    {
      fieldId: 'field-101',
      fieldName: 'Dala 12 Paxta',
      cropType: 'COTTON',
      soilType: 'LOAM',
      areaHectares: 12.5,
      currentMoisturePercent: 12.0,
      targetMoisturePercent: 60.0,
      moistureTrend: 'DECREASING',
      telemetryFreshness: 'FRESH',
      waterStatus: 'ATTENTION_REQUIRED',
      allocatedM3: 15000,
      consumedM3: 12500,
      remainingM3: 2500,
      sensorHealth: 'ONLINE',
      activeAnomalyCount: 1,
      criticalAnomaly: 'SOIL_MOISTURE_CRITICAL',
      activeRecommendationCount: 1,
      topRecommendation: 'Tungi sugʻorish tavsiyasi',
      activePlanStatus: 'PROPOSED',
      hydraulicSafetyBlocked: false,
      hydraulicSafetyReason: null,
      priority: 'CRITICAL',
      priorityScore: 92,
      priorityReasons: [
        'Critical drought: Soil moisture (12.0%) below wilting point (15%)',
        'High crop water stress sensitivity (COTTON)',
        'Smart irrigation plan awaiting operator/farmer approval',
      ],
      recommendedAction: 'APPROVE_PLAN',
      actionTargetSection: 'irrigation',
    },
    {
      fieldId: 'field-102',
      fieldName: 'Dala 04 Bugʻdoy',
      cropType: 'WHEAT',
      soilType: 'SANDY',
      areaHectares: 8.0,
      currentMoisturePercent: 62.0,
      targetMoisturePercent: 60.0,
      moistureTrend: 'STABLE',
      telemetryFreshness: 'FRESH',
      waterStatus: 'OPTIMAL',
      allocatedM3: 10000,
      consumedM3: 4000,
      remainingM3: 6000,
      sensorHealth: 'ONLINE',
      activeAnomalyCount: 0,
      criticalAnomaly: null,
      activeRecommendationCount: 0,
      topRecommendation: null,
      activePlanStatus: 'NONE',
      hydraulicSafetyBlocked: false,
      hydraulicSafetyReason: null,
      priority: 'LOW',
      priorityScore: 20,
      priorityReasons: ['Soil moisture at or above optimal target (62.0%)'],
      recommendedAction: 'MONITOR',
      actionTargetSection: 'fields',
    },
  ]

  const mockUrgentActions: UrgentAction[] = [
    {
      id: 'act-1',
      actionType: 'CRITICAL_DROUGHT',
      severity: 'CRITICAL',
      title: 'Critical Soil Moisture Deficit',
      description: 'Root-zone moisture at 12%, below wilting threshold (15%).',
      fieldId: 'field-101',
      fieldName: 'Dala 12 Paxta',
      targetSection: 'irrigation',
      actionLabel: 'Review Irrigation',
      createdAt: '2026-11-01T10:00:00Z',
    },
    {
      id: 'act-2',
      actionType: 'PLAN_APPROVAL_PENDING',
      severity: 'HIGH',
      title: 'Smart Irrigation Plan Awaiting Approval',
      description: 'Recommended volume: 450 m³ scheduled for tomorrow.',
      fieldId: 'field-101',
      fieldName: 'Dala 12 Paxta',
      targetSection: 'irrigation',
      actionLabel: 'Review & Approve',
      createdAt: '2026-11-01T10:15:00Z',
    },
  ]

  const mockDashboardData: DashboardOverviewResponse = {
    fieldMetrics: {
      totalFields: 2,
      activeFields: 2,
      inactiveFields: 0,
      fieldsRequiringAttention: 1,
      totalAreaHectares: 20.5,
    },
    waterMetrics: {
      totalAllocatedM3: 25000,
      totalConsumedM3: 16500,
      totalRemainingM3: 8500,
      totalCommittedM3: 2000,
      totalReservedM3: 1000,
      totalOverConsumedM3: 0,
      overConsumedFieldCount: 0,
      overallConsumptionPercentage: 66.0,
    },
    sensorMetrics: {
      totalSensors: 4,
      activeSensors: 4,
      offlineSensors: 0,
      maintenanceSensors: 0,
      staleTelemetrySensors: 0,
      validTelemetryPercentage: 98.5,
    },
    anomalyMetrics: {
      totalOpen: 1,
      critical: 1,
      high: 0,
      medium: 0,
      acknowledged: 0,
    },
    recommendationMetrics: {
      totalActive: 1,
      critical: 1,
      high: 0,
      accepted: 0,
    },
    irrigationMetrics: {
      draftCount: 0,
      proposedCount: 1,
      approvedCount: 0,
      scheduledTodayCount: 0,
      inProgressCount: 0,
      completedCount: 2,
      blockedCount: 0,
    },
    waterBudget: {
      totalAllocatedM3: 25000,
      consumedM3: 16500,
      committedM3: 2000,
      reservedM3: 1000,
      remainingAvailableM3: 5500,
      isExhausted: false,
      utilizationPercentage: 78.0,
    },
    priorityFields: mockPriorityFields,
    urgentActions: mockUrgentActions,
    recentActivity: [
      {
        id: 'act-1',
        timestamp: new Date().toISOString(),
        activityType: 'ANOMALY_DETECTED',
        title: 'Anomaly: SOIL_MOISTURE_CRITICAL',
        description: 'Moisture dropped below 15%',
        severity: 'CRITICAL',
        fieldId: 'field-101',
        fieldName: 'Dala 12 Paxta',
      },
    ],
    generatedAt: new Date().toISOString(),
    tenantScope: 'FARMER (user-1, 2 fields)',
  }

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    })
    vi.clearAllMocks()
  })

  // 1. PriorityBadge Tests
  describe('PriorityBadge Component', () => {
    it('renders CRITICAL priority with score and pulse icon', () => {
      render(<PriorityBadge priority="CRITICAL" score={95} lang="uz" />)
      expect(screen.getByText(/Kritik/i)).toBeInTheDocument()
      expect(screen.getByText(/\(95\)/i)).toBeInTheDocument()
    })

    it('renders LOW priority with normal emerald badge', () => {
      render(<PriorityBadge priority="LOW" score={15} lang="en" />)
      expect(screen.getByText(/Low/i)).toBeInTheDocument()
      expect(screen.getByText(/\(15\)/i)).toBeInTheDocument()
    })

    it('supports Russian localization', () => {
      render(<PriorityBadge priority="HIGH" score={72} lang="ru" />)
      expect(screen.getByText(/Высокий/i)).toBeInTheDocument()
      expect(screen.getByText(/\(72\)/i)).toBeInTheDocument()
    })
  })

  // 2. ActionCenter Tests
  describe('ActionCenter Component', () => {
    it('renders urgent action items with badges and triggers navigation', () => {
      const onNavigateMock = vi.fn()
      render(
        <ActionCenter
          urgentActions={mockUrgentActions}
          lang="uz"
          onNavigate={onNavigateMock}
        />
      )

      expect(screen.getByText(/Harakatlar Markazi/i)).toBeInTheDocument()
      expect(screen.getByText(/Critical Soil Moisture Deficit/i)).toBeInTheDocument()
      expect(screen.getByText(/Smart Irrigation Plan Awaiting Approval/i)).toBeInTheDocument()

      const reviewButtons = screen.getAllByRole('button', { name: /Review/i })
      expect(reviewButtons.length).toBeGreaterThanOrEqual(1)
      fireEvent.click(reviewButtons[0])
      expect(onNavigateMock).toHaveBeenCalledWith('irrigation')
    })

    it('renders reassuring empty state when there are no urgent actions', () => {
      render(<ActionCenter urgentActions={[]} lang="uz" onNavigate={vi.fn()} />)
      expect(screen.getByText(/Barcha holatlar meʼyorda/i)).toBeInTheDocument()
    })
  })

  // 3. FarmOverviewMetrics Tests
  describe('FarmOverviewMetrics Component', () => {
    it('renders 5 KPI blocks: Fields, Water, Sensors, Anomalies, Irrigation', () => {
      render(
        <FarmOverviewMetrics
          fieldMetrics={mockDashboardData.fieldMetrics}
          waterMetrics={mockDashboardData.waterMetrics}
          sensorMetrics={mockDashboardData.sensorMetrics}
          anomalyMetrics={mockDashboardData.anomalyMetrics}
          irrigationMetrics={mockDashboardData.irrigationMetrics}
          lang="uz"
          onNavigate={vi.fn()}
        />
      )

      expect(screen.getByText(/Dala Maydonlari/i)).toBeInTheDocument()
      expect(screen.getByText(/Suv Taqsimoti/i)).toBeInTheDocument()
      expect(screen.getByText(/Datchiklar Floti/i)).toBeInTheDocument()
      expect(screen.getByText(/Xavf & Ogohlantirish/i)).toBeInTheDocument()
      expect(screen.getByText(/Sugʻorish Jarayoni/i)).toBeInTheDocument()
      expect(screen.getByText(/66%/i)).toBeInTheDocument()
    })
  })

  // 4. PriorityFieldsTable Tests
  describe('PriorityFieldsTable Component', () => {
    it('renders prioritized parcels with soil moisture and action buttons', () => {
      const onNavigateMock = vi.fn()
      render(
        <PriorityFieldsTable
          fields={mockPriorityFields}
          lang="uz"
          onNavigate={onNavigateMock}
        />
      )

      expect(screen.getAllByText('Dala 12 Paxta').length).toBeGreaterThanOrEqual(1)
      expect(screen.getAllByText('Dala 04 Bugʻdoy').length).toBeGreaterThanOrEqual(1)
      expect(screen.getAllByText('12%').length).toBeGreaterThanOrEqual(1)
      expect(screen.getAllByText('62%').length).toBeGreaterThanOrEqual(1)
    })

    it('filters parcels dynamically by search term', () => {
      render(
        <PriorityFieldsTable
          fields={mockPriorityFields}
          lang="uz"
          onNavigate={vi.fn()}
        />
      )

      const searchInput = screen.getByPlaceholderText(/qidirish/i)
      fireEvent.change(searchInput, { target: { value: 'Bugʻdoy' } })

      expect(screen.queryAllByText('Dala 12 Paxta')).toHaveLength(0)
      expect(screen.getAllByText('Dala 04 Bugʻdoy').length).toBeGreaterThanOrEqual(1)
    })

    it('opens priority explanation modal when info icon is clicked', () => {
      render(
        <PriorityFieldsTable
          fields={mockPriorityFields}
          lang="uz"
          onNavigate={vi.fn()}
        />
      )

      const infoButtons = screen.getAllByTitle(/Prioritet Asoslari/i)
      fireEvent.click(infoButtons[0])

      expect(screen.getByText(/Nega ushbu dalaga ushbu prioritet darajasi berildi/i)).toBeInTheDocument()
      expect(screen.getByText(/Critical drought: Soil moisture/i)).toBeInTheDocument()
    })
  })

  // 5. WaterBudgetOverviewCard Tests
  describe('WaterBudgetOverviewCard Component', () => {
    it('renders volumetric breakdown and calculation formula', () => {
      render(
        <WaterBudgetOverviewCard
          waterBudget={mockDashboardData.waterBudget}
          lang="uz"
          onNavigate={vi.fn()}
        />
      )

      expect(screen.getByText(/Mavsumiy Suv Balansi/i)).toBeInTheDocument()
      expect(screen.getByText(/25,000/i)).toBeInTheDocument()
      expect(screen.getByText(/16,500/i)).toBeInTheDocument()
      expect(screen.getByText(/5,500/i)).toBeInTheDocument()
      expect(screen.getByText(/Qolgan = Ajratilgan - Sarflangan - Tasdiqlangan - Band qilingan/i)).toBeInTheDocument()
    })
  })

  // 6. IrrigationLifecycleTracker Tests
  describe('IrrigationLifecycleTracker Component', () => {
    it('renders all operational lifecycle stages', () => {
      render(
        <IrrigationLifecycleTracker
          metrics={mockDashboardData.irrigationMetrics}
          lang="uz"
          onNavigate={vi.fn()}
        />
      )

      expect(screen.getByText(/Sugʻorish Bosqichlari/i)).toBeInTheDocument()
      expect(screen.getByText(/Tasdiq Kutilmoqda/i)).toBeInTheDocument()
      expect(screen.getByText(/Muvaffaqiyatli Tugagan/i)).toBeInTheDocument()
    })
  })

  // 7. RecentActivityFeed Tests
  describe('RecentActivityFeed Component', () => {
    it('renders recent operational events', () => {
      render(
        <RecentActivityFeed
          activity={mockDashboardData.recentActivity}
          lang="uz"
        />
      )

      expect(screen.getByText(/Soʻnggi Hodisalar Oqimi/i)).toBeInTheDocument()
      expect(screen.getByText(/SOIL_MOISTURE_CRITICAL/i)).toBeInTheDocument()
    })
  })

  // 8. Full DashboardPage Integration Tests
  describe('DashboardPage Workspace Integration', () => {
    it('renders loading skeleton during initial fetch', () => {
      vi.spyOn(dashboardApi, 'fetchDashboardOverview').mockImplementation(() => new Promise(() => {}))

      render(
        <QueryClientProvider client={queryClient}>
          <DashboardPage lang="uz" onNavigate={vi.fn()} />
        </QueryClientProvider>
      )

      expect(screen.getByTestId('dashboard-loading')).toBeInTheDocument()
    })

    it('renders error state with retry when fetch fails', async () => {
      vi.spyOn(dashboardApi, 'fetchDashboardOverview').mockRejectedValue(new Error('Network error'))

      render(
        <QueryClientProvider client={queryClient}>
          <DashboardPage lang="uz" onNavigate={vi.fn()} />
        </QueryClientProvider>
      )

      await waitFor(() => {
        expect(screen.getByTestId('dashboard-error')).toBeInTheDocument()
      })
      expect(screen.getByText(/Network error/i)).toBeInTheDocument()
    })

    it('renders complete unified farm intelligence workspace with all sections', async () => {
      vi.spyOn(dashboardApi, 'fetchDashboardOverview').mockResolvedValue(mockDashboardData)

      render(
        <QueryClientProvider client={queryClient}>
          <DashboardPage lang="uz" onNavigate={vi.fn()} />
        </QueryClientProvider>
      )

      await waitFor(() => {
        expect(screen.getByTestId('dashboard-workspace')).toBeInTheDocument()
      })

      // Check all 6 main sections are rendered
      expect(screen.getByTestId('section-action-center')).toBeInTheDocument()
      expect(screen.getByTestId('section-farm-metrics')).toBeInTheDocument()
      expect(screen.getByTestId('section-priority-fields')).toBeInTheDocument()
      expect(screen.getByTestId('section-water-budget')).toBeInTheDocument()
      expect(screen.getByTestId('section-irrigation-lifecycle')).toBeInTheDocument()
      expect(screen.getByTestId('section-recent-activity')).toBeInTheDocument()
      expect(screen.getByTestId('section-gis-map')).toBeInTheDocument()

      // User greeting
      expect(screen.getByText(/Assalomu alaykum, Rustam!/i)).toBeInTheDocument()
    })
  })
})
