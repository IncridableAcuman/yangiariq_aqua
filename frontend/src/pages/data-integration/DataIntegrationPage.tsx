import React, { useState } from 'react'
import {
  useDataSourcesQuery,
  useDataQualitySummaryQuery,
  useIngestionHistoryQuery,
  useImportBatchesQuery,
  useRegisterDataSourceMutation,
  useUpdateDataSourceMutation,
  useToggleDataSourceMutation,
  useSyncDataSourceMutation,
  useRecordManualMeasurementMutation,
  useUploadCsvMutation,
} from '@/features/data-integration/queries/useDataIntegrationQueries'
import { DataSourcesSummaryCards } from '@/features/data-integration/components/DataSourcesSummaryCards'
import { DataSourcesTable } from '@/features/data-integration/components/DataSourcesTable'
import { DataSourceDetailModal } from '@/features/data-integration/components/DataSourceDetailModal'
import { RegisterDataSourceDialog } from '@/features/data-integration/components/RegisterDataSourceDialog'
import { CsvImportDialog } from '@/features/data-integration/components/CsvImportDialog'
import { ManualMeasurementDialog } from '@/features/data-integration/components/ManualMeasurementDialog'
import { DataQualityMetricsView } from '@/features/data-integration/components/DataQualityMetricsView'
import { ProvenanceHistoryTable } from '@/features/data-integration/components/ProvenanceHistoryTable'
import { DataSource, CreateDataSourceRequest, UpdateDataSourceRequest, RecordManualMeasurementRequest } from '@/entities/integration'
import { useAuth } from '@/features/auth/context/AuthContext'
import { Button } from '@/shared/ui/button'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/ui/table'
import { Badge } from '@/shared/ui/badge'
import {
  Database,
  PlusCircle,
  FileSpreadsheet,
  UserCheck,
  RefreshCw,
  ShieldCheck,
  FileUp,
} from 'lucide-react'
import { translations, SupportedLanguage } from '@/shared/i18n'

interface DataIntegrationPageProps {
  lang: SupportedLanguage
}

