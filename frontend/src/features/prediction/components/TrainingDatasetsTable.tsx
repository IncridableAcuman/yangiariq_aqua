import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import { Database } from 'lucide-react'
import { TrainingDatasetDto } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface TrainingDatasetsTableProps {
  datasets: TrainingDatasetDto[]
  isLoading?: boolean
  lang: SupportedLanguage
}

export const TrainingDatasetsTable: React.FC<TrainingDatasetsTableProps> = ({
  datasets,
  isLoading,
  lang,
}) => {
  const t = {
    uz: {
      title: 'O‘qitish Maʼlumotlar To‘plamlari Reyestri (Reproducibility)',
      subtitle: 'Har bir o‘qitish amali uchun saqlangan o‘zgarmas dataset snapshotlari va ularning sifati',
      snapshotId: 'Snapshot ID / Hash',
      type: 'Prognoz Turi',
      schema: 'Belgi Sxemasi',
      period: 'Vaqt Oralig‘i',
      samples: 'Tasdiqlangan Namunalar',
      quality: 'Sifat Holati',
      created: 'Yaratilgan',
      empty: 'Saqlangan o‘qitish to‘plamlari mavjud emas.',
    },
    ru: {
      title: 'Реестр Снапшотов Данных Обучения',
      subtitle: 'Воспроизводимые снапшоты исторических данных и оценки качества для каждой итерации',
      snapshotId: 'ID Снапшота / Хэш',
      type: 'Тип Прогноза',
      schema: 'Схема Признаков',
      period: 'Период',
      samples: 'Выборка',
      quality: 'Качество',
      created: 'Создан',
      empty: 'Снапшоты данных отсутствуют.',
    },
    en: {
      title: 'Training Dataset Snapshots (Reproducibility Registry)',
      subtitle: 'Immutable canonical dataset snapshots and data quality evaluations produced during model training',
      snapshotId: 'Snapshot ID / Hash',
      type: 'Prediction Type',
      schema: 'Feature Schema',
      period: 'Time Window',
      samples: 'Verified Records',
      quality: 'Quality Status',
      created: 'Created At',
      empty: 'No training dataset snapshots recorded yet.',
    },
  }[lang]

  return (
    <Card className="border border-border shadow-xs">
      <CardHeader className="p-4 sm:p-5 border-b border-border/50">
        <div className="flex items-center gap-2">
          <Database className="h-4 w-4 text-primary" />
          <CardTitle className="text-base font-bold tracking-tight text-foreground">
            {t.title}
          </CardTitle>
          <Badge variant="outline" className="text-[11px] font-mono">
            {datasets.length} snapshots
          </Badge>
        </div>
        <CardDescription className="text-xs text-muted-foreground">
          {t.subtitle}
        </CardDescription>
      </CardHeader>

      <CardContent className="p-0">
        {isLoading ? (
          <div className="py-10 text-center text-xs text-muted-foreground animate-pulse">
            Loading dataset snapshots...
          </div>
        ) : datasets.length === 0 ? (
          <div className="py-10 text-center text-xs text-muted-foreground">
            {t.empty}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs text-left">
              <thead className="bg-muted/40 text-muted-foreground text-[11px] font-semibold uppercase tracking-wider border-b border-border">
                <tr>
                  <th className="py-3 px-4">{t.snapshotId}</th>
                  <th className="py-3 px-3">{t.schema}</th>
                  <th className="py-3 px-3">{t.period}</th>
                  <th className="py-3 px-3 text-right">{t.samples}</th>
                  <th className="py-3 px-3">{t.quality}</th>
                  <th className="py-3 px-4 text-right">{t.created}</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/60 font-mono text-[11px]">
                {datasets.map((d) => (
                  <tr key={d.id} className="hover:bg-muted/20 transition-colors">
                    <td className="py-3 px-4">
                      <div className="font-bold text-foreground truncate max-w-[200px]" title={d.id}>
                        {d.id.substring(0, 8)}...
                      </div>
                      <div className="text-[10px] text-muted-foreground truncate max-w-[200px]" title={d.datasetHash}>
                        SHA: {d.datasetHash.substring(0, 16)}...
                      </div>
                    </td>

                    <td className="py-3 px-3 font-sans">
                      <Badge variant="outline" className="text-[10px]">
                        {d.featureSchemaVersion}
                      </Badge>
                    </td>

                    <td className="py-3 px-3 text-[10px] text-muted-foreground font-sans">
                      {new Date(d.periodStart).toLocaleDateString()} — {new Date(d.periodEnd).toLocaleDateString()}
                    </td>

                    <td className="py-3 px-3 text-right font-bold text-foreground">
                      {d.recordCount} obs
                    </td>

                    <td className="py-3 px-3 font-sans">
                      {d.qualityStatus === 'HIGH_QUALITY' || d.qualityStatus === 'ACCEPTABLE' ? (
                        <Badge variant="outline" className="bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/30 text-[10px]">
                          {d.qualityStatus}
                        </Badge>
                      ) : (
                        <Badge variant="outline" className="bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/30 text-[10px]">
                          {d.qualityStatus}
                        </Badge>
                      )}
                    </td>

                    <td className="py-3 px-4 text-right text-[10px] text-muted-foreground font-sans">
                      {new Date(d.createdAt).toLocaleDateString()} ({d.createdBy})
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
