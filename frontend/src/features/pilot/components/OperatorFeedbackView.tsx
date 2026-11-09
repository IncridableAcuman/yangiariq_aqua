import React, { useState } from 'react'
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  Button,
  Badge,
} from '@/shared/ui'
import {
  MessageSquare,
  PlusCircle,
  Search,
  Filter,
  CheckCircle2,
  Clock,
  AlertTriangle,
  XCircle,
  FileText,
} from 'lucide-react'
import { translations, SupportedLang } from '@/shared/i18n'
import {
  OperatorFeedbackStatus,
  OperatorFeedbackCategory,
} from '@/entities/pilot'
import {
  useOperatorFeedbackQuery,
  useSubmitFeedbackMutation,
} from '../queries/usePilotQueries'
import { SubmitFeedbackDialog } from './SubmitFeedbackDialog'

interface OperatorFeedbackViewProps {
  pilotId: string
  lang: SupportedLang
}

export const OperatorFeedbackView: React.FC<OperatorFeedbackViewProps> = ({
  pilotId,
  lang,
}) => {
  const t = translations[lang].pilot

  const [selectedStatus, setSelectedStatus] = useState<OperatorFeedbackStatus | 'ALL'>('ALL')
  const [selectedCategory, setSelectedCategory] = useState<OperatorFeedbackCategory | 'ALL'>('ALL')
  const [searchTerm, setSearchTerm] = useState('')
  const [isSubmitDialogOpen, setIsSubmitDialogOpen] = useState(false)

  const {
    data: feedbackList = [],
    isLoading,
    refetch,
  } = useOperatorFeedbackQuery(
    pilotId,
    selectedStatus === 'ALL' ? undefined : selectedStatus,
    selectedCategory === 'ALL' ? undefined : selectedCategory
  )

  const submitMutation = useSubmitFeedbackMutation()

  const filtered = feedbackList.filter((f) => {
    if (!searchTerm) return true
    const term = searchTerm.toLowerCase()
    return (
      f.title.toLowerCase().includes(term) ||
      f.description.toLowerCase().includes(term) ||
      f.category.toLowerCase().includes(term) ||
      f.submittedBy.toLowerCase().includes(term)
    )
  })

  const totalFeedback = feedbackList.length
  const pendingFeedback = feedbackList.filter(
    (f) => f.status === 'SUBMITTED' || f.status === 'UNDER_REVIEW'
  ).length
  const actionRequiredFeedback = feedbackList.filter((f) => f.status === 'ACTION_REQUIRED').length
  const resolvedFeedback = feedbackList.filter((f) => f.status === 'RESOLVED').length

  const getStatusBadge = (status: OperatorFeedbackStatus) => {
    switch (status) {
      case 'RESOLVED':
        return (
          <Badge className="bg-emerald-100 text-emerald-800 dark:bg-emerald-950/50 dark:text-emerald-400 border-emerald-300">
            <CheckCircle2 className="h-3 w-3 mr-1" /> {status}
          </Badge>
        )
      case 'ACTION_REQUIRED':
        return (
          <Badge className="bg-rose-100 text-rose-800 dark:bg-rose-950/50 dark:text-rose-400 border-rose-300">
            <AlertTriangle className="h-3 w-3 mr-1" /> {status}
          </Badge>
        )
      case 'UNDER_REVIEW':
      case 'SUBMITTED':
        return (
          <Badge className="bg-amber-100 text-amber-800 dark:bg-amber-950/50 dark:text-amber-400 border-amber-300">
            <Clock className="h-3 w-3 mr-1" /> {status}
          </Badge>
        )
      case 'REJECTED':
      default:
        return (
          <Badge className="bg-slate-100 text-slate-800 dark:bg-slate-950/50 dark:text-slate-400 border-slate-300">
            <XCircle className="h-3 w-3 mr-1" /> {status}
          </Badge>
        )
    }
  }

  const getCategoryBadge = (cat: OperatorFeedbackCategory) => {
    return <Badge variant="outline" className="font-mono text-[10px]">{cat}</Badge>
  }

  return (
    <div className="space-y-6">
      {/* Counters */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <Card className="p-4 flex items-center justify-between">
          <div>
            <div className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">
              Total Observations
            </div>
            <div className="text-2xl font-bold mt-1">{totalFeedback}</div>
          </div>
          <MessageSquare className="h-7 w-7 text-muted-foreground opacity-60" />
        </Card>
        <Card className="p-4 flex items-center justify-between">
          <div>
            <div className="text-xs text-amber-600 dark:text-amber-400 uppercase tracking-wider font-semibold">
              Open / In Review
            </div>
            <div className="text-2xl font-bold mt-1 text-amber-600 dark:text-amber-400">
              {pendingFeedback}
            </div>
          </div>
          <Clock className="h-7 w-7 text-amber-500 opacity-60" />
        </Card>
        <Card className="p-4 flex items-center justify-between">
          <div>
            <div className="text-xs text-rose-600 dark:text-rose-400 uppercase tracking-wider font-semibold">
              Action Required
            </div>
            <div className="text-2xl font-bold mt-1 text-rose-600 dark:text-rose-400">
              {actionRequiredFeedback}
            </div>
          </div>
          <AlertTriangle className="h-7 w-7 text-rose-500 opacity-60" />
        </Card>
        <Card className="p-4 flex items-center justify-between">
          <div>
            <div className="text-xs text-emerald-600 dark:text-emerald-400 uppercase tracking-wider font-semibold">
              Resolved Issues
            </div>
            <div className="text-2xl font-bold mt-1 text-emerald-600 dark:text-emerald-400">
              {resolvedFeedback}
            </div>
          </div>
          <CheckCircle2 className="h-7 w-7 text-emerald-500 opacity-60" />
        </Card>
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between pb-3">
          <div>
            <CardTitle className="text-base font-bold flex items-center gap-2">
              <MessageSquare className="h-5 w-5 text-primary" />
              {t.operatorFeedback}
            </CardTitle>
            <CardDescription className="text-xs mt-1">
              Field observations, sensor calibrations, telemetry discrepancies, and operator field notes.
            </CardDescription>
          </div>
          <Button size="sm" onClick={() => setIsSubmitDialogOpen(true)}>
            <PlusCircle className="h-4 w-4 mr-1.5" />
            {t.newFeedback}
          </Button>
        </CardHeader>

        <CardContent className="space-y-4">
          {/* Search & Filters */}
          <div className="flex flex-col sm:flex-row gap-3">
            <div className="relative flex-1">
              <Search className="absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
              <input
                type="text"
                placeholder={t.searchPlaceholder}
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-9 pr-3 py-1.5 text-xs rounded-md border border-input bg-background focus:outline-none focus:ring-1 focus:ring-ring"
              />
            </div>
            <div className="flex items-center gap-2">
              <Filter className="h-4 w-4 text-muted-foreground" />
              <select
                value={selectedCategory}
                onChange={(e) =>
                  setSelectedCategory(e.target.value as OperatorFeedbackCategory | 'ALL')
                }
                className="h-8 text-xs rounded-md border border-input bg-background px-2"
              >
                <option value="ALL">All Categories</option>
                <option value="DATA_QUALITY">DATA_QUALITY</option>
                <option value="SENSOR_ISSUE">SENSOR_ISSUE</option>
                <option value="WEATHER_DISCREPANCY">WEATHER_DISCREPANCY</option>
                <option value="IRRIGATION_OPERATION">IRRIGATION_OPERATION</option>
                <option value="WATER_DELIVERY">WATER_DELIVERY</option>
                <option value="FIELD_CONDITION">FIELD_CONDITION</option>
                <option value="SYSTEM_USABILITY">SYSTEM_USABILITY</option>
                <option value="OTHER">OTHER</option>
              </select>
              <select
                value={selectedStatus}
                onChange={(e) =>
                  setSelectedStatus(e.target.value as OperatorFeedbackStatus | 'ALL')
                }
                className="h-8 text-xs rounded-md border border-input bg-background px-2"
              >
                <option value="ALL">{t.allStatuses}</option>
                <option value="SUBMITTED">SUBMITTED</option>
                <option value="UNDER_REVIEW">UNDER_REVIEW</option>
                <option value="ACTION_REQUIRED">ACTION_REQUIRED</option>
                <option value="RESOLVED">RESOLVED</option>
                <option value="REJECTED">REJECTED</option>
              </select>
            </div>
          </div>

          {/* List */}
          {isLoading ? (
            <div className="py-8 text-center text-xs text-muted-foreground">Loading feedback...</div>
          ) : filtered.length === 0 ? (
            <div className="py-10 border border-dashed rounded-lg text-center text-xs text-muted-foreground space-y-2">
              <FileText className="h-8 w-8 mx-auto opacity-40" />
              <p>No operator feedback entries found.</p>
            </div>
          ) : (
            <div className="space-y-3">
              {filtered.map((item) => (
                <div
                  key={item.id}
                  className="border rounded-lg p-4 bg-card hover:bg-muted/30 transition-colors space-y-2"
                >
                  <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                    <div className="flex items-center gap-2">
                      {getCategoryBadge(item.category)}
                      <h4 className="font-semibold text-sm">{item.title}</h4>
                    </div>
                    <div className="flex items-center gap-2">
                      {getStatusBadge(item.status)}
                      <span className="text-[11px] text-muted-foreground">
                        {item.createdAt ? new Date(item.createdAt).toLocaleDateString() : '—'}
                      </span>
                    </div>
                  </div>

                  <p className="text-xs text-muted-foreground leading-relaxed whitespace-pre-wrap">
                    {item.description}
                  </p>

                  <div className="flex flex-wrap items-center gap-4 pt-2 border-t text-[11px] text-muted-foreground">
                    <span>
                      Submitted by: <strong className="font-mono text-foreground">{item.submittedBy}</strong>
                    </span>
                    {item.farmId && (
                      <span>
                        Farm: <span className="font-mono">{item.farmId}</span>
                      </span>
                    )}
                    {item.fieldId && (
                      <span>
                        Field: <span className="font-mono">{item.fieldId}</span>
                      </span>
                    )}
                    {item.sensorId && (
                      <span>
                        Sensor: <span className="font-mono">{item.sensorId}</span>
                      </span>
                    )}
                    {item.resolutionNotes && (
                      <div className="w-full mt-1 p-2 bg-muted/50 rounded text-foreground font-medium">
                        Resolution: {item.resolutionNotes}
                      </div>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Submit Feedback Dialog */}
      <SubmitFeedbackDialog
        isOpen={isSubmitDialogOpen}
        onClose={() => setIsSubmitDialogOpen(false)}
        pilotId={pilotId}
        onSubmit={async (payload) => {
          await submitMutation.mutateAsync({ pilotId, payload })
          refetch()
        }}
        lang={lang}
      />
    </div>
  )
}
