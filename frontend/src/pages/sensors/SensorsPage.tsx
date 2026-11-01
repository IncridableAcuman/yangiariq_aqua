import React from 'react'
import { Activity } from 'lucide-react'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { FieldTelemetryDashboard } from '@/features/sensor/analytics/FieldTelemetryDashboard'

interface SensorsPageProps {
  lang: SupportedLanguage
}

export const SensorsPage: React.FC<SensorsPageProps> = ({ lang }) => {
  const t = translations[lang]

  return (
    <div className="space-y-6 animate-in fade-in duration-300">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-border/80 pb-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground flex items-center gap-2.5">
            <Activity className="w-6 h-6 text-primary" />
            <span>{t.telemetry.title}</span>
          </h1>
          <p className="text-xs sm:text-sm text-muted-foreground mt-1">
            {t.telemetry.subtitle}
          </p>
        </div>
      </div>

      {/* Main Interactive Field Telemetry Dashboard */}
      <FieldTelemetryDashboard lang={lang} />
    </div>
  )
}
