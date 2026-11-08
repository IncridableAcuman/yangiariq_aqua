import React from 'react'
import { ShieldCheck, Link2, GitCommit, HelpCircle, Slash } from 'lucide-react'
import { AttributionLevel } from '@/entities/performance'

interface Props {
  level: AttributionLevel
  size?: 'sm' | 'md'
}

export const AttributionLevelBadge: React.FC<Props> = ({ level, size = 'md' }) => {
  const getConfig = () => {
    switch (level) {
      case 'DIRECTLY_VERIFIED':
        return {
          label: 'TOʻGʻRIDAN-TOʻGʻRI TASDIQLANGAN',
          icon: ShieldCheck,
          bg: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/30',
          desc: 'Mustaqil oʻlchov va dalillar bilan toʻliq isbotlangan sababiy taʼsir',
        }
      case 'STRONGLY_ASSOCIATED':
        return {
          label: 'KUCHLI BOGʻLIQLIK',
          icon: Link2,
          bg: 'bg-blue-500/10 text-blue-600 dark:text-blue-400 border-blue-500/30',
          desc: 'Operatsion va vaqt boʻyicha aniq korrelyatsiya mavjud, ammo tashqi omillar toʻliq chiqarib tashlanmagan',
        }
      case 'ASSOCIATED':
        return {
          label: 'BOGʻLIQ (KORRELYATSIYA)',
          icon: GitCommit,
          bg: 'bg-indigo-500/10 text-indigo-600 dark:text-indigo-400 border-indigo-500/30',
          desc: 'Natija yaxshilangan, biroq toʻgʻridan-toʻgʻri sababiyat tasdiqlanmagan',
        }
      case 'INCONCLUSIVE':
        return {
          label: 'NOANIQ (INCONCLUSIVE)',
          icon: HelpCircle,
          bg: 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/30',
          desc: 'Dalillar yetarli emas yoki qarama-qarshi koʻrsatkichlar mavjud',
        }
      case 'NOT_ASSESSABLE':
      default:
        return {
          label: 'BAHOLAB BOʻLMAYDI',
          icon: Slash,
          bg: 'bg-zinc-500/10 text-zinc-600 dark:text-zinc-400 border-zinc-500/30',
          desc: 'Tegishli baza yoki intervensiyadan keyingi maʼlumotlar yetishmaydi',
        }
    }
  }

  const config = getConfig()
  const Icon = config.icon
  const padding = size === 'sm' ? 'px-2 py-0.5 text-[10px]' : 'px-2.5 py-1 text-xs'

  return (
    <span
      title={config.desc}
      className={`inline-flex items-center gap-1.5 font-medium rounded-full border ${config.bg} ${padding}`}
    >
      <Icon className={size === 'sm' ? 'w-3 h-3' : 'w-3.5 h-3.5'} />
      <span>{config.label}</span>
    </span>
  )
}
