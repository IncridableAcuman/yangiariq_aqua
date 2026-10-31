import React from 'react'
import { Eye, Edit3, PowerOff, RotateCcw, Sprout } from 'lucide-react'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/ui/table'
import { Button } from '@/shared/ui/button'
import { Skeleton } from '@/shared/ui/skeleton'
import { Field } from '@/entities/field'
import { FieldStatusBadge } from '@/entities/field/ui/FieldStatusBadge'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { UserRole } from '@/entities/user/types'

interface FieldTableProps {
  fields: Field[]
  isLoading: boolean
  lang: SupportedLanguage
  userRole?: UserRole
  onView: (field: Field) => void
  onEdit: (field: Field) => void
  onDeactivate: (field: Field) => void
  onReactivate: (field: Field) => void
}

export const FieldTable: React.FC<FieldTableProps> = ({
  fields,
  isLoading,
  lang,
  userRole,
  onView,
  onEdit,
  onDeactivate,
  onReactivate,
}) => {
  const t = translations[lang]?.fields || translations.uz.fields

  // Role permissions
  // Farmer: can edit, deactivate, reactivate own fields
  // Admin: can edit, deactivate, reactivate any field
  // Operator: read-only district monitoring
  const canMutate = userRole === 'FARMER' || userRole === 'ADMIN'

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

  if (isLoading) {
    return (
      <div className="rounded-lg border border-border bg-card overflow-hidden">
        <Table>
          <TableHeader>
            <TableRow className="bg-muted/30">
              <TableHead className="w-[200px]">{t.table.name}</TableHead>
              <TableHead>{t.table.crop}</TableHead>
              <TableHead>{t.table.area}</TableHead>
              <TableHead>{t.table.soil}</TableHead>
              <TableHead>{t.table.irrigation}</TableHead>
              <TableHead>{t.table.status}</TableHead>
              <TableHead>{t.table.updated}</TableHead>
              <TableHead className="text-right">{t.table.actions}</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {Array.from({ length: 5 }).map((_, idx) => (
              <TableRow key={idx}>
                <TableCell><Skeleton className="h-4 w-32" /></TableCell>
                <TableCell><Skeleton className="h-4 w-20" /></TableCell>
                <TableCell><Skeleton className="h-4 w-14" /></TableCell>
                <TableCell><Skeleton className="h-4 w-24" /></TableCell>
                <TableCell><Skeleton className="h-4 w-28" /></TableCell>
                <TableCell><Skeleton className="h-5 w-16" /></TableCell>
                <TableCell><Skeleton className="h-4 w-20" /></TableCell>
                <TableCell className="text-right"><Skeleton className="h-8 w-20 ml-auto" /></TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    )
  }

  if (fields.length === 0) {
    return null
  }

  return (
    <div className="rounded-lg border border-border bg-card overflow-hidden shadow-2xs">
      <Table>
        <TableHeader>
          <TableRow className="bg-muted/40 text-xs">
            <TableHead className="font-semibold">{t.table.name}</TableHead>
            <TableHead className="font-semibold">{t.table.crop}</TableHead>
            <TableHead className="font-semibold">{t.table.area}</TableHead>
            <TableHead className="font-semibold">{t.table.soil}</TableHead>
            <TableHead className="font-semibold">{t.table.irrigation}</TableHead>
            <TableHead className="font-semibold">{t.table.status}</TableHead>
            <TableHead className="font-semibold">{t.table.updated}</TableHead>
            <TableHead className="font-semibold text-right">{t.table.actions}</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {fields.map((field) => {
            const isActive = field.status === 'ACTIVE'
            const cropLabel = t.crops[field.cropType] || field.cropType
            const soilLabel = t.soils[field.soilType] || field.soilType
            const irrigationLabel = t.irrigations[field.irrigationMethod] || field.irrigationMethod

            return (
              <TableRow
                key={field.id}
                className="hover:bg-muted/30 transition-colors text-xs"
              >
                {/* Field Name & Subtext */}
                <TableCell className="font-medium">
                  <div className="flex flex-col">
                    <span className="font-semibold text-foreground">
                      {field.name}
                    </span>
                    {field.description && (
                      <span className="text-[11px] text-muted-foreground line-clamp-1">
                        {field.description}
                      </span>
                    )}
                  </div>
                </TableCell>

                {/* Crop */}
                <TableCell>
                  <span className="inline-flex items-center gap-1 text-foreground">
                    <Sprout className="h-3.5 w-3.5 text-emerald-600 dark:text-emerald-400 shrink-0" />
                    <span>{cropLabel}</span>
                  </span>
                </TableCell>

                {/* Area */}
                <TableCell className="tabular-nums font-medium text-foreground">
                  {Number(field.areaHectares).toFixed(2)}{' '}
                  <span className="text-muted-foreground text-[11px]">ga</span>
                </TableCell>

                {/* Soil */}
                <TableCell className="text-muted-foreground">
                  {soilLabel}
                </TableCell>

                {/* Irrigation Method */}
                <TableCell className="text-muted-foreground">
                  {irrigationLabel}
                </TableCell>

                {/* Status Badge */}
                <TableCell>
                  <FieldStatusBadge status={field.status} lang={lang} />
                </TableCell>

                {/* Updated Date */}
                <TableCell className="text-muted-foreground tabular-nums">
                  {formatDate(field.updatedAt)}
                </TableCell>

                {/* Actions */}
                <TableCell className="text-right">
                  <div className="flex items-center justify-end gap-1">
                    {/* View Details */}
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

                    {/* Edit (Farmer or Admin) */}
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

                    {/* Deactivate (Active field only) */}
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

                    {/* Reactivate (Inactive field only) */}
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
                </TableCell>
              </TableRow>
            )
          })}
        </TableBody>
      </Table>
    </div>
  )
}
