import React, { useState } from 'react'
import {
  Search,
  RotateCcw,
  Calendar,
  AlertCircle,
  SlidersHorizontal,
} from 'lucide-react'
import {
  DateRangePreset,
  getDateRangeFromPreset,
} from '../gisWaterUtils'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { cn } from '@/shared/lib/utils'

export interface GisFilterState {
  search: string
  fieldStatus: 'ALL' | 'ACTIVE' | 'INACTIVE'
  cropType: string
  soilType: string
  irrigationMethod: string
  waterStatus: 'ALL' | 'NORMAL' | 'WARNING' | 'OVER_CONSUMED' | 'NO_ALLOCATION'
  waterSourceStatus: 'ALL' | 'ACTIVE' | 'INACTIVE'
  datePreset: DateRangePreset
  customFrom: string
  customTo: string
}

export const DEFAULT_GIS_FILTERS: GisFilterState = {
  search: '',
  fieldStatus: 'ALL',
  cropType: 'ALL',
  soilType: 'ALL',
  irrigationMethod: 'ALL',
  waterStatus: 'ALL',
  waterSourceStatus: 'ALL',
  datePreset: 'all',
  customFrom: '',
  customTo: '',
}

interface MapFiltersProps {
  filters: GisFilterState
  onChange: (filters: GisFilterState) => void
  onDateRangeChange: (range: { from?: string; to?: string }) => void
  lang: SupportedLanguage
  className?: string
}

