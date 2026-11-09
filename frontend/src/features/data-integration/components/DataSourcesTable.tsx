import React from 'react'
import { DataSource, DataSourceStatus } from '@/entities/integration'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/ui/table'
import { RefreshCw, Power, ExternalLink, Activity, CloudSun, FileSpreadsheet, UserCheck, Cpu } from 'lucide-react'
import { translations, SupportedLanguage } from '@/shared/i18n'

interface DataSourcesTableProps {
  sources: DataSource[]
  lang: SupportedLanguage
  onSelectSource: (source: DataSource) => void
  onToggleStatus: (id: string) => void
  onSyncSource: (id: string) => void
  isToggling?: boolean
  isSyncing?: boolean
  canManage: boolean
}

export const DataSourcesTable: React.FC<DataSourcesTableProps> = ({
  sources,
  lang,
  onSelectSource,
  onToggleStatus,
  onSyncSource,
  isToggling,
  isSyncing,
  canManage,
}) => {
  const t = translations[lang].dataIntegration

  const renderStatusBadge = (status: DataSourceStatus) => {
    switch (status) {
      case 'ACTIVE':
        return (
          <Badge variant="outline" className="bg-emerald-50 text-emerald-700 border-emerald-300 dark:bg-emerald-950/40 dark:text-emerald-300">
            {t.statusActive}
          </Badge>
        )
      case 'DEGRADED':
        return (
          <Badge variant="outline" className="bg-amber-50 text-amber-700 border-amber-300 dark:bg-amber-950/40 dark:text-amber-300">
            {t.statusDegraded}
          </Badge>
        )
      case 'NOT_CONFIGURED':
        return (
          <Badge variant="outline" className="bg-rose-50 text-rose-700 border-rose-300 dark:bg-rose-950/40 dark:text-rose-300">
            {t.statusNotConfigured}
          </Badge>
        )
      case 'INACTIVE':
        return (
          <Badge variant="outline" className="bg-muted text-muted-foreground border-border">
            {t.statusInactive}
          </Badge>
        )
      case 'ERROR':
      default:
        return (
          <Badge variant="destructive">
            {t.statusError}
          </Badge>
        )
    }
  }

  const renderTypeIcon = (type: string) => {
    switch (type) {
      case 'WEATHER_PROVIDER':
        return <CloudSun className="h-4 w-4 text-sky-500" />
      case 'CSV_IMPORT':
        return <FileSpreadsheet className="h-4 w-4 text-emerald-500" />
      case 'OPERATOR_INPUT':
      case 'FARMER_INPUT':
      case 'MANUAL_MEASUREMENT':
        return <UserCheck className="h-4 w-4 text-amber-500" />
      case 'SENSOR_DEVICE':
        return <Cpu className="h-4 w-4 text-blue-500" />
      case 'SIMULATED_PROVIDER':
      default:
        return <Activity className="h-4 w-4 text-purple-500" />
    }
  }

  const formatTimestamp = (ts?: string) => {
    if (!ts) return t.neverSynced
    try {
      const date = new Date(ts)
      return date.toLocaleDateString(lang === 'uz' ? 'uz-UZ' : lang === 'ru' ? 'ru-RU' : 'en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      })
    } catch {
      return ts
    }
  }

  return (
    <div className="rounded-lg border border-border/70 overflow-hidden bg-card/60 backdrop-blur-sm">
      <Table>
        <TableHeader className="bg-muted/40">
          <TableRow>
            <TableHead className="w-[180px]">{t.sourceName}</TableHead>
            <TableHead>{t.type}</TableHead>
            <TableHead>{t.provider}</TableHead>
            <TableHead>{t.status}</TableHead>
            <TableHead className="text-right">{t.ingestedCount}</TableHead>
            <TableHead className="text-right">{t.reliability}</TableHead>
            <TableHead>{t.lastSynchronized}</TableHead>
            <TableHead className="text-right">{t.actions}</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {sources.length === 0 ? (
            <TableRow>
              <TableCell colSpan={8} className="text-center py-8 text-muted-foreground">
                {t.noSourcesFound}
              </TableCell>
            </TableRow>
          ) : (
            sources.map((source) => (
              <TableRow key={source.id} className="hover:bg-muted/30 transition-colors">
                <TableCell className="font-medium">
                  <div className="flex flex-col">
                    <span className="text-sm font-semibold text-foreground">{source.name}</span>
                    <span className="text-[11px] font-mono text-muted-foreground">{source.sourceCode}</span>
                  </div>
                </TableCell>
                <TableCell>
                  <div className="flex items-center gap-1.5 text-xs text-muted-foreground">
                    {renderTypeIcon(source.sourceType)}
                    <span>{source.sourceType}</span>
                  </div>
                </TableCell>
                <TableCell className="text-xs text-muted-foreground font-mono">
                  {source.providerName}
                </TableCell>
                <TableCell>{renderStatusBadge(source.status)}</TableCell>
                <TableCell className="text-right">
                  <div className="flex flex-col items-end">
                    <span className="text-sm font-semibold text-foreground">
                      {source.totalIngestedCount.toLocaleString()}
                    </span>
                    {source.totalRejectedCount > 0 && (
                      <span className="text-[10px] text-rose-500">
                        {source.totalRejectedCount} {t.rejected}
                      </span>
                    )}
                  </div>
                </TableCell>
                <TableCell className="text-right">
                  <div className="inline-flex items-center gap-2">
                    <div className="w-16 bg-muted rounded-full h-1.5 overflow-hidden">
                      <div
                        className={`h-full rounded-full ${
                          source.reliabilityRate >= 95
                            ? 'bg-emerald-500'
                            : source.reliabilityRate >= 80
                            ? 'bg-amber-500'
                            : 'bg-rose-500'
                        }`}
                        style={{ width: `${Math.max(5, source.reliabilityRate)}%` }}
                      />
                    </div>
                    <span className="text-xs font-medium">{source.reliabilityRate}%</span>
                  </div>
                </TableCell>
                <TableCell className="text-xs text-muted-foreground">
                  {formatTimestamp(source.lastSyncAt)}
                </TableCell>
                <TableCell className="text-right">
                  <div className="flex items-center justify-end gap-1">
                    {canManage && (
                      <>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-8 w-8 p-0"
                          title={t.syncNow}
                          onClick={() => onSyncSource(source.id)}
                          disabled={isSyncing}
                        >
                          <RefreshCw className={`h-3.5 w-3.5 text-primary ${isSyncing ? 'animate-spin' : ''}`} />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-8 w-8 p-0"
                          title={source.status === 'ACTIVE' ? t.deactivate : t.activate}
                          onClick={() => onToggleStatus(source.id)}
                          disabled={isToggling}
                        >
                          <Power
                            className={`h-3.5 w-3.5 ${
                              source.status === 'ACTIVE' ? 'text-amber-500' : 'text-emerald-500'
                            }`}
                          />
                        </Button>
                      </>
                    )}
                    <Button
                      variant="ghost"
                      size="sm"
                      className="h-8 w-8 p-0"
                      title={t.viewDetails}
                      onClick={() => onSelectSource(source)}
                    >
                      <ExternalLink className="h-3.5 w-3.5 text-muted-foreground" />
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </div>
  )
}
