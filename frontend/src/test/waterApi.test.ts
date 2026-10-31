import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getWaterSources,
  createWaterSource,
  activateWaterSource,
  deactivateWaterSource,
  getWaterAllocations,
  createWaterAllocation,
  closeWaterAllocation,
  cancelWaterAllocation,
  createIrrigationPlan,
  startIrrigationPlan,
  completeIrrigationPlan,
  cancelIrrigationPlan,
  recordCompletedIrrigationEvent,
  completeIrrigationEvent,
  getFieldWaterSummary,
  getAllocationConsumption,
  getPlanMetrics,
} from '@/entities/water'
import * as clientModule from '@/shared/api/client'

describe('Water Management & Consumption API Client', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  // 1. Water Sources
  it('getWaterSources() should format query parameters correctly', async () => {
    const mockResponse: clientModule.ApiResponse<clientModule.PageResponse<unknown>> = {
      success: true,
      timestamp: '2026-10-31T00:00:00Z',
      data: {
        content: [],
        page: 0,
        size: 10,
        totalElements: 0,
        totalPages: 0,
        last: true,
      },
    }
    const spy = vi.spyOn(clientModule, 'apiClient').mockResolvedValue(mockResponse as never)

    await getWaterSources({
      query: 'Palvon',
      type: 'CANAL',
      status: 'ACTIVE',
      page: 0,
      size: 15,
      sortBy: 'name',
      direction: 'asc',
    })

    expect(spy).toHaveBeenCalledTimes(1)
    const url = spy.mock.calls[0][0]
    expect(url).toContain('/water-sources?')
    expect(url).toContain('query=Palvon')
    expect(url).toContain('type=CANAL')
    expect(url).toContain('status=ACTIVE')
    expect(url).toContain('size=15')
    expect(url).toContain('sort=name%2Casc')
  })

  it('createWaterSource() sends POST /water-sources', async () => {
    const spy = vi.spyOn(clientModule, 'apiClient').mockResolvedValue({
      success: true,
      timestamp: '2026-10-31T00:00:00Z',
      data: { id: 'ws-1', name: 'Canal A', type: 'CANAL', status: 'ACTIVE' },
    } as never)

    await createWaterSource({
      name: 'Canal A',
      type: 'CANAL',
      description: 'Regional canal',
    })

    expect(spy).toHaveBeenCalledWith('/water-sources', expect.objectContaining({
      method: 'POST',
      body: JSON.stringify({ name: 'Canal A', type: 'CANAL', description: 'Regional canal' }),
    }))
  })

  it('activateWaterSource() and deactivateWaterSource() hit lifecycle endpoints', async () => {
    const spy = vi.spyOn(clientModule, 'apiClient').mockResolvedValue({
      success: true,
      timestamp: '2026-10-31T00:00:00Z',
      data: { id: 'ws-1' },
    } as never)

    await activateWaterSource('ws-1')
    expect(spy).toHaveBeenCalledWith('/water-sources/ws-1/activate', { method: 'PATCH' })

    await deactivateWaterSource('ws-1')
    expect(spy).toHaveBeenCalledWith('/water-sources/ws-1/deactivate', { method: 'PATCH' })
  })

  // 2. Water Allocations
  it('getWaterAllocations() should format field and status params', async () => {
    const spy = vi.spyOn(clientModule, 'apiClient').mockResolvedValue({
      success: true,
      timestamp: '2026-10-31T00:00:00Z',
      data: { content: [] },
    } as never)

    await getWaterAllocations({
      fieldId: 'field-123',
      status: 'ACTIVE',
    })

    expect(spy).toHaveBeenCalledTimes(1)
    const url = spy.mock.calls[0][0]
    expect(url).toContain('/water-allocations?')
    expect(url).toContain('fieldId=field-123')
    expect(url).toContain('status=ACTIVE')
  })

  it('createWaterAllocation() sends POST /water-allocations', async () => {
    const spy = vi.spyOn(clientModule, 'apiClient').mockResolvedValue({
      success: true,
      timestamp: '2026-10-31T00:00:00Z',
      data: { id: 'alloc-1' },
    } as never)

    await createWaterAllocation({
      fieldId: 'f-1',
      waterSourceId: 'ws-1',
      allocatedVolumeM3: 2500,
      periodStart: '2026-04-01T00:00:00Z',
      periodEnd: '2026-09-30T23:59:59Z',
    })

    expect(spy).toHaveBeenCalledWith('/water-allocations', expect.objectContaining({
      method: 'POST',
    }))
  })

  it('closeWaterAllocation() and cancelWaterAllocation() hit transition endpoints', async () => {
    const spy = vi.spyOn(clientModule, 'apiClient').mockResolvedValue({
      success: true,
      timestamp: '2026-10-31T00:00:00Z',
      data: { id: 'alloc-1' },
    } as never)

    await closeWaterAllocation('alloc-1', { reason: 'Season completed' })
    expect(spy).toHaveBeenCalledWith('/water-allocations/alloc-1/close', expect.objectContaining({
      method: 'PATCH',
    }))

    await cancelWaterAllocation('alloc-1', { reason: 'Administrative revocation' })
    expect(spy).toHaveBeenCalledWith('/water-allocations/alloc-1/cancel', expect.objectContaining({
      method: 'PATCH',
    }))
  })

  // 3. Irrigation Plans
  it('createIrrigationPlan() and lifecycle transitions work correctly', async () => {
    const spy = vi.spyOn(clientModule, 'apiClient').mockResolvedValue({
      success: true,
      timestamp: '2026-10-31T00:00:00Z',
      data: { id: 'plan-1' },
    } as never)

    await createIrrigationPlan({
      fieldId: 'f-1',
      plannedStartAt: '2026-05-10T04:00:00Z',
      plannedEndAt: '2026-05-10T12:00:00Z',
      plannedVolumeM3: 450,
      method: 'FURROW',
    })
    expect(spy).toHaveBeenCalledWith('/irrigation-plans', expect.objectContaining({ method: 'POST' }))

    await startIrrigationPlan('plan-1')
    expect(spy).toHaveBeenCalledWith('/irrigation-plans/plan-1/start', { method: 'PATCH' })

    await completeIrrigationPlan('plan-1')
    expect(spy).toHaveBeenCalledWith('/irrigation-plans/plan-1/complete', { method: 'PATCH' })

    await cancelIrrigationPlan('plan-1', { reason: 'Rainfall occurred' })
    expect(spy).toHaveBeenCalledWith('/irrigation-plans/plan-1/cancel', expect.objectContaining({ method: 'PATCH' }))
  })

  // 4. Irrigation Events
  it('recordCompletedIrrigationEvent() sends POST /irrigation-events/completed', async () => {
    const spy = vi.spyOn(clientModule, 'apiClient').mockResolvedValue({
      success: true,
      timestamp: '2026-10-31T00:00:00Z',
      data: { id: 'evt-1' },
    } as never)

    await recordCompletedIrrigationEvent({
      fieldId: 'f-1',
      startAt: '2026-05-09T22:00:00Z',
      endAt: '2026-05-10T03:30:00Z',
      actualVolumeM3: 380,
    })

    expect(spy).toHaveBeenCalledWith('/irrigation-events/completed', expect.objectContaining({
      method: 'POST',
    }))
  })

  it('completeIrrigationEvent() sends PATCH /irrigation-events/{id}/complete with actual volume', async () => {
    const spy = vi.spyOn(clientModule, 'apiClient').mockResolvedValue({
      success: true,
      timestamp: '2026-10-31T00:00:00Z',
      data: { id: 'evt-1', actualVolumeM3: 420 },
    } as never)

    await completeIrrigationEvent('evt-1', {
      endAt: '2026-05-10T11:45:00Z',
      actualVolumeM3: 420,
      notes: 'Furrow watering done',
    })

    expect(spy).toHaveBeenCalledWith('/irrigation-events/evt-1/complete', expect.objectContaining({
      method: 'PATCH',
      body: JSON.stringify({
        endAt: '2026-05-10T11:45:00Z',
        actualVolumeM3: 420,
        notes: 'Furrow watering done',
      }),
    }))
  })

  // 5. Water Consumption & Metrics Endpoints
  it('getFieldWaterSummary() sends GET /water/fields/{fieldId}/summary with date range', async () => {
    const spy = vi.spyOn(clientModule, 'apiClient').mockResolvedValue({
      success: true,
      timestamp: '2026-10-31T00:00:00Z',
      data: {
        fieldId: 'f-1',
        totalAllocatedVolumeM3: 3500,
        totalConsumedVolumeM3: 1050,
        totalRemainingVolumeM3: 2450,
        isOverConsumed: false,
      },
    } as never)

    await getFieldWaterSummary('f-1', {
      from: '2026-05-01T00:00:00Z',
      to: '2026-05-31T23:59:59Z',
    })

    expect(spy).toHaveBeenCalledTimes(1)
    const url = spy.mock.calls[0][0]
    expect(url).toContain('/water/fields/f-1/summary?')
    expect(url).toContain('from=2026-05-01T00%3A00%3A00Z')
    expect(url).toContain('to=2026-05-31T23%3A59%3A59Z')
  })

  it('getAllocationConsumption() sends GET /water/allocations/{id}/summary', async () => {
    const spy = vi.spyOn(clientModule, 'apiClient').mockResolvedValue({
      success: true,
      timestamp: '2026-10-31T00:00:00Z',
      data: {
        allocationId: 'alloc-1',
        allocatedVolumeM3: 1000,
        consumedVolumeM3: 350,
        remainingVolumeM3: 650,
        isOverConsumed: false,
      },
    } as never)

    const result = await getAllocationConsumption('alloc-1')
    expect(spy).toHaveBeenCalledWith('/water/allocations/alloc-1/summary', { method: 'GET' })
    expect(result.remainingVolumeM3).toBe(650)
  })

  it('getPlanMetrics() sends GET /water/plans/{id}/metrics', async () => {
    const spy = vi.spyOn(clientModule, 'apiClient').mockResolvedValue({
      success: true,
      timestamp: '2026-10-31T00:00:00Z',
      data: {
        planId: 'plan-1',
        plannedVolumeM3: 400,
        actualVolumeM3: 380,
        varianceVolumeM3: -20,
        consumptionRatioPercentage: 95,
      },
    } as never)

    const result = await getPlanMetrics('plan-1')
    expect(spy).toHaveBeenCalledWith('/water/plans/plan-1/metrics', { method: 'GET' })
    expect(result.varianceVolumeM3).toBe(-20)
    expect(result.consumptionRatioPercentage).toBe(95)
  })
})
