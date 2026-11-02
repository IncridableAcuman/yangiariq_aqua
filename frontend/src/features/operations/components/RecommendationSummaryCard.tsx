import { Sparkles, FileText, CheckCircle } from 'lucide-react'
import { RecommendationSummary } from '@/entities/operations'
import { SupportedLanguage } from '@/shared/i18n'

interface RecommendationSummaryCardProps {
  recommendation: RecommendationSummary | null
  lang: SupportedLanguage
  onPreparePlan?: () => void
}

export function RecommendationSummaryCard({
  recommendation,
  lang,
  onPreparePlan,
}: RecommendationSummaryCardProps) {
  if (!recommendation) {
    return (
      <div className="bg-card border border-border rounded-xl p-5 shadow-sm text-center py-8">
        <Sparkles className="h-8 w-8 text-muted-foreground/40 mx-auto mb-2" />
        <h3 className="text-sm font-semibold text-foreground">
          {lang === 'uz' ? 'Faol tavsiya yoʻq' : lang === 'ru' ? 'Нет активных рекомендаций' : 'No Active Recommendation'}
        </h3>
        <p className="text-xs text-muted-foreground mt-1">
          {lang === 'uz'
            ? 'Tuproq namligi va iqlim parametrlari meʼyorida.'
            : 'Влажность почвы и климатические параметры в норме.'}
        </p>
      </div>
    )
  }

  return (
    <div className="bg-card border border-border rounded-xl p-5 shadow-sm space-y-4">
      <div className="flex items-center justify-between border-b border-border pb-3">
        <div className="flex items-center space-x-2">
          <Sparkles className="h-4 w-4 text-primary" />
          <h2 className="text-base font-semibold text-foreground">
            {lang === 'uz'
              ? 'Agronomik Tavsiya (AI Advisory)'
              : lang === 'ru'
              ? 'Агрономическая рекомендация'
              : 'Agronomic Recommendation'}
          </h2>
        </div>
        <div className="flex items-center space-x-2">
          <span className="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-semibold bg-primary/10 text-primary border border-primary/20">
            {recommendation.confidence} {lang === 'uz' ? 'ishonch' : 'доверие'}
          </span>
          <span className="inline-flex items-center px-2 py-0.5 rounded text-[11px] font-semibold bg-amber-50 text-amber-700 dark:bg-amber-950/40 dark:text-amber-300 border border-amber-200">
            {recommendation.priority}
          </span>
        </div>
      </div>

      <div className="space-y-3">
        <div>
          <h3 className="text-sm font-semibold text-foreground">{recommendation.reason}</h3>
          {recommendation.aiExplanation && (
            <p className="text-xs text-muted-foreground mt-1 leading-relaxed">
              {recommendation.aiExplanation}
            </p>
          )}
        </div>

        {recommendation.evidence && recommendation.evidence.length > 0 && (
          <div className="p-3 bg-muted/30 rounded-lg space-y-1.5 border border-border/60">
            <span className="text-[11px] font-semibold uppercase tracking-wider text-muted-foreground">
              {lang === 'uz' ? 'Asoslovchi dalillar (Evidence):' : 'Обоснование:'}
            </span>
            <ul className="text-xs text-foreground/90 space-y-1">
              {recommendation.evidence.map((item, idx) => (
                <li key={idx} className="flex items-start">
                  <CheckCircle className="h-3.5 w-3.5 text-emerald-500 mr-1.5 shrink-0 mt-0.5" />
                  <span>{item}</span>
                </li>
              ))}
            </ul>
          </div>
        )}

        {onPreparePlan && (
          <div className="pt-2 flex justify-end">
            <button
              type="button"
              onClick={onPreparePlan}
              className="inline-flex items-center px-4 py-2 rounded-lg text-xs font-semibold bg-primary text-primary-foreground hover:bg-primary/90 transition-colors shadow-sm"
            >
              <FileText className="h-3.5 w-3.5 mr-1.5" />
              {lang === 'uz' ? 'Ushbu tavsiya asosida reja tuzish' : 'Создать план на основе рекомендации'}
            </button>
          </div>
        )}
      </div>
    </div>
  )
}
