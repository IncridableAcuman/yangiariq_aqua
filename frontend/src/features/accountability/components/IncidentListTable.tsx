import React, { useState, useMemo } from 'react'
import { WaterIncident } from '@/entities/accountability'
import { IncidentSeverityBadge } from './IncidentSeverityBadge'
import { IncidentStatusBadge } from './IncidentStatusBadge'
import { AccountabilityEvidenceBadge } from './AccountabilityEvidenceBadge'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import {
  AlertTriangle,
  Clock,
  Eye,
  Filter,
  MapPin,
  Search,
  UserCheck,
} from 'lucide-react'

interface Props {
  incidents: WaterIncident[]
  isLoading?: boolean
  onSelectIncident: (incident: WaterIncident) => void
  onReviewIncident?: (incident: WaterIncident) => void
  onAssignIncident?: (incident: WaterIncident) => void
  activeStatusFilter?: string
  onClearFilters?: () => void
}

export const IncidentListTable: React.FC<Props> = ({
  incidents,
  isLoading = false,
  onSelectIncident,
  onReviewIncident,
  onAssignIncident,
  activeStatusFilter,
  onClearFilters,
}) => {
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL')
  const [selectedSeverity, setSelectedSeverity] = useState<string>('ALL')
  const [selectedStatus, setSelectedStatus] = useState<string>(activeStatusFilter || 'ALL')
  const [overdueOnly, setOverdueOnly] = useState<boolean>(activeStatusFilter === 'OVERDUE')

  const filteredIncidents = useMemo(() => {
    return incidents.filter((inc) => {
      // Search term
      if (searchTerm.trim()) {
        const term = searchTerm.toLowerCase()
        const matchesTitle = inc.title.toLowerCase().includes(term)
        const matchesNumber = inc.incidentNumber.toLowerCase().includes(term)
        const matchesDesc = inc.description?.toLowerCase().includes(term) ?? false
        const matchesLocation = inc.locationDescription?.toLowerCase().includes(term) ?? false
        if (!matchesTitle && !matchesNumber && !matchesDesc && !matchesLocation) {
          return false
        }
      }

      // Category filter
      if (selectedCategory !== 'ALL' && inc.category !== selectedCategory) {
        return false
      }

      // Severity filter
      if (selectedSeverity !== 'ALL' && inc.severity !== selectedSeverity) {
        return false
      }

      // Status filter
      if (selectedStatus !== 'ALL' && selectedStatus !== 'OVERDUE' && inc.status !== selectedStatus) {
        return false
      }

      // Overdue filter
      if (overdueOnly && !inc.isOverdue) {
        return false
      }

      return true
    })
  }, [incidents, searchTerm, selectedCategory, selectedSeverity, selectedStatus, overdueOnly])

  return (
    <div className="space-y-4">
      {/* Controls & Filters Bar */}
      <div className="flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-3 bg-card p-3 sm:p-4 rounded-xl border border-border">
        {/* Search */}
        <div className="relative flex-1 min-w-[240px]">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <Input
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Hodisa raqami, kanal, maydon yoki tavsif boʻyicha qidirish..."
            className="pl-9 h-9 text-xs"
          />
        </div>

        {/* Filter Dropdowns */}
        <div className="flex flex-wrap items-center gap-2">
          {/* Category */}
          <select
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
            className="h-9 px-2.5 rounded-md border border-input bg-background text-xs font-medium text-foreground focus:outline-none focus:ring-1 focus:ring-primary"
          >
            <option value="ALL">Barcha kategoriyalar</option>
            <option value="WATER_BALANCE_DISCREPANCY">Suv balansi tafovuti</option>
            <option value="CONVEYANCE_LOSS">Kanal oqish/yoʻqotishi</option>
            <option value="UNACCOUNTED_WITHDRAWAL">Hisobga olinmagan olish</option>
            <option value="ALLOCATION_EXCEEDED">Limitdan oshish</option>
            <option value="UNDER_DELIVERY">Yetkazib berilmaganlik</option>
            <option value="DISTRIBUTION_DISPARITY">Tengsizlik/Adolatsizlik</option>
            <option value="HYDRAULIC_SAFETY">Gidravlik xavfsizlik</option>
            <option value="SENSOR_DATA_QUALITY">Datchik maʼlumot sifati</option>
            <option value="OPERATIONAL_NON_COMPLIANCE">Operatsion qoidabuzarlik</option>
          </select>

          {/* Severity */}
          <select
            value={selectedSeverity}
            onChange={(e) => setSelectedSeverity(e.target.value)}
            className="h-9 px-2.5 rounded-md border border-input bg-background text-xs font-medium text-foreground focus:outline-none focus:ring-1 focus:ring-primary"
          >
            <option value="ALL">Barcha darajalar</option>
            <option value="CRITICAL">Kritik (4h SLA)</option>
            <option value="HIGH">Yuqori (24h SLA)</option>
            <option value="MEDIUM">Oʻrtacha (72h SLA)</option>
            <option value="LOW">Past (7 kun SLA)</option>
            <option value="INFORMATIONAL">Axborot</option>
          </select>

          {/* Status */}
          <select
            value={selectedStatus}
            onChange={(e) => {
              setSelectedStatus(e.target.value)
              if (e.target.value === 'OVERDUE') {
                setOverdueOnly(true)
              }
            }}
            className="h-9 px-2.5 rounded-md border border-input bg-background text-xs font-medium text-foreground focus:outline-none focus:ring-1 focus:ring-primary"
          >
            <option value="ALL">Barcha holatlar</option>
            <option value="DETECTED">Aniqlangan (Detected)</option>
            <option value="UNDER_REVIEW">Koʻrib chiqilmoqda</option>
            <option value="OPEN">Ochiq (Open)</option>
            <option value="INVESTIGATING">Tekshirilmoqda</option>
            <option value="ACTION_PROPOSED">Chora taklif etilgan</option>
            <option value="ACTION_APPROVED">Chora tasdiqlangan</option>
            <option value="PENDING_VERIFICATION">Tekshiruv kutilmoqda</option>
            <option value="RESOLVED">Hal qilingan</option>
            <option value="DISMISSED">Rad etilgan</option>
            <option value="REOPENED">Qayta ochilgan</option>
          </select>

          {/* Overdue Button */}
          <Button
            variant={overdueOnly ? 'destructive' : 'outline'}
            size="sm"
            onClick={() => setOverdueOnly(!overdueOnly)}
            className="h-9 text-xs gap-1.5"
          >
            <AlertTriangle className="w-3.5 h-3.5" />
            <span>Faqat muddat oʻtganlar</span>
          </Button>

          {/* Clear filters */}
          {(searchTerm || selectedCategory !== 'ALL' || selectedSeverity !== 'ALL' || selectedStatus !== 'ALL' || overdueOnly) && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => {
                setSearchTerm('')
                setSelectedCategory('ALL')
                setSelectedSeverity('ALL')
                setSelectedStatus('ALL')
                setOverdueOnly(false)
                onClearFilters?.()
              }}
              className="h-9 text-xs text-muted-foreground hover:text-foreground"
            >
              Tozalash
            </Button>
          )}
        </div>
      </div>

      {/* Incidents Table */}
      <div className="bg-card border border-border rounded-xl overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="border-b border-border bg-muted/40 text-muted-foreground uppercase text-[10px] tracking-wider font-semibold">
                <th className="py-3 px-4">Hodisa / Nomi</th>
                <th className="py-3 px-4">Kategoriya</th>
                <th className="py-3 px-4">Ogʻirligi</th>
                <th className="py-3 px-4">Joylashuvi</th>
                <th className="py-3 px-4">Dalil ishonchliligi</th>
                <th className="py-3 px-4">Holati</th>
                <th className="py-3 px-4">SLA / Muddat</th>
                <th className="py-3 px-4 text-right">Amal</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/60">
              {isLoading ? (
                [...Array(5)].map((_, i) => (
                  <tr key={i} className="animate-pulse">
                    <td colSpan={8} className="py-4 px-4">
                      <div className="h-4 bg-muted rounded w-3/4" />
                    </td>
                  </tr>
                ))
              ) : filteredIncidents.length === 0 ? (
                <tr>
                  <td colSpan={8} className="py-12 text-center text-muted-foreground">
                    <div className="flex flex-col items-center justify-center space-y-2">
                      <Filter className="w-8 h-8 text-muted-foreground/40" />
                      <p className="text-sm font-medium">Hech qanday operatsion hodisa topilmadi</p>
                      <p className="text-xs text-muted-foreground">
                        Filtrlarni oʻzgartiring yoki yangi tekshiruv hodisasini roʻyxatdan oʻtkazing.
                      </p>
                    </div>
                  </td>
                </tr>
              ) : (
                filteredIncidents.map((inc) => (
                  <tr
                    key={inc.id}
                    className="hover:bg-muted/30 transition-colors cursor-pointer group"
                    onClick={() => onSelectIncident(inc)}
                  >
                    {/* Number & Title */}
                    <td className="py-3.5 px-4 font-medium text-foreground max-w-[260px]">
                      <div className="flex items-center gap-2">
                        <span className="font-mono text-[11px] px-1.5 py-0.5 rounded bg-muted text-muted-foreground font-semibold">
                          {inc.incidentNumber}
                        </span>
                        {inc.recurrenceCount > 0 && (
                          <span className="text-[10px] font-bold px-1.5 py-0.5 rounded bg-rose-500/10 text-rose-500 border border-rose-500/20" title={`Qaytalanishlar soni: ${inc.recurrenceCount}`}>
                            Qayta #{inc.recurrenceCount}
                          </span>
                        )}
                      </div>
                      <p className="text-xs font-semibold text-foreground line-clamp-1 mt-1 group-hover:text-primary transition-colors">
                        {inc.title}
                      </p>
                    </td>

                    {/* Category */}
                    <td className="py-3.5 px-4 text-muted-foreground font-mono text-[11px]">
                      {inc.category.replace(/_/g, ' ')}
                    </td>

                    {/* Severity */}
                    <td className="py-3.5 px-4">
                      <IncidentSeverityBadge severity={inc.severity} />
                    </td>

                    {/* Location */}
                    <td className="py-3.5 px-4 text-muted-foreground max-w-[180px]">
                      <div className="flex items-center gap-1.5 line-clamp-1" title={inc.locationDescription || 'Nomaʼlum joy'}>
                        <MapPin className="w-3.5 h-3.5 shrink-0 text-muted-foreground/60" />
                        <span className="truncate">{inc.locationDescription || 'Hududiy obyekt'}</span>
                      </div>
                    </td>

                    {/* Evidence Confidence */}
                    <td className="py-3.5 px-4">
                      <AccountabilityEvidenceBadge confidence={inc.confidence} />
                    </td>

                    {/* Status */}
                    <td className="py-3.5 px-4">
                      <IncidentStatusBadge status={inc.status} />
                    </td>

                    {/* SLA Deadline / Overdue */}
                    <td className="py-3.5 px-4">
                      {inc.isOverdue ? (
                        <div className="inline-flex items-center gap-1 text-rose-600 dark:text-rose-400 font-semibold text-[11px]">
                          <AlertTriangle className="w-3.5 h-3.5 animate-pulse" />
                          <span>MUDDAT BUZILGAN</span>
                        </div>
                      ) : inc.slaDeadline ? (
                        <div className="flex items-center gap-1 text-muted-foreground text-[11px]">
                          <Clock className="w-3 h-3 text-muted-foreground/60" />
                          <span>{new Date(inc.slaDeadline).toLocaleDateString()} {new Date(inc.slaDeadline).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                        </div>
                      ) : (
                        <span className="text-muted-foreground/60 text-[11px]">—</span>
                      )}
                    </td>

                    {/* Action buttons */}
                    <td className="py-3.5 px-4 text-right">
                      <div className="flex items-center justify-end gap-1.5" onClick={(e) => e.stopPropagation()}>
                        {inc.status === 'DETECTED' && onReviewIncident && (
                          <Button
                            size="sm"
                            variant="outline"
                            className="h-7 text-[11px] px-2.5 font-medium border-primary/40 text-primary hover:bg-primary/10"
                            onClick={() => onReviewIncident(inc)}
                          >
                            Koʻrib chiqish
                          </Button>
                        )}
                        {inc.status === 'OPEN' && onAssignIncident && (
                          <Button
                            size="sm"
                            variant="outline"
                            className="h-7 text-[11px] px-2.5 font-medium border-indigo-500/40 text-indigo-600 dark:text-indigo-400 hover:bg-indigo-500/10"
                            onClick={() => onAssignIncident(inc)}
                          >
                            <UserCheck className="w-3 h-3 mr-1" />
                            Biriktirish
                          </Button>
                        )}
                        <Button
                          size="sm"
                          variant="ghost"
                          className="h-7 w-7 p-0 text-muted-foreground hover:text-foreground"
                          onClick={() => onSelectIncident(inc)}
                        >
                          <Eye className="w-3.5 h-3.5" />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
