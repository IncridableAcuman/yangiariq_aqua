import React, { useState, useRef } from 'react'
import { ImportBatchSummary } from '@/entities/integration'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { AlertCircle, CheckCircle2, FileUp, UploadCloud, AlertTriangle, ArrowRight, RefreshCw } from 'lucide-react'
import { translations, SupportedLanguage } from '@/shared/i18n'

interface CsvImportDialogProps {
  isOpen: boolean
  onClose: () => void
  onUpload: (file: File, dryRun: boolean) => Promise<ImportBatchSummary | undefined>
  isUploading?: boolean
  lang: SupportedLanguage
}

export const CsvImportDialog: React.FC<CsvImportDialogProps> = ({
  isOpen,
  onClose,
  onUpload,
  isUploading,
  lang,
}) => {
  const t = translations[lang].dataIntegration

  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [previewResult, setPreviewResult] = useState<ImportBatchSummary | null>(null)
  const [finalResult, setFinalResult] = useState<ImportBatchSummary | null>(null)
  const [error, setError] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setError(null)
    setPreviewResult(null)
    setFinalResult(null)

    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0]
      if (file.size > 10 * 1024 * 1024) {
        setError(t.fileSizeLimitError)
        return
      }
      setSelectedFile(file)
    }
  }

  const handleValidateDryRun = async () => {
    if (!selectedFile) return
    setError(null)
    try {
      const summary = await onUpload(selectedFile, true)
      if (summary) {
        setPreviewResult(summary)
      }
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Validation failed')
    }
  }

  const handleCommitImport = async () => {
    if (!selectedFile) return
    setError(null)
    try {
      const summary = await onUpload(selectedFile, false)
      if (summary) {
        setFinalResult(summary)
      }
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Import failed')
    }
  }

  const handleReset = () => {
    setSelectedFile(null)
    setPreviewResult(null)
    setFinalResult(null)
    setError(null)
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }

  const handleCloseDialog = () => {
    handleReset()
    onClose()
  }

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && handleCloseDialog()}>
      <DialogContent className="max-w-xl">
        <DialogHeader>
          <DialogTitle className="text-lg font-bold flex items-center gap-2">
            <FileUp className="h-5 w-5 text-primary" />
            {t.csvImportTitle}
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-4 py-2">
          {error && (
            <div className="p-3 rounded-lg bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900/60 text-rose-800 dark:text-rose-300 text-xs flex items-start gap-2">
              <AlertCircle className="h-4 w-4 shrink-0 mt-0.5" />
              <div>{error}</div>
            </div>
          )}

          {/* Final Completed Summary State */}
          {finalResult ? (
            <div className="p-4 rounded-lg bg-emerald-50/60 dark:bg-emerald-950/20 border border-emerald-200 dark:border-emerald-900/40 space-y-3">
              <div className="flex items-center gap-2 text-emerald-700 dark:text-emerald-400 font-semibold">
                <CheckCircle2 className="h-5 w-5" />
                {t.importCompletedTitle}
              </div>
              <div className="grid grid-cols-3 gap-2 text-center text-xs">
                <div className="p-2 rounded bg-background border">
                  <span className="text-muted-foreground">{t.totalRows}</span>
                  <p className="text-sm font-bold">{finalResult.totalRows}</p>
                </div>
                <div className="p-2 rounded bg-background border">
                  <span className="text-emerald-600">{t.successful}</span>
                  <p className="text-sm font-bold text-emerald-600">{finalResult.successfulRows}</p>
                </div>
                <div className="p-2 rounded bg-background border">
                  <span className="text-rose-600">{t.rejected}</span>
                  <p className="text-sm font-bold text-rose-600">{finalResult.rejectedRows}</p>
                </div>
              </div>

              {finalResult.sampleErrors && finalResult.sampleErrors.length > 0 && (
                <div className="text-xs space-y-1">
                  <span className="font-semibold text-rose-600">{t.rowErrorsSample}:</span>
                  <div className="max-h-28 overflow-y-auto font-mono text-[11px] bg-background p-2 rounded border space-y-1">
                    {finalResult.sampleErrors.map((err, i) => (
                      <div key={i} className="text-rose-600">
                        {t.row} {err.rowNumber}: {err.errorMessage}
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          ) : previewResult ? (
            /* Dry Run Preview State */
            <div className="space-y-3">
              <div className="p-3 rounded-lg bg-amber-50 dark:bg-amber-950/30 border border-amber-200 dark:border-amber-900/40 text-xs text-amber-800 dark:text-amber-300 flex items-start gap-2">
                <AlertTriangle className="h-4 w-4 shrink-0 mt-0.5" />
                <div>
                  <span className="font-semibold">{t.dryRunPreviewNotice}:</span> {t.dryRunPreviewDescription}
                </div>
              </div>

              <div className="grid grid-cols-3 gap-2 text-center text-xs">
                <div className="p-2 rounded bg-muted/50 border">
                  <span className="text-muted-foreground">{t.totalRows}</span>
                  <p className="text-sm font-bold">{previewResult.totalRows}</p>
                </div>
                <div className="p-2 rounded bg-muted/50 border">
                  <span className="text-emerald-600">{t.validRows}</span>
                  <p className="text-sm font-bold text-emerald-600">{previewResult.successfulRows}</p>
                </div>
                <div className="p-2 rounded bg-muted/50 border">
                  <span className="text-rose-600">{t.invalidRows}</span>
                  <p className="text-sm font-bold text-rose-600">{previewResult.rejectedRows}</p>
                </div>
              </div>

              {previewResult.sampleErrors && previewResult.sampleErrors.length > 0 && (
                <div className="text-xs space-y-1">
                  <span className="font-semibold text-rose-600">{t.validationErrorsFound}:</span>
                  <div className="max-h-28 overflow-y-auto font-mono text-[11px] bg-muted/50 p-2 rounded border space-y-1">
                    {previewResult.sampleErrors.map((err, i) => (
                      <div key={i} className="text-rose-600">
                        {t.row} {err.rowNumber}: {err.errorMessage}
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          ) : (
            /* File Upload Input State */
            <div className="space-y-3">
              <div
                className="border-2 border-dashed border-border/80 rounded-lg p-6 flex flex-col items-center justify-center cursor-pointer hover:border-primary/60 transition-colors bg-muted/20"
                onClick={() => fileInputRef.current?.click()}
              >
                <UploadCloud className="h-10 w-10 text-muted-foreground mb-2" />
                <span className="text-sm font-medium">{t.dropCsvHere}</span>
                <span className="text-xs text-muted-foreground mt-1">{t.csvFormatNotice}</span>
                <input
                  type="file"
                  ref={fileInputRef}
                  accept=".csv,text/csv"
                  className="hidden"
                  onChange={handleFileChange}
                />
              </div>

              {selectedFile && (
                <div className="flex items-center justify-between p-2.5 rounded bg-muted/40 border text-xs">
                  <span className="font-medium truncate">{selectedFile.name}</span>
                  <span className="text-muted-foreground font-mono">
                    {(selectedFile.size / 1024).toFixed(1)} KB
                  </span>
                </div>
              )}

              <div className="text-[11px] text-muted-foreground space-y-0.5">
                <span className="font-semibold">{t.expectedHeaders}:</span>
                <p className="font-mono text-[10px] bg-muted/40 p-1.5 rounded">
                  field_id, type, value, unit, timestamp, external_record_id
                </p>
              </div>
            </div>
          )}
        </div>

        <DialogFooter className="pt-2">
          {finalResult ? (
            <Button size="sm" onClick={handleCloseDialog}>
              {t.done}
            </Button>
          ) : previewResult ? (
            <div className="flex items-center justify-between w-full">
              <Button variant="ghost" size="sm" onClick={handleReset} disabled={isUploading}>
                {t.changeFile}
              </Button>
              <div className="flex gap-2">
                <Button variant="outline" size="sm" onClick={handleCloseDialog}>
                  {t.cancel}
                </Button>
                <Button size="sm" onClick={handleCommitImport} disabled={isUploading}>
                  {isUploading ? (
                    <>
                      <RefreshCw className="h-3.5 w-3.5 mr-1.5 animate-spin" />
                      {t.importing}
                    </>
                  ) : (
                    <>
                      {t.confirmAndImport}
                      <ArrowRight className="h-3.5 w-3.5 ml-1.5" />
                    </>
                  )}
                </Button>
              </div>
            </div>
          ) : (
            <div className="flex items-center justify-between w-full">
              <Button variant="outline" size="sm" onClick={handleCloseDialog}>
                {t.cancel}
              </Button>
              <Button size="sm" onClick={handleValidateDryRun} disabled={!selectedFile || isUploading}>
                {isUploading ? (
                  <>
                    <RefreshCw className="h-3.5 w-3.5 mr-1.5 animate-spin" />
                    {t.validating}
                  </>
                ) : (
                  t.validatePreview
                )}
              </Button>
            </div>
          )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
