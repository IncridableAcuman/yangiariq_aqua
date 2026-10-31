import React from 'react'
import { Filter, X, ArrowUpDown } from 'lucide-react'
import { Button } from '@/shared/ui/button'
import { SupportedLanguage, translations } from '@/shared/i18n'
import {
  CropType,
  SoilType,
  IrrigationMethod,
  FieldStatus,
  FieldQueryParams,
} from '@/entities/field'

interface FieldFiltersProps {
  lang: SupportedLanguage
  filters: FieldQueryParams
  onFilterChange: (filters: Partial<FieldQueryParams>) => void
  onResetFilters: () => void
}

export const FieldFilters: React.FC<FieldFiltersProps> = ({
  lang,
  filters,
  onFilterChange,
  onResetFilters,
}) => {
  const t = translations[lang]?.fields || translations.uz.fields

  const hasActiveFilters = Boolean(
    filters.status ||
    filters.cropType ||
    filters.soilType ||
    filters.irrigationMethod ||
    (filters.sort && filters.sort !== 'createdAt,desc')
  )

  const selectClassName =
    'h-9 rounded-md border border-input bg-background px-2.5 py-1 text-xs focus:outline-none focus:ring-1 focus:ring-ring transition-colors shadow-2xs font-medium text-foreground'

  return (
    <div className="flex flex-wrap items-center gap-2 pt-1 pb-1">
      <div className="flex items-center gap-1.5 text-xs text-muted-foreground mr-1 select-none">
        <Filter className="h-3.5 w-3.5" />
        <span className="hidden sm:inline font-medium">Filtrlar:</span>
      </div>

      {/* Status Filter */}
      <select
        className={selectClassName}
        value={filters.status || 'ALL'}
        onChange={(e) =>
          onFilterChange({
            status: e.target.value === 'ALL' ? undefined : (e.target.value as FieldStatus),
          })
        }
        aria-label={t.filter.allStatus}
      >
        <option value="ALL">{t.filter.allStatus}</option>
        <option value="ACTIVE">{t.status.ACTIVE}</option>
        <option value="INACTIVE">{t.status.INACTIVE}</option>
      </select>

      {/* Crop Filter */}
      <select
        className={selectClassName}
        value={filters.cropType || 'ALL'}
        onChange={(e) =>
          onFilterChange({
            cropType: e.target.value === 'ALL' ? undefined : (e.target.value as CropType),
          })
        }
        aria-label={t.filter.allCrops}
      >
        <option value="ALL">{t.filter.allCrops}</option>
        <option value="COTTON">{t.crops.COTTON}</option>
        <option value="WHEAT">{t.crops.WHEAT}</option>
        <option value="RICE">{t.crops.RICE}</option>
        <option value="MAIZE">{t.crops.MAIZE}</option>
        <option value="VEGETABLE">{t.crops.VEGETABLE}</option>
        <option value="MELON">{t.crops.MELON}</option>
        <option value="OTHER">{t.crops.OTHER}</option>
      </select>

      {/* Soil Filter */}
      <select
        className={selectClassName}
        value={filters.soilType || 'ALL'}
        onChange={(e) =>
          onFilterChange({
            soilType: e.target.value === 'ALL' ? undefined : (e.target.value as SoilType),
          })
        }
        aria-label={t.filter.allSoils}
      >
        <option value="ALL">{t.filter.allSoils}</option>
        <option value="SANDY">{t.soils.SANDY}</option>
        <option value="LOAM">{t.soils.LOAM}</option>
        <option value="CLAY">{t.soils.CLAY}</option>
        <option value="SANDY_LOAM">{t.soils.SANDY_LOAM}</option>
        <option value="CLAY_LOAM">{t.soils.CLAY_LOAM}</option>
        <option value="OTHER">{t.soils.OTHER}</option>
      </select>

      {/* Irrigation Method Filter */}
      <select
        className={selectClassName}
        value={filters.irrigationMethod || 'ALL'}
        onChange={(e) =>
          onFilterChange({
            irrigationMethod:
              e.target.value === 'ALL' ? undefined : (e.target.value as IrrigationMethod),
          })
        }
        aria-label={t.filter.allIrrigation}
      >
        <option value="ALL">{t.filter.allIrrigation}</option>
        <option value="CANAL">{t.irrigations.CANAL}</option>
        <option value="DITCH">{t.irrigations.DITCH}</option>
        <option value="FURROW">{t.irrigations.FURROW}</option>
        <option value="DRIP">{t.irrigations.DRIP}</option>
        <option value="SPRINKLER">{t.irrigations.SPRINKLER}</option>
        <option value="OTHER">{t.irrigations.OTHER}</option>
      </select>

      {/* Sorting Dropdown */}
      <div className="flex items-center ml-auto">
        <ArrowUpDown className="h-3 w-3 text-muted-foreground mr-1.5" />
        <select
          className={selectClassName}
          value={filters.sort || 'createdAt,desc'}
          onChange={(e) => onFilterChange({ sort: e.target.value })}
          aria-label={t.filter.sortBy}
        >
          <option value="createdAt,desc">{t.filter.sortCreatedAtDesc}</option>
          <option value="createdAt,asc">{t.filter.sortCreatedAtAsc}</option>
          <option value="name,asc">{t.filter.sortNameAsc}</option>
          <option value="name,desc">{t.filter.sortNameDesc}</option>
          <option value="areaHectares,desc">{t.filter.sortAreaDesc}</option>
          <option value="areaHectares,asc">{t.filter.sortAreaAsc}</option>
        </select>
      </div>

      {/* Reset Filters Button */}
      {hasActiveFilters && (
        <Button
          variant="ghost"
          size="sm"
          onClick={onResetFilters}
          className="h-9 px-2 text-xs text-muted-foreground hover:text-foreground gap-1"
        >
          <X className="h-3.5 w-3.5" />
          <span>{t.filter.clearFilters}</span>
        </Button>
      )}
    </div>
  )
}
