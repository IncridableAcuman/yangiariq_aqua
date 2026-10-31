import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { FieldToolbar, FieldTable, FieldCard } from '@/features/field/components'
import { Field } from '@/entities/field'

const dummyField: Field = {
  id: '00000000-0000-0000-0000-000000000001',
  name: 'Guliston 1',
  description: 'Sinov maydoni',
  areaHectares: 14.5,
  cropType: 'COTTON',
  soilType: 'LOAM',
  irrigationMethod: 'CANAL',
  status: 'ACTIVE',
  createdAt: '2026-10-31T00:00:00Z',
  updatedAt: '2026-10-31T00:00:00Z',
}

const dummyInactiveField: Field = {
  ...dummyField,
  id: '00000000-0000-0000-0000-000000000002',
  name: 'Guliston 2',
  status: 'INACTIVE',
}

const dummyStats = {
  total: 2,
  active: 1,
  inactive: 1,
  totalArea: 29.0,
}

describe('Field Role-Aware UX Controls', () => {
  it('FieldToolbar should show "Add Field" button for FARMER role', () => {
    render(
      <FieldToolbar
        lang="uz"
        userRole="FARMER"
        searchQuery=""
        onSearchChange={vi.fn()}
        onOpenCreateDialog={vi.fn()}
        stats={dummyStats}
      />
    )

    const addButton = screen.getByText('Yangi dala qoʻshish')
    expect(addButton).toBeInTheDocument()
  })

  it('FieldToolbar should HIDE "Add Field" button for OPERATOR role', () => {
    render(
      <FieldToolbar
        lang="uz"
        userRole="OPERATOR"
        searchQuery=""
        onSearchChange={vi.fn()}
        onOpenCreateDialog={vi.fn()}
        stats={dummyStats}
      />
    )

    expect(screen.queryByText('Yangi dala qoʻshish')).not.toBeInTheDocument()
  })

  it('FieldToolbar should HIDE "Add Field" button for ADMIN role (only farmers cultivate parcels)', () => {
    render(
      <FieldToolbar
        lang="uz"
        userRole="ADMIN"
        searchQuery=""
        onSearchChange={vi.fn()}
        onOpenCreateDialog={vi.fn()}
        stats={dummyStats}
      />
    )

    expect(screen.queryByText('Yangi dala qoʻshish')).not.toBeInTheDocument()
  })

  it('FieldTable should display edit and deactivate buttons for FARMER', () => {
    render(
      <FieldTable
        fields={[dummyField]}
        isLoading={false}
        lang="uz"
        userRole="FARMER"
        onView={vi.fn()}
        onEdit={vi.fn()}
        onDeactivate={vi.fn()}
        onReactivate={vi.fn()}
      />
    )

    // View button
    expect(screen.getByLabelText(/Batafsil koʻrish/i)).toBeInTheDocument()
    // Edit button
    expect(screen.getByLabelText(/Dala maʼlumotlarini tahrirlash/i)).toBeInTheDocument()
    // Deactivate button
    expect(screen.getByLabelText(/Faoliyatini toʻxtatish/i)).toBeInTheDocument()
  })

  it('FieldTable should HIDE mutation buttons for OPERATOR (read-only)', () => {
    render(
      <FieldTable
        fields={[dummyField]}
        isLoading={false}
        lang="uz"
        userRole="OPERATOR"
        onView={vi.fn()}
        onEdit={vi.fn()}
        onDeactivate={vi.fn()}
        onReactivate={vi.fn()}
      />
    )

    // View button is visible
    expect(screen.getByLabelText(/Batafsil koʻrish/i)).toBeInTheDocument()
    // Mutation buttons are hidden
    expect(screen.queryByLabelText(/Dala maʼlumotlarini tahrirlash/i)).not.toBeInTheDocument()
    expect(screen.queryByLabelText(/Faoliyatini toʻxtatish/i)).not.toBeInTheDocument()
    expect(screen.queryByLabelText(/Faoliyatini tiklash/i)).not.toBeInTheDocument()
  })

  it('FieldTable should display reactivate button for INACTIVE field for FARMER', () => {
    render(
      <FieldTable
        fields={[dummyInactiveField]}
        isLoading={false}
        lang="uz"
        userRole="FARMER"
        onView={vi.fn()}
        onEdit={vi.fn()}
        onDeactivate={vi.fn()}
        onReactivate={vi.fn()}
      />
    )

    expect(screen.getByLabelText(/Faoliyatini tiklash/i)).toBeInTheDocument()
    expect(screen.queryByLabelText(/Faoliyatini toʻxtatish/i)).not.toBeInTheDocument()
  })

  it('FieldCard should restrict mutation actions for OPERATOR', () => {
    render(
      <FieldCard
        field={dummyField}
        lang="uz"
        userRole="OPERATOR"
        onView={vi.fn()}
        onEdit={vi.fn()}
        onDeactivate={vi.fn()}
        onReactivate={vi.fn()}
      />
    )

    expect(screen.getByLabelText(/Batafsil koʻrish/i)).toBeInTheDocument()
    expect(screen.queryByLabelText(/Dala maʼlumotlarini tahrirlash/i)).not.toBeInTheDocument()
    expect(screen.queryByLabelText(/Faoliyatini toʻxtatish/i)).not.toBeInTheDocument()
  })
})
