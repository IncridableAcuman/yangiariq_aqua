import React, { useState } from 'react'
import { MapPin } from 'lucide-react'
import { EvidenceClassificationBadge } from './EvidenceClassificationBadge'

interface Props {
  onSelectEntity?: (type: string, id: string, name: string) => void
}

export const GeographicPerformanceTable: React.FC<Props> = ({ onSelectEntity }) => {
  const [geoLevel, setGeoLevel] = useState<'canals' | 'wuas' | 'fields'>('canals')

  const canals = [
    {
      id: 'c1',
      name: 'Boʻzyop Bosh Kanali (PK0 - PK12)',
      type: 'Magistral Kanal',
      lengthKm: 12.4,
      deliveryFulfillmentPct: 94.2,
      conveyanceLossPct: 8.0,
      disparityLevel: 'LOW_DISPARITY',
      evidence: 'CALCULATED' as const,
    },
    {
      id: 'c2',
      name: 'Qiyot-Qilichyop Tarmoq Kanali',
      type: 'Tarmoq Kanal',
      lengthKm: 8.6,
      deliveryFulfillmentPct: 91.5,
      conveyanceLossPct: 9.0,
      disparityLevel: 'MODERATE_DISPARITY',
      evidence: 'CALCULATED' as const,
    },
    {
      id: 'c3',
      name: 'Qorakoʻz Ariq Tarmogʻi',
      type: 'Taqsimlovchi Ariq',
      lengthKm: 6.2,
      deliveryFulfillmentPct: 89.0,
      conveyanceLossPct: 11.2,
      disparityLevel: 'LOW_DISPARITY',
      evidence: 'OBSERVED' as const,
    },
  ]

  const wuas = [
    {
      id: 'w1',
      name: 'Bogʻolon WUA',
      fieldsCount: 14,
      totalAreaHa: 342.0,
      deliveryPerHa: 4120,
      quotaUtilizationPct: 91.2,
      status: 'MET',
      evidence: 'OBSERVED' as const,
    },
    {
      id: 'w2',
      name: 'Guliston Obi-Hayot WUA',
      fieldsCount: 18,
      totalAreaHa: 410.5,
      deliveryPerHa: 4350,
      quotaUtilizationPct: 94.0,
      status: 'MET',
      evidence: 'CALCULATED' as const,
    },
    {
      id: 'w3',
      name: 'Yangiariq Suv Ziyosi WUA',
      fieldsCount: 12,
      totalAreaHa: 285.0,
      deliveryPerHa: 3980,
      quotaUtilizationPct: 88.5,
      status: 'WITHIN_TOLERANCE',
      evidence: 'CALCULATED' as const,
    },
  ]

  const fields = [
    {
      id: 'f1',
      name: 'Field F-101 (Paxta, 24.5 ga)',
      owner: 'Yangiariq Paxtakor FX',
      deliveryFulfillmentPct: 96.0,
      adherencePct: 92.0,
      intensityM3Ha: 4200,
      evidence: 'OBSERVED' as const,
    },
    {
      id: 'f2',
      name: 'Field F-102 (Gʻalla, 18.0 ga)',
      owner: 'Zarafshon Baraka FX',
      deliveryFulfillmentPct: 91.0,
      adherencePct: 88.5,
      intensityM3Ha: 3950,
      evidence: 'OBSERVED' as const,
    },
    {
      id: 'f3',
      name: 'Field F-103 (Makkajoʻxori, 15.2 ga)',
      owner: 'Bogʻolon Hosildor FX',
      deliveryFulfillmentPct: 88.0,
      adherencePct: 84.0,
      intensityM3Ha: 4400,
      evidence: 'CALCULATED' as const,
    },
  ]

  return (
    <div className="p-5 rounded-xl bg-card border border-border shadow-sm space-y-4">
      {/* Header and Level Toggle */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div>
          <h3 className="font-semibold text-foreground flex items-center gap-2">
            <MapPin className="w-4 h-4 text-primary" />
            Hududiy Geografik Samaradorlik Koʻrsatkichlari
          </h3>
          <p className="text-xs text-muted-foreground mt-0.5">
            Magistral kanallar, Suv Isteʼmolchilari Uyushmalari (WUA) va dala kesimida natijalar
          </p>
        </div>

        <div className="flex items-center rounded-lg border border-border p-0.5 bg-muted/40 text-xs font-medium">
          <button
            onClick={() => setGeoLevel('canals')}
            className={`px-3 py-1 rounded-md transition-all ${
              geoLevel === 'canals'
                ? 'bg-background text-foreground shadow-sm'
                : 'text-muted-foreground hover:text-foreground'
            }`}
          >
            Kanallar
          </button>
          <button
            onClick={() => setGeoLevel('wuas')}
            className={`px-3 py-1 rounded-md transition-all ${
              geoLevel === 'wuas'
                ? 'bg-background text-foreground shadow-sm'
                : 'text-muted-foreground hover:text-foreground'
            }`}
          >
            WUA Uyushmalari
          </button>
          <button
            onClick={() => setGeoLevel('fields')}
            className={`px-3 py-1 rounded-md transition-all ${
              geoLevel === 'fields'
                ? 'bg-background text-foreground shadow-sm'
                : 'text-muted-foreground hover:text-foreground'
            }`}
          >
            Dalalar
          </button>
        </div>
      </div>

      {/* Table based on selected level */}
      {geoLevel === 'canals' && (
        <div className="overflow-x-auto rounded-lg border border-border">
          <table className="w-full text-xs text-left">
            <thead className="bg-muted/50 text-muted-foreground uppercase text-[10px] tracking-wider border-b border-border">
              <tr>
                <th className="py-2.5 px-3">Kanal Tarmogʻi</th>
                <th className="py-2.5 px-3">Turi & Uzunligi</th>
                <th className="py-2.5 px-3">Reja Ijrosi (%)</th>
                <th className="py-2.5 px-3">Yoʻqotish (%)</th>
                <th className="py-2.5 px-3">Tengsizlik Darajasi</th>
                <th className="py-2.5 px-3">Dalillar</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {canals.map((c) => (
                <tr
                  key={c.id}
                  onClick={() => onSelectEntity?.('CANAL', c.id, c.name)}
                  className="hover:bg-muted/30 transition-colors cursor-pointer"
                >
                  <td className="py-2.5 px-3 font-semibold text-foreground">{c.name}</td>
                  <td className="py-2.5 px-3 text-muted-foreground">
                    {c.type} • {c.lengthKm} km
                  </td>
                  <td className="py-2.5 px-3 font-mono font-bold text-emerald-500">
                    {c.deliveryFulfillmentPct}%
                  </td>
                  <td className="py-2.5 px-3 font-mono font-bold text-blue-500">
                    {c.conveyanceLossPct}%
                  </td>
                  <td className="py-2.5 px-3 text-[11px] text-muted-foreground">{c.disparityLevel}</td>
                  <td className="py-2.5 px-3">
                    <EvidenceClassificationBadge classification={c.evidence} size="sm" />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {geoLevel === 'wuas' && (
        <div className="overflow-x-auto rounded-lg border border-border">
          <table className="w-full text-xs text-left">
            <thead className="bg-muted/50 text-muted-foreground uppercase text-[10px] tracking-wider border-b border-border">
              <tr>
                <th className="py-2.5 px-3">WUA Nomi</th>
                <th className="py-2.5 px-3">Maydon & Dalalar</th>
                <th className="py-2.5 px-3">Intensivlik (m³/ga)</th>
                <th className="py-2.5 px-3">Kvota Sarfi (%)</th>
                <th className="py-2.5 px-3">Meʼyor Holati</th>
                <th className="py-2.5 px-3">Dalillar</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {wuas.map((w) => (
                <tr
                  key={w.id}
                  onClick={() => onSelectEntity?.('WUA', w.id, w.name)}
                  className="hover:bg-muted/30 transition-colors cursor-pointer"
                >
                  <td className="py-2.5 px-3 font-semibold text-foreground">{w.name}</td>
                  <td className="py-2.5 px-3 text-muted-foreground">
                    {w.totalAreaHa} ga • {w.fieldsCount} ta dala
                  </td>
                  <td className="py-2.5 px-3 font-mono font-bold text-foreground">
                    {w.deliveryPerHa.toLocaleString()} m³/ga
                  </td>
                  <td className="py-2.5 px-3 font-mono font-bold text-purple-500">
                    {w.quotaUtilizationPct}%
                  </td>
                  <td className="py-2.5 px-3 text-emerald-600 font-medium">{w.status}</td>
                  <td className="py-2.5 px-3">
                    <EvidenceClassificationBadge classification={w.evidence} size="sm" />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {geoLevel === 'fields' && (
        <div className="overflow-x-auto rounded-lg border border-border">
          <table className="w-full text-xs text-left">
            <thead className="bg-muted/50 text-muted-foreground uppercase text-[10px] tracking-wider border-b border-border">
              <tr>
                <th className="py-2.5 px-3">Dala & Ekin</th>
                <th className="py-2.5 px-3">Fermer Xoʻjaligi</th>
                <th className="py-2.5 px-3">Yetkazish Rejasi (%)</th>
                <th className="py-2.5 px-3">Jadval Intizomi (%)</th>
                <th className="py-2.5 px-3">Sarf (m³/ga)</th>
                <th className="py-2.5 px-3">Dalillar</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {fields.map((f) => (
                <tr
                  key={f.id}
                  onClick={() => onSelectEntity?.('FIELD', f.id, f.name)}
                  className="hover:bg-muted/30 transition-colors cursor-pointer"
                >
                  <td className="py-2.5 px-3 font-semibold text-foreground">{f.name}</td>
                  <td className="py-2.5 px-3 text-muted-foreground">{f.owner}</td>
                  <td className="py-2.5 px-3 font-mono font-bold text-emerald-500">
                    {f.deliveryFulfillmentPct}%
                  </td>
                  <td className="py-2.5 px-3 font-mono font-bold text-blue-500">{f.adherencePct}%</td>
                  <td className="py-2.5 px-3 font-mono font-bold text-foreground">
                    {f.intensityM3Ha.toLocaleString()}
                  </td>
                  <td className="py-2.5 px-3">
                    <EvidenceClassificationBadge classification={f.evidence} size="sm" />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
