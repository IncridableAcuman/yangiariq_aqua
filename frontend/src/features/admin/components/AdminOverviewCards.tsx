import React from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import { Users, UserCheck, UserX, ShieldAlert, ShieldCheck, Cpu, Database, Key } from 'lucide-react'
import { UserSummaryStats, SystemSettingsDto } from '@/entities/admin'
import { SupportedLang, translations } from '@/shared/i18n'

interface AdminOverviewCardsProps {
  stats: UserSummaryStats | null | undefined
  settings: SystemSettingsDto | null | undefined
  lang: SupportedLang
  isLoading?: boolean
}

export const AdminOverviewCards: React.FC<AdminOverviewCardsProps> = ({
  stats,
  settings,
  lang,
  isLoading,
}) => {
  const t = translations[lang].admin

  if (isLoading) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 animate-pulse">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="h-28 bg-muted rounded-xl" />
        ))}
      </div>
    )
  }

  const statItems = [
    {
      title: t.stats.totalUsers,
      value: stats?.totalUsers ?? 0,
      icon: Users,
      color: 'text-primary',
      bgColor: 'bg-primary/10',
      description: `${t.stats.activeUsers}: ${stats?.activeUsers ?? 0}`,
    },
    {
      title: t.stats.superAdmins,
      value: stats?.superAdminsCount ?? 0,
      icon: ShieldAlert,
      color: 'text-rose-600 dark:text-rose-400',
      bgColor: 'bg-rose-500/10',
      description: `${t.stats.admins}: ${stats?.adminsCount ?? 0}`,
    },
    {
      title: t.stats.operators,
      value: stats?.operatorsCount ?? 0,
      icon: UserCheck,
      color: 'text-sky-600 dark:text-sky-400',
      bgColor: 'bg-sky-500/10',
      description: `${t.stats.farmers}: ${stats?.farmersCount ?? 0}`,
    },
    {
      title: t.stats.blockedUsers,
      value: (stats?.blockedUsers ?? 0) + (stats?.inactiveUsers ?? 0),
      icon: UserX,
      color: 'text-amber-600 dark:text-amber-400',
      bgColor: 'bg-amber-500/10',
      description: `${t.stats.inactiveUsers}: ${stats?.inactiveUsers ?? 0} | ${t.stats.blockedUsers}: ${stats?.blockedUsers ?? 0}`,
    },
  ]

  return (
    <div className="space-y-4">
      {/* Primary KPI Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {statItems.map((item, index) => {
          const Icon = item.icon
          return (
            <Card key={index} className="border shadow-xs hover:shadow-sm transition-shadow">
              <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
                <CardTitle className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                  {item.title}
                </CardTitle>
                <div className={`p-2 rounded-lg ${item.bgColor}`}>
                  <Icon className={`w-4 h-4 ${item.color}`} />
                </div>
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold tracking-tight">{item.value}</div>
                <p className="text-xs text-muted-foreground mt-1 truncate">
                  {item.description}
                </p>
              </CardContent>
            </Card>
          )
        })}
      </div>

      {/* Security Governance Quick Indicators */}
      {settings && (
        <Card className="border shadow-xs bg-muted/30">
          <CardContent className="p-4">
            <div className="flex flex-wrap items-center justify-between gap-4 text-xs">
              <div className="flex items-center gap-2">
                <ShieldCheck className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
                <span className="font-medium text-foreground">RBAC Enforced:</span>
                <Badge variant="outline" className="bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border-emerald-300">
                  30 Centralized Permissions
                </Badge>
              </div>

              <div className="flex items-center gap-2">
                <Key className="w-4 h-4 text-sky-600 dark:text-sky-400" />
                <span className="font-medium text-foreground">JWT Session:</span>
                <span className="font-mono text-muted-foreground">{settings.accessTokenExpirationMinutes} min</span>
              </div>

              <div className="flex items-center gap-2">
                <Database className="w-4 h-4 text-purple-600 dark:text-purple-400" />
                <span className="font-medium text-foreground">Audit Logging:</span>
                <Badge variant="outline" className="bg-purple-500/10 text-purple-700 dark:text-purple-400 border-purple-300">
                  ACTIVE (Immutable Trail)
                </Badge>
              </div>

              <div className="flex items-center gap-2">
                <Cpu className="w-4 h-4 text-amber-600 dark:text-amber-400" />
                <span className="font-medium text-foreground">Environment:</span>
                <span className="font-mono text-muted-foreground">{settings.environment} (v{settings.version})</span>
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
