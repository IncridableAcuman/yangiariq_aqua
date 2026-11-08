import React, { useState } from 'react'
import { Button } from '@/shared/ui/button'
import { Target, X } from 'lucide-react'
import { useCreateTarget } from '../queries/usePerformanceQueries'

interface Props {
  isOpen: boolean
  onClose: () => void
}

export const CreateTargetDialog: React.FC<Props> = ({ isOpen, onClose }) => {
  const createMutation = useCreateTarget()

  const [indicatorCode, setIndicatorCode] = useState('WDP_DELIVERY_FULFILLMENT')
  const [title, setTitle] = useState('')
  const [scopeType, setScopeType] = useState('DISTRICT')
  const [scopeName, setScopeName] = useState('Yangiariq Tumani')
  const [targetType, setTargetType] = useState('MINIMUM')
  const [targetCategory, setTargetCategory] = useState('OPERATIONAL_TARGET')
  const [targetValue, setTargetValue] = useState<number>(92.0)
  const [measurementUnit, setMeasurementUnit] = useState('PERCENT')
  const [targetOwner, setTargetOwner] = useState('Tuman Suv Xoʻjaligi')
  const [rationale, setRationale] = useState('')

  if (!isOpen) return null

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const now = new Date()
    const future = new Date(now.getTime() + 90 * 86400000)

    try {
      await createMutation.mutateAsync({
        indicatorCode,
        title,
        scopeType,
        scopeName,
        targetType,
        targetCategory,
        targetValue: Number(targetValue),
        measurementUnit,
        effectiveStart: now.toISOString(),
        effectiveEnd: future.toISOString(),
        targetOwner,
        rationale: rationale || 'Operatsion samaradorlik meʼyori',
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
            <Target className="w-5 h-5 text-primary" />
            <h3 className="text-sm font-bold text-foreground">Yangi Operatsion Meʼyor (Target) Belgilash</h3>
          </div>
          <Button variant="ghost" size="sm" onClick={onClose} className="h-7 w-7 p-0">
            <X className="w-4 h-4" />
          </Button>
        </div>

        <form onSubmit={handleSubmit} className="p-4 space-y-3 text-xs overflow-y-auto">
          <div>
            <label className="font-medium text-foreground">Meʼyor Sarlavhasi</label>
            <input
              type="text"
              required
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Masalan: Reja bajarilishi eng kamida 92%"
              className="mt-1 w-full px-3 py-1.5 rounded-lg border border-border bg-background text-foreground"
            />
          </div>

          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="font-medium text-foreground">Indikator</label>
              <select
                value={indicatorCode}
                onChange={(e) => setIndicatorCode(e.target.value)}
                className="mt-1 w-full px-2.5 py-1.5 rounded-lg border border-border bg-background text-foreground"
              >
                <option value="WDP_DELIVERY_FULFILLMENT">Yetkazib berish rejasi (%)</option>
                <option value="CCP_LOSS_PERCENTAGE">Kanal yoʻqotishi (%)</option>
                <option value="DEO_EQUITY_CV">Taqsimot tengligi (CV)</option>
                <option value="CAE_RESOLUTION_RATE">Hal etish darajasi (%)</option>
              </select>
            </div>
            <div>
              <label className="font-medium text-foreground">Meʼyor Kategoriya</label>
              <select
                value={targetCategory}
                onChange={(e) => setTargetCategory(e.target.value)}
                className="mt-1 w-full px-2.5 py-1.5 rounded-lg border border-border bg-background text-foreground"
              >
                <option value="OPERATIONAL_TARGET">Operatsion meʼyor</option>
                <option value="ENGINEERING_CONSTRAINT">Gidrotexnik chegara</option>
                <option value="POLICY_THRESHOLD">Siyosat / Nizomiy chegara</option>
                <option value="PILOT_DEMO_TARGET">Tajriba-sinov meʼyori (Demo)</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="font-medium text-foreground">Meʼyor Turi</label>
              <select
                value={targetType}
                onChange={(e) => setTargetType(e.target.value)}
                className="mt-1 w-full px-2.5 py-1.5 rounded-lg border border-border bg-background text-foreground"
              >
                <option value="MINIMUM">Eng kamida (Minimum)</option>
                <option value="MAXIMUM">Oshmasligi lozim (Maximum)</option>
                <option value="RANGE">Oraliq (Range)</option>
              </select>
            </div>
            <div>
              <label className="font-medium text-foreground">Maqsadli Qiymat</label>
              <input
                type="number"
                step="0.01"
                required
                value={targetValue}
                onChange={(e) => setTargetValue(Number(e.target.value))}
                className="mt-1 w-full px-3 py-1.5 rounded-lg border border-border bg-background text-foreground"
              />
            </div>
          </div>

          <div className="grid grid-cols-3 gap-2">
            <div>
              <label className="font-medium text-foreground">Qamrov Turi</label>
              <select
                value={scopeType}
                onChange={(e) => setScopeType(e.target.value)}
                className="mt-1 w-full px-2.5 py-1.5 rounded-lg border border-border bg-background text-foreground"
              >
                <option value="DISTRICT">Tuman</option>
                <option value="CANAL_REACH">Kanal</option>
                <option value="WUA">WUA</option>
              </select>
            </div>
            <div>
              <label className="font-medium text-foreground">Qamrov Nomi</label>
              <input
                type="text"
                required
                value={scopeName}
                onChange={(e) => setScopeName(e.target.value)}
                className="mt-1 w-full px-3 py-1.5 rounded-lg border border-border bg-background text-foreground"
              />
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
            <label className="font-medium text-foreground">Meʼyor Asosi (Rationale)</label>
            <input
              type="text"
              value={rationale}
              onChange={(e) => setRationale(e.target.value)}
              placeholder="Vazirlik nizomi yoki texnik reglament asosi"
              className="mt-1 w-full px-3 py-1.5 rounded-lg border border-border bg-background text-foreground"
            />
          </div>

          <div>
            <label className="font-medium text-foreground">Masʼul Shaxs / Tashkilot</label>
            <input
              type="text"
              required
              value={targetOwner}
              onChange={(e) => setTargetOwner(e.target.value)}
              className="mt-1 w-full px-3 py-1.5 rounded-lg border border-border bg-background text-foreground"
            />
          </div>

          <div className="pt-2 border-t border-border flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={onClose}>
              Bekor qilish
            </Button>
            <Button type="submit" disabled={createMutation.isPending}>
              {createMutation.isPending ? 'Saqlanmoqda...' : 'Meʼyorni Saqlash'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
