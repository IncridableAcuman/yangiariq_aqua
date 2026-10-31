import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import {
  FieldDeactivateDialog,
  FieldReactivateDialog,
} from '@/features/field/components'
import { Field } from '@/entities/field'

const sampleField: Field = {
  id: 'f-101',
  name: 'Bogʻot Chegarasi',
  description: 'Sinov maydoni',
  areaHectares: 18.0,
  cropType: 'COTTON',
  soilType: 'LOAM',
  irrigationMethod: 'CANAL',
  status: 'ACTIVE',
  createdAt: '2026-10-31T00:00:00Z',
  updatedAt: '2026-10-31T00:00:00Z',
}

describe('Field Lifecycle Confirmation Dialogs', () => {
  it('FieldDeactivateDialog should render soft-deactivation message and handle callbacks', async () => {
    const handleClose = vi.fn()
    const handleConfirm = vi.fn()

    render(
      <FieldDeactivateDialog
        field={sampleField}
        isOpen={true}
        onClose={handleClose}
        onConfirm={handleConfirm}
        isSubmitting={false}
        lang="uz"
      />
    )

    // Verify soft-deactivation preservation text is present
    expect(
      screen.getByText(/Dala faoliyatini toʻxtatishni tasdiqlaysizmi\?/i)
    ).toBeInTheDocument()
    expect(
      screen.getByText(/arxivda toʻliq saqlanadi/i)
    ).toBeInTheDocument()

    // Test Cancel button
    const cancelButton = screen.getByRole('button', { name: /Bekor qilish/i })
    fireEvent.click(cancelButton)
    expect(handleClose).toHaveBeenCalledTimes(1)

    // Test Confirm button
    const confirmButton = screen.getByRole('button', { name: /Ha, toʻxtatilsin/i })
    fireEvent.click(confirmButton)
    expect(handleConfirm).toHaveBeenCalledTimes(1)
  })

  it('FieldReactivateDialog should render reactivation prompt and handle callbacks', async () => {
    const handleClose = vi.fn()
    const handleConfirm = vi.fn()

    render(
      <FieldReactivateDialog
        field={{ ...sampleField, status: 'INACTIVE' }}
        isOpen={true}
        onClose={handleClose}
        onConfirm={handleConfirm}
        isSubmitting={false}
        lang="uz"
      />
    )

    expect(
      screen.getByText(/Dala faoliyatini qayta tiklashni tasdiqlaysizmi\?/i)
    ).toBeInTheDocument()

    const confirmButton = screen.getByRole('button', { name: /Tiklash/i })
    fireEvent.click(confirmButton)
    expect(handleConfirm).toHaveBeenCalledTimes(1)
  })
})
