import React, { useState } from 'react'
import { Button } from '@/shared/ui/button'
import { Layers, X } from 'lucide-react'
import { useCreateBaseline } from '../queries/usePerformanceQueries'

interface Props {
  isOpen: boolean
  onClose: () => void
}

export const CreateBaselineDialog: React.FC<Props> = ({ isOpen, onClose }) => {
  const createMutation = useCreateBaseline()

  const [indicatorCode, setIndicatorCode] = useState('WDP_DELIVERY_FULFILLMENT')
  const [name, setName] = useState('')
  const [scopeType, setScopeType] = useState('DISTRICT')
  const [scopeName, setScopeName] = useState('Yangiariq Tumani')
  const [baselineValue, setBaselineValue] = useState<number>(85.0)
  const [measurementUnit, setMeasurementUnit] = useState('PERCENT')
  const [dataCompletenessPct, setDataCompletenessPct] = useState<number>(95.0)
  const [seasonContext, setSeasonContext] = useState('MAIN_IRRIGATION_SEASON')

  if (!isOpen) return null

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const now = new Date()
    const past60 = new Date(now.getTime() - 60 * 86400000)
    const past30 = new Date(now.getTime() - 30 * 86400000)

    try {
      await createMutation.mutateAsync({
        indicatorCode,
        name,
        scopeType,
        scopeName,
        periodStart: past60.toISOString(),
        periodEnd: past30.toISOString(),
        baselineValue: Number(baselineValue),
        measurementUnit,
        dataCompletenessPct: Number(dataCompletenessPct),
        evidenceClassification: 'CALCULATED',
        baselineQuality: 'VERIFIED_BASELINE',
        calculationMethod: 'HISTORICAL_WEIGHTED_AVERAGE',
        seasonContext,
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
            <Layers className="w-5 h-5 text-primary" />
            <h3 className="text-sm font-bold text-foreground">Yangi Tarixiy Baza (Baseline) Qoʻshish</h3>
          </div>
          <Button variant="ghost" size="sm" onClick={onClose} className="h-7 w-7 p-0">
            <X className="w-4 h-4" />
          </Button>
        </div>

        <form onSubmit={handleSubmit} className="p-4 space-y-3 text-xs overflow-y-auto">
          <div>
            <label className="font-medium text-foreground">Baza Nomi</label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Masalan: 2025-yilgi mavsum oʻrtacha yetkazish bazasi"
              className="mt-1 w-full px-3 py-1.5 rounded-lg border border-border bg-background text-foreground"
            />
          </div>

          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="font-medium text-foreground">Indikator Kodi</label>
              <select
                value={indicatorCode}
                onChange={(e) => setIndicatorCode(e.target.value)}
                className="mt-1 w-full px-2.5 py-1.5 rounded-lg border border-border bg-background text-foreground"
              >
                <option value="WDP_DELIVERY_FULFILLMENT">Yetkazib berish rejasi (%)</option>
                <option value="CCP_LOSS_PERCENTAGE">Kanal yoʻqotishi (%)</option>
                <option value="DEO_EQUITY_CV">Taqsimot tengligi (CV)</option>
                <option value="CAE_RECURRENCE_RATE">Hodisalar qaytalanishi (%)</option>
              </select>
            </div>
            <div>
              <label className="font-medium text-foreground">Qamrov Turi</label>
              <select
                value={scopeType}
                onChange={(e) => setScopeType(e.target.value)}
                className="mt-1 w-full px-2.5 py-1.5 rounded-lg border border-border bg-background text-foreground"
              >
                <option value="DISTRICT">Tuman miqyosida (District)</option>
                <option value="CANAL_REACH">Kanal tarmogʻi (Canal Reach)</option>
                <option value="WUA">WUA hududi</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="font-medium text-foreground">Hudud Nomi</label>
              <input
                type="text"
                required
                value={scopeName}
                onChange={(e) => setScopeName(e.target.value)}
                className="mt-1 w-full px-3 py-1.5 rounded-lg border border-border bg-background text-foreground"
              />
            </div>
            <div>
              <label className="font-medium text-foreground">Oʻlchov Birligi</label>
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

          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="font-medium text-foreground">Bazaviy Qiymat</label>
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
              <label className="font-medium text-foreground">Maʼlumot Toʻliqligi (%)</label>
              <input
                type="number"
                step="0.1"
                required
                value={dataCompletenessPct}
                onChange={(e) => setDataCompletenessPct(Number(e.target.value))}
                className="mt-1 w-full px-3 py-1.5 rounded-lg border border-border bg-background text-foreground"
              />
            </div>
          </div>

          <div>
            <label className="font-medium text-foreground">Mavsum Konteksti</label>
            <input
              type="text"
              value={seasonContext}
              onChange={(e) => setSeasonContext(e.target.value)}
              className="mt-1 w-full px-3 py-1.5 rounded-lg border border-border bg-background text-foreground"
            />
          </div>

          <div className="pt-2 border-t border-border flex justify-end gap-2">
            <Button type="button" variant="outline" onClick={onClose}>
              Bekor qilish
            </Button>
            <Button type="submit" disabled={createMutation.isPending}>
              {createMutation.isPending ? 'Saqlanmoqda...' : 'Bazani Saqlash'}
            </Button>
          </div>
        </form>
      </div>
    </div>
  )
}
