import { z } from 'zod'

export const WATER_SOURCE_TYPES = [
  'CANAL',
  'DITCH',
  'RESERVOIR',
  'WELL',
  'RIVER',
  'OTHER',
] as const

export const IRRIGATION_METHODS = [
  'CANAL',
  'DITCH',
  'FURROW',
  'DRIP',
  'SPRINKLER',
  'OTHER',
] as const

// -------------------------------------------------------------
// Water Source Schema
// -------------------------------------------------------------
export const waterSourceSchema = z
  .object({
    name: z
      .string()
      .trim()
      .min(1, { message: 'water.validation.nameRequired' })
      .max(128, { message: 'water.validation.nameTooLong' }),
    type: z.enum(WATER_SOURCE_TYPES, {
      errorMap: () => ({ message: 'water.validation.typeRequired' }),
    }),
    description: z
      .string()
      .trim()
      .max(500, { message: 'water.validation.descriptionTooLong' })
      .optional()
      .or(z.literal('')),
    latitude: z
      .preprocess(
        (val) => (val === '' || val === null || val === undefined ? undefined : Number(val)),
        z
          .number({ invalid_type_error: 'water.validation.latRange' })
          .min(-90, { message: 'water.validation.latRange' })
          .max(90, { message: 'water.validation.latRange' })
          .optional()
      ),
    longitude: z
      .preprocess(
        (val) => (val === '' || val === null || val === undefined ? undefined : Number(val)),
        z
          .number({ invalid_type_error: 'water.validation.lonRange' })
          .min(-180, { message: 'water.validation.lonRange' })
          .max(180, { message: 'water.validation.lonRange' })
          .optional()
      ),
  })
  .refine(
    (data) => {
      const hasLat = data.latitude !== undefined && data.latitude !== null
      const hasLon = data.longitude !== undefined && data.longitude !== null
      return (hasLat && hasLon) || (!hasLat && !hasLon)
    },
    {
      message: 'water.validation.coordsBothOrNeither',
      path: ['latitude'],
    }
  )

export type WaterSourceFormData = z.infer<typeof waterSourceSchema>

// -------------------------------------------------------------
// Water Allocation Schema
// -------------------------------------------------------------
export const waterAllocationSchema = z
  .object({
    fieldId: z.string().min(1, { message: 'water.validation.fieldRequired' }),
    waterSourceId: z.string().min(1, { message: 'water.validation.waterSourceRequired' }),
    allocatedVolumeM3: z.coerce
      .number({ invalid_type_error: 'water.validation.volumePositive' })
      .positive({ message: 'water.validation.volumePositive' })
      .max(10_000_000, { message: 'water.validation.volumeTooLarge' }),
    periodStart: z.string().min(1, { message: 'water.validation.periodStartRequired' }),
    periodEnd: z.string().min(1, { message: 'water.validation.periodEndRequired' }),
    notes: z
      .string()
      .trim()
      .max(1000, { message: 'water.validation.notesTooLong' })
      .optional()
      .or(z.literal('')),
  })
  .refine(
    (data) => {
      if (!data.periodStart || !data.periodEnd) return true
      return new Date(data.periodStart) <= new Date(data.periodEnd)
    },
    {
      message: 'water.validation.invalidPeriodRange',
      path: ['periodEnd'],
    }
  )

export type WaterAllocationFormData = z.infer<typeof waterAllocationSchema>

