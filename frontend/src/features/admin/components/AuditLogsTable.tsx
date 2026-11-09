import React, { useState } from 'react'
import {
  Button,
  Badge,
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/ui'
import {
  ShieldAlert,
  ChevronLeft,
  ChevronRight,
  Eye,
  Filter,
  Terminal,
} from 'lucide-react'
import { SecurityAuditLogDto } from '@/entities/admin'
import { PageResponse } from '@/shared/api/client'
import { translations, SupportedLang } from '@/shared/i18n'

interface AuditLogsTableProps {
  logsPage: PageResponse<SecurityAuditLogDto> | undefined
  lang: SupportedLang
  isLoading?: boolean
  selectedAction?: string
  setSelectedAction: (a?: string) => void
  currentPage: number
  setCurrentPage: (p: number) => void
}

export const AuditLogsTable: React.FC<AuditLogsTableProps> = ({
  logsPage,
  lang,
  isLoading,
  selectedAction,
  setSelectedAction,
  currentPage,
  setCurrentPage,
}) => {
  const t = translations[lang].admin
  const [detailModalLog, setDetailModalLog] = useState<SecurityAuditLogDto | null>(null)

  const logs = logsPage?.content || []
  const totalPages = logsPage?.totalPages || 0
  const totalElements = logsPage?.totalElements || 0

  const getActionBadge = (action: string) => {
    if (action.includes('CREATED') || action.includes('SUCCESS')) {
      return (
        <Badge variant="outline" className="bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border-emerald-300 font-mono text-[10px]">
          {action}
        </Badge>
      )
    }
    if (action.includes('DELETED') || action.includes('BLOCKED') || action.includes('FAILURE') || action.includes('DENIED')) {
      return (
        <Badge variant="outline" className="bg-rose-500/10 text-rose-700 dark:text-rose-400 border-rose-300 font-mono text-[10px]">
          {action}
        </Badge>
      )
    }
    if (action.includes('ROLE')) {
      return (
        <Badge variant="outline" className="bg-purple-500/10 text-purple-700 dark:text-purple-400 border-purple-300 font-mono text-[10px]">
          {action}
        </Badge>
      )
    }
    return (
      <Badge variant="outline" className="bg-amber-500/10 text-amber-700 dark:text-amber-400 border-amber-300 font-mono text-[10px]">
        {action}
      </Badge>
    )
  }

  const formatJson = (str?: string | null) => {
    if (!str) return 'No payload details'
    try {
      const parsed = JSON.parse(str)
      return JSON.stringify(parsed, null, 2)
    } catch {
      return str
    }
  }

  return (
    <div className="space-y-4">
      {/* Filter Bar */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <Filter className="w-4 h-4 text-muted-foreground" />
          <select
            value={selectedAction || ''}
            onChange={(e) => {
              setSelectedAction(e.target.value || undefined)
              setCurrentPage(0)
            }}
            className="h-9 rounded-md border border-input bg-background px-3 py-1 text-xs shadow-xs focus-visible:outline-hidden focus-visible:ring-1 focus-visible:ring-ring"
          >
            <option value="">{t.auditSection.allActions}</option>
            <option value="USER_CREATED">USER_CREATED</option>
            <option value="USER_UPDATED">USER_UPDATED</option>
            <option value="USER_ROLE_CHANGED">USER_ROLE_CHANGED</option>
            <option value="USER_STATUS_CHANGED">USER_STATUS_CHANGED</option>
            <option value="USER_DELETED">USER_DELETED</option>
            <option value="ROLE_CREATED">ROLE_CREATED</option>
            <option value="ROLE_PERMISSIONS_UPDATED">ROLE_PERMISSIONS_UPDATED</option>
            <option value="LOGIN_SUCCESS">LOGIN_SUCCESS</option>
            <option value="LOGIN_FAILURE">LOGIN_FAILURE</option>
            <option value="ACCESS_DENIED">ACCESS_DENIED</option>
          </select>
        </div>

        <div className="text-xs text-muted-foreground">
          Immutable append-only audit trail
        </div>
      </div>

      {/* Logs Table */}
      <div className="border rounded-xl bg-card shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-muted/50 border-b text-xs font-semibold text-muted-foreground uppercase tracking-wider">
              <tr>
                <th className="px-4 py-3">{t.auditSection.timestamp}</th>
                <th className="px-4 py-3">{t.auditSection.actor}</th>
                <th className="px-4 py-3">{t.auditSection.action}</th>
                <th className="px-4 py-3">{t.auditSection.targetType}</th>
                <th className="px-4 py-3">{t.auditSection.ipAddress}</th>
                <th className="px-4 py-3">Result</th>
                <th className="px-4 py-3 text-right">{t.auditSection.details}</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {isLoading ? (
                <tr>
                  <td colSpan={7} className="px-4 py-8 text-center text-muted-foreground">
                    <div className="flex items-center justify-center gap-2">
                      <div className="w-4 h-4 border-2 border-primary border-t-transparent rounded-full animate-spin" />
                      <span>Loading audit trail...</span>
                    </div>
                  </td>
                </tr>
              ) : logs.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-4 py-8 text-center text-muted-foreground">
                    {t.auditSection.noLogsFound}
                  </td>
                </tr>
              ) : (
                logs.map((log) => (
                  <tr key={log.id} className="hover:bg-muted/30 transition-colors">
                    {/* Timestamp */}
                    <td className="px-4 py-3 text-xs font-mono text-muted-foreground whitespace-nowrap">
                      {new Date(log.createdAt).toLocaleString()}
                    </td>

                    {/* Actor */}
                    <td className="px-4 py-3 text-xs font-medium">
                      <span className="font-mono text-foreground">@{log.actorName || 'system'}</span>
                    </td>

                    {/* Action */}
                    <td className="px-4 py-3">{getActionBadge(log.action)}</td>

                    {/* Target */}
                    <td className="px-4 py-3 text-xs">
                      {log.targetType ? (
                        <div className="flex items-center gap-1.5 font-mono">
                          <span className="text-muted-foreground">{log.targetType}:</span>
                          <span className="text-foreground max-w-[120px] truncate" title={log.targetId || undefined}>
                            {log.targetId || '—'}
                          </span>
                        </div>
                      ) : (
                        <span className="text-muted-foreground">—</span>
                      )}
                    </td>

                    {/* IP */}
                    <td className="px-4 py-3 text-xs font-mono text-muted-foreground">
                      {log.ipAddress || '—'}
                    </td>

                    {/* Result */}
                    <td className="px-4 py-3">
                      <Badge
                        variant="outline"
                        className={
                          log.result === 'SUCCESS'
                            ? 'bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border-emerald-300 text-[10px]'
                            : 'bg-rose-500/10 text-rose-700 dark:text-rose-400 border-rose-300 text-[10px]'
                        }
                      >
                        {log.result}
                      </Badge>
                    </td>

                    {/* Details button */}
                    <td className="px-4 py-3 text-right">
                      <Button
                        size="sm"
                        variant="ghost"
                        onClick={() => setDetailModalLog(log)}
                        className="h-7 px-2 text-xs gap-1"
                      >
                        <Eye className="w-3.5 h-3.5" />
                        <span>{t.auditSection.viewDetails}</span>
                      </Button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Footer */}
        <div className="flex items-center justify-between px-4 py-3 border-t bg-muted/20 text-xs text-muted-foreground">
          <div>
            Showing <span className="font-semibold text-foreground">{logs.length}</span> of{' '}
            <span className="font-semibold text-foreground">{totalElements}</span> logs
          </div>
          <div className="flex items-center gap-2">
            <Button
              size="sm"
              variant="outline"
              onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
              disabled={currentPage === 0 || isLoading}
              className="h-8 px-2 gap-1"
            >
              <ChevronLeft className="w-4 h-4" />
              <span>Previous</span>
            </Button>
            <span className="text-xs">
              Page {currentPage + 1} of {Math.max(1, totalPages)}
            </span>
            <Button
              size="sm"
              variant="outline"
              onClick={() => setCurrentPage(currentPage + 1)}
              disabled={currentPage + 1 >= totalPages || isLoading}
              className="h-8 px-2 gap-1"
            >
              <span>Next</span>
              <ChevronRight className="w-4 h-4" />
            </Button>
          </div>
        </div>
      </div>

      {/* Audit Detail Modal */}
      {detailModalLog && (
        <Dialog open={Boolean(detailModalLog)} onOpenChange={() => setDetailModalLog(null)}>
          <DialogContent className="sm:max-w-xl max-h-[85vh] overflow-y-auto">
            <DialogHeader>
              <div className="flex items-center gap-2">
                <div className="p-2 bg-primary/10 rounded-lg text-primary">
                  <ShieldAlert className="w-5 h-5" />
                </div>
                <div>
                  <DialogTitle>{t.auditSection.modalTitle}</DialogTitle>
                  <p className="text-xs text-muted-foreground mt-0.5">
                    {detailModalLog.action} by @{detailModalLog.actorName || 'system'}
                  </p>
                </div>
              </div>
            </DialogHeader>

            <div className="space-y-4 pt-2 text-xs">
              <div className="grid grid-cols-2 gap-3 p-3 bg-muted/30 rounded-lg border">
                <div>
                  <span className="text-muted-foreground block">Event ID:</span>
                  <span className="font-mono text-foreground">{detailModalLog.id}</span>
                </div>
                <div>
                  <span className="text-muted-foreground block">Timestamp:</span>
                  <span className="font-mono text-foreground">
                    {new Date(detailModalLog.createdAt).toISOString()}
                  </span>
                </div>
                <div>
                  <span className="text-muted-foreground block">Actor User ID:</span>
                  <span className="font-mono text-foreground">{detailModalLog.actorId || 'N/A'}</span>
                </div>
                <div>
                  <span className="text-muted-foreground block">Actor Role:</span>
                  <span className="font-semibold text-foreground">{detailModalLog.actorRole || 'N/A'}</span>
                </div>
                <div>
                  <span className="text-muted-foreground block">Target Type:</span>
                  <span className="font-mono text-foreground">{detailModalLog.targetType || 'N/A'}</span>
                </div>
                <div>
                  <span className="text-muted-foreground block">Target ID:</span>
                  <span className="font-mono text-foreground">{detailModalLog.targetId || 'N/A'}</span>
                </div>
                <div>
                  <span className="text-muted-foreground block">Client IP:</span>
                  <span className="font-mono text-foreground">{detailModalLog.ipAddress || 'N/A'}</span>
                </div>
                <div>
                  <span className="text-muted-foreground block">Outcome:</span>
                  <span className="font-semibold text-foreground">{detailModalLog.result}</span>
                </div>
              </div>

              <div className="space-y-1.5">
                <div className="flex items-center gap-1.5 font-semibold text-foreground">
                  <Terminal className="w-3.5 h-3.5 text-primary" />
                  <span>Payload Details:</span>
                </div>
                <pre className="p-3 bg-zinc-950 text-zinc-100 dark:bg-zinc-900 rounded-lg font-mono text-[11px] overflow-x-auto border border-zinc-800">
                  {formatJson(detailModalLog.detailsJson)}
                </pre>
              </div>

              <div className="flex justify-end pt-2">
                <Button size="sm" variant="outline" onClick={() => setDetailModalLog(null)}>
                  {t.auditSection.close}
                </Button>
              </div>
            </div>
          </DialogContent>
        </Dialog>
      )}
    </div>
  )
}
