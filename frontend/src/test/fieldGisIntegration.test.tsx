import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { FieldMap } from '@/features/field/gis/FieldMap'
import { FieldDetailsModal, FieldFormDialog } from '@/features/field/components'
import { Field, FieldBoundary } from '@/entities/field'

const dummyBoundary: FieldBoundary = {
  type: 'Polygon',
  coordinates: [
    [
      [60.51, 41.41],
      [60.52, 41.41],
      [60.52, 41.42],
      [60.51, 41.42],
      [60.51, 41.41],
    ],
  ],
}

const dummyGisField: Field = {
  id: 'gis-field-001',
  name: 'Yangiariq Paxtakor Maydoni',
  description: 'Gis sinov maydoni',
  areaHectares: 12.5,
  cropType: 'COTTON',
  soilType: 'LOAM',
  irrigationMethod: 'CANAL',
  latitude: 41.415,
  longitude: 60.515,
  boundary: dummyBoundary,
  status: 'ACTIVE',
  createdAt: '2026-10-31T00:00:00Z',
  updatedAt: '2026-10-31T00:00:00Z',
}

describe('Field GIS Component & Integration Tests', () => {
  it('FieldMap should render successfully with latitude, longitude and boundary', () => {
    const { container } = render(
      <FieldMap
        latitude={dummyGisField.latitude}
        longitude={dummyGisField.longitude}
        boundary={dummyGisField.boundary}
        fieldName={dummyGisField.name}
        cropType="Paxta"
        areaHectares={dummyGisField.areaHectares}
        status="ACTIVE"
      />
    )

    // Verify map container element exists
    expect(container.querySelector('.leaflet-container')).toBeInTheDocument()
  })

  it('FieldDetailsModal should render embedded FieldMap and boundary polygon metadata', () => {
    render(
      <FieldDetailsModal
        field={dummyGisField}
        isOpen={true}
        onClose={vi.fn()}
        lang="uz"
        userRole="FARMER"
        onEdit={vi.fn()}
        onDeactivate={vi.fn()}
        onReactivate={vi.fn()}
      />
    )

    // Check title and polygon indicator
    expect(screen.getByText('Yangiariq Paxtakor Maydoni')).toBeInTheDocument()
    expect(screen.getAllByText('5 nuqta (Poligon)').length).toBeGreaterThanOrEqual(1)
    // Coordinates formatted in readable format
    expect(screen.getByText(/41.415000° N/i)).toBeInTheDocument()
    expect(screen.getByText(/60.515000° E/i)).toBeInTheDocument()
  })

  it('FieldFormDialog should provide Location Picker and Boundary Drawing triggers', () => {
    render(
      <FieldFormDialog
        isOpen={true}
        onClose={vi.fn()}
        fieldToEdit={dummyGisField}
        onSubmit={vi.fn()}
        isSubmitting={false}
        lang="uz"
      />
    )

    // Verify "Xaritada tanlash" (Select on Map) button exists
    expect(screen.getByRole('button', { name: /Xaritada tanlash/i })).toBeInTheDocument()

    // Verify boundary edit button exists
    expect(screen.getByRole('button', { name: /Chegarani tahrirlash/i })).toBeInTheDocument()
    expect(screen.getByText(/Poligon biriktirilgan/i)).toBeInTheDocument()
  })
})
