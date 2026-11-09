import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import React from 'react'
import { PilotPage } from '@/pages/pilot'
import {
  PilotProgramsSummaryCards,
  PilotProgramsTable,
  CreatePilotDialog,
  PilotDetailsModal,
  PilotReadinessPanel,
  SubmitGroundTruthDialog,
  ReviewGroundTruthDialog,
  GroundTruthValidationView,
  PredictionValidationView,
  OperatorFeedbackView,
  PilotEvaluationReportView,
} from '@/features/pilot'
import {
  PilotProgram,
  PilotReadinessAssessment,
  GroundTruthMeasurement,
  PredictionValidationRecord,
  OperatorFeedback,
  PilotEvaluationReport,
  PilotOperationalMonitoring,
} from '@/entities/pilot'
import * as clientModule from '@/shared/api/client'

// Mock AuthContext
vi.mock('@/features/auth/context/AuthContext', () => ({
  useAuth: () => ({
    user: {
      id: 'usr-operator-1',
      fullName: 'Yangiariq Agronomist',
      role: 'OPERATOR',
    },
    isAuthenticated: true,
  }),
}))

const mockPilots: PilotProgram[] = [
  {
    id: 'pilot-1',
    code: 'PILOT-2026-YANGIARIQ-COTTON',
    name: 'Yangiariq High-Efficiency Drip Cotton Pilot',
    description: 'Ground-truth validation of tensiometer arrays vs hydraulic surrogate model',
    geographicScope: 'Yangiariq District, Sector 4',
    startDate: '2026-04-01',
    endDate: '2026-10-31',
    organization: 'Yangiariq Water Users Association',
    ownerId: 'usr-operator-1',
    operationalObjectives: 'Evaluate 24h soil moisture forecast accuracy and canal intake reliability',
    status: 'ACTIVE',
    approvedBy: 'admin-1',
    approvedAt: '2026-03-25T10:00:00Z',
    createdAt: '2026-03-20T10:00:00Z',
    updatedAt: '2026-03-25T10:00:00Z',
  },
  {
    id: 'pilot-2',
    code: 'PILOT-2026-WHEAT-RUNOFF',
    name: 'Winter Wheat Canal Runoff Pilot',
    description: 'Furrow irrigation volumetric distribution check',
    geographicScope: 'Yangiariq District, Sector 1',
    startDate: '2026-03-01',
    endDate: '2026-06-30',
    organization: 'Yangiariq WUA',
    ownerId: 'usr-operator-2',
    operationalObjectives: 'Measure tailwater runoff volume and compare against canal gate schedule',
    status: 'DRAFT',
    createdAt: '2026-02-15T10:00:00Z',
    updatedAt: '2026-02-15T10:00:00Z',
  },
]

const mockAssessment: PilotReadinessAssessment = {
  id: 'assess-1',
  pilotId: 'pilot-1',
  overallStatus: 'PASS',
  infrastructureStatus: 'PASS',
  dataStatus: 'PASS',
  operationalStatus: 'PASS',
  governanceStatus: 'PASS',
  checks: [
    {
      category: 'INFRASTRUCTURE',
      name: 'Sensors / Metering Baseline',
      result: 'PASS',
      reason: '2 physical calibrated flow meters and tensiometers registered',
      isMandatory: true,
    },
    {
      category: 'DATA',
      name: 'Telemetry Ingestion Pathway',
      result: 'PASS',
      reason: 'Active data source connected with verified provenance',
      isMandatory: true,
    },
    {
      category: 'OPERATIONAL',
      name: 'Independent Verifier Roster',
      result: 'PASS',
      reason: 'At least one independent verifier enrolled under four-eyes protocol',
      isMandatory: true,
    },
    {
      category: 'GOVERNANCE',
      name: 'Formal Stakeholder Approval',
      result: 'PASS',
      reason: 'Program officially approved by administrative governance',
      isMandatory: true,
    },
  ],
  assessedBy: 'usr-operator-1',
  assessedAt: '2026-03-25T11:00:00Z',
}

