import React, { useEffect, useRef, useMemo } from 'react'
import L from 'leaflet'
import {
  GisField,
  GisWaterSource,
  GisFieldWaterStatus,
  GisAllocationLink,
} from '@/entities/gis'
import { MapLayerState } from './MapLayerControl'
import {
  WATER_STATUS_STYLES,
  getWaterSourceSvgPin,
  getFieldCentroid,
  calculateCombinedBounds,
  YANGIARIQ_DEFAULT_CENTER,
} from '../gisWaterUtils'
import { geoJsonToLeafletLatLngs, validateBoundary } from '@/features/field/gis/gis.utils'
import { formatVolume, formatPercentage } from '@/features/water/lib/waterFormatters'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { cn } from '@/shared/lib/utils'

interface WaterFieldMapProps {
  fields: GisField[]
  waterSources: GisWaterSource[]
  waterStatuses: GisFieldWaterStatus[]
  allocations: GisAllocationLink[]
  layers: MapLayerState
  selectedField: GisField | null
  selectedWaterSource: GisWaterSource | null
  onSelectField: (field: GisField | null) => void
  onSelectWaterSource: (source: GisWaterSource | null) => void
  onInvalidGeometryDetected?: (warning: string) => void
  lang: SupportedLanguage
  className?: string
  height?: string
}

