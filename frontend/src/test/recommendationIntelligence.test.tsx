import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { RecommendationPriorityBadge } from '@/features/recommendation/components/RecommendationPriorityBadge'
import { RecommendationStatusBadge } from '@/features/recommendation/components/RecommendationStatusBadge'
import { RecommendationCard } from '@/features/recommendation/components/RecommendationCard'
import { DismissRecommendationDialog } from '@/features/recommendation/components/DismissRecommendationDialog'
import { RecommendationDetailsModal } from '@/features/recommendation/components/RecommendationDetailsModal'
import { RecommendationFeed } from '@/features/recommendation/components/RecommendationFeed'
import { Recommendation } from '@/entities/recommendation'

describe('Recommendation Intelligence UI Suite', () => {
  const sampleRec: Recommendation = {
    id: 'rec-001',
    fieldId: 'field-101',
    recommendationType: 'IRRIGATE_NOW',
    priority: 'CRITICAL',
    status: 'GENERATED',
    title: 'Immediate Irrigation Required for Cotton Plot 01',
    summary: 'Soil moisture dropped to 13.5%, approaching permanent wilting point.',
    explanation: 'Root zone moisture is below allowable depletion for alluvial loam soil.',
    confidence: 0.92,
    confidenceLevel: 'HIGH',
    validFrom: '2026-11-01T08:00:00Z',
    validUntil: '2026-11-02T08:00:00Z',
    generatedAt: '2026-11-01T08:00:00Z',
    source: 'HYBRID_AI',
    limitations: 'Provisional regional baseline calibration.',
    isSimulated: false,
    evidence: [
      {
        id: 'ev-1',
        evidenceType: 'SENSOR_TELEMETRY',
        metric: 'SOIL_MOISTURE',
        observedValue: 13.5,
        baselineValue: 22.0,
        unit: '%',
        summary: 'Moisture critically below MAD',
        sourceReference: 'Sensor S-101',
      },
    ],
    actions: [
      {
        id: 'act-1',
        actionOrder: 1,
        actionText: 'Execute furrow irrigation with target depth of 50mm',
        urgency: 'IMMEDIATE',
        isNegativeAction: false,
      },
      {
        id: 'act-2',
        actionOrder: 2,
        actionText: 'Do not apply mineral fertilizer while root zone is dehydrated',
        urgency: 'IMMEDIATE',
        isNegativeAction: true,
      },
    ],
    createdAt: '2026-11-01T08:00:00Z',
    updatedAt: '2026-11-01T08:00:00Z',
    version: 0,
  }

  it('renders RecommendationPriorityBadge correctly for all priorities', () => {
    const { rerender } = render(<RecommendationPriorityBadge priority="CRITICAL" lang="uz" />)
    expect(screen.getByText('Kritik')).toBeInTheDocument()

    rerender(<RecommendationPriorityBadge priority="HIGH" lang="en" />)
    expect(screen.getByText('High')).toBeInTheDocument()

    rerender(<RecommendationPriorityBadge priority="MEDIUM" lang="ru" />)
    expect(screen.getByText('Средний')).toBeInTheDocument()
  })

  it('renders RecommendationStatusBadge correctly for all statuses', () => {
    const { rerender } = render(<RecommendationStatusBadge status="GENERATED" lang="uz" />)
    expect(screen.getByText('Yaratilgan')).toBeInTheDocument()

    rerender(<RecommendationStatusBadge status="ACCEPTED" lang="en" />)
    expect(screen.getByText('Accepted')).toBeInTheDocument()

    rerender(<RecommendationStatusBadge status="DISMISSED" lang="ru" />)
    expect(screen.getByText('Отклонена')).toBeInTheDocument()
  })

  it('renders RecommendationCard with title, summary, confidence, and triggers view details', () => {
    const onViewDetails = vi.fn()
    const onAccept = vi.fn()

    render(
      <RecommendationCard
        recommendation={sampleRec}
        lang="uz"
        onViewDetails={onViewDetails}
        onAccept={onAccept}
      />
    )

    expect(screen.getByText('Immediate Irrigation Required for Cotton Plot 01')).toBeInTheDocument()
    expect(screen.getByText('92%')).toBeInTheDocument()
    expect(screen.getByText('Hybrid AI')).toBeInTheDocument()

    // Click card triggers onViewDetails
    fireEvent.click(screen.getByText('Immediate Irrigation Required for Cotton Plot 01'))
    expect(onViewDetails).toHaveBeenCalledWith(sampleRec)

    // Click Accept button
    const acceptBtn = screen.getByRole('button', { name: /Qabul qilish/i })
    fireEvent.click(acceptBtn)
    expect(onAccept).toHaveBeenCalledWith('rec-001')
  })

  it('validates mandatory reason in DismissRecommendationDialog', async () => {
    const onConfirm = vi.fn()
    const onOpenChange = vi.fn()

    render(
      <DismissRecommendationDialog
        open={true}
        onOpenChange={onOpenChange}
        onConfirm={onConfirm}
        lang="uz"
      />
    )

    // Initially button disabled or error triggered on empty click
    const confirmBtn = screen.getByRole('button', { name: /Rad etishni tasdiqlash/i })
    fireEvent.click(confirmBtn)
    expect(onConfirm).not.toHaveBeenCalled()

    // Type valid reason
    const textarea = screen.getByPlaceholderText(/Nima sababdan tavsiya qoʻllanilmayapti/i)
    fireEvent.change(textarea, { target: { value: 'Field was watered manually via auxiliary pump' } })

    fireEvent.click(confirmBtn)
    await waitFor(() => {
      expect(onConfirm).toHaveBeenCalledWith('Field was watered manually via auxiliary pump')
    })
  })

  it('renders RecommendationDetailsModal with actions, evidence, and DO NOT alert box', () => {
    const onOpenChange = vi.fn()

    render(
      <RecommendationDetailsModal
        recommendation={sampleRec}
        open={true}
        onOpenChange={onOpenChange}
        lang="uz"
      />
    )

    // Title & Explanation
    expect(screen.getByText('Immediate Irrigation Required for Cotton Plot 01')).toBeInTheDocument()
    expect(screen.getByText('Root zone moisture is below allowable depletion for alluvial loam soil.')).toBeInTheDocument()

    // Action step
    expect(screen.getByText('Execute furrow irrigation with target depth of 50mm')).toBeInTheDocument()

    // DO NOT action
    expect(screen.getByText('Do not apply mineral fertilizer while root zone is dehydrated')).toBeInTheDocument()

    // Evidence
    expect(screen.getByText('SOIL_MOISTURE')).toBeInTheDocument()
    expect(screen.getByText('13.5 %')).toBeInTheDocument()

    // Limitations disclaimer
    expect(screen.getByText('Provisional regional baseline calibration.')).toBeInTheDocument()
  })

  it('filters RecommendationFeed by priority and search query', () => {
    const rec2: Recommendation = {
      ...sampleRec,
      id: 'rec-002',
      title: 'Salinity Wash Recommended',
      summary: 'Electrical conductivity at 4.5 dS/m',
      priority: 'HIGH',
      recommendationType: 'MANAGE_SALINITY',
    }

    render(
      <RecommendationFeed
        recommendations={[sampleRec, rec2]}
        lang="en"
      />
    )

    // Both visible initially
    expect(screen.getByText('Immediate Irrigation Required for Cotton Plot 01')).toBeInTheDocument()
    expect(screen.getByText('Salinity Wash Recommended')).toBeInTheDocument()

    // Filter by priority: CRITICAL
    const prioritySelect = screen.getByRole('combobox', { name: /All Priorities/i })
    fireEvent.change(prioritySelect, { target: { value: 'CRITICAL' } })

    expect(screen.getByText('Immediate Irrigation Required for Cotton Plot 01')).toBeInTheDocument()
    expect(screen.queryByText('Salinity Wash Recommended')).not.toBeInTheDocument()

    // Search query
    const searchInput = screen.getByPlaceholderText(/Search by title or summary/i)
    fireEvent.change(searchInput, { target: { value: 'Cotton' } })

    expect(screen.getByText('Immediate Irrigation Required for Cotton Plot 01')).toBeInTheDocument()
  })
})
