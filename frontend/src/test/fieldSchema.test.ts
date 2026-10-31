import { describe, it, expect } from 'vitest'
import { fieldFormSchema } from '@/features/field/schemas/fieldSchema'

describe('Field Form Zod Validation Schema', () => {
  it('should accept valid field data', () => {
    const validData = {
      name: 'Paxtakor 1-sektor',
      description: 'Yangiariq bosh kanalidan sugʻoriladigan paxta maydoni',
      areaHectares: 12.5,
      cropType: 'COTTON',
      soilType: 'LOAM',
      irrigationMethod: 'CANAL',
      latitude: 41.450123,
      longitude: 60.520456,
    }

    const result = fieldFormSchema.safeParse(validData)
    expect(result.success).toBe(true)
    if (result.success) {
      expect(result.data.name).toBe('Paxtakor 1-sektor')
      expect(result.data.areaHectares).toBe(12.5)
      expect(result.data.cropType).toBe('COTTON')
    }
  })

  it('should fail when name is empty or only whitespace', () => {
    const invalidData = {
      name: '   ',
      areaHectares: 10,
      cropType: 'WHEAT',
      soilType: 'SANDY',
      irrigationMethod: 'DRIP',
    }

    const result = fieldFormSchema.safeParse(invalidData)
    expect(result.success).toBe(false)
    if (!result.success) {
      const issue = result.error.issues.find((i) => i.path[0] === 'name')
      expect(issue?.message).toBe('validation.nameRequired')
    }
  })

  it('should fail when name exceeds 128 characters', () => {
    const invalidData = {
      name: 'a'.repeat(129),
      areaHectares: 10,
      cropType: 'WHEAT',
      soilType: 'SANDY',
      irrigationMethod: 'DRIP',
    }

    const result = fieldFormSchema.safeParse(invalidData)
    expect(result.success).toBe(false)
    if (!result.success) {
      const issue = result.error.issues.find((i) => i.path[0] === 'name')
      expect(issue?.message).toBe('validation.nameTooLong')
    }
  })

  it('should fail when area is negative or zero', () => {
    const zeroArea = {
      name: 'Valid Name',
      areaHectares: 0,
      cropType: 'RICE',
      soilType: 'CLAY',
      irrigationMethod: 'FLOOD',
    }

    const negativeArea = {
      name: 'Valid Name',
      areaHectares: -5,
      cropType: 'RICE',
      soilType: 'CLAY',
      irrigationMethod: 'CANAL',
    }

    expect(fieldFormSchema.safeParse(zeroArea).success).toBe(false)
    expect(fieldFormSchema.safeParse(negativeArea).success).toBe(false)
  })

  it('should fail when area exceeds 100,000 hectares', () => {
    const excessiveArea = {
      name: 'Valid Name',
      areaHectares: 100001,
      cropType: 'COTTON',
      soilType: 'LOAM',
      irrigationMethod: 'CANAL',
    }

    const result = fieldFormSchema.safeParse(excessiveArea)
    expect(result.success).toBe(false)
    if (!result.success) {
      const issue = result.error.issues.find((i) => i.path[0] === 'areaHectares')
      expect(issue?.message).toBe('validation.areaTooLarge')
    }
  })

  it('should fail when latitude is outside Uzbekistan bounds (37.0 to 45.6)', () => {
    const tooLowLat = {
      name: 'Field Outside Uzb',
      areaHectares: 5,
      cropType: 'COTTON',
      soilType: 'LOAM',
      irrigationMethod: 'CANAL',
      latitude: 35.0, // Too south
      longitude: 60.0,
    }

    const tooHighLat = {
      name: 'Field Outside Uzb',
      areaHectares: 5,
      cropType: 'COTTON',
      soilType: 'LOAM',
      irrigationMethod: 'CANAL',
      latitude: 47.0, // Too north
      longitude: 60.0,
    }

    expect(fieldFormSchema.safeParse(tooLowLat).success).toBe(false)
    expect(fieldFormSchema.safeParse(tooHighLat).success).toBe(false)
  })

  it('should fail when longitude is outside Uzbekistan bounds (56.0 to 73.2)', () => {
    const tooWestLon = {
      name: 'Field West',
      areaHectares: 5,
      cropType: 'COTTON',
      soilType: 'LOAM',
      irrigationMethod: 'CANAL',
      latitude: 41.0,
      longitude: 50.0, // Too west
    }

    const tooEastLon = {
      name: 'Field East',
      areaHectares: 5,
      cropType: 'COTTON',
      soilType: 'LOAM',
      irrigationMethod: 'CANAL',
      latitude: 41.0,
      longitude: 75.0, // Too east
    }

    expect(fieldFormSchema.safeParse(tooWestLon).success).toBe(false)
    expect(fieldFormSchema.safeParse(tooEastLon).success).toBe(false)
  })

  it('should accept omitted or empty coordinates and description', () => {
    const minimalData = {
      name: 'Minimal Field',
      areaHectares: 8.5,
      cropType: 'WHEAT',
      soilType: 'SANDY_LOAM',
      irrigationMethod: 'FURROW',
      description: '',
      latitude: '',
      longitude: '',
    }

    const result = fieldFormSchema.safeParse(minimalData)
    expect(result.success).toBe(true)
  })
})
