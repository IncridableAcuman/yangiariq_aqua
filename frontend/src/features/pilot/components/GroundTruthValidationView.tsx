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
  CheckCircle2,
  Clock,
  AlertTriangle,
  XCircle,
  PlusCircle,
  Search,
  Filter,
  CheckCheck,
  Scale,
} from 'lucide-react'
import { translations, SupportedLang } from '@/shared/i18n'
import { GroundTruthMeasurement, GroundTruthStatus } from '@/entities/pilot'
import {
  useGroundTruthMeasurementsQuery,
  useSubmitGroundTruthMutation,
  useReviewGroundTruthMutation,
} from '../queries/usePilotQueries'
import { SubmitGroundTruthDialog } from './SubmitGroundTruthDialog'
import { ReviewGroundTruthDialog } from './ReviewGroundTruthDialog'

interface GroundTruthValidationViewProps {
  pilotId: string
  lang: SupportedLang
}

export const GroundTruthValidationView: React.FC<GroundTruthValidationViewProps> = ({
  pilotId,
  lang,
}) => {
  const t = translations[lang].pilot

  const [selectedStatus, setSelectedStatus] = useState<GroundTruthStatus | 'ALL'>('ALL')
  const [searchTerm, setSearchTerm] = useState('')
  const [isSubmitDialogOpen, setIsSubmitDialogOpen] = useState(false)
  const [reviewMeasurement, setReviewMeasurement] = useState<GroundTruthMeasurement | null>(null)

  const {
    data: measurements = [],
    isLoading,
    refetch,
  } = useGroundTruthMeasurementsQuery(
    pilotId,
    selectedStatus === 'ALL' ? undefined : selectedStatus
  )

  const submitMutation = useSubmitGroundTruthMutation()
  const reviewMutation = useReviewGroundTruthMutation()

  const filtered = measurements.filter((m) => {
    if (!searchTerm) return true
    const term = searchTerm.toLowerCase()
    return (
      m.fieldId.toLowerCase().includes(term) ||
      m.measurementType.toLowerCase().includes(term) ||
      m.submittedBy.toLowerCase().includes(term) ||
      (m.notes && m.notes.toLowerCase().includes(term))
    )
  })

  const countTotal = measurements.length
  const countPending = measurements.filter(
    (m) => m.verificationStatus === 'SUBMITTED' || m.verificationStatus === 'PENDING_REVIEW'
  ).length
  const countVerified = measurements.filter((m) => m.verificationStatus === 'VERIFIED').length
  const countRejected = measurements.filter(
    (m) => m.verificationStatus === 'REJECTED' || m.verificationStatus === 'NEEDS_REMEASUREMENT'
  ).length

  const getStatusBadge = (status: GroundTruthStatus) => {
    switch (status) {
      case 'VERIFIED':
        return (
          <Badge className="bg-emerald-100 text-emerald-800 dark:bg-emerald-950/50 dark:text-emerald-400 border-emerald-300">
            <CheckCircle2 className="h-3 w-3 mr-1" /> {status}
          </Badge>
        )
      case 'PENDING_REVIEW':
      case 'SUBMITTED':
        return (
          <Badge className="bg-amber-100 text-amber-800 dark:bg-amber-950/50 dark:text-amber-400 border-amber-300">
            <Clock className="h-3 w-3 mr-1" /> {status}
          </Badge>
        )
      case 'NEEDS_REMEASUREMENT':
        return (
          <Badge className="bg-blue-100 text-blue-800 dark:bg-blue-950/50 dark:text-blue-400 border-blue-300">
            <AlertTriangle className="h-3 w-3 mr-1" /> {status}
          </Badge>
        )
      case 'REJECTED':
      default:
        return (
          <Badge className="bg-rose-100 text-rose-800 dark:bg-rose-950/50 dark:text-rose-400 border-rose-300">
            <XCircle className="h-3 w-3 mr-1" /> {status}
          </Badge>
        )
    }
  }

  return (
    <div className="space-y-6">
      {/* Top summary counters */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <Card className="p-4 flex items-center justify-between">
          <div>
            <div className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">
              Total Ingested
            </div>
            <div className="text-2xl font-bold mt-1">{countTotal}</div>
          </div>
          <Scale className="h-7 w-7 text-muted-foreground opacity-60" />
        </Card>
        <Card className="p-4 flex items-center justify-between">
          <div>
            <div className="text-xs text-amber-600 dark:text-amber-400 uppercase tracking-wider font-semibold">
              Pending Review
            </div>
            <div className="text-2xl font-bold mt-1 text-amber-600 dark:text-amber-400">
              {countPending}
            </div>
          </div>
          <Clock className="h-7 w-7 text-amber-500 opacity-60" />
        </Card>
        <Card className="p-4 flex items-center justify-between">
          <div>
            <div className="text-xs text-emerald-600 dark:text-emerald-400 uppercase tracking-wider font-semibold">
              Verified
            </div>
            <div className="text-2xl font-bold mt-1 text-emerald-600 dark:text-emerald-400">
              {countVerified}
            </div>
          </div>
          <CheckCircle2 className="h-7 w-7 text-emerald-500 opacity-60" />
        </Card>
        <Card className="p-4 flex items-center justify-between">
          <div>
            <div className="text-xs text-rose-600 dark:text-rose-400 uppercase tracking-wider font-semibold">
              Rejected / Re-measure
            </div>
            <div className="text-2xl font-bold mt-1 text-rose-600 dark:text-rose-400">
              {countRejected}
            </div>
          </div>
          <XCircle className="h-7 w-7 text-rose-500 opacity-60" />
        </Card>
      </div>

      <Card>
        <CardHeader className="flex flex-row items-center justify-between pb-3">
          <div>
            <CardTitle className="text-base font-bold flex items-center gap-2">
              <CheckCheck className="h-5 w-5 text-primary" />
              {t.groundTruthValidation}
            </CardTitle>
            <CardDescription className="text-xs mt-1">
              Independent ground-truth measurements verified under the strict Four-Eyes Principle.
            </CardDescription>
          </div>
          <Button size="sm" onClick={() => setIsSubmitDialogOpen(true)}>
            <PlusCircle className="h-4 w-4 mr-1.5" />
            {t.newMeasurement}
          </Button>
        </CardHeader>

        <CardContent className="space-y-4">
          {/* Filter and search bar */}
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
                onChange={(e) => setSelectedStatus(e.target.value as GroundTruthStatus | 'ALL')}
                className="h-8 text-xs rounded-md border border-input bg-background px-2"
              >
                <option value="ALL">{t.allStatuses}</option>
                <option value="SUBMITTED">SUBMITTED</option>
                <option value="PENDING_REVIEW">PENDING_REVIEW</option>
                <option value="VERIFIED">VERIFIED</option>
                <option value="NEEDS_REMEASUREMENT">NEEDS_REMEASUREMENT</option>
                <option value="REJECTED">REJECTED</option>
              </select>
            </div>
          </div>

          {/* Measurements table */}
          {isLoading ? (
            <div className="py-8 text-center text-xs text-muted-foreground">Loading observations...</div>
          ) : filtered.length === 0 ? (
            <div className="py-10 border border-dashed rounded-lg text-center text-xs text-muted-foreground space-y-2">
              <Scale className="h-8 w-8 mx-auto opacity-40" />
              <p>No ground-truth measurements match current criteria.</p>
            </div>
          ) : (
            <div className="border rounded-md overflow-x-auto">
              <table className="w-full text-xs">
                <thead className="bg-muted text-muted-foreground border-b text-left">
                  <tr>
                    <th className="py-2.5 px-3 font-semibold">{t.fieldId}</th>
                    <th className="py-2.5 px-3 font-semibold">{t.measurementType}</th>
                    <th className="py-2.5 px-3 font-semibold">{t.recordedValue}</th>
                    <th className="py-2.5 px-3 font-semibold">{t.collectionMethod}</th>
                    <th className="py-2.5 px-3 font-semibold">{t.measuredAt}</th>
                    <th className="py-2.5 px-3 font-semibold">{t.submittedBy}</th>
                    <th className="py-2.5 px-3 font-semibold">{t.status}</th>
                    <th className="py-2.5 px-3 font-semibold">{t.verifier}</th>
                    <th className="py-2.5 px-3 font-semibold text-right">{t.actions}</th>
                  </tr>
                </thead>
                <tbody className="divide-y">
                  {filtered.map((m) => {
                    const isPending =
                      m.verificationStatus === 'SUBMITTED' ||
                      m.verificationStatus === 'PENDING_REVIEW'
                    return (
                      <tr key={m.id} className="hover:bg-muted/40 transition-colors">
                        <td className="py-2.5 px-3 font-mono text-xs">{m.fieldId}</td>
                        <td className="py-2.5 px-3 font-medium">{m.measurementType}</td>
                        <td className="py-2.5 px-3 font-semibold">
                          {m.measuredValue} <span className="font-mono text-muted-foreground text-[10px]">{m.canonicalUnit}</span>
                        </td>
                        <td className="py-2.5 px-3 text-muted-foreground">
                          {m.collectionMethod} ({m.measurementSource})
                        </td>
                        <td className="py-2.5 px-3 text-muted-foreground">
                          {m.measuredAt ? new Date(m.measuredAt).toLocaleString() : '—'}
                        </td>
                        <td className="py-2.5 px-3 font-mono text-[11px] text-muted-foreground">
                          {m.submittedBy}
                        </td>
                        <td className="py-2.5 px-3">{getStatusBadge(m.verificationStatus)}</td>
                        <td className="py-2.5 px-3 font-mono text-[11px] text-muted-foreground">
                          {m.verifierId || '—'}
                        </td>
                        <td className="py-2.5 px-3 text-right">
                          {isPending ? (
                            <Button
                              size="sm"
                              variant="outline"
                              className="h-7 text-xs font-semibold"
                              onClick={() => setReviewMeasurement(m)}
                            >
                              <CheckCircle2 className="h-3 w-3 mr-1 text-primary" />
                              {t.reviewAction}
                            </Button>
                          ) : (
                            <Button
                              size="sm"
                              variant="ghost"
                              className="h-7 text-xs text-muted-foreground"
                              onClick={() => setReviewMeasurement(m)}
                            >
                              {t.viewDetails}
                            </Button>
                          )}
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Submit Ground Truth Dialog */}
      <SubmitGroundTruthDialog
        isOpen={isSubmitDialogOpen}
        onClose={() => setIsSubmitDialogOpen(false)}
        onSubmit={async (payload) => {
          await submitMutation.mutateAsync({ pilotId, payload })
          refetch()
        }}
        lang={lang}
      />

      {/* Review Ground Truth Dialog */}
      <ReviewGroundTruthDialog
        isOpen={Boolean(reviewMeasurement)}
        onClose={() => setReviewMeasurement(null)}
        measurement={reviewMeasurement}
        onReview={async (payload) => {
          if (!reviewMeasurement) return
          await reviewMutation.mutateAsync({
            pilotId,
            measurementId: reviewMeasurement.id,
            payload,
          })
          setReviewMeasurement(null)
          refetch()
        }}
        lang={lang}
      />
    </div>
  )
}
