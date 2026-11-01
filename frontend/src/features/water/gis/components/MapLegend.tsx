import React, { useState } from 'react'
import { ChevronDown, ChevronUp, Layers } from 'lucide-react'
import { WATER_STATUS_STYLES, WATER_SOURCE_COLORS } from '../gisWaterUtils'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { Button } from '@/shared/ui/button'
import { cn } from '@/shared/lib/utils'

interface MapLegendProps {
  lang: SupportedLanguage
  className?: string
}

export const MapLegend: React.FC<MapLegendProps> = ({ lang, className }) => {
  const [isCollapsed, setIsCollapsed] = useState(false)
  const t = translations[lang]

  return (
    <div
      className={cn(
        'bg-background/95 dark:bg-card/95 backdrop-blur-md border border-border/80 shadow-md rounded-lg p-3 text-xs w-56 select-none transition-all',
        className
      )}
      aria-label="Map Legend"
    >
      <div className="flex items-center justify-between font-semibold pb-1.5 border-b border-border/60">
        <span className="flex items-center gap-1.5 text-foreground">
          <Layers className="w-3.5 h-3.5 text-primary" />
          {t.gis?.legend?.title || 'Xarita shartli belgilari'}
        </span>
        <Button
          variant="ghost"
          size="sm"
          className="h-5 w-5 p-0"
          onClick={() => setIsCollapsed(!isCollapsed)}
          aria-label={isCollapsed ? 'Expand legend' : 'Collapse legend'}
        >
          {isCollapsed ? <ChevronUp className="w-3 h-3" /> : <ChevronDown className="w-3 h-3" />}
        </Button>
      </div>

      {!isCollapsed && (
        <div className="mt-2 space-y-3">
          {/* Water Status Section */}
          <div>
            <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground block mb-1.5">
              {t.gis?.legend?.waterStatus || 'Suv taʼminoti holati'}
            </span>
            <div className="space-y-1.5">
              <div className="flex items-center gap-2">
                <span
                  className="w-3 h-3 rounded-sm border shrink-0"
                  style={{
                    backgroundColor: WATER_STATUS_STYLES.NORMAL.fillColor,
                    borderColor: WATER_STATUS_STYLES.NORMAL.color,
                  }}
                />
                <span className="text-foreground">
                  {t.gis?.status?.normal || 'Normal (≤80%)'}
                </span>
              </div>
              <div className="flex items-center gap-2">
                <span
                  className="w-3 h-3 rounded-sm border shrink-0"
                  style={{
                    backgroundColor: WATER_STATUS_STYLES.WARNING.fillColor,
                    borderColor: WATER_STATUS_STYLES.WARNING.color,
                  }}
                />
                <span className="text-foreground">
                  {t.gis?.status?.warning || 'Ogohlantirish (80–100%)'}
                </span>
              </div>
              <div className="flex items-center gap-2">
                <span
                  className="w-3 h-3 rounded-sm border shrink-0"
                  style={{
                    backgroundColor: WATER_STATUS_STYLES.OVER_CONSUMED.fillColor,
                    borderColor: WATER_STATUS_STYLES.OVER_CONSUMED.color,
                  }}
                />
                <span className="text-rose-600 dark:text-rose-400 font-medium">
                  {t.gis?.status?.overConsumed || 'Meʼyordan ortiq (>100%)'}
                </span>
              </div>
              <div className="flex items-center gap-2">
                <span
                  className="w-3 h-3 rounded-sm border shrink-0"
                  style={{
                    backgroundColor: WATER_STATUS_STYLES.NO_ALLOCATION.fillColor,
                    borderColor: WATER_STATUS_STYLES.NO_ALLOCATION.color,
                  }}
                />
                <span className="text-muted-foreground">
                  {t.gis?.status?.noAllocation || 'Limit ajratilmagan'}
                </span>
              </div>
            </div>
          </div>

          {/* Water Sources Section */}
          <div className="pt-2 border-t border-border/50">
            <span className="text-[10px] uppercase font-bold tracking-wider text-muted-foreground block mb-1.5">
              {t.gis?.legend?.waterSources || 'Suv manbalari'}
            </span>
            <div className="grid grid-cols-2 gap-1.5">
              {Object.entries(WATER_SOURCE_COLORS).map(([type, color]) => (
                <div key={type} className="flex items-center gap-1.5">
                  <span
                    className="w-2.5 h-2.5 rounded-full shrink-0"
                    style={{ backgroundColor: color }}
                  />
                  <span className="text-[11px] text-foreground truncate">
                    {t.water.sourceTypes[type as keyof typeof t.water.sourceTypes] || type}
                  </span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