export const DataIntegrationPage: React.FC<DataIntegrationPageProps> = ({ lang }) => {
  const t = translations[lang].dataIntegration
  const { user } = useAuth()
  const canManage = user?.role === 'OPERATOR' || user?.role === 'ADMIN'

  const [activeTab, setActiveTab] = useState<'sources' | 'quality' | 'imports'>('sources')
  const [selectedSource, setSelectedSource] = useState<DataSource | null>(null)
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false)
  const [isRegisterOpen, setIsRegisterOpen] = useState(false)
  const [isCsvImportOpen, setIsCsvImportOpen] = useState(false)
  const [isManualEntryOpen, setIsManualEntryOpen] = useState(false)

  // React Queries
  const { data: sources = [], refetch: refetchSources } = useDataSourcesQuery()
  const { data: qualitySummary, isLoading: isLoadingQuality } = useDataQualitySummaryQuery(30)
  const { data: provenanceHistory = [], isLoading: isLoadingHistory, refetch: refetchHistory } = useIngestionHistoryQuery({ limit: 50 })
  const { data: importBatches = [], isLoading: isLoadingBatches } = useImportBatchesQuery()

  // Mutations
  const registerMutation = useRegisterDataSourceMutation()
  const updateMutation = useUpdateDataSourceMutation()
  const toggleMutation = useToggleDataSourceMutation()
  const syncMutation = useSyncDataSourceMutation()
  const manualMutation = useRecordManualMeasurementMutation()
  const csvMutation = useUploadCsvMutation()

  const handleSelectSource = (src: DataSource) => {
    setSelectedSource(src)
    setIsDetailModalOpen(true)
  }

  const handleToggleSource = async (id: string) => {
    await toggleMutation.mutateAsync(id)
  }

  const handleSyncSource = async (id: string) => {
    await syncMutation.mutateAsync(id)
  }

  const handleRegisterSource = async (data: CreateDataSourceRequest) => {
    await registerMutation.mutateAsync(data)
  }

  const handleUpdateSource = async (id: string, data: UpdateDataSourceRequest) => {
    await updateMutation.mutateAsync({ id, data })
  }

  const handleManualMeasurement = async (data: RecordManualMeasurementRequest) => {
    await manualMutation.mutateAsync(data)
  }

  const handleUploadCsv = async (file: File, dryRun: boolean) => {
    return await csvMutation.mutateAsync({ file, dryRun })
  }

  return (
    <div className="container mx-auto p-4 md:p-6 space-y-6">
      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl md:text-3xl font-bold tracking-tight text-foreground flex items-center gap-2">
            <Database className="h-7 w-7 text-primary" />
            {t.pageTitle}
          </h1>
          <p className="text-sm text-muted-foreground mt-1">
            {t.pageSubtitle}
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => setIsManualEntryOpen(true)}
            className="flex items-center gap-1.5"
          >
            <UserCheck className="h-4 w-4 text-primary" />
            {t.manualMeasurement}
          </Button>

          {canManage && (
            <>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setIsCsvImportOpen(true)}
                className="flex items-center gap-1.5"
              >
                <FileSpreadsheet className="h-4 w-4 text-emerald-500" />
                {t.importCsv}
              </Button>

              <Button
                size="sm"
                onClick={() => setIsRegisterOpen(true)}
                className="flex items-center gap-1.5"
              >
                <PlusCircle className="h-4 w-4" />
                {t.registerSource}
              </Button>
            </>
          )}

          <Button
            variant="ghost"
            size="sm"
            onClick={() => {
              refetchSources()
              refetchHistory()
            }}
            title={t.refresh}
          >
            <RefreshCw className="h-4 w-4 text-muted-foreground" />
          </Button>
        </div>
      </div>

      {/* KPI Cards */}
      <DataSourcesSummaryCards sources={sources} lang={lang} />

      {/* Tabs Bar */}
      <div className="flex border-b border-border/80 gap-2">
        <button
          onClick={() => setActiveTab('sources')}
          className={`pb-2.5 px-3 text-sm font-semibold border-b-2 transition-colors flex items-center gap-2 ${
            activeTab === 'sources'
              ? 'border-primary text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          <Database className="h-4 w-4" />
          {t.tabSources}
        </button>

        <button
          onClick={() => setActiveTab('quality')}
          className={`pb-2.5 px-3 text-sm font-semibold border-b-2 transition-colors flex items-center gap-2 ${
            activeTab === 'quality'
              ? 'border-primary text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          <ShieldCheck className="h-4 w-4" />
          {t.tabQualityProvenance}
        </button>

        <button
          onClick={() => setActiveTab('imports')}
          className={`pb-2.5 px-3 text-sm font-semibold border-b-2 transition-colors flex items-center gap-2 ${
            activeTab === 'imports'
              ? 'border-primary text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          <FileUp className="h-4 w-4" />
          {t.tabCsvBatches}
        </button>
      </div>

      {/* Tab 1: Data Sources Registry Table */}
      {activeTab === 'sources' && (
        <div className="space-y-4">
          <DataSourcesTable
            sources={sources}
            lang={lang}
            onSelectSource={handleSelectSource}
            onToggleStatus={handleToggleSource}
            onSyncSource={handleSyncSource}
            isToggling={toggleMutation.isPending}
            isSyncing={syncMutation.isPending}
            canManage={canManage}
          />
        </div>
      )}

      {/* Tab 2: Quality & Provenance Monitoring */}
      {activeTab === 'quality' && (
        <div className="space-y-6">
          <DataQualityMetricsView summary={qualitySummary} isLoading={isLoadingQuality} lang={lang} />

          <div>
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-base font-bold text-foreground">{t.recentProvenanceTrace}</h3>
              <span className="text-xs text-muted-foreground">{t.last50Observations}</span>
            </div>
            <ProvenanceHistoryTable records={provenanceHistory} isLoading={isLoadingHistory} lang={lang} />
          </div>
        </div>
      )}

      {/* Tab 3: CSV Batch Imports History */}
      {activeTab === 'imports' && (
        <div className="space-y-4">
          <div className="rounded-lg border border-border/70 overflow-hidden bg-card/60 backdrop-blur-sm">
            <Table>
              <TableHeader className="bg-muted/40">
                <TableRow>
                  <TableHead>{t.fileName}</TableHead>
                  <TableHead className="text-right">{t.fileSize}</TableHead>
                  <TableHead className="text-right">{t.totalRows}</TableHead>
                  <TableHead className="text-right">{t.successful}</TableHead>
                  <TableHead className="text-right">{t.rejected}</TableHead>
                  <TableHead>{t.status}</TableHead>
                  <TableHead>{t.startedAt}</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {isLoadingBatches ? (
                  <TableRow>
                    <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">
                      {t.loadingBatches}
                    </TableCell>
                  </TableRow>
                ) : importBatches.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={7} className="text-center py-8 text-muted-foreground">
                      {t.noBatchesFound}
                    </TableCell>
                  </TableRow>
                ) : (
                  importBatches.map((b) => (
                    <TableRow key={b.batchId} className="hover:bg-muted/30 transition-colors text-xs">
                      <TableCell className="font-medium text-foreground">{b.fileName}</TableCell>
                      <TableCell className="text-right font-mono text-muted-foreground">
                        {(b.fileSizeBytes / 1024).toFixed(1)} KB
                      </TableCell>
                      <TableCell className="text-right font-mono">{b.totalRows}</TableCell>
                      <TableCell className="text-right font-mono text-emerald-600 font-semibold">
                        {b.successfulRows}
                      </TableCell>
                      <TableCell className="text-right font-mono text-rose-600 font-semibold">
                        {b.rejectedRows}
                      </TableCell>
                      <TableCell>
                        <Badge
                          variant="outline"
                          className={
                            b.status === 'COMPLETED'
                              ? 'bg-emerald-50 text-emerald-700 border-emerald-300'
                              : b.status === 'PARTIAL'
                              ? 'bg-amber-50 text-amber-700 border-amber-300'
                              : 'bg-rose-50 text-rose-700 border-rose-300'
                          }
                        >
                          {b.status}
                        </Badge>
                      </TableCell>
                      <TableCell className="text-xs text-muted-foreground font-mono">
                        {b.startedAt ? new Date(b.startedAt).toLocaleString() : '-'}
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </div>
        </div>
      )}

      {/* Modals & Dialogs */}
      <DataSourceDetailModal
        source={selectedSource}
        isOpen={isDetailModalOpen}
        onClose={() => setIsDetailModalOpen(false)}
        onUpdate={handleUpdateSource}
        isUpdating={updateMutation.isPending}
        canManage={canManage}
        lang={lang}
      />

      <RegisterDataSourceDialog
        isOpen={isRegisterOpen}
        onClose={() => setIsRegisterOpen(false)}
        onRegister={handleRegisterSource}
        isRegistering={registerMutation.isPending}
        lang={lang}
      />

      <CsvImportDialog
        isOpen={isCsvImportOpen}
        onClose={() => setIsCsvImportOpen(false)}
        onUpload={handleUploadCsv}
        isUploading={csvMutation.isPending}
        lang={lang}
      />

      <ManualMeasurementDialog
        isOpen={isManualEntryOpen}
        onClose={() => setIsManualEntryOpen(false)}
        onSubmit={handleManualMeasurement}
        isSubmitting={manualMutation.isPending}
        canVerify={canManage}
        lang={lang}
      />
    </div>
  )
}
