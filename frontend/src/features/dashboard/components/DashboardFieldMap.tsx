import React, { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { Map, ExternalLink, Droplets, ArrowRight, Layers } from 'lucide-react'
import { WaterFieldMap, useGisOverviewQuery } from '@/features/water/gis'
import { GisField } from '@/entities/gis'
import { SupportedLanguage } from '@/shared/i18n'
import { NavSection } from '@/widgets/sidebar'

interface DashboardFieldMapProps {
  lang: SupportedLanguage
  onNavigate: (section: NavSection) => void
}

export const DashboardFieldMap: React.FC<DashboardFieldMapProps> = ({
  lang,
  onNavigate,
}) => {
  const [selectedField, setSelectedField] = useState<GisField | null>(null)

  const { data: gisOverview, isLoading } = useGisOverviewQuery()

  const t = {
    uz: {
      title: 'Dala & Suv Manbalari GIS Xaritasi',
      subtitle: 'Yangiariq tumani boʻyicha ekin maydonlari va magistral kanallar kartografiyasi',
      openFullGis: 'GIS Xaritani Toʻliq Ochish',
      quickPanelTitle: 'Tanlangan Dala Tafsiloti',
      area: 'Maydon',
      crop: 'Ekin',
      status: 'Holat',
      openField: 'Dala Tafsilotiga Oʻtish',
      noSelection: 'Xaritadagi istalgan dalani bosing — tezkor gidro-agronomik status bu yerda aks etadi',
    },
    ru: {
      title: 'ГИС Карта Полей и Водных Источников',
      subtitle: 'Картография земельных участков и магистральных каналов Янгиарыкского района',
      openFullGis: 'Открыть ГИС Карту Полностью',
      quickPanelTitle: 'Сводка по Выбранному Полю',
      area: 'Площадь',
      crop: 'Культура',
      status: 'Статус',
      openField: 'Перейти к Полю',
      noSelection: 'Нажмите на любое поле на карте для отображения оперативной сводки',
    },
    en: {
      title: 'GIS Farm Cartography',
      subtitle: 'Spatial distribution of parcels, supply canals, and hydroposts in Yangiariq',
      openFullGis: 'Open Full GIS Map',
      quickPanelTitle: 'Selected Parcel Quick Panel',
      area: 'Area',
      crop: 'Crop',
      status: 'Status',
      openField: 'Open Parcel Details',
      noSelection: 'Click any field polygon on the map to inspect live status and actions',
    },
  }[lang]

  const fields = gisOverview?.fields || []
  const waterSources = gisOverview?.waterSources || []
  const waterStatuses = gisOverview?.waterStatuses || []
  const allocations = gisOverview?.allocations || []

  const selectedWaterStatus = selectedField
    ? waterStatuses.find((s) => s.fieldId === selectedField.id)
    : null

  return (
    <Card className="shadow-xs border-border/80">
      <CardHeader className="pb-3 border-b border-border/60">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
          <div className="space-y-0.5">
            <CardTitle className="text-base font-bold text-foreground flex items-center gap-2">
              <Map className="w-5 h-5 text-primary" />
              <span>{t.title}</span>
            </CardTitle>
            <CardDescription className="text-xs">
              {t.subtitle}
            </CardDescription>
          </div>

          <Button
            variant="outline"
            size="sm"
            className="text-xs gap-1.5 self-start sm:self-auto"
            onClick={() => onNavigate('gis')}
          >
            <span>{t.openFullGis}</span>
            <ExternalLink className="w-3.5 h-3.5" />
          </Button>
        </div>
      </CardHeader>

      <CardContent className="p-0">
        <div className="grid grid-cols-1 lg:grid-cols-4">
          {/* Map View */}
          <div className="lg:col-span-3 min-h-[380px] h-[440px] relative border-b lg:border-b-0 lg:border-r border-border/60">
            {isLoading ? (
              <div className="h-full flex items-center justify-center bg-muted/20 text-xs text-muted-foreground">
                <span className="h-5 w-5 rounded-full border-2 border-primary border-t-transparent animate-spin mr-2" />
                GIS xaritasi yuklanmoqda...
              </div>
            ) : (
              <WaterFieldMap
                fields={fields}
                waterSources={waterSources}
                waterStatuses={waterStatuses}
                allocations={allocations}
                layers={{
                  showFields: true,
                  showWaterSources: true,
                  showWaterStatus: true,
                  showSupplyLinks: true,
                }}
                selectedField={selectedField}
                selectedWaterSource={null}
                onSelectField={setSelectedField}
                onSelectWaterSource={() => {}}
                lang={lang}
                height="440px"
              />
            )}
          </div>

          {/* Quick Parcel Status Panel */}
          <div className="p-4 space-y-4 flex flex-col justify-between bg-card/60">
            {selectedField ? (
              <div className="space-y-3.5">
                <div>
                  <Badge variant="outline" className="text-[10px] mb-1 py-0 px-1 font-mono">
                    {selectedField.cropType}
                  </Badge>
                  <h4 className="font-bold text-base text-foreground">
                    {selectedField.name}
                  </h4>
                  <p className="text-xs text-muted-foreground">
                    {selectedField.areaHectares} ha • {selectedField.soilType}
                  </p>
                </div>

                {/* Volumetric Quota Summary */}
                {selectedWaterStatus && (
                  <div className="p-3 rounded-lg bg-muted/40 border border-border/70 space-y-2 text-xs">
                    <div className="flex items-center justify-between">
                      <span className="text-muted-foreground">Sarflangan / Ajratilgan:</span>
                      <span className="font-bold font-mono text-foreground">
                        {Math.round(selectedWaterStatus.totalConsumedVolumeM3).toLocaleString()} / {Math.round(selectedWaterStatus.totalAllocatedVolumeM3).toLocaleString()} m³
                      </span>
                    </div>

                    <div className="w-full h-1.5 bg-muted rounded-full overflow-hidden">
                      <div
                        className={`h-full ${
                          selectedWaterStatus.isOverConsumed
                            ? 'bg-rose-500'
                            : selectedWaterStatus.consumptionPercentage > 80
                            ? 'bg-amber-500'
                            : 'bg-primary'
                        }`}
                        style={{ width: `${Math.min(100, selectedWaterStatus.consumptionPercentage)}%` }}
                      />
                    </div>

                    <div className="flex items-center justify-between text-[11px]">
                      <span className="text-muted-foreground">Mavjud qoldiq:</span>
                      <strong className="font-mono text-emerald-600 dark:text-emerald-400">
                        {Math.round(selectedWaterStatus.totalRemainingVolumeM3).toLocaleString()} m³
                      </strong>
                    </div>
                  </div>
                )}

                <div className="pt-1 space-y-2">
                  <Button
                    variant="default"
                    size="sm"
                    className="w-full text-xs gap-1.5 shadow-xs"
                    onClick={() => onNavigate('fields')}
                  >
                    <span>{t.openField}</span>
                    <ArrowRight className="w-3.5 h-3.5" />
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    className="w-full text-xs gap-1.5"
                    onClick={() => onNavigate('irrigation')}
                  >
                    <Droplets className="w-3.5 h-3.5 text-primary" />
                    <span>Sugʻorish Jurnali</span>
                  </Button>
                </div>
              </div>
            ) : (
              <div className="h-full flex flex-col items-center justify-center text-center p-4 space-y-2 text-muted-foreground">
                <Layers className="w-8 h-8 text-muted-foreground/60" />
                <p className="text-xs leading-relaxed">
                  {t.noSelection}
                </p>
              </div>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  )
}
