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
import { MapPin, AlertCircle, Check } from 'lucide-react'
import {
  isInsideUzbekistan,
  YANGIARIQ_DEFAULT_CENTER,
} from './gis.utils'
import { LatLngPoint } from './gis.types'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface FieldLocationPickerModalProps {
  isOpen: boolean
  onClose: () => void
  initialLat?: number | null
  initialLng?: number | null
  onSelectLocation: (lat: number, lng: number) => void
  lang: SupportedLanguage
}

export const FieldLocationPickerModal: React.FC<FieldLocationPickerModalProps> = ({
  isOpen,
  onClose,
  initialLat,
  initialLng,
  onSelectLocation,
  lang,
}) => {
  const mapContainerRef = useRef<HTMLDivElement | null>(null)
  const mapInstanceRef = useRef<L.Map | null>(null)
  const markerRef = useRef<L.Marker | null>(null)

  const [selectedPoint, setSelectedPoint] = useState<LatLngPoint | null>(null)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)

  useEffect(() => {
    if (!isOpen) {
      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove()
        mapInstanceRef.current = null
      }
      return
    }

    const hasInitial =
      typeof initialLat === 'number' &&
      typeof initialLng === 'number' &&
      isInsideUzbekistan(initialLat, initialLng)

    const startPos: LatLngPoint = hasInitial
      ? { lat: initialLat!, lng: initialLng! }
      : YANGIARIQ_DEFAULT_CENTER

    setSelectedPoint(hasInitial ? startPos : null)
    setErrorMessage(null)

    // Small delay to ensure DialogContent DOM is rendered
    const timer = setTimeout(() => {
      if (!mapContainerRef.current) return

      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove()
        mapInstanceRef.current = null
      }

      const map = L.map(mapContainerRef.current, {
        center: [startPos.lat, startPos.lng],
        zoom: hasInitial ? 15 : 12,
      })

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution:
          '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      }).addTo(map)

      mapInstanceRef.current = map

      const updateMarker = (lat: number, lng: number) => {
        if (!isInsideUzbekistan(lat, lng)) {
          setErrorMessage(
            lang === 'uz'
              ? 'Tanlangan joy Oʻzbekiston hududidan tashqarida'
              : lang === 'ru'
              ? 'Выбранная точка находится за пределами Узбекистана'
              : 'Selected location is outside Uzbekistan'
          )
          return
        }

        setErrorMessage(null)
        setSelectedPoint({ lat, lng })

        const pinSvg = `
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="32" height="32" fill="#059669" stroke="#ffffff" stroke-width="1.5">
            <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"/>
            <circle cx="12" cy="9" r="2.5" fill="#ffffff"/>
          </svg>
        `
        const icon = L.divIcon({
          className: 'picker-pin',
          html: pinSvg,
          iconSize: [32, 32],
          iconAnchor: [16, 32],
        })

        if (markerRef.current) {
          markerRef.current.setLatLng([lat, lng])
        } else {
          const marker = L.marker([lat, lng], {
            icon,
            draggable: true,
          }).addTo(map)

          marker.on('dragend', () => {
            const pos = marker.getLatLng()
            updateMarker(pos.lat, pos.lng)
          })

          markerRef.current = marker
        }
      }

      if (hasInitial) {
        updateMarker(startPos.lat, startPos.lng)
      }

      // Map click handler
      map.on('click', (e: L.LeafletMouseEvent) => {
        updateMarker(e.latlng.lat, e.latlng.lng)
      })

      map.invalidateSize()
    }, 150)

    return () => {
      clearTimeout(timer)
      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove()
        mapInstanceRef.current = null
      }
      markerRef.current = null
    }
  }, [isOpen, initialLat, initialLng, lang])

  const handleConfirm = () => {
    if (selectedPoint && isInsideUzbekistan(selectedPoint.lat, selectedPoint.lng)) {
      onSelectLocation(selectedPoint.lat, selectedPoint.lng)
      onClose()
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-2xl max-h-[92vh] flex flex-col p-4 sm:p-6">
        <DialogHeader className="space-y-1">
          <div className="flex items-center gap-2">
            <div className="h-7 w-7 rounded-md bg-primary/10 text-primary flex items-center justify-center shrink-0">
              <MapPin className="h-4 w-4" />
            </div>
            <DialogTitle className="text-base font-bold text-foreground">
              {lang === 'uz'
                ? 'Dala markaziy koordinatasini tanlash'
                : lang === 'ru'
                ? 'Выбор центральных координат поля'
                : 'Select Centroid Coordinates on Map'}
            </DialogTitle>
          </div>
          <DialogDescription className="text-xs text-muted-foreground">
            {lang === 'uz'
              ? 'Xaritadagi kerakli joyga bosing yoki belgini kerakli nuqtaga suring'
              : lang === 'ru'
              ? 'Кликните на карту или переместите метку в нужную точку'
              : 'Click on the map or drag the pin to set the centroid coordinates'}
          </DialogDescription>
        </DialogHeader>

        {/* Map Container */}
        <div className="relative w-full h-[360px] sm:h-[400px] rounded-lg overflow-hidden border border-border mt-2 my-2 bg-muted/20">
          <div ref={mapContainerRef} className="w-full h-full z-0" />
        </div>

        {/* Selected Coordinates & Validation Alert */}
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-2 text-xs py-1">
          {selectedPoint ? (
            <div className="font-mono bg-muted/50 px-3 py-1.5 rounded border border-border/60 text-foreground">
              Lat: <span className="font-semibold">{selectedPoint.lat.toFixed(6)}°</span>, Lon:{' '}
              <span className="font-semibold">{selectedPoint.lng.toFixed(6)}°</span>
            </div>
          ) : (
            <p className="text-muted-foreground italic">
              {lang === 'uz'
                ? 'Xaritadan nuqta tanlang'
                : lang === 'ru'
                ? 'Выберите точку на карте'
                : 'Select a point on the map'}
            </p>
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
            onClick={handleConfirm}
            disabled={!selectedPoint || Boolean(errorMessage)}
            className="text-xs gap-1.5 bg-emerald-600 hover:bg-emerald-700 text-white"
          >
            <Check className="h-3.5 w-3.5" />
            <span>
              {lang === 'uz'
                ? 'Koordinatani biriktirish'
                : lang === 'ru'
                ? 'Применить координаты'
                : 'Apply Coordinates'}
            </span>
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
