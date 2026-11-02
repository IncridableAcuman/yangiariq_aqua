import { useState } from 'react'
import { ShieldCheck, AlertTriangle, X } from 'lucide-react'
import { SmartIrrigationPlan } from '@/entities/irrigation/types'
import { SupportedLanguage } from '@/shared/i18n'

interface PlanApprovalDialogProps {
  isOpen: boolean
  onClose: () => void
  onConfirm: (notes?: string) => Promise<void>
  plan: SmartIrrigationPlan
  fieldName: string
  cropType: string | null
  waterRemainingM3: number
  isLoading?: boolean
  lang: SupportedLanguage
}

export function PlanApprovalDialog({
  isOpen,
  onClose,
  onConfirm,
  plan,
  fieldName,
  cropType,
  waterRemainingM3,
  isLoading,
  lang,
}: PlanApprovalDialogProps) {
  const [acknowledged, setAcknowledged] = useState(false)
  const [notes, setNotes] = useState('')

  if (!isOpen) return null

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!acknowledged) return
    await onConfirm(notes)
    onClose()
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="bg-card border border-border w-full max-w-lg rounded-2xl shadow-2xl overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="px-6 py-4 border-b border-border flex items-center justify-between">
          <div className="flex items-center space-x-2.5">
            <div className="h-8 w-8 rounded-lg bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 flex items-center justify-center">
              <ShieldCheck className="h-5 w-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-foreground">
                {lang === 'uz' ? 'Sugʻorish Rejasini Insoniy Tasdiqlash' : 'Утверждение плана полива'}
              </h3>
              <p className="text-xs text-muted-foreground">
                {fieldName} • {cropType || 'Ekin'}
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="text-muted-foreground hover:text-foreground transition-colors p-1"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Content Body */}
        <form onSubmit={handleSubmit} className="p-6 space-y-4 overflow-y-auto">
          {/* Summary Box */}
          <div className="p-4 rounded-xl bg-muted/30 border border-border/80 space-y-3">
            <div className="grid grid-cols-2 gap-2 text-xs">
              <div>
                <span className="text-muted-foreground">{lang === 'uz' ? 'Rejalashtirilgan hajm:' : 'Объем:'}</span>
                <p className="font-bold font-mono text-foreground text-sm">
                  {plan.recommendedVolumeM3.toFixed(1)} m³
                </p>
              </div>
              <div>
                <span className="text-muted-foreground">{lang === 'uz' ? 'Davomiyligi:' : 'Длительность:'}</span>
                <p className="font-bold font-mono text-foreground text-sm">
                  {plan.estimatedDurationMinutes} daqiqa
                </p>
              </div>
              <div>
                <span className="text-muted-foreground">{lang === 'uz' ? 'Mavjud suv limiti:' : 'Остаток лимита:'}</span>
                <p className="font-bold font-mono text-teal-600 dark:text-teal-400 text-sm">
                  {Math.round(waterRemainingM3).toLocaleString()} m³
                </p>
              </div>
              <div>
                <span className="text-muted-foreground">{lang === 'uz' ? 'Ishonch darajasi:' : 'Доверие:'}</span>
                <p className="font-bold text-foreground text-sm">{plan.confidenceLevel || 'HIGH'}</p>
              </div>
            </div>

            {plan.reason && (
              <p className="text-xs text-muted-foreground border-t border-border/60 pt-2 leading-relaxed">
                {plan.reason}
              </p>
            )}
          </div>

          {/* Mandatory Constraint Acknowledgement */}
          <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/30 space-y-2">
            <div className="flex items-start space-x-2.5">
              <AlertTriangle className="h-4 w-4 text-amber-600 dark:text-amber-400 shrink-0 mt-0.5" />
              <label htmlFor="ack-checkbox" className="text-xs text-foreground font-medium cursor-pointer leading-relaxed">
                {lang === 'uz'
                  ? 'Men barcha operatsion cheklovlar, gidravlik xavfsizlik va mavsumiy suv limitini koʻrib chiqdim hamda ushbu sugʻorish amaliyotini tasdiqlayman.'
                  : 'Я ознакомлен со всеми эксплуатационными ограничениями, гидравлическими параметрами и сезонным лимитом воды, и утверждаю проведение данного полива.'}
              </label>
            </div>
            <div className="pl-6.5 pt-1">
              <input
                id="ack-checkbox"
                type="checkbox"
                checked={acknowledged}
                onChange={(e) => setAcknowledged(e.target.checked)}
                className="h-4 w-4 rounded border-border text-primary focus:ring-primary cursor-pointer"
              />
              <span className="ml-2 text-xs text-muted-foreground font-semibold">
                {lang === 'uz' ? 'Majburiy tasdiqlash belgisi' : 'Обязательное подтверждение'}
              </span>
            </div>
          </div>

          {/* Optional Notes */}
          <div className="space-y-1.5">
            <label className="text-xs font-medium text-foreground">
              {lang === 'uz' ? 'Operator izohi (ixtiyoriy)' : 'Примечания оператора (необязательно)'}
            </label>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder={lang === 'uz' ? 'Masalan: Tungi navbat uchun tayyorlandi...' : 'Примечания к поливу...'}
              rows={2}
              className="w-full text-xs p-2.5 rounded-lg border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary/20"
            />
          </div>

          {/* Footer Actions */}
          <div className="pt-2 flex items-center justify-end space-x-2 border-t border-border">
            <button
              type="button"
              onClick={onClose}
              disabled={isLoading}
              className="px-4 py-2 rounded-lg text-xs font-medium text-muted-foreground hover:bg-muted transition-colors"
            >
              {lang === 'uz' ? 'Bekor qilish' : 'Отмена'}
            </button>
            <button
              type="submit"
              disabled={!acknowledged || isLoading}
              className={`px-4 py-2 rounded-lg text-xs font-semibold text-white shadow-sm transition-all ${
                acknowledged && !isLoading
                  ? 'bg-emerald-600 hover:bg-emerald-700 cursor-pointer'
                  : 'bg-emerald-600/40 cursor-not-allowed opacity-60'
              }`}
            >
              {isLoading
                ? lang === 'uz'
                  ? 'Tasdiqlanmoqda...'
                  : 'Утверждение...'
                : lang === 'uz'
                ? 'Tasdiqlashni yakunlash'
                : 'Подтвердить план'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
