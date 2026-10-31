import React, { useEffect, useRef } from 'react'
import L from 'leaflet'
import { FieldMapProps } from './gis.types'
import {
  geoJsonToLeafletLatLngs,
  YANGIARIQ_DEFAULT_CENTER,
} from './gis.utils'
import { cn } from '@/shared/lib/utils'

export const FieldMap: React.FC<FieldMapProps> = ({
  latitude,
  longitude,
  boundary,
  fieldName,
  cropType,
  areaHectares,
  status = 'ACTIVE',
  className,
  height = '320px',
  interactive = true,
  showPopup = true,
}) => {
  const mapContainerRef = useRef<HTMLDivElement | null>(null)
  const mapInstanceRef = useRef<L.Map | null>(null)

  useEffect(() => {
    if (!mapContainerRef.current) return

    // Avoid double initialization in React StrictMode
    if (mapInstanceRef.current) {
      mapInstanceRef.current.remove()
      mapInstanceRef.current = null
    }

    const hasCoords =
      typeof latitude === 'number' &&
      typeof longitude === 'number' &&
      !isNaN(latitude) &&
      !isNaN(longitude)

    const initialCenter: [number, number] = hasCoords
      ? [latitude, longitude]
      : [YANGIARIQ_DEFAULT_CENTER.lat, YANGIARIQ_DEFAULT_CENTER.lng]

    const initialZoom = hasCoords ? 14 : 11

    const map = L.map(mapContainerRef.current, {
      center: initialCenter,
      zoom: initialZoom,
      zoomControl: interactive,
      dragging: interactive,
      scrollWheelZoom: interactive ? 'center' : false,
      doubleClickZoom: interactive,
      touchZoom: interactive,
      attributionControl: true,
    })

    // OpenStreetMap Standard Tiles
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution:
        '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
    }).addTo(map)

    mapInstanceRef.current = map

    const isActive = status === 'ACTIVE'
    const color = isActive ? '#059669' : '#6b7280' // Emerald vs Slate
    const fillColor = isActive ? '#10b981' : '#9ca3af'

    let polygonLayer: L.Polygon | null = null
    let markerLayer: L.Marker | null = null

    // 1. Render Boundary Polygon if available
    const polygonLatLngs = geoJsonToLeafletLatLngs(boundary)
    if (polygonLatLngs.length >= 3) {
      polygonLayer = L.polygon(polygonLatLngs, {
        color,
        weight: 2.5,
        opacity: 0.9,
        fillColor,
        fillOpacity: 0.25,
      }).addTo(map)

      try {
        map.fitBounds(polygonLayer.getBounds(), {
          padding: [24, 24],
          maxZoom: 16,
        })
      } catch {
        // fitBounds fallback
      }
    }

    // 2. Render Centroid Marker if coordinates available
    if (hasCoords) {
      // Create clean SVG pin icon
      const pinSvg = `
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="28" height="28" fill="${color}" stroke="#ffffff" stroke-width="1.5">
          <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"/>
          <circle cx="12" cy="9" r="2.5" fill="#ffffff"/>
        </svg>
      `
      const customIcon = L.divIcon({
        className: 'custom-field-pin',
        html: pinSvg,
        iconSize: [28, 28],
        iconAnchor: [14, 28],
        popupAnchor: [0, -28],
      })

      markerLayer = L.marker([latitude, longitude], { icon: customIcon }).addTo(map)
    }

    // 3. Attach Popup
    if (showPopup && (fieldName || cropType || areaHectares)) {
      const popupContent = `
        <div style="font-family: inherit; font-size: 12px; padding: 2px 4px;">
          <strong style="display:block; font-size: 13px; margin-bottom: 3px; color: #111827;">
            ${fieldName || 'Dala'}
          </strong>
          ${cropType ? `<div style="color: #4b5563;">Ekin: <strong>${cropType}</strong></div>` : ''}
          ${areaHectares ? `<div style="color: #4b5563;">Maydon: <strong>${Number(areaHectares).toFixed(2)} ga</strong></div>` : ''}
          <div style="margin-top: 4px; display: inline-block; padding: 2px 6px; border-radius: 4px; font-size: 10px; font-weight: 600; background: ${isActive ? '#ecfdf5' : '#f3f4f6'}; color: ${isActive ? '#047857' : '#4b5563'};">
            ${isActive ? 'Faol (Active)' : 'Faol emas (Inactive)'}
          </div>
        </div>
      `
      if (polygonLayer) {
        polygonLayer.bindPopup(popupContent)
      } else if (markerLayer) {
        markerLayer.bindPopup(popupContent)
      }
    }

    // Invalidate size after rendering to ensure proper tile coverage in dialogs/tabs
    const timer = setTimeout(() => {
      map.invalidateSize()
    }, 200)

    return () => {
      clearTimeout(timer)
      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove()
        mapInstanceRef.current = null
      }
    }
  }, [latitude, longitude, boundary, fieldName, cropType, areaHectares, status, interactive, showPopup])

  return (
    <div
      className={cn(
        'relative w-full rounded-lg overflow-hidden border border-border/80 bg-muted/20 shadow-2xs',
        className
      )}
      style={{ height }}
    >
      <div ref={mapContainerRef} className="w-full h-full z-0" />
    </div>
  )
}
