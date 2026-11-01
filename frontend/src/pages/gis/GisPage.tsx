import React, { useState, useMemo } from 'react'
import {
  MapPin,
  Waves,
  AlertTriangle,
  RotateCcw,
  Info,
} from 'lucide-react'
import {
  GisField,
  GisWaterSource,
  GisOverviewParams,
} from '@/entities/gis'
import {
  WaterFieldMap,
  FieldWaterSidePanel,
  MapFilters,
  MapLayerControl,
  MapLegend,
  GisFilterState,
  DEFAULT_GIS_FILTERS,
  MapLayerState,
  useGisOverviewQuery,
} from '@/features/water/gis'
import { useFieldAnalyticsSummaryQuery } from '@/features/sensor/queries/useSensorQueries'
import { useFieldAnomalySummary } from '@/features/anomaly/queries/useAnomalyQueries'
import { useFieldRecommendations } from '@/features/recommendation/queries/useRecommendationQueries'
import { useActiveFieldSmartIrrigationPlan, useFieldWaterBudget } from '@/features/irrigation-planning'
import { useAuth } from '@/features/auth/context/AuthContext'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { Button } from '@/shared/ui/button'
import { Card } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import { NavSection } from '@/widgets/sidebar'

interface GisPageProps {
  lang: SupportedLanguage
  onNavigate?: (section: NavSection) => void
}