export const WaterFieldMap: React.FC<WaterFieldMapProps> = ({
  fields,
  waterSources,
  waterStatuses,
  allocations,
  layers,
  selectedField,
  selectedWaterSource,
  onSelectField,
  onSelectWaterSource,
  onInvalidGeometryDetected,
  lang,
  className,
  height = '620px',
}) => {
  const mapContainerRef = useRef<HTMLDivElement | null>(null)
  const mapInstanceRef = useRef<L.Map | null>(null)
  const featureGroupRef = useRef<L.FeatureGroup | null>(null)
  const linkagesGroupRef = useRef<L.FeatureGroup | null>(null)
  const isInitialFitDone = useRef(false)
  const t = translations[lang]

  // Map of fieldId to water status for O(1) lookup
  const statusMap = useMemo(() => {
    const map = new Map<string, GisFieldWaterStatus>()
    for (const s of waterStatuses) {
      map.set(s.fieldId, s)
    }
    return map
  }, [waterStatuses])

  // -------------------------------------------------------------
  // Map Lifecycle: Initialize once
  // -------------------------------------------------------------
  useEffect(() => {
    if (!mapContainerRef.current) return

    if (mapInstanceRef.current) {
      mapInstanceRef.current.remove()
      mapInstanceRef.current = null
    }

    const map = L.map(mapContainerRef.current, {
      center: [YANGIARIQ_DEFAULT_CENTER.lat, YANGIARIQ_DEFAULT_CENTER.lng],
      zoom: 12,
      zoomControl: true,
      scrollWheelZoom: true,
      attributionControl: true,
    })

    // OpenStreetMap Standard Tiles
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution:
        '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(map)

    const featureGroup = L.featureGroup().addTo(map)
    const linkagesGroup = L.featureGroup().addTo(map)
    featureGroupRef.current = featureGroup
    linkagesGroupRef.current = linkagesGroup
    mapInstanceRef.current = map

    const timer = setTimeout(() => {
      map.invalidateSize()
    }, 200)

    return () => {
      clearTimeout(timer)
      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove()
        mapInstanceRef.current = null
      }
      featureGroupRef.current = null
      linkagesGroupRef.current = null
    }
  }, [])

  // -------------------------------------------------------------
  // Layer Rendering: Fields, Water Sources & Popups
  // -------------------------------------------------------------
  useEffect(() => {
    const map = mapInstanceRef.current
    const featureGroup = featureGroupRef.current
    if (!map || !featureGroup) return

    featureGroup.clearLayers()

    // 1. Render Field Polygons
    if (layers.showFields) {
      for (const field of fields) {
        const isSelected = selectedField?.id === field.id
        const status = statusMap.get(field.id)
        const category = status?.category || 'NO_ALLOCATION'

        // Determine polygon coloring
        let strokeColor = '#059669'
        let fillColor = '#10b981'

        if (layers.showWaterStatus) {
          const style = WATER_STATUS_STYLES[category]
          strokeColor = style.color
          fillColor = style.fillColor
        } else {
          strokeColor = field.status === 'ACTIVE' ? '#059669' : '#6b7280'
          fillColor = field.status === 'ACTIVE' ? '#10b981' : '#9ca3af'
        }

        if (isSelected) {
          strokeColor = '#2563eb' // Distinct Blue highlight
        }

        // Validate boundary
        if (field.boundary) {
          const validation = validateBoundary(field.boundary)
          if (!validation.valid) {
            onInvalidGeometryDetected?.(
              `Dala "${field.name}" chegarasida xatolik: ${validation.error}`
            )
            continue
          }
        }

        const latlngs = geoJsonToLeafletLatLngs(field.boundary)
        if (latlngs.length >= 3) {
          const polygon = L.polygon(latlngs, {
            color: strokeColor,
            weight: isSelected ? 4 : 2,
            opacity: isSelected ? 1.0 : 0.85,
            fillColor,
            fillOpacity: isSelected ? 0.45 : 0.25,
            dashArray: category === 'OVER_CONSUMED' && layers.showWaterStatus ? '4, 4' : undefined,
          })

          // Build popup
          const allocated = status?.totalAllocatedVolumeM3 || 0
          const consumed = status?.totalConsumedVolumeM3 || 0
          const percent = status?.consumptionPercentage || 0
          const isOver = status?.isOverConsumed || false

          const popupContent = `
            <div style="font-family: inherit; font-size: 12px; min-width: 170px; padding: 2px;">
              <div style="font-weight: 700; font-size: 13px; color: #111827; margin-bottom: 2px;">
                ${field.name}
              </div>
              <div style="color: #4b5563; font-size: 11px; margin-bottom: 6px;">
                ${field.cropType} • ${Number(field.areaHectares).toFixed(2)} ga
              </div>
              <div style="background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 6px; padding: 6px; margin-bottom: 6px;">
                <div style="display: flex; justify-content: space-between; margin-bottom: 2px;">
                  <span style="color: #6b7280;">Ajratilgan:</span>
                  <strong style="color: #111827;">${formatVolume(allocated)}</strong>
                </div>
                <div style="display: flex; justify-content: space-between; margin-bottom: 2px;">
                  <span style="color: #6b7280;">Sarflangan:</span>
                  <strong style="color: ${isOver ? '#e11d48' : '#111827'};">${formatVolume(consumed)}</strong>
                </div>
                <div style="display: flex; justify-content: space-between; border-top: 1px dashed #d1d5db; padding-top: 2px; margin-top: 2px;">
                  <span style="color: #6b7280;">Ishlatildi:</span>
                  <strong style="color: ${isOver ? '#e11d48' : '#059669'};">${formatPercentage(percent)}</strong>
                </div>
              </div>
              ${isOver ? '<div style="color: #e11d48; font-weight: 700; font-size: 10px; margin-bottom: 4px;">⚠ MEʼYORDAN ORTIQ SARFLANGAN</div>' : ''}
              <div style="font-size: 10px; color: #6b7280; text-align: center;">Batafsil maʼlumot uchun bosing</div>
            </div>
          `

          polygon.bindPopup(popupContent)

          polygon.on('click', () => {
            onSelectField(field)
          })

          polygon.addTo(featureGroup)
        } else if (field.latitude != null && field.longitude != null) {
          // Centroid marker if polygon absent
          const pinHtml = `
            <div style="background: ${fillColor}; border: 2px solid ${strokeColor}; width: 14px; height: 14px; border-radius: 50%; box-shadow: 0 1px 3px rgba(0,0,0,0.3);"></div>
          `
          const marker = L.marker([field.latitude, field.longitude], {
            icon: L.divIcon({
              className: 'custom-centroid-pin',
              html: pinHtml,
              iconSize: [14, 14],
              iconAnchor: [7, 7],
            }),
          })
          marker.on('click', () => onSelectField(field))
          marker.addTo(featureGroup)
        }
      }
    }

    // 2. Render Water Sources
    if (layers.showWaterSources) {
      for (const source of waterSources) {
        if (source.latitude == null || source.longitude == null) continue

        const isSelected = selectedWaterSource?.id === source.id
        const pinHtml = getWaterSourceSvgPin(source.type, source.status)

        const marker = L.marker([source.latitude, source.longitude], {
          icon: L.divIcon({
            className: cn('custom-water-source-pin', isSelected && 'custom-pin-selected'),
            html: pinHtml,
            iconSize: [32, 32],
            iconAnchor: [16, 32],
            popupAnchor: [0, -32],
          }),
          zIndexOffset: isSelected ? 1000 : 500,
        })

        const sourceAllocations = allocations.filter((a) => a.waterSourceId === source.id)
        const totalAllocated = sourceAllocations.reduce(
          (sum, a) => sum + Number(a.allocatedVolumeM3 || 0),
          0
        )

        const popupContent = `
          <div style="font-family: inherit; font-size: 12px; min-width: 170px; padding: 2px;">
            <div style="font-weight: 700; font-size: 13px; color: #111827; margin-bottom: 2px;">
              ${source.name}
            </div>
            <div style="color: #0284c7; font-size: 11px; font-weight: 600; margin-bottom: 6px;">
              ${t.water.sourceTypes[source.type as keyof typeof t.water.sourceTypes] || source.type}
            </div>
            <div style="background: #f0f9ff; border: 1px solid #bae6fd; border-radius: 6px; padding: 6px; margin-bottom: 6px;">
              <div style="display: flex; justify-content: space-between; margin-bottom: 2px;">
                <span style="color: #0369a1;">Biriktirilgan dalalar:</span>
                <strong style="color: #0c4a6e;">${sourceAllocations.length} ta</strong>
              </div>
              <div style="display: flex; justify-content: space-between;">
                <span style="color: #0369a1;">Jami limit:</span>
                <strong style="color: #0c4a6e;">${formatVolume(totalAllocated)}</strong>
              </div>
            </div>
            <div style="font-size: 10px; color: #6b7280; text-align: center;">Batafsil maʼlumot uchun bosing</div>
          </div>
        `

        marker.bindPopup(popupContent)
        marker.on('click', () => {
          onSelectWaterSource(source)
        })

        marker.addTo(featureGroup)
      }
    }

    // Fit bounds on first load
    if (!isInitialFitDone.current) {
      const bounds = calculateCombinedBounds(fields, waterSources)
      if (bounds) {
        try {
          map.fitBounds(bounds, { padding: [32, 32], maxZoom: 15 })
          isInitialFitDone.current = true
        } catch {
          // Fallback
        }
      }
    }
  }, [
    fields,
    waterSources,
    statusMap,
    allocations,
    layers,
    selectedField,
    selectedWaterSource,
    onSelectField,
    onSelectWaterSource,
    onInvalidGeometryDetected,
    t,
  ])

  // -------------------------------------------------------------
  // Supply Linkage Polylines
  // -------------------------------------------------------------
  useEffect(() => {
    const linkagesGroup = linkagesGroupRef.current
    if (!linkagesGroup) return

    linkagesGroup.clearLayers()

    if (!layers.showSupplyLinks) return

    // 1. If Field is selected: draw lines to supplying water sources
    if (selectedField) {
      const fieldCentroid = getFieldCentroid(selectedField)
      if (fieldCentroid) {
        const fieldAllocations = allocations.filter((a) => a.fieldId === selectedField.id)
        const sourceIds = new Set(fieldAllocations.map((a) => a.waterSourceId))
        const connectedSources = waterSources.filter((s) => sourceIds.has(s.id))

        for (const source of connectedSources) {
          if (source.latitude != null && source.longitude != null) {
            const line = L.polyline(
              [
                [source.latitude, source.longitude],
                [fieldCentroid[0], fieldCentroid[1]],
              ],
              {
                color: '#0284c7', // Sky blue
                weight: 2.5,
                dashArray: '6, 8',
                opacity: 0.85,
              }
            )
            line.addTo(linkagesGroup)
          }
        }
      }
    }

    // 2. If Water Source is selected: draw lines to serviced fields
    if (selectedWaterSource && selectedWaterSource.latitude != null && selectedWaterSource.longitude != null) {
      const sourceAllocations = allocations.filter((a) => a.waterSourceId === selectedWaterSource.id)
      const fieldIds = new Set(sourceAllocations.map((a) => a.fieldId))
      const connectedFields = fields.filter((f) => fieldIds.has(f.id))

      for (const field of connectedFields) {
        const centroid = getFieldCentroid(field)
        if (centroid) {
          const line = L.polyline(
            [
              [selectedWaterSource.latitude, selectedWaterSource.longitude],
              [centroid[0], centroid[1]],
            ],
            {
              color: '#0284c7',
              weight: 2.5,
              dashArray: '6, 8',
              opacity: 0.85,
            }
          )
          line.addTo(linkagesGroup)
        }
      }
    }
  }, [selectedField, selectedWaterSource, allocations, fields, waterSources, layers.showSupplyLinks])

  // -------------------------------------------------------------
  // Selection Focus: Smoothly Pan / FitBounds on selection
  // -------------------------------------------------------------
  useEffect(() => {
    const map = mapInstanceRef.current
    if (!map) return

    if (selectedField) {
      const latlngs = geoJsonToLeafletLatLngs(selectedField.boundary)
      if (latlngs.length >= 3) {
        try {
          const polygon = L.polygon(latlngs)
          map.fitBounds(polygon.getBounds(), { padding: [48, 48], maxZoom: 16 })
        } catch {
          // Fallback
        }
      } else if (selectedField.latitude != null && selectedField.longitude != null) {
        map.panTo([selectedField.latitude, selectedField.longitude])
      }
    } else if (selectedWaterSource) {
      if (selectedWaterSource.latitude != null && selectedWaterSource.longitude != null) {
        map.panTo([selectedWaterSource.latitude, selectedWaterSource.longitude])
      }
    }
  }, [selectedField, selectedWaterSource])

  return (
    <div
      ref={mapContainerRef}
      style={{ height }}
      className={cn(
        'w-full rounded-xl overflow-hidden shadow-inner border border-border relative z-0',
        className
      )}
      tabIndex={0}
      aria-label="Interactive Agricultural GIS Map"
    />
  )
}
