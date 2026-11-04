import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import {
  ActiveModelComparisonCard,
  ModelCandidatesTable,
  ModelCandidateDetailModal,
  TrainingRunDialog,
  TrainingDatasetsTable,
} from '@/features/prediction/components'
import { ErrorBoundary } from '@/shared/ui/ErrorBoundary'
import {
  ActiveModelComparisonResponse,
  ModelCandidateDto,
  ModelCandidateDetailResponse,
  TrainingDatasetDto,
} from '@/entities/prediction'

describe('Milestone 08.11: Hybrid ML Prediction Engine & Evidence-Based Learning UI Suite', () => {
  const mockActiveComparison: ActiveModelComparisonResponse = {
    predictionType: 'SOIL_MOISTURE_FORECAST',
    activeModelVersion: 'SM-ML-2026.11-001',
    activeModelType: 'REGRESSION_RIDGE_HYBRID',
    governanceStatus: 'ACTIVE',
    baselineModelVersion: 'SM-RULE-2026.01',
    activeMae: 5.2,
    baselineMae: 6.8,
    relativeImprovementPercent: 23.5,
    sampleSize: 45,
    contextHealth: 'HEALTHY',
    dataQualityStatus: 'OPTIMAL',
    isHybridActive: true,
    summaryRationale: 'ML residual hybrid active',
  }

  const mockCandidates: ModelCandidateDto[] = [
    {
      id: 'cand-001',
      predictionType: 'SOIL_MOISTURE_FORECAST',
      versionTag: 'SM-ML-2026.11-001',
      modelType: 'REGRESSION_RIDGE_HYBRID',
      status: 'CANDIDATE',
      datasetSnapshotId: 'ds-001',
      featureSchemaVersion: 'FEAT_SOIL_MOISTURE_V1',
      trainingSampleCount: 60,
      validationSampleCount: 20,
      testSampleCount: 20,
      testMae: 5.2,
      baselineMae: 6.8,
      maeImprovementPercent: 23.5,
      regressionDetected: false,
      artifactHash: 'sha256:abc1234567890abcdef',
      isImmutable: true,
      createdAt: '2026-11-03T10:00:00Z',
      createdBy: 'system',
      recommendationStatus: 'QUALIFIED',
    },
    {
      id: 'cand-002',
      predictionType: 'SOIL_MOISTURE_FORECAST',
      versionTag: 'SM-ML-2026.11-002',
      modelType: 'REGRESSION_RIDGE_HYBRID',
      status: 'RETIRED',
      datasetSnapshotId: 'ds-002',
      featureSchemaVersion: 'FEAT_SOIL_MOISTURE_V1',
      trainingSampleCount: 60,
      validationSampleCount: 20,
      testSampleCount: 20,
      testMae: 7.5,
      baselineMae: 6.8,
      maeImprovementPercent: -10.3,
      regressionDetected: true,
      artifactHash: 'sha256:def9876543210fedcba',
      isImmutable: true,
      createdAt: '2026-11-02T10:00:00Z',
      createdBy: 'system',
      recommendationStatus: 'REJECTED',
    },
  ]

  const mockCandidateDetail: ModelCandidateDetailResponse = {
    ...mockCandidates[0],
    trainingPeriodStart: '2026-08-01T00:00:00Z',
    trainingPeriodEnd: '2026-09-30T00:00:00Z',
    validationPeriodStart: '2026-10-01T00:00:00Z',
    validationPeriodEnd: '2026-10-15T00:00:00Z',
    testPeriodStart: '2026-10-16T00:00:00Z',
    testPeriodEnd: '2026-11-01T00:00:00Z',
    trainingMetricsJson: '{"mae":4.9,"rmse":6.1,"mbe":-0.2}',
    validationMetricsJson: '{"mae":5.1,"rmse":6.3,"mbe":-0.3}',
    testMetricsJson: '{"mae":5.2,"rmse":6.5,"mbe":-0.4}',
    baselineComparisonMetricsJson:
      '{"baselineMae":6.8,"mlMae":5.2,"baselineRmse":8.1,"mlRmse":6.5,"baselineMbe":1.2,"mlMbe":-0.4,"maeImprovementPercent":23.5,"regressionDetected":false}',
    configurationSnapshotJson: '{"l2Penalty":1.0,"standardize":true}',
    featureImportance: {
      past_moisture_trend_24h: 0.28,
      current_soil_moisture: 0.24,
      irrigation_volume_recent_m3: 0.18,
      soil_temperature: 0.12,
      soil_type_code: 0.08,
      crop_type_code: 0.06,
      telemetry_confidence_score: 0.04,
    },
    limitations: 'Residual bounded to +/- 5.0%. Physical soil bounds [0, 100]. Zero actuator authority.',
  }

  const mockDatasets: TrainingDatasetDto[] = [
    {
      id: 'ds-001-snapshot',
      predictionType: 'SOIL_MOISTURE_FORECAST',
      datasetHash: 'sha256:ds1234567890abcdef',
      featureSchemaVersion: 'FEAT_SOIL_MOISTURE_V1',
      periodStart: '2026-08-01T00:00:00Z',
      periodEnd: '2026-11-01T00:00:00Z',
      fieldScope: 'ALL_FIELDS',
      recordCount: 100,
      qualityStatus: 'HIGH_QUALITY',
      createdAt: '2026-11-03T09:30:00Z',
      createdBy: 'system',
    },
  ]

  describe('ActiveModelComparisonCard', () => {
    it('renders comparison metrics and safety interlock principles', () => {
      const onOpenTraining = vi.fn()
      render(
        <ActiveModelComparisonCard
          comparison={mockActiveComparison}
          onOpenTrainingDialog={onOpenTraining}
          lang="uz"
          userRole="ADMIN"
        />
      )

      expect(screen.getByText('SM-ML-2026.11-001')).toBeInTheDocument()
      expect(screen.getByText('REGRESSION_RIDGE_HYBRID')).toBeInTheDocument()
      expect(screen.getByText('5.2%')).toBeInTheDocument()
      expect(screen.getByText(/SM-RULE-2026.01/)).toBeInTheDocument()
      expect(screen.getByText('+23.5%')).toBeInTheDocument()
      expect(screen.getByText('45')).toBeInTheDocument()

      const trainBtn = screen.getByText('Boshqariladigan ML O‘qitishni Boshlash')
      fireEvent.click(trainBtn)
      expect(onOpenTraining).toHaveBeenCalledTimes(1)
    })

    it('renders safely when comparison metrics are null or undefined', () => {
      render(
        <ActiveModelComparisonCard
          comparison={{
            ...mockActiveComparison,
            activeMae: undefined as any,
            baselineMae: undefined as any,
            relativeImprovementPercent: undefined as any,
          }}
          onOpenTrainingDialog={vi.fn()}
          lang="en"
          userRole="FARMER"
        />
      )

      expect(screen.getByText('—')).toBeInTheDocument()
      expect(screen.getByText('Baseline')).toBeInTheDocument()
    })
  })

  describe('ModelCandidatesTable', () => {
    it('renders candidate rows, MAE comparison, and regression status', () => {
      const onSelect = vi.fn()
      const onPromote = vi.fn()

      render(
        <ModelCandidatesTable
          candidates={mockCandidates}
          onSelectCandidate={onSelect}
          onPromoteCandidate={onPromote}
          lang="uz"
          userRole="ADMIN"
        />
      )

      expect(screen.getByText('SM-ML-2026.11-001')).toBeInTheDocument()
      expect(screen.getByText('SM-ML-2026.11-002')).toBeInTheDocument()
      expect(screen.getByText('+23.5%')).toBeInTheDocument()
      expect(screen.getByText('-10.3%')).toBeInTheDocument()
      expect(screen.getByText('Regressiya Aniqlangan')).toBeInTheDocument()

      const detailBtns = screen.getAllByText('Dalillarni Ko‘rish')
      fireEvent.click(detailBtns[0])
      expect(onSelect).toHaveBeenCalledWith(mockCandidates[0])
    })

    it('hides promotion buttons for FARMER role', () => {
      render(
        <ModelCandidatesTable
          candidates={mockCandidates}
          onSelectCandidate={vi.fn()}
          onPromoteCandidate={vi.fn()}
          lang="en"
          userRole="FARMER"
        />
      )

      expect(screen.queryByText('Shadow Rollout')).not.toBeInTheDocument()
      expect(screen.queryByText('Canary Rollout')).not.toBeInTheDocument()
      expect(screen.queryByText('Activate')).not.toBeInTheDocument()
    })
  })

  describe('TrainingRunDialog', () => {
    it('renders preflight checks and handles insufficient data response', async () => {
      const mockTraining = vi.fn().mockResolvedValue({
        status: 'INSUFFICIENT_TRAINING_DATA',
        message: 'Insufficient ground-truth samples (found: 12, required: 30)',
        sampleCount: 12,
        minRequiredSamples: 30,
        predictionType: 'SOIL_MOISTURE_FORECAST',
        datasetId: 'ds-001',
        datasetHash: 'sha256:abc',
        regressionDetected: false,
      })

      render(
        <TrainingRunDialog
          isOpen={true}
          onClose={vi.fn()}
          onStartTraining={mockTraining}
          isTraining={false}
          lang="en"
        />
      )

      expect(screen.getByText('Controlled ML Model Training Run')).toBeInTheDocument()
      expect(screen.getByText('Scientific Preflight Rules & Gates')).toBeInTheDocument()

      const startBtn = screen.getByText('Start Controlled Training')
      fireEvent.click(startBtn)

      await waitFor(() => {
        expect(screen.getByTestId('insufficient-data-alert')).toBeInTheDocument()
      })
      expect(screen.getByText('Insufficient Training Data')).toBeInTheDocument()
      expect(screen.getByText('12')).toBeInTheDocument()
      expect(screen.getByText('30')).toBeInTheDocument()
    })

    it('displays successful training result with metrics comparison', async () => {
      const mockTraining = vi.fn().mockResolvedValue({
        status: 'SUCCESS',
        candidateId: 'cand-003',
        versionTag: 'SM-ML-2026.11-003',
        sampleCount: 80,
        minRequiredSamples: 30,
        candidateMae: 5.1,
        baselineMae: 6.8,
        improvementPercent: 25.0,
        message: 'Candidate trained successfully',
        predictionType: 'SOIL_MOISTURE_FORECAST',
        datasetId: 'ds-003',
        datasetHash: 'sha256:xyz',
        regressionDetected: false,
      })

      render(
        <TrainingRunDialog
          isOpen={true}
          onClose={vi.fn()}
          onStartTraining={mockTraining}
          isTraining={false}
          lang="en"
        />
      )

      const startBtn = screen.getByText('Start Controlled Training')
      fireEvent.click(startBtn)

      await waitFor(() => {
        expect(screen.getByTestId('training-success-alert')).toBeInTheDocument()
      })
      expect(screen.getByText('5.10%')).toBeInTheDocument()
      expect(screen.getByText('6.80%')).toBeInTheDocument()
      expect(screen.getByText('+25.0%')).toBeInTheDocument()
    })
  })

  describe('ModelCandidateDetailModal', () => {
    it('renders empirical evidence, feature weights, and governance constraints', () => {
      render(
        <ModelCandidateDetailModal
          candidate={mockCandidateDetail}
          isOpen={true}
          onClose={vi.fn()}
          lang="uz"
          userRole="ADMIN"
        />
      )

      expect(screen.getByText('SM-ML-2026.11-001')).toBeInTheDocument()
      expect(screen.getByText('60 samples')).toBeInTheDocument()
      expect(screen.getByText('past_moisture_trend_24h')).toBeInTheDocument()
      expect(screen.getByText('current_soil_moisture')).toBeInTheDocument()
      expect(screen.getByText('0.280')).toBeInTheDocument()
      expect(screen.getByText('QUALIFIED')).toBeInTheDocument()
    })
  })

  describe('TrainingDatasetsTable', () => {
    it('renders snapshot records and dataset hash', () => {
      render(
        <TrainingDatasetsTable
          datasets={mockDatasets}
          lang="uz"
        />
      )

      expect(screen.getByText('ds-001-s...')).toBeInTheDocument()
      expect(screen.getByText('FEAT_SOIL_MOISTURE_V1')).toBeInTheDocument()
      expect(screen.getByText('100 obs')).toBeInTheDocument()
      expect(screen.getByText('VALID')).toBeInTheDocument()
    })
  })

  describe('ErrorBoundary', () => {
    it('catches render errors and displays fallback UI without crashing', () => {
      const BadComponent = () => {
        throw new Error('Test intentional crash')
      }

      const spy = vi.spyOn(console, 'error').mockImplementation(() => {})

      render(
        <ErrorBoundary fallbackTitle="Custom Fallback Title">
          <BadComponent />
        </ErrorBoundary>
      )

      expect(screen.getByText('Custom Fallback Title')).toBeInTheDocument()
      expect(screen.getByText('Test intentional crash')).toBeInTheDocument()
      expect(screen.getByText('Qayta urinish')).toBeInTheDocument()

      spy.mockRestore()
    })
  })
})

