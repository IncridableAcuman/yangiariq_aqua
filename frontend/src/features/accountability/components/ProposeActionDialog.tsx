import React, { useState } from 'react'
import { WaterIncident, ProposeActionPayload } from '@/entities/accountability'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { Textarea } from '@/shared/ui/textarea'
import { FileCheck, X } from 'lucide-react'

interface Props {
  isOpen: boolean
  onClose: () => void
  incident?: WaterIncident
  onSubmit: (payload: ProposeActionPayload) => Promise<void>
  isSubmitting?: boolean
}

export const ProposeActionDialog: React.FC<Props> = ({
  isOpen,
  onClose,
  incident,
  onSubmit,
  isSubmitting = false,
}) => {
  const [title, setTitle] = useState('')
  const [description, setDescription] = useState('')
  const [objective, setObjective] = useState('')
  const [actionOwnerName, setActionOwnerName] = useState('')
  const [priority, setPriority] = useState('MEDIUM')
  const [expectedOutcome, setExpectedOutcome] = useState('')
  const [verificationRequirement, setVerificationRequirement] = useState('')
  const [error, setError] = useState<string | null>(null)

  if (!isOpen || !incident) return null

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!title.trim() || !description.trim() || !objective.trim() || !expectedOutcome.trim()) {
      setError('Barcha majburiy maydonlarni toʻldiring')
      return
    }
    setError(null)
    await onSubmit({
      title: title.trim(),
      description: description.trim(),
      objective: objective.trim(),
      actionOwnerName: actionOwnerName.trim() || 'Tuman Texnik Xizmati',
      priority,
      expectedOutcome: expectedOutcome.trim(),
      verificationRequirement: verificationRequirement.trim() || 'Fizik koʻrik akti talab etiladi',
    })
    onClose()
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/80 backdrop-blur-sm animate-in fade-in">
      <div className="bg-card border border-border rounded-xl shadow-xl w-full max-w-lg overflow-hidden">
        <div className="p-4 sm:p-5 border-b border-border flex items-center justify-between">
          <div className="flex items-center gap-2">
            <FileCheck className="w-4 h-4 text-primary" />
            <h3 className="text-sm font-bold text-foreground">Tuzatish chorasini taklif etish</h3>
          </div>
          <Button variant="ghost" size="sm" onClick={onClose} className="h-7 w-7 p-0">
            <X className="w-4 h-4" />
          </Button>
        </div>

        <form onSubmit={handleSubmit} className="p-4 sm:p-5 space-y-3.5">
          <div className="space-y-1">
            <label className="text-xs font-semibold text-foreground">Chora nomi *:</label>
            <Input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Masalan: Gidropost datchiklarini kalibrlash va klapanni tozalash"
              className="text-xs"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-foreground">Tavsif va koʻriladigan amallar *:</label>
            <Textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Amalga oshirilishi kerak boʻlgan fizik yoki texnik ishlar ketma-ketligi..."
              rows={2}
              className="text-xs"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <label className="text-xs font-semibold text-foreground">Maqsad (Objective) *:</label>
              <Input
                value={objective}
                onChange={(e) => setObjective(e.target.value)}
                placeholder="Oqishni 0% ga tushirish"
                className="text-xs"
              />
            </div>
            <div className="space-y-1">
              <label className="text-xs font-semibold text-foreground">Ustuvorlik:</label>
              <select
                value={priority}
                onChange={(e) => setPriority(e.target.value)}
                className="w-full h-9 px-2.5 rounded-md border border-input bg-background text-xs"
              >
                <option value="IMMEDIATE">Favqulodda (Immediate)</option>
                <option value="HIGH">Yuqori (High)</option>
                <option value="MEDIUM">Oʻrtacha (Medium)</option>
                <option value="ROUTINE">Rejaviy (Routine)</option>
              </select>
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-foreground">Masʼul ijrochi (F.I.Sh. yoki guruh):</label>
            <Input
              value={actionOwnerName}
              onChange={(e) => setActionOwnerName(e.target.value)}
              placeholder="Masalan: Shovot kanali taʼmirlash brigadasi"
              className="text-xs"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-foreground">Kutilayotgan natija *:</label>
            <Input
              value={expectedOutcome}
              onChange={(e) => setExpectedOutcome(e.target.value)}
              placeholder="Suv oqimi yoʻqotishining 3% dan pastga tushishi"
              className="text-xs"
            />
          </div>

          <div className="space-y-1">
            <label className="text-xs font-semibold text-foreground">Mustaqil tekshiruv talabi:</label>
            <Input
              value={verificationRequirement}
              onChange={(e) => setVerificationRequirement(e.target.value)}
              placeholder="Fizik koʻrik akti va ikkilamchi akustik oʻlchov"
              className="text-xs"
            />
          </div>

          {error && <p className="text-[11px] text-destructive">{error}</p>}

          <div className="flex items-center justify-end gap-2 pt-2 border-t border-border">
            <Button type="button" variant="outline" size="sm" onClick={onClose} disabled={isSubmitting}>
              Bekor qilish
            </Button>
            <Button type="submit" size="sm" disabled={isSubmitting}>
              {isSubmitting ? 'Saqlanmoqda...' : 'Chorani taklif etish'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