const mockMeasurements: GroundTruthMeasurement[] = [
  {
    id: 'gt-1',
    pilotId: 'pilot-1',
    fieldId: 'field-101',
    measurementType: 'SOIL_MOISTURE',
    measuredValue: 24.5,
    canonicalUnit: 'PERCENTAGE',
    measuredAt: '2026-05-10T08:00:00Z',
    submittedAt: '2026-05-10T08:30:00Z',
    collectionMethod: 'IN_SITU_TENSIOMETER',
    measurementSource: 'OPERATOR_FIELD_KIT',
    submittedBy: 'usr-operator-1',
    verificationStatus: 'VERIFIED',
    verifierId: 'usr-agronomist-2',
    evidenceReference: 'https://storage.aquaai.uz/evidence/core-sample-101.jpg',
    verifiedAt: '2026-05-10T10:00:00Z',
  },
  {
    id: 'gt-2',
    pilotId: 'pilot-1',
    fieldId: 'field-102',
    measurementType: 'WATER_FLOW_RATE',
    measuredValue: 12.8,
    canonicalUnit: 'LITERS_PER_SECOND',
    measuredAt: '2026-05-11T09:00:00Z',
    submittedAt: '2026-05-11T09:15:00Z',
    collectionMethod: 'ULTRASONIC_FLOW_METER',
    measurementSource: 'INDEPENDENT_AUDIT',
    submittedBy: 'usr-farmer-3',
    verificationStatus: 'PENDING_REVIEW',
  },
]

const mockValidations: PredictionValidationRecord[] = [
  {
    id: 'pv-1',
    pilotId: 'pilot-1',
    fieldId: 'field-101',
    predictionId: 'pred-701',
    modelName: 'SOIL_MOISTURE_HYDRAULIC_SURROGATE',
    modelVersion: 'v2.4.1',
    predictedAt: '2026-05-09T08:00:00Z',
    predictionHorizonHours: 24,
    predictedValue: 26.3,
    actualValue: 24.5,
    groundTruthId: 'gt-1',
    absoluteError: 1.8,
    relativeErrorPercentage: 7.35,
    evaluationStatus: 'VALIDATED',
    evaluatedAt: '2026-05-10T10:05:00Z',
  },
]

const mockFeedback: OperatorFeedback[] = [
  {
    id: 'fb-1',
    pilotId: 'pilot-1',
    farmId: 'farm-1',
    fieldId: 'field-101',
    category: 'DATA_QUALITY',
    title: 'Soil temperature probe calibration offset',
    description: 'Thermistor reading 3 degrees higher than portable reference probe at 20cm depth',
    status: 'UNDER_REVIEW',
    submittedBy: 'usr-operator-1',
    createdAt: '2026-05-08T09:00:00Z',
    updatedAt: '2026-05-08T09:00:00Z',
  },
]

const mockReport: PilotEvaluationReport = {
  id: 'rep-1',
  pilotId: 'pilot-1',
  periodStart: '2026-04-01T00:00:00Z',
  periodEnd: '2026-05-15T00:00:00Z',
  dataPerformanceJson: JSON.stringify({
    totalGroundTruthCount: 15,
    verifiedCount: 14,
    pendingCount: 1,
    verificationRate: 0.933,
  }),
  predictionPerformanceJson: JSON.stringify({
    totalValidationsCount: 12,
    validatedCount: 12,
    meanAbsoluteError: 1.45,
    meanRelativeErrorPercentage: 5.8,
  }),
  operationalPerformanceJson: JSON.stringify({
    totalFeedbackCount: 3,
    resolvedCount: 2,
    actionRequiredCount: 1,
    operationalGateStatus: 'PASS',
  }),
  waterOutcomesJson: JSON.stringify({
    totalMeasuredVolumeM3: 45200,
    baselineMultiSeasonStatus: 'PENDING_MULTI_SEASON_EVIDENCE',
  }),
  limitationsText: 'Hardware gateway operating under hybrid cellular-lora uplink',
  recommendationsText: 'Expand four-eyes verification pool across adjacent canal branches',
  createdBy: 'usr-operator-1',
  createdAt: '2026-05-15T14:00:00Z',
}

