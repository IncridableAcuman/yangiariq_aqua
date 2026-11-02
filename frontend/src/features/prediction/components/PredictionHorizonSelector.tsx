import React from 'react'
import { Button } from '@/shared/ui/button'
import { Clock, Calendar, Compass } from 'lucide-react'
import { PredictionHorizon } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface PredictionHorizonSelectorProps {
  horizon: PredictionHorizon
  onHorizonChange: (horizon: PredictionHorizon) => void
  lang: SupportedLanguage
}

export const PredictionHorizonSelector: React.FC<PredictionHorizonSelectorProps> = ({
  horizon,
  onHorizonChange,
  lang,
}) => {
  const labels = {
    uz: {
      NEXT_24_HOURS: 'Keyingi 24 soat',
      NEXT_48_HOURS: 'Keyingi 48 soat (Asosiy)',
      NEXT_7_DAYS: 'Keyingi 7 kun (Haftalik)',
    },
    ru: {
      NEXT_24_HOURS: 'Следующие 24 часа',
      NEXT_48_HOURS: 'Следующие 48 часов (Базовый)',
      NEXT_7_DAYS: 'Следующие 7 дней (Неделя)',
    },
    en: {
      NEXT_24_HOURS: 'Next 24 Hours',
      NEXT_48_HOURS: 'Next 48 Hours (Primary)',
      NEXT_7_DAYS: 'Next 7 Days (Weekly)',
    },
  }[lang]

  const horizons: { id: PredictionHorizon; icon: typeof Clock }[] = [
    { id: 'NEXT_24_HOURS', icon: Clock },
    { id: 'NEXT_48_HOURS', icon: Compass },
    { id: 'NEXT_7_DAYS', icon: Calendar },
  ]

  return (
    <div className="flex items-center gap-1.5 p-1 bg-muted/60 rounded-xl border border-border/60">
      {horizons.map(({ id, icon: Icon }) => {
        const isActive = horizon === id
        return (
          <Button
            key={id}
            variant={isActive ? 'default' : 'ghost'}
            size="sm"
            onClick={() => onHorizonChange(id)}
            className={`gap-1.5 text-xs font-medium rounded-lg transition-all ${
              isActive
                ? 'shadow-xs bg-primary text-primary-foreground'
                : 'text-muted-foreground hover:text-foreground'
            }`}
          >
            <Icon className="h-3.5 w-3.5" />
            <span>{labels[id]}</span>
          </Button>
        )
      })}
    </div>
  )
}
