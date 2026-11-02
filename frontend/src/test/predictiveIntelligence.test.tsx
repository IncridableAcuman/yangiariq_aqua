import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import {
  PredictiveSummaryCard,
  SoilMoistureForecastCard,
  WaterDemandForecastCard,
  PredictionRiskOverview,
  IrrigationNeedMatrix,
  ForecastHistoryTable,
  PredictionHorizonSelector,
  PredictionExplanationModal,
} from '@/features/prediction/components'
import { PredictionsPage } from '@/pages/predictions/PredictionsPage'
import {
  PredictionItem,
  PredictionOverview,
  RankedFieldNeedItem,
  IrrigationNeedRanking,
  ForecastHistoryResponse,
} from '@/entities/prediction'
import * as predictionApiModule from '@/entities/prediction/api'

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
        { id: 'field-1', name: 'Paxtakor 1', cropType: 'COTTON', areaHectares: 10.0 },
        { id: 'field-2', name: 'Bogʻdor 2', cropType: 'WHEAT', areaHectares: 8.5 },
      ],
    },
    isLoading: false,
  }),
}))

describe('Predictive Farm Intelligence & Forecasting Suite (Milestone 08.7)', () => {
  let queryClient: QueryClient

  const mockPredictionItem: PredictionItem = {
    id: 'pred-101',
    fieldId: 'field-1',
    fieldName: 'Paxtakor 1',
    predictionType: 'SOIL_MOISTURE_FORECAST',
    horizon: 'NEXT_48_HOURS',
    targetTime: '2026-11-04T08:00:00Z',
    predictedValue: 21.5,
    lowerBound: 18.0,
    upperBound: 24.0,
    unit: '%',
    confidencePercent: 88,
    riskLevel: 'HIGH',
    dataSufficiency: 'SUFFICIENT',
    likelihood: 'HIGHLY_LIKELY',
    evidence: 'Regression velocity: -2.8%/day over 24h. Depletion exceeds MAD threshold 50.0%.',
    assumptions: 'FAO-56 standard daily ETc 5.2 mm/day, loam soil root zone 0.70m, zero precipitation.',
    recommendedAction: 'Schedule irrigation within next 24-48 hours with 450 m³ volume.',
    actionTargetModule: 'irrigation',
    actualOutcomeValue: null,
    accuracyStatus: 'PENDING',
    generatedAt: '2026-11-02T08:00:00Z',
  }

  const mockOverviewData: PredictionOverview = {
    horizon: 'NEXT_48_HOURS',
    totalParcelsCount: 5,
    fieldsNeedingIrrigationCount: 2,
    totalWaterDemandM3: 920.0,
    waterDemandLowerBoundM3: 820.0,
    waterDemandUpperBoundM3: 1050.0,
    fieldsAtWaterRiskCount: 1,
    sensorHealthPercent: 94.0,
    activeHydraulicBlocksCount: 1,
    urgentForecasts: [mockPredictionItem],
    generatedAt: '2026-11-02T08:00:00Z',
  }

  const mockRankedFields: RankedFieldNeedItem[] = [
    {
      fieldId: 'field-1',
      fieldName: 'Paxtakor 1',
      cropType: 'COTTON',
      areaHectares: 10.0,
      likelihood: 'HIGHLY_LIKELY',
      riskLevel: 'HIGH',
      currentMoisturePercent: 54.0,
      forecastMoisturePercent: 21.5,
      expectedVolumeM3: 450.0,
      confidencePercent: 88,
      dataSufficiency: 'SUFFICIENT',
      recommendedAction: 'Rejalashtirilgan sug‘orishni tasdiqlash tavsiya etiladi.',
    },
    {
      fieldId: 'field-2',
      fieldName: 'Bogʻdor 2',
      cropType: 'WHEAT',
      areaHectares: 8.5,
      likelihood: 'NOT_EXPECTED',
      riskLevel: 'LOW',
      currentMoisturePercent: 68.0,
      forecastMoisturePercent: 64.2,
      expectedVolumeM3: 0.0,
      confidencePercent: 92,
      dataSufficiency: 'SUFFICIENT',
      recommendedAction: 'Suv yetarli, sug‘orish talab qilinmaydi.',
    },
  ]

  const mockRankingResponse: IrrigationNeedRanking = {
    horizon: 'NEXT_48_HOURS',
    totalFieldsCount: 2,
    urgentCount: 1,
    rankedFields: mockRankedFields,
    generatedAt: '2026-11-02T08:00:00Z',
  }

  const mockForecastHistory: ForecastHistoryResponse = {
    accuracyPercent: 85.0,
    totalEvaluated: 24,
    verifiedAccurateCount: 17,
    verifiedInaccurateCount: 3,
    pendingCount: 4,
    history: [
      {
        id: 'pred-99',
        fieldId: 'field-1',
        fieldName: 'Paxtakor 1',
        predictionType: 'SOIL_MOISTURE_FORECAST',
        horizon: 'NEXT_24_HOURS',
        targetTime: '2026-11-01T12:00:00Z',
        predictedValue: 52.0,
        actualOutcomeValue: 53.2,
        unit: '%',
        confidencePercent: 85,
        accuracyStatus: 'VERIFIED_ACCURATE',
        generatedAt: '2026-10-31T12:00:00Z',
      },
      {
        id: 'pred-98',
        fieldId: 'field-2',
        fieldName: 'Bogʻdor 2',
        predictionType: 'WATER_DEMAND_FORECAST',
        horizon: 'NEXT_48_HOURS',
        targetTime: '2026-10-30T10:00:00Z',
        predictedValue: 350.0,
        actualOutcomeValue: 460.0,
        unit: 'm³',
        confidencePercent: 70,
        accuracyStatus: 'VERIFIED_INACCURATE',
        generatedAt: '2026-10-28T10:00:00Z',
      },
    ],
  }

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false, gcTime: 0 },
      },
    })
    vi.clearAllMocks()
  })

  it('renders PredictionHorizonSelector and triggers horizon change', () => {
    const handleHorizonChange = vi.fn()
    render(
      <PredictionHorizonSelector
        horizon="NEXT_48_HOURS"
        onHorizonChange={handleHorizonChange}
        lang="uz"
      />
    )

    expect(screen.getByText(/Keyingi 48 soat/i)).toBeInTheDocument()
    expect(screen.getByText(/Keyingi 24 soat/i)).toBeInTheDocument()
    expect(screen.getByText(/Keyingi 7 kun/i)).toBeInTheDocument()

    fireEvent.click(screen.getByText(/Keyingi 24 soat/i))
    expect(handleHorizonChange).toHaveBeenCalledWith('NEXT_24_HOURS')

    fireEvent.click(screen.getByText(/Keyingi 7 kun/i))
    expect(handleHorizonChange).toHaveBeenCalledWith('NEXT_7_DAYS')
  })

  it('renders PredictiveSummaryCard with 4 forecast signals and hydraulic block warning', async () => {
    vi.spyOn(predictionApiModule, 'getPredictionOverview').mockResolvedValue(mockOverviewData)
    const handleNavigate = vi.fn()

    render(
      <QueryClientProvider client={queryClient}>
        <PredictiveSummaryCard lang="uz" onNavigate={handleNavigate} />
      </QueryClientProvider>
    )

    await waitFor(() => {
      expect(screen.getByText(/Prediktiv Ferma Intellekti/i)).toBeInTheDocument()
    })

    // Signals check
    expect(screen.getByText('2')).toBeInTheDocument() // fields needing irrigation
    expect(screen.getByText(/920/)).toBeInTheDocument() // water demand m3
    expect(screen.getByText('1')).toBeInTheDocument() // fields at water risk
    expect(screen.getByText('94%')).toBeInTheDocument() // sensor health

    // Hydraulic block badge check
    expect(screen.getByText(/1 ta dalada gidravlik blokirovkasi faol/i)).toBeInTheDocument()

    // 1-Click CTA check
    const ctaBtn = screen.getByText(/Barcha prognozlarni ko‘rish/i)
    fireEvent.click(ctaBtn)
    expect(handleNavigate).toHaveBeenCalledWith('predictions')
  })

  it('renders SoilMoistureForecastCard with predicted value, uncertainty bounds, and explain modal trigger', () => {
    const handleExplain = vi.fn()
    const handleNavigateToIrrigation = vi.fn()

    render(
      <SoilMoistureForecastCard
        prediction={mockPredictionItem}
        lang="uz"
        onExplain={handleExplain}
        onNavigateToIrrigation={handleNavigateToIrrigation}
      />
    )

    expect(screen.getByText('21.5%')).toBeInTheDocument()
    expect(screen.getByText(/oralıq/i)).toBeInTheDocument()
    expect(screen.getByText('88%')).toBeInTheDocument()
    expect(screen.getByText('HIGH')).toBeInTheDocument()

    const explainBtn = screen.getByText(/Batafsil asoslash/i)
    fireEvent.click(explainBtn)
    expect(handleExplain).toHaveBeenCalledWith(mockPredictionItem)

    const actionBtn = screen.getByText(/Sug‘orish rejasini ko‘rish/i)
    fireEvent.click(actionBtn)
    expect(handleNavigateToIrrigation).toHaveBeenCalledWith('field-1')
  })

  it('renders SoilMoistureForecastCard fallback when prediction is missing', () => {
    render(
      <SoilMoistureForecastCard
        prediction={null}
        lang="uz"
        onExplain={vi.fn()}
      />
    )

    expect(screen.getByText(/Prognoz mavjud emas: datchik telemetriyasi yetarli emas/i)).toBeInTheDocument()
  })

  it('renders WaterDemandForecastCard with expected demand and bounds', () => {
    const demandPrediction: PredictionItem = {
      ...mockPredictionItem,
      predictionType: 'WATER_DEMAND_FORECAST',
      predictedValue: 450.0,
      lowerBound: 410.0,
      upperBound: 500.0,
      unit: 'm³',
      riskLevel: 'MEDIUM',
    }

    const handleExplain = vi.fn()
    const handleNavigateToWater = vi.fn()

    render(
      <WaterDemandForecastCard
        prediction={demandPrediction}
        lang="uz"
        onExplain={handleExplain}
        onNavigateToWater={handleNavigateToWater}
      />
    )

    expect(screen.getByText('450 m³')).toBeInTheDocument()
    expect(screen.getByText(/410 – 500 m³/)).toBeInTheDocument()

    const explainBtn = screen.getByText(/Batafsil/i)
    fireEvent.click(explainBtn)
    expect(handleExplain).toHaveBeenCalledWith(demandPrediction)
  })

  it('renders PredictionRiskOverview with hydraulic safety block enforcement', () => {
    const blockedPlanRisk: PredictionItem = {
      ...mockPredictionItem,
      predictionType: 'IRRIGATION_PLAN_RISK',
      riskLevel: 'BLOCKED',
      evidence: 'Hydraulic safety interlock active: HIGH_PRESSURE_SURGE detected on pipeline mainline #2.',
      recommendedAction: 'Gidravlik avariya bartaraf etilmaguncha nasoslarni yoqish taqiqlanadi.',
      actionTargetModule: 'irrigation',
    }

    const waterRisk: PredictionItem = {
      ...mockPredictionItem,
      predictionType: 'WATER_AVAILABILITY_RISK',
      riskLevel: 'HIGH',
      evidence: 'Remaining allocation 200 m³ is insufficient for expected 450 m³ demand.',
      recommendedAction: 'Request water quota transfer or modify irrigation schedule.',
      actionTargetModule: 'water',
    }

    const handleExplain = vi.fn()
    const handleNavigate = vi.fn()

    render(
      <PredictionRiskOverview
        planRisk={blockedPlanRisk}
        waterRisk={waterRisk}
        sensorRisk={null}
        lang="uz"
        onExplain={handleExplain}
        onNavigateToModule={handleNavigate}
      />
    )

    expect(screen.getByText(/GIDRAVLIK BLOKIROVKA/i)).toBeInTheDocument()
    expect(screen.getByText(/HIGH_PRESSURE_SURGE/i)).toBeInTheDocument()
    expect(screen.getByText('HIGH')).toBeInTheDocument()

    const explainBtns = screen.getAllByText(/Asoslash/i)
    fireEvent.click(explainBtns[0])
    expect(handleExplain).toHaveBeenCalled()
  })

  it('renders IrrigationNeedMatrix and filters parcels by search term', () => {
    const handleSelectField = vi.fn()
    const handleNavigateToIrrigation = vi.fn()

    render(
      <IrrigationNeedMatrix
        fields={mockRankedFields}
        lang="uz"
        onSelectField={handleSelectField}
        onNavigateToIrrigation={handleNavigateToIrrigation}
      />
    )

    expect(screen.getByText('Paxtakor 1')).toBeInTheDocument()
    expect(screen.getByText('Bogʻdor 2')).toBeInTheDocument()

    // Test search filter
    const searchInput = screen.getByPlaceholderText(/qidirish/i)
    fireEvent.change(searchInput, { target: { value: 'Bogʻdor' } })

    expect(screen.queryByText('Paxtakor 1')).not.toBeInTheDocument()
    expect(screen.getByText('Bogʻdor 2')).toBeInTheDocument()
  })

  it('renders ForecastHistoryTable with accuracy metric and calibration fallback message', () => {
    // When accuracy is available
    render(
      <ForecastHistoryTable
        historyData={mockForecastHistory}
        lang="uz"
      />
    )

    expect(screen.getByText(/85%/)).toBeInTheDocument()
    expect(screen.getByText('52 %')).toBeInTheDocument()
    expect(screen.getByText('53.2 %')).toBeInTheDocument()

    // When accuracy is null (insufficient samples)
    const insufficientHistory: ForecastHistoryResponse = {
      totalEvaluated: 1,
      verifiedAccurateCount: 1,
      verifiedInaccurateCount: 0,
      pendingCount: 2,
      accuracyPercent: null,
      history: [],
    }

    render(
      <ForecastHistoryTable
        historyData={insufficientHistory}
        lang="uz"
      />
    )

    expect(screen.getByText(/Aniqlik ko‘rsatkichi mavjud emas: statistik kalibratsiya uchun/i)).toBeInTheDocument()
  })

  it('renders PredictionExplanationModal with 5 structured XAI sections', () => {
    const handleClose = vi.fn()
    const handleNavigate = vi.fn()

    render(
      <PredictionExplanationModal
        prediction={mockPredictionItem}
        isOpen={true}
        onClose={handleClose}
        lang="uz"
        onNavigateToModule={handleNavigate}
      />
    )

    expect(screen.getByText(/KUTILAYOTGAN HOLAT \(WHAT\)/i)).toBeInTheDocument()
    expect(screen.getByText(/SABAB VA AGRONOMIK ASOS \(WHY\)/i)).toBeInTheDocument()
    expect(screen.getByText(/MIQDORIY DALILLAR \(EVIDENCE\)/i)).toBeInTheDocument()
    expect(screen.getByText(/FARAZLAR VA CHEKLOVLAR \(ASSUMPTIONS & LIMITATIONS\)/i)).toBeInTheDocument()
    expect(screen.getByText(/TAVSIYA ETILGAN KEYINGI AMAL \(NEXT STEP\)/i)).toBeInTheDocument()

    // Content verification
    expect(screen.getByText(/Regression velocity: -2.8%\/day over 24h/i)).toBeInTheDocument()
    expect(screen.getByText(/FAO-56 standard daily ETc 5.2 mm\/day/i)).toBeInTheDocument()

    // Test Next Step button
    const actionBtn = screen.getByText(/Operatsion bo‘limga o‘tish/i)
    fireEvent.click(actionBtn)
    expect(handleNavigate).toHaveBeenCalledWith('irrigation', 'field-1')
  })

  it('renders PredictionsPage full workspace with data from mock APIs', async () => {
    vi.spyOn(predictionApiModule, 'getPredictionOverview').mockResolvedValue(mockOverviewData)
    vi.spyOn(predictionApiModule, 'getIrrigationNeedRanking').mockResolvedValue(mockRankingResponse)
    vi.spyOn(predictionApiModule, 'getEmergingRisks').mockResolvedValue([mockPredictionItem])
    vi.spyOn(predictionApiModule, 'getForecastHistory').mockResolvedValue(mockForecastHistory)

    render(
      <QueryClientProvider client={queryClient}>
        <PredictionsPage lang="uz" onNavigate={vi.fn()} />
      </QueryClientProvider>
    )

    await waitFor(() => {
      expect(screen.getByText(/Prediktiv Ferma Intellekti va Prognozlash/i)).toBeInTheDocument()
    })

    // Check presence of overview tabs
    expect(screen.getAllByText(/Sug‘orish Ehtiyoji Reytingi/i).length).toBeGreaterThan(0)
    expect(screen.getByRole('button', { name: /Prognozlar Tarixi va Aniqlik/i })).toBeInTheDocument()

    // Check that field ranking table is rendered
    expect(screen.getByText('Paxtakor 1')).toBeInTheDocument()
  })
})
