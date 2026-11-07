import React, { useState } from 'react'
import {
  AlertOctagon,
  ChevronLeft,
  ChevronRight,
  Eye,
  Search,
} from 'lucide-react'
import { FieldRiskProfile } from '@/entities/risk'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/ui/table'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { Badge } from '@/shared/ui/badge'
import { RiskSeverityBadge } from './RiskSeverityBadge'
import { RiskConfidenceIndicator } from './RiskConfidenceIndicator'
import { cn } from '@/shared/lib/utils'

interface FieldRiskTableProps {
  fields: FieldRiskProfile[]
  isLoading?: boolean
  totalElements?: number
  page?: number
  totalPages?: number
  onPageChange?: (newPage: number) => void
  onSelectField: (fieldId: string) => void
  onSearchChange?: (term: string) => void
  onSeverityFilterChange?: (severity: string) => void
  onCategoryFilterChange?: (category: string) => void
}

export const FieldRiskTable: React.FC<FieldRiskTableProps> = ({
  fields,
  isLoading,
  totalElements = 0,
  page = 0,
  totalPages = 1,
  onPageChange,
  onSelectField,
  onSearchChange,
  onSeverityFilterChange,
  onCategoryFilterChange,
}) => {
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedSeverity, setSelectedSeverity] = useState('ALL')
  const [selectedCategory, setSelectedCategory] = useState('ALL')

  const handleSearch = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value
    setSearchTerm(val)
    if (onSearchChange) onSearchChange(val)
  }

  const handleSeverity = (sev: string) => {
    setSelectedSeverity(sev)
    if (onSeverityFilterChange) onSeverityFilterChange(sev)
  }

  const handleCategory = (cat: string) => {
    setSelectedCategory(cat)
    if (onCategoryFilterChange) onCategoryFilterChange(cat)
  }

  return (
    <div className="space-y-4" data-testid="field-risk-table-container">
      {/* Controls Bar: Search & Filters */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-3">
        <div className="relative w-full sm:w-72">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
          <Input
            value={searchTerm}
            onChange={handleSearch}
            placeholder="Search parcel name..."
            className="pl-9 h-9 text-xs"
            data-testid="field-risk-search-input"
          />
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto overflow-x-auto pb-1 sm:pb-0">
          <select
            value={selectedSeverity}
            onChange={(e) => handleSeverity(e.target.value)}
            className="h-9 px-2.5 py-1 text-xs rounded-md border border-input bg-background text-foreground shadow-sm focus:outline-none focus:ring-1 focus:ring-ring"
            data-testid="field-risk-severity-select"
          >
            <option value="ALL">All Severities</option>
            <option value="CRITICAL">Critical</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>

          <select
            value={selectedCategory}
            onChange={(e) => handleCategory(e.target.value)}
            className="h-9 px-2.5 py-1 text-xs rounded-md border border-input bg-background text-foreground shadow-sm focus:outline-none focus:ring-1 focus:ring-ring"
            data-testid="field-risk-category-select"
          >
            <option value="ALL">All Risk Domains</option>
            <option value="HYDRAULIC">Hydraulic</option>
            <option value="WATER_AVAILABILITY">Water Availability</option>
            <option value="SOIL_MOISTURE">Soil Moisture</option>
            <option value="IRRIGATION_OPERATIONAL">Irrigation Operations</option>
            <option value="SENSOR_DATA_RELIABILITY">Sensor & Telemetry</option>
            <option value="PREDICTIVE_MODEL_RELIABILITY">Predictive Models</option>
          </select>
        </div>
      </div>

      {/* Table */}
      <div className="rounded-xl border bg-card overflow-hidden shadow-sm">
        <Table>
          <TableHeader className="bg-muted/40">
            <TableRow>
              <TableHead className="w-16 text-center text-xs font-semibold">Priority</TableHead>
              <TableHead className="text-xs font-semibold">Field Parcel</TableHead>
              <TableHead className="text-xs font-semibold">Severity</TableHead>
              <TableHead className="text-xs font-semibold">Evidence Confidence</TableHead>
              <TableHead className="text-xs font-semibold">Primary Risk Domain</TableHead>
              <TableHead className="text-xs font-semibold">Composite Score</TableHead>
              <TableHead className="text-xs font-semibold">Status</TableHead>
              <TableHead className="w-24 text-right text-xs font-semibold">Action</TableHead>
            </TableRow>
          </TableHeader>

          <TableBody>
            {isLoading ? (
              Array.from({ length: 5 }).map((_, i) => (
                <TableRow key={i} className="animate-pulse">
                  <TableCell colSpan={8} className="h-12 bg-muted/20" />
                </TableRow>
              ))
            ) : fields.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} className="h-32 text-center text-muted-foreground text-xs">
                  No risk profiles matching the specified criteria.
                </TableCell>
              </TableRow>
            ) : (
              fields.map((f) => (
                <TableRow
                  key={f.fieldId}
                  className={cn(
                    'cursor-pointer hover:bg-muted/40 transition-colors',
                    f.isHydraulicBlocked ? 'bg-rose-500/5' : ''
                  )}
                  onClick={() => onSelectField(f.fieldId)}
                  data-testid={`field-risk-row-${f.fieldId}`}
                >
                  {/* Priority Rank */}
                  <TableCell className="text-center font-bold text-xs">
                    <span
                      className={cn(
                        'inline-flex items-center justify-center w-6 h-6 rounded-full text-xs font-bold',
                        f.priorityRank === 1
                          ? 'bg-rose-600 text-white animate-pulse'
                          : f.priorityRank <= 3
                          ? 'bg-amber-500/20 text-amber-700 dark:text-amber-400'
                          : 'bg-muted text-muted-foreground'
                      )}
                    >
                      {f.priorityRank}
                    </span>
                  </TableCell>

                  {/* Parcel Name & Details */}
                  <TableCell>
                    <div className="space-y-0.5">
                      <div className="flex items-center gap-1.5 font-bold text-xs text-foreground">
                        {f.fieldName}
                        {f.isHydraulicBlocked && (
                          <span title="Hydraulic Interlock">
                            <AlertOctagon className="w-3.5 h-3.5 text-rose-600 shrink-0" />
                          </span>
                        )}
                      </div>
                      <div className="text-[11px] text-muted-foreground">
                        {f.cropType || 'N/A'} • {f.areaHectares ? `${f.areaHectares} ha` : ''}
                      </div>
                    </div>
                  </TableCell>

                  {/* Severity */}
                  <TableCell>
                    <RiskSeverityBadge severity={f.overallSeverity} size="sm" />
                  </TableCell>

                  {/* Confidence */}
                  <TableCell>
                    <RiskConfidenceIndicator confidence={f.overallConfidence} size="sm" />
                  </TableCell>

                  {/* Category */}
                  <TableCell>
                    <span className="text-xs font-medium text-foreground">
                      {f.primaryRiskCategory.replace(/_/g, ' ')}
                    </span>
                  </TableCell>

                  {/* Score */}
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <div className="w-16 h-2 rounded-full bg-muted overflow-hidden">
                        <div
                          className={cn(
                            'h-full rounded-full',
                            f.riskScore >= 75 ? 'bg-rose-500' : f.riskScore >= 50 ? 'bg-amber-500' : 'bg-emerald-500'
                          )}
                          style={{ width: `${Math.min(100, Math.max(0, f.riskScore))}%` }}
                        />
                      </div>
                      <span className="text-xs font-bold text-foreground">{f.riskScore.toFixed(0)}</span>
                    </div>
                  </TableCell>

                  {/* Status */}
                  <TableCell>
                    <Badge
                      variant="outline"
                      className={cn(
                        'text-[10px] font-semibold tracking-wider uppercase',
                        f.lifecycleStatus === 'OPEN'
                          ? 'bg-amber-500/10 text-amber-700 border-amber-300 dark:border-amber-900'
                          : f.lifecycleStatus === 'ACKNOWLEDGED'
                          ? 'bg-blue-500/10 text-blue-700 border-blue-300 dark:border-blue-900'
                          : 'bg-emerald-500/10 text-emerald-700 border-emerald-300 dark:border-emerald-900'
                      )}
                    >
                      {f.lifecycleStatus}
                    </Badge>
                  </TableCell>

                  {/* Action */}
                  <TableCell className="text-right">
                    <Button
                      size="sm"
                      variant="ghost"
                      className="h-8 px-2 text-xs gap-1 text-primary hover:text-primary hover:bg-primary/10"
                      onClick={(e) => {
                        e.stopPropagation()
                        onSelectField(f.fieldId)
                      }}
                    >
                      <Eye className="w-3.5 h-3.5" />
                      <span>Details</span>
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      {/* Pagination Bar */}
      {totalPages > 1 && onPageChange && (
        <div className="flex items-center justify-between text-xs text-muted-foreground px-1">
          <span>
            Showing page {page + 1} of {totalPages} ({totalElements} total parcels)
          </span>
          <div className="flex items-center gap-1.5">
            <Button
              size="sm"
              variant="outline"
              disabled={page <= 0}
              onClick={() => onPageChange(page - 1)}
              className="h-8 px-2.5 text-xs gap-1"
            >
              <ChevronLeft className="w-3.5 h-3.5" />
              <span>Previous</span>
            </Button>
            <Button
              size="sm"
              variant="outline"
              disabled={page + 1 >= totalPages}
              onClick={() => onPageChange(page + 1)}
              className="h-8 px-2.5 text-xs gap-1"
            >
              <span>Next</span>
              <ChevronRight className="w-3.5 h-3.5" />
            </Button>
          </div>
        </div>
      )}
    </div>
  )
}
