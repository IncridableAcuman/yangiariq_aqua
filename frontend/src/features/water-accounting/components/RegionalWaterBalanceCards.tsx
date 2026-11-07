import React from 'react';
import { RegionalWaterBalanceResponse } from '@/entities/water-accounting';
import { AccountingEvidenceBadge } from './AccountingEvidenceBadge';
import {
  ArrowDownRight,
  ArrowUpRight,
  Droplets,
  Layers,
  Scale,
  ShieldCheck,
  AlertTriangle,
} from 'lucide-react';

interface RegionalWaterBalanceCardsProps {
  balance: RegionalWaterBalanceResponse;
}

export const RegionalWaterBalanceCards: React.FC<RegionalWaterBalanceCardsProps> = ({ balance }) => {
  const formatM3 = (val: number | undefined) =>
    (val ?? 0).toLocaleString('uz-UZ', { maximumFractionDigits: 1 }) + ' m³';

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3 p-4 bg-card border rounded-xl shadow-xs">
        <div>
          <div className="flex items-center gap-2">
            <Scale className="w-5 h-5 text-primary" />
            <h3 className="font-semibold text-base text-foreground">
              {balance.boundaryName} — Suv Balansi (Water Balance)
            </h3>
          </div>
          <p className="text-xs text-muted-foreground mt-0.5">
            Davr: {new Date(balance.periodStart).toLocaleDateString()} — {new Date(balance.periodEnd).toLocaleDateString()} ({balance.periodType})
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <AccountingEvidenceBadge classification={balance.evidenceClassification} />
          {balance.isReconciled ? (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300 border border-emerald-300 dark:border-emerald-800">
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-600 dark:text-emerald-400" />
              Muvofiqlashtirilgan (Reconciled)
            </span>
          ) : (
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-300 border border-amber-300 dark:border-amber-800">
              <AlertTriangle className="w-3.5 h-3.5 text-amber-600 dark:text-amber-400" />
              Farq Aniqlangan ({(balance.discrepancyM3 ?? 0).toLocaleString('uz-UZ')} m³)
            </span>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3.5">
        {/* Inflow */}
        <div className="p-4 bg-card border rounded-xl shadow-xs">
          <div className="flex items-center justify-between text-muted-foreground mb-1.5">
            <span className="text-xs font-medium uppercase tracking-wider">Kiruvchi Suv (Inflow)</span>
            <ArrowDownRight className="w-4 h-4 text-emerald-500" />
          </div>
          <div className="text-xl font-bold text-foreground">
            {formatM3(balance.inflowReceivedM3)}
          </div>
          <p className="text-xs text-muted-foreground mt-1">
            Bosh suv olish inshootidan
          </p>
        </div>

        {/* Conveyance Losses */}
        <div className="p-4 bg-card border rounded-xl shadow-xs">
          <div className="flex items-center justify-between text-muted-foreground mb-1.5">
            <span className="text-xs font-medium uppercase tracking-wider">Kanal Isrofi (Losses)</span>
            <ArrowUpRight className="w-4 h-4 text-rose-500" />
          </div>
          <div className="text-xl font-bold text-rose-600 dark:text-rose-400">
            {formatM3(balance.verifiedLossesM3)}
          </div>
          <p className="text-xs text-muted-foreground mt-1">
            Filtratsiya va bugʻlanish
          </p>
        </div>

        {/* Delivered */}
        <div className="p-4 bg-card border rounded-xl shadow-xs">
          <div className="flex items-center justify-between text-muted-foreground mb-1.5">
            <span className="text-xs font-medium uppercase tracking-wider">Yetkazilgan (Delivered)</span>
            <Droplets className="w-4 h-4 text-blue-500" />
          </div>
          <div className="text-xl font-bold text-blue-600 dark:text-blue-400">
            {formatM3(balance.waterDeliveredM3)}
          </div>
          <p className="text-xs text-muted-foreground mt-1">
            Dalalar kirish qulogʻiga (Turnout)
          </p>
        </div>

        {/* Consumed */}
        <div className="p-4 bg-card border rounded-xl shadow-xs">
          <div className="flex items-center justify-between text-muted-foreground mb-1.5">
            <span className="text-xs font-medium uppercase tracking-wider">Isteʼmol (Consumed)</span>
            <Droplets className="w-4 h-4 text-indigo-500" />
          </div>
          <div className="text-xl font-bold text-indigo-600 dark:text-indigo-400">
            {formatM3(balance.waterConsumedM3)}
          </div>
          <p className="text-xs text-muted-foreground mt-1">
            Sugʻorish tadbirlarida
          </p>
        </div>

        {/* Closing / Storage Balance */}
        <div className="p-4 bg-card border rounded-xl shadow-xs">
          <div className="flex items-center justify-between text-muted-foreground mb-1.5">
            <span className="text-xs font-medium uppercase tracking-wider">Qoldiq Balans (Closing)</span>
            <Layers className="w-4 h-4 text-primary" />
          </div>
          <div className="text-xl font-bold text-foreground">
            {formatM3(balance.closingBalanceM3)}
          </div>
          <p className="text-xs text-muted-foreground mt-1">
            Tizimdagi joriy rezerv
          </p>
        </div>
      </div>
    </div>
  );
};
