import React from 'react'
import { Calendar, Filter, RefreshCw } from 'lucide-react'
import { PerformancePeriodType } from '@/entities/analytics'
import { SupportedLanguage } from '@/shared/i18n'

interface PeriodFilterBarProps {
  lang: SupportedLanguage
  selectedPeriod: PerformancePeriodType
  onSelectPeriod: (period: PerformancePeriodType) => void
  dateRangeLabel?: string
  fields: Array<{ id: string; name: string }>
  selectedFieldId: string | null
  onSelectField: (fieldId: string | null) => void
  selectedCrop: string | null
  onSelectCrop: (crop: string | null) => void
  customFrom: string
  customTo: string
  onCustomDatesChange: (from: string, to: string) => void
  onRefresh: () => void
  isFetching?: boolean
}

export const PeriodFilterBar: React.FC<PeriodFilterBarProps> = ({
  lang,
  selectedPeriod,
  onSelectPeriod,
  dateRangeLabel,
  fields,
  selectedFieldId,
  onSelectField,
  selectedCrop,
  onSelectCrop,
  customFrom,
  customTo,
  onCustomDatesChange,
  onRefresh,
  isFetching,
}) => {
  const t = {
    uz: {
      today: 'Bugun',
      last7Days: '7 kun',
      last30Days: '30 kun',
      last90Days: '90 kun',
      season: 'Mavsum',
      custom: 'Boshqa davr',
      allFields: 'Barcha dalalar',
      allCrops: 'Barcha ekinlar',
      cotton: 'Paxta',
      wheat: 'Bugʻdoy',
      rice: 'Sholi',
      maize: 'Makkajoʻxori',
      vegetable: 'Sabzavot',
      melon: 'Poliz',
      filterField: 'Dala boʻyicha:',
      filterCrop: 'Ekin turi:',
      refresh: 'Yangilash',
      from: 'Dan:',
      to: 'Gacha:',
    },
    ru: {
      today: 'Сегодня',
      last7Days: '7 дней',
      last30Days: '30 дней',
      last90Days: '90 дней',
      season: 'Сезон',
      custom: 'Другой период',
      allFields: 'Все поля',
      allCrops: 'Все культуры',
      cotton: 'Хлопок',
      wheat: 'Пшеница',
      rice: 'Рис',
      maize: 'Кукуруза',
      vegetable: 'Овощи',
      melon: 'Бахчевые',
      filterField: 'По полю:',
      filterCrop: 'Культура:',
      refresh: 'Обновить',
      from: 'С:',
      to: 'По:',
    },
    en: {
      today: 'Today',
      last7Days: '7 Days',
      last30Days: '30 Days',
      last90Days: '90 Days',
      season: 'Season',
      custom: 'Custom',
      allFields: 'All Fields',
      allCrops: 'All Crops',
      cotton: 'Cotton',
      wheat: 'Wheat',
      rice: 'Rice',
      maize: 'Maize',
      vegetable: 'Vegetables',
      melon: 'Melons',
      filterField: 'Filter by field:',
      filterCrop: 'Crop type:',
      refresh: 'Refresh',
      from: 'From:',
      to: 'To:',
    },
  }[lang]

  const periodOptions: Array<{ type: PerformancePeriodType; label: string }> = [
    { type: 'TODAY', label: t.today },
    { type: 'LAST_7_DAYS', label: t.last7Days },
    { type: 'LAST_30_DAYS', label: t.last30Days },
    { type: 'LAST_90_DAYS', label: t.last90Days },
    { type: 'SEASON', label: t.season },
    { type: 'CUSTOM', label: t.custom },
  ]

  return (
    <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-4 shadow-sm space-y-4">
      {/* Top Row: Period Presets & Refresh */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-1.5">
          {periodOptions.map((opt) => {
            const isActive = selectedPeriod === opt.type
            return (
              <button
                key={opt.type}
                onClick={() => onSelectPeriod(opt.type)}
                className={`px-3 py-1.5 text-xs font-medium rounded-lg transition-colors ${
                  isActive
                    ? 'bg-blue-600 text-white shadow-sm'
                    : 'bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300'
                }`}
              >
                {opt.label}
              </button>
            )
          })}
        </div>

        <div className="flex items-center gap-3">
          {dateRangeLabel && (
            <div className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 rounded-lg border border-slate-200 dark:border-slate-700">
              <Calendar className="w-3.5 h-3.5 text-blue-600 dark:text-blue-400" />
              <span>{dateRangeLabel}</span>
            </div>
          )}

          <button
            onClick={onRefresh}
            disabled={isFetching}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 rounded-lg transition-colors disabled:opacity-50"
            title={t.refresh}
          >
            <RefreshCw className={`w-3.5 h-3.5 ${isFetching ? 'animate-spin text-blue-600' : ''}`} />
            <span className="hidden sm:inline">{t.refresh}</span>
          </button>
        </div>
      </div>

      {/* Custom Date Range Inputs if CUSTOM selected */}
      {selectedPeriod === 'CUSTOM' && (
        <div className="flex flex-wrap items-center gap-3 pt-2 border-t border-slate-100 dark:border-slate-800 text-xs">
          <div className="flex items-center gap-2">
            <span className="text-slate-500 dark:text-slate-400">{t.from}</span>
            <input
              type="date"
              value={customFrom}
              onChange={(e) => onCustomDatesChange(e.target.value, customTo)}
              className="px-2.5 py-1 rounded border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-200 text-xs focus:ring-1 focus:ring-blue-500 outline-none"
            />
          </div>
          <div className="flex items-center gap-2">
            <span className="text-slate-500 dark:text-slate-400">{t.to}</span>
            <input
              type="date"
              value={customTo}
              onChange={(e) => onCustomDatesChange(customFrom, e.target.value)}
              className="px-2.5 py-1 rounded border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-200 text-xs focus:ring-1 focus:ring-blue-500 outline-none"
            />
          </div>
        </div>
      )}

      {/* Bottom Row: Parcel and Crop Filters */}
      <div className="flex flex-wrap items-center gap-4 pt-2 border-t border-slate-100 dark:border-slate-800">
        <div className="flex items-center gap-2 text-xs">
          <Filter className="w-3.5 h-3.5 text-slate-400" />
          <span className="text-slate-500 dark:text-slate-400">{t.filterField}</span>
          <select
            value={selectedFieldId || ''}
            onChange={(e) => onSelectField(e.target.value ? e.target.value : null)}
            className="px-2.5 py-1 rounded border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-200 text-xs focus:ring-1 focus:ring-blue-500 outline-none"
          >
            <option value="">{t.allFields}</option>
            {fields.map((f) => (
              <option key={f.id} value={f.id}>
                {f.name}
              </option>
            ))}
          </select>
        </div>

        <div className="flex items-center gap-2 text-xs">
          <span className="text-slate-500 dark:text-slate-400">{t.filterCrop}</span>
          <select
            value={selectedCrop || ''}
            onChange={(e) => onSelectCrop(e.target.value ? e.target.value : null)}
            className="px-2.5 py-1 rounded border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-200 text-xs focus:ring-1 focus:ring-blue-500 outline-none"
          >
            <option value="">{t.allCrops}</option>
            <option value="COTTON">{t.cotton}</option>
            <option value="WHEAT">{t.wheat}</option>
            <option value="RICE">{t.rice}</option>
            <option value="MAIZE">{t.maize}</option>
            <option value="VEGETABLE">{t.vegetable}</option>
            <option value="MELON">{t.melon}</option>
          </select>
        </div>
      </div>
    </div>
  )
}
