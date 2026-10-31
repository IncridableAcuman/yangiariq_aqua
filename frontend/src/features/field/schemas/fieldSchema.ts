import { z } from 'zod'

const CROP_TYPES = [
  'COTTON',
  'WHEAT',
  'RICE',
  'MAIZE',
  'VEGETABLE',
  'MELON',
  'OTHER',
] as const

const SOIL_TYPES = [
  'SANDY',
  'LOAM',
  'CLAY',
  'SANDY_LOAM',
  'CLAY_LOAM',
  'OTHER',
] as const

const IRRIGATION_METHODS = [
  'CANAL',
  'DITCH',
  'FURROW',
  'DRIP',
  'SPRINKLER',
  'OTHER',
] as const

export const fieldFormSchema = z.object({
  name: z
    .string()
    .trim()
    .min(1, { message: 'validation.nameRequired' })
    .max(128, { message: 'validation.nameTooLong' }),

  description: z
    .string()
    .trim()
    .max(500, { message: 'Description must be under 500 characters' })
    .optional()
    .or(z.literal('')),

  areaHectares: z
    .coerce
    .number({ invalid_type_error: 'validation.areaPositive' })
    .positive({ message: 'validation.areaPositive' })
    .max(100000, { message: 'validation.areaTooLarge' }),

  cropType: z.enum(CROP_TYPES, {
    errorMap: () => ({ message: 'validation.cropRequired' }),
  }),

  soilType: z.enum(SOIL_TYPES, {
    errorMap: () => ({ message: 'validation.soilRequired' }),
  }),

  irrigationMethod: z.enum(IRRIGATION_METHODS, {
    errorMap: () => ({ message: 'validation.irrigationRequired' }),
  }),

  latitude: z
    .preprocess(
      (val) => (val === '' || val === null || val === undefined ? undefined : Number(val)),
      z
        .number({ invalid_type_error: 'validation.latRange' })
        .min(37.0, { message: 'validation.latRange' })
        .max(45.6, { message: 'validation.latRange' })
        .optional()
    ),

  longitude: z
    .preprocess(
      (val) => (val === '' || val === null || val === undefined ? undefined : Number(val)),
      z
        .number({ invalid_type_error: 'validation.lonRange' })
        .min(56.0, { message: 'validation.lonRange' })
        .max(73.2, { message: 'validation.lonRange' })
        .optional()
    ),
})

export type FieldFormData = z.infer<typeof fieldFormSchema>
