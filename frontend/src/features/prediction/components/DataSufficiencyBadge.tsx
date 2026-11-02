import React from 'react'
import { Badge } from '@/shared/ui/badge'
import { CheckCircle2, AlertTriangle, AlertOctagon } from 'lucide-react'
import { DataSufficiency } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface DataSufficiencyBadgeProps {
  sufficiency: DataSufficiency
  lang: SupportedLanguage
  className?: string
}

export const DataSufficiencyBadge: React.FC<DataSufficiencyBadgeProps> = ({
  sufficiency,
  lang,
  className = '',
}) => {
  const config = {
    SUFFICIENT: {
      color: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/20',
      icon: CheckCircle2,
      labels: {
        uz: "To'liq ma'lumot",
        ru: 'Полные данные',
        en: 'Sufficient Data',
      },
    },
    LIMITED: {
      color: 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/20',
      icon: AlertTriangle,
      labels: {
        uz: "Cheklangan ma'lumot",
        ru: 'Ограниченные данные',
        en: 'Limited Data',
      },
    },
    INSUFFICIENT: {
      color: 'bg-rose-500/10 text-rose-600 dark:text-rose-400 border-rose-500/20',
      icon: AlertOctagon,
      labels: {
        uz: "Yetarsiz ma'lumot",
        ru: 'Недостаточно данных',
        en: 'Insufficient Data',
      },
    },
  }[sufficiency]

  const Icon = config.icon

  return (
    <Badge
      variant="outline"
      className={`inline-flex items-center gap-1 text-[11px] font-medium ${config.color} ${className}`}
    >
      <Icon className="h-3 w-3" />
      <span>{config.labels[lang]}</span>
    </Badge>
  )
}
