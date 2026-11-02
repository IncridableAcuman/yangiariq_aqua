import React, { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Badge } from '@/shared/ui/badge'
import { Button } from '@/shared/ui/button'
import {
  Lock,
  Unlock,
  Eye,
  Sliders,
} from 'lucide-react'
import { MethodVersionStatus, PredictionMethodVersion, PredictionType } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'

interface MethodVersionTableProps {
  versions: PredictionMethodVersion[]
  isLoading?: boolean
  onActivateVersion?: (versionId: string, mode: 'SHADOW' | 'CANARY' | 'ACTIVE') => void
  isActivating?: boolean
  lang: SupportedLanguage
}

export const MethodVersionTable: React.FC<MethodVersionTableProps> = ({
  versions,
  onActivateVersion,
  isActivating,
  lang,
}) => {
  const [selectedConfigVersion, setSelectedConfigVersion] = useState<PredictionMethodVersion | null>(null)

  const t = {
    uz: {
      title: 'Prediktiv Usullar va Model Versiyalari Reyestri',
      subtitle: 'Har bir prognoz turi uchun faol, sinovdagi va arxivlangan usullar versiyasi va ularning parametrlari',
      predictionType: 'Prognoz Turi',
      versionTag: 'Versiya Tegi',
      status: 'Holati',
      immutability: 'O‘zgarmaslik (Immutability)',
      createdActivated: 'Yaratilgan / Faollashtirilgan',
      config: 'Konfiguratsiya',
      actions: 'Amallar',
      activeBadge: 'FAOL (ACTIVE)',
      shadowBadge: 'SOYA (SHADOW)',
      canaryBadge: 'KANAREYKA (CANARY)',
      candidateBadge: 'NOMZOD (CANDIDATE)',
      deprecatedBadge: 'BEKOR QILINGAN',
      retiredBadge: 'ARXIVDA',
      viewConfig: 'Ko‘rish',
      activate: 'Faollashtirish',
      launchShadow: 'Soyaga o‘tkazish',
      launchCanary: 'Kanareyka',
      immutableYes: 'Qulflangan',
      immutableNo: 'Tahrirlanadi',
      noVersions: 'Hech qanday versiya topilmadi.',
      configModalTitle: 'Model Konfiguratsiyasi Snapshot',
      close: 'Yopish',
    },
    ru: {
      title: 'Реестр Версий Предиктивных Методов',
      subtitle: 'Активные, теневые, канареечные и архивные версии конфигураций для каждого типа прогноза',
      predictionType: 'Тип Прогноза',
      versionTag: 'Тег Версии',
      status: 'Статус',
      immutability: 'Неизменяемость',
      createdActivated: 'Создана / Активирована',
      config: 'Конфигурация',
      actions: 'Действия',
      activeBadge: 'АКТИВНА',
      shadowBadge: 'ТЕНЕВАЯ (SHADOW)',
      canaryBadge: 'КАНАРЕЙКА (CANARY)',
      candidateBadge: 'КАНДИДАТ',
      deprecatedBadge: 'УСТАРЕЛА',
      retiredBadge: 'АРХИВ',
      viewConfig: 'Просмотр',
      activate: 'Активировать',
      launchShadow: 'В тень (Shadow)',
      launchCanary: 'Канарейка',
      immutableYes: 'Заблокирована',
      immutableNo: 'Редактируема',
      noVersions: 'Версии не найдены.',
      configModalTitle: 'Снимок Конфигурации Модели',
      close: 'Закрыть',
    },
    en: {
      title: 'Prediction Method Version Registry',
      subtitle: 'Active production, shadow, canary, and retired model configurations per prediction type',
      predictionType: 'Prediction Type',
      versionTag: 'Version Tag',
      status: 'Status',
      immutability: 'Immutability',
      createdActivated: 'Created / Activated',
      config: 'Configuration',
      actions: 'Actions',
      activeBadge: 'ACTIVE',
      shadowBadge: 'SHADOW',
      canaryBadge: 'CANARY',
      candidateBadge: 'CANDIDATE',
      deprecatedBadge: 'DEPRECATED',
      retiredBadge: 'RETIRED',
      viewConfig: 'View',
      activate: 'Activate',
      launchShadow: 'Shadow Mode',
      launchCanary: 'Canary Mode',
      immutableYes: 'Immutable (Locked)',
      immutableNo: 'Mutable',
      noVersions: 'No method versions found.',
      configModalTitle: 'Model Configuration Snapshot',
      close: 'Close',
    },
  }[lang]

  const getStatusBadge = (status: MethodVersionStatus) => {
    switch (status) {
      case 'ACTIVE':
        return <Badge className="bg-emerald-600 text-white font-medium text-[11px]">{t.activeBadge}</Badge>
      case 'SHADOW':
        return <Badge variant="outline" className="border-purple-500 text-purple-600 dark:text-purple-400 bg-purple-500/10 text-[11px]">{t.shadowBadge}</Badge>
      case 'CANARY':
        return <Badge variant="outline" className="border-cyan-500 text-cyan-600 dark:text-cyan-400 bg-cyan-500/10 text-[11px]">{t.canaryBadge}</Badge>
      case 'CANDIDATE':
      case 'APPROVED':
        return <Badge variant="outline" className="border-amber-500 text-amber-600 dark:text-amber-400 bg-amber-500/10 text-[11px]">{t.candidateBadge}</Badge>
      case 'DEPRECATED':
        return <Badge variant="secondary" className="text-muted-foreground text-[11px]">{t.deprecatedBadge}</Badge>
      case 'RETIRED':
      default:
        return <Badge variant="destructive" className="text-[11px]">{t.retiredBadge}</Badge>
    }
  }

  const formatType = (type: PredictionType) => {
    return type.replace(/_/g, ' ')
  }

  return (
    <Card className="border bg-card/60 backdrop-blur-sm">
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="text-lg font-bold flex items-center gap-2">
              <Sliders className="w-5 h-5 text-primary" />
              {t.title}
            </CardTitle>
            <CardDescription className="text-xs mt-1 text-muted-foreground">
              {t.subtitle}
            </CardDescription>
          </div>
          <Badge variant="outline" className="text-xs">
            {versions.length} {lang === 'uz' ? 'ta versiya' : lang === 'ru' ? 'версий' : 'versions'}
          </Badge>
        </div>
      </CardHeader>

      <CardContent>
        <div className="overflow-x-auto">
          <table className="w-full text-xs text-left">
            <thead className="bg-muted/40 uppercase text-[10px] text-muted-foreground font-semibold border-b">
              <tr>
                <th className="py-2.5 px-3">{t.predictionType}</th>
                <th className="py-2.5 px-3">{t.versionTag}</th>
                <th className="py-2.5 px-3">{t.status}</th>
                <th className="py-2.5 px-3">{t.immutability}</th>
                <th className="py-2.5 px-3">{t.createdActivated}</th>
                <th className="py-2.5 px-3">{t.config}</th>
                <th className="py-2.5 px-3 text-right">{t.actions}</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {versions.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-6 text-muted-foreground">
                    {t.noVersions}
                  </td>
                </tr>
              ) : (
                versions.map((ver) => (
                  <tr key={ver.id} className="hover:bg-muted/20 transition-colors">
                    <td className="py-3 px-3 font-medium text-foreground">
                      {formatType(ver.predictionType)}
                    </td>
                    <td className="py-3 px-3">
                      <span className="font-mono text-[11px] bg-muted/60 px-1.5 py-0.5 rounded border border-muted">
                        {ver.versionTag}
                      </span>
                    </td>
                    <td className="py-3 px-3">
                      {getStatusBadge(ver.status)}
                    </td>
                    <td className="py-3 px-3">
                      {ver.isImmutable ? (
                        <span className="flex items-center gap-1 text-emerald-600 dark:text-emerald-400 font-medium text-[11px]">
                          <Lock className="w-3.5 h-3.5" />
                          {t.immutableYes}
                        </span>
                      ) : (
                        <span className="flex items-center gap-1 text-muted-foreground text-[11px]">
                          <Unlock className="w-3.5 h-3.5" />
                          {t.immutableNo}
                        </span>
                      )}
                    </td>
                    <td className="py-3 px-3 text-muted-foreground text-[11px]">
                      <div>{ver.createdAt ? new Date(ver.createdAt).toLocaleDateString() : '—'}</div>
                      {ver.activatedAt && (
                        <div className="text-[10px] text-emerald-600 dark:text-emerald-400">
                          {new Date(ver.activatedAt).toLocaleDateString()}
                        </div>
                      )}
                    </td>
                    <td className="py-3 px-3">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setSelectedConfigVersion(ver)}
                        className="h-7 text-xs flex items-center gap-1 text-primary hover:text-primary"
                      >
                        <Eye className="w-3.5 h-3.5" />
                        {t.viewConfig}
                      </Button>
                    </td>
                    <td className="py-3 px-3 text-right">
                      {onActivateVersion && ver.status !== 'ACTIVE' && ver.status !== 'RETIRED' && (
                        <div className="flex items-center justify-end gap-1.5">
                          {ver.status !== 'SHADOW' && (
                            <Button
                              variant="outline"
                              size="sm"
                              disabled={isActivating}
                              onClick={() => onActivateVersion(ver.id, 'SHADOW')}
                              className="h-7 text-[11px] px-2 text-purple-600 border-purple-500/30"
                            >
                              {t.launchShadow}
                            </Button>
                          )}
                          {ver.status !== 'CANARY' && (
                            <Button
                              variant="outline"
                              size="sm"
                              disabled={isActivating}
                              onClick={() => onActivateVersion(ver.id, 'CANARY')}
                              className="h-7 text-[11px] px-2 text-cyan-600 border-cyan-500/30"
                            >
                              {t.launchCanary}
                            </Button>
                          )}
                          <Button
                            variant="default"
                            size="sm"
                            disabled={isActivating}
                            onClick={() => onActivateVersion(ver.id, 'ACTIVE')}
                            className="h-7 text-[11px] px-2 bg-emerald-600 hover:bg-emerald-700 text-white"
                          >
                            {t.activate}
                          </Button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Configuration JSON Modal */}
        {selectedConfigVersion && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
            <div className="bg-card border rounded-lg max-w-lg w-full p-5 shadow-2xl space-y-4">
              <div className="flex items-center justify-between border-b pb-3">
                <div>
                  <h3 className="text-sm font-bold flex items-center gap-2">
                    <Lock className="w-4 h-4 text-emerald-500" />
                    {t.configModalTitle}
                  </h3>
                  <p className="text-xs text-muted-foreground mt-0.5 font-mono">
                    {selectedConfigVersion.versionTag} ({selectedConfigVersion.predictionType})
                  </p>
                </div>
                {getStatusBadge(selectedConfigVersion.status)}
              </div>

              <div>
                <p className="text-xs text-muted-foreground mb-2">
                  {selectedConfigVersion.description || 'Production parameters snapshot.'}
                </p>
                <pre className="p-3 bg-muted/60 border rounded text-xs font-mono overflow-x-auto text-foreground max-h-60">
                  {(() => {
                    try {
                      return JSON.stringify(JSON.parse(selectedConfigVersion.configurationJson), null, 2)
                    } catch {
                      return selectedConfigVersion.configurationJson
                    }
                  })()}
                </pre>
              </div>

              <div className="flex justify-end pt-2">
                <Button variant="outline" size="sm" onClick={() => setSelectedConfigVersion(null)}>
                  {t.close}
                </Button>
              </div>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
