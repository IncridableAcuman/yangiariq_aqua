import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import {
  calculateCombinedBounds,
  getFieldCentroid,
  getDateRangeFromPreset,
  WATER_STATUS_STYLES,
  DateRangePreset,
} from '@/features/water/gis/gisWaterUtils'
import { MapLegend } from '@/features/water/gis/components/MapLegend'
import { MapLayerControl } from '@/features/water/gis/components/MapLayerControl'
import { MapFilters, GisFilterState, DEFAULT_GIS_FILTERS } from '@/features/water/gis/components/MapFilters'
import { FieldWaterSidePanel } from '@/features/water/gis/components/FieldWaterSidePanel'
import { WaterFieldMap } from '@/features/water/gis/components/WaterFieldMap'
import { GisField, GisWaterSource, GisFieldWaterStatus, GisAllocationLink } from '@/entities/gis'

describe('GIS Water Intelligence Utilities & Calculations', () => {
  const dummyFieldWithCentroid: GisField = {
    id: 'field-1',
    ownerId: 'farmer-101',
    name: 'Paxtakor 1',
    areaHectares: 10,
    cropType: 'COTTON',
    soilType: 'LOAM',
    irrigationMethod: 'CANAL',
    status: 'ACTIVE',
    latitude: 41.34,
    longitude: 60.62,
    boundary: {
      type: 'Polygon',
      coordinates: [
        [
          [60.61, 41.33],
          [60.63, 41.33],
          [60.63, 41.35],
          [60.61, 41.35],
          [60.61, 41.33],
        ],
      ],
    },
  }

  const dummyFieldNoCentroid: GisField = {
    id: 'field-2',
    ownerId: 'farmer-101',
    name: 'Bugʻdoyzor 2',
    areaHectares: 5,
    cropType: 'WHEAT',
    soilType: 'CLAY',
    irrigationMethod: 'DRIP',
    status: 'ACTIVE',
    boundary: {
      type: 'Polygon',
      coordinates: [
        [
          [60.70, 41.40],
          [60.72, 41.40],
          [60.72, 41.42],
          [60.70, 41.42],
          [60.70, 41.40],
        ],
      ],
    },
  }

  const dummyWaterSource: GisWaterSource = {
    id: 'src-1',
    name: 'Shovot Kanali',
    type: 'CANAL',
    status: 'ACTIVE',
    latitude: 41.35,
    longitude: 60.65,
  }

  it('getFieldCentroid should prioritize explicit coordinates and fallback to polygon centroid', () => {
    const c1 = getFieldCentroid(dummyFieldWithCentroid)
    expect(c1).toEqual([41.34, 60.62])

    const c2 = getFieldCentroid(dummyFieldNoCentroid)
    expect(c2).not.toBeNull()
    if (c2) {
      expect(c2[0]).toBeCloseTo(41.41, 1)
      expect(c2[1]).toBeCloseTo(60.71, 1)
    }
  })

  it('calculateCombinedBounds should return correct latLng bounds array or fallback to null', () => {
    // Empty lists
    const emptyBounds = calculateCombinedBounds([], [])
    expect(emptyBounds).toBeNull()

    // With field and source
    const bounds = calculateCombinedBounds([dummyFieldWithCentroid], [dummyWaterSource])
    expect(bounds).not.toBeNull()
    if (bounds) {
      const [[minLat, minLng], [maxLat, maxLng]] = bounds
      expect(minLat).toBeLessThan(maxLat)
      expect(minLng).toBeLessThan(maxLng)
    }
  })

  it('getDateRangeFromPreset should correctly generate ISO timestamps for all presets', () => {
    const presets: DateRangePreset[] = ['today', '7days', '30days', 'season', 'custom', 'all']

    presets.forEach((p) => {
      const range = getDateRangeFromPreset(p)
      if (p === 'custom' || p === 'all') {
        expect(range.from).toBeUndefined()
        expect(range.to).toBeUndefined()
      } else {
        expect(range.from).toBeDefined()
        expect(range.to).toBeDefined()
        expect(new Date(range.from!).getTime()).toBeLessThanOrEqual(new Date(range.to!).getTime())
      }
    })
  })

  it('WATER_STATUS_STYLES should define distinct colors for NORMAL, WARNING, OVER_CONSUMED, NO_ALLOCATION', () => {
    expect(WATER_STATUS_STYLES.NORMAL.fillColor).toBe('#10b981')
    expect(WATER_STATUS_STYLES.WARNING.fillColor).toBe('#f59e0b')
    expect(WATER_STATUS_STYLES.OVER_CONSUMED.fillColor).toBe('#f43f5e')
    expect(WATER_STATUS_STYLES.NO_ALLOCATION.fillColor).toBe('#94a3b8')
  })
})

