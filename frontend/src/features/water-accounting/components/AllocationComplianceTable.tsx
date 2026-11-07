import React, { useState } from 'react';
import { AllocationComplianceResponse, AllocationComplianceStatus } from '@/entities/water-accounting';
import { AccountingEvidenceBadge } from './AccountingEvidenceBadge';
import { Search, Eye, AlertCircle, CheckCircle2, AlertTriangle } from 'lucide-react';

interface AllocationComplianceTableProps {
  compliances: AllocationComplianceResponse[];
  onSelectField?: (fieldId: string) => void;
}

export const AllocationComplianceTable: React.FC<AllocationComplianceTableProps> = ({
  compliances,
  onSelectField,
}) => {
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  const filtered = compliances.filter((c) => {
    if (statusFilter !== 'ALL' && c.status !== statusFilter) return false;
    if (!search.trim()) return true;
    const q = search.toLowerCase();
    return (
      c.targetName.toLowerCase().includes(q) ||
      c.wuaName.toLowerCase().includes(q) ||
      c.cropType.toLowerCase().includes(q)
    );
  });

  const getStatusBadge = (status: AllocationComplianceStatus) => {
    switch (status) {
      case 'ALLOCATION_RESPECTED':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300">
            <CheckCircle2 className="w-3 h-3 text-emerald-600 dark:text-emerald-400" />
            Meʼyorda (Respected)
          </span>
        );
      case 'APPROACHING_LIMIT':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-300">
            <AlertTriangle className="w-3 h-3 text-amber-600 dark:text-amber-400" />
            Limitga Yaqin (&ge;90%)
          </span>
        );
      case 'ALLOCATION_EXCEEDED':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-rose-100 text-rose-800 dark:bg-rose-950 dark:text-rose-300">
            <AlertCircle className="w-3 h-3 text-rose-600 dark:text-rose-400" />
            Limitdan Oshgan (Exceeded)
          </span>
        );
      case 'UNDER_DELIVERED':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-300">
            Kam Yetkazilgan (&lt;80%)
          </span>
        );
      case 'UNVERIFIED_DELIVERY':
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-purple-100 text-purple-800 dark:bg-purple-950 dark:text-purple-300">
            Tasdiqlanmagan
          </span>
        );
      case 'INSUFFICIENT_DATA':
      default:
        return (
          <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-medium bg-muted text-muted-foreground">
            Maʼlumot Yetarsiz
          </span>
        );
    }
  };

  return (
    <div className="space-y-3">
      {/* Filters */}
      <div className="flex flex-wrap items-center justify-between gap-3 p-3 bg-card border rounded-xl">
        <div className="relative flex-1 min-w-[200px] max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Dala, SIU yoki ekin boʻyicha qidirish..."
            className="w-full pl-9 pr-3 py-1.5 text-xs bg-background border rounded-lg focus:outline-hidden focus:ring-1 focus:ring-primary"
          />
        </div>

        <div className="flex items-center gap-2">
          <span className="text-xs text-muted-foreground">Holat:</span>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="text-xs bg-background border rounded-lg px-2.5 py-1.5 focus:outline-hidden focus:ring-1 focus:ring-primary"
          >
            <option value="ALL">Barchasi (All)</option>
            <option value="ALLOCATION_RESPECTED">Meʼyorda (Respected)</option>
            <option value="APPROACHING_LIMIT">Limitga Yaqin (Approaching Limit)</option>
            <option value="ALLOCATION_EXCEEDED">Limitdan Oshgan (Exceeded)</option>
            <option value="UNDER_DELIVERED">Kam Yetkazilgan (Under-Delivered)</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <div className="border rounded-xl bg-card overflow-x-auto shadow-xs">
        <table className="w-full text-left text-xs">
          <thead className="bg-muted/50 border-b text-muted-foreground font-medium">
            <tr>
              <th className="py-2.5 px-3">Dala Maydoni</th>
              <th className="py-2.5 px-3">SIU (WUA)</th>
              <th className="py-2.5 px-3">Ekin / Maydon</th>
              <th className="py-2.5 px-3 text-right">Ajratilgan Kvota (m³)</th>
              <th className="py-2.5 px-3 text-right">Yetkazilgan (m³)</th>
              <th className="py-2.5 px-3 text-right">Isteʼmol (m³)</th>
              <th className="py-2.5 px-3 text-right">Qoldiq Kvota (m³)</th>
              <th className="py-2.5 px-3 min-w-[120px]">Bajarilish %</th>
              <th className="py-2.5 px-3">Muvofiqlik Holati</th>
              <th className="py-2.5 px-3 text-center">Batafsil</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {filtered.length === 0 ? (
              <tr>
                <td colSpan={10} className="py-8 text-center text-muted-foreground">
                  Kvota muvofiqligi boʻyicha yozuvlar topilmadi.
                </td>
              </tr>
            ) : (
              filtered.map((item) => (
                <tr key={item.targetId} className="hover:bg-muted/30 transition-colors">
                  <td className="py-2.5 px-3 font-semibold text-foreground whitespace-nowrap">
                    <div className="flex flex-col items-start gap-1">
                      <span>{item.targetName}</span>
                      <AccountingEvidenceBadge classification={item.evidenceClassification} />
                    </div>
                  </td>
                  <td className="py-2.5 px-3 text-muted-foreground whitespace-nowrap">{item.wuaName}</td>
                  <td className="py-2.5 px-3 text-muted-foreground whitespace-nowrap">
                    {item.cropType} ({item.areaHectares} ga)
                  </td>
                  <td className="py-2.5 px-3 text-right font-medium text-foreground whitespace-nowrap">
                    {item.allocatedVolumeM3.toLocaleString('uz-UZ', { maximumFractionDigits: 1 })}
                  </td>
                  <td className="py-2.5 px-3 text-right font-medium text-blue-600 dark:text-blue-400 whitespace-nowrap">
                    {item.actualDeliveredVolumeM3.toLocaleString('uz-UZ', { maximumFractionDigits: 1 })}
                  </td>
                  <td className="py-2.5 px-3 text-right font-medium text-indigo-600 dark:text-indigo-400 whitespace-nowrap">
                    {item.actualConsumptionM3.toLocaleString('uz-UZ', { maximumFractionDigits: 1 })}
                  </td>
                  <td className="py-2.5 px-3 text-right font-medium text-emerald-600 dark:text-emerald-400 whitespace-nowrap">
                    {item.remainingAllocationM3.toLocaleString('uz-UZ', { maximumFractionDigits: 1 })}
                  </td>
                  <td className="py-2.5 px-3">
                    <div className="flex items-center gap-2">
                      <div className="w-16 h-1.5 rounded-full bg-muted overflow-hidden">
                        <div
                          className={`h-full rounded-full ${
                            item.deliveryCompletionPercent > 100
                              ? 'bg-rose-500'
                              : item.deliveryCompletionPercent > 80
                              ? 'bg-amber-500'
                              : 'bg-emerald-500'
                          }`}
                          style={{ width: `${Math.min(100, item.deliveryCompletionPercent)}%` }}
                        />
                      </div>
                      <span className="font-mono text-[11px] font-semibold text-foreground">
                        {item.deliveryCompletionPercent.toFixed(1)}%
                      </span>
                    </div>
                  </td>
                  <td className="py-2.5 px-3 whitespace-nowrap">{getStatusBadge(item.status)}</td>
                  <td className="py-2.5 px-3 text-center whitespace-nowrap">
                    {onSelectField && (
                      <button
                        onClick={() => onSelectField(item.targetId)}
                        className="p-1 rounded-md text-primary hover:bg-primary/10 transition-colors"
                        title="Dala boʻyicha toʻliq hisob-kitobni koʻrish"
                      >
                        <Eye className="w-4 h-4" />
                      </button>
                    )}
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
