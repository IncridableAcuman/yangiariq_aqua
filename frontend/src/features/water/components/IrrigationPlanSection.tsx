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
import { IrrigationPlanRow } from './IrrigationPlanRow'
import { IrrigationPlanCard } from './IrrigationPlanCard'
import { IrrigationPlan } from '@/entities/water'
import { Field } from '@/entities/field'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface IrrigationPlanSectionProps {
  plans: IrrigationPlan[]
  fields: Field[]
  canManage: boolean
  isLoading: boolean
  lang: SupportedLanguage
  onCreateClick: () => void
  onStartClick: (plan: IrrigationPlan) => void
  onCompleteClick: (plan: IrrigationPlan) => void
  onCancelClick: (plan: IrrigationPlan) => void
  onEditClick: (plan: IrrigationPlan) => void
}

export const IrrigationPlanSection: React.FC<IrrigationPlanSectionProps> = ({
  plans,
  fields,
  canManage,
  isLoading,
  lang,
  onCreateClick,
  onStartClick,
  onCompleteClick,
  onCancelClick,
  onEditClick,
}) => {
  const t = translations[lang]
  const [selectedFieldId, setSelectedFieldId] = useState<string>('ALL')
  const [selectedStatus, setSelectedStatus] = useState<string>('ALL')

  const fieldMap = new Map<string, Field>(fields.map((f) => [f.id, f]))

  const filteredPlans = plans.filter((p) => {
    if (selectedFieldId !== 'ALL' && p.fieldId !== selectedFieldId) return false
    if (selectedStatus !== 'ALL' && p.status !== selectedStatus) return false
    return true
  })

  return (
    <div className="space-y-4">
      {/* Header & Controls */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3">
        <div>
          <h3 className="text-lg font-semibold tracking-tight text-foreground">
            {t.water.plans.title}
          </h3>
          <p className="text-xs text-muted-foreground">
            Total {filteredPlans.length} irrigation plans
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
              <option value="PLANNED">{t.water.statuses.PLANNED}</option>
              <option value="IN_PROGRESS">{t.water.statuses.IN_PROGRESS}</option>
              <option value="COMPLETED">{t.water.statuses.COMPLETED}</option>
              <option value="CANCELLED">{t.water.statuses.CANCELLED}</option>
            </select>
          </div>

          {/* Create Plan Button */}
          {canManage && (
            <Button
              size="sm"
              onClick={onCreateClick}
              className="gap-1.5 text-xs h-8 ml-auto sm:ml-0"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>{t.water.plans.create}</span>
            </Button>
          )}
        </div>
      </div>

      {/* Empty State */}
      {filteredPlans.length === 0 && !isLoading && (
        <div className="text-center py-12 px-4 rounded-xl border border-dashed border-border bg-card">
          <p className="text-sm font-medium text-muted-foreground">
            {t.water.plans.noPlans}
          </p>
          {canManage && (
            <Button
              variant="outline"
              size="sm"
              onClick={onCreateClick}
              className="mt-3 gap-1.5"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>{t.water.plans.create}</span>
            </Button>
          )}
        </div>
      )}

      {/* Desktop Table View */}
      {filteredPlans.length > 0 && (
        <div className="hidden md:block rounded-lg border border-border bg-card overflow-hidden">
          <Table>
            <TableHeader className="bg-muted/40">
              <TableRow>
                <TableHead>{t.water.plans.field}</TableHead>
                <TableHead>{t.water.plans.plannedStart}</TableHead>
                <TableHead>{t.water.plans.plannedVolume}</TableHead>
                <TableHead>{t.water.plans.actualVolume}</TableHead>
                <TableHead>{t.water.plans.variance}</TableHead>
                <TableHead>{t.water.plans.status}</TableHead>
                <TableHead className="text-right">{t.common.actions}</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredPlans.map((plan) => (
                <IrrigationPlanRow
                  key={plan.id}
                  plan={plan}
                  field={fieldMap.get(plan.fieldId)}
                  canManage={canManage}
                  lang={lang}
                  onStart={onStartClick}
                  onComplete={onCompleteClick}
                  onCancel={onCancelClick}
                  onEdit={onEditClick}
                />
              ))}
            </TableBody>
          </Table>
        </div>
      )}

      {/* Mobile Stacked Card View */}
      {filteredPlans.length > 0 && (
        <div className="grid grid-cols-1 gap-3 md:hidden">
          {filteredPlans.map((plan) => (
            <IrrigationPlanCard
              key={plan.id}
              plan={plan}
              field={fieldMap.get(plan.fieldId)}
              canManage={canManage}
              lang={lang}
              onStart={onStartClick}
              onComplete={onCompleteClick}
              onCancel={onCancelClick}
              onEdit={onEditClick}
            />
          ))}
        </div>
      )}
    </div>
  )
}