export const MapFilters: React.FC<MapFiltersProps> = ({
  filters,
  onChange,
  onDateRangeChange,
  lang,
  className,
}) => {
  const t = translations[lang]
  const [showAdvanced, setShowAdvanced] = useState(false)
  const [dateError, setDateError] = useState<string | null>(null)

  const handleSearchChange = (val: string) => {
    onChange({ ...filters, search: val })
  }

  const handlePresetChange = (preset: DateRangePreset) => {
    setDateError(null)
    const range = getDateRangeFromPreset(preset)
    onChange({
      ...filters,
      datePreset: preset,
      customFrom: range.from ? range.from.substring(0, 10) : '',
      customTo: range.to ? range.to.substring(0, 10) : '',
    })
    onDateRangeChange(range)
  }

  const handleCustomDateChange = (from: string, to: string) => {
    if (from && to && from > to) {
      setDateError(t.gis?.validation?.invalidDateRange || 'Boshlanish sanasi tugash sanasidan oldin boʻlishi kerak')
      return
    }
    setDateError(null)
    onChange({
      ...filters,
      datePreset: 'custom',
      customFrom: from,
      customTo: to,
    })

    const fromIso = from ? new Date(`${from}T00:00:00Z`).toISOString() : undefined
    const toIso = to ? new Date(`${to}T23:59:59Z`).toISOString() : undefined
    onDateRangeChange({ from: fromIso, to: toIso })
  }

  const handleReset = () => {
    setDateError(null)
    onChange(DEFAULT_GIS_FILTERS)
    onDateRangeChange({})
  }

  const hasActiveFilters =
    filters.search !== '' ||
    filters.fieldStatus !== 'ALL' ||
    filters.cropType !== 'ALL' ||
    filters.soilType !== 'ALL' ||
    filters.irrigationMethod !== 'ALL' ||
    filters.waterStatus !== 'ALL' ||
    filters.waterSourceStatus !== 'ALL' ||
    filters.datePreset !== 'all'

  return (
    <div
      className={cn(
        'bg-card border border-border/80 shadow-sm rounded-xl p-3 sm:p-4 space-y-3',
        className
      )}
    >
      {/* Primary Bar: Search, Water Status, Date Preset, Advanced Toggle, Reset */}
      <div className="flex flex-wrap items-center gap-2.5">
        {/* Search Input */}
        <div className="relative min-w-[200px] flex-1">
          <Search className="w-4 h-4 text-muted-foreground absolute left-3 top-1/2 -translate-y-1/2" />
          <Input
            value={filters.search}
            onChange={(e) => handleSearchChange(e.target.value)}
            placeholder={t.gis?.filters?.searchPlaceholder || 'Dala, ekin yoki suv manbasini qidirish...'}
            className="pl-9 h-9 text-xs"
          />
        </div>

        {/* Water Status Filter */}
        <div className="flex items-center gap-1.5 shrink-0">
          <span className="text-xs text-muted-foreground font-medium hidden sm:inline">
            {t.gis?.filters?.waterStatus || 'Suv holati'}:
          </span>
          <select
            value={filters.waterStatus}
            onChange={(e) =>
              onChange({
                ...filters,
                waterStatus: e.target.value as GisFilterState['waterStatus'],
              })
            }
            className="h-9 px-2.5 py-1 text-xs rounded-md border border-input bg-background text-foreground focus:outline-none focus:ring-1 focus:ring-primary"
            aria-label="Water Status Filter"
          >
            <option value="ALL">{t.gis?.filters?.allStatuses || 'Barcha holatlar'}</option>
            <option value="NORMAL">{t.gis?.status?.normal || 'Normal (≤80%)'}</option>
            <option value="WARNING">{t.gis?.status?.warning || 'Ogohlantirish (80–100%)'}</option>
            <option value="OVER_CONSUMED">{t.gis?.status?.overConsumed || 'Meʼyordan oshgan (>100%)'}</option>
            <option value="NO_ALLOCATION">{t.gis?.status?.noAllocation || 'Limit ajratilmagan'}</option>
          </select>
        </div>

        {/* Date Preset Filter */}
        <div className="flex items-center gap-1.5 shrink-0">
          <Calendar className="w-3.5 h-3.5 text-muted-foreground hidden sm:inline" />
          <select
            value={filters.datePreset}
            onChange={(e) => handlePresetChange(e.target.value as DateRangePreset)}
            className="h-9 px-2.5 py-1 text-xs rounded-md border border-input bg-background text-foreground focus:outline-none focus:ring-1 focus:ring-primary"
            aria-label="Date Range Preset Filter"
          >
            <option value="all">{t.gis?.date?.all || 'Barcha vaqt'}</option>
            <option value="today">{t.gis?.date?.today || 'Bugun'}</option>
            <option value="7days">{t.gis?.date?.last7Days || 'Soʻnggi 7 kun'}</option>
            <option value="30days">{t.gis?.date?.last30Days || 'Soʻnggi 30 kun'}</option>
            <option value="season">{t.gis?.date?.thisSeason || 'Joriy mavsum'}</option>
            <option value="custom">{t.gis?.date?.custom || 'Boshqa muddat'}</option>
          </select>
        </div>

        {/* Advanced Filters Toggle */}
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={() => setShowAdvanced(!showAdvanced)}
          className={cn('h-9 gap-1.5 text-xs', showAdvanced && 'bg-accent text-accent-foreground')}
        >
          <SlidersHorizontal className="w-3.5 h-3.5" />
          <span className="hidden md:inline">{t.gis?.filters?.advanced || 'Filtrlar'}</span>
        </Button>

        {/* Reset Button */}
        {hasActiveFilters && (
          <Button
            type="button"
            variant="ghost"
            size="sm"
            onClick={handleReset}
            className="h-9 gap-1 text-xs text-muted-foreground hover:text-foreground"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            <span className="hidden lg:inline">{t.common.cancel || 'Tozalash'}</span>
          </Button>
        )}
      </div>

      {/* Custom Date Pickers (when datePreset == 'custom') */}
      {filters.datePreset === 'custom' && (
        <div className="flex flex-wrap items-center gap-2 pt-1 pb-1">
          <span className="text-xs text-muted-foreground font-medium">{t.gis?.date?.from || 'Dan'}:</span>
          <Input
            type="date"
            value={filters.customFrom}
            onChange={(e) => handleCustomDateChange(e.target.value, filters.customTo)}
            className="w-36 h-8 text-xs"
          />
          <span className="text-xs text-muted-foreground font-medium">{t.gis?.date?.to || 'Gacha'}:</span>
          <Input
            type="date"
            value={filters.customTo}
            onChange={(e) => handleCustomDateChange(filters.customFrom, e.target.value)}
            className="w-36 h-8 text-xs"
          />
          {dateError && (
            <span className="flex items-center gap-1 text-xs text-rose-500 font-medium">
              <AlertCircle className="w-3 h-3" />
              {dateError}
            </span>
          )}
        </div>
      )}

      {/* Advanced Filters Drawer / Section */}
      {showAdvanced && (
        <div className="pt-3 border-t border-border/60 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-2.5">
          {/* Field Status */}
          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-muted-foreground block">
              {t.gis?.filters?.fieldStatus || 'Dala holati'}
            </label>
            <select
              value={filters.fieldStatus}
              onChange={(e) =>
                onChange({ ...filters, fieldStatus: e.target.value as GisFilterState['fieldStatus'] })
              }
              className="w-full h-8 px-2 text-xs rounded-md border border-input bg-background text-foreground"
            >
              <option value="ALL">{t.common.status || 'Barchasi'}</option>
              <option value="ACTIVE">Faol (Active)</option>
              <option value="INACTIVE">Nofaol (Inactive)</option>
            </select>
          </div>

          {/* Crop Type */}
          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-muted-foreground block">
              {t.fields?.table?.crop || 'Ekin turi'}
            </label>
            <select
              value={filters.cropType}
              onChange={(e) => onChange({ ...filters, cropType: e.target.value })}
              className="w-full h-8 px-2 text-xs rounded-md border border-input bg-background text-foreground"
            >
              <option value="ALL">Barchasi</option>
              <option value="COTTON">Paxta (Cotton)</option>
              <option value="WHEAT">Bugʻdoy (Wheat)</option>
              <option value="CORN">Makkajoʻxori (Corn)</option>
              <option value="RICE">Sholi (Rice)</option>
              <option value="FRUIT">Bogʻ (Fruit)</option>
              <option value="VEGETABLE">Sabzavot (Vegetables)</option>
              <option value="FODDER">Ozuqa (Fodder)</option>
              <option value="OTHER">Boshqa (Other)</option>
            </select>
          </div>

          {/* Soil Type */}
          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-muted-foreground block">
              {t.fields?.table?.soil || 'Tuproq turi'}
            </label>
            <select
              value={filters.soilType}
              onChange={(e) => onChange({ ...filters, soilType: e.target.value })}
              className="w-full h-8 px-2 text-xs rounded-md border border-input bg-background text-foreground"
            >
              <option value="ALL">Barchasi</option>
              <option value="SANDY">Qumloq (Sandy)</option>
              <option value="LOAM">Qumloq-soz (Loam)</option>
              <option value="CLAY">Soz (Clay)</option>
              <option value="SALINE">Shoʻrlangan (Saline)</option>
              <option value="SILT">Gilli (Silt)</option>
            </select>
          </div>

          {/* Irrigation Method */}
          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-muted-foreground block">
              {t.fields?.table?.irrigation || 'Sugʻorish usuli'}
            </label>
            <select
              value={filters.irrigationMethod}
              onChange={(e) => onChange({ ...filters, irrigationMethod: e.target.value })}
              className="w-full h-8 px-2 text-xs rounded-md border border-input bg-background text-foreground"
            >
              <option value="ALL">Barchasi</option>
              <option value="CANAL">Kanal (Canal)</option>
              <option value="DITCH">Ariq (Ditch)</option>
              <option value="FURROW">Egatlardan (Furrow)</option>
              <option value="DRIP">Tomchilatib (Drip)</option>
              <option value="SPRINKLER">Yomgʻirlatib (Sprinkler)</option>
              <option value="OTHER">Boshqa (Other)</option>
            </select>
          </div>

          {/* Water Source Status */}
          <div className="space-y-1">
            <label className="text-[11px] font-semibold text-muted-foreground block">
              {t.gis?.filters?.sourceStatus || 'Manba holati'}
            </label>
            <select
              value={filters.waterSourceStatus}
              onChange={(e) =>
                onChange({
                  ...filters,
                  waterSourceStatus: e.target.value as GisFilterState['waterSourceStatus'],
                })
              }
              className="w-full h-8 px-2 text-xs rounded-md border border-input bg-background text-foreground"
            >
              <option value="ALL">Barchasi</option>
              <option value="ACTIVE">Faol (Active)</option>
              <option value="INACTIVE">Nofaol (Inactive)</option>
            </select>
          </div>
        </div>
      )}
    </div>
  )
}
