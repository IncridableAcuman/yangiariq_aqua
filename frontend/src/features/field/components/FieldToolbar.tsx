import React from 'react'
import { Plus, Search, Layers, CheckCircle2, AlertCircle, MapPin } from 'lucide-react'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { Card, CardContent } from '@/shared/ui/card'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { UserRole } from '@/entities/user/types'

interface FieldToolbarProps {
  lang: SupportedLanguage
  userRole?: UserRole
  searchQuery: string
  onSearchChange: (query: string) => void
  onOpenCreateDialog: () => void
  stats: {
    total: number
    active: number
    inactive: number
    totalArea: number
  }
}

export const FieldToolbar: React.FC<FieldToolbarProps> = ({
  lang,
  userRole,
  searchQuery,
  onSearchChange,
  onOpenCreateDialog,
  stats,
}) => {
  const t = translations[lang]?.fields || translations.uz.fields
  const canCreate = userRole === 'FARMER'

  return (
    <div className="space-y-4">
      {/* Metric Cards Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <Card className="border border-border/70 shadow-xs bg-card/60 backdrop-blur-xs">
          <CardContent className="p-3.5 flex items-center justify-between">
            <div>
              <p className="text-[11px] font-medium text-muted-foreground uppercase tracking-wider">
                {t.stats.total}
              </p>
              <p className="text-xl font-bold tracking-tight text-foreground mt-0.5">
                {stats.total}
              </p>
            </div>
            <div className="h-8 w-8 rounded-lg bg-primary/10 text-primary flex items-center justify-center shrink-0">
              <Layers className="h-4 w-4" />
            </div>
          </CardContent>
        </Card>

        <Card className="border border-border/70 shadow-xs bg-card/60 backdrop-blur-xs">
          <CardContent className="p-3.5 flex items-center justify-between">
            <div>
              <p className="text-[11px] font-medium text-emerald-600 dark:text-emerald-400 uppercase tracking-wider">
                {t.stats.active}
              </p>
              <p className="text-xl font-bold tracking-tight text-emerald-700 dark:text-emerald-300 mt-0.5">
                {stats.active}
              </p>
            </div>
            <div className="h-8 w-8 rounded-lg bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 flex items-center justify-center shrink-0">
              <CheckCircle2 className="h-4 w-4" />
            </div>
          </CardContent>
        </Card>

        <Card className="border border-border/70 shadow-xs bg-card/60 backdrop-blur-xs">
          <CardContent className="p-3.5 flex items-center justify-between">
            <div>
              <p className="text-[11px] font-medium text-muted-foreground uppercase tracking-wider">
                {t.stats.inactive}
              </p>
              <p className="text-xl font-bold tracking-tight text-muted-foreground mt-0.5">
                {stats.inactive}
              </p>
            </div>
            <div className="h-8 w-8 rounded-lg bg-muted text-muted-foreground flex items-center justify-center shrink-0">
              <AlertCircle className="h-4 w-4" />
            </div>
          </CardContent>
        </Card>

        <Card className="border border-border/70 shadow-xs bg-card/60 backdrop-blur-xs">
          <CardContent className="p-3.5 flex items-center justify-between">
            <div>
              <p className="text-[11px] font-medium text-primary uppercase tracking-wider">
                {t.stats.totalArea}
              </p>
              <p className="text-xl font-bold tracking-tight text-foreground mt-0.5">
                {stats.totalArea.toFixed(1)}{' '}
                <span className="text-xs font-normal text-muted-foreground">
                  {t.stats.ha}
                </span>
              </p>
            </div>
            <div className="h-8 w-8 rounded-lg bg-blue-500/10 text-blue-600 dark:text-blue-400 flex items-center justify-center shrink-0">
              <MapPin className="h-4 w-4" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Action Bar (Search + Create Field Button) */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 pt-1">
        <div className="relative flex-1 max-w-md">
          <Input
            placeholder={t.filter.searchPlaceholder}
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            leftIcon={<Search className="h-4 w-4" />}
            className="h-9 text-xs sm:text-sm bg-background"
          />
        </div>

        {canCreate && (
          <Button
            size="sm"
            onClick={onOpenCreateDialog}
            className="gap-1.5 shadow-sm font-medium shrink-0"
          >
            <Plus className="h-4 w-4" />
            <span>{t.create}</span>
          </Button>
        )}
      </div>
    </div>
  )
}
