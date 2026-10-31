import React, { useEffect, useRef, useState } from 'react'
import L from 'leaflet'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import {
  Pentagon,
  RotateCcw,
  Trash2,
  Check,
  AlertCircle,
  HelpCircle,
  Info,
} from 'lucide-react'
import { FieldBoundary } from '@/entities/field'
import {
  geoJsonToLeafletLatLngs,
  leafletLatLngsToGeoJson,
  isInsideUzbekistan,
  calculatePolygonAreaHectares,
  YANGIARIQ_DEFAULT_CENTER,
} from './gis.utils'
import { LatLngPoint } from './gis.types'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface FieldBoundaryEditorModalProps {
  isOpen: boolean
  onClose: () => void
  initialBoundary?: FieldBoundary | null
  initialLat?: number | null
  initialLng?: number | null
  registeredAreaHectares?: number
  onSaveBoundary: (boundary: FieldBoundary | null) => void
  lang: SupportedLanguage
}

export const FieldBoundaryEditorModal: React.FC<FieldBoundaryEditorModalProps> = ({
  isOpen,
  onClose,
  initialBoundary,
  initialLat,
  initialLng,
  registeredAreaHectares,
  onSaveBoundary,
  lang,
}) => {
  const mapContainerRef = useRef<HTMLDivElement | null>(null)
  const mapInstanceRef = useRef<L.Map | null>(null)

  // Layers
  const polygonLayerRef = useRef<L.Polygon | null>(null)
  const polylineLayerRef = useRef<L.Polyline | null>(null)
  const vertexMarkersRef = useRef<L.Marker[]>([])

  // State
  const [points, setPoints] = useState<LatLngPoint[]>([])
  const [isClosed, setIsClosed] = useState(false)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  // Calculated area in hectares
  const estimatedArea = calculatePolygonAreaHectares(points)

  // Area consistency warning check
  const showAreaMismatchWarning =
    isClosed &&
    points.length >= 3 &&
    typeof registeredAreaHectares === 'number' &&
    registeredAreaHectares > 0 &&
    Math.abs(estimatedArea - registeredAreaHectares) / registeredAreaHectares > 0.35

  // Initialize or reset points from initialBoundary
  useEffect(() => {
    if (!isOpen) return

    setErrorMessage(null)
    const latlngs = geoJsonToLeafletLatLngs(initialBoundary)

    if (latlngs.length >= 3) {
      // Exclude closing duplicate if present
      const uniquePoints: LatLngPoint[] = []
      for (let i = 0; i < latlngs.length; i++) {
        if (
          i === latlngs.length - 1 &&
          Math.abs(latlngs[i][0] - latlngs[0][0]) < 1e-6 &&
          Math.abs(latlngs[i][1] - latlngs[0][1]) < 1e-6
        ) {
          continue
        }
        uniquePoints.push({ lat: latlngs[i][0], lng: latlngs[i][1] })
      }
      setPoints(uniquePoints)
      setIsClosed(true)
    } else {
      setPoints([])
      setIsClosed(false)
    }
  }, [isOpen, initialBoundary])

  // Setup Leaflet map instance
  useEffect(() => {
    if (!isOpen) {
      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove()
        mapInstanceRef.current = null
      }
      return
    }

    const timer = setTimeout(() => {
      if (!mapContainerRef.current) return

      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove()
        mapInstanceRef.current = null
      }

      const hasCoords =
        typeof initialLat === 'number' &&
        typeof initialLng === 'number' &&
        isInsideUzbekistan(initialLat, initialLng)

      const startCenter: LatLngPoint = hasCoords
        ? { lat: initialLat!, lng: initialLng! }
        : points.length > 0
        ? points[0]
        : YANGIARIQ_DEFAULT_CENTER

      const map = L.map(mapContainerRef.current, {
        center: [startCenter.lat, startCenter.lng],
        zoom: points.length > 0 ? 15 : 13,
      })

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution:
          '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      }).addTo(map)

      mapInstanceRef.current = map

      // Map click handler when drawing
      map.on('click', (e: L.LeafletMouseEvent) => {
        if (!isInsideUzbekistan(e.latlng.lat, e.latlng.lng)) {
          setErrorMessage(
            lang === 'uz'
              ? 'Nuqta Oʻzbekiston hududidan tashqarida'
              : lang === 'ru'
              ? 'Точка находится за пределами Узбекистана'
              : 'Point is outside Uzbekistan bounds'
          )
          return
        }

        setErrorMessage(null)
        setPoints((prev) => {
          if (isClosed) return prev // Can't add points to closed polygon
          return [...prev, { lat: e.latlng.lat, lng: e.latlng.lng }]
        })
      })

      map.invalidateSize()
    }, 150)

    return () => {
      clearTimeout(timer)
      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove()
        mapInstanceRef.current = null
      }
    }
  }, [isOpen, initialLat, initialLng, isClosed, lang])

  // Synchronize Leaflet layers with current `points` state
  useEffect(() => {
    const map = mapInstanceRef.current
    if (!map) return

    // Clean up previous layers
    if (polygonLayerRef.current) {
      polygonLayerRef.current.remove()
      polygonLayerRef.current = null
    }
    if (polylineLayerRef.current) {
      polylineLayerRef.current.remove()
      polylineLayerRef.current = null
    }
    vertexMarkersRef.current.forEach((m) => m.remove())
    vertexMarkersRef.current = []

    if (points.length === 0) return

    const latlngs: [number, number][] = points.map((p) => [p.lat, p.lng])

    if (isClosed && points.length >= 3) {
      // 1. Render Completed Closed Polygon
      const polygon = L.polygon(latlngs, {
        color: '#059669',
        weight: 2.5,
        fillColor: '#10b981',
        fillOpacity: 0.25,
      }).addTo(map)
      polygonLayerRef.current = polygon

      // 2. Render Draggable Vertex Markers for editing
      points.forEach((pt, idx) => {
        const handleHtml = `
          <div style="background: #ffffff; border: 2.5px solid #059669; border-radius: 50%; width: 14px; height: 14px; box-shadow: 0 1px 3px rgba(0,0,0,0.3); cursor: grab;"></div>
        `
        const handleIcon = L.divIcon({
          className: 'vertex-handle',
          html: handleHtml,
          iconSize: [14, 14],
          iconAnchor: [7, 7],
        })

        const marker = L.marker([pt.lat, pt.lng], {
          icon: handleIcon,
          draggable: true,
        }).addTo(map)

        marker.on('drag', () => {
          const newLatLng = marker.getLatLng()
          if (!isInsideUzbekistan(newLatLng.lat, newLatLng.lng)) return

          setPoints((prev) => {
            const next = [...prev]
            next[idx] = { lat: newLatLng.lat, lng: newLatLng.lng }
            return next
          })
        })

        vertexMarkersRef.current.push(marker)
      })

      try {
        map.fitBounds(polygon.getBounds(), { padding: [24, 24], maxZoom: 16 })
      } catch {
        // ignore
      }
    } else {
      // Render Open Polyline during drawing
      const polyline = L.polyline(latlngs, {
        color: '#2563eb',
        weight: 2,
        dashArray: '6, 6',
      }).addTo(map)
      polylineLayerRef.current = polyline

      // Render vertex dots
      points.forEach((pt, idx) => {
        const isFirst = idx === 0
        const handleHtml = `
          <div style="background: ${isFirst ? '#ef4444' : '#2563eb'}; border: 2px solid #ffffff; border-radius: 50%; width: 12px; height: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.4);"></div>
        `
        const handleIcon = L.divIcon({
          className: 'drawing-dot',
          html: handleHtml,
          iconSize: [12, 12],
          iconAnchor: [6, 6],
        })

        const marker = L.marker([pt.lat, pt.lng], { icon: handleIcon }).addTo(map)

        // If clicking on the first point when we have at least 3 points -> close the polygon!
        if (isFirst && points.length >= 3) {
          marker.on('click', (e) => {
            L.DomEvent.stopPropagation(e)
            setIsClosed(true)
          })
        }

        vertexMarkersRef.current.push(marker)
      })
    }
  }, [points, isClosed])

  const handleFinishDrawing = () => {
    if (points.length < 3) {
      setErrorMessage(
        lang === 'uz'
          ? 'Poligonni yopish uchun kamida 3 ta nuqta kerak'
          : lang === 'ru'
          ? 'Для завершения полигона требуется минимум 3 точки'
          : 'At least 3 vertices are required to close polygon'
      )
      return
    }
    setIsClosed(true)
  }

  const handleRedraw = () => {
    setPoints([])
    setIsClosed(false)
    setErrorMessage(null)
  }

  const handleClear = () => {
    setPoints([])
    setIsClosed(false)
    setErrorMessage(null)
  }

  const handleSave = () => {
    if (points.length === 0) {
      onSaveBoundary(null)
      onClose()
      return
    }

    if (points.length < 3 || !isClosed) {
      setErrorMessage(
        lang === 'uz'
          ? 'Chegara chizigʻini yopish talab etiladi'
          : lang === 'ru'
          ? 'Требуется замкнуть контур границ'
          : 'Please finish and close the polygon before saving'
      )
      return
    }

    try {
      const geoJsonBoundary = leafletLatLngsToGeoJson(points)
      onSaveBoundary(geoJsonBoundary)
      onClose()
    } catch (err: unknown) {
      setErrorMessage(err instanceof Error ? err.message : 'Invalid boundary')
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-3xl max-h-[94vh] flex flex-col p-4 sm:p-6">
        <DialogHeader className="space-y-1">
          <div className="flex items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              <div className="h-7 w-7 rounded-md bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 flex items-center justify-center shrink-0">
                <Pentagon className="h-4 w-4" />
              </div>
              <DialogTitle className="text-base font-bold text-foreground">
                {lang === 'uz'
                  ? 'Dala chegarasini xaritada chizish / tahrirlash'
                  : lang === 'ru'
                  ? 'Отрисовка и редактирование границ поля'
                  : 'Draw & Edit Field Boundary'}
              </DialogTitle>
            </div>
            {isClosed && points.length >= 3 && (
              <span className="text-xs font-semibold px-2.5 py-1 rounded-full bg-emerald-50 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-300 border border-emerald-300 dark:border-emerald-800">
                {points.length} nuqta • {estimatedArea.toFixed(2)} ga
              </span>
            )}
          </div>
          <DialogDescription className="text-xs text-muted-foreground">
            {!isClosed ? (
              <span className="flex items-center gap-1.5 text-blue-600 dark:text-blue-400 font-medium">
                <HelpCircle className="h-3.5 w-3.5" />
                {lang === 'uz'
                  ? 'Xaritada nuqtalarni ketma-ket bosing. Yakunlash uchun birinchi qizil nuqtaga bosing yoki "Yakunlash" tugmasini bosing.'
                  : lang === 'ru'
                  ? 'Кликайте на карту для добавления точек. Для завершения кликните на первую точку или кнопку "Замкнуть".'
                  : 'Click on map to add boundary vertices. Click the first red dot or "Finish" button to close polygon.'}
              </span>
            ) : (
              <span>
                {lang === 'uz'
                  ? 'Chegara yopilgan. Nuqtalarni sudrab shaklni tahrirlashingiz mumkin.'
                  : lang === 'ru'
                  ? 'Контур замкнут. Вы можете перемещать точки для точной подгонки.'
                  : 'Polygon is closed. You can drag handle markers to adjust vertices.'}
              </span>
            )}
          </DialogDescription>
        </DialogHeader>

        {/* Map Container */}
        <div className="relative w-full h-[380px] sm:h-[420px] rounded-lg overflow-hidden border border-border mt-2 my-2 bg-muted/20">
          <div ref={mapContainerRef} className="w-full h-full z-0" />

          {/* Floating Action Controls on Top of Map */}
          <div className="absolute top-3 right-3 z-[400] flex items-center gap-1.5 bg-background/90 backdrop-blur-xs p-1.5 rounded-lg border border-border shadow-md">
            {!isClosed && points.length >= 3 && (
              <Button
                size="sm"
                variant="default"
                onClick={handleFinishDrawing}
                className="h-7 text-xs bg-emerald-600 hover:bg-emerald-700 text-white gap-1"
              >
                <Check className="h-3 w-3" />
                <span>
                  {lang === 'uz' ? 'Yakunlash' : lang === 'ru' ? 'Замкнуть' : 'Finish'}
                </span>
              </Button>
            )}

            <Button
              size="sm"
              variant="outline"
              onClick={handleRedraw}
              className="h-7 text-xs gap-1"
              title="Qayta chizish"
            >
              <RotateCcw className="h-3 w-3" />
              <span>{lang === 'uz' ? 'Qayta' : lang === 'ru' ? 'Заново' : 'Redraw'}</span>
            </Button>

            {points.length > 0 && (
              <Button
                size="sm"
                variant="ghost"
                onClick={handleClear}
                className="h-7 text-xs text-destructive hover:bg-destructive/10"
                title="Tozalash"
              >
                <Trash2 className="h-3 w-3" />
              </Button>
            )}
          </div>
        </div>

        {/* Footer Area: Warnings, Estimated Area, Actions */}
        <div className="space-y-2 py-1">
          {showAreaMismatchWarning && (
            <div className="flex items-center gap-2 p-2.5 rounded-md bg-amber-50 dark:bg-amber-950/40 border border-amber-200 dark:border-amber-800 text-amber-800 dark:text-amber-200 text-xs">
              <Info className="h-4 w-4 shrink-0 text-amber-600 dark:text-amber-400" />
              <span>
                {lang === 'uz'
                  ? `Chizilgan poligon maydoni (${estimatedArea.toFixed(2)} ga) roʻyxatdan oʻtgan maydondan (${Number(registeredAreaHectares).toFixed(2)} ga) farq qiladi. Rasmiy maydon oʻzgarishsiz qoladi.`
                  : lang === 'ru'
                  ? `Площадь контура (${estimatedArea.toFixed(2)} га) отличается от заявленной (${Number(registeredAreaHectares).toFixed(2)} га). Зарегистрированная площадь останется неизменной.`
                  : `Polygon area (${estimatedArea.toFixed(2)} ha) differs from registered area (${Number(registeredAreaHectares).toFixed(2)} ha). The registered area remains authoritative.`}
              </span>
            </div>
          )}

          {errorMessage && (
            <div className="flex items-center gap-1.5 text-destructive font-medium text-xs">
              <AlertCircle className="h-3.5 w-3.5 shrink-0" />
              <span>{errorMessage}</span>
            </div>
          )}
        </div>

        <DialogFooter className="pt-3 border-t border-border/60 gap-2">
          <Button variant="outline" size="sm" onClick={onClose} className="text-xs">
            {translations[lang]?.common?.cancel || 'Bekor qilish'}
          </Button>
          <Button
            size="sm"
            onClick={handleSave}
            disabled={Boolean(errorMessage) || (!isClosed && points.length > 0)}
            className="text-xs gap-1.5 bg-emerald-600 hover:bg-emerald-700 text-white"
          >
            <Check className="h-3.5 w-3.5" />
            <span>
              {lang === 'uz'
                ? 'Chegarani saqlash'
                : lang === 'ru'
                ? 'Сохранить границы'
                : 'Save Boundary'}
            </span>
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
