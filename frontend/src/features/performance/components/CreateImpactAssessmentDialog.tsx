import React, { useState } from 'react'
import { Button } from '@/shared/ui/button'
import { Sparkles, X } from 'lucide-react'
import { useCreateImpactAssessment } from '../queries/usePerformanceQueries'

interface Props {
  isOpen: boolean
  onClose: () => void
}

export const CreateImpactAssessmentDialog: React.FC<Props> = ({ isOpen, onClose }) => {
  const createMutation = useCreateImpactAssessment()

  const [title, setTitle] = useState('')
  const [interventionType, setInterventionType] = useState('CANAL_MAINTENANCE')
  const [interventionReference, setInterventionReference] = useState('')
  const [indicatorCode, setIndicatorCode] = useState('CCP_LOSS_PERCENTAGE')
  const [scopeType, setScopeType] = useState('CANAL_REACH')
  const [scopeName, setScopeName] = useState('')
  const [baselineValue, setBaselineValue] = useState<number>(12.0)
  const [postInterventionValue, setPostInterventionValue] = useState<number>(7.5)
  const [measurementUnit, setMeasurementUnit] = useState('PERCENT')
  const [confoundingFactors, setConfoundingFactors] = useState('')
  const [methodologyNotes, setMethodologyNotes] = useState('Gidropost koʻrsatkichlari va suv balansi solishtirmasi')

  if (!isOpen) return null

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const now = new Date()
    const past30 = new Date(now.getTime() - 30 * 86400000)
    const past60 = new Date(now.getTime() - 60 * 86400000)

    try {
      await createMutation.mutateAsync({
        title,
        interventionType,
        interventionId: '00000000-0000-0000-0000-000000000001',
        interventionReference: interventionReference || 'MANUAL-INTERVENTION',
        interventionDate: past30.toISOString(),
        indicatorCode,
        scopeType,
        scopeName: scopeName || 'Yangiariq Boʻzyop Kanali',
        baselinePeriodStart: past60.toISOString(),
        baselinePeriodEnd: past30.toISOString(),
        baselineValue: Number(baselineValue),
        evaluationPeriodStart: past30.toISOString(),
        evaluationPeriodEnd: now.toISOString(),
        postInterventionValue: Number(postInterventionValue),
        measurementUnit,
        evidenceClassification: 'CALCULATED',
        baselineQuality: 'VERIFIED_BASELINE',
        confoundingFactors: confoundingFactors || undefined,
        methodologyNotes: methodologyNotes || 'Mass-balans va gidropost koʻrsatkichlari solishtirmasi',
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
            <Sparkles className="w-5 h-5 text-primary" />
            <h3 className="text-sm font-bold text-foreground">Yangi Taʼsir Baholash Yaratish</h3>
          </div>
          <Button variant="ghost" size="sm" onClick={onClose} className="h-7 w-7 p-0">
            <X className="w-4 h-4" />
          </Button>
        </div>

        <form onSubmit={handleSubmit} className="p-4 space-y-3 text-xs overflow-y-auto">
          <div>
            <label className="font-medium text-foreground">Sarlavha</label>
            <input
              type="text"
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Masalan: Boʻzyop kanali oʻzanini betonlash natijasi"
              className="mt-1 w-full px-3 py-1.5 rounded-lg border border-border bg-background text-foreground"
            />
          </div>

          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="font-medium text-foreground">Intervensiya Turi</label>
              <select
                value={interventionType}
                onChange={(e) => setInterventionType(e.target.value)}
                className="mt-1 w-full px-2.5 py-1.5 rounded-lg border border-border bg-background text-foreground"
              >
                <option value="CANAL_MAINTENANCE">Kanal taʼmiri & Betonlash</option>
                <option value="DISPATCH_SCHEDULE">Dispecherlik jadvali</option>
                <option value="FIELD_IRRIGATION_PLAN">Dala sugʻorish rejasi</option>
                <option value="CORRECTIVE_ACTION">Tuzatish chorasi</option>
              </select>
            </div>
            <div>
              <label className="font-medium text-foreground">Harakat Hujjati / Kod</label>
              <input
                type="text"
                value={interventionReference}
                onChange={(e) => setInterventionReference(e.target.value)}
                placeholder="INC-2026-001 yoki REPAIR-42"
                className="mt-1 w-full px-3 py-1.5 rounded-lg border border-border bg-background text-foreground"
              />
            </div>
          </div>

          <div className="grid grid-cols-3 gap-2">
            <div>
              <label className="font-medium text-foreground">Indikator</label>
              <select
                value={indicatorCode}
                onChange={(e) => setIndicatorCode(e.target.value)}
                className="mt-1 w-full px-2.5 py-1.5 rounded-lg border border-border bg-background text-foreground"
              >
                <option value="CCP_LOSS_PERCENTAGE">Kanal yoʻqotishi (%)</option>
                <option value="WDP_DELIVERY_FULFILLMENT">Yetkazib berish rejasi (%)</option>
                <option value="DEO_EQUITY_CV">Taqsimot tengligi (CV)</option>
              </select>
            </div>
            <div>
              <label className="font-medium text-foreground">Qamrov Turi</label>
              <select
                value={scopeType}
                onChange={(e) => setScopeType(e.target.value)}
                className="mt-1 w-full px-2.5 py-1.5 rounded-lg border border-border bg-background text-foreground"
              >
                <option value="CANAL_REACH">Kanal qismi</option>
                <option value="DISTRICT">Tuman</option>
                <option value="WUA">WUA</option>
              </select>
            </div>
            <div>
              <label className="font-medium text-foreground">Birlik</label>
              <select
                value={measurementUnit}
                onChange={(e) => setMeasurementUnit(e.target.value)}
                className="mt-1 w-full px-2.5 py-1.5 rounded-lg border border-border bg-background text-foreground"
              >
                <option value="PERCENT">Foiz (%)</option>
                <option value="M3">m³</option>
                <option value="M3_PER_HA">m³/ha</option>
              </select>
            </div>
          </div>

          <div>
            <label className="font-medium text-foreground">Qamrov Obyekti Nomi</label>
            <input
              type="text"
              value={scopeName}
              onChange={(e) => setScopeName(e.target.value)}
              placeholder="Boʻzyop Kanali PK12"
              className="mt-1 w-full px-3 py-1.5 rounded-lg border border-border bg-background text-foreground"
            />
          </div>

          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="font-medium text-foreground">Baza Qiymati</label>
              <input
                type="number"
                step="0.01"
                required
                value={baselineValue}
                onChange={(e) => setBaselineValue(Number(e.target.value))}
                className="mt-1 w-full px-3 py-1.5 rounded-lg border border-border bg-background text-foreground"
              />
            </div>
            <div>
              <label className="font-medium text-foreground">Hozirgi Natija</label>
              <input
                type="number"
                step="0.01"
                required
                value={postInterventionValue}
                onChange={(e) => setPostInterventionValue(Number(e.target.value))}
                className="mt-1 w-full px-3 py-1.5 rounded-lg border border-border bg-background text-foreground"
              />
            </div>
          </div>

          <div>
            <label className="font-medium text-foreground">Tashqi Omil / Chalgʻituvchilar (Confounders)</label>
            <input
              type="text"
              value={confoundingFactors}
              onChange={(e) => setConfoundingFactors(e.target.value)}
              placeholder="Masalan: Davr mobaynida 12 mm yogʻingarchilik kuzatildi"
              className="mt-1 w-full px-3 py-1.5 rounded-lg border border-border bg-background text-foreground"
            />
          </div>

          <div>
            <label className="font-medium text-foreground">Metodologiya va Oʻlchov Asosi</label>
            <textarea
              rows={2}
              required
              value={methodologyNotes}
              onChange={(e) => setMethodologyNotes(e.target.value)}
              placeholder="Oʻlchov qaysi datchiklar yoki qabul dalolatnomalari orqali amalga oshirildi..."
              className="mt-1 w-full px-3 py-1.5 rounded-lg border border-border bg-background text-foreground resize-none"
            />
          </div>

          <div className="pt-2 border-t border-border flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={onClose}>
              Bekor qilish
            </Button>
            <Button type="submit" disabled={createMutation.isPending}>
              {createMutation.isPending ? 'Saqlanmoqda...' : 'Baholashni Saqlash'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
