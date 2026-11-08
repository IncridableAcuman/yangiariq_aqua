import React, { useState } from 'react'
import { Button } from '@/shared/ui/button'
import { FileText, Printer, ShieldAlert, X } from 'lucide-react'
import { performanceApi } from '@/entities/performance'
import { PerformanceReport } from '@/entities/performance'

interface Props {
  isOpen: boolean
  onClose: () => void
}

export const PerformanceReportDialog: React.FC<Props> = ({ isOpen, onClose }) => {
  const [report, setReport] = useState<PerformanceReport | null>(null)
  const [loading, setLoading] = useState(false)

  const handleFetchReport = async () => {
    setLoading(true)
    try {
      const data = await performanceApi.generateReport('DISTRICT', undefined, 'MONTHLY')
      setReport(data)
    } catch (err) {
      console.error(err)
    } finally {
      setLoading(false)
    }
  }

  React.useEffect(() => {
    if (isOpen) {
      handleFetchReport()
    }
  }, [isOpen])

  const handlePrint = () => {
    window.print()
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/80 backdrop-blur-sm animate-in fade-in">
      <div className="bg-card border border-border rounded-xl shadow-xl w-full max-w-2xl max-h-[85vh] overflow-hidden flex flex-col">
        <div className="p-4 border-b border-border flex items-center justify-between">
          <div className="flex items-center gap-2">
            <FileText className="w-5 h-5 text-primary" />
            <h3 className="text-sm font-bold text-foreground">Mintaqaviy Operatsion Samaradorlik Hisoboti</h3>
          </div>
          <Button variant="ghost" size="sm" onClick={onClose} className="h-7 w-7 p-0">
            <X className="w-4 h-4" />
          </Button>
        </div>

        <div className="p-4 overflow-y-auto space-y-4 text-xs">
          {loading ? (
            <div className="py-12 text-center text-xs text-muted-foreground">
              Hisobot maʼlumotlari yigʻilmoqda...
            </div>
          ) : report ? (
            <div className="space-y-4">
              {/* Disclaimer banner */}
              <div className="p-3 rounded-lg bg-amber-500/10 border border-amber-500/30 text-amber-700 dark:text-amber-400 flex items-start gap-2">
                <ShieldAlert className="w-4 h-4 shrink-0 mt-0.5" />
                <p className="text-[11px] leading-relaxed">
                  <strong>Eslatma:</strong> {report.legalDisclaimer}
                </p>
              </div>

              {/* Header metadata */}
              <div className="grid grid-cols-2 gap-2 p-3 bg-muted/40 rounded-lg border border-border">
                <div>
                  <span className="text-muted-foreground text-[10px]">Hujjat raqami:</span>
                  <p className="font-mono font-semibold">{report.reportId}</p>
                </div>
                <div>
                  <span className="text-muted-foreground text-[10px]">Tuzuvchi va sana:</span>
                  <p className="font-medium">
                    {report.generatedBy} • {new Date(report.generatedAt).toLocaleDateString()}
                  </p>
                </div>
                <div>
                  <span className="text-muted-foreground text-[10px]">Hisobot davri:</span>
                  <p className="font-medium">
                    {new Date(report.reportingPeriodStart).toLocaleDateString()} —{' '}
                    {new Date(report.reportingPeriodEnd).toLocaleDateString()}
                  </p>
                </div>
                <div>
                  <span className="text-muted-foreground text-[10px]">Qamrov hududi:</span>
                  <p className="font-semibold text-primary">{report.geographicScope}</p>
                </div>
              </div>

              {/* Core metrics summary */}
              <div className="grid grid-cols-3 gap-2">
                <div className="p-2.5 rounded-lg bg-card border border-border text-center">
                  <span className="text-muted-foreground text-[10px]">Yetkazib Berish:</span>
                  <p className="text-base font-bold text-emerald-500">
                    {report.overview.deliveryFulfillmentPct.toFixed(1)}%
                  </p>
                </div>
                <div className="p-2.5 rounded-lg bg-card border border-border text-center">
                  <span className="text-muted-foreground text-[10px]">Kanal Yoʻqotishi:</span>
                  <p className="text-base font-bold text-blue-500">
                    {report.overview.averageConveyanceLossPct.toFixed(1)}%
                  </p>
                </div>
                <div className="p-2.5 rounded-lg bg-card border border-border text-center">
                  <span className="text-muted-foreground text-[10px]">Taqsimot Tengligi:</span>
                  <p className="text-base font-bold text-indigo-500">
                    CV {report.overview.distributionEquityCv.toFixed(2)}
                  </p>
                </div>
              </div>

              {/* Evaluations list */}
              <div className="space-y-1.5">
                <h4 className="font-semibold text-foreground">Baholash Yozuvlari (Evaluations):</h4>
                <div className="border border-border rounded-lg overflow-hidden">
                  <table className="w-full text-[11px] text-left">
                    <thead className="bg-muted/60 text-muted-foreground uppercase text-[9px]">
                      <tr>
                        <th className="py-2 px-2.5">Raqami</th>
                        <th className="py-2 px-2.5">Indikator</th>
                        <th className="py-2 px-2.5">Natija</th>
                        <th className="py-2 px-2.5">Holati</th>
                        <th className="py-2 px-2.5">Klassifikatsiya</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-border">
                      {report.evaluations.map((ev) => (
                        <tr key={ev.id}>
                          <td className="py-2 px-2.5 font-mono">{ev.evaluationNumber}</td>
                          <td className="py-2 px-2.5">{ev.indicatorCode}</td>
                          <td className="py-2 px-2.5 font-bold">
                            {ev.calculatedValue} {ev.measurementUnit}
                          </td>
                          <td className="py-2 px-2.5 text-emerald-600">{ev.targetCompliance}</td>
                          <td className="py-2 px-2.5">{ev.evidenceClassification}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>

              <div className="pt-2 border-t border-border flex justify-end gap-2">
                <Button variant="outline" onClick={handlePrint} className="gap-1.5">
                  <Printer className="w-3.5 h-3.5" />
                  Chop etish
                </Button>
                <Button onClick={onClose}>Yopish</Button>
              </div>
            </div>
          ) : null}
        </div>
      </div>
    </div>
  )
}
