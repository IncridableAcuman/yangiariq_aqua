import React, { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/ui/table'
import {
  History,
  ShieldAlert,
  ChevronLeft,
  ChevronRight,
  ExternalLink,
} from 'lucide-react'
import {
  EvaluationHistoryResponse,
  PredictionClassificationResult,
  PredictionEvaluationDetail,
} from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'
import { EvaluationDetailModal } from './EvaluationDetailModal'

interface EvaluationHistoryTableProps {
  data?: EvaluationHistoryResponse | null
  isLoading?: boolean
  currentPage: number
  onPageChange: (newPage: number) => void
  statusFilter: string
  onStatusFilterChange: (status: string) => void
  typeFilter: string
  onTypeFilterChange: (type: string) => void
  lang: SupportedLanguage
}

export const EvaluationHistoryTable: React.FC<EvaluationHistoryTableProps> = ({
  data,
  isLoading,
  currentPage,
  onPageChange,
  statusFilter,
  onStatusFilterChange,
  typeFilter,
  onTypeFilterChange,
  lang,
}) => {
  const [selectedAuditItem, setSelectedAuditItem] = useState<PredictionEvaluationDetail | null>(null)

  const t = {
    uz: {
      title: 'Prognoz va Natija Solishtiruv Jurnali (Evaluation Ledger)',
      subtitle: 'Har bir bashoratning haqiqiy natijasi, xatolik qiymati va operatsion konteksti auditi',
      allStatuses: 'Barcha holatlar',
      allTypes: 'Barcha turlar',
      targetCol: 'Nishon Vaqti',
      fieldCol: 'Dala',
      typeCol: 'Bashorat Turi',
      predictedCol: 'Prognoz',
      actualCol: 'Haqiqat',
      errorCol: 'Xato',
      resultCol: 'Xulosa / Natija',
      auditCol: 'Audit',
      noRecords: 'Filtrga mos baholash yozuvlari topilmadi.',
      prev: 'Oldingi',
      next: 'Keyingi',
      page: 'Sahifa',
      auditBtn: 'Batafsil',
    },
    ru: {
      title: 'Журнал Сравнения Прогнозов и Фактов (Evaluation Ledger)',
      subtitle: 'Аудит каждого прогноза: фактический результат, величина ошибки и операционный контекст',
      allStatuses: 'Все статусы',
      allTypes: 'Все типы',
      targetCol: 'Целевое Время',
      fieldCol: 'Поле',
      typeCol: 'Тип Прогноза',
      predictedCol: 'Прогноз',
      actualCol: 'Факт',
      errorCol: 'Ошибка',
      resultCol: 'Результат',
      auditCol: 'Аудит',
      noRecords: 'Записи оценки по заданным фильтрам не найдены.',
      prev: 'Назад',
      next: 'Вперед',
      page: 'Страница',
      auditBtn: 'Аудит',
    },
    en: {
      title: 'Prediction vs Reality Evaluation Ledger',
      subtitle: 'Immutable audit trail linking predictions with observed telemetry and operational events',
      allStatuses: 'All Statuses',
      allTypes: 'All Types',
      targetCol: 'Target Time',
      fieldCol: 'Field',
      typeCol: 'Forecast Type',
      predictedCol: 'Forecast',
      actualCol: 'Actual',
      errorCol: 'Error',
      resultCol: 'Outcome Result',
      auditCol: 'Audit',
      noRecords: 'No evaluation records matched the filter criteria.',
      prev: 'Previous',
      next: 'Next',
      page: 'Page',
      auditBtn: 'Audit Detail',
    },
  }[lang]

  const evaluations = data?.evaluations || []
  const totalPages = data?.totalPages || 1

  const getClassificationBadge = (res: PredictionClassificationResult, safety: boolean) => {
    if (safety || res === 'INTERRUPTED') {
      return (
        <Badge className="bg-purple-500/10 text-purple-600 dark:text-purple-400 border-purple-500/20 text-[10px] gap-1">
          <ShieldAlert className="h-3 w-3" />
          <span>INTERRUPTED</span>
        </Badge>
      )
    }

    switch (res) {
      case 'TRUE_POSITIVE':
        return <Badge className="bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20 text-[10px]">TRUE POSITIVE</Badge>
      case 'TRUE_NEGATIVE':
        return <Badge className="bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20 text-[10px]">TRUE NEGATIVE</Badge>
      case 'FALSE_POSITIVE':
        return <Badge className="bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20 text-[10px]">FALSE POSITIVE</Badge>
      case 'FALSE_NEGATIVE':
        return <Badge className="bg-rose-500/10 text-rose-600 dark:text-rose-400 border-rose-500/20 text-[10px]">FALSE NEGATIVE</Badge>
      default:
        return <Badge variant="outline" className="text-muted-foreground text-[10px]">EVALUATED</Badge>
    }
  }

  return (
    <Card className="border-border shadow-xs" data-testid="evaluation-history-table">
      <CardHeader className="pb-3 border-b border-border/60">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <History className="h-4 w-4 text-primary" />
              <CardTitle className="text-base font-bold">{t.title}</CardTitle>
            </div>
            <CardDescription className="text-xs text-muted-foreground">
              {t.subtitle}
            </CardDescription>
          </div>

          {/* Filters Bar */}
          <div className="flex items-center gap-2 flex-wrap">
            <select
              value={typeFilter}
              onChange={(e) => onTypeFilterChange(e.target.value)}
              className="text-xs h-8 px-2.5 rounded-md border border-input bg-background text-foreground"
            >
              <option value="">{t.allTypes}</option>
              <option value="SOIL_MOISTURE_FORECAST">SOIL_MOISTURE_FORECAST</option>
              <option value="IRRIGATION_NEED_FORECAST">IRRIGATION_NEED_FORECAST</option>
              <option value="WATER_DEMAND_FORECAST">WATER_DEMAND_FORECAST</option>
              <option value="WATER_AVAILABILITY_RISK">WATER_AVAILABILITY_RISK</option>
              <option value="IRRIGATION_PLAN_RISK">IRRIGATION_PLAN_RISK</option>
              <option value="SENSOR_RELIABILITY_FORECAST">SENSOR_RELIABILITY_FORECAST</option>
            </select>

            <select
              value={statusFilter}
              onChange={(e) => onStatusFilterChange(e.target.value)}
              className="text-xs h-8 px-2.5 rounded-md border border-input bg-background text-foreground"
            >
              <option value="">{t.allStatuses}</option>
              <option value="EVALUATED">EVALUATED</option>
              <option value="INSUFFICIENT_DATA">INSUFFICIENT_DATA</option>
              <option value="OUTCOME_INTERRUPTED">OUTCOME_INTERRUPTED</option>
              <option value="PENDING">PENDING</option>
            </select>
          </div>
        </div>
      </CardHeader>

      <CardContent className="pt-0 p-0">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader className="bg-muted/30">
              <TableRow className="text-xs">
                <TableHead className="font-semibold text-foreground">{t.targetCol}</TableHead>
                <TableHead className="font-semibold text-foreground">{t.fieldCol}</TableHead>
                <TableHead className="font-semibold text-foreground">{t.typeCol}</TableHead>
                <TableHead className="font-semibold text-foreground text-right">{t.predictedCol}</TableHead>
                <TableHead className="font-semibold text-foreground text-right">{t.actualCol}</TableHead>
                <TableHead className="font-semibold text-foreground text-right">{t.errorCol}</TableHead>
                <TableHead className="font-semibold text-foreground text-center">{t.resultCol}</TableHead>
                <TableHead className="font-semibold text-foreground text-center">{t.auditCol}</TableHead>
              </TableRow>
            </TableHeader>

            <TableBody>
              {evaluations.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} className="h-28 text-center text-xs text-muted-foreground">
                    {t.noRecords}
                  </TableCell>
                </TableRow>
              ) : (
                evaluations.map((item) => (
                  <TableRow
                    key={item.id}
                    className="hover:bg-muted/40 transition-colors text-xs"
                  >
                    <TableCell className="font-mono text-[11px] whitespace-nowrap">
                      {new Date(item.targetTime).toLocaleDateString()} {new Date(item.targetTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </TableCell>

                    <TableCell className="font-medium whitespace-nowrap">
                      {item.fieldName || item.fieldId.slice(0, 8)}
                    </TableCell>

                    <TableCell className="font-mono text-[11px]">
                      {item.predictionType}
                    </TableCell>

                    <TableCell className="text-right font-mono font-medium">
                      {item.predictedValue !== null && item.predictedValue !== undefined
                        ? item.predictedValue
                        : item.predictedClass || '—'}
                    </TableCell>

                    <TableCell className="text-right font-mono font-medium">
                      {item.actualValue !== null && item.actualValue !== undefined
                        ? item.actualValue
                        : item.actualClass || '—'}
                    </TableCell>

                    <TableCell className="text-right font-mono">
                      {item.absoluteError !== null ? (
                        <span className={item.directionalError && item.directionalError > 0 ? 'text-amber-600 dark:text-amber-400' : 'text-foreground'}>
                          {item.directionalError !== null && item.directionalError > 0 ? '+' : ''}
                          {item.directionalError !== null ? item.directionalError.toFixed(1) : item.absoluteError.toFixed(1)}
                        </span>
                      ) : (
                        '—'
                      )}
                    </TableCell>

                    <TableCell className="text-center">
                      {getClassificationBadge(item.classificationResult, item.safetyInterlockTriggered)}
                    </TableCell>

                    <TableCell className="text-center">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setSelectedAuditItem(item)}
                        className="h-6 px-2 text-[11px] gap-1 text-primary hover:text-primary font-semibold"
                        data-testid="view-evaluation-audit-button"
                      >
                        <ExternalLink className="h-3 w-3" />
                        <span>{t.auditBtn}</span>
                      </Button>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>

        {/* Pagination Footer */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between p-3 border-t border-border text-xs text-muted-foreground">
            <span>
              {t.page} {currentPage + 1} / {totalPages}
            </span>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => onPageChange(Math.max(0, currentPage - 1))}
                disabled={currentPage === 0 || isLoading}
                className="h-7 text-xs"
              >
                <ChevronLeft className="h-3.5 w-3.5 mr-1" />
                <span>{t.prev}</span>
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => onPageChange(Math.min(totalPages - 1, currentPage + 1))}
                disabled={currentPage >= totalPages - 1 || isLoading}
                className="h-7 text-xs"
              >
                <span>{t.next}</span>
                <ChevronRight className="h-3.5 w-3.5 ml-1" />
              </Button>
            </div>
          </div>
        )}
      </CardContent>

      {/* Audit Detail Modal */}
      <EvaluationDetailModal
        evaluation={selectedAuditItem}
        isOpen={!!selectedAuditItem}
        onClose={() => setSelectedAuditItem(null)}
        lang={lang}
      />
    </Card>
  )
}
