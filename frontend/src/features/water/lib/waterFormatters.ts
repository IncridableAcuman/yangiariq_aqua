/**
 * Utility functions for volumetric formatting, percentage computations,
 * and clamped progress bar indicators in the Water Management domain.
 */

export function formatVolume(volume: number | null | undefined): string {
  if (volume === null || volume === undefined || isNaN(volume)) {
    return '0 m³'
  }
  // Formats numbers with spaces (e.g., "12 500 m³")
  const parts = Number(volume).toFixed(1).replace(/\.0$/, '').split('.')
  const integerPart = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ' ')
  return parts.length > 1 ? `${integerPart}.${parts[1]} m³` : `${integerPart} m³`
}

export function formatPercentage(percentage: number | null | undefined): string {
  if (percentage === null || percentage === undefined || isNaN(percentage)) {
    return '0%'
  }
  return `${Number(percentage).toFixed(1).replace(/\.0$/, '')}%`
}

export function calculateFillPercent(consumed: number, allocated: number): number {
  if (!allocated || allocated <= 0 || !consumed || consumed <= 0) {
    return 0
  }
  const raw = (consumed / allocated) * 100
  // Visual fill is clamped to 100% to prevent progress bar overflow
  return Math.min(Math.max(raw, 0), 100)
}

export function formatDateTime(dateStr: string | null | undefined): string {
  if (!dateStr) return '—'
  try {
    const d = new Date(dateStr)
    if (isNaN(d.getTime())) return dateStr
    return d.toLocaleString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    })
  } catch {
    return dateStr
  }
}

export function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return '—'
  try {
    const d = new Date(dateStr)
    if (isNaN(d.getTime())) return dateStr
    return d.toLocaleDateString(undefined, {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    })
  } catch {
    return dateStr
  }
}
