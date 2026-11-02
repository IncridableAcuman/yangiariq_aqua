import React, { useState } from 'react'
import {
  Search,
  Filter,
  CheckCircle2,
  AlertTriangle,
  XCircle,
  ChevronLeft,
  ChevronRight,
  ExternalLink,
  HelpCircle,
} from 'lucide-react'
import { FieldPerformanceDto, FieldPerformanceStatus } from '@/entities/analytics'
import { SupportedLanguage } from '@/shared/i18n'

interface FieldPerformanceTableProps {
  fields: FieldPerformanceDto[]
  totalElements: number
  page: number
  size: number
  totalPages: number
  onPageChange: (newPage: number) => void
  onSortChange: (sortBy: string, direction: 'asc' | 'desc') => void
  currentSortBy: string
  currentSortDir: 'asc' | 'desc'
  lang: SupportedLanguage
  onNavigateToOperations?: (fieldId: string) => void
}

export const FieldPerformanceTable: React.FC<FieldPerformanceTableProps> = ({
  fields,
  totalElements,
  page,
  size,
  totalPages,
  onPageChange,
  onSortChange,
  currentSortBy,
  currentSortDir,
  lang,
  onNavigateToOperations,
}) => {
  const [searchTerm, setSearchTerm] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('ALL')
  const [selectedScoreField, setSelectedScoreField] = useState<FieldPerformanceDto | null>(null)

  const t = {
    uz: {
      title: 'Dalalar boʻyicha samaradorlik reytingi',
      subtitle: 'Har bir dala boʻyicha suv sarfi, reja intizomi va xavfsizlik koʻrsatkichlari',
      searchPlaceholder: 'Dala nomini qidirish...',
      statusAll: 'Barcha holatlar',
      normal: 'NORMAL (Meʼyorda)',
      warning: 'WARNING (Diqqat)',
      attention: 'ATTENTION (Xavf/Kritik)',
      colField: 'Dala va ekin',
      colArea: 'Maydon',
      colWater: 'Suv sarfi / Kvota',
      colAdherence: 'Reja intizomi',
      colTelemetry: 'Telemetriya',
      colSafety: 'Xavfsizlik',
      colScore: 'Samaradorlik indeksi',
      colActions: 'Amallar',
      noFields: 'Mos keluvchi dalalar topilmadi.',
      scoreBreakdownTitle: 'Samaradorlik indeksi hisob-kitobi',
      waterCompliance: 'Suv kvotasiga rioya qilish (25%)',
      planAdherence: 'Reja bajarilishi intizomi (25%)',
      telemetryReliability: 'Telemetriya ishonchliligi (20%)',
      safetyRecord: 'Gidravlik xavfsizlik rekordi (20%)',
      verificationCoverage: 'Natija tekshiruvi qamrovi (10%)',
      close: 'Yopish',
      openOps: 'Operatsiyalar',
      pageInfo: (p: number, _tp: number, tot: number) => `${tot} ta daladan ${p * size + 1}–${Math.min((p + 1) * size, tot)} koʻrsatilmoqda`,
    },
    ru: {
      title: 'Рейтинг эффективности по полям',
      subtitle: 'Расход воды, дисциплина планов и безопасность по каждому участку',
      searchPlaceholder: 'Поиск по названию поля...',
      statusAll: 'Все статусы',
      normal: 'NORMAL (Норма)',
      warning: 'WARNING (Внимание)',
      attention: 'ATTENTION (Критично/Опасность)',
      colField: 'Поле и культура',
      colArea: 'Площадь',
      colWater: 'Расход / Квота',
      colAdherence: 'Дисциплина планов',
      colTelemetry: 'Телеметрия',
      colSafety: 'Безопасность',
      colScore: 'Индекс эффективности',
      colActions: 'Действия',
      noFields: 'Поля не найдены.',
      scoreBreakdownTitle: 'Расчет индекса эффективности',
      waterCompliance: 'Соблюдение лимита воды (25%)',
      planAdherence: 'Дисциплина выполнения планов (25%)',
      telemetryReliability: 'Достоверность телеметрии (20%)',
      safetyRecord: 'Гидравлическая безопасность (20%)',
      verificationCoverage: 'Верификация результатов (10%)',
      close: 'Закрыть',
      openOps: 'Операции',
      pageInfo: (p: number, _tp: number, tot: number) => `Показано ${p * size + 1}–${Math.min((p + 1) * size, tot)} из ${tot} полей`,
    },
    en: {
      title: 'Field Performance & Ranking Table',
      subtitle: 'Parcel-level water utilization, adherence, and hydraulic safety records',
      searchPlaceholder: 'Search field name...',
      statusAll: 'All Statuses',
      normal: 'NORMAL (Compliant)',
      warning: 'WARNING (Watch)',
      attention: 'ATTENTION (Action Required)',
      colField: 'Field & Crop',
      colArea: 'Area',
      colWater: 'Water / Quota',
      colAdherence: 'Plan Adherence',
      colTelemetry: 'Telemetry',
      colSafety: 'Safety',
      colScore: 'Performance Score',
      colActions: 'Actions',
      noFields: 'No matching parcels found.',
      scoreBreakdownTitle: 'Explainable Score Breakdown',
      waterCompliance: 'Water Quota Compliance (25%)',
      planAdherence: 'Plan Adherence Factor (25%)',
      telemetryReliability: 'Telemetry Reliability (20%)',
      safetyRecord: 'Safety Record Factor (20%)',
      verificationCoverage: 'Verification Coverage (10%)',
      close: 'Close',
      openOps: 'Operations',
      pageInfo: (p: number, _tp: number, tot: number) => `Showing ${p * size + 1}–${Math.min((p + 1) * size, tot)} of ${tot} fields`,
    },
  }[lang]

  const filteredFields = fields.filter((f) => {
    const matchesSearch =
      searchTerm.trim() === '' ||
      f.fieldName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      f.cropType.toLowerCase().includes(searchTerm.toLowerCase())

    const matchesStatus =
      statusFilter === 'ALL' || f.performanceStatus === statusFilter

    return matchesSearch && matchesStatus
  })

  const getStatusBadge = (status: FieldPerformanceStatus) => {
    switch (status) {
      case 'NORMAL':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300">
            <CheckCircle2 className="w-3 h-3" />
            NORMAL
          </span>
        )
      case 'WARNING':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300">
            <AlertTriangle className="w-3 h-3" />
            WARNING
          </span>
        )
      case 'ATTENTION':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-semibold bg-rose-100 text-rose-800 dark:bg-rose-900/40 dark:text-rose-300">
            <XCircle className="w-3 h-3" />
            ATTENTION
          </span>
        )
    }
  }

  const handleHeaderClick = (colKey: string) => {
    if (currentSortBy === colKey) {
      onSortChange(colKey, currentSortDir === 'asc' ? 'desc' : 'asc')
    } else {
      onSortChange(colKey, 'desc')
    }
  }

  return (
    <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 shadow-sm space-y-4">
      {/* Header and Filters */}
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h3 className="text-base font-semibold text-slate-900 dark:text-white">{t.title}</h3>
          <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">{t.subtitle}</p>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          {/* Search */}
          <div className="relative">
            <Search className="w-3.5 h-3.5 text-slate-400 absolute left-2.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              placeholder={t.searchPlaceholder}
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-8 pr-3 py-1.5 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-200 text-xs focus:ring-1 focus:ring-blue-500 outline-none w-48 sm:w-56"
            />
          </div>

          {/* Status Filter */}
          <div className="flex items-center gap-1.5 text-xs">
            <Filter className="w-3.5 h-3.5 text-slate-400" />
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="px-2.5 py-1.5 rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-200 text-xs focus:ring-1 focus:ring-blue-500 outline-none"
            >
              <option value="ALL">{t.statusAll}</option>
              <option value="NORMAL">{t.normal}</option>
              <option value="WARNING">{t.warning}</option>
              <option value="ATTENTION">{t.attention}</option>
            </select>
          </div>
        </div>
      </div>

      {/* Table */}
      <div className="overflow-x-auto rounded-lg border border-slate-100 dark:border-slate-800">
        <table className="w-full text-left text-xs">
          <thead className="bg-slate-50 dark:bg-slate-800/60 text-slate-500 dark:text-slate-400 uppercase tracking-wider font-semibold border-b border-slate-200 dark:border-slate-800">
            <tr>
              <th
                onClick={() => handleHeaderClick('name')}
                className="py-3 px-4 cursor-pointer hover:text-slate-900 dark:hover:text-slate-200"
              >
                {t.colField}
              </th>
              <th
                onClick={() => handleHeaderClick('area')}
                className="py-3 px-3 cursor-pointer hover:text-slate-900 dark:hover:text-slate-200"
              >
                {t.colArea}
              </th>
              <th
                onClick={() => handleHeaderClick('utilization')}
                className="py-3 px-3 cursor-pointer hover:text-slate-900 dark:hover:text-slate-200"
              >
                {t.colWater}
              </th>
              <th
                onClick={() => handleHeaderClick('adherence')}
                className="py-3 px-3 cursor-pointer hover:text-slate-900 dark:hover:text-slate-200"
              >
                {t.colAdherence}
              </th>
              <th
                onClick={() => handleHeaderClick('telemetry')}
                className="py-3 px-3 cursor-pointer hover:text-slate-900 dark:hover:text-slate-200"
              >
                {t.colTelemetry}
              </th>
              <th
                onClick={() => handleHeaderClick('safety')}
                className="py-3 px-3 cursor-pointer hover:text-slate-900 dark:hover:text-slate-200"
              >
                {t.colSafety}
              </th>
              <th
                onClick={() => handleHeaderClick('score')}
                className="py-3 px-3 cursor-pointer hover:text-slate-900 dark:hover:text-slate-200"
              >
                {t.colScore}
              </th>
              <th className="py-3 px-3 text-right">{t.colActions}</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
            {filteredFields.length === 0 ? (
              <tr>
                <td colSpan={8} className="py-8 text-center text-slate-400">
                  {t.noFields}
                </td>
              </tr>
            ) : (
              filteredFields.map((f) => (
                <tr
                  key={f.fieldId}
                  className="hover:bg-slate-50 dark:hover:bg-slate-800/40 transition-colors"
                >
                  <td className="py-3 px-4 font-medium text-slate-900 dark:text-white">
                    <div className="flex items-center gap-2">
                      <span>{f.fieldName}</span>
                    </div>
                    <span className="text-[11px] text-slate-400">{f.cropType}</span>
                  </td>
                  <td className="py-3 px-3 text-slate-600 dark:text-slate-300">
                    {f.areaHectares} ga
                  </td>
                  <td className="py-3 px-3">
                    <div className="font-semibold text-slate-800 dark:text-slate-200">
                      {f.consumedM3.toLocaleString()} / {f.allocatedM3.toLocaleString()} m³
                    </div>
                    <span
                      className={`text-[11px] font-medium ${
                        f.utilizationPercent && f.utilizationPercent > 100
                          ? 'text-rose-500'
                          : f.utilizationPercent && f.utilizationPercent > 80
                          ? 'text-amber-500'
                          : 'text-emerald-500'
                      }`}
                    >
                      {f.utilizationPercent !== null ? `${f.utilizationPercent}% limit` : '—'}
                    </span>
                  </td>
                  <td className="py-3 px-3 text-slate-600 dark:text-slate-300">
                    <div className="font-semibold">
                      {f.planAdherencePercent !== null ? `${f.planAdherencePercent}%` : '—'}
                    </div>
                    <span className="text-[11px] text-slate-400">
                      {f.completedOperationsCount}/{f.plansCount} amal
                    </span>
                  </td>
                  <td className="py-3 px-3 text-slate-600 dark:text-slate-300">
                    <div className="font-semibold">
                      {f.telemetryCoveragePercent !== null ? `${f.telemetryCoveragePercent}%` : '—'}
                    </div>
                    <span className="text-[11px] text-slate-400">{f.sensorHealth}</span>
                  </td>
                  <td className="py-3 px-3">
                    {f.criticalAnomalyCount > 0 ? (
                      <span className="inline-flex items-center gap-1 text-rose-600 font-semibold">
                        <AlertTriangle className="w-3.5 h-3.5" />
                        {f.criticalAnomalyCount} kritik
                      </span>
                    ) : f.safetyEventCount > 0 ? (
                      <span className="text-amber-600 font-medium">
                        {f.safetyEventCount} hodisa
                      </span>
                    ) : (
                      <span className="text-emerald-600">0 hodisa</span>
                    )}
                  </td>
                  <td className="py-3 px-3">
                    <div className="flex items-center gap-2">
                      <button
                        onClick={() => setSelectedScoreField(f)}
                        className="font-bold text-sm text-blue-600 dark:text-blue-400 hover:underline inline-flex items-center gap-1"
                        title="Bal tafsilotini koʻrish"
                      >
                        {f.performanceScore} / 100
                        <HelpCircle className="w-3 h-3 text-slate-400" />
                      </button>
                      {getStatusBadge(f.performanceStatus)}
                    </div>
                  </td>
                  <td className="py-3 px-3 text-right">
                    {onNavigateToOperations && (
                      <button
                        onClick={() => onNavigateToOperations(f.fieldId)}
                        className="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-medium text-blue-600 hover:text-blue-700 dark:text-blue-400 dark:hover:text-blue-300 bg-blue-50 hover:bg-blue-100 dark:bg-blue-950/40 dark:hover:bg-blue-900/40 rounded transition-colors"
                      >
                        <span>{t.openOps}</span>
                        <ExternalLink className="w-3 h-3" />
                      </button>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Pagination Controls */}
      <div className="flex flex-wrap items-center justify-between gap-3 pt-2 text-xs text-slate-500 dark:text-slate-400">
        <span>{t.pageInfo(page, totalPages, totalElements)}</span>
        <div className="flex items-center gap-2">
          <button
            onClick={() => onPageChange(page - 1)}
            disabled={page <= 0}
            className="p-1.5 rounded border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 disabled:opacity-40 hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors"
          >
            <ChevronLeft className="w-4 h-4" />
          </button>
          <span className="font-medium text-slate-700 dark:text-slate-300">
            {page + 1} / {Math.max(1, totalPages)}
          </span>
          <button
            onClick={() => onPageChange(page + 1)}
            disabled={page >= totalPages - 1}
            className="p-1.5 rounded border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 disabled:opacity-40 hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors"
          >
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Explainable Score Modal / Popover */}
      {selectedScoreField && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm animate-fade-in">
          <div className="bg-white dark:bg-slate-900 rounded-xl max-w-md w-full p-6 shadow-xl border border-slate-200 dark:border-slate-800 space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h4 className="text-base font-bold text-slate-900 dark:text-white">
                  {selectedScoreField.fieldName}
                </h4>
                <p className="text-xs text-slate-500">{t.scoreBreakdownTitle}</p>
              </div>
              <div className="text-right">
                <span className="text-2xl font-extrabold text-blue-600 dark:text-blue-400">
                  {selectedScoreField.performanceScore}
                </span>
                <span className="text-xs text-slate-400"> / 100</span>
              </div>
            </div>

            <div className="space-y-3 pt-2 text-xs">
              <div className="flex items-center justify-between p-2 rounded bg-slate-50 dark:bg-slate-800/60">
                <span className="text-slate-600 dark:text-slate-400">{t.waterCompliance}</span>
                <span className="font-bold text-slate-800 dark:text-slate-200">
                  {selectedScoreField.scoreBreakdown?.waterCompliance ?? '—'} / 100
                </span>
              </div>
              <div className="flex items-center justify-between p-2 rounded bg-slate-50 dark:bg-slate-800/60">
                <span className="text-slate-600 dark:text-slate-400">{t.planAdherence}</span>
                <span className="font-bold text-slate-800 dark:text-slate-200">
                  {selectedScoreField.scoreBreakdown?.planAdherence ?? '—'} / 100
                </span>
              </div>
              <div className="flex items-center justify-between p-2 rounded bg-slate-50 dark:bg-slate-800/60">
                <span className="text-slate-600 dark:text-slate-400">{t.telemetryReliability}</span>
                <span className="font-bold text-slate-800 dark:text-slate-200">
                  {selectedScoreField.scoreBreakdown?.telemetryReliability ?? '—'} / 100
                </span>
              </div>
              <div className="flex items-center justify-between p-2 rounded bg-slate-50 dark:bg-slate-800/60">
                <span className="text-slate-600 dark:text-slate-400">{t.safetyRecord}</span>
                <span className="font-bold text-slate-800 dark:text-slate-200">
                  {selectedScoreField.scoreBreakdown?.safetyRecord ?? '—'} / 100
                </span>
              </div>
              <div className="flex items-center justify-between p-2 rounded bg-slate-50 dark:bg-slate-800/60">
                <span className="text-slate-600 dark:text-slate-400">{t.verificationCoverage}</span>
                <span className="font-bold text-slate-800 dark:text-slate-200">
                  {selectedScoreField.scoreBreakdown?.verificationCoverage ?? '—'} / 100
                </span>
              </div>
            </div>

            {selectedScoreField.criticalAnomalyCount > 0 && (
              <div className="p-3 bg-rose-50 dark:bg-rose-950/30 border border-rose-200 dark:border-rose-900 rounded-lg text-xs text-rose-700 dark:text-rose-300 flex items-start gap-2">
                <AlertTriangle className="w-4 h-4 text-rose-600 shrink-0 mt-0.5" />
                <p>
                  <strong>Xavfsizlik ustuvorligi (Safety Dominance):</strong> Kritik anomaliya mavjudligi sababli ushbu dalaning holati majburiy ravishda ATTENTION etib belgilangan.
                </p>
              </div>
            )}

            <div className="flex justify-end pt-2">
              <button
                onClick={() => setSelectedScoreField(null)}
                className="px-4 py-1.5 text-xs font-medium bg-slate-200 hover:bg-slate-300 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-800 dark:text-slate-200 rounded-lg transition-colors"
              >
                {t.close}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