const mockMonitoring: PilotOperationalMonitoring = {
  pilotId: 'pilot-1',
  pilotName: 'Yangiariq High-Efficiency Drip Cotton Pilot',
  pilotStatus: 'ACTIVE',
  totalParticipants: 5,
  totalFields: 3,
  totalGroundTruthMeasurements: 15,
  pendingVerifications: 1,
  verifiedGroundTruths: 14,
  totalPredictionValidations: 12,
  averageAbsoluteError: 1.45,
  openFeedbackCount: 1,
  latestReadinessStatus: 'PASS',
}

function createTestQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: { retry: false, gcTime: 0 },
      mutations: { retry: false },
    },
  })
}

function renderWithClient(ui: React.ReactElement) {
  const testQueryClient = createTestQueryClient()
  return render(<QueryClientProvider client={testQueryClient}>{ui}</QueryClientProvider>)
}

describe('Milestone 08.20 — Controlled Field Pilot & Ground Truth Suite', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
    vi.spyOn(clientModule, 'apiClient').mockImplementation(async (url: string) => {
      const now = new Date().toISOString()
      if (url === '/pilot-programs' || url.startsWith('/pilot-programs?')) {
        return { success: true, data: mockPilots, timestamp: now }
      }
      if (url.startsWith('/pilot-readiness/')) {
        return { success: true, data: mockAssessment, timestamp: now }
      }
      if (url.startsWith('/pilot-ground-truth/pilot/')) {
        return { success: true, data: mockMeasurements, timestamp: now }
      }
      if (url.startsWith('/pilot-validation/pilot/')) {
        return { success: true, data: mockValidations, timestamp: now }
      }
      if (url.startsWith('/pilot-feedback/pilot/')) {
        return { success: true, data: mockFeedback, timestamp: now }
      }
      if (url.includes('/report')) {
        return { success: true, data: mockReport, timestamp: now }
      }
      if (url.includes('/monitoring')) {
        return { success: true, data: mockMonitoring, timestamp: now }
      }
      return { success: true, data: null, timestamp: now }
    })
  })

  it('renders PilotProgramsSummaryCards with correct operational counts', () => {
    renderWithClient(
      <PilotProgramsSummaryCards
        programs={mockPilots}
        monitoring={mockMonitoring}
        lang="en"
      />
    )

    expect(screen.getByText('Total Programs')).toBeInTheDocument()
    expect(screen.getByText('2')).toBeInTheDocument()
    expect(screen.getByText('Active Pilots')).toBeInTheDocument()
    expect(screen.getAllByText('1').length).toBeGreaterThan(0)
    expect(screen.getByText('Verified Observations')).toBeInTheDocument()
    expect(screen.getByText('14')).toBeInTheDocument()
    expect(screen.getByText('Prediction Validations')).toBeInTheDocument()
    expect(screen.getByText('12')).toBeInTheDocument()
  })

  it('renders PilotProgramsTable and handles pilot selection and actions', () => {
    const onSelectPilot = vi.fn()
    const onViewDetails = vi.fn()
    const onAssessReadiness = vi.fn()
    const onActivatePilot = vi.fn()

    renderWithClient(
      <PilotProgramsTable
        programs={mockPilots}
        selectedPilotId="pilot-1"
        onSelectPilot={onSelectPilot}
        onViewDetails={onViewDetails}
        onAssessReadiness={onAssessReadiness}
        onActivatePilot={onActivatePilot}
        canManage={true}
        lang="en"
      />
    )

    expect(screen.getByText('PILOT-2026-YANGIARIQ-COTTON')).toBeInTheDocument()
    expect(screen.getByText('Yangiariq High-Efficiency Drip Cotton Pilot')).toBeInTheDocument()
    expect(screen.getByText('Active')).toBeInTheDocument()

    // Test clicking row
    fireEvent.click(screen.getByText('PILOT-2026-YANGIARIQ-COTTON'))
    expect(onSelectPilot).toHaveBeenCalledWith(mockPilots[0])

    // Test clicking Readiness button
    const readinessBtns = screen.getAllByTitle('Assess Readiness')
    fireEvent.click(readinessBtns[0])
    expect(onAssessReadiness).toHaveBeenCalledWith(mockPilots[0])
  })

  it('submits CreatePilotDialog when valid payload is entered', async () => {
    const onCreate = vi.fn().mockResolvedValue(undefined)
    const onClose = vi.fn()

    renderWithClient(
      <CreatePilotDialog
        isOpen={true}
        onClose={onClose}
        onCreate={onCreate}
        lang="en"
      />
    )

    expect(screen.getByText('Create Field Pilot Program')).toBeInTheDocument()

    fireEvent.change(screen.getByPlaceholderText('PILOT-YNG-2026-01'), {
      target: { value: 'PILOT-2026-NEW-01' },
    })
    fireEvent.change(screen.getByPlaceholderText('Khorezm Cotton Water Efficiency Pilot'), {
      target: { value: 'New Test Pilot' },
    })
    fireEvent.change(
      screen.getByPlaceholderText('Water distribution, sensor accuracy, predictive validation...'),
      {
        target: { value: 'Validate 10 tensiometer probes' },
      }
    )

    const saveButton = screen.getByRole('button', { name: /new pilot/i })
    fireEvent.click(saveButton)

    await waitFor(() => {
      expect(onCreate).toHaveBeenCalledWith(
        expect.objectContaining({
          code: 'PILOT-2026-NEW-01',
          name: 'New Test Pilot',
          operationalObjectives: 'Validate 10 tensiometer probes',
        })
      )
    })
  })

  it('renders PilotDetailsModal with tabs for Info, Participants, and Fields', () => {
    renderWithClient(
      <PilotDetailsModal
        isOpen={true}
        onClose={vi.fn()}
        pilot={mockPilots[0]}
        lang="en"
      />
    )

    expect(screen.getByText('Yangiariq High-Efficiency Drip Cotton Pilot')).toBeInTheDocument()
    expect(screen.getByText('PILOT-2026-YANGIARIQ-COTTON')).toBeInTheDocument()
    expect(screen.getByText(/Yangiariq District, Sector 4/)).toBeInTheDocument()

    // Check tabs
    expect(screen.getByText(/Participants/)).toBeInTheDocument()
    expect(screen.getByText(/Enrolled Fields/)).toBeInTheDocument()
  })

  it('renders PilotReadinessPanel and displays 4 operational gates with PASS status', () => {
    renderWithClient(
      <PilotReadinessPanel
        assessment={mockAssessment}
        onTriggerAssessment={vi.fn()}
        isAssessing={false}
        canManage={true}
        lang="en"
      />
    )

    expect(screen.getByText('Detailed Readiness Checks')).toBeInTheDocument()
    expect(screen.getByText('Infrastructure')).toBeInTheDocument()
    expect(screen.getByText('Data Readiness')).toBeInTheDocument()
    expect(screen.getByText('Operational')).toBeInTheDocument()
    expect(screen.getByText('Governance & Oversight')).toBeInTheDocument()
    expect(screen.getByText('Sensors / Metering Baseline')).toBeInTheDocument()
    expect(screen.getByText('Telemetry Ingestion Pathway')).toBeInTheDocument()
  })

  it('renders SubmitGroundTruthDialog and calls onSubmit', async () => {
    const onSubmit = vi.fn().mockResolvedValue(undefined)
    const onClose = vi.fn()

    renderWithClient(
      <SubmitGroundTruthDialog
        isOpen={true}
        onClose={onClose}
        onSubmit={onSubmit}
        lang="en"
      />
    )

    expect(screen.getByText('Submit Ground-Truth Observation')).toBeInTheDocument()

    fireEvent.change(screen.getByPlaceholderText('00000000-0000-0000-0000-000000000000'), {
      target: { value: 'field-101' },
    })
    fireEvent.change(screen.getByPlaceholderText('48.5'), {
      target: { value: '25.4' },
    })

    const submitBtn = screen.getByRole('button', { name: /submit for verification/i })
    fireEvent.click(submitBtn)

    await waitFor(() => {
      expect(onSubmit).toHaveBeenCalledWith(
        expect.objectContaining({
          fieldId: 'field-101',
          measuredValue: 25.4,
        })
      )
    })
  })

  it('renders ReviewGroundTruthDialog and enforces Four-Eyes decision', async () => {
    const onReview = vi.fn().mockResolvedValue(undefined)
    const onClose = vi.fn()

    renderWithClient(
      <ReviewGroundTruthDialog
        isOpen={true}
        onClose={onClose}
        measurement={mockMeasurements[1]} // Pending review measurement
        onReview={onReview}
        lang="en"
      />
    )

    expect(screen.getByText('Independent Review & Verification')).toBeInTheDocument()
    expect(screen.getByText(/12\.8/)).toBeInTheDocument()

    const confirmBtn = screen.getByRole('button', { name: /submit review decision/i })
    fireEvent.click(confirmBtn)

    await waitFor(() => {
      expect(onReview).toHaveBeenCalledWith(
        expect.objectContaining({
          action: 'VERIFY',
        })
      )
    })
  })

  it('renders GroundTruthValidationView with summary counters and measurements', async () => {
    renderWithClient(<GroundTruthValidationView pilotId="pilot-1" lang="en" />)

    await waitFor(() => {
      expect(screen.getByText('field-101')).toBeInTheDocument()
    })
    expect(screen.getByText('Ground-Truth Independent Validation')).toBeInTheDocument()
    expect(screen.getByText('SOIL_MOISTURE')).toBeInTheDocument()
  })

  it('renders PredictionValidationView and computes error metrics', async () => {
    renderWithClient(<PredictionValidationView pilotId="pilot-1" lang="en" />)

    await waitFor(() => {
      expect(screen.getByText('SOIL_MOISTURE_HYDRAULIC_SURROGATE')).toBeInTheDocument()
    })
    expect(screen.getByText('Prediction vs Actual Outcome Validation')).toBeInTheDocument()
    expect(screen.getByText('26.3')).toBeInTheDocument()
    expect(screen.getByText('24.5')).toBeInTheDocument()
    expect(screen.getAllByText('1.80').length).toBeGreaterThan(0)
    expect(screen.getAllByText(/7\.3%/).length).toBeGreaterThan(0)
  })

  it('renders OperatorFeedbackView with discrepancy logs', async () => {
    renderWithClient(<OperatorFeedbackView pilotId="pilot-1" lang="en" />)

    await waitFor(() => {
      expect(screen.getByText('Operator Feedback & Discrepancies')).toBeInTheDocument()
      expect(screen.getByText('Soil temperature probe calibration offset')).toBeInTheDocument()
      expect(screen.getByText(/Thermistor reading 3 degrees higher/)).toBeInTheDocument()
    })
  })

  it('renders PilotEvaluationReportView with synthesized pillars and limitation disclaimers', async () => {
    renderWithClient(<PilotEvaluationReportView pilotId="pilot-1" lang="en" />)

    await waitFor(() => {
      expect(screen.getByText('Field Pilot Evaluation Report')).toBeInTheDocument()
      expect(screen.getByText('Data Quality & Reliability')).toBeInTheDocument()
      expect(screen.getByText('Predictive Model Accuracy')).toBeInTheDocument()
      expect(screen.getByText('Operational Execution & Compliance')).toBeInTheDocument()
      expect(screen.getByText('Water Outcomes & Hydrological Impact')).toBeInTheDocument()
      expect(screen.getByText(/45,200 m³/)).toBeInTheDocument()
      expect(screen.getByText(/Technical & Methodological Limitations/)).toBeInTheDocument()
      expect(screen.getByText(/Recommendations & Next Steps/)).toBeInTheDocument()
    })
  })

  it('renders full PilotPage and switches tabs', async () => {
    renderWithClient(<PilotPage lang="en" />)

    expect(screen.getByText('Field Pilot & Ground-Truth Validation')).toBeInTheDocument()
    expect(screen.getByText('Pilot Programs')).toBeInTheDocument()

    // Switch to Ground-Truth Validation tab
    const gtTab = screen.getByRole('button', { name: /ground-truth validation/i })
    fireEvent.click(gtTab)

    await waitFor(() => {
      expect(screen.getByText('Total Ingested')).toBeInTheDocument()
    })
  })
})
