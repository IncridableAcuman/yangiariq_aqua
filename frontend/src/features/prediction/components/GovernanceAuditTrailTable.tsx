import React from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import {
  History,
  ArrowRight,
} from 'lucide-react'
import { GovernanceAction, GovernanceAuditLog } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface GovernanceAuditTrailTableProps {
  logs: GovernanceAuditLog[]
  isLoading?: boolean
  lang: SupportedLanguage
}

export const GovernanceAuditTrailTable: React.FC<GovernanceAuditTrailTableProps> = ({
  logs,
  lang,
}) => {
  const t = {
    uz: {
      title: 'Boshqaruv va Qarorlar Audit Jurnali',
      subtitle: 'Kim, qachon, nima uchun yangi versiyani tasdiqlagani, faollashtirgani yoki qaytarganini to‘liq kuzatuvchi o‘zgarmas jurnal',
      timestamp: 'Vaqt',
      actor: 'Masʼul / Rol',
      action: 'Amal',
      target: 'Model / Versiya',
      transition: 'Holat O‘zgarishi',
      reason: 'Sabab va Asoslash',
      noLogs: 'Hozircha audit yozuvlari mavjud emas.',
    },
    ru: {
      title: 'Журнал Аудита Управления Моделями',
      subtitle: 'Неизменяемый реестр всех решений: кто, когда и почему одобрил, активировал или откатил версию модели',
      timestamp: 'Время',
      actor: 'Пользователь / Роль',
      action: 'Действие',
      target: 'Модель / Версия',
      transition: 'Смена Статуса',
      reason: 'Обоснование',
      noLogs: 'Записи аудита отсутствуют.',
    },
    en: {
      title: 'Governance & Model Audit Trail',
      subtitle: 'Immutable record tracing who approved, backtested, activated, or rolled back model versions and why',
      timestamp: 'Timestamp',
      actor: 'Actor / Role',
      action: 'Action',
      target: 'Target Model',
      transition: 'State Transition',
      reason: 'Reason & Justification',
      noLogs: 'No governance audit records logged yet.',
    },
  }[lang]

  const getActionBadge = (action: GovernanceAction) => {
    switch (action) {
      case 'VERSION_ACTIVATED':
        return <Badge className="bg-emerald-600 text-white text-[10px] font-semibold">ACTIVATED</Badge>
      case 'VERSION_ROLLBACK':
        return <Badge variant="destructive" className="text-[10px] font-bold">ROLLBACK</Badge>
      case 'PROPOSAL_APPROVED':
        return <Badge className="bg-blue-600 text-white text-[10px]">APPROVED</Badge>
      case 'PROPOSAL_REJECTED':
        return <Badge variant="outline" className="border-red-500 text-red-600 text-[10px]">REJECTED</Badge>
      case 'BACKTEST_EXECUTED':
        return <Badge variant="outline" className="border-primary text-primary text-[10px]">BACKTEST</Badge>
      case 'VERSION_SHADOW_STARTED':
        return <Badge variant="outline" className="border-purple-500 text-purple-600 text-[10px]">SHADOW</Badge>
      case 'VERSION_CANARY_STARTED':
        return <Badge variant="outline" className="border-cyan-500 text-cyan-600 text-[10px]">CANARY</Badge>
      case 'PROPOSAL_CREATED':
      default:
        return <Badge variant="secondary" className="text-[10px]">CREATED</Badge>
    }
  }

  return (
    <Card className="border bg-card/60 backdrop-blur-sm">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="text-base font-bold flex items-center gap-2">
              <History className="w-5 h-5 text-primary" />
              {t.title}
            </CardTitle>
            <CardDescription className="text-xs mt-1 text-muted-foreground">
              {t.subtitle}
            </CardDescription>
          </div>
          <Badge variant="outline" className="text-xs">
            {logs.length} {lang === 'uz' ? 'ta yozuv' : lang === 'ru' ? 'записей' : 'events'}
          </Badge>
        </div>
      </CardHeader>

      <CardContent>
        <div className="overflow-x-auto">
          <table className="w-full text-xs text-left">
            <thead className="bg-muted/40 uppercase text-[10px] text-muted-foreground font-semibold border-b">
              <tr>
                <th className="py-2.5 px-3">{t.timestamp}</th>
                <th className="py-2.5 px-3">{t.actor}</th>
                <th className="py-2.5 px-3">{t.action}</th>
                <th className="py-2.5 px-3">{t.target}</th>
                <th className="py-2.5 px-3">{t.transition}</th>
                <th className="py-2.5 px-3">{t.reason}</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {logs.length === 0 ? (
                <tr>
                  <td colSpan={6} className="text-center py-6 text-muted-foreground">
                    {t.noLogs}
                  </td>
                </tr>
              ) : (
                logs.map((log) => (
                  <tr key={log.id} className="hover:bg-muted/20 transition-colors">
                    <td className="py-3 px-3 text-muted-foreground whitespace-nowrap font-mono text-[11px]">
                      {log.timestamp ? new Date(log.timestamp).toLocaleString() : '—'}
                    </td>
                    <td className="py-3 px-3 whitespace-nowrap">
                      <div className="font-semibold text-foreground">{log.actorId}</div>
                      <div className="text-[10px] text-muted-foreground uppercase">{log.actorRole}</div>
                    </td>
                    <td className="py-3 px-3 whitespace-nowrap">
                      {getActionBadge(log.action)}
                    </td>
                    <td className="py-3 px-3">
                      <div className="font-medium text-foreground text-[11px]">
                        {log.predictionType?.replace(/_/g, ' ')}
                      </div>
                      {log.versionTag && (
                        <div className="font-mono text-[10px] text-primary">{log.versionTag}</div>
                      )}
                    </td>
                    <td className="py-3 px-3 whitespace-nowrap">
                      {log.previousState && log.newState ? (
                        <span className="flex items-center gap-1 font-mono text-[10px]">
                          <span className="text-muted-foreground">{log.previousState}</span>
                          <ArrowRight className="w-3 h-3 text-muted-foreground" />
                          <span className="text-foreground font-bold">{log.newState}</span>
                        </span>
                      ) : (
                        '—'
                      )}
                    </td>
                    <td className="py-3 px-3 text-muted-foreground text-[11px] max-w-xs">
                      {log.reason}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </CardContent>
    </Card>
  )
}
