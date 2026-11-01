import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import {
  WaterBudgetKpiCards,
  SmartPlanCard,
  PlanApprovalDialog,
  PlanCancelDialog,
  ScheduleTimeline,
  FieldPriorityTable,
} from '@/features/irrigation-planning'
import { FieldWaterSidePanel } from '@/features/water/gis/components/FieldWaterSidePanel'
import { SmartIrrigationPlan } from '@/entities/irrigation'
import { GisField } from '@/entities/gis'

describe('Smart Irrigation Planning & Water Allocation UI Suite', () => {
  const dummyPlan: SmartIrrigationPlan = {
    id: 'plan-101',
    fieldId: 'field-1',
    waterAllocationId: 'alloc-1',
    status: 'PROPOSED',
    priority: 'HIGH',
    plannedDate: '2026-11-02',
    plannedStartAt: '2026-11-02T06:00:00Z',
    plannedEndAt: '2026-11-02T10:00:00Z',
    recommendedVolumeM3: 150.0,
    estimatedDurationMinutes: 240,
    recommendedFlowRateM3H: 37.5,
    targetMoisturePercent: 70.0,
    currentMoisturePercent: 45.0,
    confidenceLevel: 'HIGH',
    confidenceScore: 0.92,
    reason: 'Root-zone moisture deficit (45% vs 70% target) on Cotton parcel requires 150 m³.',
    reasoningSummary: 'Hargreaves-Samani ET0 estimated at 5.2 mm/day with zero rain forecast.',
    isBlocked: false,
    source: 'DETERMINISTIC_SCHEDULER',
    createdAt: '2026-11-01T10:00:00Z',
    updatedAt: '2026-11-01T10:00:00Z',
    constraints: [
      {
        id: 'c-1',
        type: 'HYDRAULIC_SAFETY',
        status: 'SATISFIED',
        message: 'Line integrity verified normal. No pipe burst anomalies.',
      },
      {
        id: 'c-2',
        type: 'WATER_QUOTA',
        status: 'WARNING',
        message: 'Remaining quota is within 25% of seasonal allocation limit.',
      },
    ],
    tradeoffs: [
      {
        id: 't-1',
        category: 'APPLICATION_UNIFORMITY',
        description: 'High-volume irrigation should be monitored for uniform furrow advance.',
      },
    ],
  }

  const dummyBlockedPlan: SmartIrrigationPlan = {
    ...dummyPlan,
    id: 'plan-blocked',
    isBlocked: true,
    blockedReason: 'Hydraulic Interlock: SUSPECTED_PIPE_BURST detected on mainline.',
    constraints: [
      {
        id: 'c-block',
        type: 'HYDRAULIC_SAFETY',
        status: 'BLOCKED',
        message: 'SUSPECTED_PIPE_BURST anomaly active. Pressurization blocked.',
      },
    ],
  }

  describe('WaterBudgetKpiCards', () => {
    it('renders 5 KPI metrics with allocated, consumed, planned, remaining, and operational status', () => {
      render(
        <WaterBudgetKpiCards
          totalAllocatedM3={10000}
          consumedM3={6000}
          committedM3={1500}
          reservedM3={500}
          remainingAvailableM3={2000}
          requiringIrrigationCount={3}
          blockedCount={0}
          criticalCount={1}
          lang="uz"
        />
      )

      expect(screen.getByText(/10 000 m³/)).toBeInTheDocument()
      expect(screen.getByText(/6 000 m³/)).toBeInTheDocument()
      expect(screen.getAllByText(/2 000 m³/).length).toBe(2)
      expect(screen.getByText(/3/)).toBeInTheDocument()
      expect(screen.getByText(/1 shoshilinch/)).toBeInTheDocument()
    })

    it('displays low quota warning when remaining water is critical', () => {
      render(
        <WaterBudgetKpiCards
          totalAllocatedM3={10000}
          consumedM3={9200}
          committedM3={0}
          reservedM3={0}
          remainingAvailableM3={800} // < 15%
          lang="uz"
        />
      )

      expect(screen.getByText(/Kritik qoldiq!/i)).toBeInTheDocument()
    })
  })

  describe('SmartPlanCard', () => {
    it('renders plan attributes, volume, flow rate, priority badge, and triggers approve', () => {
      const onApprove = vi.fn()
      render(
        <SmartPlanCard
          plan={dummyPlan}
          fieldName="Paxtakor-1"
          cropType="COTTON"
          areaHectares={10}
          onApprove={onApprove}
          lang="uz"
        />
      )

      expect(screen.getByText('Paxtakor-1')).toBeInTheDocument()
      expect(screen.getAllByText(/150 m³/).length).toBeGreaterThanOrEqual(1)
      expect(screen.getByText(/37.5 m³\/h/)).toBeInTheDocument()
      expect(screen.getByText('45% / 70%')).toBeInTheDocument()
      expect(screen.getByText(/Remaining quota is within 25%/)).toBeInTheDocument()

      const approveBtn = screen.getByRole('button', { name: /Tasdiqlash/i })
      expect(approveBtn).not.toBeDisabled()
      fireEvent.click(approveBtn)
      expect(onApprove).toHaveBeenCalledWith(dummyPlan)
    })

    it('disables approval button and shows alert when plan is blocked by hydraulic safety interlock', () => {
      const onApprove = vi.fn()
      render(
        <SmartPlanCard
          plan={dummyBlockedPlan}
          fieldName="Paxtakor-Blocked"
          cropType="COTTON"
          onApprove={onApprove}
          lang="uz"
        />
      )

      expect(screen.getByRole('alert')).toHaveTextContent(/Gidravlik xavfsizlik: Sugʻorish bloklangan!/i)
      const approveBtn = screen.getByRole('button', { name: /Tasdiqlash/i })
      expect(approveBtn).toBeDisabled()
    })
  })

  describe('PlanApprovalDialog', () => {
    it('requires warning acknowledgement checkbox before allowing approval if warnings exist', () => {
      const onConfirm = vi.fn()
      const onOpenChange = vi.fn()

      render(
        <PlanApprovalDialog
          plan={dummyPlan}
          fieldName="Paxtakor-1"
          cropType="COTTON"
          open={true}
          onOpenChange={onOpenChange}
          onConfirm={onConfirm}
          lang="uz"
        />
      )

      // Warning banner is rendered
      expect(screen.getByText(/Diqqat: Operatsion ogohlantirishlar mavjud/i)).toBeInTheDocument()

      // Confirm button is initially disabled because warning is not acknowledged
      const confirmBtn = screen.getByRole('button', { name: /Rejani tasdiqlash/i })
      expect(confirmBtn).toBeDisabled()

      // Check acknowledgment checkbox
      const checkbox = screen.getByRole('checkbox')
      expect(checkbox).not.toBeChecked()
      fireEvent.click(checkbox)
      expect(checkbox).toBeChecked()

      // Button is now enabled
      expect(confirmBtn).not.toBeDisabled()
      fireEvent.click(confirmBtn)
      expect(onConfirm).toHaveBeenCalledWith(dummyPlan.id, true)
    })

    it('prohibits approval when plan is blocked by hydraulic safety interlock', () => {
      const onConfirm = vi.fn()
      render(
        <PlanApprovalDialog
          plan={dummyBlockedPlan}
          fieldName="Paxtakor-1"
          cropType="COTTON"
          open={true}
          onOpenChange={vi.fn()}
          onConfirm={onConfirm}
          lang="uz"
        />
      )

      expect(screen.getByText(/Gidravlik xavfsizlik: Reja tasdiqlanishi mumkin emas!/i)).toBeInTheDocument()
      const confirmBtn = screen.getByRole('button', { name: /Rejani tasdiqlash/i })
      expect(confirmBtn).toBeDisabled()
    })
  })

  describe('PlanCancelDialog', () => {
    it('requires non-blank reason of at least 3 characters', () => {
      const onConfirm = vi.fn()
      render(
        <PlanCancelDialog
          planId="plan-1"
          open={true}
          onOpenChange={vi.fn()}
          onConfirm={onConfirm}
          lang="uz"
        />
      )

      const textarea = screen.getByRole('textbox')
      const confirmBtn = screen.getByRole('button', { name: /Rejani bekor qilish/i })

      expect(confirmBtn).toBeDisabled()

      // Enter valid reason
      fireEvent.change(textarea, { target: { value: 'Mainline canal cleaning in progress' } })
      expect(confirmBtn).not.toBeDisabled()

      fireEvent.click(confirmBtn)
      expect(onConfirm).toHaveBeenCalledWith('plan-1', 'Mainline canal cleaning in progress')
    })
  })

  describe('ScheduleTimeline', () => {
    it('renders scheduled turns and marks nocturnal window', () => {
      const nocturnalPlan: SmartIrrigationPlan = {
        ...dummyPlan,
        id: 'plan-nocturnal',
        plannedStartAt: '2026-11-02T22:00:00Z',
        plannedEndAt: '2026-11-03T02:00:00Z',
      }

      render(
        <ScheduleTimeline
          plans={[nocturnalPlan]}
          fieldNamesById={{ 'field-1': 'Paxtakor 1' }}
          cropsById={{ 'field-1': 'COTTON' }}
          lang="uz"
        />
      )

      expect(screen.getByText('Paxtakor 1')).toBeInTheDocument()
      expect(screen.getByText(/Tungi salqin/i)).toBeInTheDocument()
    })
  })

  describe('FieldPriorityTable', () => {
    it('renders parcels and allows triggering AI plan generation', () => {
      const onGenerate = vi.fn()
      const onSelect = vi.fn()

      const items = [
        {
          field: {
            id: 'f-1',
            name: 'Bugʻdoyzor 1',
            cropType: 'WHEAT',
            areaHectares: 5.0,
          },
          soilMoisturePercent: 38.0,
          targetMoisturePercent: 70.0,
          calculatedPriority: 'CRITICAL' as const,
        },
      ]

      render(
        <FieldPriorityTable
          items={items}
          onGeneratePlan={onGenerate}
          onSelectPlan={onSelect}
          lang="uz"
        />
      )

      expect(screen.getByText('Bugʻdoyzor 1')).toBeInTheDocument()
      expect(screen.getByText('38.0%')).toBeInTheDocument()
      expect(screen.getByText('CRITICAL')).toBeInTheDocument()

      const genBtn = screen.getByRole('button', { name: /AI Reja/i })
      fireEvent.click(genBtn)
      expect(onGenerate).toHaveBeenCalledWith('f-1')
    })
  })

  describe('FieldWaterSidePanel Smart Irrigation Integration', () => {
    const dummyGisField: GisField = {
      id: 'field-1',
      ownerId: 'owner-1',
      name: 'Paxtakor Dalasi 1',
      areaHectares: 12.0,
      cropType: 'COTTON',
      soilType: 'LOAM',
      irrigationMethod: 'DRIP',
      status: 'ACTIVE',
    }

    it('renders active smart plan details and navigation button in GIS side panel', () => {
      const onNavigate = vi.fn()
      render(
        <FieldWaterSidePanel
          selectedField={dummyGisField}
          selectedWaterSource={null}
          activePlan={dummyPlan}
          onClose={vi.fn()}
          onNavigateToIrrigation={onNavigate}
          lang="uz"
        />
      )

      expect(screen.getByText(/Smart Sugʻorish Rejasi/i)).toBeInTheDocument()
      expect(screen.getAllByText(/150 m³/).length).toBeGreaterThanOrEqual(1)

      const navBtn = screen.getByRole('button', { name: /Smart Rejalashtirish/i })
      fireEvent.click(navBtn)
      expect(onNavigate).toHaveBeenCalled()
    })

    it('renders hydraulic safety interlock blocked warning in GIS side panel when plan is blocked', () => {
      render(
        <FieldWaterSidePanel
          selectedField={dummyGisField}
          selectedWaterSource={null}
          activePlan={dummyBlockedPlan}
          onClose={vi.fn()}
          lang="uz"
        />
      )

      expect(screen.getByText(/Gidravlik xavfsizlik: Sugʻorish bloklangan!/i)).toBeInTheDocument()
    })
  })
})
