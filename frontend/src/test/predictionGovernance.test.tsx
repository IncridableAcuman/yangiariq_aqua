import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import {
  GovernanceOverviewCard,
  MethodVersionTable,
  CalibrationProposalList,
  BacktestComparisonModal,
  RollbackModal,
  GovernanceAuditTrailTable,
  PredictionTypeEvaluationCard,
  FieldEvaluationTable,
} from '@/features/prediction/components'
import { PredictiveModelHealthCard } from '@/features/dashboard/components'
import { PredictionsPage } from '@/pages/predictions/PredictionsPage'
import {
  CalibrationProposal,
  CalibrationProposalDetailResponse,
  GovernanceAuditLog,
  GovernanceOverview,
  PredictionMethodVersion,
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

describe('Milestone 08.9: Adaptive Intelligence, Model Governance & Controlled Calibration Suite', () => {
  let queryClient: QueryClient

  const mockGovernanceOverview: GovernanceOverview = {
    activeVersionsCount: 4,
    candidateVersionsCount: 2,
    openProposalsCount: 1,
    healthyModelsCount: 3,
    watchModelsCount: 1,
    degradedModelsCount: 0,
    performanceDriftDetected: true,
    driftSummary: 'Soil moisture forecast MAE increased by 14% across loam parcels in past 14 days.',
    activeVersions: [],
    openProposals: [],
    recentAuditLogs: [],
  }

  const mockVersions: PredictionMethodVersion[] = [
    {
      id: 'ver-1',
      predictionType: 'SOIL_MOISTURE_FORECAST',
      versionTag: 'SOIL_MOISTURE_TREND_V1',
      status: 'ACTIVE',
      description: 'Production baseline regression model',
      configurationJson: '{"trendWindowHours":24,"smoothingAlpha":0.30}',
      isImmutable: true,
      createdAt: '2026-10-01T00:00:00Z',
      activatedAt: '2026-10-01T00:00:00Z',
      retiredAt: null,
      createdBy: 'SYSTEM_INIT',
      activatedBy: 'ADMIN',
    },
    {
      id: 'ver-2',
      predictionType: 'SOIL_MOISTURE_FORECAST',
      versionTag: 'SOIL_MOISTURE_TREND_V2',
      status: 'CANDIDATE',
      description: 'Calibrated candidate with lower smoothing alpha',
      configurationJson: '{"trendWindowHours":24,"smoothingAlpha":0.25}',
      isImmutable: false,
      createdAt: '2026-11-01T10:00:00Z',
      activatedAt: null,
      retiredAt: null,
      createdBy: 'PROPOSAL_GEN',
      activatedBy: null,
    },
  ]

  const mockProposals: CalibrationProposal[] = [
    {
      id: 'prop-1',
      proposalCode: 'PROP-1001',
      predictionType: 'SOIL_MOISTURE_FORECAST',
      currentVersionTag: 'SOIL_MOISTURE_TREND_V1',
      candidateVersionTag: 'SOIL_MOISTURE_TREND_V2',
      status: 'REVIEW_REQUIRED',
      reason: 'Persistent positive bias (+6.2%) observed in soil moisture readings',
      evidence: 'Historical MAE is 4.10% with systematic overprediction',
      candidateChanges: 'Reduce smoothing alpha from 0.30 to 0.25',
      proposedConfigurationJson: '{"trendWindowHours":24,"smoothingAlpha":0.25}',
      expectedEffect: 'Reduce systematic overprediction bias by ~18%',
      riskLevel: 'LOW',
      createdAt: '2026-11-01T12:00:00Z',
      updatedAt: '2026-11-01T12:00:00Z',
      createdBy: 'AUTO_EVAL_GENERATOR',
      reviewedBy: null,
      reviewedAt: null,
      reviewNotes: null,
    },
  ]

  const mockDetailWithBacktest: CalibrationProposalDetailResponse = {
    proposal: mockProposals[0],
    currentVersion: mockVersions[0],
    candidateVersion: mockVersions[1],
    latestBacktest: {
      id: 'bt-1',
      proposalId: 'prop-1',
      currentVersionTag: 'SOIL_MOISTURE_TREND_V1',
      candidateVersionTag: 'SOIL_MOISTURE_TREND_V2',
      dateRangeFrom: '2026-10-01T00:00:00Z',
      dateRangeTo: '2026-11-01T00:00:00Z',
      sampleSize: 24,
      currentMae: 4.10,
      candidateMae: 3.40,
      maeImprovementPercent: 17.07,
      currentRmse: 4.80,
      candidateRmse: 3.90,
      currentMbe: 6.20,
      candidateMbe: 2.10,
      currentAccuracyPercent: 88.0,
      candidateAccuracyPercent: 91.5,
      currentF1: 0.85,
      candidateF1: 0.89,
      regressionDetected: false,
      regressionDetails: 'No regressions detected across primary and secondary metrics.',
      dataQuality: 'DATA_COMPLETE',
      isComparable: true,
      hasSufficientSample: true,
      executedAt: '2026-11-01T14:00:00Z',
      executedBy: 'ADMIN_OPERATOR',
    },
    comparison: null,
  }

  const mockAuditLogs: GovernanceAuditLog[] = [
    {
      id: 'log-1',
      action: 'VERSION_ACTIVATED',
      predictionType: 'SOIL_MOISTURE_FORECAST',
      versionTag: 'SOIL_MOISTURE_TREND_V1',
      proposalId: null,
      actorId: 'admin-director',
      actorRole: 'ROLE_ADMIN',
      timestamp: '2026-10-01T00:00:00Z',
      reason: 'Initial system baseline activated',
      previousState: 'CANDIDATE',
      newState: 'ACTIVE',
      detailsJson: '{}',
    },
  ]

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    })
    vi.clearAllMocks()
  })

  it('1. GovernanceOverviewCard: renders fleet counts, drift alert, and triggers rollback/analysis', () => {
    const onRollback = vi.fn()
    const onAnalysis = vi.fn()

    render(
      <GovernanceOverviewCard
        overview={mockGovernanceOverview}
        isLoading={false}
        onOpenRollback={onRollback}
        onTriggerAnalysis={onAnalysis}
        isAnalyzing={false}
        lang="en"
      />
    )

    expect(screen.getByText('Active Production Models')).toBeInTheDocument()
    expect(screen.getByText('4')).toBeInTheDocument()
    expect(screen.getByText('Candidate / Shadow Versions')).toBeInTheDocument()
    expect(screen.getByText('2')).toBeInTheDocument()
    expect(screen.getByText('Open Calibration Proposals')).toBeInTheDocument()
    expect(screen.getByText('1')).toBeInTheDocument()

    // Drift alert banner
    expect(screen.getByText('Performance Drift Detected')).toBeInTheDocument()
    expect(screen.getByText(/Soil moisture forecast MAE increased by 14%/)).toBeInTheDocument()

    // Rollback button click
    const rollbackBtn = screen.getByRole('button', { name: /1-Click Rollback/i })
    fireEvent.click(rollbackBtn)
    expect(onRollback).toHaveBeenCalledTimes(1)

    // Trigger analysis click
    const analyzeBtn = screen.getByRole('button', { name: /Adaptive Calibration Analysis/i })
    fireEvent.click(analyzeBtn)
    expect(onAnalysis).toHaveBeenCalledTimes(1)
  })

  it('2. MethodVersionTable: renders immutable lock, status badges, and configuration snapshot', () => {
    const onActivate = vi.fn()

    render(
      <MethodVersionTable
        versions={mockVersions}
        isLoading={false}
        onActivateVersion={onActivate}
        isActivating={false}
        lang="en"
      />
    )

    expect(screen.getAllByText('SOIL MOISTURE FORECAST')[0]).toBeInTheDocument()
    expect(screen.getByText('SOIL_MOISTURE_TREND_V1')).toBeInTheDocument()
    expect(screen.getByText('SOIL_MOISTURE_TREND_V2')).toBeInTheDocument()

    // Immutability status
    expect(screen.getByText('Immutable (Locked)')).toBeInTheDocument()
    expect(screen.getByText('Mutable')).toBeInTheDocument()

    // Open configuration snapshot
    const viewButtons = screen.getAllByRole('button', { name: /View/i })
    fireEvent.click(viewButtons[0])

    expect(screen.getByText('Model Configuration Snapshot')).toBeInTheDocument()
    expect(screen.getByText(/trendWindowHours/)).toBeInTheDocument()

    // Close modal
    fireEvent.click(screen.getByRole('button', { name: /Close/i }))
    expect(screen.queryByText('Model Configuration Snapshot')).not.toBeInTheDocument()
  })

  it('3. CalibrationProposalList: renders proposals with risk badges and selects proposal', () => {
    const onSelect = vi.fn()

    render(
      <CalibrationProposalList
        proposals={mockProposals}
        isLoading={false}
        onSelectProposal={onSelect}
        lang="en"
      />
    )

    expect(screen.getByText('PROP-1001')).toBeInTheDocument()
    expect(screen.getByText('Low Risk')).toBeInTheDocument()
    expect(screen.getByText(/Persistent positive bias/)).toBeInTheDocument()
    expect(screen.getByText(/Reduce systematic overprediction bias/)).toBeInTheDocument()

    const reviewBtn = screen.getByRole('button', { name: /Compare & Review/i })
    fireEvent.click(reviewBtn)
    expect(onSelect).toHaveBeenCalledWith('prop-1')
  })

  it('4. BacktestComparisonModal: renders metrics comparison, clean regression guard, and handles approval', () => {
    const onBacktest = vi.fn()
    const onApprove = vi.fn()
    const onReject = vi.fn()
    const onActivate = vi.fn()
    const onClose = vi.fn()

    render(
      <BacktestComparisonModal
        detail={mockDetailWithBacktest}
        isLoading={false}
        onClose={onClose}
        onRunBacktest={onBacktest}
        isBacktesting={false}
        onApprove={onApprove}
        isApproving={false}
        onReject={onReject}
        isRejecting={false}
        onActivate={onActivate}
        isActivating={false}
        lang="en"
      />
    )

    expect(screen.getByText('Historical Backtest & Candidate Comparison')).toBeInTheDocument()
    expect(screen.getByText('Regression Guard: PASSED —')).toBeInTheDocument()
    expect(screen.getByText('+17.1%')).toBeInTheDocument() // MAE improvement
    expect(screen.getByText('4.10')).toBeInTheDocument() // current MAE
    expect(screen.getByText('3.40')).toBeInTheDocument() // candidate MAE

    // Test Approval Flow
    const approveBtn = screen.getByRole('button', { name: /Approve Proposal/i })
    fireEvent.click(approveBtn)

    // Notes textarea appears
    const confirmApproveBtn = screen.getByRole('button', { name: /Confirm Approval/i })
    expect(confirmApproveBtn).toBeInTheDocument()
    fireEvent.click(confirmApproveBtn)
    expect(onApprove).toHaveBeenCalledWith('prop-1', '')
  })

  it('5. BacktestComparisonModal: flags regression alert when regression is detected', () => {
    const regressedDetail: CalibrationProposalDetailResponse = {
      ...mockDetailWithBacktest,
      latestBacktest: {
        ...mockDetailWithBacktest.latestBacktest!,
        regressionDetected: true,
        regressionDetails: 'Candidate systematically inflates bias from 1.00 to 5.50 units.',
      },
    }

    render(
      <BacktestComparisonModal
        detail={regressedDetail}
        isLoading={false}
        onClose={vi.fn()}
        onRunBacktest={vi.fn()}
        onApprove={vi.fn()}
        onReject={vi.fn()}
        onActivate={vi.fn()}
        lang="en"
      />
    )

    expect(screen.getByText(/WARNING: PERFORMANCE REGRESSION DETECTED/i)).toBeInTheDocument()
    expect(screen.getByText(/Candidate systematically inflates bias/i)).toBeInTheDocument()
  })

  it('6. RollbackModal: executes rollback with target version and mandatory reason', () => {
    const onExecute = vi.fn()
    const onClose = vi.fn()

    render(
      <RollbackModal
        isOpen={true}
        onClose={onClose}
        activeVersions={mockVersions}
        onExecuteRollback={onExecute}
        isRollingBack={false}
        lang="en"
      />
    )

    expect(screen.getByText('Emergency Model Rollback')).toBeInTheDocument()
    expect(screen.getByText(/Rollback immediately deactivates the active production model/i)).toBeInTheDocument()

    // Fill reason
    const textarea = screen.getByPlaceholderText(/Unforeseen negative bias/i)
    fireEvent.change(textarea, { target: { value: 'Unexpected depletion underestimation in parcel 3.' } })

    const confirmBtn = screen.getByRole('button', { name: /Confirm Emergency Rollback/i })
    fireEvent.click(confirmBtn)

    expect(onExecute).toHaveBeenCalledWith(
      'SOIL_MOISTURE_FORECAST',
      'SOIL_MOISTURE_TREND_V1',
      'Unexpected depletion underestimation in parcel 3.'
    )
  })

  it('7. GovernanceAuditTrailTable: displays chronological audit logs', () => {
    render(
      <GovernanceAuditTrailTable
        logs={mockAuditLogs}
        isLoading={false}
        lang="en"
      />
    )

    expect(screen.getByText('Governance & Model Audit Trail')).toBeInTheDocument()
    expect(screen.getByText('admin-director')).toBeInTheDocument()
    expect(screen.getByText('ROLE_ADMIN')).toBeInTheDocument()
    expect(screen.getByText('ACTIVATED')).toBeInTheDocument()
    expect(screen.getByText('Initial system baseline activated')).toBeInTheDocument()
  })

  it('8. PredictionsPage: switches to Governance Tab cleanly', async () => {
    vi.spyOn(predictionApiModule, 'getGovernanceOverview').mockResolvedValue(mockGovernanceOverview)
    vi.spyOn(predictionApiModule, 'getPredictionMethodVersions').mockResolvedValue(mockVersions)
    vi.spyOn(predictionApiModule, 'getCalibrationProposals').mockResolvedValue(mockProposals)
    vi.spyOn(predictionApiModule, 'getGovernanceAuditTrail').mockResolvedValue(mockAuditLogs)

    render(
      <QueryClientProvider client={queryClient}>
        <PredictionsPage
          lang="en"
          onNavigate={vi.fn()}
        />
      </QueryClientProvider>
    )

    const govTabBtn = screen.getByTestId('tab-governance')
    fireEvent.click(govTabBtn)

    await waitFor(() => {
      expect(screen.getByTestId('governance-dashboard-view')).toBeInTheDocument()
    })
  })

  it('9. PredictiveModelHealthCard: renders overview metrics and drift alerts', () => {
    const onOpenGov = vi.fn()
    render(
      <PredictiveModelHealthCard
        predictiveHealth={{
          activeVersions: 4,
          healthy: 3,
          watch: 1,
          candidates: 1,
          degraded: 0,
          driftDetected: true,
        }}
        lang="en"
        onOpenGovernance={onOpenGov}
      />
    )

    expect(screen.getByText('Predictive Health')).toBeInTheDocument()
    expect(screen.getByText(/Warning: Performance drift detected/i)).toBeInTheDocument()

    const openBtn = screen.getByRole('button', { name: /Open Governance/i })
    fireEvent.click(openBtn)
    expect(onOpenGov).toHaveBeenCalledTimes(1)
  })

  it('10. PredictionTypeEvaluationCard and FieldEvaluationTable: handle undefined metrics without throwing', () => {
    // Render FieldEvaluationTable with missing/undefined numerical properties
    render(
      <FieldEvaluationTable
        data={{
          fields: [
            {
              fieldId: 'f-1',
              fieldName: 'Field 1',
              cropType: 'WHEAT',
              totalEvaluations: 5,
              soilMoistureMae: undefined as unknown as number,
              waterDemandMaeM3: undefined as unknown as number,
              irrigationNeedAccuracyPercent: undefined as unknown as number,
              interruptedCount: 0,
              dataQualityStatus: 'DATA_COMPLETE',
              healthStatus: 'HEALTHY',
              hasSufficientData: true,
            },
          ],
          page: 0,
          size: 10,
          totalElements: 1,
          totalPages: 1,
        }}
        currentPage={0}
        onPageChange={vi.fn()}
        isLoading={false}
        lang="en"
        onSelectField={vi.fn()}
      />
    )

    expect(screen.getByText('Field 1')).toBeInTheDocument()

    // Render PredictionTypeEvaluationCard with undefined numerical properties
    render(
      <PredictionTypeEvaluationCard
        performances={[
          {
            predictionType: 'SOIL_MOISTURE_FORECAST',
            displayName: 'Soil Moisture Forecast',
            evaluationMethod: 'Sensor-grounded regression',
            modelVersion: 'SOIL_MOISTURE_TREND_V1',
            sampleSize: 10,
            isSufficientSample: true,
            minSampleSizeRequired: 5,
            modelHealth: 'HEALTHY',
            modelHealthReason: 'Normal operation',
            numericalMetrics: {
              sampleSize: 10,
              validComparisonsCount: 10,
              sufficientSample: true,
              meanAbsoluteError: undefined as unknown as number,
              rootMeanSquaredError: undefined as unknown as number,
              meanAbsolutePercentageError: undefined as unknown as number,
              mapeReliable: true,
              meanBiasError: undefined as unknown as number,
              withinToleranceCount: 8,
              withinTolerancePercentage: undefined as unknown as number,
            },
            classificationMetrics: null,
            confidenceTiers: [],
            qualityImpacts: [],
            insights: [],
          },
        ]}
        lang="en"
      />
    )

    expect(screen.getByText('Soil Moisture Forecast')).toBeInTheDocument()
  })
})
