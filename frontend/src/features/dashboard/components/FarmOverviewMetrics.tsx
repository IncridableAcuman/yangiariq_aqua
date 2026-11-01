import React from 'react'
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import {
  Layers,
  Droplets,
  Cpu,
  ShieldAlert,
  CalendarCheck2,
} from 'lucide-react'
import {
  FieldMetrics,
  WaterMetrics,
  SensorMetrics,
  AnomalyMetrics,
  IrrigationMetrics,
} from '@/entities/dashboard'
import { SupportedLanguage } from '@/shared/i18n'
import { NavSection } from '@/widgets/sidebar'

interface FarmOverviewMetricsProps {
  fieldMetrics: FieldMetrics
  waterMetrics: WaterMetrics
  sensorMetrics: SensorMetrics
  anomalyMetrics: AnomalyMetrics
  irrigationMetrics: IrrigationMetrics
  lang: SupportedLanguage
  onNavigate?: (section: NavSection) => void
}

export const FarmOverviewMetrics: React.FC<FarmOverviewMetricsProps> = ({
  fieldMetrics,
  waterMetrics,
  sensorMetrics,
  anomalyMetrics,
  irrigationMetrics,
  lang,
  onNavigate,
}) => {
  const t = {
    uz: {
      fields: 'Dala Maydonlari',
      activeFields: 'faol',
      attentionRequired: 'eʼtibor talab',
      area: 'ga umumiy maydon',
      water: 'Suv Taqsimoti',
      allocated: 'Ajratilgan',
      consumed: 'Sarflangan',
      remaining: 'Qolgan',
      overConsumed: 'limitdan oshgan',
      sensors: 'Datchiklar Floti',
      online: 'onlayn',
      offline: 'aloqa yoʻq',
      stale: 'eskirgan',
      validData: 'aniqlik darajasi',
      anomalies: 'Xavf & Ogohlantirish',
      critical: 'kritik xavf',
      open: 'ochiq alertlar',
      irrigation: 'Sugʻorish Jarayoni',
      awaitingApproval: 'tasdiqlash kutilmoqda',
      scheduledToday: 'bugun rejalashtirilgan',
      inProgress: 'bajarilmoqda',
      blocked: 'xavfsizlik blokida',
      viewAll: 'Koʻrish',
    },
    ru: {
      fields: 'Полевые Участки',
      activeFields: 'активно',
      attentionRequired: 'требует внимания',
      area: 'га общая площадь',
      water: 'Водный Баланс',
      allocated: 'Выделено',
      consumed: 'Израсходовано',
      remaining: 'Остаток',
      overConsumed: 'превышение лимита',
      sensors: 'Парк Датчиков',
      online: 'в сети',
      offline: 'не в сети',
      stale: 'устаревшие',
      validData: 'достоверность данных',
      anomalies: 'Аномалии и Угрозы',
      critical: 'критических',
      open: 'открытых алертов',
      irrigation: 'Статус Орошения',
      awaitingApproval: 'ожидает одобрения',
      scheduledToday: 'сегодня в графике',
      inProgress: 'выполняется',
      blocked: 'заблокировано защитой',
      viewAll: 'Подробнее',
    },
    en: {
      fields: 'Field Parcels',
      activeFields: 'active',
      attentionRequired: 'need attention',
      area: 'ha total cultivated',
      water: 'Water Allocation',
      allocated: 'Allocated',
      consumed: 'Consumed',
      remaining: 'Remaining',
      overConsumed: 'over-consumed',
      sensors: 'Sensor Fleet',
      online: 'online',
      offline: 'offline',
      stale: 'stale',
      validData: 'telemetry validity',
      anomalies: 'Anomalies & Risks',
      critical: 'critical',
      open: 'open alerts',
      irrigation: 'Irrigation Operations',
      awaitingApproval: 'awaiting approval',
      scheduledToday: 'scheduled today',
      inProgress: 'in progress',
      blocked: 'safety blocked',
      viewAll: 'View',
    },
  }[lang]

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3.5">
      {/* 1. Fields Overview */}
      <Card
        className="cursor-pointer hover:border-primary/50 transition-colors shadow-xs"
        onClick={() => onNavigate?.('fields')}
      >
        <CardHeader className="flex flex-row items-center justify-between pb-1.5 space-y-0">
          <CardTitle className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
            {t.fields}
          </CardTitle>
          <div className="h-7 w-7 rounded-md bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
            <Layers className="h-4 w-4" />
          </div>
        </CardHeader>
        <CardContent className="space-y-1.5">
          <div className="flex items-baseline justify-between">
            <span className="text-2xl font-bold tracking-tight text-foreground">
              {fieldMetrics.totalFields}
            </span>
            <span className="text-xs text-muted-foreground font-mono">
              {fieldMetrics.totalAreaHectares} {t.area}
            </span>
          </div>

          <div className="flex items-center gap-1.5 flex-wrap pt-0.5">
            <Badge variant="outline" className="bg-emerald-500/10 text-emerald-600 border-emerald-500/20 text-[10px] py-0 px-1.5">
              {fieldMetrics.activeFields} {t.activeFields}
            </Badge>
            {fieldMetrics.fieldsRequiringAttention > 0 && (
              <Badge variant="outline" className="bg-rose-500/10 text-rose-600 border-rose-500/25 text-[10px] py-0 px-1.5 font-semibold">
                {fieldMetrics.fieldsRequiringAttention} {t.attentionRequired}
              </Badge>
            )}
          </div>
        </CardContent>
      </Card>

      {/* 2. Water Balance Overview */}
      <Card
        className="cursor-pointer hover:border-primary/50 transition-colors shadow-xs"
        onClick={() => onNavigate?.('irrigation')}
      >
        <CardHeader className="flex flex-row items-center justify-between pb-1.5 space-y-0">
          <CardTitle className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
            {t.water}
          </CardTitle>
          <div className="h-7 w-7 rounded-md bg-sky-500/10 text-sky-600 dark:text-sky-400 flex items-center justify-center">
            <Droplets className="h-4 w-4" />
          </div>
        </CardHeader>
        <CardContent className="space-y-1.5">
          <div className="flex items-baseline justify-between">
            <span className="text-2xl font-bold tracking-tight text-foreground font-mono">
              {waterMetrics.overallConsumptionPercentage.toFixed(0)}%
            </span>
            <span className="text-xs text-muted-foreground font-mono">
              {Math.round(waterMetrics.totalConsumedM3).toLocaleString()} / {Math.round(waterMetrics.totalAllocatedM3).toLocaleString()} m³
            </span>
          </div>

          {/* Mini progress bar */}
          <div className="w-full h-1.5 bg-muted rounded-full overflow-hidden">
            <div
              className={`h-full transition-all ${
                waterMetrics.overallConsumptionPercentage > 100
                  ? 'bg-rose-500'
                  : waterMetrics.overallConsumptionPercentage > 80
                  ? 'bg-amber-500'
                  : 'bg-primary'
              }`}
              style={{ width: `${Math.min(100, waterMetrics.overallConsumptionPercentage)}%` }}
            />
          </div>

          <div className="flex items-center justify-between text-[11px] text-muted-foreground">
            <span>{t.remaining}: <strong className="text-foreground font-mono">{Math.round(waterMetrics.totalRemainingM3).toLocaleString()} m³</strong></span>
            {waterMetrics.overConsumedFieldCount > 0 && (
              <Badge variant="outline" className="bg-rose-500/10 text-rose-600 border-rose-500/20 text-[10px] py-0 px-1">
                {waterMetrics.overConsumedFieldCount} {t.overConsumed}
              </Badge>
            )}
          </div>
        </CardContent>
      </Card>

      {/* 3. Sensor Health Overview */}
      <Card
        className="cursor-pointer hover:border-primary/50 transition-colors shadow-xs"
        onClick={() => onNavigate?.('sensors')}
      >
        <CardHeader className="flex flex-row items-center justify-between pb-1.5 space-y-0">
          <CardTitle className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
            {t.sensors}
          </CardTitle>
          <div className="h-7 w-7 rounded-md bg-cyan-500/10 text-cyan-600 dark:text-cyan-400 flex items-center justify-center">
            <Cpu className="h-4 w-4" />
          </div>
        </CardHeader>
        <CardContent className="space-y-1.5">
          <div className="flex items-baseline justify-between">
            <span className="text-2xl font-bold tracking-tight text-foreground">
              {sensorMetrics.activeSensors}
              <span className="text-sm font-normal text-muted-foreground"> / {sensorMetrics.totalSensors}</span>
            </span>
            <span className="text-xs text-emerald-600 font-semibold font-mono">
              {sensorMetrics.validTelemetryPercentage}% {t.validData}
            </span>
          </div>

          <div className="flex items-center gap-1.5 flex-wrap pt-0.5">
            <Badge variant="outline" className="bg-emerald-500/10 text-emerald-600 border-emerald-500/20 text-[10px] py-0 px-1.5">
              {sensorMetrics.activeSensors} {t.online}
            </Badge>
            {sensorMetrics.offlineSensors > 0 && (
              <Badge variant="outline" className="bg-rose-500/10 text-rose-600 border-rose-500/25 text-[10px] py-0 px-1.5 font-semibold">
                {sensorMetrics.offlineSensors} {t.offline}
              </Badge>
            )}
            {sensorMetrics.staleTelemetrySensors > 0 && (
              <Badge variant="outline" className="bg-amber-500/10 text-amber-600 border-amber-500/25 text-[10px] py-0 px-1.5">
                {sensorMetrics.staleTelemetrySensors} {t.stale}
              </Badge>
            )}
          </div>
        </CardContent>
      </Card>

      {/* 4. Anomalies & Risks Overview */}
      <Card
        className="cursor-pointer hover:border-primary/50 transition-colors shadow-xs"
        onClick={() => onNavigate?.('anomalies')}
      >
        <CardHeader className="flex flex-row items-center justify-between pb-1.5 space-y-0">
          <CardTitle className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
            {t.anomalies}
          </CardTitle>
          <div className={`h-7 w-7 rounded-md flex items-center justify-center ${
            anomalyMetrics.critical > 0
              ? 'bg-rose-500/15 text-rose-600 animate-pulse'
              : 'bg-muted text-muted-foreground'
          }`}>
            <ShieldAlert className="h-4 w-4" />
          </div>
        </CardHeader>
        <CardContent className="space-y-1.5">
          <div className="flex items-baseline justify-between">
            <span className={`text-2xl font-bold tracking-tight ${
              anomalyMetrics.critical > 0 ? 'text-rose-600 dark:text-rose-400' : 'text-foreground'
            }`}>
              {anomalyMetrics.totalOpen}
            </span>
            <span className="text-xs text-muted-foreground font-mono">
              {t.open}
            </span>
          </div>

          <div className="flex items-center gap-1.5 flex-wrap pt-0.5">
            {anomalyMetrics.critical > 0 ? (
              <Badge variant="outline" className="bg-rose-500/15 text-rose-700 border-rose-400 text-[10px] py-0 px-1.5 font-bold animate-pulse">
                {anomalyMetrics.critical} {t.critical}
              </Badge>
            ) : (
              <Badge variant="outline" className="bg-emerald-500/10 text-emerald-600 border-emerald-500/20 text-[10px] py-0 px-1.5">
                0 kritik xavf
              </Badge>
            )}
            {anomalyMetrics.high > 0 && (
              <Badge variant="outline" className="bg-amber-500/10 text-amber-600 border-amber-500/20 text-[10px] py-0 px-1.5">
                {anomalyMetrics.high} yuqori
              </Badge>
            )}
          </div>
        </CardContent>
      </Card>

      {/* 5. Irrigation Operations Overview */}
      <Card
        className="cursor-pointer hover:border-primary/50 transition-colors shadow-xs"
        onClick={() => onNavigate?.('irrigation')}
      >
        <CardHeader className="flex flex-row items-center justify-between pb-1.5 space-y-0">
          <CardTitle className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
            {t.irrigation}
          </CardTitle>
          <div className="h-7 w-7 rounded-md bg-primary/10 text-primary flex items-center justify-center">
            <CalendarCheck2 className="h-4 w-4" />
          </div>
        </CardHeader>
        <CardContent className="space-y-1.5">
          <div className="flex items-baseline justify-between">
            <span className="text-2xl font-bold tracking-tight text-foreground font-mono">
              {irrigationMetrics.inProgressCount + irrigationMetrics.scheduledTodayCount}
            </span>
            <span className="text-xs text-muted-foreground">
              {irrigationMetrics.scheduledTodayCount} {t.scheduledToday}
            </span>
          </div>

          <div className="flex items-center gap-1.5 flex-wrap pt-0.5">
            {irrigationMetrics.proposedCount > 0 && (
              <Badge variant="outline" className="bg-amber-500/15 text-amber-700 border-amber-400 text-[10px] py-0 px-1.5 font-semibold">
                {irrigationMetrics.proposedCount} {t.awaitingApproval}
              </Badge>
            )}
            {irrigationMetrics.inProgressCount > 0 && (
              <Badge variant="outline" className="bg-sky-500/15 text-sky-700 border-sky-400 text-[10px] py-0 px-1.5 font-medium animate-pulse">
                {irrigationMetrics.inProgressCount} {t.inProgress}
              </Badge>
            )}
            {irrigationMetrics.blockedCount > 0 && (
              <Badge variant="outline" className="bg-rose-500/15 text-rose-700 border-rose-400 text-[10px] py-0 px-1.5 font-bold">
                {irrigationMetrics.blockedCount} {t.blocked}
              </Badge>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
