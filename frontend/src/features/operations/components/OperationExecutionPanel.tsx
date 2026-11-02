import { useState } from 'react'
import { PlayCircle, CheckCircle2, Droplets, Info } from 'lucide-react'
import { OperationalWorkflowState, SmartIrrigationPlan } from '@/entities/operations'
import { SupportedLanguage } from '@/shared/i18n'

interface OperationExecutionPanelProps {
  plan: SmartIrrigationPlan | null
  currentState: OperationalWorkflowState
  lang: SupportedLanguage
  onStartOperation: (notes?: string) => Promise<void>
  onCompleteOperation: (volumeM3: number, notes?: string) => Promise<void>
  isLoading?: boolean
}

export function OperationExecutionPanel({
  plan,
  currentState,
  lang,
  onStartOperation,
  onCompleteOperation,
  isLoading,
}: OperationExecutionPanelProps) {
  const [completeDialogOpen, setCompleteDialogOpen] = useState(false)
  const [actualVolume, setActualVolume] = useState<string>(
    plan?.recommendedVolumeM3 ? plan.recommendedVolumeM3.toString() : '400'
  )
  const [notes, setNotes] = useState('')

  if (!plan) return null

  const isReadyOrScheduled = currentState === 'READY' || currentState === 'SCHEDULED' || currentState === 'APPROVED'
  const isInProgress = currentState === 'IN_PROGRESS'

  const handleCompleteSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const vol = parseFloat(actualVolume) || plan.recommendedVolumeM3
    await onCompleteOperation(vol, notes)
    setCompleteDialogOpen(false)
  }

  return (
    <div className="bg-card border border-border rounded-xl p-5 shadow-sm space-y-4">
      <div className="flex items-center justify-between border-b border-border pb-3">
        <h2 className="text-base font-semibold text-foreground">
          {lang === 'uz' ? 'Dala Operatsiyasini Boshqarish (Execution)' : 'Управление поливом'}
        </h2>
        <span
          className={`inline-flex items-center px-2.5 py-0.5 rounded text-xs font-semibold ${
            isInProgress
              ? 'bg-blue-50 text-blue-700 dark:bg-blue-950/40 dark:text-blue-300 border border-blue-200 animate-pulse'
              : 'bg-muted text-muted-foreground'
          }`}
        >
          {isInProgress
            ? lang === 'uz' ? 'Jarayonda (In Progress)' : 'В процессе'
            : lang === 'uz' ? 'Kutilmoqda' : 'Ожидание'}
        </span>
      </div>

      {/* Operational Disclaimer */}
      <div className="flex items-start space-x-2.5 p-3 rounded-lg bg-blue-50/60 border border-blue-200 dark:bg-blue-950/20 dark:border-blue-900 text-xs text-blue-800 dark:text-blue-300">
        <Info className="h-4 w-4 shrink-0 mt-0.5" />
        <p>
          {lang === 'uz'
            ? 'Ushbu tizim qaror qabul qilishni qoʻllab-quvvatlash va operatsiyalarni qayd etish uchun moʻljallangan. Klapan yoki nasoslarni avtomatik ochmaydi. Barcha amaliyotlar dalada operator tomonidan bajariladi va qayd etiladi.'
            : 'Система предназначена для поддержки принятия решений и учета операций. Не производит автоматическое управление задвижками или насосами. Все действия выполняются оператором.'}
        </p>
      </div>

      {/* Execution Actions */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 pt-2">
        <div className="text-xs text-muted-foreground space-y-0.5 w-full sm:w-auto">
          <p>
            <strong>{lang === 'uz' ? 'Rejalashtirilgan hajm: ' : 'План: '}</strong>
            {plan.recommendedVolumeM3.toFixed(1)} m³
          </p>
          <p>
            <strong>{lang === 'uz' ? 'Kutilayotgan davomiylik: ' : 'Длительность: '}</strong>
            {plan.estimatedDurationMinutes} daqiqa
          </p>
        </div>

        <div className="flex items-center space-x-3 w-full sm:w-auto justify-end">
          {isReadyOrScheduled && (
            <button
              type="button"
              disabled={isLoading}
              onClick={() => onStartOperation('Boshlash qoʻlda qayd etildi')}
              className="w-full sm:w-auto inline-flex items-center justify-center px-4 py-2.5 rounded-lg text-xs font-semibold bg-blue-600 text-white hover:bg-blue-700 transition-colors shadow-sm disabled:opacity-50"
            >
              <PlayCircle className="h-4 w-4 mr-1.5" />
              {lang === 'uz' ? 'Poliv Boshlanishini Qayd Etish' : 'Начать полив'}
            </button>
          )}

          {isInProgress && (
            <button
              type="button"
              disabled={isLoading}
              onClick={() => setCompleteDialogOpen(true)}
              className="w-full sm:w-auto inline-flex items-center justify-center px-4 py-2.5 rounded-lg text-xs font-semibold bg-emerald-600 text-white hover:bg-emerald-700 transition-colors shadow-sm disabled:opacity-50"
            >
              <CheckCircle2 className="h-4 w-4 mr-1.5" />
              {lang === 'uz' ? 'Poliv Tugallanishini Qayd Etish' : 'Завершить полив'}
            </button>
          )}
        </div>
      </div>

      {/* Complete Operation Modal */}
      {completeDialogOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="bg-card border border-border w-full max-w-md rounded-2xl shadow-2xl p-6 space-y-4">
            <div className="flex items-center space-x-2 border-b border-border pb-3">
              <CheckCircle2 className="h-5 w-5 text-emerald-600" />
              <h3 className="text-base font-bold text-foreground">
                {lang === 'uz' ? 'Sugʻorish Yakunini Qayd Etish' : 'Завершение полива'}
              </h3>
            </div>

            <form onSubmit={handleCompleteSubmit} className="space-y-4">
              <div className="space-y-1.5">
                <label className="text-xs font-medium text-foreground">
                  {lang === 'uz' ? 'Amalda berilgan suv hajmi (m³)' : 'Фактический объем воды (м³)'}
                </label>
                <div className="relative">
                  <input
                    type="number"
                    step="0.1"
                    min="0"
                    value={actualVolume}
                    onChange={(e) => setActualVolume(e.target.value)}
                    required
                    className="w-full text-xs p-2.5 rounded-lg border border-border bg-background text-foreground pl-8 font-mono focus:outline-none focus:ring-2 focus:ring-primary/20"
                  />
                  <Droplets className="h-4 w-4 text-muted-foreground absolute left-2.5 top-2.5" />
                </div>
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-medium text-foreground">
                  {lang === 'uz' ? 'Yakuniy izoh yoki kuzatuvlar' : 'Примечания оператора'}
                </label>
                <textarea
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  placeholder={lang === 'uz' ? 'Masalan: Poliv meʼyorida yakunlandi, tuproq toʻliq toʻyindi...' : 'Примечания...'}
                  rows={2}
                  className="w-full text-xs p-2.5 rounded-lg border border-border bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary/20"
                />
              </div>

              <div className="pt-2 flex items-center justify-end space-x-2 border-t border-border">
                <button
                  type="button"
                  onClick={() => setCompleteDialogOpen(false)}
                  className="px-4 py-2 rounded-lg text-xs font-medium text-muted-foreground hover:bg-muted"
                >
                  {lang === 'uz' ? 'Bekor qilish' : 'Отмена'}
                </button>
                <button
                  type="submit"
                  disabled={isLoading}
                  className="px-4 py-2 rounded-lg text-xs font-semibold bg-emerald-600 text-white hover:bg-emerald-700 shadow-sm disabled:opacity-50"
                >
                  {lang === 'uz' ? 'Qayd etish va tekshirish' : 'Зафиксировать'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
