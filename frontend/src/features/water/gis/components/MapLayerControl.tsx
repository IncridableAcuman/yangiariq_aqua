import React, { useState } from 'react'
import { Eye, ChevronDown, ChevronUp } from 'lucide-react'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { Button } from '@/shared/ui/button'
import { cn } from '@/shared/lib/utils'

export interface MapLayerState {
  showFields: boolean
  showWaterSources: boolean
  showWaterStatus: boolean
  showSupplyLinks: boolean
}

interface MapLayerControlProps {
  layers: MapLayerState
  onChange: (layers: MapLayerState) => void
  lang: SupportedLanguage
  className?: string
}

export const MapLayerControl: React.FC<MapLayerControlProps> = ({
  layers,
  onChange,
  lang,
  className,
}) => {
  const [isCollapsed, setIsCollapsed] = useState(false)
  const t = translations[lang]

  const handleToggle = (key: keyof MapLayerState) => {
    onChange({
      ...layers,
      [key]: !layers[key],
    })
  }

  return (
    <div
      className={cn(
        'bg-background/95 dark:bg-card/95 backdrop-blur-md border border-border/80 shadow-md rounded-lg p-3 text-xs w-52 select-none transition-all',
        className
      )}
      aria-label="Map Layer Controls"
    >
      <div className="flex items-center justify-between font-semibold pb-1.5 border-b border-border/60">
        <span className="flex items-center gap-1.5 text-foreground">
          <Eye className="w-3.5 h-3.5 text-primary" />
          {t.gis?.layers?.title || 'Qatlamlar'}
        </span>
        <Button
          variant="ghost"
          size="sm"
          className="h-5 w-5 p-0"
          onClick={() => setIsCollapsed(!isCollapsed)}
          aria-label={isCollapsed ? 'Expand layer control' : 'Collapse layer control'}
        >
          {isCollapsed ? <ChevronUp className="w-3 h-3" /> : <ChevronDown className="w-3 h-3" />}
        </Button>
      </div>

      {!isCollapsed && (
        <div className="mt-2 space-y-2">
          <label className="flex items-center gap-2 cursor-pointer hover:text-foreground">
            <input
              type="checkbox"
              checked={layers.showFields}
              onChange={() => handleToggle('showFields')}
              className="rounded border-input text-primary focus:ring-primary w-3.5 h-3.5"
            />
            <span className="text-foreground">{t.gis?.layers?.fields || 'Dala maydonlari'}</span>
          </label>

          <label className="flex items-center gap-2 cursor-pointer hover:text-foreground">
            <input
              type="checkbox"
              checked={layers.showWaterSources}
              onChange={() => handleToggle('showWaterSources')}
              className="rounded border-input text-primary focus:ring-primary w-3.5 h-3.5"
            />
            <span className="text-foreground">{t.gis?.layers?.waterSources || 'Suv manbalari'}</span>
          </label>

          <label className="flex items-center gap-2 cursor-pointer hover:text-foreground">
            <input
              type="checkbox"
              checked={layers.showWaterStatus}
              onChange={() => handleToggle('showWaterStatus')}
              className="rounded border-input text-primary focus:ring-primary w-3.5 h-3.5"
            />
            <span className="text-foreground">{t.gis?.layers?.waterStatus || 'Suv taʼminoti holati'}</span>
          </label>

          <label className="flex items-center gap-2 cursor-pointer hover:text-foreground">
            <input
              type="checkbox"
              checked={layers.showSupplyLinks}
              onChange={() => handleToggle('showSupplyLinks')}
              className="rounded border-input text-primary focus:ring-primary w-3.5 h-3.5"
            />
            <span className="text-foreground">{t.gis?.layers?.supplyLinks || 'Suv tarmoqlari bogʻlanishi'}</span>
          </label>
        </div>
      )}
    </div>
  )
}
