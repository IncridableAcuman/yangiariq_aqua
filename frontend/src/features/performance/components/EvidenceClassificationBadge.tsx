import React from 'react'
import { CheckCircle2, Calculator, AlertTriangle, HelpCircle, XCircle } from 'lucide-react'
import { EvidenceClassification } from '@/entities/performance'

interface Props {
  classification: EvidenceClassification
  size?: 'sm' | 'md'
  showTooltip?: boolean
}

export const EvidenceClassificationBadge: React.FC<Props> = ({ classification, size = 'md' }) => {
  const getBadgeConfig = () => {
    switch (classification) {
      case 'OBSERVED':
        return {
          label: 'KUZATILGAN (OBSERVED)',
          icon: CheckCircle2,
          bg: 'bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-emerald-500/30',
          desc: 'Jismoniy datchik yoki operator dalolatnomasi bilan bevosita tasdiqlangan',
        }
      case 'CALCULATED':
        return {
          label: 'HISOBLANGAN (CALCULATED)',
          icon: Calculator,
          bg: 'bg-blue-500/10 text-blue-600 dark:text-blue-400 border-blue-500/30',
          desc: 'Kuzatilgan kirish va chiqish oqimlari asosida deterministik hisoblangan',
        }
      case 'ESTIMATED':
        return {
          label: 'BAHOLANGAN (ESTIMATED)',
          icon: HelpCircle,
          bg: 'bg-amber-500/10 text-amber-600 dark:text-amber-400 border-amber-500/30',
          desc: 'Statistik ekstrapolyatsiya yoki oraliq taxminlar orqali baholangan',
        }
      case 'SIMULATED':
        return {
          label: 'SIMULYATSIYA (SIMULATED)',
          icon: AlertTriangle,
          bg: 'bg-purple-500/10 text-purple-600 dark:text-purple-400 border-purple-500/30',
          desc: 'Gidravlik model (Saint-Venant) yoki ML natijasi. Haqiqiy kuzatuv emas!',
        }
      case 'MISSING':
      default:
        return {
          label: 'MAʼLUMOT YOʻQ (MISSING)',
          icon: XCircle,
          bg: 'bg-rose-500/10 text-rose-600 dark:text-rose-400 border-rose-500/30',
          desc: 'Ushbu oraliqda telemetriya datchigi uzilgan yoki oʻlchov kiritilmagan',
        }
    }
  }

  const config = getBadgeConfig()
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
