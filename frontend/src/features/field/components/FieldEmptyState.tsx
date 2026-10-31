import React from 'react'
import { Sprout, Plus, SearchX } from 'lucide-react'
import { Button } from '@/shared/ui/button'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { UserRole } from '@/entities/user/types'

interface FieldEmptyStateProps {
  isFiltered: boolean
  lang: SupportedLanguage
  userRole?: UserRole
  onOpenCreateDialog: () => void
  onClearFilters: () => void
}

export const FieldEmptyState: React.FC<FieldEmptyStateProps> = ({
  isFiltered,
  lang,
  userRole,
  onOpenCreateDialog,
  onClearFilters,
}) => {
  const t = translations[lang]?.fields || translations.uz.fields
  const canCreate = userRole === 'FARMER'

  if (isFiltered) {
    return (
      <div className="flex flex-col items-center justify-center p-8 sm:p-12 text-center rounded-xl border border-dashed border-border bg-card/40 my-4 space-y-3">
        <div className="h-12 w-12 rounded-full bg-muted/80 text-muted-foreground flex items-center justify-center">
          <SearchX className="h-6 w-6" />
        </div>
        <div className="max-w-md space-y-1">
          <h3 className="font-semibold text-sm text-foreground">
            {t.empty.filteredTitle}
          </h3>
          <p className="text-xs text-muted-foreground leading-relaxed">
            {t.empty.filteredDescription}
          </p>
        </div>
        <Button
          variant="outline"
          size="sm"
          onClick={onClearFilters}
          className="text-xs"
        >
          {t.filter.clearFilters}
        </Button>
      </div>
    )
  }

  return (
    <div className="flex flex-col items-center justify-center p-10 sm:p-16 text-center rounded-xl border border-dashed border-border bg-card/40 my-4 space-y-4">
      <div className="h-14 w-14 rounded-2xl bg-primary/10 text-primary flex items-center justify-center shadow-xs">
        <Sprout className="h-7 w-7 text-primary" />
      </div>
      <div className="max-w-md space-y-1.5">
        <h3 className="font-bold text-base text-foreground">
          {t.empty.title}
        </h3>
        <p className="text-xs text-muted-foreground leading-relaxed">
          {t.empty.description}
        </p>
      </div>
      {canCreate && (
        <Button
          size="sm"
          onClick={onOpenCreateDialog}
          className="gap-1.5 shadow-sm text-xs"
        >
          <Plus className="h-4 w-4" />
          <span>{t.create}</span>
        </Button>
      )}
    </div>
  )
}
