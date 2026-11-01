import React from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import { SmartIrrigationPlan } from '@/entities/irrigation'
import {
  Droplets,
  Layers,
  ArrowRight,
  ShieldAlert,
  PlusCircle,
} from 'lucide-react'
import { formatVolume } from '@/features/water/lib/waterFormatters'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { cn } from '@/shared/lib/utils'

interface FieldPriorityItem {
  field: {
    id: string
    name: string
    cropType: string
    areaHectares: number
    soilType?: string
  }
  activePlan?: SmartIrrigationPlan | null
  soilMoisturePercent?: number | null
  targetMoisturePercent?: number | null
  calculatedPriority?: 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'
  isBlocked?: boolean
}

interface FieldPriorityTableProps {
  items: FieldPriorityItem[]
  onGeneratePlan: (fieldId: string) => void
  onSelectPlan: (plan: SmartIrrigationPlan) => void
  isGenerating?: boolean
  lang: SupportedLanguage
  className?: string
}

export const FieldPriorityTable: React.FC<FieldPriorityTableProps> = ({
  items,
  onGeneratePlan,
  onSelectPlan,
  isGenerating = false,
  lang,
  className,
}) => {
  const t = translations[lang]

  // Sort by priority (CRITICAL > HIGH > MEDIUM > LOW)
  const priorityRank: Record<string, number> = {
    CRITICAL: 4,
    HIGH: 3,
    MEDIUM: 2,
    LOW: 1,
  }

  const sortedItems = [...items].sort((a, b) => {
    const prioA = a.activePlan?.priority || a.calculatedPriority || 'LOW'
    const prioB = b.activePlan?.priority || b.calculatedPriority || 'LOW'
    return (priorityRank[prioB] || 0) - (priorityRank[prioA] || 0)
  })

  return (
    <Card className={cn('border border-border/70 shadow-sm bg-card overflow-hidden', className)}>
      <CardHeader className="p-4 pb-3 border-b border-border/60 bg-muted/20 flex flex-row items-center justify-between">
        <div className="flex items-center gap-2">
          <Layers className="w-4 h-4 text-primary" />
          <CardTitle className="text-sm font-bold text-foreground">
            {t.irrigationPlanning?.priorityTable?.title || 'Dala Navbatlari va Sugʻorish Ehtiyoji'}
          </CardTitle>
        </div>
        <span className="text-xs text-muted-foreground">
          {sortedItems.length} {t.irrigationPlanning?.priorityTable?.parcelsCount || 'dala maydoni'}
        </span>
      </CardHeader>

      <CardContent className="p-0">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs border-collapse">
            <thead>
              <tr className="border-b border-border/60 bg-muted/40 text-muted-foreground font-semibold">
                <th className="p-3 pl-4">{t.irrigationPlanning?.priorityTable?.field || 'Dala & Ekin'}</th>
                <th className="p-3">{t.irrigationPlanning?.priorityTable?.moisture || 'Namlik (Joriy / Maqsad)'}</th>
                <th className="p-3">{t.irrigationPlanning?.priorityTable?.priority || 'Ustuvorlik'}</th>
                <th className="p-3">{t.irrigationPlanning?.priorityTable?.plannedVolume || 'Hajm / Holat'}</th>
                <th className="p-3 pr-4 text-right">{t.common?.actions || 'Amallar'}</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/40">
              {sortedItems.map(({ field, activePlan, soilMoisturePercent, targetMoisturePercent, calculatedPriority, isBlocked }) => {
                const effectivePriority = activePlan?.priority || calculatedPriority || 'MEDIUM'
                const effectiveBlocked = activePlan?.isBlocked || isBlocked || false

                return (
                  <tr
                    key={field.id}
                    className="hover:bg-muted/30 transition-colors group cursor-default"
                  >
                    {/* Field & Crop */}
                    <td className="p-3 pl-4">
                      <div className="font-bold text-foreground group-hover:text-primary transition-colors">
                        {field.name}
                      </div>
                      <div className="flex items-center gap-1.5 text-[11px] text-muted-foreground">
                        <span>{field.cropType}</span>
                        <span>•</span>
                        <span>{Number(field.areaHectares).toFixed(1)} ga</span>
                      </div>
                    </td>

                    {/* Moisture */}
                    <td className="p-3">
                      <div className="flex items-center gap-1.5 font-semibold text-foreground">
                        <Droplets className="w-3.5 h-3.5 text-sky-500" />
                        <span>
                          {soilMoisturePercent !== null && soilMoisturePercent !== undefined
                            ? `${soilMoisturePercent.toFixed(1)}%`
                            : activePlan?.currentMoisturePercent
                            ? `${activePlan.currentMoisturePercent}%`
                            : '—'}
                        </span>
                        <span className="text-muted-foreground font-normal">/</span>
                        <span className="text-muted-foreground">
                          {targetMoisturePercent !== null && targetMoisturePercent !== undefined
                            ? `${targetMoisturePercent}%`
                            : activePlan?.targetMoisturePercent
                            ? `${activePlan.targetMoisturePercent}%`
                            : '70%'}
                        </span>
                      </div>
                    </td>

                    {/* Priority */}
                    <td className="p-3">
                      <Badge
                        variant="outline"
                        className={cn(
                          'text-[10px] uppercase font-bold py-0.5 px-2',
                          effectivePriority === 'CRITICAL' && 'border-rose-500 text-rose-600 bg-rose-50 dark:bg-rose-950/30',
                          effectivePriority === 'HIGH' && 'border-amber-500 text-amber-600 bg-amber-50 dark:bg-amber-950/30',
                          effectivePriority === 'MEDIUM' && 'border-sky-500 text-sky-600 bg-sky-50 dark:bg-sky-950/30',
                          effectivePriority === 'LOW' && 'text-muted-foreground'
                        )}
                      >
                        {effectivePriority}
                      </Badge>
                    </td>

                    {/* Planned Volume / Status */}
                    <td className="p-3">
                      {effectiveBlocked ? (
                        <div className="flex items-center gap-1 text-rose-600 dark:text-rose-400 font-bold">
                          <ShieldAlert className="w-3.5 h-3.5" />
                          <span>BLOKLANGAN</span>
                        </div>
                      ) : activePlan ? (
                        <div>
                          <span className="font-bold text-foreground block">
                            {formatVolume(activePlan.recommendedVolumeM3)}
                          </span>
                          <span className="text-[10px] text-muted-foreground">
                            {activePlan.status} • {activePlan.plannedDate}
                          </span>
                        </div>
                      ) : (
                        <span className="text-muted-foreground italic text-[11px]">
                          {t.irrigationPlanning?.priorityTable?.noActivePlan || 'Reja yoʻq'}
                        </span>
                      )}
                    </td>

                    {/* Actions */}
                    <td className="p-3 pr-4 text-right">
                      {activePlan ? (
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => onSelectPlan(activePlan)}
                          className="h-7 text-xs px-2 text-primary hover:text-primary"
                        >
                          <span>{t.irrigationPlanning?.priorityTable?.viewPlan || 'Rejani koʻrish'}</span>
                          <ArrowRight className="w-3.5 h-3.5 ml-1" />
                        </Button>
                      ) : (
                        <Button
                          variant="outline"
                          size="sm"
                          disabled={isGenerating}
                          onClick={() => onGeneratePlan(field.id)}
                          className="h-7 text-xs px-2 border-primary/40 text-primary hover:bg-primary/10"
                        >
                          <PlusCircle className="w-3.5 h-3.5 mr-1" />
                          <span>{t.irrigationPlanning?.priorityTable?.generate || 'AI Reja'}</span>
                        </Button>
                      )}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      </CardContent>
    </Card>
  )
}
