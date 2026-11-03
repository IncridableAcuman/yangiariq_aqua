import React, { useState, useMemo } from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/ui/table'
import {
  Activity,
  AlertTriangle,
  CheckCircle2,
  HelpCircle,
} from 'lucide-react'
import { ContextPerformanceProfile } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface ContextPerformanceTableProps {
  profiles?: ContextPerformanceProfile[]
  isLoading?: boolean
  lang: SupportedLanguage
}

export const ContextPerformanceTable: React.FC<ContextPerformanceTableProps> = ({
  profiles = [],
  isLoading,
  lang,
}) => {
  const [selectedType, setSelectedType] = useState<string>('ALL')
  const [selectedCrop, setSelectedCrop] = useState<string>('ALL')

  const t = {
    uz: {
      title: 'Kontekstual Samaradorlik Matritsasi',
      subtitle: 'Har bir agronomik kontekst (ekin, tuproq, sug‘orish usuli, mavsum) bo‘yicha empirik sinov natijalari (n ≥ 10 cheklovi bilan)',
      allTypes: 'Barcha Prognoz Turlari',
      allCrops: 'Barcha Ekinlar',
      colSlice: 'Kontekst Bo‘lagi',
      colSeason: 'Mavsum',
      colTelemetry: 'Telemetriya',
      colSamples: 'Sinovlar (n)',
      colMae: 'O‘rtacha Xatolik (MAE)',
      colRmse: 'RMSE',
      colMbe: 'Yo‘nalishli Og‘ish (MBE)',
      colHealth: 'Kontekst Salomatligi',
      sampleGated: 'Yetarsiz (n < 10)',
      emptyText: 'Belgilangan parametrlar bo‘yicha kontekstual profillar topilmadi.',
    },
    ru: {
      title: 'Матрица Контекстной Точности',
      subtitle: 'Эмпирические показатели по агрономическим контекстам (культура, почва, полив, сезон) с порогом выборки n ≥ 10',
      allTypes: 'Все Типы Прогнозов',
      allCrops: 'Все Культуры',
      colSlice: 'Срез Контекста',
      colSeason: 'Сезон',
      colTelemetry: 'Телеметрия',
      colSamples: 'Выборка (n)',
      colMae: 'Средняя Ошибка (MAE)',
      colRmse: 'RMSE',
      colMbe: 'Смещение (MBE)',
      colHealth: 'Здоровье Среза',
      sampleGated: 'Недостаточно (n < 10)',
      emptyText: 'Контекстные профили не найдены для выбранных критериев.',
    },
    en: {
      title: 'Contextual Performance Matrix',
      subtitle: 'Empirical accuracy slices across agronomic contexts (crop, soil, irrigation, season) with minimum sample gating (n ≥ 10)',
      allTypes: 'All Prediction Types',
      allCrops: 'All Crops',
      colSlice: 'Context Slice',
      colSeason: 'Season',
      colTelemetry: 'Telemetry Quality',
      colSamples: 'Samples (n)',
      colMae: 'Mean Abs Error (MAE)',
      colRmse: 'RMSE',
      colMbe: 'Mean Bias (MBE)',
      colHealth: 'Context Health',
      sampleGated: 'Gated (n < 10)',
      emptyText: 'No contextual profiles found matching the current filters.',
    },
  }[lang]

  const filteredProfiles = useMemo(() => {
    return profiles.filter((p) => {
      if (selectedType !== 'ALL' && p.predictionType !== selectedType) return false
      if (selectedCrop !== 'ALL' && p.cropType !== selectedCrop) return false
      return true
    })
  }, [profiles, selectedType, selectedCrop])

  const cropOptions = useMemo(() => {
    const set = new Set<string>()
    profiles.forEach((p) => {
      if (p.cropType) set.add(p.cropType)
    })
    return Array.from(set)
  }, [profiles])

  return (
    <Card className="border-border/60 bg-card/60 backdrop-blur-sm">
      <CardHeader className="p-4 border-b border-border/40">
        <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
          <div>
            <CardTitle className="text-base font-semibold flex items-center gap-2">
              <Activity className="w-4 h-4 text-primary" />
              {t.title}
            </CardTitle>
            <CardDescription className="text-xs text-muted-foreground mt-0.5">
              {t.subtitle}
            </CardDescription>
          </div>

          <div className="flex flex-wrap items-center gap-2 w-full sm:w-auto">
            <select
              value={selectedType}
              onChange={(e) => setSelectedType(e.target.value)}
              className="h-8 text-xs rounded-lg border border-border bg-background px-2.5 py-1 text-foreground focus:outline-hidden focus:ring-2 focus:ring-primary/20"
            >
              <option value="ALL">{t.allTypes}</option>
              <option value="SOIL_MOISTURE_FORECAST">Soil Moisture Forecast</option>
              <option value="WATER_DEMAND_FORECAST">Water Demand Forecast</option>
              <option value="IRRIGATION_NEED_FORECAST">Irrigation Need Forecast</option>
              <option value="WATER_AVAILABILITY_RISK">Water Availability Risk</option>
              <option value="IRRIGATION_PLAN_RISK">Plan Risk</option>
              <option value="SENSOR_RELIABILITY_FORECAST">Sensor Reliability</option>
            </select>

            {cropOptions.length > 0 && (
              <select
                value={selectedCrop}
                onChange={(e) => setSelectedCrop(e.target.value)}
                className="h-8 text-xs rounded-lg border border-border bg-background px-2.5 py-1 text-foreground focus:outline-hidden focus:ring-2 focus:ring-primary/20"
              >
                <option value="ALL">{t.allCrops}</option>
                {cropOptions.map((crop) => (
                  <option key={crop} value={crop}>{crop}</option>
                ))}
              </select>
            )}
          </div>
        </div>
      </CardHeader>

      <CardContent className="p-0">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader className="bg-muted/30">
              <TableRow>
                <TableHead className="text-xs font-semibold">{t.colSlice}</TableHead>
                <TableHead className="text-xs font-semibold">{t.colSeason}</TableHead>
                <TableHead className="text-xs font-semibold">{t.colTelemetry}</TableHead>
                <TableHead className="text-xs font-semibold text-center">{t.colSamples}</TableHead>
                <TableHead className="text-xs font-semibold text-right">{t.colMae}</TableHead>
                <TableHead className="text-xs font-semibold text-right">{t.colRmse}</TableHead>
                <TableHead className="text-xs font-semibold text-right">{t.colMbe}</TableHead>
                <TableHead className="text-xs font-semibold text-center">{t.colHealth}</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading ? (
                <TableRow>
                  <TableCell colSpan={8} className="text-center py-8 text-xs text-muted-foreground">
                    Loading contextual profiles...
                  </TableCell>
                </TableRow>
              ) : filteredProfiles.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} className="text-center py-8 text-xs text-muted-foreground">
                    {t.emptyText}
                  </TableCell>
                </TableRow>
              ) : (
                filteredProfiles.map((p, idx) => {
                  const isDegraded = p.contextualModelHealth === 'DEGRADED' || p.contextDriftDetected
                  const isWatch = p.contextualModelHealth === 'WATCH'
                  const isGated = !p.hasSufficientSample

                  return (
                    <TableRow key={`${p.versionTag}-${p.cropType}-${p.irrigationMethod}-${idx}`} className="hover:bg-muted/20">
                      <TableCell className="text-xs py-3">
                        <div className="font-semibold text-foreground flex items-center gap-1.5">
                          <span>{p.cropType}</span>
                          <span className="text-muted-foreground font-normal">/</span>
                          <span className="text-muted-foreground font-normal">{p.soilType}</span>
                          <span className="text-muted-foreground font-normal">/</span>
                          <Badge variant="outline" className="text-[10px] px-1 py-0 border-border">
                            {p.irrigationMethod}
                          </Badge>
                        </div>
                        <div className="text-[11px] text-muted-foreground mt-0.5 flex items-center gap-1">
                          <span className="font-mono text-[10px] text-primary">{p.versionTag}</span>
                          <span className="text-muted-foreground">•</span>
                          <span>{p.predictionType}</span>
                        </div>
                      </TableCell>

                      <TableCell className="text-xs py-3">
                        <Badge variant="secondary" className="text-[10px] px-1.5 py-0.5 font-normal">
                          {p.season}
                        </Badge>
                      </TableCell>

                      <TableCell className="text-xs py-3">
                        <span className={`text-[11px] font-medium ${
                          p.telemetryQuality === 'HIGH'
                            ? 'text-emerald-600 dark:text-emerald-400'
                            : p.telemetryQuality === 'MEDIUM'
                            ? 'text-amber-600 dark:text-amber-400'
                            : 'text-muted-foreground'
                        }`}>
                          {p.telemetryQuality}
                        </span>
                      </TableCell>

                      <TableCell className="text-xs py-3 text-center">
                        <div className="flex flex-col items-center">
                          <span className={`font-mono font-semibold ${isGated ? 'text-muted-foreground' : 'text-foreground'}`}>
                            {p.sampleSize}
                          </span>
                          <Badge
                            variant="outline"
                            className={`text-[9px] px-1 py-0 mt-0.5 ${
                              isGated
                                ? 'border-amber-500/30 text-amber-600 bg-amber-500/5'
                                : 'border-emerald-500/30 text-emerald-600 bg-emerald-500/5'
                            }`}
                          >
                            {isGated ? 'n < 10' : 'n ≥ 10'}
                          </Badge>
                        </div>
                      </TableCell>

                      <TableCell className="text-xs py-3 text-right font-mono">
                        {p.meanAbsoluteError != null ? `${p.meanAbsoluteError.toFixed(1)}%` : '—'}
                      </TableCell>

                      <TableCell className="text-xs py-3 text-right font-mono text-muted-foreground">
                        {p.rootMeanSquaredError != null ? `${p.rootMeanSquaredError.toFixed(1)}%` : '—'}
                      </TableCell>

                      <TableCell className="text-xs py-3 text-right font-mono">
                        {p.meanBiasError != null ? (
                          <span className={p.meanBiasError > 0 ? 'text-blue-600 dark:text-blue-400' : 'text-purple-600 dark:text-purple-400'}>
                            {p.meanBiasError > 0 ? `+${p.meanBiasError.toFixed(1)}%` : `${p.meanBiasError.toFixed(1)}%`}
                          </span>
                        ) : (
                          '—'
                        )}
                      </TableCell>

                      <TableCell className="text-xs py-3 text-center">
                        {isGated ? (
                          <Badge variant="outline" className="text-[10px] text-muted-foreground border-dashed">
                            <HelpCircle className="w-3 h-3 mr-1" />
                            {t.sampleGated}
                          </Badge>
                        ) : isDegraded ? (
                          <Badge variant="destructive" className="text-[10px] flex items-center gap-1 justify-center">
                            <AlertTriangle className="w-3 h-3" />
                            DEGRADED
                          </Badge>
                        ) : isWatch ? (
                          <Badge variant="outline" className="text-[10px] border-amber-500/40 text-amber-700 dark:text-amber-400 bg-amber-500/10">
                            WATCH
                          </Badge>
                        ) : (
                          <Badge variant="outline" className="text-[10px] border-emerald-500/40 text-emerald-700 dark:text-emerald-400 bg-emerald-500/10">
                            <CheckCircle2 className="w-3 h-3 mr-1" />
                            HEALTHY
                          </Badge>
                        )}
                      </TableCell>
                    </TableRow>
                  )
                })
              )}
            </TableBody>
          </Table>
        </div>
      </CardContent>
    </Card>
  )
}
