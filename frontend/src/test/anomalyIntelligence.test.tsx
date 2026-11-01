import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { AnomalySeverityBadge } from '@/features/anomaly/components/AnomalySeverityBadge'
import { AnomalyStatusBadge } from '@/features/anomaly/components/AnomalyStatusBadge'
import { AnomalyCard } from '@/features/anomaly/components/AnomalyCard'
import { AnomalyDetailsModal } from '@/features/anomaly/components/AnomalyDetailsModal'
import { AnomalyActionDialog } from '@/features/anomaly/components/AnomalyActionDialog'
import { AnomalyRulesTable } from '@/features/anomaly/components/AnomalyRulesTable'
import { Anomaly, AnomalyRuleConfig } from '@/entities/anomaly'
import * as anomalyQueries from '@/features/anomaly/queries/useAnomalyQueries'
import * as authContext from '@/features/auth/context/AuthContext'

// Mock react-query mutations
vi.mock('@/features/anomaly/queries/useAnomalyQueries', () => ({
  useAcknowledgeAnomalyMutation: () => ({ mutateAsync: vi.fn(), isPending: false }),
  useInvestigateAnomalyMutation: () => ({ mutateAsync: vi.fn(), isPending: false }),
  useResolveAnomalyMutation: () => ({ mutateAsync: vi.fn(), isPending: false }),
  useDismissAnomalyMutation: () => ({ mutateAsync: vi.fn(), isPending: false }),
  useAnomalyRules: vi.fn(),
  useToggleAnomalyRuleMutation: () => ({ mutateAsync: vi.fn(), isPending: false }),
  useUpdateAnomalyRuleMutation: () => ({ mutateAsync: vi.fn(), isPending: false }),
}))

// Mock auth context
vi.mock('@/features/auth/context/AuthContext', () => ({
  useAuth: vi.fn(),
}))

