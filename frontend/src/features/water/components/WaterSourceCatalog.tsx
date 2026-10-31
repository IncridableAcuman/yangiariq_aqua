import React, { useState } from 'react'
import { Plus, Search, Waves, CheckCircle2, Power, Edit } from 'lucide-react'
import { Card } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { Badge } from '@/shared/ui/badge'
import { WaterSource } from '@/entities/water'
import { WaterStatusBadge } from './WaterStatusBadge'
import { formatDate } from '../lib/waterFormatters'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { cn } from '@/shared/lib/utils'

interface WaterSourceCatalogProps {
  sources: WaterSource[]
  canManage: boolean
  isLoading: boolean
  lang: SupportedLanguage
  onCreateClick: () => void
  onEditClick: (source: WaterSource) => void
  onActivateClick: (source: WaterSource) => void
  onDeactivateClick: (source: WaterSource) => void
}

export const WaterSourceCatalog: React.FC<WaterSourceCatalogProps> = ({
  sources,
  canManage,
  isLoading,
  lang,
  onCreateClick,
  onEditClick,
  onActivateClick,
  onDeactivateClick,
}) => {
  const t = translations[lang]

  const [search, setSearch] = useState<string>('')
  const [selectedType, setSelectedType] = useState<string>('ALL')
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL')

  const filteredSources = sources.filter((s) => {
    if (search && !s.name.toLowerCase().includes(search.toLowerCase())) return false
    if (selectedType !== 'ALL' && s.type !== selectedType) return false
    if (selectedStatus !== 'ALL' && s.status !== selectedStatus) return false
    return true
  })

  return (
    <div className="space-y-4">
      {/* Header & Controls */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
        <div>
          <h3 className="text-lg font-semibold tracking-tight text-foreground">
            {t.water.sources.title}
          </h3>
          <p className="text-xs text-muted-foreground">
            Regional hydraulic intake infrastructure in Yangiariq district ({filteredSources.length} sources)
          </p>
        </div>

        {canManage && (
          <Button size="sm" onClick={onCreateClick} className="gap-1.5 text-xs h-8">
            <Plus className="w-3.5 h-3.5" />
            <span>{t.water.sources.create}</span>
          </Button>
        )}
      </div>

      {/* Filter Toolbar */}
      <div className="flex flex-wrap items-center gap-2">
        <div className="relative flex-1 min-w-[200px] max-w-sm">
          <Search className="w-4 h-4 absolute left-2.5 top-2.5 text-muted-foreground" />
          <Input
            placeholder="Search water source..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-8 text-xs h-8"
          />
        </div>

        {/* Type Filter */}
        <select
          value={selectedType}
          onChange={(e) => setSelectedType(e.target.value)}
          className="bg-background border border-input rounded-md px-2.5 py-1 text-xs h-8 focus:outline-none focus:ring-1 focus:ring-ring"
        >
          <option value="ALL">All Types</option>
          <option value="CANAL">{t.water.sourceTypes.CANAL}</option>
          <option value="DITCH">{t.water.sourceTypes.DITCH}</option>
          <option value="RESERVOIR">{t.water.sourceTypes.RESERVOIR}</option>
          <option value="WELL">{t.water.sourceTypes.WELL}</option>
          <option value="RIVER">{t.water.sourceTypes.RIVER}</option>
          <option value="OTHER">{t.water.sourceTypes.OTHER}</option>
        </select>

        {/* Status Filter */}
        <select
          value={selectedStatus}
          onChange={(e) => setSelectedStatus(e.target.value)}
          className="bg-background border border-input rounded-md px-2.5 py-1 text-xs h-8 focus:outline-none focus:ring-1 focus:ring-ring"
        >
          <option value="ALL">All Statuses</option>
          <option value="ACTIVE">{t.water.statuses.ACTIVE}</option>
          <option value="INACTIVE">{t.water.statuses.INACTIVE}</option>
        </select>
      </div>

      {/* Empty State */}
      {filteredSources.length === 0 && !isLoading && (
        <div className="text-center py-12 px-4 rounded-xl border border-dashed border-border bg-card">
          <p className="text-sm font-medium text-muted-foreground">
            {t.water.sources.noSources}
          </p>
          {canManage && (
            <Button
              variant="outline"
              size="sm"
              onClick={onCreateClick}
              className="mt-3 gap-1.5"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>{t.water.sources.create}</span>
            </Button>
          )}
        </div>
      )}

      {/* Source Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {filteredSources.map((source) => (
          <Card
            key={source.id}
            className={cn(
              'border-border/60 shadow-sm transition-all hover:border-primary/40 bg-card p-4 space-y-3',
              source.status === 'INACTIVE' && 'opacity-70'
            )}
          >
            <div className="flex items-start justify-between gap-2">
              <div className="flex items-center gap-2">
                <div className="p-2 rounded-lg bg-sky-100 dark:bg-sky-950/60 text-sky-600 dark:text-sky-400">
                  <Waves className="w-4 h-4" />
                </div>
                <div>
                  <h4 className="font-semibold text-sm text-foreground">
                    {source.name}
                  </h4>
                  <Badge variant="outline" className="text-[10px] px-1.5 py-0 font-medium">
                    {t.water.sourceTypes[source.type] || source.type}
                  </Badge>
                </div>
              </div>
              <WaterStatusBadge status={source.status} lang={lang} />
            </div>

            <p className="text-xs text-muted-foreground min-h-[36px] line-clamp-2">
              {source.description || 'No hydraulic description provided.'}
            </p>

            <div className="pt-2 border-t border-border/50 flex items-center justify-between text-[11px] text-muted-foreground">
              <span>Registered {formatDate(source.createdAt)}</span>

              {canManage && (
                <div className="flex items-center gap-1">
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-7 px-2 text-xs"
                    onClick={() => onEditClick(source)}
                  >
                    <Edit className="w-3 h-3 mr-1" />
                    {t.water.sources.edit}
                  </Button>

                  {source.status === 'ACTIVE' ? (
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-7 px-2 text-xs text-rose-600 hover:text-rose-700 hover:bg-rose-50 dark:hover:bg-rose-950/40"
                      onClick={() => onDeactivateClick(source)}
                    >
                      <Power className="w-3 h-3 mr-1" />
                      {t.water.sources.deactivate}
                    </Button>
                  ) : (
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-7 px-2 text-xs text-emerald-600 hover:text-emerald-700 hover:bg-emerald-50 dark:hover:bg-emerald-950/40"
                      onClick={() => onActivateClick(source)}
                    >
                      <CheckCircle2 className="w-3 h-3 mr-1" />
                      {t.water.sources.activate}
                    </Button>
                  )}
                </div>
              )}
            </div>
          </Card>
        ))}
      </div>
    </div>
  )
}
