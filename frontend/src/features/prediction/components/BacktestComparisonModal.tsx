import React, { useState } from 'react'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import {
  AlertTriangle,
  CheckCircle2,
  Clock,
  Play,
  RotateCw,
  ShieldAlert,
  ShieldCheck,
  X,
  XCircle,
  FileCheck2,
  Lock,
} from 'lucide-react'
import { CalibrationProposalDetailResponse } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface BacktestComparisonModalProps {
  detail: CalibrationProposalDetailResponse | null
  isLoading?: boolean
  onClose: () => void
  onRunBacktest: (proposalId: string) => void
  isBacktesting?: boolean
  onApprove: (proposalId: string, notes?: string) => void
  isApproving?: boolean
  onReject: (proposalId: string, reason: string) => void
  isRejecting?: boolean
  onActivate: (versionId: string, mode: 'SHADOW' | 'CANARY' | 'ACTIVE') => void
  isActivating?: boolean
  lang: SupportedLanguage
}

export const BacktestComparisonModal: React.FC<BacktestComparisonModalProps> = ({
  detail,
  onClose,
  onRunBacktest,
  isBacktesting,
  onApprove,
  isApproving,
  onReject,
  isRejecting,
  onActivate,
  isActivating,
  lang,
}) => {
  const [rejectReason, setRejectReason] = useState('')
  const [showRejectInput, setShowRejectInput] = useState(false)
  const [approveNotes, setApproveNotes] = useState('')
  const [showApproveInput, setShowApproveInput] = useState(false)

  const t = {
    uz: {
      modalTitle: 'Tarixiy Orqa Test (Backtest) va Modellar Taqqoslashi',
      subtitle: 'Nomzod modelni haqiqiy o‘tmish natijalarida simulyatsiya qilish (Anti-Leakage Kafolati)',
      proposal: 'Taklif',
      currentModel: 'Hozirgi Faol Model',
      candidateModel: 'Kalibratsiya Nomzodi',
      backtestParams: 'Orqa Test Parametrlari',
      sampleSize: 'Namunalar Soni',
      dateRange: 'Baholash Oralig‘i',
      runBacktestBtn: 'Orqa Testni Boshlash (Backtest)',
      runningBacktest: 'Hisoblanmoqda...',
      mae: 'O‘rtacha Mutlaq Xatolik (MAE)',
      rmse: 'Kvadratik Xatolik (RMSE)',
      mbe: 'Sistematik Siljish (MBE Bias)',
      accuracy: 'Tasniflash Aniqligi',
      f1: 'F1 Balli',
      improvement: 'Yaxshilanish',
      regressionDetected: 'DIQQAT: REGRESSIYA VA XATOLIK ANIQLANDI',
      regressionClean: 'Regressiya Tekshiruvi: MUVAFFAQ QAYDLARI',
      regressionCleanDesc: 'Nomzod model asosiy va ikkilamchi parametrlarda degradatsiya keltirib chiqarmadi.',
      configDiff: 'Taklif Qilinayotgan Parametrlar',
      approveBtn: 'Tasdiqlash (Approve)',
      rejectBtn: 'Rad Etish (Reject)',
      activateBtn: 'Ishlab Chiqarishga Faollashtirish',
      launchShadowBtn: 'Soyada Sinov (Shadow Mode)',
      confirmReject: 'Rad Etishni Tasdiqlash',
      confirmApprove: 'Tasdiqlashni Yakunlash',
      cancel: 'Bekor Qilish',
      reasonPlaceholder: 'Rad etish sababini batafsil yozing...',
      notesPlaceholder: 'Tasdiqlovchi eslatmasi (ixtiyoriy)...',
      safetyInterlockDominance: 'Gidravlik Xavfsizlik Ustuvorligi: Ushbu taklif faqat yumshoq parametrlarni o‘zgartiradi va gidravlik xavfsizlik klapanlari ustidan to‘liq inson boshqaruvini saqlab qoladi.',
    },
    ru: {
      modalTitle: 'Исторический Бэктест и Сравнение Моделей',
      subtitle: 'Симуляция кандидата на исторических данных без утечки в будущее (Anti-Leakage Guaranteed)',
      proposal: 'Предложение',
      currentModel: 'Текущая Модель',
      candidateModel: 'Кандидат Калибровки',
      backtestParams: 'Параметры Бэктеста',
      sampleSize: 'Размер Выборки',
      dateRange: 'Период Оценки',
      runBacktestBtn: 'Запустить Бэктест',
      runningBacktest: 'Выполняется...',
      mae: 'Средняя Абсолютная Ошибка (MAE)',
      rmse: 'Среднеквадратичная (RMSE)',
      mbe: 'Систематическое Смещение (MBE)',
      accuracy: 'Точность Классификации',
      f1: 'F1-мера',
      improvement: 'Улучшение',
      regressionDetected: 'ВНИМАНИЕ: ОБНАРУЖЕНА РЕГРЕССИЯ',
      regressionClean: 'Проверка на Регрессию: ПРОЙДЕНА',
      regressionCleanDesc: 'Кандидат не ухудшает вторичные метрики и не увеличивает систематическое смещение.',
      configDiff: 'Предлагаемые Параметры',
      approveBtn: 'Одобрить (Approve)',
      rejectBtn: 'Отклонить (Reject)',
      activateBtn: 'Активировать в Прод',
      launchShadowBtn: 'Теневой Режим (Shadow)',
      confirmReject: 'Подтвердить Отклонение',
      confirmApprove: 'Завершить Одобрение',
      cancel: 'Отмена',
      reasonPlaceholder: 'Укажите причину отклонения...',
      notesPlaceholder: 'Примечания проверяющего (необязательно)...',
      safetyInterlockDominance: 'Приоритет Гидравлической Безопасности: Данное предложение изменяет только мягкие параметры прогнозирования без изменения аварийных порогов.',
    },
    en: {
      modalTitle: 'Historical Backtest & Candidate Comparison',
      subtitle: 'Deterministic simulation of candidate model on past observations (Strict Anti-Leakage)',
      proposal: 'Proposal',
      currentModel: 'Current Production Model',
      candidateModel: 'Calibration Candidate',
      backtestParams: 'Backtest Parameters',
      sampleSize: 'Sample Size',
      dateRange: 'Evaluation Window',
      runBacktestBtn: 'Execute Historical Backtest',
      runningBacktest: 'Executing Backtest...',
      mae: 'Mean Absolute Error (MAE)',
      rmse: 'Root Mean Squared Error (RMSE)',
      mbe: 'Mean Bias Error (MBE)',
      accuracy: 'Classification Accuracy',
      f1: 'F1 Score',
      improvement: 'Improvement',
      regressionDetected: 'WARNING: PERFORMANCE REGRESSION DETECTED',
      regressionClean: 'Regression Guard: PASSED',
      regressionCleanDesc: 'Candidate demonstrates healthy convergence without secondary metric deterioration or bias inflation.',
      configDiff: 'Proposed Configuration',
      approveBtn: 'Approve Proposal',
      rejectBtn: 'Reject Proposal',
      activateBtn: 'Activate in Production',
      launchShadowBtn: 'Launch in Shadow Mode',
      confirmReject: 'Confirm Rejection',
      confirmApprove: 'Confirm Approval',
      cancel: 'Cancel',
      reasonPlaceholder: 'Enter justification for rejecting this proposal...',
      notesPlaceholder: 'Reviewer audit notes (optional)...',
      safetyInterlockDominance: 'Hydraulic Safety Dominance: This proposal only modifies soft forecasting parameters. Hard hydraulic safety locks remain fully sovereign.',
    },
  }[lang]

  if (!detail) return null

  const { proposal, currentVersion, candidateVersion, latestBacktest } = detail

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4 overflow-y-auto">
      <div className="bg-card border rounded-xl max-w-4xl w-full p-6 shadow-2xl space-y-6 my-8 max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-start justify-between border-b pb-4">
          <div>
            <div className="flex items-center gap-2">
              <ShieldCheck className="w-6 h-6 text-emerald-500" />
              <h2 className="text-lg font-bold tracking-tight">{t.modalTitle}</h2>
              <Badge variant="outline" className="font-mono text-xs text-primary border-primary/30">
                {proposal.proposalCode}
              </Badge>
            </div>
            <p className="text-xs text-muted-foreground mt-1">{t.subtitle}</p>
          </div>
          <Button variant="ghost" size="icon" onClick={onClose} className="rounded-full">
            <X className="w-4 h-4" />
          </Button>
        </div>

        {/* Models Ribbon */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="p-3.5 rounded-lg border bg-muted/20">
            <span className="text-xs text-muted-foreground font-semibold">{t.currentModel}</span>
            <div className="mt-1 font-mono text-sm font-bold text-foreground">
              {proposal.currentVersionTag}
            </div>
            <div className="text-[11px] text-muted-foreground mt-0.5">
              {currentVersion?.description || 'Baseline version in active production.'}
            </div>
          </div>

          <div className="p-3.5 rounded-lg border border-primary/30 bg-primary/5">
            <span className="text-xs text-primary font-semibold">{t.candidateModel}</span>
            <div className="mt-1 font-mono text-sm font-bold text-primary">
              {proposal.candidateVersionTag}
            </div>
            <div className="text-[11px] text-muted-foreground mt-0.5">
              {proposal.candidateChanges || 'Tuned parameters targeting detected systematic bias.'}
            </div>
          </div>
        </div>

        {/* Backtest Section */}
        {latestBacktest ? (
          <div className="space-y-4">
            {/* Sample metadata */}
            <div className="flex items-center justify-between text-xs bg-muted/30 p-2.5 rounded border">
              <span className="flex items-center gap-2 text-muted-foreground">
                <Clock className="w-3.5 h-3.5 text-primary" />
                {t.dateRange}: {new Date(latestBacktest.dateRangeFrom).toLocaleDateString()} — {new Date(latestBacktest.dateRangeTo).toLocaleDateString()}
              </span>
              <span className="font-semibold text-foreground">
                {t.sampleSize}: {latestBacktest.sampleSize} {latestBacktest.hasSufficientSample ? '(Sufficient)' : '(Limited)'}
              </span>
            </div>

            {/* Regression Guard Banner */}
            {latestBacktest.regressionDetected ? (
              <div className="p-4 rounded-lg border border-red-500/50 bg-red-500/10 text-red-900 dark:text-red-200 space-y-1">
                <div className="flex items-center gap-2 font-bold text-sm text-red-600 dark:text-red-400">
                  <AlertTriangle className="w-5 h-5" />
                  {t.regressionDetected}
                </div>
                <p className="text-xs">
                  {latestBacktest.regressionDetails || 'Regression guard detected secondary metric worsening.'}
                </p>
              </div>
            ) : (
              <div className="p-3.5 rounded-lg border border-emerald-500/40 bg-emerald-500/10 text-emerald-900 dark:text-emerald-200 flex items-center gap-3">
                <ShieldCheck className="w-5 h-5 text-emerald-600 dark:text-emerald-400 shrink-0" />
                <div className="text-xs">
                  <span className="font-bold">{t.regressionClean} — </span>
                  {t.regressionCleanDesc}
                </div>
              </div>
            )}

            {/* Metric Comparison Table */}
            <div className="border rounded-lg overflow-hidden">
              <table className="w-full text-xs text-left">
                <thead className="bg-muted/50 uppercase text-[10px] text-muted-foreground font-semibold border-b">
                  <tr>
                    <th className="py-2.5 px-3">Metric</th>
                    <th className="py-2.5 px-3">Current ({proposal.currentVersionTag})</th>
                    <th className="py-2.5 px-3">Candidate ({proposal.candidateVersionTag})</th>
                    <th className="py-2.5 px-3 text-right">Delta / Improvement</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {/* MAE */}
                  <tr>
                    <td className="py-2.5 px-3 font-semibold">{t.mae}</td>
                    <td className="py-2.5 px-3 font-mono">{latestBacktest.currentMae?.toFixed(2) ?? '—'}</td>
                    <td className="py-2.5 px-3 font-mono font-bold text-primary">
                      {latestBacktest.candidateMae?.toFixed(2) ?? '—'}
                    </td>
                    <td className="py-2.5 px-3 text-right">
                      {latestBacktest.maeImprovementPercent !== null && latestBacktest.maeImprovementPercent !== undefined ? (
                        <span className={`font-bold ${latestBacktest.maeImprovementPercent >= 0 ? 'text-emerald-600 dark:text-emerald-400' : 'text-red-500'}`}>
                          {latestBacktest.maeImprovementPercent >= 0 ? '+' : ''}
                          {latestBacktest.maeImprovementPercent.toFixed(1)}%
                        </span>
                      ) : (
                        '—'
                      )}
                    </td>
                  </tr>

                  {/* RMSE */}
                  <tr>
                    <td className="py-2.5 px-3 font-semibold">{t.rmse}</td>
                    <td className="py-2.5 px-3 font-mono">{latestBacktest.currentRmse?.toFixed(2) ?? '—'}</td>
                    <td className="py-2.5 px-3 font-mono">{latestBacktest.candidateRmse?.toFixed(2) ?? '—'}</td>
                    <td className="py-2.5 px-3 text-right text-muted-foreground">
                      {latestBacktest.currentRmse && latestBacktest.candidateRmse ? (
                        <span>{(latestBacktest.currentRmse - latestBacktest.candidateRmse).toFixed(2)} units</span>
                      ) : (
                        '—'
                      )}
                    </td>
                  </tr>

                  {/* MBE (Mean Bias Error) */}
                  <tr>
                    <td className="py-2.5 px-3 font-semibold">{t.mbe}</td>
                    <td className="py-2.5 px-3 font-mono">{latestBacktest.currentMbe?.toFixed(2) ?? '—'}</td>
                    <td className="py-2.5 px-3 font-mono font-bold">
                      {latestBacktest.candidateMbe?.toFixed(2) ?? '—'}
                    </td>
                    <td className="py-2.5 px-3 text-right text-muted-foreground">
                      {latestBacktest.currentMbe && latestBacktest.candidateMbe ? (
                        <span>
                          {Math.abs(latestBacktest.candidateMbe) < Math.abs(latestBacktest.currentMbe)
                            ? 'Bias reduced'
                            : 'Bias unchanged'}
                        </span>
                      ) : (
                        '—'
                      )}
                    </td>
                  </tr>

                  {/* Classification Accuracy (if applicable) */}
                  {typeof latestBacktest.currentAccuracyPercent === 'number' && (
                    <tr>
                      <td className="py-2.5 px-3 font-semibold">{t.accuracy}</td>
                      <td className="py-2.5 px-3 font-mono">{latestBacktest.currentAccuracyPercent?.toFixed(1)}%</td>
                      <td className="py-2.5 px-3 font-mono font-bold text-primary">
                        {latestBacktest.candidateAccuracyPercent?.toFixed(1) ?? '—'}%
                      </td>
                      <td className="py-2.5 px-3 text-right">
                        {typeof latestBacktest.candidateAccuracyPercent === 'number' && typeof latestBacktest.currentAccuracyPercent === 'number' ? (
                          <span className={latestBacktest.candidateAccuracyPercent >= latestBacktest.currentAccuracyPercent ? 'text-emerald-600' : 'text-red-500 font-bold'}>
                            {(latestBacktest.candidateAccuracyPercent - latestBacktest.currentAccuracyPercent).toFixed(1)}%
                          </span>
                        ) : '—'}
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        ) : (
          <div className="p-6 border border-dashed rounded-lg text-center space-y-3">
            <Clock className="w-8 h-8 text-muted-foreground mx-auto" />
            <p className="text-xs text-muted-foreground">
              No historical backtest has been executed yet for this proposal.
            </p>
            <Button
              variant="outline"
              size="sm"
              disabled={isBacktesting}
              onClick={() => onRunBacktest(proposal.id)}
              className="text-xs"
            >
              <Play className="w-3.5 h-3.5 mr-1" />
              {isBacktesting ? t.runningBacktest : t.runBacktestBtn}
            </Button>
          </div>
        )}

        {/* Configuration Diff View */}
        <div className="space-y-2">
          <span className="text-xs font-semibold text-foreground flex items-center gap-1.5">
            <Lock className="w-3.5 h-3.5 text-primary" />
            {t.configDiff}
          </span>
          <pre className="p-3 bg-muted/60 border rounded text-xs font-mono overflow-x-auto text-foreground max-h-40">
            {(() => {
              try {
                return JSON.stringify(JSON.parse(proposal.proposedConfigurationJson), null, 2)
              } catch {
                return proposal.proposedConfigurationJson
              }
            })()}
          </pre>
        </div>

        {/* Reject Input */}
        {showRejectInput && (
          <div className="p-3.5 rounded-lg border border-red-500/30 bg-red-500/5 space-y-2">
            <label className="text-xs font-semibold text-red-600 dark:text-red-400">
              {t.confirmReject}
            </label>
            <textarea
              className="w-full text-xs p-2 rounded border bg-background"
              rows={3}
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder={t.reasonPlaceholder}
            />
            <div className="flex justify-end gap-2">
              <Button variant="ghost" size="sm" onClick={() => setShowRejectInput(false)}>
                {t.cancel}
              </Button>
              <Button
                variant="destructive"
                size="sm"
                disabled={!rejectReason.trim() || isRejecting}
                onClick={() => onReject(proposal.id, rejectReason)}
              >
                {t.confirmReject}
              </Button>
            </div>
          </div>
        )}

        {/* Approve Input */}
        {showApproveInput && (
          <div className="p-3.5 rounded-lg border border-emerald-500/30 bg-emerald-500/5 space-y-2">
            <label className="text-xs font-semibold text-emerald-600 dark:text-emerald-400">
              {t.confirmApprove}
            </label>
            <textarea
              className="w-full text-xs p-2 rounded border bg-background"
              rows={2}
              value={approveNotes}
              onChange={(e) => setApproveNotes(e.target.value)}
              placeholder={t.notesPlaceholder}
            />
            <div className="flex justify-end gap-2">
              <Button variant="ghost" size="sm" onClick={() => setShowApproveInput(false)}>
                {t.cancel}
              </Button>
              <Button
                variant="default"
                size="sm"
                disabled={isApproving}
                onClick={() => onApprove(proposal.id, approveNotes)}
                className="bg-emerald-600 hover:bg-emerald-700 text-white"
              >
                {t.confirmApprove}
              </Button>
            </div>
          </div>
        )}

        {/* Safety Footer & Buttons */}
        <div className="border-t pt-4 space-y-4">
          <p className="text-[11px] text-muted-foreground flex items-center gap-1.5">
            <ShieldAlert className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
            {t.safetyInterlockDominance}
          </p>

          <div className="flex items-center justify-between flex-wrap gap-2">
            <Button variant="outline" size="sm" onClick={onClose}>
              {t.cancel}
            </Button>

            <div className="flex items-center gap-2 flex-wrap">
              {/* Backtest Trigger */}
              {latestBacktest && (
                <Button
                  variant="outline"
                  size="sm"
                  disabled={isBacktesting}
                  onClick={() => onRunBacktest(proposal.id)}
                  className="text-xs"
                >
                  <RotateCw className="w-3.5 h-3.5 mr-1" />
                  {isBacktesting ? t.runningBacktest : t.runBacktestBtn}
                </Button>
              )}

              {/* Reject Button */}
              {proposal.status !== 'REJECTED' && proposal.status !== 'ACTIVE' && (
                <Button
                  variant="destructive"
                  size="sm"
                  disabled={isRejecting || showRejectInput}
                  onClick={() => setShowRejectInput(true)}
                  className="text-xs"
                >
                  <XCircle className="w-3.5 h-3.5 mr-1" />
                  {t.rejectBtn}
                </Button>
              )}

              {/* Approve Button */}
              {proposal.status !== 'APPROVED' && proposal.status !== 'ACTIVE' && proposal.status !== 'REJECTED' && (
                <Button
                  variant="default"
                  size="sm"
                  disabled={isApproving || showApproveInput || !latestBacktest}
                  onClick={() => setShowApproveInput(true)}
                  className="text-xs bg-emerald-600 hover:bg-emerald-700 text-white"
                >
                  <CheckCircle2 className="w-3.5 h-3.5 mr-1" />
                  {t.approveBtn}
                </Button>
              )}

              {/* Activate Button (if already approved) */}
              {proposal.status === 'APPROVED' && candidateVersion && (
                <>
                  <Button
                    variant="outline"
                    size="sm"
                    disabled={isActivating}
                    onClick={() => onActivate(candidateVersion.id, 'SHADOW')}
                    className="text-xs text-purple-600 border-purple-500/30"
                  >
                    {t.launchShadowBtn}
                  </Button>
                  <Button
                    variant="default"
                    size="sm"
                    disabled={isActivating}
                    onClick={() => onActivate(candidateVersion.id, 'ACTIVE')}
                    className="text-xs bg-emerald-600 hover:bg-emerald-700 text-white font-semibold"
                  >
                    <FileCheck2 className="w-3.5 h-3.5 mr-1" />
                    {t.activateBtn}
                  </Button>
                </>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
