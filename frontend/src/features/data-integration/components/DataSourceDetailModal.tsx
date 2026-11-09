import React, { useState } from 'react'
import { DataSource, UpdateDataSourceRequest } from '@/entities/integration'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { Label } from '@/shared/ui/label'
import { Textarea } from '@/shared/ui/textarea'
import { Badge } from '@/shared/ui/badge'
import { AlertCircle, Server } from 'lucide-react'
import { translations, SupportedLanguage } from '@/shared/i18n'

interface DataSourceDetailModalProps {
  source: DataSource | null
  isOpen: boolean
  onClose: () => void
  onUpdate: (id: string, data: UpdateDataSourceRequest) => Promise<void>
  isUpdating?: boolean
  canManage: boolean
  lang: SupportedLanguage
}

export const DataSourceDetailModal: React.FC<DataSourceDetailModalProps> = ({
  source,
  isOpen,
  onClose,
  onUpdate,
  isUpdating,
  canManage,
  lang,
}) => {
  const t = translations[lang].dataIntegration

  const [isEditing, setIsEditing] = useState(false)
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [endpointUrl, setEndpointUrl] = useState('')
  const [configurationRef, setConfigurationRef] = useState('')

  React.useEffect(() => {
    if (source) {
      setName(source.name || '')
      setDescription(source.description || '')
      setEndpointUrl(source.endpointUrl || '')
      setConfigurationRef(source.configurationRef || '')
      setIsEditing(false)
    }
  }, [source])

  if (!source) return null

  const handleSave = async () => {
    await onUpdate(source.id, {
      name: name.trim() || undefined,
      description: description.trim() || undefined,
      endpointUrl: endpointUrl.trim() || undefined,
      configurationRef: configurationRef.trim() || undefined,
    })
    setIsEditing(false)
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <div className="flex items-center justify-between pr-4">
            <DialogTitle className="text-xl font-bold flex items-center gap-2">
              <Server className="h-5 w-5 text-primary" />
              {source.name}
            </DialogTitle>
            <Badge variant="outline" className="font-mono text-xs">
              {source.status}
            </Badge>
          </div>
          <p className="text-xs text-muted-foreground font-mono">{source.sourceCode}</p>
        </DialogHeader>

        <div className="space-y-4 py-2">
          {/* Status & Error Banner */}
          {source.lastErrorMessage && (
            <div className="p-3 rounded-lg bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900/60 text-rose-800 dark:text-rose-300 text-xs flex items-start gap-2">
              <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
              <div>
                <span className="font-semibold">{t.recentError}:</span> {source.lastErrorMessage}
              </div>
            </div>
          )}

          {/* Quick Metrics */}
          <div className="grid grid-cols-3 gap-3">
            <div className="p-3 rounded-lg bg-muted/40 border border-border/60">
              <span className="text-[11px] text-muted-foreground uppercase">{t.ingestedCount}</span>
              <div className="text-lg font-bold text-foreground mt-0.5">
                {source.totalIngestedCount.toLocaleString()}
              </div>
            </div>
            <div className="p-3 rounded-lg bg-muted/40 border border-border/60">
              <span className="text-[11px] text-muted-foreground uppercase">{t.rejectedCount}</span>
              <div className="text-lg font-bold text-rose-600 dark:text-rose-400 mt-0.5">
                {source.totalRejectedCount.toLocaleString()}
              </div>
            </div>
            <div className="p-3 rounded-lg bg-muted/40 border border-border/60">
              <span className="text-[11px] text-muted-foreground uppercase">{t.reliability}</span>
              <div className="text-lg font-bold text-foreground mt-0.5">
                {source.reliabilityRate}%
              </div>
            </div>
          </div>

          {/* Configuration Form / View */}
          {isEditing ? (
            <div className="space-y-3 pt-2">
              <div>
                <Label htmlFor="src-name">{t.sourceName}</Label>
                <Input
                  id="src-name"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  className="mt-1"
                />
              </div>

              <div>
                <Label htmlFor="src-desc">{t.description}</Label>
                <Textarea
                  id="src-desc"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  rows={2}
                  className="mt-1 text-sm"
                />
              </div>

              <div>
                <Label htmlFor="src-url">{t.endpointUrl}</Label>
                <Input
                  id="src-url"
                  value={endpointUrl}
                  onChange={(e) => setEndpointUrl(e.target.value)}
                  placeholder="https://api.external.com/v1"
                  className="mt-1 font-mono text-xs"
                />
              </div>

              <div>
                <Label htmlFor="src-ref">{t.configurationRef}</Label>
                <Input
                  id="src-ref"
                  value={configurationRef}
                  onChange={(e) => setConfigurationRef(e.target.value)}
                  placeholder="ENV:API_KEY"
                  className="mt-1 font-mono text-xs"
                />
              </div>
            </div>
          ) : (
            <div className="space-y-3 pt-2 text-sm">
              <div>
                <span className="text-xs text-muted-foreground">{t.description}:</span>
                <p className="mt-0.5 text-foreground">{source.description || t.noDescription}</p>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <span className="text-xs text-muted-foreground">{t.type}:</span>
                  <p className="font-mono text-xs text-foreground mt-0.5">{source.sourceType}</p>
                </div>
                <div>
                  <span className="text-xs text-muted-foreground">{t.provider}:</span>
                  <p className="font-mono text-xs text-foreground mt-0.5">{source.providerName}</p>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <span className="text-xs text-muted-foreground">{t.endpointUrl}:</span>
                  <p className="font-mono text-xs text-foreground mt-0.5 truncate">
                    {source.endpointUrl || t.noneSpecified}
                  </p>
                </div>
                <div>
                  <span className="text-xs text-muted-foreground">{t.configurationRef}:</span>
                  <p className="font-mono text-xs text-foreground mt-0.5">
                    {source.configurationRef || t.noneSpecified}
                  </p>
                </div>
              </div>

              <div className="border-t border-border/60 pt-3 text-xs text-muted-foreground space-y-1">
                <div className="flex justify-between">
                  <span>{t.lastSynchronized}:</span>
                  <span className="font-mono text-foreground">{source.lastSyncAt || t.never}</span>
                </div>
                <div className="flex justify-between">
                  <span>{t.lastSuccessfulSync}:</span>
                  <span className="font-mono text-emerald-600 dark:text-emerald-400">{source.lastSuccessAt || t.never}</span>
                </div>
                <div className="flex justify-between">
                  <span>{t.createdAt}:</span>
                  <span className="font-mono text-foreground">{source.createdAt}</span>
                </div>
              </div>
            </div>
          )}
        </div>

        <DialogFooter className="flex items-center justify-between sm:justify-between pt-2">
          {canManage && !isEditing ? (
            <Button variant="outline" size="sm" onClick={() => setIsEditing(true)}>
              {t.editConfiguration}
            </Button>
          ) : isEditing ? (
            <Button variant="ghost" size="sm" onClick={() => setIsEditing(false)}>
              {t.cancel}
            </Button>
          ) : <div />}

          <div className="flex items-center gap-2">
            {isEditing && (
              <Button size="sm" onClick={handleSave} disabled={isUpdating}>
                {isUpdating ? t.saving : t.saveChanges}
              </Button>
            )}
            <Button variant="secondary" size="sm" onClick={onClose}>
              {t.close}
            </Button>
          </div>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