describe('Anomaly Intelligence UI Suite', () => {
  const mockAnomaly: Anomaly = {
    id: 'anom-1',
    fieldId: 'field-101',
    sensorId: 'sensor-202',
    sensorType: 'SOIL_MOISTURE',
    anomalyType: 'SOIL_MOISTURE_CRITICAL',
    severity: 'CRITICAL',
    status: 'DETECTED',
    detectedValue: 12.4,
    expectedValue: 25.0,
    deviation: -12.6,
    confidence: 0.94,
    isSimulated: false,
    description: 'Tuproq namligi 12.4% ga tushib ketdi (chegara: 15%)',
    explanation: 'Ildiz zonasida jiddiy namlik defitsiti. Oʻsimlik soʻlish nuqtasiga yaqinlashmoqda.',
    source: 'RULE_ENGINE',
    observationCount: 3,
    firstObservedAt: '2026-11-01T08:00:00Z',
    lastObservedAt: '2026-11-01T08:30:00Z',
    detectedAt: '2026-11-01T08:00:00Z',
    evidence: [
      {
        id: 'ev-1',
        evidenceType: 'READING_SAMPLE',
        metric: 'SOIL_MOISTURE',
        observedValue: 12.4,
        expectedValue: 25.0,
        measuredAt: '2026-11-01T08:30:00Z',
        metadata: 'Sensor S-101 depth 30cm',
      },
    ],
    createdAt: '2026-11-01T08:00:00Z',
    updatedAt: '2026-11-01T08:30:00Z',
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(authContext.useAuth).mockReturnValue({
      user: { id: 'user-1', role: 'FARMER', fullName: 'Test Farmer' } as any,
      isAuthenticated: true,
      isLoading: false,
      login: vi.fn(),
      register: vi.fn(),
      logout: vi.fn(),
      refreshUser: vi.fn(),
      updateProfile: vi.fn(),
      changePassword: vi.fn(),
      requestReset: vi.fn(),
      confirmReset: vi.fn(),
    } as any)
  })

  describe('AnomalySeverityBadge', () => {
    it('renders CRITICAL badge in Uzbek with alert icon', () => {
      render(<AnomalySeverityBadge severity="CRITICAL" lang="uz" />)
      expect(screen.getByText('Kritik')).toBeDefined()
    })

    it('renders HIGH badge in English', () => {
      render(<AnomalySeverityBadge severity="HIGH" lang="en" />)
      expect(screen.getByText('High')).toBeDefined()
    })

    it('renders MEDIUM badge in Russian', () => {
      render(<AnomalySeverityBadge severity="MEDIUM" lang="ru" />)
      expect(screen.getByText('Средний')).toBeDefined()
    })
  })

  describe('AnomalyStatusBadge', () => {
    it('renders DETECTED status in Uzbek', () => {
      render(<AnomalyStatusBadge status="DETECTED" lang="uz" />)
      expect(screen.getByText('Aniqlangan')).toBeDefined()
    })

    it('renders RESOLVED status in English', () => {
      render(<AnomalyStatusBadge status="RESOLVED" lang="en" />)
      expect(screen.getByText('Resolved')).toBeDefined()
    })
  })

  describe('AnomalyCard', () => {
    it('renders detected value, confidence percentage, and quick action buttons for active anomaly', () => {
      const onAction = vi.fn()
      const onInspect = vi.fn()

      render(
        <AnomalyCard
          anomaly={mockAnomaly}
          onAction={onAction}
          onInspect={onInspect}
          lang="uz"
        />
      )

      expect(screen.getByText('Kritik namlik tanqisligi')).toBeDefined()
      expect(screen.getByText('12.4')).toBeDefined()
      expect(screen.getByText('94%')).toBeDefined()
      expect(screen.getByText('3×')).toBeDefined()

      // Quick action buttons for DETECTED state
      const ackButton = screen.getByRole('button', { name: /Qabul qilish/i })
      expect(ackButton).toBeDefined()
      fireEvent.click(ackButton)
      expect(onAction).toHaveBeenCalledWith(mockAnomaly, 'acknowledge')

      const detailsBtn = screen.getByRole('button', { name: /Batafsil tahlil/i })
      fireEvent.click(detailsBtn)
      expect(onInspect).toHaveBeenCalledWith(mockAnomaly)
    })
  })

  describe('AnomalyDetailsModal', () => {
    it('displays comprehensive diagnostics, explanation, and evidence records', () => {
      render(
        <AnomalyDetailsModal
          isOpen={true}
          onClose={vi.fn()}
          anomaly={mockAnomaly}
          lang="uz"
        />
      )

      expect(screen.getByText('Kritik namlik tanqisligi')).toBeDefined()
      expect(screen.getByText(/Ildiz zonasida jiddiy namlik defitsiti/i)).toBeDefined()
      expect(screen.getByText('Sensor S-101 depth 30cm')).toBeDefined()
      expect(screen.getByText('-12.6')).toBeDefined()
    })
  })

  describe('AnomalyActionDialog', () => {
    it('requires notes before resolving or dismissing an anomaly', async () => {
      render(
        <AnomalyActionDialog
          isOpen={true}
          onClose={vi.fn()}
          anomaly={mockAnomaly}
          actionType="resolve"
          lang="uz"
        />
      )

      const confirmBtn = screen.getByRole('button', { name: /Tasdiqlash/i })
      fireEvent.click(confirmBtn)

      // Verification: validation message displayed because notes are mandatory
      expect(screen.getByText('Izoh kiritilishi shart')).toBeDefined()
    })
  })

  describe('AnomalyRulesTable', () => {
    const mockRules: AnomalyRuleConfig[] = [
      {
        id: 'rule-1',
        ruleCode: 'RULE_SOIL_MOISTURE_DEFICIT',
        sensorType: 'SOIL_MOISTURE',
        name: 'Tuproq namligi kritik kamayishi',
        description: 'Kritik qurgʻoqchilik signali',
        enabled: true,
        severity: 'CRITICAL',
        threshold: 15.0,
        secondaryThreshold: 25.0,
        timeWindowMinutes: 60,
        minConsecutiveReadings: 1,
        createdAt: '2026-01-01T00:00:00Z',
        updatedAt: '2026-01-01T00:00:00Z',
      },
    ]

    it('renders rules table with threshold values and disclaims farmer role is read-only', () => {
      vi.mocked(anomalyQueries.useAnomalyRules).mockReturnValue({
        data: mockRules,
        isLoading: false,
      } as any)

      render(<AnomalyRulesTable lang="uz" />)

      expect(screen.getByText('RULE_SOIL_MOISTURE_DEFICIT')).toBeDefined()
      expect(screen.getByText('15')).toBeDefined()
      expect(screen.getByText('60 daq')).toBeDefined()
      expect(screen.getByText(/Faqat administrator qoidalarni oʻzgartirishi mumkin/i)).toBeDefined()
    })
  })
})
