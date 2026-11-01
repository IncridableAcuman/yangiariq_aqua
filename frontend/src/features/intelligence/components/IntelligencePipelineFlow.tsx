import React from 'react'
import { Card, CardContent } from '@/shared/ui/card'
import { Activity, ShieldCheck, Cpu, ArrowRight, UserCheck, Calendar } from 'lucide-react'

export const IntelligencePipelineFlow: React.FC = () => {
  const steps = [
    {
      title: 'Telemetry',
      desc: 'Physical sensors & quality gate',
      icon: Activity,
      color: 'text-sky-600 bg-sky-50 dark:bg-sky-950/50 dark:text-sky-400',
    },
    {
      title: 'Assessment',
      desc: 'Deterministic agronomic signals',
      icon: Cpu,
      color: 'text-amber-600 bg-amber-50 dark:bg-amber-950/50 dark:text-amber-400',
    },
    {
      title: 'Safety Interlock',
      desc: 'Pipe burst & quota gates',
      icon: ShieldCheck,
      color: 'text-rose-600 bg-rose-50 dark:bg-rose-950/50 dark:text-rose-400',
    },
    {
      title: 'Recommendation',
      desc: 'Advisory volume & window',
      icon: ArrowRight,
      color: 'text-indigo-600 bg-indigo-50 dark:bg-indigo-950/50 dark:text-indigo-400',
    },
    {
      title: 'Human Review',
      desc: 'Farmer verification & approval',
      icon: UserCheck,
      color: 'text-emerald-600 bg-emerald-50 dark:bg-emerald-950/50 dark:text-emerald-400',
    },
    {
      title: 'Smart Plan',
      desc: 'Authorized operational schedule',
      icon: Calendar,
      color: 'text-blue-600 bg-blue-50 dark:bg-blue-950/50 dark:text-blue-400',
    },
  ]

  return (
    <Card className="border border-border/70 bg-gradient-to-r from-slate-50 via-white to-slate-50 dark:from-slate-900/40 dark:via-slate-900/20 dark:to-slate-900/40">
      <CardContent className="p-4">
        <div className="flex flex-col md:flex-row items-center justify-between gap-3 overflow-x-auto">
          {steps.map((step, idx) => {
            const Icon = step.icon
            return (
              <React.Fragment key={step.title}>
                <div className="flex items-center gap-2.5 min-w-[130px]">
                  <div className={`p-2 rounded-lg ${step.color} shrink-0`}>
                    <Icon className="w-4 h-4" />
                  </div>
                  <div>
                    <div className="text-xs font-semibold leading-tight">{step.title}</div>
                    <div className="text-[11px] text-muted-foreground leading-tight">{step.desc}</div>
                  </div>
                </div>
                {idx < steps.length - 1 && (
                  <ArrowRight className="hidden md:block w-4 h-4 text-muted-foreground/40 shrink-0" />
                )}
              </React.Fragment>
            )
          })}
        </div>
      </CardContent>
    </Card>
  )
}