describe('GIS Map UI Controls & Layers', () => {
  it('MapLegend should render semantic status badges and source types', () => {
    render(<MapLegend lang="uz" />)
    expect(screen.getByText('Normal (≤80%)')).toBeInTheDocument()
    expect(screen.getByText('Ogohlantirish (80–100%)')).toBeInTheDocument()
    expect(screen.getByText(/Meʼyordan oshgan \(>100%\)/i)).toBeInTheDocument()
    expect(screen.getByText(/Limit ajratilmagan/i)).toBeInTheDocument()
    expect(screen.getByText('Kanal')).toBeInTheDocument()
    expect(screen.getByText(/Suv havzasi \/ Ombor/i)).toBeInTheDocument()
  })

  it('MapLayerControl should trigger callback on toggling layers', () => {
    const onChangeMock = vi.fn()
    render(
      <MapLayerControl
        layers={{
          showFields: true,
          showWaterSources: true,
          showWaterStatus: true,
          showSupplyLinks: false,
        }}
        onChange={onChangeMock}
        lang="uz"
      />
    )

    // Find checkboxes
    const checkboxes = screen.getAllByRole('checkbox')
    expect(checkboxes.length).toBe(4)

    // Toggle the 4th checkbox (supply links)
    fireEvent.click(checkboxes[3])
    expect(onChangeMock).toHaveBeenCalledWith({
      showFields: true,
      showWaterSources: true,
      showWaterStatus: true,
      showSupplyLinks: true,
    })
  })

  it('MapFilters should handle date preset change and reject invalid custom date ranges', () => {
    const onChangeMock = vi.fn()
    const onDateRangeChangeMock = vi.fn()

    const initialFilters: GisFilterState = {
      ...DEFAULT_GIS_FILTERS,
      datePreset: 'custom',
      customFrom: '2026-11-10',
      customTo: '2026-11-15',
    }

    render(
      <MapFilters
        filters={initialFilters}
        onChange={onChangeMock}
        onDateRangeChange={onDateRangeChangeMock}
        lang="uz"
      />
    )

    // Find custom date inputs
    const inputs = screen.getAllByDisplayValue(/2026-11/)
    expect(inputs.length).toBe(2)

    // Set 'to' input to an earlier date than 'from' (2026-11-01 < 2026-11-10)
    fireEvent.change(inputs[1], { target: { value: '2026-11-01' } })

    // Expect date error alert to appear
    expect(screen.getByText(/Boshlanish sanasi tugash sanasidan oldin boʻlishi kerak/i)).toBeInTheDocument()
  })
})

