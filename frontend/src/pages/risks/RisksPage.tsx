import React, { useState, useEffect } from 'react'
import {
  CheckCircle2,
  Grid,
  Layers,
  LayoutList,
  Lightbulb,
  RotateCcw,
  ShieldAlert,
} from 'lucide-react'
import { SupportedLanguage } from '@/shared/i18n'
import { NavSection } from '@/widgets/sidebar'
import {
  useRiskOverviewQuery,
  useFieldRiskListQuery,
  useRiskRecommendationsQuery,
  RiskOverviewCards,
  CriticalRiskBanner,
  FieldRiskTable,
  FieldRiskCard,
  RiskRecommendationCard,
  RiskAssessmentDetailModal,
} from '@/features/risk'
import { Button } from '@/shared/ui/button'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/shared/ui/tabs'
import { Badge } from '@/shared/ui/badge'

interface RisksPageProps {
  lang: SupportedLanguage
  initialFieldId?: string | null
  onNavigate?: (section: NavSection, fieldId?: string) => void
}

export const RisksPage: React.FC<RisksPageProps> = ({
  lang,
  initialFieldId,
  onNavigate,
}) => {
  const [selectedFieldId, setSelectedFieldId] = useState<string | null>(initialFieldId || null)
  const [detailModalOpen, setDetailModalOpen] = useState<boolean>(Boolean(initialFieldId))
  const [activeTab, setActiveTab] = useState<'fields' | 'recommendations' | 'safety'>('fields')
  const [viewMode, setViewMode] = useState<'table' | 'cards'>('table')

  // Filters state
  const [activeOverviewFilter, setActiveOverviewFilter] = useState<string>('ALL')
  const [searchTerm, setSearchTerm] = useState('')
  const [severityFilter, setSeverityFilter] = useState('ALL')
  const [categoryFilter, setCategoryFilter] = useState('ALL')
  const [currentPage, setCurrentPage] = useState(0)

  useEffect(() => {
    if (initialFieldId) {
      setSelectedFieldId(initialFieldId)
      setDetailModalOpen(true)
    }
  }, [initialFieldId])

  // Queries
  const {
    data: overview,
    refetch: refetchOverview,
    isFetching: isOverviewFetching,
  } = useRiskOverviewQuery()

  const {
    data: fieldsPage,
    isLoading: isFieldsLoading,
    refetch: refetchFields,
    isFetching: isFieldsFetching,
  } = useFieldRiskListQuery({
    severity: severityFilter !== 'ALL' ? severityFilter : undefined,
    category: categoryFilter !== 'ALL' ? categoryFilter : undefined,
    search: searchTerm || undefined,
    page: currentPage,
    size: 15,
  })

  const {
    data: recommendationsPage,
    isLoading: isRecsLoading,
    refetch: refetchRecs,
  } = useRiskRecommendationsQuery({
    page: 0,
    size: 20,
  })

  const handleSelectField = (fieldId: string) => {
    setSelectedFieldId(fieldId)
    setDetailModalOpen(true)
  }

  const handleOverviewFilterChange = (filterKey: string) => {
    setActiveOverviewFilter(filterKey)
    setCurrentPage(0)
    if (filterKey === 'ALL') {
      setSeverityFilter('ALL')
      setCategoryFilter('ALL')
    } else if (filterKey === 'CRITICAL') {
      setSeverityFilter('CRITICAL')
      setCategoryFilter('ALL')
    } else if (filterKey === 'HYDRAULIC') {
      setSeverityFilter('ALL')
      setCategoryFilter('HYDRAULIC')
    } else if (filterKey === 'WATER_SHORTAGE') {
      setSeverityFilter('ALL')
      setCategoryFilter('WATER_AVAILABILITY')
    } else if (filterKey === 'SOIL_MOISTURE') {
      setSeverityFilter('ALL')
      setCategoryFilter('SOIL_MOISTURE')
    } else if (filterKey === 'TELEMETRY') {
      setSeverityFilter('ALL')
      setCategoryFilter('SENSOR_DATA_RELIABILITY')
    }
  }

  const handleRefreshAll = () => {
    refetchOverview()
    refetchFields()
    refetchRecs()
  }

  const handleNavigateWorkflow = (destinationRoute: string) => {
    if (!onNavigate) return

    if (destinationRoute.startsWith('/anomalies')) {
      const match = destinationRoute.match(/fieldId=([^&]+)/)
      onNavigate('anomalies', match ? match[1] : undefined)
    } else if (destinationRoute.startsWith('/operations')) {
      const match = destinationRoute.match(/fieldId=([^&]+)/)
      onNavigate('operations', match ? match[1] : undefined)
    } else if (destinationRoute.startsWith('/irrigation')) {
      const match = destinationRoute.match(/fieldId=([^&]+)/)
      onNavigate('irrigation', match ? match[1] : undefined)
    } else if (destinationRoute.startsWith('/sensors')) {
      const match = destinationRoute.match(/fieldId=([^&]+)/)
      onNavigate('sensors', match ? match[1] : undefined)
    } else if (destinationRoute.startsWith('/gis')) {
      const match = destinationRoute.match(/fieldId=([^&]+)/)
      onNavigate('gis', match ? match[1] : undefined)
    } else if (destinationRoute.startsWith('/predictions')) {
      onNavigate('predictions')
    }
  }

  const isHydraulicBlockedList = (fieldsPage?.content || []).filter((f) => f.isHydraulicBlocked)

  return (
    <div className="space-y-6 p-4 sm:p-6 max-w-7xl mx-auto" data-testid="risks-page">
      {/* 1. Header Bar */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-2 border-b border-border">
        <div className="space-y-1">
          <div className="flex items-center gap-2">
            <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-foreground">
              {lang === 'uz'
                ? 'Dala Xavflari va Barqarorlik Intellekti'
                : lang === 'ru'
                ? 'Интеллект Рисков и Устойчивости Полей'
                : 'Farm-Level Predictive Risk & Resilience Intelligence'}
            </h1>
            <Badge variant="outline" className="text-xs font-semibold bg-primary/10 text-primary border-primary/30">
              Milestone 08.13
            </Badge>
          </div>
          <p className="text-xs sm:text-sm text-muted-foreground">
            {lang === 'uz'
              ? 'Gidravlik xavfsizlik, suv taqchilligi, tuproq namligi va telemetriya ishonchliligi boʻyicha dalillarga asoslangan tahlil'
              : lang === 'ru'
              ? 'Доказательная аналитика гидравлической безопасности, дефицита воды, влажности почвы и надежности телеметрии'
              : 'Evidence-based risk assessments synthesized across hydraulic safety, water availability, soil moisture, and sensor reliability.'}
          </p>
        </div>

        <div className="flex items-center gap-2 self-start sm:self-center">
          <Button
            variant="outline"
            size="sm"
            onClick={handleRefreshAll}
            disabled={isOverviewFetching || isFieldsFetching}
            className="h-8 text-xs gap-1.5"
            data-testid="btn-refresh-risks"
          >
            <RotateCcw className={`w-3.5 h-3.5 ${isOverviewFetching || isFieldsFetching ? 'animate-spin' : ''}`} />
            <span>{lang === 'uz' ? 'Yangilash' : lang === 'ru' ? 'Обновить' : 'Refresh'}</span>
          </Button>
        </div>
      </div>

      {/* 2. Critical Emergency Banner (Hydraulic & High-Confidence Critical Risks) */}
      {overview && overview.criticalEmergencyFields && overview.criticalEmergencyFields.length > 0 && (
        <CriticalRiskBanner
          emergencyFields={overview.criticalEmergencyFields}
          onSelectField={handleSelectField}
          onNavigateRoute={handleNavigateWorkflow}
        />
      )}

      {/* 3. Farm-Level Overview Cards */}
      <RiskOverviewCards
        overview={overview}
        activeFilter={activeOverviewFilter}
        onFilterChange={handleOverviewFilterChange}
      />

      {/* 4. Tab Navigation */}
      <Tabs
        value={activeTab}
        onValueChange={(val) => setActiveTab(val as any)}
        className="w-full space-y-4"
      >
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-border pb-2">
          <TabsList className="grid grid-cols-3 w-full sm:w-auto">
            <TabsTrigger value="fields" className="text-xs font-semibold">
              <span className="flex items-center gap-1.5">
                <Layers className="w-3.5 h-3.5" />
                <span>Field Risk Matrix</span>
                <span className="text-[10px] px-1.5 py-0.2 rounded-full bg-muted font-mono">
                  {overview?.totalAssessedFields ?? 0}
                </span>
              </span>
            </TabsTrigger>

            <TabsTrigger value="recommendations" className="text-xs font-semibold">
              <span className="flex items-center gap-1.5">
                <Lightbulb className="w-3.5 h-3.5" />
                <span>Actionable Recommendations</span>
                <span className="text-[10px] px-1.5 py-0.2 rounded-full bg-primary/20 text-primary font-mono font-bold">
                  {overview?.activeRecommendationsCount ?? 0}
                </span>
              </span>
            </TabsTrigger>

            <TabsTrigger value="safety" className="text-xs font-semibold">
              <span className="flex items-center gap-1.5">
                <ShieldAlert className="w-3.5 h-3.5" />
                <span>Safety Interlocks</span>
                {overview && overview.hydraulicEmergencyCount > 0 && (
                  <span className="text-[10px] px-1.5 py-0.2 rounded-full bg-rose-600 text-white font-mono font-bold animate-pulse">
                    {overview.hydraulicEmergencyCount}
                  </span>
                )}
              </span>
            </TabsTrigger>
          </TabsList>

          {/* View Toggle for Fields Tab */}
          {activeTab === 'fields' && (
            <div className="flex items-center gap-1 self-end sm:self-center">
              <Button
                variant={viewMode === 'table' ? 'secondary' : 'ghost'}
                size="icon"
                className="h-8 w-8"
                onClick={() => setViewMode('table')}
                title="Table View"
                data-testid="btn-view-table"
              >
                <LayoutList className="w-4 h-4" />
              </Button>
              <Button
                variant={viewMode === 'cards' ? 'secondary' : 'ghost'}
                size="icon"
                className="h-8 w-8"
                onClick={() => setViewMode('cards')}
                title="Grid Cards View"
                data-testid="btn-view-cards"
              >
                <Grid className="w-4 h-4" />
              </Button>
            </div>
          )}
        </div>

        {/* Tab 1: Field Risk Matrix */}
        <TabsContent value="fields" className="space-y-4">
          {viewMode === 'table' ? (
            <FieldRiskTable
              fields={fieldsPage?.content || []}
              isLoading={isFieldsLoading}
              totalElements={fieldsPage?.totalElements || 0}
              page={currentPage}
              totalPages={fieldsPage?.totalPages || 1}
              onPageChange={setCurrentPage}
              onSelectField={handleSelectField}
              onSearchChange={setSearchTerm}
              onSeverityFilterChange={setSeverityFilter}
              onCategoryFilterChange={setCategoryFilter}
            />
          ) : (
            <div className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {(fieldsPage?.content || []).map((field) => (
                  <FieldRiskCard
                    key={field.fieldId}
                    profile={field}
                    onSelect={handleSelectField}
                  />
                ))}
              </div>
            </div>
          )}
        </TabsContent>

        {/* Tab 2: Actionable Recommendations */}
        <TabsContent value="recommendations" className="space-y-4">
          <div className="space-y-3">
            {isRecsLoading ? (
              <div className="p-8 text-center text-xs text-muted-foreground animate-pulse">
                Loading prioritized recommendations...
              </div>
            ) : !recommendationsPage || recommendationsPage.content.length === 0 ? (
              <div className="p-12 text-center rounded-xl border border-dashed text-xs text-muted-foreground">
                No active recommendations pending. Agricultural and operational targets are currently satisfied.
              </div>
            ) : (
              recommendationsPage.content.map((rec) => (
                <RiskRecommendationCard
                  key={rec.id}
                  recommendation={rec}
                  onNavigateRoute={handleNavigateWorkflow}
                />
              ))
            )}
          </div>
        </TabsContent>

        {/* Tab 3: Hydraulic & Safety Interlocks */}
        <TabsContent value="safety" className="space-y-4">
          <div className="p-4 rounded-xl border bg-muted/30 text-xs text-muted-foreground space-y-1">
            <strong className="text-foreground block font-bold text-sm">
              Non-Negotiable Hydraulic Precedence
            </strong>
            <p>
              Under Yangiariq Aqua AI safety governance, pipeline integrity and canal rupture interlocks hold absolute priority over all irrigation dispatches. No automated score or algorithm may override a physical pressure collapse or pipe burst interlock.
            </p>
          </div>

          {isHydraulicBlockedList.length === 0 ? (
            <div className="p-8 text-center rounded-xl border bg-emerald-500/5 border-emerald-300 dark:border-emerald-900 text-emerald-800 dark:text-emerald-300 text-xs flex flex-col items-center gap-2">
              <CheckCircle2 className="w-8 h-8 text-emerald-600" />
              <span className="font-semibold text-sm">All Monitored Hydraulic Subsystems Nominal</span>
              <p className="max-w-md text-muted-foreground text-xs">
                Zero pipeline bursts or critical distribution pressure drops detected across active parcels.
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {isHydraulicBlockedList.map((field) => (
                <FieldRiskCard
                  key={field.fieldId}
                  profile={field}
                  onSelect={handleSelectField}
                />
              ))}
            </div>
          )}
        </TabsContent>
      </Tabs>

      {/* 5. Comprehensive Field Detail & Remedial Action Modal */}
      <RiskAssessmentDetailModal
        fieldId={selectedFieldId}
        isOpen={detailModalOpen}
        onClose={() => {
          setDetailModalOpen(false)
          setSelectedFieldId(null)
        }}
        onNavigateRoute={handleNavigateWorkflow}
      />
    </div>
  )
}
