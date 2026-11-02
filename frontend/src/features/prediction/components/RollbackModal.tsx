import React, { useState } from 'react'
import { Button } from '@/shared/ui/button'
import {
  RotateCcw,
  AlertTriangle,
  X,
} from 'lucide-react'
import { PredictionMethodVersion, PredictionType } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface RollbackModalProps {
  isOpen: boolean
  onClose: () => void
  activeVersions: PredictionMethodVersion[]
  onExecuteRollback: (type: PredictionType, priorVersionTag: string, reason: string) => void
  isRollingBack?: boolean
  lang: SupportedLanguage
}

export const RollbackModal: React.FC<RollbackModalProps> = ({
  isOpen,
  onClose,
  activeVersions,
  onExecuteRollback,
  isRollingBack,
  lang,
}) => {
  const [selectedType, setSelectedType] = useState<PredictionType>('SOIL_MOISTURE_FORECAST')
  const [priorVersionTag, setPriorVersionTag] = useState('SOIL_MOISTURE_TREND_V1')
  const [reason, setReason] = useState('')

  const t = {
    uz: {
      title: 'Favqulodda Model Qaytarish (Emergency Rollback)',
      subtitle: 'Agar ishlab chiqarishdagi yangi model kutilmagan og‘ish keltirib chiqarsa, darhol avvalgi barqaror versiyaga qaytarish',
      selectType: 'Model Turini Tanlang',
      currentActive: 'Hozirgi Faol Versiya',
      priorVersion: 'Qaytariladigan Barqaror Versiya Tegi',
      reasonLabel: 'Qaytarish Sababi (Audit jurnali uchun majburiy)',
      reasonPlaceholder: 'Masalan: Sho‘rlangan tuproqlarda namlik kutilmaganda kam hisoblandi...',
      warningBanner: 'DIQQAT: Qaytarish (Rollback) joriy versiyani darhol bekor qiladi va ko‘rsatilgan avvalgi versiyani zudlik bilan ishlab chiqarishga qaytaradi. Barcha operatsiyalar audit jurnalida muhrlanadi.',
      cancel: 'Bekor Qilish',
      confirmBtn: 'Qaytarishni Tasdiqlash',
      rollingBack: 'Qaytarilmoqda...',
    },
    ru: {
      title: 'Экстренный Откат Модели (Emergency Rollback)',
      subtitle: 'Немедленный возврат к предыдущей стабильной версии при обнаружении аномалий или ухудшения качества',
      selectType: 'Выберите Тип Модели',
      currentActive: 'Текущая Активная Версия',
      priorVersion: 'Тег Предыдущей Стабильной Версии',
      reasonLabel: 'Причина Отката (Обязательно для аудита)',
      reasonPlaceholder: 'Например: Зафиксировано неожиданное занижение влажности на суглинистых почвах...',
      warningBanner: 'ВНИМАНИЕ: Откат немедленно выводит текущую версию из эксплуатации и восстанавливает выбранную базовую версию в продакшене. Все действия логируются в журнал аудита.',
      cancel: 'Отмена',
      confirmBtn: 'Подтвердить Откат',
      rollingBack: 'Выполняется откат...',
    },
    en: {
      title: 'Emergency Model Rollback',
      subtitle: 'Immediately restore a prior stable model version if operational degradation or systematic bias occurs',
      selectType: 'Select Prediction Model Type',
      currentActive: 'Current Active Version',
      priorVersion: 'Target Prior Version Tag to Restore',
      reasonLabel: 'Rollback Justification (Mandatory Audit Trail)',
      reasonPlaceholder: 'e.g., Unforeseen negative bias observed in central district cotton parcels...',
      warningBanner: 'WARNING: Rollback immediately deactivates the active production model and restores the prior baseline version. All decisions are immutably logged to the governance audit trail.',
      cancel: 'Cancel',
      confirmBtn: 'Confirm Emergency Rollback',
      rollingBack: 'Rolling Back...',
    },
  }[lang]

  if (!isOpen) return null

  const currentActiveForType = activeVersions.find((v) => v.predictionType === selectedType)

  const handleConfirm = () => {
    if (!priorVersionTag.trim() || !reason.trim()) return
    onExecuteRollback(selectedType, priorVersionTag.trim(), reason.trim())
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
      <div className="bg-card border rounded-xl max-w-lg w-full p-6 shadow-2xl space-y-5">
        {/* Header */}
        <div className="flex items-start justify-between border-b pb-3">
          <div className="flex items-center gap-2">
            <RotateCcw className="w-5 h-5 text-red-500" />
            <h3 className="text-base font-bold text-foreground">{t.title}</h3>
          </div>
          <Button variant="ghost" size="icon" onClick={onClose} className="rounded-full">
            <X className="w-4 h-4" />
          </Button>
        </div>

        <p className="text-xs text-muted-foreground">{t.subtitle}</p>

        {/* Warning Banner */}
        <div className="p-3.5 rounded-lg border border-red-500/50 bg-red-500/10 text-red-900 dark:text-red-200 flex items-start gap-3">
          <AlertTriangle className="w-5 h-5 text-red-600 shrink-0 mt-0.5" />
          <p className="text-xs">{t.warningBanner}</p>
        </div>

        {/* Form Controls */}
        <div className="space-y-4">
          {/* Prediction Type Selection */}
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground">{t.selectType}</label>
            <select
              className="w-full text-xs p-2.5 rounded border bg-background"
              value={selectedType}
              onChange={(e) => {
                const newType = e.target.value as PredictionType
                setSelectedType(newType)
                // Set default prior version tag suggestion
                if (newType === 'SOIL_MOISTURE_FORECAST') setPriorVersionTag('SOIL_MOISTURE_TREND_V1')
                else if (newType === 'WATER_DEMAND_FORECAST') setPriorVersionTag('WATER_DEMAND_RULE_V1')
                else if (newType === 'IRRIGATION_NEED_FORECAST') setPriorVersionTag('IRRIGATION_NEED_RULE_V1')
                else setPriorVersionTag('SENSOR_RELIABILITY_V1')
              }}
            >
              <option value="SOIL_MOISTURE_FORECAST">SOIL MOISTURE FORECAST</option>
              <option value="WATER_DEMAND_FORECAST">WATER DEMAND FORECAST</option>
              <option value="IRRIGATION_NEED_FORECAST">IRRIGATION NEED FORECAST</option>
              <option value="SENSOR_RELIABILITY_FORECAST">SENSOR RELIABILITY FORECAST</option>
            </select>
          </div>

          {/* Current Active Indicator */}
          <div className="p-2.5 rounded bg-muted/40 border flex items-center justify-between text-xs">
            <span className="text-muted-foreground">{t.currentActive}:</span>
            <span className="font-mono font-bold text-foreground">
              {currentActiveForType?.versionTag || 'None registered'}
            </span>
          </div>

          {/* Target Prior Version Tag */}
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground">{t.priorVersion}</label>
            <input
              type="text"
              className="w-full text-xs p-2.5 rounded border bg-background font-mono"
              value={priorVersionTag}
              onChange={(e) => setPriorVersionTag(e.target.value)}
              placeholder="e.g. SOIL_MOISTURE_TREND_V1"
            />
          </div>

          {/* Reason */}
          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground">{t.reasonLabel}</label>
            <textarea
              className="w-full text-xs p-2.5 rounded border bg-background"
              rows={3}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder={t.reasonPlaceholder}
            />
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center justify-end gap-2 border-t pt-4">
          <Button variant="outline" size="sm" onClick={onClose}>
            {t.cancel}
          </Button>
          <Button
            variant="destructive"
            size="sm"
            disabled={!priorVersionTag.trim() || !reason.trim() || isRollingBack}
            onClick={handleConfirm}
            className="text-xs flex items-center gap-1.5"
          >
            <RotateCcw className="w-3.5 h-3.5" />
            {isRollingBack ? t.rollingBack : t.confirmBtn}
          </Button>
        </div>
      </div>
    </div>
  )
}
