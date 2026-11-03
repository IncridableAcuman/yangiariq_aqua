import React from 'react'
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
  MapPin,
  ChevronLeft,
  ChevronRight,
} from 'lucide-react'
import {
  FieldEvaluationResponse,
  ModelHealthStatus,
} from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface FieldEvaluationTableProps {
  data?: FieldEvaluationResponse | null
  isLoading?: boolean
  currentPage: number
  onPageChange: (newPage: number) => void
  onSelectField?: (fieldId: string) => void
  lang: SupportedLanguage
}

export const FieldEvaluationTable: React.FC<FieldEvaluationTableProps> = ({
  data,
  isLoading,
  currentPage,
  onPageChange,
  onSelectField,
  lang,
}) => {
  const t = {
    uz: {
      title: 'Dalalar Bo‘yicha Aniqlik va Model Salomatligi',
      subtitle: 'Har bir kontur va ekin turining prognoz qilinish barqarorligi',
      fieldCol: 'Dala va Ekin',
      evalsCol: 'Namunalar (n)',
      moistureMaeCol: 'Namlik MAE (%)',
      demandMaeCol: 'Suv Talabi MAE (m³)',
      needAccCol: 'Sug‘orish Aniqligi',
      safetyCol: 'Gidravlik Blok',
      qualityCol: 'Datchik Holati',
      healthCol: 'Salomatlik',
      limitedNotice: 'kam namuna (n < 5)',
      noData: 'Dalalar bo‘yicha baholangan ma’lumotlar mavjud emas.',
      prev: 'Oldingi',
      next: 'Keyingi',
      page: 'Sahifa',
    },
    ru: {
      title: 'Точность и Здоровье Моделей по Полям',
      subtitle: 'Стабильность прогнозов по каждому полю и сельскохозяйственной культуре',
      fieldCol: 'Поле и Культура',
      evalsCol: 'Выборка (n)',
      moistureMaeCol: 'MAE Влажности (%)',
      demandMaeCol: 'MAE Водопотребности (м³)',
      needAccCol: 'Точность Полива',
      safetyCol: 'Блок Защиты',
      qualityCol: 'Телеметрия',
      healthCol: 'Статус',
      limitedNotice: 'мало данных (n < 5)',
      noData: 'Нет данных оценки по полям.',
      prev: 'Назад',
      next: 'Вперед',
      page: 'Страница',
    },
    en: {
      title: 'Parcel-Level Accuracy & Model Health Breakdown',
      subtitle: 'Verifying predictive stability across individual parcels and crop varieties',
      fieldCol: 'Field & Crop',
      evalsCol: 'Evaluations (n)',
      moistureMaeCol: 'Moisture MAE (%)',
      demandMaeCol: 'Water Demand MAE (m³)',
      needAccCol: 'Irrigation Need Acc.',
      safetyCol: 'Safety Interlocks',
      qualityCol: 'Sensor Quality',
      healthCol: 'Model Health',
      limitedNotice: 'limited (n < 5)',
      noData: 'No field-level evaluation records available.',
      prev: 'Previous',
      next: 'Next',
      page: 'Page',
    },
  }[lang]

  const fields = data?.fields || []
  const totalPages = data?.totalPages || 1

  const getHealthBadge = (health: ModelHealthStatus, hasSufficient: boolean) => {
    if (!hasSufficient) {
      return (
        <Badge variant="outline" className="text-[10px] text-muted-foreground border-dashed">
          {t.limitedNotice}
        </Badge>
      )
    }

    switch (health) {
      case 'HEALTHY':
        return (
          <Badge className="bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20 text-[10px]">
            HEALTHY
          </Badge>
        )
      case 'WATCH':
        return (
          <Badge className="bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20 text-[10px]">
            WATCH
          </Badge>
        )
      case 'DEGRADED':
        return (
          <Badge className="bg-rose-500/10 text-rose-600 dark:text-rose-400 border-rose-500/20 text-[10px]">
            DEGRADED
          </Badge>
        )
      default:
        return (
          <Badge variant="outline" className="text-[10px] text-muted-foreground">
            INSUFFICIENT
          </Badge>
        )
    }
  }

  return (
    <Card className="border-border shadow-xs" data-testid="field-evaluation-table">
      <CardHeader className="pb-3 border-b border-border/60">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <MapPin className="h-4 w-4 text-primary" />
              <CardTitle className="text-base font-bold">{t.title}</CardTitle>
            </div>
            <CardDescription className="text-xs text-muted-foreground">
              {t.subtitle}
            </CardDescription>
          </div>
        </div>
      </CardHeader>

      <CardContent className="pt-0 p-0">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader className="bg-muted/30">
              <TableRow className="text-xs">
                <TableHead className="font-semibold text-foreground">{t.fieldCol}</TableHead>
                <TableHead className="font-semibold text-foreground text-center">{t.evalsCol}</TableHead>
                <TableHead className="font-semibold text-foreground text-right">{t.moistureMaeCol}</TableHead>
                <TableHead className="font-semibold text-foreground text-right">{t.demandMaeCol}</TableHead>
                <TableHead className="font-semibold text-foreground text-center">{t.needAccCol}</TableHead>
                <TableHead className="font-semibold text-foreground text-center">{t.safetyCol}</TableHead>
                <TableHead className="font-semibold text-foreground text-center">{t.qualityCol}</TableHead>
                <TableHead className="font-semibold text-foreground text-center">{t.healthCol}</TableHead>
              </TableRow>
            </TableHeader>

            <TableBody>
              {fields.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={8} className="h-28 text-center text-xs text-muted-foreground">
                    {t.noData}
                  </TableCell>
                </TableRow>
              ) : (
                fields.map((field) => (
                  <TableRow
                    key={field.fieldId}
                    className="hover:bg-muted/40 transition-colors text-xs cursor-pointer"
                    onClick={() => onSelectField && onSelectField(field.fieldId)}
                  >
                    <TableCell className="font-medium">
                      <div className="flex items-center gap-1.5">
                        <span className="font-semibold text-foreground">{field.fieldName}</span>
                        <span className="text-[11px] text-muted-foreground font-normal">
                          ({field.cropType})
                        </span>
                      </div>
                    </TableCell>

                    <TableCell className="text-center font-mono font-semibold">
                      {field.totalEvaluations}
                    </TableCell>

                    <TableCell className="text-right font-mono">
                      {typeof field.soilMoistureMae === 'number' ? `${field.soilMoistureMae.toFixed(1)}%` : '—'}
                    </TableCell>

                    <TableCell className="text-right font-mono">
                      {typeof field.waterDemandMaeM3 === 'number' ? `${field.waterDemandMaeM3.toFixed(1)} m³` : '—'}
                    </TableCell>

                    <TableCell className="text-center font-mono">
                      {typeof field.irrigationNeedAccuracyPercent === 'number'
                        ? `${field.irrigationNeedAccuracyPercent.toFixed(1)}%`
                        : '—'}
                    </TableCell>

                    <TableCell className="text-center">
                      {field.interruptedCount > 0 ? (
                        <Badge variant="outline" className="text-rose-600 dark:text-rose-400 border-rose-500/30 text-[10px] font-mono">
                          {field.interruptedCount}
                        </Badge>
                      ) : (
                        <span className="text-muted-foreground font-mono">0</span>
                      )}
                    </TableCell>

                    <TableCell className="text-center">
                      <Badge variant="outline" className="text-[10px] font-mono">
                        {field.dataQualityStatus}
                      </Badge>
                    </TableCell>

                    <TableCell className="text-center">
                      {getHealthBadge(field.healthStatus, field.hasSufficientData)}
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
    </Card>
  )
}