// -------------------------------------------------------------
// Irrigation Plan Schema
// -------------------------------------------------------------
export const irrigationPlanSchema = z
  .object({
    fieldId: z.string().min(1, { message: 'water.validation.fieldRequired' }),
    waterAllocationId: z.string().optional().or(z.literal('')),
    plannedStartAt: z.string().min(1, { message: 'water.validation.plannedStartRequired' }),
    plannedEndAt: z.string().min(1, { message: 'water.validation.plannedEndRequired' }),
    plannedVolumeM3: z.coerce
      .number({ invalid_type_error: 'water.validation.volumePositive' })
      .positive({ message: 'water.validation.volumePositive' })
      .max(1_000_000, { message: 'water.validation.volumeTooLarge' }),
    method: z.enum(IRRIGATION_METHODS, {
      errorMap: () => ({ message: 'water.validation.methodRequired' }),
    }),
    notes: z
      .string()
      .trim()
      .max(1000, { message: 'water.validation.notesTooLong' })
      .optional()
      .or(z.literal('')),
  })
  .refine(
    (data) => {
      if (!data.plannedStartAt || !data.plannedEndAt) return true
      return new Date(data.plannedStartAt) <= new Date(data.plannedEndAt)
    },
    {
      message: 'water.validation.invalidPlanRange',
      path: ['plannedEndAt'],
    }
  )

export type IrrigationPlanFormData = z.infer<typeof irrigationPlanSchema>

// -------------------------------------------------------------
// Start Irrigation Event Schema
// -------------------------------------------------------------
export const startIrrigationEventSchema = z.object({
  fieldId: z.string().min(1, { message: 'water.validation.fieldRequired' }),
  irrigationPlanId: z.string().optional().or(z.literal('')),
  startAt: z.string().min(1, { message: 'water.validation.startAtRequired' }),
  notes: z
    .string()
    .trim()
    .max(1000, { message: 'water.validation.notesTooLong' })
    .optional()
    .or(z.literal('')),
})

export type StartIrrigationEventFormData = z.infer<typeof startIrrigationEventSchema>

// -------------------------------------------------------------
// Complete Irrigation Event Schema
// -------------------------------------------------------------
export const completeIrrigationEventSchema = z.object({
  endAt: z.string().min(1, { message: 'water.validation.endAtRequired' }),
  actualVolumeM3: z.coerce
    .number({ invalid_type_error: 'water.validation.actualVolumeNonNegative' })
    .min(0, { message: 'water.validation.actualVolumeNonNegative' })
    .max(1_000_000, { message: 'water.validation.volumeTooLarge' }),
  notes: z
    .string()
    .trim()
    .max(1000, { message: 'water.validation.notesTooLong' })
    .optional()
    .or(z.literal('')),
})

export type CompleteIrrigationEventFormData = z.infer<typeof completeIrrigationEventSchema>

// -------------------------------------------------------------
// Direct Record Completed Irrigation Event Schema
// -------------------------------------------------------------
export const recordCompletedEventSchema = z
  .object({
    fieldId: z.string().min(1, { message: 'water.validation.fieldRequired' }),
    irrigationPlanId: z.string().optional().or(z.literal('')),
    startAt: z.string().min(1, { message: 'water.validation.startAtRequired' }),
    endAt: z.string().min(1, { message: 'water.validation.endAtRequired' }),
    actualVolumeM3: z.coerce
      .number({ invalid_type_error: 'water.validation.actualVolumeNonNegative' })
      .min(0, { message: 'water.validation.actualVolumeNonNegative' })
      .max(1_000_000, { message: 'water.validation.volumeTooLarge' }),
    notes: z
      .string()
      .trim()
      .max(1000, { message: 'water.validation.notesTooLong' })
      .optional()
      .or(z.literal('')),
  })
  .refine(
    (data) => {
      if (!data.startAt || !data.endAt) return true
      return new Date(data.startAt) <= new Date(data.endAt)
    },
    {
      message: 'water.validation.invalidEventRange',
      path: ['endAt'],
    }
  )

export type RecordCompletedEventFormData = z.infer<typeof recordCompletedEventSchema>

// -------------------------------------------------------------
// Cancel Operation Schema
// -------------------------------------------------------------
export const cancelOperationSchema = z.object({
  reason: z
    .string()
    .trim()
    .max(500, { message: 'water.validation.reasonTooLong' })
    .optional()
    .or(z.literal('')),
})

export type CancelOperationFormData = z.infer<typeof cancelOperationSchema>
