import React, { useState } from 'react'
import { CreateDataSourceRequest, DataSourceType } from '@/entities/integration'
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
import { PlusCircle } from 'lucide-react'
import { translations, SupportedLanguage } from '@/shared/i18n'

interface RegisterDataSourceDialogProps {
  isOpen: boolean
  onClose: () => void
  onRegister: (data: CreateDataSourceRequest) => Promise<void>
  isRegistering?: boolean
  lang: SupportedLanguage
}

export const RegisterDataSourceDialog: React.FC<RegisterDataSourceDialogProps> = ({
  isOpen,
  onClose,
  onRegister,
  isRegistering,
  lang,
}) => {
  const t = translations[lang].dataIntegration

  const [sourceCode, setSourceCode] = useState('')
  const [name, setName] = useState('')
  const [sourceType, setSourceType] = useState<DataSourceType>('EXTERNAL_API')
  const [providerName, setProviderName] = useState('')
  const [description, setDescription] = useState('')
  const [endpointUrl, setEndpointUrl] = useState('')
  const [configurationRef, setConfigurationRef] = useState('')
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError(null)

    if (!sourceCode.trim()) {
      setError(t.errorSourceCodeRequired)
      return
    }
    if (!name.trim()) {
      setError(t.errorNameRequired)
      return
    }
    if (!providerName.trim()) {
      setError(t.errorProviderRequired)
      return
    }

    try {
      await onRegister({
        sourceCode: sourceCode.trim().toUpperCase(),
        name: name.trim(),
        sourceType,
        providerName: providerName.trim(),
        description: description.trim() || undefined,
        endpointUrl: endpointUrl.trim() || undefined,
        configurationRef: configurationRef.trim() || undefined,
      })
      // Reset form
      setSourceCode('')
      setName('')
      setProviderName('')
      setDescription('')
      setEndpointUrl('')
      setConfigurationRef('')
      onClose()
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Registration failed')
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle className="text-lg font-bold flex items-center gap-2">
            <PlusCircle className="h-5 w-5 text-primary" />
            {t.registerNewSource}
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-3 py-1">
          {error && (
            <div className="p-2.5 rounded bg-rose-50 dark:bg-rose-950/40 text-rose-700 dark:text-rose-300 text-xs border border-rose-200">
              {error}
            </div>
          )}

          <div className="grid grid-cols-2 gap-3">
            <div>
              <Label htmlFor="reg-code" className="text-xs">{t.sourceCode} *</Label>
              <Input
                id="reg-code"
                placeholder="KHOREZM_METEO_API"
                value={sourceCode}
                onChange={(e) => setSourceCode(e.target.value.toUpperCase())}
                className="mt-1 font-mono text-xs uppercase"
                required
              />
            </div>

            <div>
              <Label htmlFor="reg-type" className="text-xs">{t.type} *</Label>
              <select
                id="reg-type"
                value={sourceType}
                onChange={(e) => setSourceType(e.target.value as DataSourceType)}
                className="mt-1 flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-xs shadow-xs transition-colors focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
              >
                <option value="EXTERNAL_API">EXTERNAL_API</option>
                <option value="WEATHER_PROVIDER">WEATHER_PROVIDER</option>
                <option value="SENSOR_DEVICE">SENSOR_DEVICE</option>
                <option value="CSV_IMPORT">CSV_IMPORT</option>
                <option value="OPERATOR_INPUT">OPERATOR_INPUT</option>
              </select>
            </div>
          </div>

          <div>
            <Label htmlFor="reg-name" className="text-xs">{t.sourceName} *</Label>
            <Input
              id="reg-name"
              placeholder="Khorezm Regional Agro-Meteo Gateway"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="mt-1 text-sm"
              required
            />
          </div>

          <div>
            <Label htmlFor="reg-provider" className="text-xs">{t.provider} *</Label>
            <Input
              id="reg-provider"
              placeholder="RegionalAgroMeteoAdapter"
              value={providerName}
              onChange={(e) => setProviderName(e.target.value)}
              className="mt-1 font-mono text-xs"
              required
            />
          </div>

          <div>
            <Label htmlFor="reg-url" className="text-xs">{t.endpointUrl}</Label>
            <Input
              id="reg-url"
              placeholder="https://meteo.yangiariq.uz/api/v1/telemetry"
              value={endpointUrl}
              onChange={(e) => setEndpointUrl(e.target.value)}
              className="mt-1 font-mono text-xs"
            />
          </div>

          <div>
            <Label htmlFor="reg-ref" className="text-xs">{t.configurationRef}</Label>
            <Input
              id="reg-ref"
              placeholder="ENV:METEO_API_KEY"
              value={configurationRef}
              onChange={(e) => setConfigurationRef(e.target.value)}
              className="mt-1 font-mono text-xs"
            />
          </div>

          <div>
            <Label htmlFor="reg-desc" className="text-xs">{t.description}</Label>
            <Textarea
              id="reg-desc"
              placeholder="Integration details, ingestion cadence, and authorized maintainer contact"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={2}
              className="mt-1 text-xs"
            />
          </div>

          <DialogFooter className="pt-2">
            <Button type="button" variant="outline" size="sm" onClick={onClose}>
              {t.cancel}
            </Button>
            <Button type="submit" size="sm" disabled={isRegistering}>
              {isRegistering ? t.registering : t.register}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
