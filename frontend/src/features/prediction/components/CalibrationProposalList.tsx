import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import {
  FileText,
  ArrowRight,
  TrendingUp,
  ShieldCheck,
  Sparkles,
} from 'lucide-react'
import { CalibrationProposal, GovernanceRiskLevel, ProposalStatus } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface CalibrationProposalListProps {
  proposals: CalibrationProposal[]
  isLoading?: boolean
  onSelectProposal: (proposalId: string) => void
  lang: SupportedLanguage
}

export const CalibrationProposalList: React.FC<CalibrationProposalListProps> = ({
  proposals,
  onSelectProposal,
  lang,
}) => {
  const t = {
    uz: {
      title: 'Kalibratsiya Takliflari va Tarixiy Natijalar',
      subtitle: 'Xatolik va drift asosida tayyorlangan parametr o‘zgarishlari, orqa test (Backtest) va inson ko‘rigi',
      proposalCode: 'Taklif Kodi',
      predictionType: 'Model Turi',
      transition: 'Versiya O‘tishi',
      reason: 'Sabab va Asos',
      expectedEffect: 'Kutilayotgan Taʼsir',
      risk: 'Xavf Darajasi',
      status: 'Holati',
      actions: 'Amal',
      reviewBtn: 'Taqqoslash va Baholash',
      noProposals: 'Hozircha kalibratsiya takliflari mavjud emas. Barcha modellar belgilangan talablarga muvofiq ishlamoqda.',
      lowRisk: 'Past Xavf',
      mediumRisk: 'O‘rtacha',
      highRisk: 'Yuqori Xavf',
      statusDraft: 'Qoralama',
      statusBacktesting: 'Test qilinmoqda',
      statusReview: 'Ko‘rib chiqish zarur',
      statusApproved: 'Tasdiqlangan',
      statusRejected: 'Rad etilgan',
      statusActive: 'Faol (Ishlab chiqarishda)',
      statusRolledBack: 'Qaytarilgan (Rolled back)',
    },
    ru: {
      title: 'Предложения по Калибровке и Исторические Бэктесты',
      subtitle: 'Подготовленные изменения параметров на основе ошибок и дрейфа, бэктесты и экспертная оценка',
      proposalCode: 'Код Предложения',
      predictionType: 'Тип Модели',
      transition: 'Смена Версии',
      reason: 'Причина и Данные',
      expectedEffect: 'Ожидаемый Эффект',
      risk: 'Уровень Риска',
      status: 'Статус',
      actions: 'Действие',
      reviewBtn: 'Сравнить и Оценить',
      noProposals: 'Предложения по калибровке отсутствуют. Все модели функционируют стабильно.',
      lowRisk: 'Низкий Риск',
      mediumRisk: 'Средний Риск',
      highRisk: 'Высокий Риск',
      statusDraft: 'Черновик',
      statusBacktesting: 'Тестирование',
      statusReview: 'Требует проверки',
      statusApproved: 'Одобрено',
      statusRejected: 'Отклонено',
      statusActive: 'Активно в прод',
      statusRolledBack: 'Откачено',
    },
    en: {
      title: 'Calibration Proposals & Historical Backtests',
      subtitle: 'Data-driven parameter tuning proposals based on error drift, historical simulation, and human sign-off',
      proposalCode: 'Proposal Code',
      predictionType: 'Model Type',
      transition: 'Version Transition',
      reason: 'Issue & Evidence',
      expectedEffect: 'Expected Effect',
      risk: 'Risk Level',
      status: 'Status',
      actions: 'Action',
      reviewBtn: 'Compare & Review',
      noProposals: 'No calibration proposals recorded. All predictive models are operating within normal tolerances.',
      lowRisk: 'Low Risk',
      mediumRisk: 'Medium Risk',
      highRisk: 'High Risk',
      statusDraft: 'Draft',
      statusBacktesting: 'Backtesting',
      statusReview: 'Review Required',
      statusApproved: 'Approved',
      statusRejected: 'Rejected',
      statusActive: 'Active in Prod',
      statusRolledBack: 'Rolled Back',
    },
  }[lang]

  const getRiskBadge = (risk: GovernanceRiskLevel) => {
    switch (risk) {
      case 'LOW':
        return <Badge variant="outline" className="border-emerald-500/40 text-emerald-600 dark:text-emerald-400 bg-emerald-500/10 text-[11px]">{t.lowRisk}</Badge>
      case 'MEDIUM':
        return <Badge variant="outline" className="border-amber-500/40 text-amber-600 dark:text-amber-400 bg-amber-500/10 text-[11px]">{t.mediumRisk}</Badge>
      case 'HIGH':
      default:
        return <Badge variant="destructive" className="text-[11px]">{t.highRisk}</Badge>
    }
  }

  const getStatusBadge = (status: ProposalStatus) => {
    switch (status) {
      case 'DRAFT':
        return <Badge variant="secondary" className="text-[11px]">{t.statusDraft}</Badge>
      case 'BACKTESTING':
        return <Badge variant="outline" className="border-blue-500 text-blue-600 bg-blue-500/10 text-[11px] animate-pulse">{t.statusBacktesting}</Badge>
      case 'REVIEW_REQUIRED':
        return <Badge variant="outline" className="border-amber-500 text-amber-600 dark:text-amber-400 bg-amber-500/10 text-[11px] font-semibold">{t.statusReview}</Badge>
      case 'APPROVED':
        return <Badge className="bg-emerald-600 text-white text-[11px] font-semibold">{t.statusApproved}</Badge>
      case 'REJECTED':
        return <Badge variant="destructive" className="text-[11px]">{t.statusRejected}</Badge>
      case 'ACTIVE':
        return <Badge className="bg-emerald-700 text-white text-[11px] font-bold">{t.statusActive}</Badge>
      case 'ROLLED_BACK':
        return <Badge variant="outline" className="border-red-500 text-red-600 bg-red-500/10 text-[11px]">{t.statusRolledBack}</Badge>
      default:
        return <Badge variant="secondary" className="text-[11px]">{status}</Badge>
    }
  }

  return (
    <Card className="border bg-card/60 backdrop-blur-sm">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="text-lg font-bold flex items-center gap-2">
              <FileText className="w-5 h-5 text-primary" />
              {t.title}
            </CardTitle>
            <CardDescription className="text-xs mt-1 text-muted-foreground">
              {t.subtitle}
            </CardDescription>
          </div>
          <Badge variant="outline" className="text-xs">
            {proposals.length} {lang === 'uz' ? 'ta taklif' : lang === 'ru' ? 'предложений' : 'proposals'}
          </Badge>
        </div>
      </CardHeader>

      <CardContent>
        {proposals.length === 0 ? (
          <div className="text-center py-8 text-muted-foreground border border-dashed rounded-lg p-6">
            <ShieldCheck className="w-8 h-8 text-emerald-500 mx-auto mb-2 opacity-80" />
            <p className="text-xs font-medium">{t.noProposals}</p>
          </div>
        ) : (
          <div className="space-y-3">
            {proposals.map((prop) => (
              <div
                key={prop.id}
                className="p-4 rounded-lg border bg-muted/15 hover:bg-muted/25 transition-all flex flex-col md:flex-row md:items-center justify-between gap-4"
              >
                <div className="space-y-2 flex-1">
                  <div className="flex items-center gap-2 flex-wrap">
                    <span className="font-mono text-xs font-bold text-primary">
                      {prop.proposalCode}
                    </span>
                    <Badge variant="outline" className="text-[11px]">
                      {prop.predictionType.replace(/_/g, ' ')}
                    </Badge>
                    {getStatusBadge(prop.status)}
                    {getRiskBadge(prop.riskLevel)}
                  </div>

                  <div className="flex items-center gap-1.5 text-xs text-foreground font-medium">
                    <span className="font-mono bg-muted/60 px-1.5 py-0.5 rounded border border-muted text-[11px]">
                      {prop.currentVersionTag}
                    </span>
                    <ArrowRight className="w-3.5 h-3.5 text-muted-foreground" />
                    <span className="font-mono bg-primary/10 text-primary px-1.5 py-0.5 rounded border border-primary/20 text-[11px] font-semibold">
                      {prop.candidateVersionTag}
                    </span>
                  </div>

                  <p className="text-xs text-muted-foreground line-clamp-2">
                    <strong className="text-foreground font-medium">{prop.reason}</strong>
                    {prop.evidence && ` — ${prop.evidence}`}
                  </p>

                  <div className="text-[11px] text-emerald-600 dark:text-emerald-400 flex items-center gap-1.5">
                    <Sparkles className="w-3.5 h-3.5 shrink-0" />
                    <span>{prop.expectedEffect}</span>
                  </div>
                </div>

                <div className="flex items-center gap-2 self-end md:self-center shrink-0">
                  <Button
                    variant="default"
                    size="sm"
                    onClick={() => onSelectProposal(prop.id)}
                    className="text-xs flex items-center gap-1.5"
                  >
                    <TrendingUp className="w-3.5 h-3.5" />
                    {t.reviewBtn}
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  )
}
