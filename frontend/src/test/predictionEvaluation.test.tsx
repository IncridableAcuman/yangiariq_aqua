import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import {
  PredictionEvaluationOverviewCard,
  PredictionTypeEvaluationCard,
  ConfidenceCalibrationCard,
  DataQualityImpactCard,
  EvaluationInsightsList,
  HumanDecisionImpactCard,
  FieldEvaluationTable,
  EvaluationHistoryTable,
} from '@/features/prediction/components'
import { PredictionsPage } from '@/pages/predictions/PredictionsPage'
import {
  FieldEvaluationResponse,
  PredictionEvaluationDetail,
  PredictionEvaluationOverview,
  PredictionTypePerformance,
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
      ],
    },
    isLoading: false,
  }),
}))

describe('Milestone 08.8: Predictive Model Evaluation & Continuous Intelligence Suite', () => {
  let queryClient: QueryClient

  const mockOverviewSufficient: PredictionEvaluationOverview = {
    totalPredictionsGenerated: 45,
    totalEvaluated: 32,
    pendingCount: 8,
    insufficientDataCount: 3,
    interruptedBySafetyCount: 2,
    evaluationCoveragePercentage: 71.1,
    overallModelHealth: 'HEALTHY',
    overallHealthReason: 'All predictive models operating within authoritative agronomic error tolerances.',
    performanceDriftDetected: false,
    driftSummary: null,
    typePerformances: [],
    systemInsights: [],
    humanDecisionImpacts: [],
    evaluatedAt: '2026-11-02T12:00:00Z',
  }

  const mockOverviewInsufficient: PredictionEvaluationOverview = {
    totalPredictionsGenerated: 12,
    totalEvaluated: 4,
    pendingCount: 8,
    insufficientDataCount: 0,
    interruptedBySafetyCount: 0,
    evaluationCoveragePercentage: 33.3,
    overallModelHealth: 'INSUFFICIENT_DATA',
    overallHealthReason: 'Sample size below minimum evaluation threshold (n < 10).',
    performanceDriftDetected: false,
    driftSummary: null,
    typePerformances: [],
    systemInsights: [],
    humanDecisionImpacts: [],
    evaluatedAt: '2026-11-02T12:00:00Z',
  }

  const mockOverviewDrift: PredictionEvaluationOverview = {
    ...mockOverviewSufficient,
    overallModelHealth: 'DEGRADED',
    performanceDriftDetected: true,
    driftSummary: 'Soil moisture MAE has doubled from 2.1% to 4.8% over the last 7 days.',
  }

  const mockTypePerformance: PredictionTypePerformance = {
    predictionType: 'SOIL_MOISTURE_FORECAST',
    displayName: 'Soil Moisture Forecast',
    evaluationMethod: 'TELEMETRY_OBSERVATION_WINDOW',
    modelVersion: 'DETERMINISTIC_MAD_V1',
    sampleSize: 18,
    isSufficientSample: true,
    minSampleSizeRequired: 10,
    modelHealth: 'HEALTHY',
    modelHealthReason: 'MAE 2.1% is below tolerance limit 4.0%',
    numericalMetrics: {
      sampleSize: 18,
      validComparisonsCount: 18,
      sufficientSample: true,
      meanAbsoluteError: 2.15,
      rootMeanSquaredError: 2.74,
      meanAbsolutePercentageError: 8.4,
      mapeReliable: true,
      meanBiasError: 0.85,
      withinToleranceCount: 16,
      withinTolerancePercentage: 88.9,
    },
    classificationMetrics: null,
    confidenceTiers: [
      { tierName: 'HIGH', sampleSize: 12, sufficientSample: true, meanAbsoluteError: 1.8, accuracyPercentage: 91.7, failureRatePercentage: 8.3 },
      { tierName: 'MEDIUM', sampleSize: 4, sufficientSample: false, meanAbsoluteError: 2.9, accuracyPercentage: 75.0, failureRatePercentage: 25.0 },
      { tierName: 'LOW', sampleSize: 2, sufficientSample: false, meanAbsoluteError: 4.1, accuracyPercentage: 50.0, failureRatePercentage: 50.0 },
    ],
    qualityImpacts: [
      { qualityCategory: 'DATA_COMPLETE', sampleSize: 14, meanAbsoluteError: 1.9, accuracyPercentage: 92.8, unverifiablePercentage: 0 },
      { qualityCategory: 'DATA_PARTIAL', sampleSize: 3, meanAbsoluteError: 3.4, accuracyPercentage: 66.7, unverifiablePercentage: 0 },
      { qualityCategory: 'DATA_INSUFFICIENT', sampleSize: 1, meanAbsoluteError: null, accuracyPercentage: null, unverifiablePercentage: 100 },
    ],
    insights: [],
  }

  const mockIrrigationNeedPerformance: PredictionTypePerformance = {
    predictionType: 'IRRIGATION_NEED_FORECAST',
    displayName: 'Irrigation Need Forecast',
    evaluationMethod: 'CONFUSION_MATRIX_MATCHING',
    modelVersion: 'DETERMINISTIC_MAD_V1',
    sampleSize: 20,
    isSufficientSample: true,
    minSampleSizeRequired: 10,
    modelHealth: 'HEALTHY',
    modelHealthReason: 'F1 Score 0.86 satisfies agronomic baseline',
    numericalMetrics: null,
    classificationMetrics: {
      sampleSize: 20,
      evaluatedCount: 20,
      sufficientSample: true,
      truePositives: 10,
      trueNegatives: 7,
      falsePositives: 1,
      falseNegatives: 1,
      interruptedBySafetyCount: 1,
      accuracyPercentage: 85.0,
      precisionPercentage: 90.9,
      recallPercentage: 90.9,
      f1Score: 0.91,
    },
    confidenceTiers: [],
    qualityImpacts: [],
    insights: [],
  }

  const mockDetailEvaluation: PredictionEvaluationDetail = {
    id: 'eval-1',
    predictionId: 'pred-101',
    fieldId: 'field-1',
    fieldName: 'Paxtakor 1',
    predictionType: 'SOIL_MOISTURE_FORECAST',
    horizon: 'NEXT_48_HOURS',
    modelVersion: 'DETERMINISTIC_MAD_V1',
    generatedAt: '2026-11-01T08:00:00Z',
    targetTime: '2026-11-03T08:00:00Z',
    evaluatedAt: '2026-11-03T08:15:00Z',
    actualObservedAt: '2026-11-03T08:05:00Z',
    predictedValue: 24.5,
    lowerBound: 21.0,
    upperBound: 27.0,
    actualValue: 23.8,
    absoluteError: 0.7,
    relativeErrorPercent: 2.94,
    directionalError: 0.7,
    withinBounds: true,
    predictedClass: null,
    actualClass: null,
    classificationResult: 'NOT_APPLICABLE',
    recommendedValue: null,
    approvedValue: null,
    operationalEventId: null,
    operationalNotes: null,
    safetyInterlockTriggered: false,
    confidencePercent: 88,
    evaluationStatus: 'EVALUATED',
    failureReason: 'NONE',
    dataQuality: 'DATA_COMPLETE',
    evaluationMethod: 'TELEMETRY_OBSERVATION_WINDOW',
    notes: 'Observed within ±3h tolerance window',
  }

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    })
    vi.clearAllMocks()
  })

  it('renders PredictionEvaluationOverviewCard with honest insufficient data banner when n < 10', () => {
    render(
      <QueryClientProvider client={queryClient}>
        <PredictionEvaluationOverviewCard
          overview={mockOverviewInsufficient}
          lang="uz"
        />
      </QueryClientProvider>
    )

    expect(screen.getByTestId('prediction-evaluation-overview')).toBeInTheDocument()
    expect(screen.getByTestId('insufficient-data-banner')).toBeInTheDocument()
    expect(screen.getByText(/Statistik baholash uchun yetarli maʼlumot mavjud emas/i)).toBeInTheDocument()
    expect(screen.getAllByText('INSUFFICIENT DATA').length).toBeGreaterThan(0)
  })

  it('renders PredictionEvaluationOverviewCard with HEALTHY status and triggers evaluation button', () => {
    const onTrigger = vi.fn()
    render(
      <QueryClientProvider client={queryClient}>
        <PredictionEvaluationOverviewCard
          overview={mockOverviewSufficient}
          onTriggerEvaluation={onTrigger}
          lang="uz"
        />
      </QueryClientProvider>
    )

    expect(screen.queryByTestId('insufficient-data-banner')).not.toBeInTheDocument()
    expect(screen.getByText('HEALTHY')).toBeInTheDocument()
    expect(screen.getByText('32')).toBeInTheDocument()

    const triggerBtn = screen.getByTestId('trigger-evaluation-button')
    fireEvent.click(triggerBtn)
    expect(onTrigger).toHaveBeenCalledTimes(1)
  })

  it('renders drift alert banner when performance drift is detected', () => {
    render(
      <QueryClientProvider client={queryClient}>
        <PredictionEvaluationOverviewCard
          overview={mockOverviewDrift}
          lang="en"
        />
      </QueryClientProvider>
    )

    expect(screen.getByTestId('drift-alert-banner')).toBeInTheDocument()
    expect(screen.getByText(/Performance Drift Detected/i)).toBeInTheDocument()
    expect(screen.getByText(/Soil moisture MAE has doubled/i)).toBeInTheDocument()
  })

  it('renders PredictionTypeEvaluationCard with numerical regression metrics and tolerance bounds', () => {
    render(
      <QueryClientProvider client={queryClient}>
        <PredictionTypeEvaluationCard
          performances={[mockTypePerformance]}
          lang="en"
        />
      </QueryClientProvider>
    )

    expect(screen.getByTestId('prediction-type-evaluation-card')).toBeInTheDocument()
    expect(screen.getByText('2.15')).toBeInTheDocument() // MAE
    expect(screen.getByText('2.74')).toBeInTheDocument() // RMSE
    expect(screen.getByText('8.4%')).toBeInTheDocument() // MAPE
    expect(screen.getByText('+0.85')).toBeInTheDocument() // MBE
    expect(screen.getByText('88.9%')).toBeInTheDocument() // Within tolerance
  })

  it('renders PredictionTypeEvaluationCard with confusion matrix and interrupted by safety block', () => {
    render(
      <QueryClientProvider client={queryClient}>
        <PredictionTypeEvaluationCard
          performances={[mockIrrigationNeedPerformance]}
          lang="uz"
        />
      </QueryClientProvider>
    )

    expect(screen.getByTestId('tp-count')).toHaveTextContent('10')
    expect(screen.getByTestId('tn-count')).toHaveTextContent('7')
    expect(screen.getByTestId('fp-count')).toHaveTextContent('1')
    expect(screen.getByTestId('fn-count')).toHaveTextContent('1')
    expect(screen.getByTestId('interrupted-count')).toHaveTextContent('1')
    expect(screen.getByText('85.0%')).toBeInTheDocument() // Accuracy
    expect(screen.getByText('0.91')).toBeInTheDocument() // F1
    expect(screen.getByText(/Xavfsizlik Tufayli To‘xtatilgan/i)).toBeInTheDocument()
  })

  it('renders ConfidenceCalibrationCard and detects calibration state', () => {
    render(
      <QueryClientProvider client={queryClient}>
        <ConfidenceCalibrationCard
          confidenceTiers={mockTypePerformance.confidenceTiers}
          lang="en"
        />
      </QueryClientProvider>
    )

    expect(screen.getByTestId('confidence-calibration-card')).toBeInTheDocument()
    expect(screen.getByTestId('confidence-tier-high')).toHaveTextContent('HIGH CONFIDENCE')
    expect(screen.getByTestId('confidence-tier-medium')).toHaveTextContent('MEDIUM CONFIDENCE')
    expect(screen.getByText(/Well calibrated/i)).toBeInTheDocument()
  })

  it('renders DataQualityImpactCard showing complete vs partial vs insufficient impact', () => {
    render(
      <QueryClientProvider client={queryClient}>
        <DataQualityImpactCard
          qualityImpacts={mockTypePerformance.qualityImpacts}
          lang="en"
        />
      </QueryClientProvider>
    )

    expect(screen.getByTestId('data-quality-impact-card')).toBeInTheDocument()
    expect(screen.getByText('DATA_COMPLETE')).toBeInTheDocument()
    expect(screen.getByText('DATA_PARTIAL')).toBeInTheDocument()
    expect(screen.getByText('DATA_INSUFFICIENT')).toBeInTheDocument()
  })

  it('renders EvaluationInsightsList with deterministic recommendations', () => {
    const mockInsight = {
      id: 'ins-1',
      category: 'BIAS',
      severity: 'WARNING' as const,
      title: 'Persistent Over-Prediction Bias',
      description: 'Forecasts systematically overestimated actual soil moisture by +0.85%.',
      evidence: 'MBE +0.85%, n = 18',
      recommendation: 'Review crop coefficient (Kc) and soil infiltration rate.',
      sourceModule: 'predictions',
      actionTarget: 'settings',
    }

    render(
      <QueryClientProvider client={queryClient}>
        <EvaluationInsightsList
          insights={[mockInsight]}
          lang="en"
        />
      </QueryClientProvider>
    )

    expect(screen.getByTestId('evaluation-insights-list')).toBeInTheDocument()
    expect(screen.getByText('Persistent Over-Prediction Bias')).toBeInTheDocument()
    expect(screen.getByText(/Review crop coefficient/i)).toBeInTheDocument()
  })

  it('renders HumanDecisionImpactCard with approved, modified, rejected counts', () => {
    const mockHumanImpact = {
      predictionType: 'WATER_DEMAND_FORECAST' as const,
      totalPredictionsCount: 15,
      approvedDirectlyCount: 10,
      modifiedByHumanCount: 4,
      rejectedByHumanCount: 1,
      directApprovalAccuracy: 92.5,
      humanOverrideImprovement: 'Operator manual modifications reduced water deficit risks by 8%.',
    }

    render(
      <QueryClientProvider client={queryClient}>
        <HumanDecisionImpactCard
          humanImpacts={[mockHumanImpact]}
          lang="uz"
        />
      </QueryClientProvider>
    )

    expect(screen.getByTestId('human-decision-impact-card')).toBeInTheDocument()
    expect(screen.getByText('10')).toBeInTheDocument()
    expect(screen.getByText('4')).toBeInTheDocument()
    expect(screen.getByText('1')).toBeInTheDocument()
    expect(screen.getByText('92.5%')).toBeInTheDocument()
  })

  it('renders FieldEvaluationTable and opens EvaluationDetailModal on audit click in EvaluationHistoryTable', () => {
    const mockFieldResponse: FieldEvaluationResponse = {
      fields: [
        {
          fieldId: 'field-1',
          fieldName: 'Paxtakor 1',
          cropType: 'COTTON',
          totalEvaluations: 12,
          soilMoistureMae: 2.1,
          waterDemandMaeM3: 15.4,
          irrigationNeedAccuracyPercent: 90.0,
          interruptedCount: 1,
          dataQualityStatus: 'DATA_COMPLETE',
          healthStatus: 'HEALTHY',
          hasSufficientData: true,
        },
      ],
      page: 0,
      size: 10,
      totalElements: 1,
      totalPages: 1,
    }

    render(
      <QueryClientProvider client={queryClient}>
        <FieldEvaluationTable
          data={mockFieldResponse}
          currentPage={0}
          onPageChange={vi.fn()}
          lang="en"
        />
      </QueryClientProvider>
    )

    expect(screen.getByTestId('field-evaluation-table')).toBeInTheDocument()
    expect(screen.getByText('Paxtakor 1')).toBeInTheDocument()
    expect(screen.getByText('2.1%')).toBeInTheDocument()
    expect(screen.getByText('15.4 m³')).toBeInTheDocument()
  })

  it('renders EvaluationHistoryTable and opens detail modal on audit click', async () => {
    const mockHistoryResponse = {
      evaluations: [mockDetailEvaluation],
      page: 0,
      size: 10,
      totalElements: 1,
      totalPages: 1,
    }

    render(
      <QueryClientProvider client={queryClient}>
        <EvaluationHistoryTable
          data={mockHistoryResponse}
          currentPage={0}
          onPageChange={vi.fn()}
          statusFilter=""
          onStatusFilterChange={vi.fn()}
          typeFilter=""
          onTypeFilterChange={vi.fn()}
          lang="en"
        />
      </QueryClientProvider>
    )

    expect(screen.getByTestId('evaluation-history-table')).toBeInTheDocument()
    expect(screen.getByText('Paxtakor 1')).toBeInTheDocument()

    // Click Audit Detail button
    const auditBtn = screen.getByTestId('view-evaluation-audit-button')
    fireEvent.click(auditBtn)

    await waitFor(() => {
      expect(screen.getByTestId('evaluation-detail-modal')).toBeInTheDocument()
      expect(screen.getByText(/Prediction Evaluation Audit/i)).toBeInTheDocument()
    })
  })

  it('switches to Model Evaluation tab in PredictionsPage and loads evaluation view', async () => {
    vi.spyOn(predictionApiModule, 'getPredictionEvaluationOverview').mockResolvedValue(mockOverviewSufficient)
    vi.spyOn(predictionApiModule, 'getPredictionEvaluationByType').mockResolvedValue([mockTypePerformance])

    render(
      <QueryClientProvider client={queryClient}>
        <PredictionsPage lang="uz" onNavigate={vi.fn()} />
      </QueryClientProvider>
    )

    // Tab button should be visible
    const evalTab = screen.getByTestId('tab-evaluation')
    expect(evalTab).toBeInTheDocument()

    // Click tab
    fireEvent.click(evalTab)

    await waitFor(() => {
      expect(screen.getByTestId('evaluation-dashboard-view')).toBeInTheDocument()
    })
  })
})
