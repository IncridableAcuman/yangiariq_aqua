import React from 'react';
import { ConveyanceLossResponse, ConveyanceLossClassification } from '@/entities/water-accounting';
import { AccountingEvidenceBadge } from './AccountingEvidenceBadge';
import { AlertTriangle, CheckCircle, Info, ShieldAlert } from 'lucide-react';

interface CanalLossAnalysisTableProps {
  losses: ConveyanceLossResponse[];
}

export const CanalLossAnalysisTable: React.FC<CanalLossAnalysisTableProps> = ({ losses }) => {
  const getClassificationBadge = (classification: ConveyanceLossClassification) => {
    switch (classification) {
      case 'NO_SIGNIFICANT_DISCREPANCY':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300">
            <CheckCircle className="w-3 h-3 text-emerald-600 dark:text-emerald-400" />
            Meʼyorda (&le;5%)
          </span>
        );
      case 'VERIFIED_LOSS':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-300">
            <Info className="w-3 h-3 text-blue-600 dark:text-blue-400" />
            Ochiq Oqim Isrofi (5–15%)
          </span>
        );
      case 'SUSPECTED_LOSS':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-300">
            <AlertTriangle className="w-3 h-3 text-amber-600 dark:text-amber-400" />
            Shubhali Isrof (15–30%)
          </span>
        );
      case 'UNACCOUNTED_WITHDRAWAL':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-rose-100 text-rose-800 dark:bg-rose-950 dark:text-rose-300">
            <ShieldAlert className="w-3 h-3 text-rose-600 dark:text-rose-400" />
            Hisoblanmagan Suv Olish (&gt;30%)
          </span>
        );
      case 'MEASUREMENT_DISCREPANCY':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-purple-100 text-purple-800 dark:bg-purple-950 dark:text-purple-300">
            Oʻlchov Nomutanosibligi
          </span>
        );
      case 'INSUFFICIENT_DATA':
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-muted text-muted-foreground">
            Yetarsiz Maʼlumot
          </span>
        );
    }
  };

  return (
    <div className="space-y-3">
      <div className="p-3 bg-card border rounded-xl flex items-center justify-between text-xs text-muted-foreground">
        <p>
          Ushbu jadval Yangiariq tuman magistral va tarmoq kanallaridagi oʻtkazish isroflarini hisoblaydi.
        </p>
        <span className="font-semibold text-foreground">
          Jami segmentlar: {losses.length}
        </span>
      </div>

      <div className="border rounded-xl bg-card overflow-x-auto shadow-xs">
        <table className="w-full text-left text-xs">
          <thead className="bg-muted/50 border-b text-muted-foreground font-medium">
            <tr>
              <th className="py-2.5 px-3">Kanal Segmenti</th>
              <th className="py-2.5 px-3">Turi</th>
              <th className="py-2.5 px-3">Boshlanish → Tugash Tuguni</th>
              <th className="py-2.5 px-3 text-right">Yuqori Oqim (Upstream m³)</th>
              <th className="py-2.5 px-3 text-right">Quyi Oqim (Downstream m³)</th>
              <th className="py-2.5 px-3 text-right">Isrof Hajmi (m³)</th>
              <th className="py-2.5 px-3 text-right">Isrof %</th>
              <th className="py-2.5 px-3">Holat / Klassifikatsiya</th>
              <th className="py-2.5 px-3">Ishonchlilik</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {losses.length === 0 ? (
              <tr>
                <td colSpan={9} className="py-8 text-center text-muted-foreground">
                  Kanal isrofi boʻyicha maʼlumot topilmadi.
                </td>
              </tr>
            ) : (
              losses.map((loss) => (
                <tr key={loss.edgeId} className="hover:bg-muted/30 transition-colors">
                  <td className="py-2.5 px-3 font-semibold text-foreground whitespace-nowrap">
                    {loss.canalName}
                  </td>
                  <td className="py-2.5 px-3 text-muted-foreground font-mono text-[11px] whitespace-nowrap">
                    {loss.edgeType}
                  </td>
                  <td className="py-2.5 px-3 text-muted-foreground text-[11px] whitespace-nowrap">
                    {loss.startNodeName} → {loss.endNodeName}
                  </td>
                  <td className="py-2.5 px-3 text-right font-medium text-foreground whitespace-nowrap">
                    {loss.upstreamVolumeM3.toLocaleString('uz-UZ', { maximumFractionDigits: 1 })}
                  </td>
                  <td className="py-2.5 px-3 text-right font-medium text-foreground whitespace-nowrap">
                    {loss.downstreamVolumeM3.toLocaleString('uz-UZ', { maximumFractionDigits: 1 })}
                  </td>
                  <td className="py-2.5 px-3 text-right font-bold text-rose-600 dark:text-rose-400 whitespace-nowrap">
                    {loss.lossVolumeM3.toLocaleString('uz-UZ', { maximumFractionDigits: 1 })}
                  </td>
                  <td className="py-2.5 px-3 text-right font-bold whitespace-nowrap">
                    <span
                      className={
                        loss.lossPercentage > 15
                          ? 'text-rose-600 dark:text-rose-400'
                          : loss.lossPercentage > 5
                          ? 'text-blue-600 dark:text-blue-400'
                          : 'text-emerald-600 dark:text-emerald-400'
                      }
                    >
                      {loss.lossPercentage.toFixed(2)}%
                    </span>
                  </td>
                  <td className="py-2.5 px-3 whitespace-nowrap">
                    {getClassificationBadge(loss.classification)}
                  </td>
                  <td className="py-2.5 px-3 whitespace-nowrap">
                    <AccountingEvidenceBadge classification={loss.evidenceClassification} />
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};
