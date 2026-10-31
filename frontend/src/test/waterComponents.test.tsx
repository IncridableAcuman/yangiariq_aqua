import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import {
  WaterProgressBar,
  WaterStatusBadge,
  OverConsumptionBadge,
  WaterSummaryCards,
} from '@/features/water/components'
import {
  formatVolume,
  formatPercentage,
  calculateFillPercent,
} from '@/features/water/lib/waterFormatters'

describe('Water Formatting Utilities', () => {
  it('formatVolume() formats numbers with space separators and m³ unit', () => {
    expect(formatVolume(0)).toBe('0 m³')
    expect(formatVolume(1000)).toBe('1 000 m³')
    expect(formatVolume(12500)).toBe('12 500 m³')
    expect(formatVolume(442.8)).toBe('442.8 m³')
    expect(formatVolume(null)).toBe('0 m³')
    expect(formatVolume(undefined)).toBe('0 m³')
  })

  it('formatPercentage() formats percentages correctly', () => {
    expect(formatPercentage(0)).toBe('0%')
    expect(formatPercentage(85)).toBe('85%')
    expect(formatPercentage(105.4)).toBe('105.4%')
  })

  it('calculateFillPercent() clamps visual progress between 0 and 100', () => {
    expect(calculateFillPercent(0, 1000)).toBe(0)
    expect(calculateFillPercent(500, 1000)).toBe(50)
    expect(calculateFillPercent(1000, 1000)).toBe(100)
    // Over-consumption clamped to 100% fill
    expect(calculateFillPercent(1500, 1000)).toBe(100)
    // Negative or zero allocated clamped to 0%
    expect(calculateFillPercent(100, 0)).toBe(0)
  })
})

describe('Water Management UI Primitives', () => {
  it('WaterProgressBar renders fill width and text within limit', () => {
    render(<WaterProgressBar consumed={500} allocated={1000} />)
    const progress = screen.getByRole('progressbar')
    expect(progress).toHaveAttribute('aria-valuenow', '50')
    expect(screen.getByText('50%')).toBeInTheDocument()
    expect(screen.getByText('Quota Used')).toBeInTheDocument()
  })

  it('WaterProgressBar clamps over-consumption to 100% fill and displays Over Quota label', () => {
    render(<WaterProgressBar consumed={1250} allocated={1000} isOverConsumed={true} />)
    const progress = screen.getByRole('progressbar')
    expect(progress).toHaveAttribute('aria-valuenow', '125')
    expect(screen.getByText('125%')).toBeInTheDocument()
    expect(screen.getByText('Over Quota')).toBeInTheDocument()
  })

  it('WaterStatusBadge renders semantic badge for each status', () => {
    const { rerender } = render(<WaterStatusBadge status="ACTIVE" lang="en" />)
    expect(screen.getByText('Active')).toBeInTheDocument()

    rerender(<WaterStatusBadge status="IN_PROGRESS" lang="en" />)
    expect(screen.getByText('In Progress')).toBeInTheDocument()

    rerender(<WaterStatusBadge status="COMPLETED" lang="en" />)
    expect(screen.getByText('Completed')).toBeInTheDocument()

    rerender(<WaterStatusBadge status="CANCELLED" lang="en" />)
    expect(screen.getByText('Cancelled')).toBeInTheDocument()
  })

  it('OverConsumptionBadge shows within quota vs over quota badge', () => {
    const { rerender } = render(
      <OverConsumptionBadge isOverConsumed={false} lang="en" />
    )
    expect(screen.getByText('Within Limit')).toBeInTheDocument()

    rerender(
      <OverConsumptionBadge
        isOverConsumed={true}
        overConsumedVolumeM3={250.5}
        lang="en"
      />
    )
    expect(screen.getByText('Over Quota')).toBeInTheDocument()
    expect(screen.getByText('(+250.5 m³)')).toBeInTheDocument()
  })

  it('WaterSummaryCards renders top metrics and over-consumption alert when exceeded', () => {
    const { rerender } = render(
      <WaterSummaryCards
        totalAllocatedVolumeM3={5000}
        totalConsumedVolumeM3={2000}
        totalRemainingVolumeM3={3000}
        totalOverConsumedVolumeM3={0}
        isOverConsumed={false}
        activeQuotaCount={2}
        completedEventsCount={4}
        lang="en"
      />
    )

    expect(screen.getByText('5 000 m³')).toBeInTheDocument()
    expect(screen.getByText('2 000 m³')).toBeInTheDocument()
    expect(screen.getByText('3 000 m³')).toBeInTheDocument()
    expect(screen.getByText('Within Limit')).toBeInTheDocument()
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()

    // Exceeded state
    rerender(
      <WaterSummaryCards
        totalAllocatedVolumeM3={5000}
        totalConsumedVolumeM3={5500}
        totalRemainingVolumeM3={0}
        totalOverConsumedVolumeM3={500}
        isOverConsumed={true}
        activeQuotaCount={2}
        completedEventsCount={5}
        lang="en"
      />
    )

    expect(screen.getByRole('alert')).toBeInTheDocument()
    expect(screen.getByText('+500 m³')).toBeInTheDocument()
    expect(screen.getByText('Warning: Water consumption has exceeded the allocated quota!')).toBeInTheDocument()
  })
})
