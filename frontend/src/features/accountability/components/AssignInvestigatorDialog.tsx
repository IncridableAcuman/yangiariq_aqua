import React, { useState } from 'react'
import { WaterIncident } from '@/entities/accountability'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { Textarea } from '@/shared/ui/textarea'
import { UserCheck, X } from 'lucide-react'

interface Props {
  isOpen: boolean
  onClose: () => void
  incident?: WaterIncident
  onSubmit: (investigatorId: string, investigatorName: string, organization: string, reason: string) => Promise<void>
  isSubmitting?: boolean
}

export const AssignInvestigatorDialog: React.FC<Props> = ({
  isOpen,
  onClose,
  incident,
  onSubmit,
  isSubmitting = false,
}) => {
  const [investigatorName, setInvestigatorName] = useState('')
  const [organization, setOrganization] = useState('Yangiariq tuman Suv Xoʻjaligi Boʻlimi')
  const [reason, setReason] = useState('')
  const [error, setError] = useState<string | null>(null)

  if (!isOpen || !incident) return null

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!investigatorName.trim()) {
      setError('Masʼul mutaxassis F.I.Sh. kiritilishi shart')
      return
    }
    setError(null)
    // generate UUID or use mock ID for manual investigator assignment
    const generatedId = window.crypto?.randomUUID ? window.crypto.randomUUID() : '00000000-0000-0000-0000-000000000001'
    await onSubmit(generatedId, investigatorName.trim(), organization.trim(), reason.trim() || 'Tergovchi masʼul etib tayinlandi')
    onClose()
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/80 backdrop-blur-sm animate-in fade-in">
      <div className="bg-card border border-border rounded-xl shadow-xl w-full max-w-lg overflow-hidden">
        <div className="p-4 sm:p-5 border-b border-border flex items-center justify-between">
          <div className="flex items-center gap-2">
            <UserCheck className="w-4 h-4 text-primary" />
            <h3 className="text-sm font-bold text-foreground">Masʼul tergovchini biriktirish</h3>
          </div>
          <Button variant="ghost" size="sm" onClick={onClose} className="h-7 w-7 p-0">
            <X className="w-4 h-4" />
          </Button>
        </div>

        <form onSubmit={handleSubmit} className="p-4 sm:p-5 space-y-4">
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground">Mutaxassis F.I.Sh.:</label>
            <Input
              value={investigatorName}
              onChange={(e) => setInvestigatorName(e.target.value)}
              placeholder="Masalan: Muxandis Shokirov Alisher"
              className="text-xs"
            />
            {error && <p className="text-[11px] text-destructive">{error}</p>}
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground">Masʼul tashkilot / WUA:</label>
            <Input
              value={organization}
              onChange={(e) => setOrganization(e.target.value)}
              placeholder="Masalan: Shovot Kanal Boshqarmasi yoki Bogʻot WUA"
              className="text-xs"
            />
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground">Biriktirish asosi va vazifasi:</label>
            <Textarea
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="Gidropost tekshiruvi va suv oqimi kalibrlashini oʻtkazish..."
              rows={3}
              className="text-xs"
            />
          </div>

          <div className="flex items-center justify-end gap-2 pt-2 border-t border-border">
            <Button type="button" variant="outline" size="sm" onClick={onClose} disabled={isSubmitting}>
              Bekor qilish
            </Button>
            <Button type="submit" size="sm" disabled={isSubmitting}>
              {isSubmitting ? 'Biriktirilmoqda...' : 'Tergovchini tayinlash'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
