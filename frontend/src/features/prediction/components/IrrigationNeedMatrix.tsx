import React, { useState } from 'react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { Input } from '@/shared/ui/input'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/ui/table'
import {
  Search,
  ArrowRight,
  AlertTriangle,
  Compass,
} from 'lucide-react'
import { RankedFieldNeedItem, IrrigationLikelihood } from '@/entities/prediction'
import { SupportedLanguage } from '@/shared/i18n'
import { DataSufficiencyBadge } from './DataSufficiencyBadge'

interface IrrigationNeedMatrixProps {
  fields: RankedFieldNeedItem[]
  lang: SupportedLanguage
  onSelectField: (fieldId: string) => void
  onNavigateToIrrigation: (fieldId: string) => void
}

export const IrrigationNeedMatrix: React.FC<IrrigationNeedMatrixProps> = ({
  fields,
  lang,
  onSelectField,
  onNavigateToIrrigation,
}) => {
  const [searchTerm, setSearchTerm] = useState('')
  const [filterLikelihood, setFilterLikelihood] = useState<string>('ALL')

  const t = {
    uz: {
      title: 'Dala Sug‘orish Ehtiyoji Reytingi (Irrigation Need Ranking)',
      subtitle: 'MAD chegarasi, joriy namlik va agronomik defitsit asosida tartiblangan dalalar',
      searchPlaceholder: 'Dala nomi yoki ekin turi bo‘yicha qidirish...',
      all: 'Barcha holatlar',
      urgentOnly: 'Shoshilinch (Likely+)',
      fieldCol: 'Dala va Ekin',
      urgencyCol: 'Ehtiyoj Ehtimoli',
      moistureCol: 'Joriy / Prognoz Namlik',
      volumeCol: 'Kutilayotgan Suv',
      confidenceCol: 'Ishonchlilik',
      sufficiencyCol: 'Maʼlumot',
      actionCol: 'Amal',
      planNow: 'Rejalashtirish',
      noFields: 'Mos keluvchi dalalar topilmadi.',
      ha: 'ga',
    },
    ru: {
      title: 'Рейтинг Потребности Полей в Поливе (Irrigation Need Ranking)',
      subtitle: 'Ранжирование полей по порогу MAD, текущей влажности и дефициту',
      searchPlaceholder: 'Поиск по названию или культуре...',
      all: 'Все поля',
      urgentOnly: 'Срочные (Likely+)',
      fieldCol: 'Поле и Культура',
      urgencyCol: 'Вероятность потребности',
      moistureCol: 'Текущая / Прогноз',
      volumeCol: 'Ожидаемый объем',
      confidenceCol: 'Достоверность',
      sufficiencyCol: 'Данные',
      actionCol: 'Действие',
      planNow: 'Запланировать',
      noFields: 'Поля не найдены.',
      ha: 'га',
    },
    en: {
      title: 'Field Irrigation Need Ranking Matrix',
      subtitle: 'Fields prioritized by MAD threshold proximity, current moisture, and volumetric need',
      searchPlaceholder: 'Search field name or crop...',
      all: 'All Fields',
      urgentOnly: 'Urgent (Likely+)',
      fieldCol: 'Field & Crop',
      urgencyCol: 'Need Likelihood',
      moistureCol: 'Current / Forecast',
      volumeCol: 'Projected Volume',
      confidenceCol: 'Confidence',
      sufficiencyCol: 'Sufficiency',
      actionCol: 'Action',
      planNow: 'Schedule',
      noFields: 'No matching fields found.',
      ha: 'ha',
    },
  }[lang]

  const likelihoodBadge = (likelihood: IrrigationLikelihood) => {
    switch (likelihood) {
      case 'HIGHLY_LIKELY':
        return <Badge className="bg-rose-500 text-white border-rose-600 text-[10px]">O‘ta Yuqori (24h)</Badge>
      case 'LIKELY':
        return <Badge className="bg-orange-500 text-white border-orange-600 text-[10px]">Yuqori (48h)</Badge>
      case 'POSSIBLE':
        return <Badge className="bg-amber-500/10 text-amber-600 border-amber-500/20 text-[10px]">Mumkin (7d)</Badge>
      case 'LOW_LIKELIHOOD':
        return <Badge variant="outline" className="text-muted-foreground text-[10px]">Past</Badge>
      case 'NOT_EXPECTED':
        return <Badge className="bg-emerald-500/10 text-emerald-600 border-emerald-500/20 text-[10px]">Kutilmaydi</Badge>
      default:
        return <Badge variant="outline" className="text-muted-foreground text-[10px]">Nomaʼlum</Badge>
    }
  }

  const filtered = fields.filter((f) => {
    const matchesSearch =
      f.fieldName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      f.cropType.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesFilter =
      filterLikelihood === 'ALL'
        ? true
        : filterLikelihood === 'URGENT'
        ? f.likelihood === 'HIGHLY_LIKELY' || f.likelihood === 'LIKELY'
        : f.likelihood === filterLikelihood
    return matchesSearch && matchesFilter
  })

  return (
    <Card className="border-border shadow-xs bg-card">
      <CardHeader className="pb-3 border-b border-border/60">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <CardTitle className="text-sm sm:text-base font-bold flex items-center gap-2">
              <Compass className="h-4 w-4 text-primary" />
              <span>{t.title}</span>
            </CardTitle>
            <CardDescription className="text-xs text-muted-foreground">
              {t.subtitle}
            </CardDescription>
          </div>

          {/* Filter Toggles */}
          <div className="flex items-center gap-2">
            <div className="relative w-48 sm:w-60">
              <Search className="absolute left-2.5 top-2.5 h-3.5 w-3.5 text-muted-foreground" />
              <Input
                placeholder={t.searchPlaceholder}
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-8 text-xs h-8"
              />
            </div>
            <Button
              variant={filterLikelihood === 'URGENT' ? 'default' : 'outline'}
              size="sm"
              onClick={() => setFilterLikelihood(filterLikelihood === 'URGENT' ? 'ALL' : 'URGENT')}
              className="text-xs h-8 gap-1.5"
            >
              <AlertTriangle className="h-3 w-3" />
              <span>{t.urgentOnly}</span>
            </Button>
          </div>
        </div>
      </CardHeader>

      <CardContent className="p-0">
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow className="text-[11px] text-muted-foreground bg-muted/20">
                <TableHead>{t.fieldCol}</TableHead>
                <TableHead>{t.urgencyCol}</TableHead>
                <TableHead>{t.moistureCol}</TableHead>
                <TableHead>{t.volumeCol}</TableHead>
                <TableHead>{t.confidenceCol}</TableHead>
                <TableHead>{t.sufficiencyCol}</TableHead>
                <TableHead className="text-right">{t.actionCol}</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filtered.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className="h-24 text-center text-xs text-muted-foreground">
                    {t.noFields}
                  </TableCell>
                </TableRow>
              ) : (
                filtered.map((f) => (
                  <TableRow
                    key={f.fieldId}
                    className="hover:bg-muted/40 cursor-pointer text-xs transition-colors"
                    onClick={() => onSelectField(f.fieldId)}
                  >
                    <TableCell className="font-semibold text-foreground">
                      <div>{f.fieldName}</div>
                      <div className="text-[11px] text-muted-foreground font-normal">
                        {f.cropType} • {f.areaHectares} {t.ha}
                      </div>
                    </TableCell>

                    <TableCell>{likelihoodBadge(f.likelihood)}</TableCell>

                    <TableCell className="font-mono">
                      {f.currentMoisturePercent !== null ? (
                        <span>
                          {f.currentMoisturePercent}% →{' '}
                          <strong className={f.forecastMoisturePercent && f.forecastMoisturePercent < 22 ? 'text-rose-500' : 'text-foreground'}>
                            {f.forecastMoisturePercent}%
                          </strong>
                        </span>
                      ) : (
                        <span className="text-muted-foreground">—</span>
                      )}
                    </TableCell>

                    <TableCell className="font-mono font-medium">
                      {f.expectedVolumeM3 > 0 ? `${f.expectedVolumeM3.toLocaleString()} m³` : '0 m³'}
                    </TableCell>

                    <TableCell>
                      <Badge variant="outline" className="font-mono text-[10px] bg-primary/5 text-primary">
                        {f.confidencePercent}%
                      </Badge>
                    </TableCell>

                    <TableCell>
                      <DataSufficiencyBadge sufficiency={f.dataSufficiency} lang={lang} />
                    </TableCell>

                    <TableCell className="text-right" onClick={(e) => e.stopPropagation()}>
                      {(f.likelihood === 'HIGHLY_LIKELY' || f.likelihood === 'LIKELY') ? (
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => onNavigateToIrrigation(f.fieldId)}
                          className="h-7 text-[11px] gap-1 text-sky-600 dark:text-sky-400 hover:bg-sky-50 dark:hover:bg-sky-950/40"
                        >
                          <span>{t.planNow}</span>
                          <ArrowRight className="h-3 w-3" />
                        </Button>
                      ) : (
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => onSelectField(f.fieldId)}
                          className="h-7 text-[11px] text-muted-foreground hover:text-foreground"
                        >
                          <span>Batafsil</span>
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      </CardContent>
    </Card>
  )
}
