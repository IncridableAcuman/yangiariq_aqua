import { describe, it, expect } from 'vitest'
import {
  waterSourceSchema,
  waterAllocationSchema,
  irrigationPlanSchema,
  startIrrigationEventSchema,
  completeIrrigationEventSchema,
  recordCompletedEventSchema,
  cancelOperationSchema,
} from '@/features/water/schemas/waterSchemas'

describe('Water Management Zod Form Validation Schemas', () => {
  // 1. Water Source Schema
  describe('waterSourceSchema', () => {
    it('accepts valid water source data', () => {
      const valid = {
        name: 'Palvon-Yobi Primary Canal',
        type: 'CANAL',
        description: 'Main intake canal for Yangiariq parcels',
      }
      const result = waterSourceSchema.safeParse(valid)
      expect(result.success).toBe(true)
    })

    it('rejects empty or whitespace name', () => {
      const invalid = {
        name: '   ',
        type: 'CANAL',
      }
      const result = waterSourceSchema.safeParse(invalid)
      expect(result.success).toBe(false)
    })

    it('rejects invalid source type', () => {
      const invalid = {
        name: 'Lake A',
        type: 'UNKNOWN_TYPE',
      }
      const result = waterSourceSchema.safeParse(invalid)
      expect(result.success).toBe(false)
    })
  })

  // 2. Water Allocation Schema
  describe('waterAllocationSchema', () => {
    it('accepts valid allocation payload', () => {
      const valid = {
        fieldId: '3e6077f2-a0a8-4f37-9ec8-8cb018488a51',
        waterSourceId: '7c9e6679-7425-40de-944b-e07fc1f90ae7',
        allocatedVolumeM3: '2500',
        periodStart: '2026-04-01',
        periodEnd: '2026-09-30',
        notes: 'Seasonal quota',
      }
      const result = waterAllocationSchema.safeParse(valid)
      expect(result.success).toBe(true)
      if (result.success) {
        expect(result.data.allocatedVolumeM3).toBe(2500)
      }
    })

    it('rejects non-positive allocated volume', () => {
      const invalid = {
        fieldId: 'f-1',
        waterSourceId: 'ws-1',
        allocatedVolumeM3: 0,
        periodStart: '2026-04-01',
        periodEnd: '2026-09-30',
      }
      const result = waterAllocationSchema.safeParse(invalid)
      expect(result.success).toBe(false)
    })

    it('rejects chronological violation (periodStart > periodEnd)', () => {
      const invalid = {
        fieldId: 'f-1',
        waterSourceId: 'ws-1',
        allocatedVolumeM3: 1000,
        periodStart: '2026-10-01',
        periodEnd: '2026-04-01',
      }
      const result = waterAllocationSchema.safeParse(invalid)
      expect(result.success).toBe(false)
    })
  })

  // 3. Irrigation Plan Schema
  describe('irrigationPlanSchema', () => {
    it('accepts valid irrigation plan', () => {
      const valid = {
        fieldId: 'f-1',
        waterAllocationId: 'alloc-1',
        plannedStartAt: '2026-05-10T04:00',
        plannedEndAt: '2026-05-10T12:00',
        plannedVolumeM3: '450',
        method: 'FURROW',
      }
      const result = irrigationPlanSchema.safeParse(valid)
      expect(result.success).toBe(true)
    })

    it('rejects plannedEnd before plannedStart', () => {
      const invalid = {
        fieldId: 'f-1',
        plannedStartAt: '2026-05-10T12:00',
        plannedEndAt: '2026-05-10T04:00',
        plannedVolumeM3: 450,
        method: 'FURROW',
      }
      const result = irrigationPlanSchema.safeParse(invalid)
      expect(result.success).toBe(false)
    })
  })

  // 4. Irrigation Event Schemas
  describe('startIrrigationEventSchema', () => {
    it('accepts valid start event data', () => {
      const valid = {
        fieldId: 'f-1',
        startAt: '2026-05-10T04:05',
        notes: 'Sluice gate opened',
      }
      const result = startIrrigationEventSchema.safeParse(valid)
      expect(result.success).toBe(true)
    })
  })

  describe('completeIrrigationEventSchema', () => {
    it('accepts valid completion with non-negative volume', () => {
      const valid = {
        endAt: '2026-05-10T11:45',
        actualVolumeM3: '442.8',
        notes: 'Watering completed',
      }
      const result = completeIrrigationEventSchema.safeParse(valid)
      expect(result.success).toBe(true)
      if (result.success) {
        expect(result.data.actualVolumeM3).toBe(442.8)
      }
    })

    it('rejects negative volume', () => {
      const invalid = {
        endAt: '2026-05-10T11:45',
        actualVolumeM3: -10,
      }
      const result = completeIrrigationEventSchema.safeParse(invalid)
      expect(result.success).toBe(false)
    })
  })

  describe('recordCompletedEventSchema', () => {
    it('accepts direct recorded event with valid timestamps', () => {
      const valid = {
        fieldId: 'f-1',
        startAt: '2026-05-09T22:00',
        endAt: '2026-05-10T03:30',
        actualVolumeM3: 380,
      }
      const result = recordCompletedEventSchema.safeParse(valid)
      expect(result.success).toBe(true)
    })

    it('rejects endAt earlier than startAt', () => {
      const invalid = {
        fieldId: 'f-1',
        startAt: '2026-05-10T05:00',
        endAt: '2026-05-10T04:00',
        actualVolumeM3: 380,
      }
      const result = recordCompletedEventSchema.safeParse(invalid)
      expect(result.success).toBe(false)
    })
  })

  describe('cancelOperationSchema', () => {
    it('accepts empty or populated cancellation reason', () => {
      expect(cancelOperationSchema.safeParse({}).success).toBe(true)
      expect(cancelOperationSchema.safeParse({ reason: 'Pump failure' }).success).toBe(true)
    })
  })
})
