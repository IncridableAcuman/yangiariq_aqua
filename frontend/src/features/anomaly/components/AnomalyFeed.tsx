import React, { useState } from 'react'
import {
  Anomaly,
  AnomalyFilterParams,
  AnomalySeverity,
  AnomalyStatus,
  AnomalyType,
} from '@/entities/anomaly'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { Input } from '@/shared/ui/input'
import { Button } from '@/shared/ui/button'
import { AnomalyCard } from './AnomalyCard'
import { AnomalyActionDialog, AnomalyActionType } from './AnomalyActionDialog'
import { AnomalyDetailsModal } from './AnomalyDetailsModal'
import { useAnomalies } from '../queries/useAnomalyQueries'
import { AlertCircle, RefreshCw, Search } from 'lucide-react'

interface AnomalyFeedProps {
  fieldId?: string
  sensorId?: string
  lang: SupportedLanguage
}

export const AnomalyFeed: React.FC<AnomalyFeedProps> = ({
  fieldId,
  sensorId,
  lang,
}) => {
  const t = translations[lang].anomalies
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedSeverity, setSelectedSeverity] = useState<AnomalySeverity | 'ALL'>('ALL')
  const [selectedStatus, setSelectedStatus] = useState<AnomalyStatus | 'ALL'>('ALL')
  const [selectedType, setSelectedType] = useState<AnomalyType | 'ALL'>('ALL')
  const [simFilter, setSimFilter] = useState<'ALL' | 'PROD' | 'SIM'>('ALL')

  // Selected anomaly for modal or action dialog
  const [activeAnomalyForAction, setActiveAnomalyForAction] = useState<Anomaly | null>(null)
  const [actionType, setActionType] = useState<AnomalyActionType | null>(null)
  const [activeAnomalyForDetails, setActiveAnomalyForDetails] = useState<Anomaly | null>(null)

  const filterParams: AnomalyFilterParams = {
    fieldId,
    sensorId,
    severity: selectedSeverity !== 'ALL' ? selectedSeverity : undefined,
    status: selectedStatus !== 'ALL' ? selectedStatus : undefined,
    anomalyType: selectedType !== 'ALL' ? selectedType : undefined,
    size: 50,
  }

  const { data, isLoading, refetch, isRefetching } = useAnomalies(filterParams)

  const handleOpenAction = (anomaly: Anomaly, action: AnomalyActionType) => {
    setActiveAnomalyForAction(anomaly)
    setActionType(action)
  }

  const handleResetFilters = () => {
    setSearchTerm('')
    setSelectedSeverity('ALL')
    setSelectedStatus('ALL')
    setSelectedType('ALL')
    setSimFilter('ALL')
  }

  // Client-side text & simulation filtering
  const anomalies = (data?.content || []).filter((item) => {
    if (simFilter === 'PROD' && item.isSimulated) return false
    if (simFilter === 'SIM' && !item.isSimulated) return false
    if (!searchTerm.trim()) return true
    const term = searchTerm.toLowerCase()
    return (
      item.description.toLowerCase().includes(term) ||
      item.explanation.toLowerCase().includes(term) ||
      (item.resolutionNotes && item.resolutionNotes.toLowerCase().includes(term))
    )
  })

  return (
    <div className="space-y-4">
      {/* Filter and Search Bar */}
      <div className="p-3 bg-card border border-border rounded-xl space-y-3">
        <div className="flex flex-col sm:flex-row gap-2.5">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-2.5 w-4 h-4 text-muted-foreground" />
            <Input
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder={t.filters.searchPlaceholder}
              className="pl-9 text-xs"
            />
          </div>

          <div className="flex flex-wrap items-center gap-2">
            {/* Severity Filter */}
            <select
              value={selectedSeverity}
              onChange={(e) => setSelectedSeverity(e.target.value as AnomalySeverity | 'ALL')}
              className="h-9 px-2.5 rounded-md border border-input bg-background text-xs focus:outline-none focus:ring-1 focus:ring-ring"
            >
              <option value="ALL">{t.filters.allSeverities}</option>
              <option value="CRITICAL">{t.severities.CRITICAL}</option>
              <option value="HIGH">{t.severities.HIGH}</option>
              <option value="MEDIUM">{t.severities.MEDIUM}</option>
              <option value="LOW">{t.severities.LOW}</option>
              <option value="INFO">{t.severities.INFO}</option>
            </select>

            {/* Status Filter */}
            <select
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value as AnomalyStatus | 'ALL')}
              className="h-9 px-2.5 rounded-md border border-input bg-background text-xs focus:outline-none focus:ring-1 focus:ring-ring"
            >
              <option value="ALL">{t.filters.allStatuses}</option>
              <option value="DETECTED">{t.statuses.DETECTED}</option>
              <option value="ACKNOWLEDGED">{t.statuses.ACKNOWLEDGED}</option>
              <option value="INVESTIGATING">{t.statuses.INVESTIGATING}</option>
              <option value="RESOLVED">{t.statuses.RESOLVED}</option>
              <option value="DISMISSED">{t.statuses.DISMISSED}</option>
            </select>

            {/* Simulation Filter */}
            <select
              value={simFilter}
              onChange={(e) => setSimFilter(e.target.value as 'ALL' | 'PROD' | 'SIM')}
              className="h-9 px-2.5 rounded-md border border-input bg-background text-xs focus:outline-none focus:ring-1 focus:ring-ring"
            >
              <option value="ALL">Barcha manbalar</option>
              <option value="PROD">{t.filters.productionOnly}</option>
              <option value="SIM">{t.filters.simulatedOnly}</option>
            </select>

            <Button
              variant="outline"
              size="sm"
              onClick={() => refetch()}
              disabled={isRefetching}
              className="h-9 px-2.5"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${isRefetching ? 'animate-spin' : ''}`} />
            </Button>

            {(selectedSeverity !== 'ALL' ||
              selectedStatus !== 'ALL' ||
              selectedType !== 'ALL' ||
              simFilter !== 'ALL' ||
              searchTerm) && (
              <Button
                variant="ghost"
                size="sm"
                onClick={handleResetFilters}
                className="h-9 text-xs text-muted-foreground hover:text-foreground"
              >
                {t.filters.reset}
              </Button>
            )}
          </div>
        </div>
      </div>

      {/* Feed List */}
      {isLoading ? (
        <div className="py-12 flex flex-col items-center justify-center space-y-2">
          <div className="w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin" />
          <p className="text-xs text-muted-foreground">{translations[lang].common.loading}</p>
        </div>
      ) : anomalies.length === 0 ? (
        <div className="p-10 border border-dashed border-border rounded-xl text-center space-y-2 bg-card/40">
          <AlertCircle className="w-8 h-8 text-muted-foreground mx-auto" />
          <h4 className="font-semibold text-foreground text-sm">{t.empty}</h4>
          <p className="text-xs text-muted-foreground max-w-sm mx-auto">{t.emptyDesc}</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          {anomalies.map((item) => (
            <AnomalyCard
              key={item.id}
              anomaly={item}
              onAction={handleOpenAction}
              onInspect={(a) => setActiveAnomalyForDetails(a)}
              lang={lang}
            />
          ))}
        </div>
      )}

      {/* Action Dialog */}
      <AnomalyActionDialog
        isOpen={Boolean(activeAnomalyForAction && actionType)}
        onClose={() => {
          setActiveAnomalyForAction(null)
          setActionType(null)
        }}
        anomaly={activeAnomalyForAction}
        actionType={actionType}
        lang={lang}
      />

      {/* Details Modal */}
      <AnomalyDetailsModal
        isOpen={Boolean(activeAnomalyForDetails)}
        onClose={() => setActiveAnomalyForDetails(null)}
        anomaly={activeAnomalyForDetails}
        lang={lang}
      />
    </div>
  )
}