describe('GIS Field & Water Side Panel', () => {
  const dummyField: GisField = {
    id: 'f-101',
    ownerId: 'farmer-101',
    name: 'Markaziy Paxta Dalasi',
    areaHectares: 8.5,
    cropType: 'COTTON',
    soilType: 'LOAM',
    irrigationMethod: 'CANAL',
    status: 'ACTIVE',
  }

  const dummyStatus: GisFieldWaterStatus = {
    fieldId: 'f-101',
    totalAllocatedVolumeM3: 10000,
    totalConsumedVolumeM3: 12500,
    totalRemainingVolumeM3: 0,
    totalOverConsumedVolumeM3: 2500,
    isOverConsumed: true,
    consumptionPercentage: 125,
    category: 'OVER_CONSUMED',
    allocationCount: 1,
    irrigationEventCount: 2,
  }

  const dummySource: GisWaterSource = {
    id: 'src-201',
    name: 'Yangiariq Magistral Kanali',
    type: 'CANAL',
    status: 'ACTIVE',
  }

  const dummyAlloc: GisAllocationLink = {
    id: 'alloc-1',
    fieldId: 'f-101',
    waterSourceId: 'src-201',
    allocatedVolumeM3: 10000,
    status: 'ACTIVE',
  }

  it('FieldWaterSidePanel renders field details with critical over-consumption alert', () => {
    render(
      <FieldWaterSidePanel
        selectedField={dummyField}
        selectedWaterSource={null}
        waterStatus={dummyStatus}
        allocations={[dummyAlloc]}
        allWaterSources={[dummySource]}
        onClose={vi.fn()}
        lang="uz"
        userRole="FARMER"
      />
    )

    // Verify Field Header
    expect(screen.getByText('Markaziy Paxta Dalasi')).toBeInTheDocument()
    expect(screen.getByText(/8.50 ga/)).toBeInTheDocument()

    // Verify Over-consumption warning banner
    expect(screen.getByRole('alert')).toBeInTheDocument()
    expect(screen.getByText(/DIQQAT: SUV MEʼYORDAN ORTIQ SARFLANGAN/i)).toBeInTheDocument()
    expect(screen.getAllByText(/2 500 m³/i).length).toBeGreaterThanOrEqual(1)

    // Verify supplying source connection
    expect(screen.getByText('Yangiariq Magistral Kanali')).toBeInTheDocument()
  })

  it('FieldWaterSidePanel renders water source details and connected fields count', () => {
    render(
      <FieldWaterSidePanel
        selectedField={null}
        selectedWaterSource={dummySource}
        allocations={[dummyAlloc]}
        allFields={[dummyField]}
        onClose={vi.fn()}
        lang="uz"
        userRole="OPERATOR"
      />
    )

    expect(screen.getByText('Yangiariq Magistral Kanali')).toBeInTheDocument()
    expect(screen.getByText('1')).toBeInTheDocument() // 1 connected field
  })
})

describe('WaterFieldMap Resiliency & Leaflet Integration', () => {
  it('WaterFieldMap should mount and notify if invalid geometry is encountered without crashing', () => {
    const invalidField: GisField = {
      id: 'inv-1',
      ownerId: 'farmer-101',
      name: 'Buzuq Geometriyali Dala',
      areaHectares: 2,
      cropType: 'WHEAT',
      soilType: 'CLAY',
      irrigationMethod: 'FURROW',
      status: 'ACTIVE',
      boundary: {
        type: 'Polygon',
        // Incomplete polygon: only 2 coordinates
        coordinates: [
          [
            [60.5, 41.5],
            [60.6, 41.6],
          ],
        ],
      },
    }

    const warningHandler = vi.fn()

    const { container } = render(
      <WaterFieldMap
        fields={[invalidField]}
        waterSources={[]}
        waterStatuses={[]}
        allocations={[]}
        layers={{
          showFields: true,
          showWaterSources: true,
          showWaterStatus: true,
          showSupplyLinks: true,
        }}
        selectedField={null}
        selectedWaterSource={null}
        onSelectField={vi.fn()}
        onSelectWaterSource={vi.fn()}
        onInvalidGeometryDetected={warningHandler}
        lang="uz"
      />
    )

    expect(container.querySelector('.leaflet-container')).toBeInTheDocument()
    // Warning was triggered safely
    expect(warningHandler).toHaveBeenCalledWith(
      expect.stringContaining('Buzuq Geometriyali Dala')
    )
  })
})
