import React, { useState, useMemo } from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { Input } from '@/shared/ui/input'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/ui/table'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
} from '@/shared/ui/dialog'
import {
  ArrowRight,
  ArrowUpRight,
  ArrowDownRight,
  Minus,
  Search,
  Info,
  Cpu,
  Layers,
  Sparkles,
} from 'lucide-react'
import { PriorityField } from '@/entities/dashboard'
import { PriorityBadge } from './PriorityBadge'
import { SupportedLanguage } from '@/shared/i18n'
import { NavSection } from '@/widgets/sidebar'

interface PriorityFieldsTableProps {
  fields: PriorityField[]
  lang: SupportedLanguage
  onNavigate: (section: NavSection, fieldId?: string) => void
  onSelectField?: (field: PriorityField) => void
}

export const PriorityFieldsTable: React.FC<PriorityFieldsTableProps> = ({
  fields,
  lang,
  onNavigate,
  onSelectField,
}) => {
  const [search, setSearch] = useState('')
  const [priorityFilter, setPriorityFilter] = useState<string>('ALL')
  const [cropFilter, setCropFilter] = useState<string>('ALL')
  const [explanationField, setExplanationField] = useState<PriorityField | null>(null)

  const t = {
    uz: {
      title: 'Prioritetli Dala Maydonlari',
      subtitle: 'Xavfsizlik, tuproq namligi va suv limitlari boʻyicha saralangan dalalar roʻyxati',
      searchPlaceholder: 'Dala nomi yoki ekin turi boʻyicha qidirish...',
      allPriorities: 'Barcha darajalar',
      allCrops: 'Barcha ekinlar',
      colField: 'Dala & Ekin',
      colPriority: 'Prioritet & Urgency',
      colMoisture: 'Tuproq Namligi',
      colWater: 'Suv Balansi',
      colSensors: 'Datchiklar',
      colStatus: 'Amaldagi Holat',
      colAction: 'Tavsiya etilgan amal',
      empty: 'Hech qanday dala topilmadi',
      reasonsTitle: 'Prioritet Asoslari',
      reasonsSubtitle: 'Nega ushbu dalaga ushbu prioritet darajasi berildi?',
      close: 'Yopish',
      openFeature: 'Boʻlimga oʻtish',
      score: 'Ball',
    },
    ru: {
      title: 'Приоритетный Список Полей',
      subtitle: 'Ранжированный список полей по безопасности, дефициту влаги и водным квотам',
      searchPlaceholder: 'Поиск по названию или культуре...',
      allPriorities: 'Все приоритеты',
      allCrops: 'Все культуры',
      colField: 'Поле и Культура',
      colPriority: 'Приоритет и Срочность',
      colMoisture: 'Влажность Почвы',
      colWater: 'Водный Баланс',
      colSensors: 'Датчики',
      colStatus: 'Операционный Статус',
      colAction: 'Рекомендуемое действие',
      empty: 'Поля не найдены',
      reasonsTitle: 'Обоснование Приоритета',
      reasonsSubtitle: 'Почему этому участку присвоен данный уровень срочности?',
      close: 'Закрыть',
      openFeature: 'Перейти к разделу',
      score: 'Балл',
    },
    en: {
      title: 'Priority Parcels Matrix',
      subtitle: 'Fields ranked by safety, moisture deficit, water constraints, and telemetry health',
      searchPlaceholder: 'Search parcel by name or crop...',
      allPriorities: 'All priorities',
      allCrops: 'All crops',
      colField: 'Parcel & Crop',
      colPriority: 'Priority & Score',
      colMoisture: 'Soil Moisture',
      colWater: 'Water Budget',
      colSensors: 'Sensors',
      colStatus: 'Operational State',
      colAction: 'Recommended Action',
      empty: 'No matching fields found',
      reasonsTitle: 'Priority Explanation',
      reasonsSubtitle: 'Why was this urgency score assigned to this parcel?',
      close: 'Close',
      openFeature: 'Open Feature',
      score: 'Score',
    },
  }[lang]

  // Filter options
  const uniqueCrops = useMemo(() => {
    const crops = new Set<string>()
    for (const f of fields) {
      if (f.cropType) crops.add(f.cropType)
    }
    return Array.from(crops)
  }, [fields])

  // Filtered fields
  const filteredFields = useMemo(() => {
    return fields.filter((f) => {
      if (search) {
        const q = search.toLowerCase()
        const matchName = f.fieldName.toLowerCase().includes(q)
        const matchCrop = f.cropType.toLowerCase().includes(q)
        if (!matchName && !matchCrop) return false
      }
      if (priorityFilter !== 'ALL' && f.priority !== priorityFilter) {
        return false
      }
      if (cropFilter !== 'ALL' && f.cropType !== cropFilter) {
        return false
      }
      return true
    })
  }, [fields, search, priorityFilter, cropFilter])

  const renderTrendIcon = (trend: string) => {
    switch (trend) {
      case 'INCREASING':
        return <ArrowUpRight className="w-3.5 h-3.5 text-emerald-500" />
      case 'DECREASING':
        return <ArrowDownRight className="w-3.5 h-3.5 text-rose-500" />
      case 'STABLE':
      default:
        return <Minus className="w-3.5 h-3.5 text-muted-foreground" />
    }
  }

  const renderFreshnessBadge = (freshness: string) => {
    switch (freshness) {
      case 'FRESH':
        return (
          <Badge variant="outline" className="bg-emerald-500/10 text-emerald-600 border-emerald-500/20 text-[10px] py-0 px-1">
            FRESH
          </Badge>
        )
      case 'STALE':
        return (
          <Badge variant="outline" className="bg-amber-500/10 text-amber-600 border-amber-500/20 text-[10px] py-0 px-1">
            STALE
          </Badge>
        )
      case 'MISSING':
      default:
        return (
          <Badge variant="outline" className="bg-rose-500/10 text-rose-600 border-rose-500/20 text-[10px] py-0 px-1">
            MISSING
          </Badge>
        )
    }
  }

  const formatActionLabel = (action: string) => {
    switch (action) {
      case 'INVESTIGATE_SAFETY':
        return lang === 'uz' ? 'Xavfsizlikni tekshirish' : lang === 'ru' ? 'Проверить безопасность' : 'Investigate Safety'
      case 'APPROVE_PLAN':
        return lang === 'uz' ? 'Rejani tasdiqlash' : lang === 'ru' ? 'Одобрить план' : 'Approve Plan'
      case 'GENERATE_PLAN':
        return lang === 'uz' ? 'Sugʻorish rejalash' : lang === 'ru' ? 'Создать план полива' : 'Generate Plan'
      case 'REVIEW_RECOMMENDATION':
        return lang === 'uz' ? 'Tavsiyani koʻrish' : lang === 'ru' ? 'Обзор рекомендации' : 'Review Recommendation'
      case 'REVIEW_WATER_ALLOCATION':
        return lang === 'uz' ? 'Limitni tekshirish' : lang === 'ru' ? 'Проверить лимит воды' : 'Review Quota'
      case 'INSPECT_SENSOR':
        return lang === 'uz' ? 'Datchikni koʻrish' : lang === 'ru' ? 'Осмотреть датчик' : 'Inspect Sensor'
      case 'MONITOR':
      default:
        return lang === 'uz' ? 'Kuzatish' : lang === 'ru' ? 'Мониторинг' : 'Monitor'
    }
  }

  return (
    <>
      <Card className="shadow-xs">
        <CardHeader className="pb-3 border-b border-border/70">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-3">
            <div>
              <CardTitle className="text-base font-bold text-foreground flex items-center gap-2">
                <Layers className="w-5 h-5 text-primary" />
                <span>{t.title}</span>
                <Badge variant="secondary" className="font-mono text-xs">
                  {filteredFields.length} / {fields.length}
                </Badge>
              </CardTitle>
              <CardDescription className="text-xs mt-0.5">
                {t.subtitle}
              </CardDescription>
            </div>

            {/* Filter Bar */}
            <div className="flex items-center gap-2 flex-wrap">
              <div className="relative w-full sm:w-60">
                <Search className="w-3.5 h-3.5 absolute left-2.5 top-1/2 -translate-y-1/2 text-muted-foreground" />
                <Input
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  placeholder={t.searchPlaceholder}
                  className="h-8 pl-8 text-xs bg-background"
                />
              </div>

              {/* Priority Filter */}
              <select
                value={priorityFilter}
                onChange={(e) => setPriorityFilter(e.target.value)}
                className="h-8 rounded-md border border-input bg-background px-2 py-1 text-xs text-foreground focus:outline-none focus:ring-1 focus:ring-primary"
              >
                <option value="ALL">{t.allPriorities}</option>
                <option value="CRITICAL">Critical</option>
                <option value="HIGH">High</option>
                <option value="MEDIUM">Medium</option>
                <option value="LOW">Low</option>
              </select>

              {/* Crop Filter */}
              {uniqueCrops.length > 0 && (
                <select
                  value={cropFilter}
                  onChange={(e) => setCropFilter(e.target.value)}
                  className="h-8 rounded-md border border-input bg-background px-2 py-1 text-xs text-foreground focus:outline-none focus:ring-1 focus:ring-primary"
                >
                  <option value="ALL">{t.allCrops}</option>
                  {uniqueCrops.map((c) => (
                    <option key={c} value={c}>{c}</option>
                  ))}
                </select>
              )}
            </div>
          </div>
        </CardHeader>

        <CardContent className="p-0">
          {filteredFields.length === 0 ? (
            <div className="py-12 text-center text-xs text-muted-foreground">
              {t.empty}
            </div>
          ) : (
            <>
              {/* Desktop Table View */}
              <div className="hidden md:block overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow className="hover:bg-transparent bg-muted/30">
                      <TableHead className="text-xs font-semibold">{t.colField}</TableHead>
                      <TableHead className="text-xs font-semibold">{t.colPriority}</TableHead>
                      <TableHead className="text-xs font-semibold">{t.colMoisture}</TableHead>
                      <TableHead className="text-xs font-semibold">{t.colWater}</TableHead>
                      <TableHead className="text-xs font-semibold">{t.colSensors}</TableHead>
                      <TableHead className="text-xs font-semibold">{t.colStatus}</TableHead>
                      <TableHead className="text-xs font-semibold text-right">{t.colAction}</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {filteredFields.map((field) => (
                      <TableRow
                        key={field.fieldId}
                        className={`hover:bg-muted/40 transition-colors cursor-pointer ${
                          field.hydraulicSafetyBlocked ? 'bg-rose-500/5' : ''
                        }`}
                        onClick={() => onSelectField?.(field)}
                      >
                        {/* Field & Crop */}
                        <TableCell className="py-3">
                          <div className="space-y-0.5">
                            <span className="font-semibold text-sm text-foreground block">
                              {field.fieldName}
                            </span>
                            <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
                              <Badge variant="outline" className="text-[10px] py-0 px-1 font-mono">
                                {field.cropType}
                              </Badge>
                              <span>• {field.areaHectares} ha</span>
                            </div>
                          </div>
                        </TableCell>

                        {/* Priority & Score with Explanation Trigger */}
                        <TableCell className="py-3">
                          <div className="flex items-center gap-2">
                            <PriorityBadge
                              priority={field.priority}
                              score={field.priorityScore}
                              lang={lang}
                            />
                            <Button
                              variant="ghost"
                              size="icon"
                              className="h-6 w-6 text-muted-foreground hover:text-foreground"
                              title={t.reasonsTitle}
                              onClick={(e) => {
                                e.stopPropagation()
                                setExplanationField(field)
                              }}
                            >
                              <Info className="w-3.5 h-3.5" />
                            </Button>
                          </div>
                        </TableCell>

                        {/* Soil Moisture */}
                        <TableCell className="py-3">
                          {field.currentMoisturePercent !== null ? (
                            <div className="space-y-0.5">
                              <div className="flex items-center gap-1.5">
                                <span className="font-mono font-bold text-sm text-foreground">
                                  {field.currentMoisturePercent}%
                                </span>
                                {renderTrendIcon(field.moistureTrend)}
                              </div>
                              <div className="flex items-center gap-1">
                                {renderFreshnessBadge(field.telemetryFreshness)}
                              </div>
                            </div>
                          ) : (
                            <Badge variant="outline" className="text-[10px] py-0 px-1 text-muted-foreground">
                              {field.telemetryFreshness}
                            </Badge>
                          )}
                        </TableCell>

                        {/* Water Budget */}
                        <TableCell className="py-3">
                          <div className="space-y-1">
                            <div className="flex items-center justify-between text-xs">
                              <span className="text-muted-foreground font-mono">
                                {Math.round(field.consumedM3).toLocaleString()} / {Math.round(field.allocatedM3).toLocaleString()} m³
                              </span>
                            </div>
                            <div className="flex items-center gap-1.5">
                              <span className="text-[11px] text-muted-foreground">
                                Qolgan: <strong className="text-foreground font-mono">{Math.round(field.remainingM3).toLocaleString()} m³</strong>
                              </span>
                              {field.waterStatus === 'CRITICAL_OVER_CONSUMED' && (
                                <Badge variant="outline" className="bg-rose-500/10 text-rose-600 border-rose-500/25 text-[10px] py-0 px-1 font-bold">
                                  Oshgan
                                </Badge>
                              )}
                            </div>
                          </div>
                        </TableCell>

                        {/* Sensor Fleet */}
                        <TableCell className="py-3">
                          <Badge
                            variant="outline"
                            className={`text-[10px] py-0 px-1.5 ${
                              field.sensorHealth === 'ONLINE'
                                ? 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20'
                                : field.sensorHealth === 'OFFLINE'
                                ? 'bg-rose-500/10 text-rose-600 border-rose-500/20 font-semibold'
                                : 'bg-muted text-muted-foreground'
                            }`}
                          >
                            <Cpu className="w-3 h-3 mr-1" />
                            {field.sensorHealth}
                          </Badge>
                        </TableCell>

                        {/* Active Anomalies & Plans */}
                        <TableCell className="py-3">
                          <div className="space-y-1">
                            {field.hydraulicSafetyBlocked ? (
                              <Badge variant="outline" className="bg-rose-500/15 text-rose-700 border-rose-400 font-bold text-[10px] py-0 px-1.5 animate-pulse">
                                BLOCKED (Gidro-xavf)
                              </Badge>
                            ) : field.activePlanStatus !== 'NONE' ? (
                              <Badge variant="outline" className="bg-sky-500/10 text-sky-700 border-sky-300 text-[10px] py-0 px-1.5 font-medium">
                                Plan: {field.activePlanStatus}
                              </Badge>
                            ) : field.activeRecommendationCount > 0 ? (
                              <Badge variant="outline" className="bg-primary/10 text-primary border-primary/20 text-[10px] py-0 px-1.5 font-medium">
                                <Sparkles className="w-2.5 h-2.5 mr-1" />
                                {field.activeRecommendationCount} tavsiya
                              </Badge>
                            ) : (
                              <span className="text-xs text-muted-foreground">Barqaror</span>
                            )}
                          </div>
                        </TableCell>

                        {/* Action Button */}
                        <TableCell className="py-3 text-right">
                          <Button
                            variant={field.priority === 'CRITICAL' ? 'destructive' : 'default'}
                            size="sm"
                            className="text-xs h-7 px-2.5 gap-1 shadow-xs"
                            onClick={(e) => {
                              e.stopPropagation()
                              const isOperation =
                                field.recommendedAction === 'APPROVE_PLAN' ||
                                field.recommendedAction === 'GENERATE_PLAN' ||
                                field.recommendedAction === 'EXECUTE_OPERATION' ||
                                field.actionTargetSection === 'operations'
                              const target = isOperation ? 'operations' : (field.actionTargetSection as NavSection)
                              onNavigate(target, field.fieldId)
                            }}
                          >
                            <span>{formatActionLabel(field.recommendedAction)}</span>
                            <ArrowRight className="w-3 h-3" />
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>

              {/* Mobile View: Stacked Priority Cards */}
              <div className="md:hidden divide-y divide-border/60">
                {filteredFields.map((field) => (
                  <div
                    key={field.fieldId}
                    className="p-4 space-y-3 hover:bg-muted/30 transition-colors"
                    onClick={() => onSelectField?.(field)}
                  >
                    <div className="flex items-start justify-between gap-2">
                      <div>
                        <h4 className="font-bold text-sm text-foreground">{field.fieldName}</h4>
                        <p className="text-xs text-muted-foreground">
                          {field.cropType} • {field.areaHectares} ha
                        </p>
                      </div>
                      <PriorityBadge
                        priority={field.priority}
                        score={field.priorityScore}
                        lang={lang}
                      />
                    </div>

                    {/* Quick Metrics Grid */}
                    <div className="grid grid-cols-2 gap-2 text-xs p-2.5 rounded-lg bg-muted/40 border border-border/60">
                      <div>
                        <span className="text-muted-foreground block text-[11px]">{t.colMoisture}</span>
                        <div className="flex items-center gap-1 font-bold font-mono">
                          {field.currentMoisturePercent !== null ? `${field.currentMoisturePercent}%` : 'N/A'}
                          {renderTrendIcon(field.moistureTrend)}
                        </div>
                      </div>

                      <div>
                        <span className="text-muted-foreground block text-[11px]">Suv Qoldigʻi</span>
                        <span className="font-bold font-mono text-foreground">
                          {Math.round(field.remainingM3).toLocaleString()} m³
                        </span>
                      </div>
                    </div>

                    {/* Action Bar */}
                    <div className="flex items-center justify-between gap-2 pt-1">
                      <Button
                        variant="ghost"
                        size="sm"
                        className="text-xs h-8 text-muted-foreground"
                        onClick={(e) => {
                          e.stopPropagation()
                          setExplanationField(field)
                        }}
                      >
                        <Info className="w-3.5 h-3.5 mr-1" />
                        {t.reasonsTitle}
                      </Button>

                      <Button
                        variant={field.priority === 'CRITICAL' ? 'destructive' : 'default'}
                        size="sm"
                        className="text-xs h-8 gap-1.5 shadow-xs"
                        onClick={(e) => {
                          e.stopPropagation()
                          onNavigate(field.actionTargetSection as NavSection)
                        }}
                      >
                        <span>{formatActionLabel(field.recommendedAction)}</span>
                        <ArrowRight className="w-3 h-3" />
                      </Button>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}
        </CardContent>
      </Card>

      {/* Priority Explanation Dialog */}
      {explanationField && (
        <Dialog open={!!explanationField} onOpenChange={() => setExplanationField(null)}>
          <DialogContent className="max-w-md">
            <DialogHeader>
              <div className="flex items-center gap-2">
                <PriorityBadge
                  priority={explanationField.priority}
                  score={explanationField.priorityScore}
                  lang={lang}
                />
                <DialogTitle className="text-base font-bold">
                  {explanationField.fieldName}
                </DialogTitle>
              </div>
              <DialogDescription className="text-xs">
                {t.reasonsSubtitle}
              </DialogDescription>
            </DialogHeader>

            <div className="space-y-4 py-2">
              <div className="p-3 rounded-lg bg-muted/40 border border-border/80 space-y-1.5 text-xs">
                <div className="font-semibold text-foreground flex items-center justify-between">
                  <span>{t.reasonsTitle}:</span>
                  <span className="font-mono text-primary font-bold">
                    {t.score}: {explanationField.priorityScore}/100
                  </span>
                </div>
                <ul className="space-y-1 list-disc list-inside text-muted-foreground pt-1">
                  {explanationField.priorityReasons.map((reason, idx) => (
                    <li key={idx} className="leading-relaxed">
                      {reason}
                    </li>
                  ))}
                </ul>
              </div>

              <div className="flex items-center justify-between gap-3 pt-2">
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => setExplanationField(null)}
                >
                  {t.close}
                </Button>
                <Button
                  variant="default"
                  size="sm"
                  className="gap-1.5"
                  onClick={() => {
                    const isOperation =
                      explanationField.recommendedAction === 'APPROVE_PLAN' ||
                      explanationField.recommendedAction === 'GENERATE_PLAN' ||
                      explanationField.recommendedAction === 'EXECUTE_OPERATION' ||
                      explanationField.actionTargetSection === 'operations'
                    const target = isOperation ? 'operations' : (explanationField.actionTargetSection as NavSection)
                    const fId = explanationField.fieldId
                    setExplanationField(null)
                    onNavigate(target, fId)
                  }}
                >
                  <span>{formatActionLabel(explanationField.recommendedAction)}</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      )}
    </>
  )
}
