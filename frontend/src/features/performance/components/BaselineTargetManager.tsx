import React, { useState } from 'react'
import { Target, Layers, Plus, CheckCircle2 } from 'lucide-react'
import {
  PerformanceBaseline,
  PerformanceTarget,
} from '@/entities/performance'

interface Props {
  baselines: PerformanceBaseline[]
  targets: PerformanceTarget[]
  onCreateBaselineClick?: () => void
  onCreateTargetClick?: () => void
}

export const BaselineTargetManager: React.FC<Props> = ({
  baselines,
  targets,
  onCreateBaselineClick,
  onCreateTargetClick,
}) => {
  const [activeTab, setActiveTab] = useState<'targets' | 'baselines'>('targets')

  return (
    <div className="p-5 rounded-xl bg-card border border-border shadow-sm space-y-4">
      {/* Tab toggle and actions */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div className="flex items-center gap-2">
          <button
            onClick={() => setActiveTab('targets')}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-colors flex items-center gap-1.5 ${
              activeTab === 'targets'
                ? 'bg-primary text-primary-foreground shadow-sm'
                : 'bg-muted/50 text-muted-foreground hover:bg-muted hover:text-foreground'
            }`}
          >
            <Target className="w-3.5 h-3.5" />
            <span>Operatsion Meʼyorlar (Targets & Limits)</span>
          </button>

          <button
            onClick={() => setActiveTab('baselines')}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition-colors flex items-center gap-1.5 ${
              activeTab === 'baselines'
                ? 'bg-primary text-primary-foreground shadow-sm'
                : 'bg-muted/50 text-muted-foreground hover:bg-muted hover:text-foreground'
            }`}
          >
            <Layers className="w-3.5 h-3.5" />
            <span>Tarixiy Bazalar (Baselines)</span>
          </button>
        </div>

        <button
          onClick={activeTab === 'targets' ? onCreateTargetClick : onCreateBaselineClick}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-lg bg-secondary text-secondary-foreground hover:bg-secondary/80 transition-colors"
        >
          <Plus className="w-3.5 h-3.5" />
          <span>{activeTab === 'targets' ? 'Yangi Meʼyor Qoʻshish' : 'Yangi Baza Qoʻshish'}</span>
        </button>
      </div>

      {/* Targets view */}
      {activeTab === 'targets' && (
        <div className="overflow-x-auto rounded-lg border border-border">
          <table className="w-full text-xs text-left">
            <thead className="bg-muted/50 text-muted-foreground uppercase text-[10px] tracking-wider border-b border-border">
              <tr>
                <th className="py-2.5 px-3">Sarlavha & Indikator</th>
                <th className="py-2.5 px-3">Kategoriya</th>
                <th className="py-2.5 px-3">Turi</th>
                <th className="py-2.5 px-3">Meʼyor Qiymati</th>
                <th className="py-2.5 px-3">Qamrov</th>
                <th className="py-2.5 px-3">Masʼul</th>
                <th className="py-2.5 px-3">Holati</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {targets.map((tgt) => (
                <tr key={tgt.id} className="hover:bg-muted/30 transition-colors">
                  <td className="py-2.5 px-3 font-medium text-foreground">
                    <div>
                      <p className="font-semibold text-foreground">{tgt.title}</p>
                      <span className="font-mono text-[11px] text-primary">{tgt.indicatorCode}</span>
                    </div>
                  </td>
                  <td className="py-2.5 px-3">
                    <span
                      className={`px-2 py-0.5 rounded-full text-[10px] font-medium border ${
                        tgt.targetCategory === 'ENGINEERING_CONSTRAINT'
                          ? 'bg-rose-500/10 text-rose-600 border-rose-500/30'
                          : tgt.targetCategory === 'POLICY_THRESHOLD'
                          ? 'bg-amber-500/10 text-amber-600 border-amber-500/30'
                          : 'bg-blue-500/10 text-blue-600 border-blue-500/30'
                      }`}
                    >
                      {tgt.targetCategory}
                    </span>
                  </td>
                  <td className="py-2.5 px-3 text-muted-foreground">{tgt.targetType}</td>
                  <td className="py-2.5 px-3 font-mono font-bold text-foreground">
                    {tgt.targetValue} {tgt.measurementUnit}
                  </td>
                  <td className="py-2.5 px-3 text-muted-foreground">
                    {tgt.scopeName} ({tgt.scopeType})
                  </td>
                  <td className="py-2.5 px-3 text-muted-foreground text-[11px]">{tgt.targetOwner}</td>
                  <td className="py-2.5 px-3">
                    <span className="inline-flex items-center gap-1 text-emerald-600 font-medium">
                      <CheckCircle2 className="w-3.5 h-3.5" />
                      {tgt.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Baselines view */}
      {activeTab === 'baselines' && (
        <div className="overflow-x-auto rounded-lg border border-border">
          <table className="w-full text-xs text-left">
            <thead className="bg-muted/50 text-muted-foreground uppercase text-[10px] tracking-wider border-b border-border">
              <tr>
                <th className="py-2.5 px-3">Baza Nomi & Indikator</th>
                <th className="py-2.5 px-3">Bazaviy Qiymat</th>
                <th className="py-2.5 px-3">Sifat Darajasi</th>
                <th className="py-2.5 px-3">Maʼlumot Toʻliqligi</th>
                <th className="py-2.5 px-3">Oraliq & Mavsum</th>
                <th className="py-2.5 px-3">Qamrov</th>
                <th className="py-2.5 px-3">Holat</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {baselines.map((b) => (
                <tr key={b.id} className="hover:bg-muted/30 transition-colors">
                  <td className="py-2.5 px-3 font-medium text-foreground">
                    <div>
                      <p className="font-semibold text-foreground">{b.name}</p>
                      <span className="font-mono text-[11px] text-primary">{b.indicatorCode}</span>
                    </div>
                  </td>
                  <td className="py-2.5 px-3 font-mono font-bold text-foreground">
                    {b.baselineValue} {b.measurementUnit}
                  </td>
                  <td className="py-2.5 px-3">
                    <span
                      className={`px-2 py-0.5 rounded-full text-[10px] font-medium border ${
                        b.baselineQuality === 'VERIFIED_BASELINE'
                          ? 'bg-emerald-500/10 text-emerald-600 border-emerald-500/30'
                          : 'bg-amber-500/10 text-amber-600 border-amber-500/30'
                      }`}
                    >
                      {b.baselineQuality}
                    </span>
                  </td>
                  <td className="py-2.5 px-3 font-medium text-foreground">{b.dataCompletenessPct}%</td>
                  <td className="py-2.5 px-3 text-muted-foreground text-[11px]">
                    {b.seasonContext || 'Asosiy vegetatsiya'}
                  </td>
                  <td className="py-2.5 px-3 text-muted-foreground">{b.scopeName}</td>
                  <td className="py-2.5 px-3 text-emerald-600 font-medium">{b.approvalStatus}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
