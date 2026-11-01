import React, { useState, useMemo } from 'react'
import { Recommendation } from '@/entities/recommendation'
import { RecommendationCard } from './RecommendationCard'
import { RecommendationDetailsModal } from './RecommendationDetailsModal'
import { Input } from '@/shared/ui/input'
import { Button } from '@/shared/ui/button'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { RotateCcw, Search, Sparkles } from 'lucide-react'

interface RecommendationFeedProps {
  recommendations: Recommendation[]
  isLoading?: boolean
  lang: SupportedLanguage
  onAccept?: (id: string) => Promise<void>
  onDismiss?: (id: string, reason: string) => Promise<void>
  onComplete?: (id: string) => Promise<void>
  onMarkViewed?: (id: string) => Promise<void>
}

export const RecommendationFeed: React.FC<RecommendationFeedProps> = ({
  recommendations,
  isLoading = false,
  lang,
  onAccept,
  onDismiss,
  onComplete,
  onMarkViewed,
}) => {
  const [search, setSearch] = useState('')
  const [priorityFilter, setPriorityFilter] = useState<string>('ALL')
  const [statusFilter, setStatusFilter] = useState<string>('ALL')
  const [typeFilter, setTypeFilter] = useState<string>('ALL')
  const [selectedRec, setSelectedRec] = useState<Recommendation | null>(null)
  const [isModalOpen, setIsModalOpen] = useState(false)

  const t = translations[lang].recommendations || {}
  const filtersT = t.filters || {
    searchPlaceholder: 'Search recommendations...',
    allPriorities: 'All Priorities',
    allStatuses: 'All Statuses',
    allTypes: 'All Types',
    reset: 'Reset Filters',
  }

  const filtered = useMemo(() => {
    return recommendations.filter((r) => {
      if (priorityFilter !== 'ALL' && r.priority !== priorityFilter) return false
      if (statusFilter !== 'ALL' && r.status !== statusFilter) return false
      if (typeFilter !== 'ALL' && r.recommendationType !== typeFilter) return false
      if (search.trim()) {
        const query = search.toLowerCase()
        const matchesTitle = r.title.toLowerCase().includes(query)
        const matchesSummary = r.summary.toLowerCase().includes(query)
        const matchesType = r.recommendationType.toLowerCase().includes(query)
        if (!matchesTitle && !matchesSummary && !matchesType) return false
      }
      return true
    })
  }, [recommendations, priorityFilter, statusFilter, typeFilter, search])

  const handleOpenDetails = (rec: Recommendation) => {
    setSelectedRec(rec)
    setIsModalOpen(true)
    if (rec.status === 'GENERATED' && onMarkViewed) {
      onMarkViewed(rec.id).catch(() => {})
    }
  }

  const handleResetFilters = () => {
    setSearch('')
    setPriorityFilter('ALL')
    setStatusFilter('ALL')
    setTypeFilter('ALL')
  }

  return (
    <div className="space-y-4">
      {/* Search and Filters Bar */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-2.5 bg-card p-3 rounded-lg border shadow-sm">
        <div className="relative lg:col-span-2">
          <Search className="w-4 h-4 absolute left-3 top-3 text-muted-foreground" />
          <Input
            placeholder={filtersT.searchPlaceholder}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-9 h-9 text-sm"
          />
        </div>

        <select
          value={priorityFilter}
          onChange={(e) => setPriorityFilter(e.target.value)}
          aria-label={filtersT.allPriorities}
          className="h-9 px-3 rounded-md border border-input bg-background text-sm text-foreground focus:outline-none focus:ring-1 focus:ring-primary"
        >
          <option value="ALL">{filtersT.allPriorities}</option>
          <option value="CRITICAL">Critical</option>
          <option value="HIGH">High</option>
          <option value="MEDIUM">Medium</option>
          <option value="LOW">Low</option>
          <option value="INFO">Info</option>
        </select>

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          aria-label={filtersT.allStatuses}
          className="h-9 px-3 rounded-md border border-input bg-background text-sm text-foreground focus:outline-none focus:ring-1 focus:ring-primary"
        >
          <option value="ALL">{filtersT.allStatuses}</option>
          <option value="GENERATED">Generated</option>
          <option value="VIEWED">Viewed</option>
          <option value="ACCEPTED">Accepted</option>
          <option value="COMPLETED">Completed</option>
          <option value="DISMISSED">Dismissed</option>
          <option value="EXPIRED">Expired</option>
        </select>

        <div className="flex gap-1.5">
          <select
            value={typeFilter}
            onChange={(e) => setTypeFilter(e.target.value)}
            aria-label={filtersT.allTypes}
            className="h-9 px-3 rounded-md border border-input bg-background text-sm text-foreground focus:outline-none focus:ring-1 focus:ring-primary flex-1 min-w-0"
          >
            <option value="ALL">{filtersT.allTypes}</option>
            <option value="IRRIGATE_NOW">IRRIGATE_NOW</option>
            <option value="DELAY_IRRIGATION">DELAY_IRRIGATION</option>
            <option value="REDUCE_WATER_VOLUME">REDUCE_WATER_VOLUME</option>
            <option value="AVOID_IRRIGATION">AVOID_IRRIGATION</option>
            <option value="INSPECT_PIPE_BURST">INSPECT_PIPE_BURST</option>
            <option value="INSPECT_SENSOR">INSPECT_SENSOR</option>
            <option value="MONITOR_SOIL_MOISTURE">MONITOR_SOIL_MOISTURE</option>
            <option value="MANAGE_SALINITY">MANAGE_SALINITY</option>
            <option value="PREPARE_HEAT_STRESS">PREPARE_HEAT_STRESS</option>
          </select>

          {(search || priorityFilter !== 'ALL' || statusFilter !== 'ALL' || typeFilter !== 'ALL') && (
            <Button
              size="icon"
              variant="outline"
              className="h-9 w-9 shrink-0"
              onClick={handleResetFilters}
              title={filtersT.reset}
            >
              <RotateCcw className="w-4 h-4 text-muted-foreground" />
            </Button>
          )}
        </div>
      </div>

      {/* Recommendations Cards Feed */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {[1, 2, 3, 4].map((n) => (
            <div key={n} className="h-44 rounded-lg bg-muted/40 animate-pulse border" />
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-12 rounded-lg border border-dashed bg-card/50 space-y-3">
          <div className="w-12 h-12 rounded-full bg-primary/10 text-primary mx-auto flex items-center justify-center">
            <Sparkles className="w-6 h-6" />
          </div>
          <h4 className="font-semibold text-foreground">
            {lang === 'uz'
              ? 'Tavsiyalar topilmadi'
              : lang === 'ru'
              ? 'Рекомендации не найдены'
              : 'No Recommendations Found'}
          </h4>
          <p className="text-xs text-muted-foreground max-w-sm mx-auto">
            {lang === 'uz'
              ? 'Joriy filtr mezonlariga mos keluvchi tavsiyalar mavjud emas yoki datchiklar koʻrsatkichlari optimal holatda.'
              : lang === 'ru'
              ? 'По текущим фильтрам рекомендаций нет или показатели датчиков в пределах нормы.'
              : 'No recommendations match the filter criteria, or all field sensors indicate optimal agronomic bounds.'}
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filtered.map((rec) => (
            <RecommendationCard
              key={rec.id}
              recommendation={rec}
              lang={lang}
              onViewDetails={handleOpenDetails}
              onAccept={onAccept}
              onDismiss={onDismiss}
              onComplete={onComplete}
            />
          ))}
        </div>
      )}

      {/* Details Modal */}
      <RecommendationDetailsModal
        recommendation={selectedRec}
        open={isModalOpen}
        onOpenChange={setIsModalOpen}
        lang={lang}
        onAccept={onAccept}
        onDismiss={onDismiss}
        onComplete={onComplete}
      />
    </div>
  )
}
