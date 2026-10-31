import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { WaterSourceCatalog } from '@/features/water/components'
import { WaterSource } from '@/entities/water'

const dummySources: WaterSource[] = [
  {
    id: 'ws-1',
    name: 'Palvon-Yobi Primary Canal',
    type: 'CANAL',
    description: 'Main intake canal',
    status: 'ACTIVE',
    createdAt: '2026-04-01T08:00:00Z',
    updatedAt: '2026-04-01T08:00:00Z',
  },
  {
    id: 'ws-2',
    name: 'Artesian Well 12',
    type: 'WELL',
    description: 'Deep groundwater source',
    status: 'INACTIVE',
    createdAt: '2026-04-01T08:00:00Z',
    updatedAt: '2026-04-01T08:00:00Z',
  },
]

describe('Water Management Role-Based UX Controls', () => {
  it('WaterSourceCatalog displays "Register Water Source" and lifecycle action buttons when canManage is true (OPERATOR / ADMIN)', () => {
    render(
      <WaterSourceCatalog
        sources={dummySources}
        canManage={true}
        isLoading={false}
        lang="en"
        onCreateClick={vi.fn()}
        onEditClick={vi.fn()}
        onActivateClick={vi.fn()}
        onDeactivateClick={vi.fn()}
      />
    )

    // Header create action should be visible
    expect(screen.getByText('Register Water Source')).toBeInTheDocument()

    // Edit and Deactivate buttons for active source
    expect(screen.getByText('Deactivate')).toBeInTheDocument()

    // Activate button for inactive source
    expect(screen.getByText('Activate')).toBeInTheDocument()
  })

  it('WaterSourceCatalog HIDES administrative actions when canManage is false (FARMER role)', () => {
    render(
      <WaterSourceCatalog
        sources={dummySources}
        canManage={false}
        isLoading={false}
        lang="en"
        onCreateClick={vi.fn()}
        onEditClick={vi.fn()}
        onActivateClick={vi.fn()}
        onDeactivateClick={vi.fn()}
      />
    )

    // Farmer can see the list of sources for information
    expect(screen.getByText('Palvon-Yobi Primary Canal')).toBeInTheDocument()
    expect(screen.getByText('Artesian Well 12')).toBeInTheDocument()

    // Administrative action buttons must be completely hidden
    expect(screen.queryByText('Register Water Source')).not.toBeInTheDocument()
    expect(screen.queryByText('Deactivate')).not.toBeInTheDocument()
    expect(screen.queryByText('Activate')).not.toBeInTheDocument()
  })
})
