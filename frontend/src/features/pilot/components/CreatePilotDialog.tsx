import React, { useState } from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  Button,
  Input,
  Label,
} from '@/shared/ui'
import { PlusCircle } from 'lucide-react'
import { translations, SupportedLang } from '@/shared/i18n'

interface CreatePilotDialogProps {
  isOpen: boolean
  onClose: () => void
  onCreate: (payload: {
    code: string
    name: string
    description?: string
    geographicScope: string
    startDate: string
    endDate: string
    organization: string
    operationalObjectives: string
  }) => Promise<void>
  lang: SupportedLang
}

export const CreatePilotDialog: React.FC<CreatePilotDialogProps> = ({
  isOpen,
  onClose,
  onCreate,
  lang,
}) => {
  const t = translations[lang].pilot

  const [code, setCode] = useState('')
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [geographicScope, setGeographicScope] = useState('Yangiariq District, Khorezm')
  const [startDate, setStartDate] = useState(new Date().toISOString().slice(0, 10))
  const [endDate, setEndDate] = useState(
    new Date(Date.now() + 180 * 24 * 3600 * 1000).toISOString().slice(0, 10)
  )
  const [organization, setOrganization] = useState('Yangiariq Water Users Association (WUA)')
  const [operationalObjectives, setOperationalObjectives] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!code.trim() || !name.trim() || !operationalObjectives.trim()) {
      setError(t.fillRequiredFields)
      return
    }
    setError(null)
    setIsSubmitting(true)
    try {
      await onCreate({
        code: code.trim(),
        name: name.trim(),
        description: description.trim() || undefined,
        geographicScope: geographicScope.trim(),
        startDate,
        endDate,
        organization: organization.trim(),
        operationalObjectives: operationalObjectives.trim(),
      })
      onClose()
      setCode('')
      setName('')
      setDescription('')
      setOperationalObjectives('')
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : t.createFailed)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <PlusCircle className="h-5 w-5 text-primary" />
            {t.createPilotTitle}
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-3.5 py-2">
          {error && (
            <div className="p-2.5 rounded bg-rose-50 text-rose-700 dark:bg-rose-950/40 dark:text-rose-300 text-xs">
              {error}
            </div>
          )}

          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <Label className="text-xs font-semibold">{t.pilotCode} *</Label>
              <Input
                placeholder="PILOT-YNG-2026-01"
                value={code}
                onChange={(e) => setCode(e.target.value)}
                className="h-8 text-xs font-mono"
                required
              />
            </div>
            <div className="space-y-1">
              <Label className="text-xs font-semibold">{t.organization} *</Label>
              <Input
                placeholder="Yangiariq WUA"
                value={organization}
                onChange={(e) => setOrganization(e.target.value)}
                className="h-8 text-xs"
                required
              />
            </div>
          </div>

          <div className="space-y-1">
            <Label className="text-xs font-semibold">{t.pilotName} *</Label>
            <Input
              placeholder="Khorezm Cotton Water Efficiency Pilot"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="h-8 text-xs"
              required
            />
          </div>

          <div className="space-y-1">
            <Label className="text-xs font-semibold">{t.geographicScope} *</Label>
            <Input
              value={geographicScope}
              onChange={(e) => setGeographicScope(e.target.value)}
              className="h-8 text-xs"
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <Label className="text-xs font-semibold">{t.startDate} *</Label>
              <Input
                type="date"
                value={startDate}
                onChange={(e) => setStartDate(e.target.value)}
                className="h-8 text-xs"
                required
              />
            </div>
            <div className="space-y-1">
              <Label className="text-xs font-semibold">{t.endDate} *</Label>
              <Input
                type="date"
                value={endDate}
                onChange={(e) => setEndDate(e.target.value)}
                className="h-8 text-xs"
                required
              />
            </div>
          </div>

          <div className="space-y-1">
            <Label className="text-xs font-semibold">{t.operationalObjectives} *</Label>
            <textarea
              className="w-full rounded-md border border-input bg-background px-3 py-2 text-xs ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 min-h-[60px]"
              placeholder={t.operationalObjectivesPlaceholder}
              value={operationalObjectives}
              onChange={(e) => setOperationalObjectives(e.target.value)}
              required
            />
          </div>

          <div className="space-y-1">
            <Label className="text-xs font-semibold">{t.description}</Label>
            <textarea
              className="w-full rounded-md border border-input bg-background px-3 py-2 text-xs ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 min-h-[50px]"
              placeholder={t.descriptionPlaceholder}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>

          <DialogFooter className="pt-2">
            <Button type="button" variant="outline" size="sm" onClick={onClose} disabled={isSubmitting}>
              {t.cancel}
            </Button>
            <Button type="submit" size="sm" disabled={isSubmitting}>
              {isSubmitting ? t.creating : t.createPilot}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
