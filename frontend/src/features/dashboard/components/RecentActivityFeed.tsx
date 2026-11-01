import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import {
  Activity,
  AlertTriangle,
  CalendarCheck2,
  CheckCircle2,
  Sparkles,
} from 'lucide-react'
import { RecentActivity } from '@/entities/dashboard'
import { SupportedLanguage } from '@/shared/i18n'

interface RecentActivityFeedProps {
  activity: RecentActivity[]
  lang: SupportedLanguage
}

export const RecentActivityFeed: React.FC<RecentActivityFeedProps> = ({
  activity,
  lang,
}) => {
  const t = {
    uz: {
      title: 'Soʻnggi Hodisalar Oqimi (Activity Stream)',
      subtitle: 'Tizimda qayd etilgan real vaqt gidravlik va agronomik amallar',
      empty: 'Soʻnggi 24 soat ichida favqulodda hodisalar qayd etilmadi',
      field: 'Dala',
    },
    ru: {
      title: 'Лента Последних Событий (Activity Stream)',
      subtitle: 'Регистрируемые в реальном времени гидравлические и агрономические события',
      empty: 'За последние 24 часа событий не зафиксировано',
      field: 'Поле',
    },
    en: {
      title: 'Operational Activity Stream',
      subtitle: 'Real-time timeline of agronomic, telemetry, and irrigation events',
      empty: 'No activity recorded in the past 24 hours',
      field: 'Field',
    },
  }[lang]

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'ANOMALY_DETECTED':
        return <AlertTriangle className="w-4 h-4 text-rose-500" />
      case 'RECOMMENDATION_GENERATED':
        return <Sparkles className="w-4 h-4 text-primary" />
      case 'PLAN_APPROVED':
      case 'PLAN_SCHEDULED':
        return <CalendarCheck2 className="w-4 h-4 text-sky-500" />
      case 'PLAN_COMPLETED':
        return <CheckCircle2 className="w-4 h-4 text-emerald-500" />
      default:
        return <Activity className="w-4 h-4 text-muted-foreground" />
    }
  }

  const formatRelativeTime = (timestamp: string) => {
    try {
      const date = new Date(timestamp)
      const now = new Date()
      const diffMinutes = Math.floor((now.getTime() - date.getTime()) / (1000 * 60))

      if (diffMinutes < 1) return lang === 'uz' ? 'Hozirgina' : lang === 'ru' ? 'Только что' : 'Just now'
      if (diffMinutes < 60) return `${diffMinutes} ${lang === 'uz' ? 'daq avval' : lang === 'ru' ? 'мин назад' : 'm ago'}`
      const diffHours = Math.floor(diffMinutes / 60)
      if (diffHours < 24) return `${diffHours} ${lang === 'uz' ? 'soat avval' : lang === 'ru' ? 'ч назад' : 'h ago'}`
      return date.toLocaleDateString()
    } catch {
      return timestamp
    }
  }

  return (
    <Card className="shadow-xs border-border/80">
      <CardHeader className="pb-3 border-b border-border/60">
        <CardTitle className="text-base font-bold text-foreground flex items-center gap-2">
          <Activity className="w-5 h-5 text-primary" />
          <span>{t.title}</span>
        </CardTitle>
        <CardDescription className="text-xs">
          {t.subtitle}
        </CardDescription>
      </CardHeader>

      <CardContent className="p-0">
        {activity.length === 0 ? (
          <div className="p-8 text-center text-xs text-muted-foreground">
            {t.empty}
          </div>
        ) : (
          <div className="divide-y divide-border/60 max-h-96 overflow-y-auto">
            {activity.map((item) => (
              <div key={item.id} className="p-3.5 hover:bg-muted/30 transition-colors flex items-start gap-3 text-xs">
                <div className="mt-0.5 p-1.5 rounded-md bg-muted/60 shrink-0">
                  {getActivityIcon(item.activityType)}
                </div>

                <div className="space-y-0.5 min-w-0 flex-1">
                  <div className="flex items-center justify-between gap-2">
                    <span className="font-semibold text-foreground truncate">
                      {item.title}
                    </span>
                    <span className="text-[10px] text-muted-foreground font-mono shrink-0">
                      {formatRelativeTime(item.timestamp)}
                    </span>
                  </div>

                  <p className="text-muted-foreground leading-relaxed line-clamp-2 text-[11px]">
                    {item.description}
                  </p>

                  {item.fieldName && (
                    <div className="pt-0.5">
                      <Badge variant="outline" className="text-[10px] py-0 px-1 font-mono">
                        {t.field}: {item.fieldName}
                      </Badge>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  )
}
