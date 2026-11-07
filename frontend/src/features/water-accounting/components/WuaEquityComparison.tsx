import React from 'react';
import { DistributionEquityResponse, EquityDisparityLevel } from '@/entities/water-accounting';
import { Scale, Users, AlertTriangle, CheckCircle, ShieldCheck } from 'lucide-react';

interface WuaEquityComparisonProps {
  equityList: DistributionEquityResponse[];
}

export const WuaEquityComparison: React.FC<WuaEquityComparisonProps> = ({ equityList = [] }) => {
  const getDisparityBadge = (level: EquityDisparityLevel) => {
    switch (level) {
      case 'BALANCED':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300 border border-emerald-300 dark:border-emerald-800">
            <CheckCircle className="w-3 h-3 text-emerald-600 dark:text-emerald-400" />
            Muvozanatli (Balanced)
          </span>
        );
      case 'POTENTIAL_DISPARITY':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-300 border border-amber-300 dark:border-amber-800">
            <AlertTriangle className="w-3 h-3 text-amber-600 dark:text-amber-400" />
            Oʻrtacha Tafovut (Potential Disparity)
          </span>
        );
      case 'SIGNIFICANT_DISPARITY':
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-rose-100 text-rose-800 dark:bg-rose-950 dark:text-rose-300 border border-rose-300 dark:border-rose-800">
            <AlertTriangle className="w-3 h-3 text-rose-600 dark:text-rose-400" />
            Yuqori Tafovut (Significant Disparity)
          </span>
        );
      case 'INSUFFICIENT_DATA':
      default:
        return (
          <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-muted text-muted-foreground border">
            Yetarsiz Maʼlumot
          </span>
        );
    }
  };

  return (
    <div className="space-y-4">
      <div className="p-4 bg-card border rounded-xl shadow-xs flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <Scale className="w-5 h-5 text-primary" />
            <h3 className="font-semibold text-base text-foreground">
              Suv Taqsimoti Adolatliligi (Distribution Equity & Fairness)
            </h3>
          </div>
          <p className="text-xs text-muted-foreground mt-0.5">
            Suv Isteʼmolchilari Uyushmalari (SIU) va tarmoqlar kesimidagi taqsimot tengligi koeffitsientlari (CV)
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {equityList.map((eq) => (
          <div key={eq.clusterId} className="p-4 bg-card border rounded-xl shadow-xs space-y-3.5">
            <div className="flex items-start justify-between gap-2">
              <div>
                <h4 className="font-bold text-sm text-foreground flex items-center gap-1.5">
                  <Users className="w-4 h-4 text-primary" />
                  {eq.clusterName}
                </h4>
                <p className="text-xs text-muted-foreground mt-0.5">
                  Klaster: {eq.clusterType} • Namuna: {eq.sampleCount} ta dala
                </p>
              </div>
              {getDisparityBadge(eq.disparityLevel)}
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 pt-1 border-t border-b py-2 text-xs">
              <div>
                <span className="text-[11px] text-muted-foreground block">Oʻrtacha / Ga</span>
                <span className="font-bold text-foreground">
                  {eq.meanDeliveryPerHectareM3.toLocaleString()} m³/ga
                </span>
              </div>
              <div>
                <span className="text-[11px] text-muted-foreground block">Dispersiya (CV)</span>
                <span
                  className={`font-mono font-bold ${
                    eq.coefficientOfVariation > 0.35
                      ? 'text-rose-600 dark:text-rose-400'
                      : eq.coefficientOfVariation > 0.15
                      ? 'text-amber-600 dark:text-amber-400'
                      : 'text-emerald-600 dark:text-emerald-400'
                  }`}
                >
                  {eq.coefficientOfVariation.toFixed(3)}
                </span>
              </div>
              <div>
                <span className="text-[11px] text-muted-foreground block">Kvota Bajarilishi</span>
                <span className="font-bold text-foreground">
                  {eq.meanAllocationFulfillmentPercent.toFixed(1)}%
                </span>
              </div>
              <div>
                <span className="text-[11px] text-muted-foreground block">Kechikish (Lag)</span>
                <span className="font-bold text-muted-foreground">
                  {eq.averageDeliveryDelayHours.toFixed(1)} soat
                </span>
              </div>
            </div>

            <div className="space-y-1.5 text-xs">
              <div className="flex items-center gap-1.5 text-muted-foreground">
                <ShieldCheck className="w-3.5 h-3.5 text-primary" />
                <span>
                  Qoʻllanilgan siyosat: <strong className="text-foreground">{eq.fairnessPolicyApplied}</strong>
                </span>
              </div>

              {eq.supportingEvidence && eq.supportingEvidence.length > 0 && (
                <ul className="list-disc list-inside space-y-0.5 text-muted-foreground text-[11px] pt-1">
                  {eq.supportingEvidence.map((ev, idx) => (
                    <li key={idx}>{ev}</li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
