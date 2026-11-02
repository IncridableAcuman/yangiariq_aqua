import React from 'react'
import {
  Droplet,
  CalendarCheck,
  Activity,
  ShieldCheck,
  AlertOctagon,
  Layers,
  CheckCircle2,
  XCircle,
} from 'lucide-react'
import {
  FarmOverviewMetricsDto,
  WaterPerformanceDto,
  IrrigationPerformanceDto,
  TelemetryQualityDto,
  VerificationPerformanceDto,
  SafetyEventMetricsDto,
} from '@/entities/analytics'
import { SupportedLanguage } from '@/shared/i18n'

interface FarmPerformanceKpisProps {
  lang: SupportedLanguage
  farmOverview?: FarmOverviewMetricsDto
  water?: WaterPerformanceDto
  irrigation?: IrrigationPerformanceDto
  telemetry?: TelemetryQualityDto
  verification?: VerificationPerformanceDto
  safety?: SafetyEventMetricsDto
}

export const FarmPerformanceKpis: React.FC<FarmPerformanceKpisProps> = ({
  lang,
  farmOverview,
  water,
  irrigation,
  telemetry,
  verification,
  safety,
}) => {
  const t = {
    uz: {
      farmOverviewTitle: 'Fermer xoʻjaligi umumiy koʻrsatkichlari (Farm Overview)',
      fields: 'Dalalar',
      activeFields: 'faol',
      area: 'Umumiy maydon',
      waterBalance: 'Suv hisobi',
      allocated: 'Ajratilgan',
      consumed: 'Sarflangan',
      remaining: 'Qolgan',
      operations: 'Sugʻorish amaliyotlari',
      completed: 'Bajarilgan',
      cancelled: 'Bekor qilingan',
      blocked: 'Bloklangan',
      sensors: 'Datchiklar qamrovi',
      healthy: 'Sogʻlom',
      stale: 'Eskirgan',
      offline: 'Oʻchiq',
      safetyEventsHeader: 'Xavfsizlik hodisalari',
      critSafety: 'Kritik',
      warnSafety: 'Ogohlantirish',
      waterUtil: 'Suv kvotasi sarfi',
      waterUtilHint: 'sarf / ajratilgan limit',
      planAdherence: 'Reja bajarilishi',
      planAdherenceHint: 'amallar / rejalashtirilgan',
      telemetryQuality: 'Telemetriya sifati',
      telemetryQualityHint: 'yaroqli oʻlchovlar',
      verificationCov: 'Natija tekshiruvi',
      verificationCovHint: 'tasdiqlangan amallar',
      safetyEvents: 'Xavfsizlik hodisalari',
      safetyEventsHint: 'kritik anomaliyalar',
      noData: 'Mavjud emas',
      normal: 'Meʼyorda',
      warning: 'Diqqat',
      critical: 'Kritik',
      overConsumed: 'Limit oshgan',
      activeSensors: 'ta faol datchik',
      verifiedOps: 'ta tekshirilgan',
    },
    ru: {
      farmOverviewTitle: 'Общий обзор хозяйства (Farm Overview)',
      fields: 'Поля',
      activeFields: 'активных',
      area: 'Общая площадь',
      waterBalance: 'Водный баланс',
      allocated: 'Утверждено',
      consumed: 'Израсходовано',
      remaining: 'Остаток',
      operations: 'Операции полива',
      completed: 'Выполнено',
      cancelled: 'Отменено',
      blocked: 'Заблокировано',
      sensors: 'Охват датчиков',
      healthy: 'Исправны',
      stale: 'Нестабильны',
      offline: 'Офлайн',
      safetyEventsHeader: 'Инциденты безопасности',
      critSafety: 'Критические',
      warnSafety: 'Предупреждения',
      waterUtil: 'Использование квоты воды',
      waterUtilHint: 'факт / утвержденный лимит',
      planAdherence: 'Соблюдение планов',
      planAdherenceHint: 'операции / планы',
      telemetryQuality: 'Качество телеметрии',
      telemetryQualityHint: 'достоверные замеры',
      verificationCov: 'Верификация результатов',
      verificationCovHint: 'подтвержденные поливы',
      safetyEvents: 'Инциденты безопасности',
      safetyEventsHint: 'критические аномалии',
      noData: 'Нет данных',
      normal: 'Норма',
      warning: 'Внимание',
      critical: 'Критично',
      overConsumed: 'Лимит превышен',
      activeSensors: 'активных датчиков',
      verifiedOps: 'верифицировано',
    },
    en: {
      farmOverviewTitle: 'Farm Overview',
      fields: 'Fields',
      activeFields: 'active',
      area: 'Total Area',
      waterBalance: 'Water Quota',
      allocated: 'Allocated',
      consumed: 'Consumed',
      remaining: 'Remaining',
      operations: 'Irrigation Operations',
      completed: 'Completed',
      cancelled: 'Cancelled',
      blocked: 'Blocked',
      sensors: 'Sensor Coverage',
      healthy: 'Healthy',
      stale: 'Stale',
      offline: 'Offline',
      safetyEventsHeader: 'Safety Events',
      critSafety: 'Critical',
      warnSafety: 'Warning',
      waterUtil: 'Water Quota Utilization',
      waterUtilHint: 'consumed / allocated limit',
      planAdherence: 'Plan Adherence',
      planAdherenceHint: 'operations / planned',
      telemetryQuality: 'Telemetry Quality',
      telemetryQualityHint: 'valid measurements',
      verificationCov: 'Verification Coverage',
      verificationCovHint: 'confirmed operations',
      safetyEvents: 'Safety Incidents',
      safetyEventsHint: 'critical anomalies',
      noData: 'N/A',
      normal: 'Normal',
      warning: 'Warning',
      critical: 'Critical',
      overConsumed: 'Over-consumed',
      activeSensors: 'active sensors',
      verifiedOps: 'verified',
    },
  }[lang]

  // Extract resilient KPI metrics
  const waterUtilVal = water?.utilizationPercent
  const planAdherenceVal = irrigation?.planAdherencePercent
  const telemetryVal = telemetry?.validReadingPercent ?? telemetry?.telemetryCoveragePercent
  const onlineSensorsCount = telemetry?.onlineSensors ?? telemetry?.activeSensors ?? 0
  const totalSensorsCount = telemetry?.totalSensors ?? 0
  const verificationVal = verification?.verificationCoveragePercent
  const verifiedOpsCount = verification?.verifiedOperationsCount ?? 0
  const criticalAnomaliesCount = safety?.criticalAnomaliesCount ?? safety?.criticalCount ?? 0
  const totalSafetyEventsCount = safety?.totalAnomaliesDetected ?? safety?.totalEvents ?? 0
  const activeBlockedCount = safety?.activeHydraulicBlocksCount ?? safety?.activeBlockedParcelsCount ?? 0

  const getWaterBadge = () => {
    if (waterUtilVal === null || waterUtilVal === undefined) return null
    if (waterUtilVal > 100) {
      return (
        <span className="px-2 py-0.5 rounded text-[11px] font-semibold bg-rose-100 text-rose-800 dark:bg-rose-900/40 dark:text-rose-300">
          {t.overConsumed}
        </span>
      )
    }
    if (waterUtilVal > 80) {
      return (
        <span className="px-2 py-0.5 rounded text-[11px] font-semibold bg-amber-100 text-amber-800 dark:bg-amber-900/40 dark:text-amber-300">
          {t.warning}
        </span>
      )
    }
    return (
      <span className="px-2 py-0.5 rounded text-[11px] font-semibold bg-emerald-100 text-emerald-800 dark:bg-emerald-900/40 dark:text-emerald-300">
        {t.normal}
      </span>
    )
  }

  return (
    <div className="space-y-4">
      {/* 0. Farm Overview Strip (Explicit Farm Assets & Operational Infrastructure) */}
      {farmOverview && (
        <div className="bg-slate-50 dark:bg-slate-900/60 border border-slate-200/80 dark:border-slate-800 rounded-xl p-4 shadow-xs">
          <div className="flex items-center gap-2 mb-3">
            <Layers className="w-4 h-4 text-blue-600 dark:text-blue-400" />
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-700 dark:text-slate-300">
              {t.farmOverviewTitle}
            </h4>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3 text-xs">
            {/* Fields Asset */}
            <div className="p-2.5 bg-white dark:bg-slate-800 rounded-lg border border-slate-200/60 dark:border-slate-700/60">
              <span className="text-[11px] text-slate-500 dark:text-slate-400">{t.fields}</span>
              <div className="text-sm font-bold text-slate-900 dark:text-white mt-0.5">
                {farmOverview.activeFields} / {farmOverview.totalFields} {t.activeFields}
              </div>
              <span className="text-[11px] text-slate-400">
                {farmOverview.totalAreaHectares} ga
              </span>
            </div>

            {/* Water Quota Overview */}
            <div className="p-2.5 bg-white dark:bg-slate-800 rounded-lg border border-slate-200/60 dark:border-slate-700/60">
              <span className="text-[11px] text-slate-500 dark:text-slate-400">{t.waterBalance}</span>
              <div className="text-sm font-bold text-slate-900 dark:text-white mt-0.5">
                {farmOverview.totalConsumedM3 ? farmOverview.totalConsumedM3.toLocaleString() : 0} m³
              </div>
              <span className="text-[11px] text-slate-400">
                {t.allocated}: {farmOverview.totalAllocatedM3 ? farmOverview.totalAllocatedM3.toLocaleString() : 0} m³
              </span>
            </div>

            {/* Irrigation Operations Overview */}
            <div className="p-2.5 bg-white dark:bg-slate-800 rounded-lg border border-slate-200/60 dark:border-slate-700/60">
              <span className="text-[11px] text-slate-500 dark:text-slate-400">{t.operations}</span>
              <div className="text-sm font-bold text-emerald-600 dark:text-emerald-400 mt-0.5 flex items-center gap-1">
                <CheckCircle2 className="w-3.5 h-3.5" />
                <span>{farmOverview.totalCompletedOperations ?? 0} {t.completed.toLowerCase()}</span>
              </div>
              <span className="text-[11px] text-slate-400">
                {farmOverview.totalCancelledOperations ?? 0} bekor • {farmOverview.totalBlockedOperations ?? 0} blok
              </span>
            </div>

            {/* Sensor Coverage Overview */}
            <div className="p-2.5 bg-white dark:bg-slate-800 rounded-lg border border-slate-200/60 dark:border-slate-700/60">
              <span className="text-[11px] text-slate-500 dark:text-slate-400">{t.sensors}</span>
              <div className="text-sm font-bold text-slate-900 dark:text-white mt-0.5">
                {farmOverview.healthySensors ?? 0} / {farmOverview.totalSensors ?? 0} {t.healthy.toLowerCase()}
              </div>
              <span className="text-[11px] text-slate-400">
                {farmOverview.staleSensors ?? 0} eskirgan • {farmOverview.offlineSensors ?? 0} oʻchiq
              </span>
            </div>

            {/* Safety Events Overview */}
            <div className="p-2.5 bg-white dark:bg-slate-800 rounded-lg border border-slate-200/60 dark:border-slate-700/60">
              <span className="text-[11px] text-slate-500 dark:text-slate-400">{t.safetyEventsHeader}</span>
              <div className="text-sm font-bold mt-0.5 flex items-center gap-1 text-slate-900 dark:text-white">
                {(farmOverview.criticalSafetyEvents ?? 0) > 0 ? (
                  <span className="text-rose-600 dark:text-rose-400 font-bold flex items-center gap-1">
                    <XCircle className="w-3.5 h-3.5" />
                    {farmOverview.criticalSafetyEvents} {t.critSafety}
                  </span>
                ) : (
                  <span className="text-emerald-600 dark:text-emerald-400 flex items-center gap-1">
                    <CheckCircle2 className="w-3.5 h-3.5" />
                    0 {t.critSafety}
                  </span>
                )}
              </div>
              <span className="text-[11px] text-slate-400">
                {farmOverview.warningSafetyEvents ?? 0} {t.warnSafety.toLowerCase()}
              </span>
            </div>
          </div>
        </div>
      )}

      {/* 5 Core Performance KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
        {/* 1. Water Utilization */}
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-4 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 mb-2">
            <span className="text-xs font-medium">{t.waterUtil}</span>
            <Droplet className="w-4 h-4 text-blue-500" />
          </div>
          <div>
            <div className="flex items-baseline justify-between">
              <span className="text-2xl font-bold text-slate-900 dark:text-white">
                {waterUtilVal !== null && waterUtilVal !== undefined ? `${waterUtilVal}%` : t.noData}
              </span>
              {getWaterBadge()}
            </div>
            <p className="text-[11px] text-slate-400 mt-1">
              {water?.totalConsumedM3?.toLocaleString() ?? 0} / {water?.totalAllocatedM3?.toLocaleString() ?? 0} m³
            </p>
          </div>
        </div>

        {/* 2. Plan Adherence */}
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-4 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 mb-2">
            <span className="text-xs font-medium">{t.planAdherence}</span>
            <CalendarCheck className="w-4 h-4 text-emerald-500" />
          </div>
          <div>
            <div className="flex items-baseline justify-between">
              <span className="text-2xl font-bold text-slate-900 dark:text-white">
                {planAdherenceVal !== null && planAdherenceVal !== undefined
                  ? `${planAdherenceVal}%`
                  : t.noData}
              </span>
            </div>
            <p className="text-[11px] text-slate-400 mt-1">
              {irrigation?.totalOperationsCompleted ?? irrigation?.completedCount ?? 0} /{' '}
              {irrigation?.totalPlansCreated ?? irrigation?.totalPlans ?? 0} {t.planAdherenceHint}
            </p>
          </div>
        </div>

        {/* 3. Telemetry Reliability */}
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-4 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 mb-2">
            <span className="text-xs font-medium">{t.telemetryQuality}</span>
            <Activity className="w-4 h-4 text-cyan-500" />
          </div>
          <div>
            <div className="flex items-baseline justify-between">
              <span className="text-2xl font-bold text-slate-900 dark:text-white">
                {telemetryVal !== null && telemetryVal !== undefined
                  ? `${telemetryVal}%`
                  : t.noData}
              </span>
              <span className="text-xs font-medium text-slate-500 dark:text-slate-400">
                {onlineSensorsCount}/{totalSensorsCount}
              </span>
            </div>
            <p className="text-[11px] text-slate-400 mt-1">
              {onlineSensorsCount} {t.activeSensors}
            </p>
          </div>
        </div>

        {/* 4. Verification Coverage */}
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-4 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 mb-2">
            <span className="text-xs font-medium">{t.verificationCov}</span>
            <ShieldCheck className="w-4 h-4 text-indigo-500" />
          </div>
          <div>
            <div className="flex items-baseline justify-between">
              <span className="text-2xl font-bold text-slate-900 dark:text-white">
                {verificationVal !== null && verificationVal !== undefined
                  ? `${verificationVal}%`
                  : t.noData}
              </span>
            </div>
            <p className="text-[11px] text-slate-400 mt-1">
              {verifiedOpsCount} {t.verifiedOps}
            </p>
          </div>
        </div>

        {/* 5. Safety Incidents */}
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-4 shadow-sm flex flex-col justify-between">
          <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 mb-2">
            <span className="text-xs font-medium">{t.safetyEvents}</span>
            <AlertOctagon
              className={`w-4 h-4 ${
                criticalAnomaliesCount > 0 ? 'text-rose-500' : 'text-slate-400'
              }`}
            />
          </div>
          <div>
            <div className="flex items-baseline justify-between">
              <span
                className={`text-2xl font-bold ${
                  criticalAnomaliesCount > 0
                    ? 'text-rose-600 dark:text-rose-400'
                    : 'text-slate-900 dark:text-white'
                }`}
              >
                {totalSafetyEventsCount}
              </span>
              {criticalAnomaliesCount > 0 && (
                <span className="px-2 py-0.5 rounded text-[11px] font-semibold bg-rose-100 text-rose-800 dark:bg-rose-900/40 dark:text-rose-300">
                  {criticalAnomaliesCount} {t.critical}
                </span>
              )}
            </div>
            <p className="text-[11px] text-slate-400 mt-1">
              {activeBlockedCount} bloklangan manba
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}
