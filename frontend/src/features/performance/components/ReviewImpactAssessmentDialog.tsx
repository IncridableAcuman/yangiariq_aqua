import React, { useState } from 'react'
import { Button } from '@/shared/ui/button'
import { ShieldCheck, X } from 'lucide-react'
import { ImpactAssessment } from '@/entities/performance'
import { useReviewImpactAssessment } from '../queries/usePerformanceQueries'

interface Props {
  isOpen: boolean
  onClose: () => void
  assessment: ImpactAssessment | null
}

export const ReviewImpactAssessmentDialog: React.FC<Props> = ({
  isOpen,
  onClose,
  assessment,
}) => {
  const reviewMutation = useReviewImpactAssessment()

  const [reviewStatus, setReviewStatus] = useState<string>('VERIFIED_OUTCOME')
  const [reviewNotes, setReviewNotes] = useState('')

  if (!isOpen || !assessment) return null

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      await reviewMutation.mutateAsync({
        id: assessment.id,
        payload: {
          reviewStatus,
          reviewNotes: reviewNotes || 'Mustaqil texnik audit xulosasiga binoan tasdiqlandi.',
        },
      })
      onClose()
    } catch (err) {
      console.error(err)
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/80 backdrop-blur-sm animate-in fade-in">
      <div className="bg-card border border-border rounded-xl shadow-xl w-full max-w-md overflow-hidden max-h-[90vh] flex flex-col">
        <div className="p-4 border-b border-border flex items-center justify-between">
          <div className="flex items-center gap-2">
            <ShieldCheck className="w-5 h-5 text-emerald-500" />
            <h3 className="text-sm font-bold text-foreground">Taʼsir Baholashni Koʻrib Chiqish va Tasdiqlash</h3>
          </div>
          <Button variant="ghost" size="sm" onClick={onClose} className="h-7 w-7 p-0">
            <X className="w-4 h-4" />
          </Button>
        </div>

        <form onSubmit={handleSubmit} className="p-4 space-y-3 text-xs overflow-y-auto">
          <div className="bg-muted/40 p-3 rounded-lg border border-border space-y-1">
            <p className="font-semibold text-foreground">{assessment.title}</p>
            <p className="text-muted-foreground text-[11px]">
              Intervensiya: {assessment.interventionReference} • Oʻzgarish: {assessment.percentageChange}%
            </p>
          </div>

          <div>
            <label className="font-medium text-foreground">Audit Xulosasi</label>
            <select
              value={reviewStatus}
              onChange={(e) => setReviewStatus(e.target.value)}
              className="mt-1 w-full px-2.5 py-1.5 rounded-lg border border-border bg-background text-foreground"
            >
              <option value="VERIFIED_OUTCOME">Tasdiqlangan Natija (Verified Outcome)</option>
              <option value="INCONCLUSIVE">Qarama-qarshi / Noaniq (Inconclusive)</option>
              <option value="REJECTED">Rad Etish (Rejected)</option>
            </select>
          </div>

          <div>
            <label className="font-medium text-foreground">Auditor Qaydlari</label>
            <textarea
              rows={3}
              required
              value={reviewNotes}
              onChange={(e) => setReviewNotes(e.target.value)}
              placeholder="Joyiga chiqib koʻrik oʻtkazildi, oʻlchov koʻrsatkichlari solishtirildi..."
              className="mt-1 w-full px-3 py-1.5 rounded-lg border border-border bg-background text-foreground resize-none"
            />
          </div>

          <div className="pt-2 border-t border-border flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={onClose}>
              Bekor qilish
            </Button>
            <Button type="submit" disabled={reviewMutation.isPending}>
              {reviewMutation.isPending ? 'Saqlanmoqda...' : 'Audit Xulosasini Tasdiqlash'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
