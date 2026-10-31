import { describe, it, expect, vi, beforeEach } from 'vitest'
import {
  getFields,
  getFieldById,
  createField,
  updateField,
  deactivateField,
  reactivateField,
  CreateFieldPayload,
  UpdateFieldPayload,
} from '@/entities/field'
import * as clientModule from '@/shared/api/client'

describe('Field API Client Operations', () => {
  beforeEach(() => {
    vi.restoreAllMocks()
  })

  it('getFields() should format query parameters correctly for status, crop, pagination, and sorting', async () => {
    const mockResponse: clientModule.ApiResponse<clientModule.PageResponse<any>> = {
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

    const apiClientSpy = vi.spyOn(clientModule, 'apiClient').mockResolvedValue(mockResponse)

    await getFields({
      status: 'ACTIVE',
      cropType: 'COTTON',
      soilType: 'LOAM',
      irrigationMethod: 'DRIP',
      page: 1,
      size: 20,
      sort: 'name,asc',
    })

    expect(apiClientSpy).toHaveBeenCalledTimes(1)
    const callEndpoint = apiClientSpy.mock.calls[0][0]
    expect(callEndpoint).toContain('/fields?')
    expect(callEndpoint).toContain('status=ACTIVE')
    expect(callEndpoint).toContain('cropType=COTTON')
    expect(callEndpoint).toContain('soilType=LOAM')
    expect(callEndpoint).toContain('irrigationMethod=DRIP')
    expect(callEndpoint).toContain('page=1')
    expect(callEndpoint).toContain('size=20')
    expect(callEndpoint).toContain('sort=name%2Casc')
  })

  it('getFieldById() should request /fields/{id} with GET', async () => {
    const mockField = {
      id: 'field-123',
      name: 'Test Field',
      areaHectares: 15,
      cropType: 'WHEAT',
      soilType: 'SANDY',
      irrigationMethod: 'CANAL',
      status: 'ACTIVE',
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z',
    }

    const apiClientSpy = vi.spyOn(clientModule, 'apiClient').mockResolvedValue({
      success: true,
      timestamp: '2026-10-31T00:00:00Z',
      data: mockField as any,
    })

    const result = await getFieldById('field-123')
    expect(apiClientSpy).toHaveBeenCalledWith('/fields/field-123', { method: 'GET' })
    expect(result.id).toBe('field-123')
  })

  it('createField() should send POST request with payload and never include ownerId', async () => {
    const payload: CreateFieldPayload = {
      name: 'New Field',
      description: 'Test notes',
      areaHectares: 25.5,
      cropType: 'COTTON',
      soilType: 'LOAM',
      irrigationMethod: 'CANAL',
    }

    const mockCreated = {
      id: 'new-uuid-1',
      ...payload,
      status: 'ACTIVE',
      createdAt: '2026-10-31T00:00:00Z',
      updatedAt: '2026-10-31T00:00:00Z',
    }

    const apiClientSpy = vi.spyOn(clientModule, 'apiClient').mockResolvedValue({
      success: true,
      timestamp: '2026-10-31T00:00:00Z',
      data: mockCreated as any,
    })

    const result = await createField(payload)
    expect(apiClientSpy).toHaveBeenCalledWith('/fields', {
      method: 'POST',
      body: JSON.stringify(payload),
    })

    // Security check: Payload must not have ownerId property
    expect((payload as any).ownerId).toBeUndefined()
    expect(result.id).toBe('new-uuid-1')
  })

  it('updateField() should send PATCH request to /fields/{id}', async () => {
    const payload: UpdateFieldPayload = {
      name: 'Updated Name',
      cropType: 'RICE',
    }

    const mockUpdated = {
      id: 'field-456',
      name: 'Updated Name',
      areaHectares: 10,
      cropType: 'RICE',
      soilType: 'LOAM',
      irrigationMethod: 'CANAL',
      status: 'ACTIVE',
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-10-31T00:00:00Z',
    }

    const apiClientSpy = vi.spyOn(clientModule, 'apiClient').mockResolvedValue({
      success: true,
      timestamp: '2026-10-31T00:00:00Z',
      data: mockUpdated as any,
    })

    const result = await updateField('field-456', payload)
    expect(apiClientSpy).toHaveBeenCalledWith('/fields/field-456', {
      method: 'PATCH',
      body: JSON.stringify(payload),
    })
    expect(result.name).toBe('Updated Name')
  })

  it('deactivateField() should send DELETE request to /fields/{id}', async () => {
    const apiClientSpy = vi.spyOn(clientModule, 'apiClient').mockResolvedValue({
      success: true,
      timestamp: '2026-10-31T00:00:00Z',
    })

    await deactivateField('field-789')
    expect(apiClientSpy).toHaveBeenCalledWith('/fields/field-789', {
      method: 'DELETE',
    })
  })

  it('reactivateField() should send PATCH request to /fields/{id}/activate', async () => {
    const mockReactivated = {
      id: 'field-789',
      name: 'Restored Field',
      areaHectares: 10,
      cropType: 'COTTON',
      soilType: 'LOAM',
      irrigationMethod: 'CANAL',
      status: 'ACTIVE',
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-10-31T00:00:00Z',
    }

    const apiClientSpy = vi.spyOn(clientModule, 'apiClient').mockResolvedValue({
      success: true,
      timestamp: '2026-10-31T00:00:00Z',
      data: mockReactivated as any,
    })

    const result = await reactivateField('field-789')
    expect(apiClientSpy).toHaveBeenCalledWith('/fields/field-789/activate', {
      method: 'PATCH',
    })
    expect(result.status).toBe('ACTIVE')
  })
})