export const GisPage: React.FC<GisPageProps> = ({ lang, onNavigate }) => {
  const t = translations[lang]
  const { user } = useAuth()

  // State
  const [filters, setFilters] = useState<GisFilterState>(DEFAULT_GIS_FILTERS)
  const [dateRange, setDateRange] = useState<GisOverviewParams>({})
  const [layers, setLayers] = useState<MapLayerState>({
    showFields: true,
    showWaterSources: true,
    showWaterStatus: true,
    showSupplyLinks: true,
  })

  const [selectedField, setSelectedField] = useState<GisField | null>(null)
  const [selectedWaterSource, setSelectedWaterSource] = useState<GisWaterSource | null>(null)
  const [geomWarnings, setGeomWarnings] = useState<string[]>([])

  // TanStack Query for unified overview
  const {
    data: overviewData,
    isLoading,
    isError,
    refetch,
  } = useGisOverviewQuery(dateRange)

  const allFields = overviewData?.fields || []
  const allWaterSources = overviewData?.waterSources || []
  const allStatuses = overviewData?.waterStatuses || []
  const allAllocations = overviewData?.allocations || []

  // Real-time telemetry summary for selected field parcel
  const { data: telemetrySummary } = useFieldAnalyticsSummaryQuery(selectedField?.id)
  const { data: anomalySummary } = useFieldAnomalySummary(selectedField?.id || '')
  const { data: recommendationsPage } = useFieldRecommendations(selectedField?.id || '', 0, 5)
  const { data: activeSmartPlan } = useActiveFieldSmartIrrigationPlan(selectedField?.id)
  const { data: fieldWaterBudget } = useFieldWaterBudget(selectedField?.id)

  // Status map for fast category / over-consumption lookup
  const statusMap = useMemo(() => {
    const map = new Map<string, (typeof allStatuses)[0]>()
    for (const s of allStatuses) {
      map.set(s.fieldId, s)
    }
    return map
  }, [allStatuses])

  // Client-side filtering
  const filteredFields = useMemo(() => {
    return allFields.filter((f) => {
      // 1. Search query
      if (filters.search) {
        const query = filters.search.toLowerCase()
        const matchName = f.name.toLowerCase().includes(query)
        const matchCrop = f.cropType.toLowerCase().includes(query)
        if (!matchName && !matchCrop) return false
      }

      // 2. Field status
      if (filters.fieldStatus !== 'ALL' && f.status !== filters.fieldStatus) {
        return false
      }

      // 3. Crop
      if (filters.cropType !== 'ALL' && f.cropType !== filters.cropType) {
        return false
      }

      // 4. Soil
      if (filters.soilType !== 'ALL' && f.soilType !== filters.soilType) {
        return false
      }

      // 5. Irrigation method
      if (filters.irrigationMethod !== 'ALL' && f.irrigationMethod !== filters.irrigationMethod) {
        return false
      }

      // 6. Water status category
      if (filters.waterStatus !== 'ALL') {
        const status = statusMap.get(f.id)
        const category = status?.category || 'NO_ALLOCATION'
        if (category !== filters.waterStatus) return false
      }

      return true
    })
  }, [allFields, filters, statusMap])

  const filteredWaterSources = useMemo(() => {
    return allWaterSources.filter((s) => {
      if (filters.search) {
        const query = filters.search.toLowerCase()
        const matchName = s.name.toLowerCase().includes(query)
        const matchType = s.type.toLowerCase().includes(query)
        if (!matchName && !matchType) return false
      }

      if (filters.waterSourceStatus !== 'ALL' && s.status !== filters.waterSourceStatus) {
        return false
      }

      return true
    })
  }, [allWaterSources, filters])

  // KPI counters
  const totalOverConsumedCount = useMemo(() => {
    return allStatuses.filter((s) => s.isOverConsumed).length
  }, [allStatuses])

  // Selected field's status
  const selectedFieldWaterStatus = useMemo(() => {
    if (!selectedField) return null
    return statusMap.get(selectedField.id) || null
  }, [selectedField, statusMap])

  const handleSelectField = (field: GisField | null) => {
    setSelectedField(field)
    setSelectedWaterSource(null)
  }

  const handleSelectWaterSource = (source: GisWaterSource | null) => {
    setSelectedWaterSource(source)
    setSelectedField(null)
  }

  const handleClosePanel = () => {
    setSelectedField(null)
    setSelectedWaterSource(null)
  }

  const handleInvalidGeometry = (msg: string) => {
    setGeomWarnings((prev) => (prev.includes(msg) ? prev : [...prev, msg]))
  }

  return (
    <div className="space-y-4">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl sm:text-2xl font-bold tracking-tight text-foreground flex items-center gap-2">
              <MapPin className="w-5 h-5 text-primary shrink-0" />
              <span>{t.gis?.title || 'Yangiariq Suv va Dala GIS Intellekti'}</span>
            </h1>
            <Badge variant="outline" className="text-xs px-2 py-0.5 border-primary/40 bg-primary/5 text-primary">
              v06.5
            </Badge>
          </div>
          <p className="text-xs sm:text-sm text-muted-foreground mt-0.5">
            {t.gis?.subtitle ||
              'Xorazm viloyati Yangiariq tumani dala maydonlari, suv manbalari va taqsimot meʼyorlarining yagona fazoviy xaritasi'}
          </p>
        </div>

        {/* Quick KPI Posture Pills */}
        <div className="flex items-center gap-2 text-xs flex-wrap">
          <div className="px-2.5 py-1 rounded-lg border border-border bg-card flex items-center gap-1.5 shadow-sm">
            <MapPin className="w-3.5 h-3.5 text-primary" />
            <span className="text-muted-foreground">{t.nav?.fields || 'Dalalar'}:</span>
            <strong className="text-foreground">{filteredFields.length}</strong>
          </div>

          <div className="px-2.5 py-1 rounded-lg border border-border bg-card flex items-center gap-1.5 shadow-sm">
            <Waves className="w-3.5 h-3.5 text-sky-500" />
            <span className="text-muted-foreground">{t.water?.sources?.title || 'Manbalar'}:</span>
            <strong className="text-foreground">{filteredWaterSources.length}</strong>
          </div>

          {totalOverConsumedCount > 0 && (
            <div className="px-2.5 py-1 rounded-lg border border-rose-300 dark:border-rose-900 bg-rose-50 dark:bg-rose-950/40 text-rose-700 dark:text-rose-300 flex items-center gap-1.5 font-semibold shadow-sm animate-pulse">
              <AlertTriangle className="w-3.5 h-3.5 text-rose-500" />
              <span>{t.gis?.status?.overConsumed || 'Ortiqcha sarf'}:</span>
              <strong>{totalOverConsumedCount}</strong>
            </div>
          )}
        </div>
      </div>

      {/* Non-blocking geometry warning banners */}
      {geomWarnings.length > 0 && (
        <div className="p-3 rounded-lg border border-amber-300 dark:border-amber-900 bg-amber-50 dark:bg-amber-950/30 text-amber-900 dark:text-amber-200 text-xs flex items-start justify-between gap-2">
          <div className="flex items-start gap-2">
            <Info className="w-4 h-4 text-amber-500 shrink-0 mt-0.5" />
            <div className="space-y-0.5">
              <span className="font-bold">Eslatma (Geometriya ogohlantirishi):</span>
              <ul className="list-disc list-inside space-y-0.5 text-[11px]">
                {geomWarnings.map((w, idx) => (
                  <li key={idx}>{w}</li>
                ))}
              </ul>
            </div>
          </div>
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setGeomWarnings([])}
            className="h-6 px-2 text-xs text-amber-700 dark:text-amber-300"
          >
            Yopish
          </Button>
        </div>
      )}

      {/* Filter Toolbar */}
      <MapFilters
        filters={filters}
        onChange={setFilters}
        onDateRangeChange={setDateRange}
        lang={lang}
      />

      {/* Main Map + Side Panel Viewport */}
      {isLoading ? (
        <Card className="h-[620px] flex flex-col items-center justify-center p-8 space-y-3 bg-muted/20 border-dashed">
          <div className="h-10 w-10 rounded-full border-3 border-primary border-t-transparent animate-spin" />
          <p className="text-sm font-medium text-foreground">
            {t.gis?.loading || 'Yangiariq tumani GIS xaritasi yuklanmoqda...'}
          </p>
          <span className="text-xs text-muted-foreground">
            Dala chegaralari, suv manbalari va hajmlar hisoblanmoqda
          </span>
        </Card>
      ) : isError ? (
        <Card className="p-8 text-center space-y-3 bg-rose-50/20 border-rose-200 dark:border-rose-900">
          <AlertTriangle className="w-8 h-8 text-rose-500 mx-auto" />
          <h3 className="font-bold text-base text-foreground">
            {t.gis?.errorTitle || 'Xarita maʼlumotlarini yuklab boʻlmadi'}
          </h3>
          <p className="text-xs text-muted-foreground max-w-md mx-auto">
            Internet aloqasi yoki tizim holatini tekshirib, qayta urinib koʻring.
          </p>
          <Button onClick={() => refetch()} variant="outline" size="sm" className="gap-1.5">
            <RotateCcw className="w-3.5 h-3.5" />
            <span>{t.common?.refresh || 'Qayta urinish'}</span>
          </Button>
        </Card>
      ) : (
        <div className="relative flex flex-col md:flex-row gap-4 items-start">
          {/* Interactive Map Box */}
          <div className="relative w-full flex-1 min-w-0">
            <WaterFieldMap
              fields={filteredFields}
              waterSources={filteredWaterSources}
              waterStatuses={allStatuses}
              allocations={allAllocations}
              layers={layers}
              selectedField={selectedField}
              selectedWaterSource={selectedWaterSource}
              onSelectField={handleSelectField}
              onSelectWaterSource={handleSelectWaterSource}
              onInvalidGeometryDetected={handleInvalidGeometry}
              lang={lang}
              height="620px"
            />

            {/* Floating Map Legend (Bottom-Left) */}
            <div className="absolute bottom-4 left-4 z-[400] max-w-[calc(100%-2rem)]">
              <MapLegend lang={lang} />
            </div>

            {/* Floating Layer Control (Top-Right) */}
            <div className="absolute top-4 right-4 z-[400]">
              <MapLayerControl layers={layers} onChange={setLayers} lang={lang} />
            </div>

            {/* Empty results overlay if filters hide all features */}
            {filteredFields.length === 0 && filteredWaterSources.length === 0 && (
              <div className="absolute inset-0 bg-background/60 backdrop-blur-xs flex items-center justify-center p-4 z-[300]">
                <div className="bg-card border border-border shadow-lg rounded-xl p-5 text-center max-w-sm space-y-2">
                  <MapPin className="w-6 h-6 text-muted-foreground mx-auto" />
                  <div className="font-bold text-sm text-foreground">
                    {t.gis?.emptyTitle || 'Mos keladigan fazoviy maʼlumot topilmadi'}
                  </div>
                  <p className="text-xs text-muted-foreground">
                    Qidiruv soʻzini yoki filtrlarni oʻzgartiring.
                  </p>
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => setFilters(DEFAULT_GIS_FILTERS)}
                    className="text-xs h-8 mt-2"
                  >
                    Filtrlarni bekor qilish
                  </Button>
                </div>
              </div>
            )}
          </div>

          {/* Collapsible Operational Side Panel */}
          {(selectedField || selectedWaterSource) && (
            <div className="w-full md:w-auto shrink-0 z-10">
              <FieldWaterSidePanel
                selectedField={selectedField}
                selectedWaterSource={selectedWaterSource}
                waterStatus={selectedFieldWaterStatus}
                allocations={allAllocations}
                allFields={allFields}
                allWaterSources={allWaterSources}
                telemetrySummary={telemetrySummary}
                anomalySummary={anomalySummary}
                recommendations={recommendationsPage?.content}
                activePlan={activeSmartPlan}
                waterBudget={fieldWaterBudget}
                onSelectField={handleSelectField}
                onSelectWaterSource={handleSelectWaterSource}
                onClose={handleClosePanel}
                onNavigateToIrrigation={() => onNavigate?.('irrigation')}
                userRole={user?.role}
                lang={lang}
              />
            </div>
          )}
        </div>
      )}
    </div>
  )
}
