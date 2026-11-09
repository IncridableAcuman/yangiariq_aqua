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
  TrendingUp,
  PlusCircle,
  Search,
  Filter,
  CheckCircle2,
  AlertCircle,
  HelpCircle,
  BarChart3,
  Percent,
} from 'lucide-react'
import { translations, SupportedLang } from '@/shared/i18n'
import {
  PredictionEvaluationStatus,
} from '@/entities/pilot'
import {
  usePredictionValidationsQuery,
  useEvaluatePredictionMutation,
} from '../queries/usePilotQueries'
import { EvaluatePredictionDialog } from './EvaluatePredictionDialog'

interface PredictionValidationViewProps {
  pilotId: string
  lang: SupportedLang
}

export const PredictionValidationView: React.FC<PredictionValidationViewProps> = ({
  pilotId,
  lang,
}) => {
  const t = translations[lang].pilot

  const [selectedStatus, setSelectedStatus] = useState<PredictionEvaluationStatus | 'ALL'>('ALL')
  const [searchTerm, setSearchTerm] = useState('')
  const [isEvaluateDialogOpen, setIsEvaluateDialogOpen] = useState(false)

  const {
    data: validations = [],
    isLoading,
    refetch,
  } = usePredictionValidationsQuery(
    pilotId,
    selectedStatus === 'ALL' ? undefined : selectedStatus
  )

  const evaluateMutation = useEvaluatePredictionMutation()

  const filtered = validations.filter((v) => {
    if (!searchTerm) return true
    const term = searchTerm.toLowerCase()
    return (
      v.fieldId.toLowerCase().includes(term) ||
      v.modelName.toLowerCase().includes(term) ||
      v.modelVersion.toLowerCase().includes(term)
    )
  })

  const totalValidations = validations.length
  const validatedRecords = validations.filter((v) => v.evaluationStatus === 'VALIDATED')
  const avgAbsoluteError =
    validatedRecords.length > 0
      ? validatedRecords.reduce((acc, curr) => acc + curr.absoluteError, 0) /
        validatedRecords.length
      : 0

  const avgRelativeError =
    validatedRecords.filter((v) => v.relativeErrorPercentage != null).length > 0
      ? validatedRecords.reduce((acc, curr) => acc + (curr.relativeErrorPercentage ?? 0), 0) /
        validatedRecords.filter((v) => v.relativeErrorPercentage != null).length
      : 0

  const getStatusBadge = (status: PredictionEvaluationStatus) => {
    switch (status) {
      case 'VALIDATED':
        return (
          <Badge className="bg-emerald-100 text-emerald-800 dark:bg-emerald-950/50 dark:text-emerald-400 border-emerald-300">
            <CheckCircle2 className="h-3 w-3 mr-1" /> VALIDATED
          </Badge>
        )
      case 'VALIDATION_PENDING':
        return (
          <Badge className="bg-amber-100 text-amber-800 dark:bg-amber-950/50 dark:text-amber-400 border-amber-300">
            <TrendingUp className="h-3 w-3 mr-1" /> PENDING
          </Badge>
        )
      case 'INSUFFICIENT_DATA':
        return (
          <Badge className="bg-blue-100 text-blue-800 dark:bg-blue-950/50 dark:text-blue-400 border-blue-300">
            <HelpCircle className="h-3 w-3 mr-1" /> INSUFFICIENT_DATA
          </Badge>
        )
      case 'INVALID_COMPARISON':
      default:
        return (
          <Badge className="bg-rose-100 text-rose-800 dark:bg-rose-950/50 dark:text-rose-400 border-rose-300">
            <AlertCircle className="h-3 w-3 mr-1" /> INVALID
          </Badge>
        )
    }
  }

  return (
    <div className="space-y-6">
      {/* KPI Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <Card className="p-4 flex items-center justify-between">
          <div>
            <div className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">
              Evaluations Count
            </div>
            <div className="text-2xl font-bold mt-1">{totalValidations}</div>
          </div>
          <BarChart3 className="h-7 w-7 text-muted-foreground opacity-60" />
        </Card>
        <Card className="p-4 flex items-center justify-between">
          <div>
            <div className="text-xs text-emerald-600 dark:text-emerald-400 uppercase tracking-wider font-semibold">
              Validated Comparisons
            </div>
            <div className="text-2xl font-bold mt-1 text-emerald-600 dark:text-emerald-400">
              {validatedRecords.length}
            </div>
          </div>
          <CheckCircle2 className="h-7 w-7 text-emerald-500 opacity-60" />
        </Card>
        <Card className="p-4 flex items-center justify-between">
          <div>
            <div className="text-xs text-blue-600 dark:text-blue-400 uppercase tracking-wider font-semibold">
              Mean Absolute Error (MAE)
            </div>
            <div className="text-2xl font-bold mt-1 text-blue-600 dark:text-blue-400">
              {avgAbsoluteError ? avgAbsoluteError.toFixed(2) : '—'}
            </div>
          </div>
          <TrendingUp className="h-7 w-7 text-blue-500 opacity-60" />
        </Card>
        <Card className="p-4 flex items-center justify-between">
          <div>
            <div className="text-xs text-purple-600 dark:text-purple-400 uppercase tracking-wider font-semibold">
              Mean Relative Error
            </div>
            <div className="text-2xl font-bold mt-1 text-purple-600 dark:text-purple-400">
              {avgRelativeError ? `${avgRelativeError.toFixed(1)}%` : '—'}
            </div>
          </div>
          <Percent className="h-7 w-7 text-purple-500 opacity-60" />
        </Card>
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between pb-3">
          <div>
            <CardTitle className="text-base font-bold flex items-center gap-2">
              <TrendingUp className="h-5 w-5 text-primary" />
              {t.predictionValidation}
            </CardTitle>
            <CardDescription className="text-xs mt-1">
              Rigorous comparison between hydraulic surrogate model forecasts and independently observed ground-truth.
            </CardDescription>
          </div>
          <Button size="sm" onClick={() => setIsEvaluateDialogOpen(true)}>
            <PlusCircle className="h-4 w-4 mr-1.5" />
            {t.evaluatePrediction}
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
                value={selectedStatus}
                onChange={(e) =>
                  setSelectedStatus(e.target.value as PredictionEvaluationStatus | 'ALL')
                }
                className="h-8 text-xs rounded-md border border-input bg-background px-2"
              >
                <option value="ALL">{t.allStatuses}</option>
                <option value="VALIDATED">VALIDATED</option>
                <option value="VALIDATION_PENDING">VALIDATION_PENDING</option>
                <option value="INSUFFICIENT_DATA">INSUFFICIENT_DATA</option>
                <option value="INVALID_COMPARISON">INVALID_COMPARISON</option>
              </select>
            </div>
          </div>

          {/* Table */}
          {isLoading ? (
            <div className="py-8 text-center text-xs text-muted-foreground">Loading validations...</div>
          ) : filtered.length === 0 ? (
            <div className="py-10 border border-dashed rounded-lg text-center text-xs text-muted-foreground space-y-2">
              <TrendingUp className="h-8 w-8 mx-auto opacity-40" />
              <p>No prediction validation records found.</p>
            </div>
          ) : (
            <div className="border rounded-md overflow-x-auto">
              <table className="w-full text-xs">
                <thead className="bg-muted text-muted-foreground border-b text-left">
                  <tr>
                    <th className="py-2.5 px-3 font-semibold">{t.fieldId}</th>
                    <th className="py-2.5 px-3 font-semibold">{t.modelNameVersion}</th>
                    <th className="py-2.5 px-3 font-semibold">{t.horizonHours}</th>
                    <th className="py-2.5 px-3 font-semibold">{t.predicted}</th>
                    <th className="py-2.5 px-3 font-semibold">{t.actual}</th>
                    <th className="py-2.5 px-3 font-semibold">{t.absoluteError}</th>
                    <th className="py-2.5 px-3 font-semibold">{t.relativeError}</th>
                    <th className="py-2.5 px-3 font-semibold">{t.validationStatus}</th>
                    <th className="py-2.5 px-3 font-semibold">Evaluated At</th>
                  </tr>
                </thead>
                <tbody className="divide-y">
                  {filtered.map((r) => (
                    <tr key={r.id} className="hover:bg-muted/40 transition-colors">
                      <td className="py-2.5 px-3 font-mono text-xs">{r.fieldId}</td>
                      <td className="py-2.5 px-3">
                        <span className="font-medium">{r.modelName}</span>{' '}
                        <span className="text-muted-foreground font-mono text-[10px]">
                          ({r.modelVersion})
                        </span>
                      </td>
                      <td className="py-2.5 px-3">{r.predictionHorizonHours}h</td>
                      <td className="py-2.5 px-3 font-semibold text-blue-600 dark:text-blue-400">
                        {r.predictedValue}
                      </td>
                      <td className="py-2.5 px-3 font-semibold text-emerald-600 dark:text-emerald-400">
                        {r.actualValue}
                      </td>
                      <td className="py-2.5 px-3 font-mono">
                        {r.absoluteError.toFixed(2)}
                      </td>
                      <td className="py-2.5 px-3 font-mono text-muted-foreground">
                        {r.relativeErrorPercentage != null
                          ? `${r.relativeErrorPercentage.toFixed(1)}%`
                          : '—'}
                      </td>
                      <td className="py-2.5 px-3">{getStatusBadge(r.evaluationStatus)}</td>
                      <td className="py-2.5 px-3 text-muted-foreground">
                        {r.evaluatedAt ? new Date(r.evaluatedAt).toLocaleDateString() : '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Evaluate Prediction Dialog */}
      <EvaluatePredictionDialog
        isOpen={isEvaluateDialogOpen}
        onClose={() => setIsEvaluateDialogOpen(false)}
        pilotId={pilotId}
        onSubmit={async (payload) => {
          await evaluateMutation.mutateAsync({ pilotId, payload })
          refetch()
        }}
        lang={lang}
      />
    </div>
  )
}
