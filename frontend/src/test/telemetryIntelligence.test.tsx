import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import {
  TelemetryFreshnessBadge,
  MoistureTrendIndicator,
  AgronomicAssessmentCard,
  IntelligencePipelineFlow,
} from '@/features/intelligence'
import { AgronomicAssessment } from '@/entities/intelligence'

describe('Telemetry Intelligence & Agronomic Assessment UI Suite', () => {
  describe('TelemetryFreshnessBadge', () => {
    it('renders Fresh badge with green styling', () => {
      render(<TelemetryFreshnessBadge freshness="FRESH" />)
      expect(screen.getByText('Fresh')).toBeInTheDocument()
    })

    it('renders Stale badge with amber styling', () => {
      render(<TelemetryFreshnessBadge freshness="STALE" />)
      expect(screen.getByText('Stale')).toBeInTheDocument()
    })

    it('renders Missing badge with red styling', () => {
      render(<TelemetryFreshnessBadge freshness="MISSING" />)
      expect(screen.getByText('Missing')).toBeInTheDocument()
    })
  })

  describe('MoistureTrendIndicator', () => {
    it('renders drying / decreasing trend with rate per hour', () => {
      render(
        <MoistureTrendIndicator
          trend={{
            direction: 'DECREASING',
            rateOfChangePerHour: -0.65,
            dataPointsEvaluated: 4,
          }}
        />
      )
      expect(screen.getByText(/Drying \(-0.65%\/h\)/i)).toBeInTheDocument()
    })

    it('renders wetting / increasing trend with rate per hour', () => {
      render(
        <MoistureTrendIndicator
          trend={{
            direction: 'INCREASING',
            rateOfChangePerHour: 1.25,
            dataPointsEvaluated: 3,
          }}
        />
      )
      expect(screen.getByText(/Wetting \(\+1.25%\/h\)/i)).toBeInTheDocument()
    })

    it('renders stable trend when within epsilon', () => {
      render(
        <MoistureTrendIndicator
          trend={{
            direction: 'STABLE',
            rateOfChangePerHour: 0.05,
            dataPointsEvaluated: 2,
          }}
        />
      )
      expect(screen.getByText(/Stable \(0.05%\/h\)/i)).toBeInTheDocument()
    })

    it('renders fallback when trend is null', () => {
      render(<MoistureTrendIndicator trend={null} />)
      expect(screen.getByText(/No trend data/i)).toBeInTheDocument()
    })
  })

  describe('IntelligencePipelineFlow', () => {
    it('renders all sequential stages of the decision-support pipeline', () => {
      render(<IntelligencePipelineFlow />)
      expect(screen.getByText('Telemetry')).toBeInTheDocument()
      expect(screen.getByText('Assessment')).toBeInTheDocument()
      expect(screen.getByText('Safety Interlock')).toBeInTheDocument()
      expect(screen.getByText('Recommendation')).toBeInTheDocument()
      expect(screen.getByText('Human Review')).toBeInTheDocument()
      expect(screen.getByText('Smart Plan')).toBeInTheDocument()
    })
  })

  describe('AgronomicAssessmentCard', () => {
    const mockAssessment: AgronomicAssessment = {
      id: 'a1111111-2222-3333-4444-555555555555',
      fieldId: 'f1111111-2222-3333-4444-555555555555',
      primarySignal: 'SOIL_TOO_DRY',
      overallSeverity: 'HIGH',
      soilMoistureTrend: {
        direction: 'DECREASING',
        rateOfChangePerHour: -0.55,
        dataPointsEvaluated: 4,
      },
      telemetryFreshness: 'FRESH',
      confidence: 'HIGH',
      confidenceScore: 0.88,
      agronomicSummary: 'Deterministic assessment indicates root zone moisture deficit across parcel.',
      recommendationsOverview: 'Schedule controlled irrigation run during night hours.',
      hydraulicSafetyBlocked: false,
      hydraulicSafetyReason: null,
      waterBudgetConstrained: false,
      waterBudgetReason: null,
      assessedAt: new Date().toISOString(),
      signals: [
        {
          id: 's1',
          signalType: 'SOIL_TOO_DRY',
          severity: 'HIGH',
          metric: 'SOIL_MOISTURE',
          observedValue: 14.5,
          thresholdValue: 20.0,
          message: 'Soil moisture is 14.5%',
        },
      ],
    }

    it('renders populated assessment card with signals, freshness, and trend', () => {
      render(<AgronomicAssessmentCard assessment={mockAssessment} />)

      expect(screen.getByText('Agronomic Telemetry Assessment')).toBeInTheDocument()
      expect(screen.getByText(/HIGH Confidence \(88%\)/i)).toBeInTheDocument()
      expect(screen.getByText('Fresh')).toBeInTheDocument()
      expect(screen.getByText(/Drying \(-0.55%\/h\)/i)).toBeInTheDocument()
      expect(screen.getByText(/Hydraulic Pipeline Interlock: Normal/i)).toBeInTheDocument()
      expect(screen.getByText(mockAssessment.recommendationsOverview!)).toBeInTheDocument()
      expect(screen.getByText(/Supporting Field Signals \(1\)/i)).toBeInTheDocument()
    })

    it('renders hydraulic safety block alert when blocked', () => {
      const blockedAssessment: AgronomicAssessment = {
        ...mockAssessment,
        hydraulicSafetyBlocked: true,
        hydraulicSafetyReason: 'Mainline pipe burst anomaly active. Pressurization blocked.',
      }

      render(<AgronomicAssessmentCard assessment={blockedAssessment} />)

      expect(screen.getByText(/Hydraulic Safety Interlock Active:/i)).toBeInTheDocument()
      expect(screen.getByText(/Mainline pipe burst anomaly active/i)).toBeInTheDocument()
    })

    it('renders water budget exhaustion warning when quota constrained', () => {
      const budgetConstrainedAssessment: AgronomicAssessment = {
        ...mockAssessment,
        waterBudgetConstrained: true,
        waterBudgetReason: 'Seasonal quota reached (0 m³ remaining).',
      }

      render(<AgronomicAssessmentCard assessment={budgetConstrainedAssessment} />)

      expect(screen.getByText(/Water Budget Exhausted:/i)).toBeInTheDocument()
      expect(screen.getByText(/Seasonal quota reached/i)).toBeInTheDocument()
    })

    it('handles onEvaluate trigger', () => {
      const onEvaluateMock = vi.fn()
      render(<AgronomicAssessmentCard assessment={mockAssessment} onEvaluate={onEvaluateMock} />)

      const reEvalBtn = screen.getByRole('button', { name: /Re-evaluate/i })
      fireEvent.click(reEvalBtn)
      expect(onEvaluateMock).toHaveBeenCalledTimes(1)
    })
  })
})
