import React, { useState } from 'react';
import { WaterLedgerEntryResponse, LedgerEntryType } from '@/entities/water-accounting';
import { AccountingEvidenceBadge } from './AccountingEvidenceBadge';
import { ArrowRight, Search } from 'lucide-react';

interface WaterLedgerTableProps {
  entries: WaterLedgerEntryResponse[];
}

export const WaterLedgerTable: React.FC<WaterLedgerTableProps> = ({ entries }) => {
  const [search, setSearch] = useState('');
  const [selectedType, setSelectedType] = useState<string>('ALL');

  const filtered = entries.filter((e) => {
    if (selectedType !== 'ALL' && e.entryType !== selectedType) return false;
    if (!search.trim()) return true;
    const q = search.toLowerCase();
    return (
      e.sourceNodeOrCanal?.toLowerCase().includes(q) ||
      e.destinationNodeOrField?.toLowerCase().includes(q) ||
      e.dataSource?.toLowerCase().includes(q) ||
      e.notes?.toLowerCase().includes(q)
    );
  });

  const getTypeBadge = (type: LedgerEntryType) => {
    switch (type) {
      case 'SOURCE_INFLOW':
        return <span className="px-2 py-0.5 rounded text-xs font-medium bg-emerald-100 text-emerald-800 dark:bg-emerald-950 dark:text-emerald-300">Bosh Oqim</span>;
      case 'CANAL_TRANSFER':
        return <span className="px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-300">Kanal Oʻtkazmasi</span>;
      case 'TURNOUT_DELIVERY':
        return <span className="px-2 py-0.5 rounded text-xs font-medium bg-cyan-100 text-cyan-800 dark:bg-cyan-950 dark:text-cyan-300">Quloq Yetkazmasi</span>;
      case 'FIELD_CONSUMPTION':
        return <span className="px-2 py-0.5 rounded text-xs font-medium bg-indigo-100 text-indigo-800 dark:bg-indigo-950 dark:text-indigo-300">Dala Isteʼmoli</span>;
      case 'VERIFIED_LOSS':
        return <span className="px-2 py-0.5 rounded text-xs font-medium bg-rose-100 text-rose-800 dark:bg-rose-950 dark:text-rose-300">Isrof</span>;
      case 'ACCOUNTING_CORRECTION':
        return <span className="px-2 py-0.5 rounded text-xs font-medium bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-300">Tuzatish</span>;
      default:
        return <span className="px-2 py-0.5 rounded text-xs font-medium bg-muted text-muted-foreground">{type}</span>;
    }
  };

  return (
    <div className="space-y-3">
      {/* Search and Filters */}
      <div className="flex flex-wrap items-center justify-between gap-3 p-3 bg-card border rounded-xl">
        <div className="relative flex-1 min-w-[200px] max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Kanal, dala yoki manba boʻyicha qidirish..."
            className="w-full pl-9 pr-3 py-1.5 text-xs bg-background border rounded-lg focus:outline-hidden focus:ring-1 focus:ring-primary"
          />
        </div>

        <div className="flex items-center gap-2">
          <span className="text-xs text-muted-foreground">Tur boʻyicha:</span>
          <select
            value={selectedType}
            onChange={(e) => setSelectedType(e.target.value)}
            className="text-xs bg-background border rounded-lg px-2.5 py-1.5 focus:outline-hidden focus:ring-1 focus:ring-primary"
          >
            <option value="ALL">Barchasi (All)</option>
            <option value="SOURCE_INFLOW">Bosh Oqim (Source Inflow)</option>
            <option value="CANAL_TRANSFER">Kanal Oʻtkazmasi (Canal Transfer)</option>
            <option value="TURNOUT_DELIVERY">Quloq Yetkazmasi (Turnout Delivery)</option>
            <option value="FIELD_CONSUMPTION">Dala Isteʼmoli (Field Consumption)</option>
            <option value="VERIFIED_LOSS">Isrof (Verified Loss)</option>
            <option value="ACCOUNTING_CORRECTION">Tuzatish (Correction)</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <div className="border rounded-xl bg-card overflow-x-auto shadow-xs">
        <table className="w-full text-left text-xs">
          <thead className="bg-muted/50 border-b text-muted-foreground font-medium">
            <tr>
              <th className="py-2.5 px-3">Vaqt (Timestamp)</th>
              <th className="py-2.5 px-3">Harakat Turi (Type)</th>
              <th className="py-2.5 px-3">Yoʻnalish (Source → Dest)</th>
              <th className="py-2.5 px-3 text-right">Hajm (Volume m³)</th>
              <th className="py-2.5 px-3 text-right">Oqim (m³/s)</th>
              <th className="py-2.5 px-3">Ishonchlilik (Evidence)</th>
              <th className="py-2.5 px-3">Maʼlumot Manbasi</th>
              <th className="py-2.5 px-3">Izoh</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {filtered.length === 0 ? (
              <tr>
                <td colSpan={8} className="py-8 text-center text-muted-foreground">
                  Harakatlar daftari boʻyicha yozuvlar topilmadi.
                </td>
              </tr>
            ) : (
              filtered.map((entry) => (
                <tr key={entry.id} className="hover:bg-muted/30 transition-colors">
                  <td className="py-2.5 px-3 font-mono text-[11px] text-muted-foreground whitespace-nowrap">
                    {new Date(entry.eventTimestamp).toLocaleString()}
                  </td>
                  <td className="py-2.5 px-3 whitespace-nowrap">{getTypeBadge(entry.entryType)}</td>
                  <td className="py-2.5 px-3">
                    <div className="flex items-center gap-1.5 font-medium text-foreground">
                      <span>{entry.sourceNodeOrCanal ?? 'N/A'}</span>
                      <ArrowRight className="w-3 h-3 text-muted-foreground shrink-0" />
                      <span className="text-primary">{entry.destinationNodeOrField ?? 'N/A'}</span>
                    </div>
                  </td>
                  <td className="py-2.5 px-3 text-right font-semibold text-foreground whitespace-nowrap">
                    {entry.volumeM3.toLocaleString('uz-UZ', { maximumFractionDigits: 1 })}
                  </td>
                  <td className="py-2.5 px-3 text-right font-mono text-muted-foreground whitespace-nowrap">
                    {entry.flowRateM3S > 0 ? entry.flowRateM3S.toFixed(4) : '—'}
                  </td>
                  <td className="py-2.5 px-3 whitespace-nowrap">
                    <AccountingEvidenceBadge classification={entry.evidenceClassification} />
                  </td>
                  <td className="py-2.5 px-3 text-muted-foreground font-mono text-[11px] whitespace-nowrap">
                    {entry.dataSource}
                  </td>
                  <td className="py-2.5 px-3 text-muted-foreground max-w-xs truncate" title={entry.notes ?? ''}>
                    {entry.notes ?? '—'}
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
