import React from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { Separator } from '@/shared/ui/separator'
import { Field } from '@/entities/field'
import { FieldStatusBadge } from '@/entities/field/ui/FieldStatusBadge'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { UserRole } from '@/entities/user/types'
import { FieldMap } from '../gis/FieldMap'
import {
  Sprout,
  MapPin,
  Mountain,
  Droplets,
  Calendar,
  Clock,
  Key,
  Edit3,
  PowerOff,
  RotateCcw,
  FileText,
} from 'lucide-react'

interface FieldDetailsModalProps {
  field: Field | null
  isOpen: boolean
  onClose: () => void
  lang: SupportedLanguage
  userRole?: UserRole
  onEdit: (field: Field) => void
  onDeactivate: (field: Field) => void
  onReactivate: (field: Field) => void
}

export const FieldDetailsModal: React.FC<FieldDetailsModalProps> = ({
  field,
  isOpen,
  onClose,
  lang,
  userRole,
  onEdit,
  onDeactivate,
  onReactivate,
}) => {
  const t = translations[lang]?.fields || translations.uz.fields

  if (!field) return null

  const isActive = field.status === 'ACTIVE'
  const canMutate = userRole === 'FARMER' || userRole === 'ADMIN'

  const cropLabel = t.crops[field.cropType] || field.cropType
  const soilLabel = t.soils[field.soilType] || field.soilType
  const irrigationLabel = t.irrigations[field.irrigationMethod] || field.irrigationMethod

  const formatDateTime = (isoString?: string) => {
    if (!isoString) return '—'
    try {
      const date = new Date(isoString)
      return new Intl.DateTimeFormat(lang === 'uz' ? 'uz-UZ' : lang === 'ru' ? 'ru-RU' : 'en-US', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      }).format(date)
    } catch {
      return isoString
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-lg max-h-[90vh] overflow-y-auto">
        <DialogHeader className="space-y-1">
          <div className="flex items-center justify-between gap-2">
            <DialogTitle className="text-lg font-bold text-foreground">
              {field.name}
            </DialogTitle>
            <FieldStatusBadge status={field.status} lang={lang} />
          </div>
          <DialogDescription className="text-xs text-muted-foreground">
            {t.details.title}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-2 text-xs">
          {/* General Information */}
          <div className="space-y-2">
            <h4 className="font-semibold text-foreground text-xs uppercase tracking-wider text-muted-foreground">
              {t.details.general}
            </h4>
            <div className="bg-muted/40 rounded-lg p-3 space-y-2 border border-border/50">
              {field.description ? (
                <div className="flex items-start gap-2">
                  <FileText className="h-4 w-4 text-muted-foreground shrink-0 mt-0.5" />
                  <p className="text-foreground leading-relaxed">
                    {field.description}
                  </p>
                </div>
              ) : (
                <p className="text-muted-foreground italic">
                  Tavsif kiritilmagan
                </p>
              )}
            </div>
          </div>

          <Separator />

          {/* Agricultural Metrics */}
          <div className="space-y-2">
            <h4 className="font-semibold text-foreground text-xs uppercase tracking-wider text-muted-foreground">
              {t.details.agricultural}
            </h4>
            <div className="grid grid-cols-2 gap-2 bg-muted/40 rounded-lg p-3 border border-border/50">
              <div className="space-y-1">
                <span className="text-[11px] text-muted-foreground flex items-center gap-1">
                  <Sprout className="h-3.5 w-3.5 text-emerald-600 dark:text-emerald-400" />
                  {t.table.crop}
                </span>
                <p className="font-semibold text-foreground">{cropLabel}</p>
              </div>

              <div className="space-y-1">
                <span className="text-[11px] text-muted-foreground flex items-center gap-1">
                  <MapPin className="h-3.5 w-3.5 text-blue-600 dark:text-blue-400" />
                  {t.table.area}
                </span>
                <p className="font-semibold text-foreground tabular-nums">
                  {Number(field.areaHectares).toFixed(4)} ga
                </p>
              </div>

              <div className="space-y-1 pt-2 border-t border-border/40">
                <span className="text-[11px] text-muted-foreground flex items-center gap-1">
                  <Mountain className="h-3.5 w-3.5 text-amber-600 dark:text-amber-400" />
                  {t.table.soil}
                </span>
                <p className="font-medium text-foreground">{soilLabel}</p>
              </div>

              <div className="space-y-1 pt-2 border-t border-border/40">
                <span className="text-[11px] text-muted-foreground flex items-center gap-1">
                  <Droplets className="h-3.5 w-3.5 text-cyan-600 dark:text-cyan-400" />
                  {t.table.irrigation}
                </span>
                <p className="font-medium text-foreground">{irrigationLabel}</p>
              </div>
            </div>
          </div>

          <Separator />

          {/* Location / Coordinates & Map */}
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <h4 className="font-semibold text-foreground text-xs uppercase tracking-wider text-muted-foreground">
                {t.details.location}
              </h4>
              {field.boundary?.coordinates?.[0]?.length ? (
                <span className="text-[11px] font-semibold text-emerald-600 dark:text-emerald-400">
                  {field.boundary.coordinates[0].length} nuqta (Poligon)
                </span>
              ) : null}
            </div>

            {/* Interactive Leaflet Map */}
            <FieldMap
              latitude={field.latitude ? Number(field.latitude) : null}
              longitude={field.longitude ? Number(field.longitude) : null}
              boundary={field.boundary}
              fieldName={field.name}
              cropType={cropLabel}
              areaHectares={Number(field.areaHectares)}
              status={field.status}
              height="240px"
              className="mt-1"
            />

            <div className="bg-muted/40 rounded-lg p-2.5 border border-border/50">
              {field.latitude && field.longitude ? (
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <span className="text-[11px] text-muted-foreground">
                      {t.details.latitude}
                    </span>
                    <p className="font-mono text-xs font-semibold text-foreground mt-0.5">
                      {Number(field.latitude).toFixed(6)}° N
                    </p>
                  </div>
                  <div>
                    <span className="text-[11px] text-muted-foreground">
                      {t.details.longitude}
                    </span>
                    <p className="font-mono text-xs font-semibold text-foreground mt-0.5">
                      {Number(field.longitude).toFixed(6)}° E
                    </p>
                  </div>
                </div>
              ) : (
                <p className="text-muted-foreground italic text-xs">
                  {t.details.noCoordinates}
                </p>
              )}

              {/* Boundary indicator */}
              <div className="mt-2 pt-2 border-t border-border/40 flex items-center justify-between text-[11px] text-muted-foreground">
                <span>{t.details.boundary}:</span>
                <span className="font-medium text-foreground">
                  {field.boundary?.coordinates?.[0]?.length
                    ? `${field.boundary.coordinates[0].length} nuqta (Poligon)`
                    : t.details.noBoundary}
                </span>
              </div>
            </div>
          </div>

          <Separator />

          {/* System Metadata */}
          <div className="space-y-2">
            <h4 className="font-semibold text-foreground text-xs uppercase tracking-wider text-muted-foreground">
              {t.details.metadata}
            </h4>
            <div className="bg-muted/40 rounded-lg p-3 space-y-1.5 border border-border/50 font-mono text-[11px] text-muted-foreground">
              <div className="flex items-center justify-between">
                <span className="flex items-center gap-1 font-sans">
                  <Key className="h-3 w-3" /> {t.details.id}:
                </span>
                <span className="text-foreground">{field.id}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="flex items-center gap-1 font-sans">
                  <Calendar className="h-3 w-3" /> {t.details.createdAt}:
                </span>
                <span className="text-foreground">{formatDateTime(field.createdAt)}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="flex items-center gap-1 font-sans">
                  <Clock className="h-3 w-3" /> {t.details.updatedAt}:
                </span>
                <span className="text-foreground">{formatDateTime(field.updatedAt)}</span>
              </div>
            </div>
          </div>
        </div>

        <DialogFooter className="pt-3 border-t border-border/60 flex-col sm:flex-row gap-2">
          {canMutate && (
            <div className="flex items-center gap-2 mr-auto">
              <Button
                variant="outline"
                size="sm"
                onClick={() => {
                  onClose()
                  onEdit(field)
                }}
                className="gap-1.5 text-xs"
              >
                <Edit3 className="h-3.5 w-3.5" />
                <span>{t.edit}</span>
              </Button>

              {isActive ? (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    onClose()
                    onDeactivate(field)
                  }}
                  className="gap-1.5 text-xs text-amber-600 hover:text-amber-700 dark:text-amber-400"
                >
                  <PowerOff className="h-3.5 w-3.5" />
                  <span>{t.deactivate}</span>
                </Button>
              ) : (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    onClose()
                    onReactivate(field)
                  }}
                  className="gap-1.5 text-xs text-emerald-600 hover:text-emerald-700 dark:text-emerald-400"
                >
                  <RotateCcw className="h-3.5 w-3.5" />
                  <span>{t.reactivate}</span>
                </Button>
              )}
            </div>
          )}

          <Button variant="secondary" size="sm" onClick={onClose} className="text-xs">
            {translations[lang]?.common?.close || 'Yopish'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
