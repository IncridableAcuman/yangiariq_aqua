import React, { useState } from 'react'
import { Plus, Filter } from 'lucide-react'
import { Button } from '@/shared/ui/button'
import {
  Table,
  TableBody,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/ui/table'
import { WaterAllocationRow } from './WaterAllocationRow'
import { WaterAllocationCard } from './WaterAllocationCard'
import { WaterAllocation, WaterSource } from '@/entities/water'
import { Field } from '@/entities/field'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface WaterAllocationSectionProps {
  allocations: WaterAllocation[]
  fields: Field[]
  sources: WaterSource[]
  canManage: boolean
  isLoading: boolean
  lang: SupportedLanguage
  onCreateClick: () => void
  onEditClick: (allocation: WaterAllocation) => void
  onCloseClick: (allocation: WaterAllocation) => void
  onCancelClick: (allocation: WaterAllocation) => void
}

export const WaterAllocationSection: React.FC<WaterAllocationSectionProps> = ({
  allocations,
  fields,
  sources,
  canManage,
  isLoading,
  lang,
  onCreateClick,
  onEditClick,
  onCloseClick,
  onCancelClick,
}) => {
  const t = translations[lang]
  const [selectedFieldId, setSelectedFieldId] = useState<string>('ALL')
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL')

  const fieldMap = new Map<string, Field>(fields.map((f) => [f.id, f]))
  const sourceMap = new Map<string, WaterSource>(sources.map((s) => [s.id, s]))

  const filteredAllocations = allocations.filter((a) => {
    if (selectedFieldId !== 'ALL' && a.fieldId !== selectedFieldId) return false
    if (selectedStatus !== 'ALL' && a.status !== selectedStatus) return false
    return true
  })

  return (
    <div className="space-y-4">
      {/* Header & Controls */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
        <div>
          <h3 className="text-lg font-semibold tracking-tight text-foreground">
            {t.water.allocations.title}
          </h3>
          <p className="text-xs text-muted-foreground">
            Total {filteredAllocations.length} quota records
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2 w-full sm:w-auto">
          {/* Field Filter */}
          <div className="flex items-center gap-1.5 bg-background border border-input rounded-md px-2 py-1 text-xs">
            <Filter className="w-3.5 h-3.5 text-muted-foreground" />
            <select
              value={selectedFieldId}
              onChange={(e) => setSelectedFieldId(e.target.value)}
              className="bg-transparent focus:outline-none text-xs"
            >
              <option value="ALL">All Fields</option>
              {fields.map((f) => (
                <option key={f.id} value={f.id}>
                  {f.name}
                </option>
              ))}
            </select>
          </div>

          {/* Status Filter */}
          <div className="flex items-center gap-1.5 bg-background border border-input rounded-md px-2 py-1 text-xs">
            <select
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
              className="bg-transparent focus:outline-none text-xs"
            >
              <option value="ALL">All Statuses</option>
              <option value="ACTIVE">{t.water.statuses.ACTIVE}</option>
              <option value="CLOSED">{t.water.statuses.CLOSED}</option>
              <option value="CANCELLED">{t.water.statuses.CANCELLED}</option>
            </select>
          </div>

          {/* Create Button */}
          {canManage && (
            <Button
              size="sm"
              onClick={onCreateClick}
              className="gap-1.5 text-xs h-8 ml-auto sm:ml-0"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>{t.water.allocations.create}</span>
            </Button>
          )}
        </div>
      </div>

      {/* Empty State */}
      {filteredAllocations.length === 0 && !isLoading && (
        <div className="text-center py-12 px-4 rounded-xl border border-dashed border-border bg-card">
          <p className="text-sm font-medium text-muted-foreground">
            {t.water.allocations.noAllocations}
          </p>
          {canManage && (
            <Button
              variant="outline"
              size="sm"
              onClick={onCreateClick}
              className="mt-3 gap-1.5"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>{t.water.allocations.create}</span>
            </Button>
          )}
        </div>
      )}

      {/* Desktop Table View */}
      {filteredAllocations.length > 0 && (
        <div className="hidden md:block rounded-lg border border-border bg-card overflow-hidden">
          <Table>
            <TableHeader className="bg-muted/40">
              <TableRow>
                <TableHead>{t.water.allocations.field}</TableHead>
                <TableHead>{t.water.allocations.source}</TableHead>
                <TableHead>{t.water.allocations.period}</TableHead>
                <TableHead>{t.water.allocations.volume}</TableHead>
                <TableHead>{t.water.allocations.consumed}</TableHead>
                <TableHead>{t.water.allocations.remaining}</TableHead>
                <TableHead>{t.water.allocations.progress}</TableHead>
                <TableHead>{t.water.allocations.status}</TableHead>
                <TableHead className="text-right">{t.common.actions}</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredAllocations.map((alloc) => (
                <WaterAllocationRow
                  key={alloc.id}
                  allocation={alloc}
                  field={fieldMap.get(alloc.fieldId)}
                  source={sourceMap.get(alloc.waterSourceId)}
                  canManage={canManage}
                  lang={lang}
                  onEdit={onEditClick}
                  onClose={onCloseClick}
                  onCancel={onCancelClick}
                />
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      {/* Mobile Stacked Card View */}
      {filteredAllocations.length > 0 && (
        <div className="grid grid-cols-1 gap-3 md:hidden">
          {filteredAllocations.map((alloc) => (
            <WaterAllocationCard
              key={alloc.id}
              allocation={alloc}
              field={fieldMap.get(alloc.fieldId)}
              source={sourceMap.get(alloc.waterSourceId)}
              canManage={canManage}
              lang={lang}
              onEdit={onEditClick}
              onClose={onCloseClick}
              onCancel={onCancelClick}
            />
          ))}
        </div>
      )}
    </div>
  )
}
