import React, { useState } from 'react'
import { BookOpen, Search } from 'lucide-react'
import { PerformanceIndicator } from '@/entities/performance'

interface Props {
  indicators: PerformanceIndicator[]
  onSelectIndicator?: (indicator: PerformanceIndicator) => void
}

export const PerformanceIndicatorCatalog: React.FC<Props> = ({ indicators, onSelectIndicator }) => {
  const [search, setSearch] = useState('')
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL')

  const categories: { id: string; label: string }[] = [
    { id: 'ALL', label: 'Barchasi' },
    { id: 'DELIVERY_RELIABILITY', label: 'Yetkazib berish' },
    { id: 'CONVEYANCE_EFFICIENCY', label: 'Kanal samaradorligi' },
    { id: 'ALLOCATION_COMPLIANCE', label: 'Kvota intizomi' },
    { id: 'DISTRIBUTION_EQUITY', label: 'Taqsimot adolati' },
    { id: 'ACCOUNTABILITY_EFFECTIVENESS', label: 'Javobgarlik va Tekshiruv' },
    { id: 'DATA_QUALITY', label: 'Maʼlumot sifati' },
    { id: 'PREDICTIVE_INTELLIGENCE', label: 'Prognoz aniqligi' },
    { id: 'CONSERVATION_OUTCOME', label: 'Suv tejamkorligi' },
  ]

  const filtered = indicators.filter((ind) => {
    const matchesSearch =
      ind.name.toLowerCase().includes(search.toLowerCase()) ||
      ind.code.toLowerCase().includes(search.toLowerCase()) ||
      ind.description.toLowerCase().includes(search.toLowerCase())

    const matchesCategory = selectedCategory === 'ALL' || ind.category === selectedCategory
    return matchesSearch && matchesCategory
  })

  return (
    <div className="p-5 rounded-xl bg-card border border-border shadow-sm space-y-4">
      {/* Search and filters */}
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3">
        <div>
          <h3 className="font-semibold text-foreground flex items-center gap-2">
            <BookOpen className="w-4 h-4 text-primary" />
            Operatsion Koʻrsatkichlar Katalogi (Indicator Catalog)
          </h3>
          <p className="text-xs text-muted-foreground mt-0.5">
            Barcha indikatorlarning qatʼiy matematik formulalari, SI birliklari va oʻlchov talablari
          </p>
        </div>

        <div className="flex items-center gap-2">
          <div className="relative flex-1 sm:w-64">
            <Search className="w-4 h-4 absolute left-2.5 top-2.5 text-muted-foreground" />
            <input
              type="text"
              placeholder="Indikatorni qidirish..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="w-full pl-8 pr-3 py-1.5 text-xs rounded-lg border border-border bg-background text-foreground focus:outline-none focus:ring-1 focus:ring-primary"
            />
          </div>
        </div>
      </div>

      {/* Category pill filters */}
      <div className="flex items-center gap-1.5 overflow-x-auto pb-1 scrollbar-none">
        {categories.map((cat) => (
          <button
            key={cat.id}
            onClick={() => setSelectedCategory(cat.id)}
            className={`px-2.5 py-1 text-xs font-medium rounded-full whitespace-nowrap transition-colors ${
              selectedCategory === cat.id
                ? 'bg-primary text-primary-foreground shadow-sm'
                : 'bg-muted/50 text-muted-foreground hover:bg-muted hover:text-foreground'
            }`}
          >
            {cat.label}
          </button>
        ))}
      </div>

      {/* Table of indicators */}
      <div className="overflow-x-auto rounded-lg border border-border">
        <table className="w-full text-xs text-left">
          <thead className="bg-muted/50 text-muted-foreground uppercase text-[10px] tracking-wider border-b border-border">
            <tr>
              <th className="py-2.5 px-3">Kod & Nomi</th>
              <th className="py-2.5 px-3">Kategoriya</th>
              <th className="py-2.5 px-3">Birlik</th>
              <th className="py-2.5 px-3">Hisoblash Formulasi</th>
              <th className="py-2.5 px-3">Hududiy Qamrov</th>
              <th className="py-2.5 px-3">Min. Sifat</th>
              <th className="py-2.5 px-3">Versiya</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-border">
            {filtered.map((ind) => (
              <tr
                key={ind.id}
                onClick={() => onSelectIndicator?.(ind)}
                className="hover:bg-muted/30 transition-colors cursor-pointer"
              >
                <td className="py-2.5 px-3 font-medium text-foreground">
                  <div>
                    <span className="font-mono text-primary font-semibold">{ind.code}</span>
                    <p className="text-[11px] text-muted-foreground mt-0.5 line-clamp-1">{ind.name}</p>
                  </div>
                </td>
                <td className="py-2.5 px-3">
                  <span className="px-2 py-0.5 rounded-full text-[10px] bg-secondary text-secondary-foreground font-medium">
                    {ind.category}
                  </span>
                </td>
                <td className="py-2.5 px-3 font-mono font-semibold text-foreground">
                  {ind.measurementUnit}
                </td>
                <td className="py-2.5 px-3 font-mono text-[11px] text-zinc-600 dark:text-zinc-300 max-w-xs truncate" title={ind.calculationMethod}>
                  {ind.calculationMethod}
                </td>
                <td className="py-2.5 px-3 text-muted-foreground">
                  {ind.supportedGeographicScope}
                </td>
                <td className="py-2.5 px-3 text-foreground font-medium">
                  {ind.minimumDataQualityPct}%
                </td>
                <td className="py-2.5 px-3 font-mono text-muted-foreground text-[10px]">
                  {ind.calculationVersion}
                </td>
              </tr>
            ))}
            {filtered.length === 0 && (
              <tr>
                <td colSpan={7} className="text-center py-6 text-muted-foreground">
                  Qidiruv boʻyicha hech qanday koʻrsatkich topilmadi
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
