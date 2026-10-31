import React from 'react'
import { Eye, Edit3, PowerOff, RotateCcw, Sprout, MapPin, Droplets, Mountain } from 'lucide-react'
import { Card, CardContent, CardFooter, CardHeader } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Field } from '@/entities/field'
import { FieldStatusBadge } from '@/entities/field/ui/FieldStatusBadge'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { UserRole } from '@/entities/user/types'

interface FieldCardProps {
  field: Field
  lang: SupportedLanguage
  userRole?: UserRole
  onView: (field: Field) => void
  onEdit: (field: Field) => void
  onDeactivate: (field: Field) => void
  onReactivate: (field: Field) => void
}

export const FieldCard: React.FC<FieldCardProps> = ({
  field,
  lang,
  userRole,
  onView,
  onEdit,
  onDeactivate,
  onReactivate,
}) => {
  const t = translations[lang]?.fields || translations.uz.fields
  const isActive = field.status === 'ACTIVE'
  const canMutate = userRole === 'FARMER' || userRole === 'ADMIN'

  const cropLabel = t.crops[field.cropType] || field.cropType
  const soilLabel = t.soils[field.soilType] || field.soilType
  const irrigationLabel = t.irrigations[field.irrigationMethod] || field.irrigationMethod

  const formatDate = (isoString: string) => {
    try {
      const date = new Date(isoString)
      return new Intl.DateTimeFormat(lang === 'uz' ? 'uz-UZ' : lang === 'ru' ? 'ru-RU' : 'en-US', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
      }).format(date)
    } catch {
      return isoString
    }
  }

  return (
    <Card className="border border-border/80 shadow-2xs hover:shadow-xs transition-shadow flex flex-col justify-between">
      <CardHeader className="p-4 pb-2.5 space-y-1">
        <div className="flex items-start justify-between gap-2">
          <h3 className="font-semibold text-sm text-foreground line-clamp-1">
            {field.name}
          </h3>
          <FieldStatusBadge status={field.status} lang={lang} />
        </div>
        {field.description && (
          <p className="text-xs text-muted-foreground line-clamp-2 leading-relaxed">
            {field.description}
          </p>
        )}
      </CardHeader>

      <CardContent className="p-4 pt-1 pb-3">
        <div className="grid grid-cols-2 gap-2.5 pt-2 border-t border-border/50 text-xs">
          {/* Crop */}
          <div className="flex items-center gap-1.5 text-foreground">
            <Sprout className="h-3.5 w-3.5 text-emerald-600 dark:text-emerald-400 shrink-0" />
            <span className="truncate">{cropLabel}</span>
          </div>

          {/* Area */}
          <div className="flex items-center gap-1.5 text-foreground tabular-nums font-medium">
            <MapPin className="h-3.5 w-3.5 text-blue-600 dark:text-blue-400 shrink-0" />
            <span>
              {Number(field.areaHectares).toFixed(2)}{' '}
              <span className="text-muted-foreground font-normal">ga</span>
            </span>
          </div>

          {/* Soil */}
          <div className="flex items-center gap-1.5 text-muted-foreground">
            <Mountain className="h-3.5 w-3.5 text-amber-600 dark:text-amber-400 shrink-0" />
            <span className="truncate">{soilLabel}</span>
          </div>

          {/* Irrigation */}
          <div className="flex items-center gap-1.5 text-muted-foreground">
            <Droplets className="h-3.5 w-3.5 text-cyan-600 dark:text-cyan-400 shrink-0" />
            <span className="truncate">{irrigationLabel}</span>
          </div>
        </div>
      </CardContent>

      <CardFooter className="p-4 pt-2.5 border-t border-border/60 flex items-center justify-between text-xs text-muted-foreground bg-muted/20">
        <span className="text-[11px] tabular-nums">
          {formatDate(field.updatedAt)}
        </span>

        <div className="flex items-center gap-1">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => onView(field)}
            className="h-7 w-7 text-muted-foreground hover:text-foreground"
            title={t.view}
            aria-label={`${t.view}: ${field.name}`}
          >
            <Eye className="h-3.5 w-3.5" />
          </Button>

          {canMutate && (
            <Button
              variant="ghost"
              size="icon"
              onClick={() => onEdit(field)}
              className="h-7 w-7 text-muted-foreground hover:text-primary"
              title={t.edit}
              aria-label={`${t.edit}: ${field.name}`}
            >
              <Edit3 className="h-3.5 w-3.5" />
            </Button>
          )}

          {canMutate && isActive && (
            <Button
              variant="ghost"
              size="icon"
              onClick={() => onDeactivate(field)}
              className="h-7 w-7 text-muted-foreground hover:text-amber-600 dark:hover:text-amber-400"
              title={t.deactivate}
              aria-label={`${t.deactivate}: ${field.name}`}
            >
              <PowerOff className="h-3.5 w-3.5" />
            </Button>
          )}

          {canMutate && !isActive && (
            <Button
              variant="ghost"
              size="icon"
              onClick={() => onReactivate(field)}
              className="h-7 w-7 text-muted-foreground hover:text-emerald-600 dark:hover:text-emerald-400"
              title={t.reactivate}
              aria-label={`${t.reactivate}: ${field.name}`}
            >
              <RotateCcw className="h-3.5 w-3.5" />
            </Button>
          )}
        </div>
      </CardFooter>
    </Card>
  )
}
