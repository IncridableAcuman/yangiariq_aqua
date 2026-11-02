import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import {
  OperationalWorkflowTimeline,
  CurrentSituationPanel,
  RecommendationSummaryCard,
  ConstraintChecklistCard,
  PlanApprovalDialog,
  PlanRejectDialog,
  ScheduleConflictAlert,
  OperationExecutionPanel,
  PostIrrigationVerificationCard,
  WorkflowActionBar,
} from '@/features/operations'
import {
  OperationalVerificationResponse,
  WorkflowStep,
  ScheduleConflict,
  SituationSummary,
} from '@/entities/operations'
import { SmartIrrigationPlan } from '@/entities/irrigation/types'

// Mock Data
const mockSteps: WorkflowStep[] = [
  {
    stepKey: 'DETECTED',
    title: 'Anomaliya Aniqlanishi',
    status: 'COMPLETED',
    timestamp: '2026-11-01T10:00:00Z',
    actor: 'Telemetry Engine',
    reason: 'Soil moisture deficit below 15%',
    availableAction: null,
  },
  {
    stepKey: 'RECOMMENDATION_READY',
    title: 'Agronomik Tavsiya',
    status: 'COMPLETED',
    timestamp: '2026-11-01T10:05:00Z',
    actor: 'Aqua AI Advisor',
    reason: 'Root-zone hydration recommendation generated',
    availableAction: null,
  },
  {
    stepKey: 'PLAN_PROPOSED',
    title: 'Aqlli Sugʻorish Rejasi',
    status: 'COMPLETED',
    timestamp: '2026-11-01T10:10:00Z',
    actor: 'Smart Planner',
    reason: 'Proposed 420 m³ over 120 minutes',
    availableAction: null,
  },
  {
    stepKey: 'APPROVAL_REQUIRED',
    title: 'Inson Tasdigʻi (Approval)',
    status: 'CURRENT',
    timestamp: null,
    actor: null,
    reason: 'Awaiting human operator confirmation',
    availableAction: 'APPROVE_PLAN',
  },
  {
    stepKey: 'SCHEDULED',
    title: 'Gidravlik Rejalashtirish',
    status: 'UPCOMING',
    timestamp: null,
    actor: null,
    reason: null,
    availableAction: null,
  },
  {
    stepKey: 'IN_PROGRESS',
    title: 'Dala Bajarilishi (Manual)',
    status: 'UPCOMING',
    timestamp: null,
    actor: null,
    reason: null,
    availableAction: null,
  },
  {
    stepKey: 'VERIFIED',
    title: 'Natija Tekshiruvi & Loop',
    status: 'UPCOMING',
    timestamp: null,
    actor: null,
    reason: null,
    availableAction: null,
  },
]

const mockSituation: SituationSummary = {
  latestSoilMoisture: 14.2,
  soilMoistureTrend12h: -2.4,
  temperatureCelsius: 28.5,
  telemetryFreshness: 'FRESH',
  lastTelemetryAt: '2026-11-01T10:15:00Z',
  waterRemainingM3: 1580.0,
  waterAllocatedM3: 3000.0,
  waterConsumedM3: 1420.0,
  hydraulicSafetyStatus: 'SAFE',
  hydraulicBlockedReason: null,
}

const mockPlan: SmartIrrigationPlan = {
  id: 'plan-101',
  fieldId: 'field-101',
  status: 'PROPOSED',
  priority: 'HIGH',
  plannedDate: '2026-11-02',
  plannedStartAt: '2026-11-02T06:00:00Z',
  plannedEndAt: '2026-11-02T08:00:00Z',
  recommendedVolumeM3: 420.0,
  estimatedDurationMinutes: 120,
  confidenceLevel: 'HIGH',
  reason: 'Severe root-zone moisture deficit detected. 420 m³ required to restore soil moisture to field capacity.',
  isBlocked: false,
  source: 'SMART_SCHEDULER',
  constraints: [],
  tradeoffs: [],
  createdAt: '2026-11-01T10:10:00Z',
  updatedAt: '2026-11-01T10:10:00Z',
}

