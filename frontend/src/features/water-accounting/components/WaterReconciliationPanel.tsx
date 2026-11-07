import React, { useState } from 'react';
import { WaterReconciliationResponse, ReconciliationStatus } from '@/entities/water-accounting';
import { AccountingEvidenceBadge } from './AccountingEvidenceBadge';
import { ReconciliationReviewDialog } from './ReconciliationReviewDialog';
import { CheckCircle2, AlertTriangle, HelpCircle, Check, FileCheck } from 'lucide-react';

interface WaterReconciliationPanelProps {
  reconciliations: WaterReconciliationResponse[];
  onReviewSuccess?: () => void;
  isOperatorOrAdmin?: boolean;
}

export const WaterReconciliationPanel: React.FC<WaterReconciliationPanelProps> = ({
  reconciliations,
  onReviewSuccess,
  isOperatorOrAdmin = true,
}) => {
  const [selectedItem, setSelectedItem] = useState<WaterReconciliationResponse | null>(null);

  const getStatusBadge = (status: ReconciliationStatus) => {
    switch (status) {
      case 'RECONCILED':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-semibold bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300">
            <CheckCircle2 className="w-3 h-3 text-emerald-600 dark:text-emerald-400" />
            Muvofiq (Reconciled)
          </span>
        );
      case 'WITHIN_TOLERANCE':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-semibold bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-300">
            <Check className="w-3 h-3 text-blue-600 dark:text-blue-400" />
            Tolerans Ichida (&le;5%)
          </span>
        );
      case 'DISCREPANCY':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-semibold bg-rose-100 text-rose-800 dark:bg-rose-950 dark:text-rose-300">
            <AlertTriangle className="w-3 h-3 text-rose-600 dark:text-rose-400" />
            Tafovut Mavjud
          </span>
        );
      case 'UNVERIFIED':
      case 'INSUFFICIENT_DATA':
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-semibold bg-muted text-muted-foreground">
            <HelpCircle className="w-3 h-3" />
            Tasdiqlanmagan
          </span>
        );
    }
  };

  return (
    <div className="space-y-3">
      <div className="p-3 bg-card border rounded-xl flex items-center justify-between text-xs text-muted-foreground">
        <p>
          Bosqichlararo suv hisobini solishtirish: Manba oqimi va kanal kirimi, tarmoqlararo uzatma, yetkazma va amaliy isteʼmol.
        </p>
        <span className="font-semibold text-foreground">
          Jami solishtirishlar: {reconciliations.length}
        </span>
      </div>

      <div className="border rounded-xl bg-card overflow-x-auto shadow-xs">
        <table className="w-full text-left text-xs">
          <thead className="bg-muted/50 border-b text-muted-foreground font-medium">
            <tr>
              <th className="py-2.5 px-3">Chegara Bosqichi (Boundary)</th>
              <th className="py-2.5 px-3">Davr</th>
              <th className="py-2.5 px-3 text-right">Kutilgan Hajm (m³)</th>
              <th className="py-2.5 px-3 text-right">Qayd Qilingan (m³)</th>
              <th className="py-2.5 px-3 text-right">Tafovut (m³)</th>
              <th className="py-2.5 px-3 text-right">Tafovut %</th>
              <th className="py-2.5 px-3">Ruxsat Etilgan Chegara</th>
              <th className="py-2.5 px-3">Holat</th>
              <th className="py-2.5 px-3">Ishonchlilik</th>
              {isOperatorOrAdmin && <th className="py-2.5 px-3 text-center">Koʻrib Chiqish</th>}
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {reconciliations.length === 0 ? (
              <tr>
                <td colSpan={10} className="py-8 text-center text-muted-foreground">
                  Solishtirish yozuvlari mavjud emas.
                </td>
              </tr>
            ) : (
              reconciliations.map((rec) => (
                <tr key={rec.id} className="hover:bg-muted/30 transition-colors">
                  <td className="py-2.5 px-3">
                    <p className="font-semibold text-foreground">{rec.boundaryName}</p>
                    <span className="text-[11px] text-muted-foreground font-mono">{rec.boundaryType}</span>
                  </td>
                  <td className="py-2.5 px-3 text-muted-foreground whitespace-nowrap text-[11px]">
                    {new Date(rec.periodStart).toLocaleDateString()} — {new Date(rec.periodEnd).toLocaleDateString()}
                  </td>
                  <td className="py-2.5 px-3 text-right font-medium text-foreground whitespace-nowrap">
                    {rec.expectedVolumeM3.toLocaleString('uz-UZ', { maximumFractionDigits: 1 })}
                  </td>
                  <td className="py-2.5 px-3 text-right font-medium text-foreground whitespace-nowrap">
                    {rec.recordedVolumeM3.toLocaleString('uz-UZ', { maximumFractionDigits: 1 })}
                  </td>
                  <td className="py-2.5 px-3 text-right font-bold whitespace-nowrap">
                    <span
                      className={
                        rec.discrepancyVolumeM3 < 0
                          ? 'text-rose-600 dark:text-rose-400'
                          : 'text-emerald-600 dark:text-emerald-400'
                      }
                    >
                      {rec.discrepancyVolumeM3.toLocaleString('uz-UZ', { maximumFractionDigits: 1 })}
                    </span>
                  </td>
                  <td className="py-2.5 px-3 text-right font-bold whitespace-nowrap">
                    <span
                      className={
                        rec.discrepancyPercent > rec.tolerancePercent
                          ? 'text-rose-600 dark:text-rose-400'
                          : 'text-emerald-600 dark:text-emerald-400'
                      }
                    >
                      {rec.discrepancyPercent.toFixed(2)}%
                    </span>
                  </td>
                  <td className="py-2.5 px-3 text-muted-foreground font-mono text-[11px] whitespace-nowrap">
                    &plusmn;{rec.tolerancePercent.toFixed(1)}%
                  </td>
                  <td className="py-2.5 px-3 whitespace-nowrap">{getStatusBadge(rec.status)}</td>
                  <td className="py-2.5 px-3 whitespace-nowrap">
                    <AccountingEvidenceBadge classification={rec.evidenceClassification} />
                  </td>
                  {isOperatorOrAdmin && (
                    <td className="py-2.5 px-3 text-center whitespace-nowrap">
                      <button
                        onClick={() => setSelectedItem(rec)}
                        className="inline-flex items-center gap-1 px-2.5 py-1 rounded-md text-xs font-semibold bg-primary/10 text-primary hover:bg-primary/20 transition-colors"
                      >
                        <FileCheck className="w-3.5 h-3.5" />
                        <span>Tekshirish</span>
                      </button>
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {selectedItem && (
        <ReconciliationReviewDialog
          item={selectedItem}
          isOpen={!!selectedItem}
          onClose={() => setSelectedItem(null)}
          onSuccess={() => {
            setSelectedItem(null);
            onReviewSuccess?.();
          }}
        />
      )}
    </div>
  );
};
