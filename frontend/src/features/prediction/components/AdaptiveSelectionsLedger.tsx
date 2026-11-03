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
  History,
  Compass,
  Info,
} from 'lucide-react'
import { AdaptiveSelectionDecision, ContextMatchLevel } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface AdaptiveSelectionsLedgerProps {
  decisions?: AdaptiveSelectionDecision[]
  isLoading?: boolean
  onExplainDecision?: (decision: AdaptiveSelectionDecision) => void
  onTestParcelSelection?: () => void
  lang: SupportedLanguage
}

export const AdaptiveSelectionsLedger: React.FC<AdaptiveSelectionsLedgerProps> = ({
  decisions = [],
  isLoading,
  onExplainDecision,
  onTestParcelSelection,
  lang,
}) => {
  const t = {
    uz: {
      title: 'Adaptiv Tanlovlar Jurnali (Audit Ledger)',
      subtitle: 'Har bir prognoz uchun bajarilgan deterministik tanlov qarorlari va ularning kontekstual asoslari',
      colParcel: 'Dala / Kontur',
      colType: 'Prognoz Turi',
      colSelectedVersion: 'Tanlangan Versiya',
      colMode: 'Tanlov Usuli',
      colConfidence: 'Ishonchlilik',
      colTime: 'Vaqti',
      colActions: 'Amallar',
      explainBtn: 'Nega bu usul?',
      testBtn: 'Dalada Sinov Tanlovi',
      emptyText: 'Hozircha saqlangan adaptiv tanlovlar mavjud emas.',
    },
    ru: {
      title: 'Журнал Контекстных Выборов (Аудит)',
      subtitle: 'Хронологический след детерминированных решений по выбору моделей и их контекстное обоснование',
      colParcel: 'Поле / Контур',
      colType: 'Тип Прогноза',
      colSelectedVersion: 'Выбранная Версия',
      colMode: 'Режим Выбора',
      colConfidence: 'Уверенность',
      colTime: 'Время',
      colActions: 'Действия',
      explainBtn: 'Почему этот метод?',
      testBtn: 'Протестировать Выбор',
      emptyText: 'Сохраненные адаптивные решения отсутствуют.',
    },
    en: {
      title: 'Adaptive Selections Audit Ledger',
      subtitle: 'Chronological audit trail of deterministic model selections with verifiable contextual rationale',
      colParcel: 'Parcel / Field',
      colType: 'Prediction Type',
      colSelectedVersion: 'Selected Version',
      colMode: 'Selection Mode',
      colConfidence: 'Confidence',
      colTime: 'Timestamp',
      colActions: 'Actions',
      explainBtn: 'Why this method?',
      testBtn: 'Test Selection on Parcel',
      emptyText: 'No adaptive selection decisions recorded yet.',
    },
  }[lang]

  const renderModeBadge = (mode: ContextMatchLevel) => {
    switch (mode) {
      case 'EXACT_CONTEXT':
        return (
          <Badge className="bg-emerald-500/15 text-emerald-700 dark:text-emerald-300 border-emerald-500/30 text-[10px] px-2 py-0.5">
            Exact Context
          </Badge>
        )
      case 'PARTIAL_CONTEXT':
        return (
          <Badge className="bg-blue-500/15 text-blue-700 dark:text-blue-300 border-blue-500/30 text-[10px] px-2 py-0.5">
            Partial Context
          </Badge>
        )
      case 'SIMILAR_CONTEXT':
        return (
          <Badge className="bg-purple-500/15 text-purple-700 dark:text-purple-300 border-purple-500/30 text-[10px] px-2 py-0.5">
            Similar Context
          </Badge>
        )
      case 'BASELINE_FALLBACK':
        return (
          <Badge variant="outline" className="border-amber-500/40 text-amber-700 dark:text-amber-300 bg-amber-500/10 text-[10px] px-2 py-0.5">
            Baseline Fallback
          </Badge>
        )
      case 'INSUFFICIENT_DATA':
        return (
          <Badge variant="outline" className="border-slate-500/40 text-muted-foreground bg-muted text-[10px] px-2 py-0.5">
            Insufficient Context
          </Badge>
        )
      default:
        return <Badge variant="secondary" className="text-[10px]">{mode}</Badge>
    }
  }

  return (
    <Card className="border-border/60 bg-card/60 backdrop-blur-sm">
      <CardHeader className="p-4 border-b border-border/40 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div>
          <CardTitle className="text-base font-semibold flex items-center gap-2">
            <History className="w-4 h-4 text-primary" />
            {t.title}
          </CardTitle>
          <CardDescription className="text-xs text-muted-foreground mt-0.5">
            {t.subtitle}
          </CardDescription>
        </div>

        {onTestParcelSelection && (
          <Button
            size="sm"
            variant="outline"
            onClick={onTestParcelSelection}
            className="text-xs h-8 border-primary/30 text-primary hover:bg-primary/10"
          >
            <Compass className="w-3.5 h-3.5 mr-1.5" />
            {t.testBtn}
          </Button>
        )}
      </CardHeader>

      <CardContent className="p-0">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader className="bg-muted/30">
              <TableRow>
                <TableHead className="text-xs font-semibold">{t.colParcel}</TableHead>
                <TableHead className="text-xs font-semibold">{t.colType}</TableHead>
                <TableHead className="text-xs font-semibold">{t.colSelectedVersion}</TableHead>
                <TableHead className="text-xs font-semibold text-center">{t.colMode}</TableHead>
                <TableHead className="text-xs font-semibold text-center">{t.colConfidence}</TableHead>
                <TableHead className="text-xs font-semibold">{t.colTime}</TableHead>
                <TableHead className="text-xs font-semibold text-right">{t.colActions}</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {isLoading ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center py-8 text-xs text-muted-foreground">
                    Loading selection audit ledger...
                  </TableCell>
                </TableRow>
              ) : decisions.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center py-8 text-xs text-muted-foreground">
                    {t.emptyText}
                  </TableCell>
                </TableRow>
              ) : (
                decisions.map((d) => (
                  <TableRow key={d.id} className="hover:bg-muted/20">
                    <TableCell className="text-xs py-3">
                      <div className="font-semibold text-foreground">{d.fieldName}</div>
                      <div className="font-mono text-[10px] text-muted-foreground">
                        {d.fieldId.substring(0, 8)}...
                      </div>
                    </TableCell>

                    <TableCell className="text-xs py-3">
                      <span className="font-medium text-foreground">{d.predictionType}</span>
                    </TableCell>

                    <TableCell className="text-xs py-3 font-mono">
                      <Badge variant="outline" className="border-primary/30 text-primary font-mono text-[11px] px-1.5 py-0">
                        {d.selectedVersionTag}
                      </Badge>
                    </TableCell>

                    <TableCell className="text-xs py-3 text-center">
                      {renderModeBadge(d.selectionMode)}
                    </TableCell>

                    <TableCell className="text-xs py-3 text-center font-mono">
                      <span className={`font-semibold ${
                        d.confidencePercent >= 80
                          ? 'text-emerald-600 dark:text-emerald-400'
                          : d.confidencePercent >= 60
                          ? 'text-amber-600 dark:text-amber-400'
                          : 'text-rose-600 dark:text-rose-400'
                      }`}>
                        {d.confidencePercent}%
                      </span>
                    </TableCell>

                    <TableCell className="text-xs py-3 text-muted-foreground">
                      {new Date(d.createdAt).toLocaleString(
                        lang === 'uz' ? 'uz-UZ' : lang === 'ru' ? 'ru-RU' : 'en-US',
                        {
                          month: 'short',
                          day: 'numeric',
                          hour: '2-digit',
                          minute: '2-digit',
                        }
                      )}
                    </TableCell>

                    <TableCell className="text-xs py-3 text-right">
                      {onExplainDecision && (
                        <Button
                          size="sm"
                          variant="ghost"
                          onClick={() => onExplainDecision(d)}
                          className="h-7 text-xs text-primary hover:bg-primary/10 px-2"
                        >
                          <Info className="w-3.5 h-3.5 mr-1" />
                          {t.explainBtn}
                        </Button>
                      )}
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