const mockVerification: OperationalVerificationResponse = {
  planId: 'plan-101',
  fieldId: 'field-101',
  fieldName: 'Dala 12 Paxta',
  cropType: 'Cotton',
  executionDate: '2026-11-02',
  plannedVolumeM3: 420.0,
  actualVolumeM3: 415.0,
  volumeVarianceM3: -5.0,
  volumeVariancePercent: -1.2,
  plannedDurationMinutes: 120,
  actualDurationMinutes: 118,
  durationVarianceMinutes: -2,
  soilMoistureBeforePercent: 14.2,
  soilMoistureAfterPercent: 27.8,
  soilMoistureChangePercent: 13.6,
  waterBudgetRemainingBeforeM3: 1580.0,
  waterBudgetRemainingAfterM3: 1165.0,
  isRechargeSuccessful: true,
  verificationSummary: 'Muvaffaqiyatli: Tuproq namligi 14.2% dan 27.8% ga yetkazildi (+13.6%). Suv sarfi meʼyorda (-1.2% farq).',
  learningEvidence: 'Model accuracy 98.8%. Infiltration coefficient calibrated for loam parcel #field-101.',
}

describe('Decision-to-Action Operations Center UI Suite', () => {
  // 1. OperationalWorkflowTimeline Tests
  describe('OperationalWorkflowTimeline Component', () => {
    it('renders all 7 chronological operational steps with correct state badges', () => {
      render(
        <OperationalWorkflowTimeline
          timeline={mockSteps}
          lang="uz"
        />
      )

      expect(screen.getByText(/Anomaliya Aniqlanishi/i)).toBeInTheDocument()
      expect(screen.getByText(/Agronomik Tavsiya/i)).toBeInTheDocument()
      expect(screen.getByText(/Aqlli Sugʻorish Rejasi/i)).toBeInTheDocument()
      expect(screen.getByText(/Inson Tasdigʻi/i)).toBeInTheDocument()
      expect(screen.getByText(/Gidravlik Rejalashtirish/i)).toBeInTheDocument()
      expect(screen.getByText(/Dala Bajarilishi/i)).toBeInTheDocument()
      expect(screen.getByText(/Natija Tekshiruvi/i)).toBeInTheDocument()
    })

    it('renders step action buttons for actionable current steps', () => {
      const onActionClick = vi.fn()
      render(
        <OperationalWorkflowTimeline
          timeline={mockSteps}
          lang="uz"
          onActionClick={onActionClick}
        />
      )

      const approveBtn = screen.getByRole('button', { name: /Tasdiqlash/i })
      expect(approveBtn).toBeInTheDocument()
      fireEvent.click(approveBtn)
      expect(onActionClick).toHaveBeenCalledWith('APPROVE_PLAN')
    })
  })

  // 2. CurrentSituationPanel Tests
  describe('CurrentSituationPanel Component', () => {
    it('renders soil moisture, trend, temperature, freshness, and water quota', () => {
      render(
        <CurrentSituationPanel
          situation={mockSituation}
          lang="uz"
        />
      )

      expect(screen.getByText('14.2%')).toBeInTheDocument()
      expect(screen.getByText('2.4%/12h')).toBeInTheDocument()
      expect(screen.getByText('28.5°C')).toBeInTheDocument()
      expect(screen.getByText('FRESH')).toBeInTheDocument()
      expect(screen.getByText('1,580')).toBeInTheDocument()
    })
  })

  // 3. RecommendationSummaryCard Tests
  describe('RecommendationSummaryCard Component', () => {
    it('renders agronomic advice, confidence badge, and rationale', () => {
      render(
        <RecommendationSummaryCard
          recommendation={{
            id: 'rec-1',
            type: 'IRRIGATE',
            priority: 'HIGH',
            reason: 'Kritik qurgʻoqchilik xavfi oldini olish',
            confidence: 'HIGH',
            suggestedVolumeM3: 420.0,
            suggestedDurationMinutes: 120,
            aiExplanation: 'Root depth is 45cm, transpiration rate is 4.8mm/day. Immediate irrigation recommended.',
            evidence: ['Soil moisture: 14.2%', 'ET0: 4.8mm/day', 'NDVI: 0.62'],
          }}
          lang="uz"
        />
      )

      expect(screen.getByText(/Kritik qurgʻoqchilik/i)).toBeInTheDocument()
      expect(screen.getByText(/HIGH ishonch/i)).toBeInTheDocument()
      expect(screen.getByText(/Root depth is 45cm/i)).toBeInTheDocument()
      expect(screen.getByText('Soil moisture: 14.2%')).toBeInTheDocument()
    })
  })

  // 4. ConstraintChecklistCard Tests
  describe('ConstraintChecklistCard Component', () => {
    it('displays precedence constraint rules and their satisfied status', () => {
      render(
        <ConstraintChecklistCard
          situation={mockSituation}
          lang="uz"
        />
      )

      expect(screen.getByText(/Gidravlik xavfsizlik interlogi/i)).toBeInTheDocument()
      expect(screen.getByText(/Mavsumiy suv kvotasi/i)).toBeInTheDocument()
      expect(screen.getByText(/Tungi dispetcherizatsiya optimalligi/i)).toBeInTheDocument()
    })

    it('displays warning badge when hydraulic safety is blocked', () => {
      render(
        <ConstraintChecklistCard
          situation={{
            ...mockSituation,
            hydraulicSafetyStatus: 'BLOCKED',
            hydraulicBlockedReason: 'Kanal taʼmirlanmoqda',
          }}
          lang="uz"
        />
      )

      expect(screen.getByText(/Bloklangan/i)).toBeInTheDocument()
    })
  })

  // 5. PlanApprovalDialog Tests
  describe('PlanApprovalDialog Component', () => {
    it('strictly enforces the mandatory operational constraints acknowledgement checkbox', async () => {
      const onConfirm = vi.fn()
      const onClose = vi.fn()

      render(
        <PlanApprovalDialog
          isOpen={true}
          onClose={onClose}
          onConfirm={onConfirm}
          plan={mockPlan}
          fieldName="Dala 12 Paxta"
          cropType="Cotton"
          waterRemainingM3={1580.0}
          lang="uz"
        />
      )

      const approveButton = screen.getByRole('button', { name: /Tasdiqlashni yakunlash/i })
      // Before checkbox is ticked, button should be disabled
      expect(approveButton).toBeDisabled()

      // Find and tick checkbox
      const checkbox = screen.getByRole('checkbox')
      expect(checkbox).not.toBeChecked()
      fireEvent.click(checkbox)
      expect(checkbox).toBeChecked()

      // Button is now enabled
      expect(approveButton).not.toBeDisabled()

      // Submit
      fireEvent.click(approveButton)
      await waitFor(() => {
        expect(onConfirm).toHaveBeenCalledTimes(1)
      })
    })
  })

  // 6. PlanRejectDialog Tests
  describe('PlanRejectDialog Component', () => {
    it('requires a non-blank rejection reason before enabling rejection button', async () => {
      const onConfirm = vi.fn()
      const onClose = vi.fn()

      render(
        <PlanRejectDialog
          isOpen={true}
          onClose={onClose}
          onConfirm={onConfirm}
          fieldName="Dala 12 Paxta"
          lang="uz"
        />
      )

      const rejectButton = screen.getByRole('button', { name: /Rad etishni tasdiqlash/i })
      expect(rejectButton).toBeDisabled()

      const textarea = screen.getByPlaceholderText(/Masalan: Yomgʻir kutilmoqda/i)
      fireEvent.change(textarea, { target: { value: 'Bugun yomgʻir kutilmoqda, sugʻorishga ehtiyoj yoʻq' } })

      expect(rejectButton).not.toBeDisabled()
      fireEvent.click(rejectButton)

      await waitFor(() => {
        expect(onConfirm).toHaveBeenCalledWith('Bugun yomgʻir kutilmoqda, sugʻorishga ehtiyoj yoʻq')
      })
    })
  })

  // 7. ScheduleConflictAlert Tests
  describe('ScheduleConflictAlert Component', () => {
    it('renders schedule conflict warning when conflict exists', () => {
      const conflict: ScheduleConflict = {
        hasConflict: true,
        conflictType: 'OVERLAPPING_DISPATCH',
        conflictMessage: 'Kanal tarmogʻida ayni vaqtda boshqa sugʻorish rejasi mavjud',
        conflictingPlanId: 'plan-999',
        conflictingStartTime: '06:00',
        conflictingEndTime: '09:00',
        suggestedAdjustment: 'Vaqtni 11:00 ga koʻchirish tavsiya etiladi',
      }

      render(<ScheduleConflictAlert conflict={conflict} lang="uz" />)

      expect(screen.getByText(/Operatsion Cheklov/i)).toBeInTheDocument()
      expect(screen.getByText(/Kanal tarmogʻida ayni vaqtda/i)).toBeInTheDocument()
      expect(screen.getByText(/11:00 ga koʻchirish tavsiya etiladi/i)).toBeInTheDocument()
    })
  })

  // 8. OperationExecutionPanel Tests
  describe('OperationExecutionPanel Component', () => {
    it('displays non-actuator disclaimer and execution trigger', () => {
      const onStart = vi.fn()
      render(
        <OperationExecutionPanel
          plan={{ ...mockPlan, status: 'SCHEDULED' }}
          currentState="READY"
          lang="uz"
          onStartOperation={onStart}
          onCompleteOperation={vi.fn()}
        />
      )

      expect(screen.getByText(/Klapan yoki nasoslarni avtomatik ochmaydi/i)).toBeInTheDocument()
      const startBtn = screen.getByRole('button', { name: /Poliv Boshlanishini Qayd Etish/i })
      fireEvent.click(startBtn)
      expect(onStart).toHaveBeenCalled()
    })
  })

  // 9. PostIrrigationVerificationCard Tests
  describe('PostIrrigationVerificationCard Component', () => {
    it('renders planned vs actual volume, moisture delta, and learning loop evidence', () => {
      render(
        <PostIrrigationVerificationCard
          verification={mockVerification}
          lang="uz"
        />
      )

      expect(screen.getByText(/Sugʻorish Natijalarini Tekshirish/i)).toBeInTheDocument()
      expect(screen.getByText(/415.0/i)).toBeInTheDocument()
      expect(screen.getAllByText(/\+13\.6%/i).length).toBeGreaterThanOrEqual(1)
      expect(screen.getByText(/Muvaffaqiyatli Namlanish/i)).toBeInTheDocument()
      expect(screen.getByText(/Model accuracy 98.8%/i)).toBeInTheDocument()
    })
  })

  // 10. WorkflowActionBar Tests
  describe('WorkflowActionBar Component', () => {
    it('renders contextual action buttons based on current state', () => {
      const onActionClick = vi.fn()
      render(
        <WorkflowActionBar
          currentState="APPROVAL_REQUIRED"
          isBlocked={false}
          availableActions={['APPROVE_PLAN', 'REJECT_PLAN']}
          lang="uz"
          onActionClick={onActionClick}
        />
      )

      const approveBtn = screen.getByRole('button', { name: /Insoniy Tasdiqlash/i })
      const rejectBtn = screen.getByRole('button', { name: /Rad Etish/i })

      expect(approveBtn).toBeInTheDocument()
      expect(rejectBtn).toBeInTheDocument()

      fireEvent.click(approveBtn)
      expect(onActionClick).toHaveBeenCalledWith('APPROVE_PLAN')
    })
  })
})
