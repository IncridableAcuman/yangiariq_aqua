import React from 'react'
import { IngestedProvenance, DataQualityStatus, EvidenceClassification } from '@/entities/integration'
import { Badge } from '@/shared/ui/badge'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/ui/table'
import { translations, SupportedLanguage } from '@/shared/i18n'

interface ProvenanceHistoryTableProps {
  records: IngestedProvenance[]
  isLoading?: boolean
  lang: SupportedLanguage
}

export const ProvenanceHistoryTable: React.FC<ProvenanceHistoryTableProps> = ({
  records,
  isLoading,
  lang,
}) => {
  const t = translations[lang].dataIntegration

  const renderQualityBadge = (quality: DataQualityStatus) => {
    switch (quality) {
      case 'VALID':
        return (
          <Badge variant="outline" className="bg-emerald-50 text-emerald-700 border-emerald-300 dark:bg-emerald-950/40 dark:text-emerald-300 text-[10px]">
            VALID
          </Badge>
        )
      case 'DUPLICATE':
        return (
          <Badge variant="outline" className="bg-amber-50 text-amber-700 border-amber-300 dark:bg-amber-950/40 dark:text-amber-300 text-[10px]">
            DUPLICATE
          </Badge>
        )
      case 'OUT_OF_RANGE':
        return (
          <Badge variant="outline" className="bg-rose-50 text-rose-700 border-rose-300 dark:bg-rose-950/40 dark:text-rose-300 text-[10px]">
            OUT_OF_RANGE
          </Badge>
        )
      case 'STALE':
        return (
          <Badge variant="outline" className="bg-blue-50 text-blue-700 border-blue-300 dark:bg-blue-950/40 dark:text-blue-300 text-[10px]">
            STALE
          </Badge>
        )
      case 'UNVERIFIED':
        return (
          <Badge variant="outline" className="bg-purple-50 text-purple-700 border-purple-300 dark:bg-purple-950/40 dark:text-purple-300 text-[10px]">
            UNVERIFIED
          </Badge>
        )
      case 'INVALID':
      case 'INCOMPLETE':
      default:
        return (
          <Badge variant="destructive" className="text-[10px]">
            {quality}
          </Badge>
        )
    }
  }

  const renderEvidenceBadge = (evidence: EvidenceClassification) => {
    switch (evidence) {
      case 'VERIFIED':
        return (
          <Badge variant="outline" className="bg-emerald-100/60 text-emerald-800 border-emerald-400 font-bold dark:bg-emerald-900/50 dark:text-emerald-200 text-[10px]">
            VERIFIED
          </Badge>
        )
      case 'OBSERVED':
        return (
          <Badge variant="outline" className="bg-sky-50 text-sky-700 border-sky-300 dark:bg-sky-950/40 dark:text-sky-300 text-[10px]">
            OBSERVED
          </Badge>
        )
      case 'SIMULATED':
        return (
          <Badge variant="outline" className="bg-purple-50 text-purple-700 border-purple-300 dark:bg-purple-950/40 dark:text-purple-300 text-[10px]">
            SIMULATED
          </Badge>
        )
      case 'CALCULATED':
      case 'ESTIMATED':
        return (
          <Badge variant="outline" className="bg-muted text-muted-foreground border-border text-[10px]">
            {evidence}
          </Badge>
        )
      case 'MISSING':
      default:
        return (
          <Badge variant="outline" className="bg-rose-50 text-rose-600 border-rose-300 text-[10px]">
            MISSING
          </Badge>
        )
    }
  }

  const formatTimestamp = (ts: string) => {
    try {
      const date = new Date(ts)
      return date.toLocaleTimeString(lang === 'uz' ? 'uz-UZ' : lang === 'ru' ? 'ru-RU' : 'en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      })
    } catch {
      return ts
    }
  }

  return (
    <div className="rounded-lg border border-border/70 overflow-hidden bg-card/60 backdrop-blur-sm">
      <Table>
        <TableHeader className="bg-muted/40">
          <TableRow>
            <TableHead className="w-[140px]">{t.measuredTime}</TableHead>
            <TableHead>{t.recordId}</TableHead>
            <TableHead>{t.parameter}</TableHead>
            <TableHead className="text-right">{t.rawReading}</TableHead>
            <TableHead className="text-right">{t.canonicalValue}</TableHead>
            <TableHead>{t.dataQuality}</TableHead>
            <TableHead>{t.evidenceClass}</TableHead>
            <TableHead className="max-w-[200px]">{t.validationRemarks}</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {isLoading ? (
            <TableRow>
              <TableCell colSpan={8} className="text-center py-8 text-muted-foreground">
                {t.loadingProvenance}
              </TableCell>
            </TableRow>
          ) : records.length === 0 ? (
            <TableRow>
              <TableCell colSpan={8} className="text-center py-8 text-muted-foreground">
                {t.noProvenanceFound}
              </TableCell>
            </TableRow>
          ) : (
            records.map((r) => (
              <TableRow key={r.id} className="hover:bg-muted/30 transition-colors text-xs">
                <TableCell className="font-mono text-[11px] text-muted-foreground">
                  {formatTimestamp(r.measuredAt)}
                </TableCell>
                <TableCell className="font-mono text-[11px] text-foreground truncate max-w-[120px]">
                  {r.externalRecordId || r.idempotencyKey.substring(0, 16) + '...'}
                </TableCell>
                <TableCell className="font-medium text-foreground">
                  {r.sensorType}
                </TableCell>
                <TableCell className="text-right font-mono text-muted-foreground">
                  {r.rawValue} {r.rawUnit}
                </TableCell>
                <TableCell className="text-right font-mono font-semibold text-foreground">
                  {r.normalizedValue} {r.canonicalUnit}
                </TableCell>
                <TableCell>{renderQualityBadge(r.qualityStatus)}</TableCell>
                <TableCell>{renderEvidenceBadge(r.evidenceClassification)}</TableCell>
                <TableCell className="text-[11px] text-muted-foreground truncate max-w-[200px]" title={r.validationMessage}>
                  {r.validationMessage || '-'}
                </TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </div>
  )
}
