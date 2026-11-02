import React from 'react'
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
import { History, CheckCircle2, XCircle, Clock } from 'lucide-react'
import { ForecastHistoryResponse, PredictionAccuracyStatus } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface ForecastHistoryTableProps {
  historyData?: ForecastHistoryResponse | null
  lang: SupportedLanguage
}

export const ForecastHistoryTable: React.FC<ForecastHistoryTableProps> = ({
  historyData,
  lang,
}) => {
  const t = {
    uz: {
      title: 'Prognozlar Tarixi va Aniqlik Baholash (Forecast Calibration & Accuracy)',
      subtitle: 'O‘tgan prognozlar qiymatini amaldagi o‘lchovlar bilan solishtirish',
      accuracySummary: 'Umumiy Model Aniqligi',
      verified: 'Tasdiqlangan to‘g‘ri',
      inaccurate: 'Noaniq / Og‘ishgan',
      pending: 'Kutilmoqda',
      insufficientNotice: 'Aniqlik ko‘rsatkichi mavjud emas: statistik kalibratsiya uchun yakunlangan prognozlar yetarli emas.',
      fieldCol: 'Dala',
      typeCol: 'Prognoz Turi',
      predictedCol: 'Prognoz Qiymati',
      actualCol: 'Haqiqiy Natija',
      accuracyCol: 'Holat',
      timeCol: 'Nishon Vaqti',
      noHistory: 'Hozircha saqlangan prognozlar tarixi mavjud emas.',
    },
    ru: {
      title: 'История Прогнозов и Оценка Точности (Forecast Calibration & Accuracy)',
      subtitle: 'Сравнение исторических прогнозов с фактическими измерениями',
      accuracySummary: 'Общая Точность Модели',
      verified: 'Подтверждено верно',
      inaccurate: 'Отклонение',
      pending: 'В ожидании',
      insufficientNotice: 'Оценка точности недоступна: недостаточно завершенных прогнозов для статистической калибровки.',
      fieldCol: 'Поле',
      typeCol: 'Тип Прогноза',
      predictedCol: 'Прогноз',
      actualCol: 'Факт',
      accuracyCol: 'Статус',
      timeCol: 'Целевое время',
      noHistory: 'История прогнозов отсутствует.',
    },
    en: {
      title: 'Forecast Calibration History & Accuracy Evaluation',
      subtitle: 'Comparing past predictive projections against measured operational outcomes',
      accuracySummary: 'Model Accuracy Score',
      verified: 'Verified Accurate',
      inaccurate: 'Inaccurate / Diverged',
      pending: 'Pending Target Window',
      insufficientNotice: 'Accuracy unavailable: insufficient completed forecasts for statistical calibration.',
      fieldCol: 'Field',
      typeCol: 'Prediction Type',
      predictedCol: 'Predicted Value',
      actualCol: 'Actual Outcome',
      accuracyCol: 'Status',
      timeCol: 'Target Horizon',
      noHistory: 'No prediction history recorded yet.',
    },
  }[lang]

  const statusBadge = (status: PredictionAccuracyStatus) => {
    switch (status) {
      case 'VERIFIED_ACCURATE':
        return (
          <Badge className="bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20 text-[10px] gap-1">
            <CheckCircle2 className="h-3 w-3" />
            <span>To‘g‘ri (Accurate)</span>
          </Badge>
        )
      case 'VERIFIED_INACCURATE':
        return (
          <Badge className="bg-rose-500/10 text-rose-600 dark:text-rose-400 border-rose-500/20 text-[10px] gap-1">
            <XCircle className="h-3 w-3" />
            <span>Og‘ishgan</span>
          </Badge>
        )
      case 'PENDING':
      default:
        return (
          <Badge variant="outline" className="text-muted-foreground text-[10px] gap-1">
            <Clock className="h-3 w-3" />
            <span>Kutilmoqda</span>
          </Badge>
        )
    }
  }

  const items = historyData?.history || []

  return (
    <Card className="border-border shadow-xs bg-card">
      <CardHeader className="pb-3 border-b border-border/60">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <CardTitle className="text-sm sm:text-base font-bold flex items-center gap-2">
              <History className="h-4 w-4 text-primary" />
              <span>{t.title}</span>
            </CardTitle>
            <CardDescription className="text-xs text-muted-foreground">
              {t.subtitle}
            </CardDescription>
          </div>

          {/* Accuracy Score Pill */}
          <div className="flex items-center gap-2">
            {historyData?.accuracyPercent !== null && historyData?.accuracyPercent !== undefined ? (
              <Badge className="bg-primary/10 text-primary border-primary/20 text-xs font-mono font-bold px-3 py-1">
                {t.accuracySummary}: {historyData.accuracyPercent}%
              </Badge>
            ) : (
              <Badge variant="outline" className="text-muted-foreground text-xs font-normal">
                {t.insufficientNotice}
              </Badge>
            )}
          </div>
        </div>
      </CardHeader>

      <CardContent className="p-0">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="text-[11px] text-muted-foreground bg-muted/20">
                <TableHead>{t.fieldCol}</TableHead>
                <TableHead>{t.typeCol}</TableHead>
                <TableHead>{t.predictedCol}</TableHead>
                <TableHead>{t.actualCol}</TableHead>
                <TableHead>{t.accuracyCol}</TableHead>
                <TableHead>{t.timeCol}</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {items.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} className="h-24 text-center text-xs text-muted-foreground">
                    {t.noHistory}
                  </TableCell>
                </TableRow>
              ) : (
                items.map((item) => (
                  <TableRow key={item.id} className="text-xs hover:bg-muted/30">
                    <TableCell className="font-semibold">{item.fieldName}</TableCell>
                    <TableCell className="font-mono text-[11px]">{item.predictionType.replace(/_/g, ' ')}</TableCell>
                    <TableCell className="font-mono">
                      {item.predictedValue !== null ? `${item.predictedValue} ${item.unit || ''}` : '—'}
                    </TableCell>
                    <TableCell className="font-mono">
                      {item.actualOutcomeValue !== null ? `${item.actualOutcomeValue} ${item.unit || ''}` : 'Kutilmoqda...'}
                    </TableCell>
                    <TableCell>{statusBadge(item.accuracyStatus)}</TableCell>
                    <TableCell className="text-muted-foreground text-[11px]">
                      {new Date(item.targetTime).toLocaleDateString()}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      </CardContent>
    </Card>
  )
}
