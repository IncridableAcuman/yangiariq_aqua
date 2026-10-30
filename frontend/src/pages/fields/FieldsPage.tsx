import React, { useState } from 'react'
import {
  Plus,
  Search,
  Droplets,
  Layers,
  CheckCircle2,
  Clock,
} from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { Input } from '@/shared/ui/input'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/shared/ui/dialog'
import { SupportedLanguage } from '@/shared/i18n'

interface FieldItem {
  id: string
  name: string
  crop: string
  area: number
  moisture: number
  targetMoisture: number
  canal: string
  lastIrrigated: string
  status: 'optimal' | 'warning' | 'critical'
  sensorId: string
}

const INITIAL_FIELDS: FieldItem[] = [
  {
    id: 'f-1',
    name: 'Dala №01 (Yangiariq Markaz)',
    crop: 'Paxta (Gʻoʻza)',
    area: 18.5,
    moisture: 68,
    targetMoisture: 70,
    canal: 'Yangiariq Bosh Kanali',
    lastIrrigated: '2 kun oldin',
    status: 'optimal',
    sensorId: 'SENSOR-ST-01',
  },
  {
    id: 'f-2',
    name: 'Dala №04 (Shix-Ariq boʻyi)',
    crop: 'Bugʻdoy',
    area: 24.0,
    moisture: 52,
    targetMoisture: 65,
    canal: 'Shix-Ariq Tarmogʻi',
    lastIrrigated: '4 kun oldin',
    status: 'warning',
    sensorId: 'SENSOR-ST-04',
  },
  {
    id: 'f-3',
    name: 'Dala №07 (Qatagʻon sektori)',
    crop: 'Paxta (Gʻoʻza)',
    area: 32.0,
    moisture: 74,
    targetMoisture: 70,
    canal: 'Qatagʻon Kanali',
    lastIrrigated: 'Kecha',
    status: 'optimal',
    sensorId: 'SENSOR-ST-07',
  },
  {
    id: 'f-4',
    name: 'Dala №09 (Bogʻot chegarasi)',
    crop: 'Poliz ekinlari',
    area: 12.0,
    moisture: 46,
    targetMoisture: 65,
    canal: 'Yangiariq Bosh Kanali',
    lastIrrigated: '5 kun oldin',
    status: 'critical',
    sensorId: 'SENSOR-ST-09',
  },
  {
    id: 'f-5',
    name: 'Dala №12 (Chigʻirchi mavzesi)',
    crop: 'Sholi maydoni',
    area: 15.0,
    moisture: 85,
    targetMoisture: 85,
    canal: 'Chigʻirchi Tarmoq Posti',
    lastIrrigated: 'Bugun 08:00',
    status: 'optimal',
    sensorId: 'SENSOR-ST-12',
  },
  {
    id: 'f-6',
    name: 'Dala №15 (Guliston boʻlimi)',
    crop: 'Makkajoʻxori',
    area: 16.5,
    moisture: 61,
    targetMoisture: 65,
    canal: 'Shix-Ariq Tarmogʻi',
    lastIrrigated: '3 kun oldin',
    status: 'optimal',
    sensorId: 'SENSOR-ST-15',
  },
]

interface FieldsPageProps {
  lang: SupportedLanguage
}

export const FieldsPage: React.FC<FieldsPageProps> = ({ lang }) => {
  const [fields, setFields] = useState<FieldItem[]>(INITIAL_FIELDS)
  const [searchQuery, setSearchQuery] = useState('')
  const [selectedCrop, setSelectedCrop] = useState<string>('all')
  const [dialogOpen, setDialogOpen] = useState(false)

  // New Field Form State
  const [newFieldName, setNewFieldName] = useState('')
  const [newCrop, setNewCrop] = useState('Paxta')
  const [newArea, setNewArea] = useState('10')
  const [newCanal, setNewCanal] = useState('Yangiariq Bosh Kanali')

  const handleAddField = (e: React.FormEvent) => {
    e.preventDefault()
    if (!newFieldName.trim()) return

    const newField: FieldItem = {
      id: `f-${fields.length + 1}`,
      name: newFieldName.trim(),
      crop: newCrop,
      area: parseFloat(newArea) || 10,
      moisture: 60,
      targetMoisture: 65,
      canal: newCanal,
      lastIrrigated: 'Kutilmoqda',
      status: 'optimal',
      sensorId: `SENSOR-ST-${String(fields.length + 1).padStart(2, '0')}`,
    }

    setFields([newField, ...fields])
    setNewFieldName('')
    setDialogOpen(false)
  }

  const filteredFields = fields.filter((f) => {
    const matchesQuery =
      f.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      f.crop.toLowerCase().includes(searchQuery.toLowerCase()) ||
      f.canal.toLowerCase().includes(searchQuery.toLowerCase())

    if (selectedCrop === 'all') return matchesQuery
    return matchesQuery && f.crop.toLowerCase().includes(selectedCrop.toLowerCase())
  })

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-foreground">
            {lang === 'uz' ? 'Dala maydonlari boshqaruvi' : 'Управление полями'}
          </h2>
          <p className="text-xs text-muted-foreground mt-0.5">
            {lang === 'uz'
              ? 'Yangiariq tumani boʻyicha biriktirilgan ekin maydonlari va datchiklar nazorati'
              : 'Закрепленные поля и мониторинг датчиков в Янгиарыкском районе'}
          </p>
        </div>

        {/* Add Field Dialog */}
        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger asChild>
            <Button size="sm" className="gap-1.5 shadow-xs">
              <Plus className="h-4 w-4" />
              <span>{lang === 'uz' ? 'Yangi dala qoʻshish' : 'Добавить поле'}</span>
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>
                {lang === 'uz' ? 'Yangi dala maydonini roʻyxatga olish' : 'Регистрация нового поля'}
              </DialogTitle>
              <DialogDescription>
                {lang === 'uz'
                  ? 'Ekin maydoni va suv manbai maʼlumotlarini kiriting'
                  : 'Введите параметры поля и источник водоснабжения'}
              </DialogDescription>
            </DialogHeader>

            <form onSubmit={handleAddField} className="space-y-4 py-2">
              <Input
                label={lang === 'uz' ? 'Dala nomi / Raqami' : 'Название / Номер поля'}
                placeholder="Masalan: Dala №22 (Shix-Ariq boʻyi)"
                value={newFieldName}
                onChange={(e) => setNewFieldName(e.target.value)}
                required
              />

              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-foreground">
                    {lang === 'uz' ? 'Ekin turi' : 'Культура'}
                  </label>
                  <select
                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    value={newCrop}
                    onChange={(e) => setNewCrop(e.target.value)}
                  >
                    <option value="Paxta (Gʻoʻza)">Paxta (Gʻoʻza)</option>
                    <option value="Bugʻdoy">Bugʻdoy</option>
                    <option value="Sholi">Sholi</option>
                    <option value="Makkajoʻxori">Makkajoʻxori</option>
                    <option value="Poliz ekinlari">Poliz ekinlari</option>
                  </select>
                </div>

                <Input
                  label={lang === 'uz' ? 'Maydoni (ga)' : 'Площадь (га)'}
                  type="number"
                  step="0.5"
                  value={newArea}
                  onChange={(e) => setNewArea(e.target.value)}
                  required
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-foreground">
                  {lang === 'uz' ? 'Suv taʼminoti kanali' : 'Канал водоснабжения'}
                </label>
                <select
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                  value={newCanal}
                  onChange={(e) => setNewCanal(e.target.value)}
                >
                  <option value="Yangiariq Bosh Kanali">Yangiariq Bosh Kanali</option>
                  <option value="Shix-Ariq Tarmogʻi">Shix-Ariq Tarmogʻi</option>
                  <option value="Qatagʻon Kanali">Qatagʻon Kanali</option>
                  <option value="Chigʻirchi Tarmoq Posti">Chigʻirchi Tarmoq Posti</option>
                </select>
              </div>

              <DialogFooter className="pt-2">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setDialogOpen(false)}
                >
                  {lang === 'uz' ? 'Bekor qilish' : 'Отмена'}
                </Button>
                <Button type="submit">
                  {lang === 'uz' ? 'Saqlash' : 'Сохранить'}
                </Button>
              </DialogFooter>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      {/* Filter and Search Bar */}
      <Card>
        <CardContent className="p-4 flex flex-col sm:flex-row gap-3 items-center justify-between">
          <div className="w-full sm:w-72">
            <Input
              placeholder={lang === 'uz' ? 'Dala yoki ekin qidirish...' : 'Поиск поля...'}
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              leftIcon={<Search className="h-4 w-4" />}
            />
          </div>

          <div className="flex items-center gap-1.5 w-full sm:w-auto overflow-x-auto pb-1 sm:pb-0">
            {['all', 'Paxta', 'Bugʻdoy', 'Poliz', 'Sholi'].map((cropKey) => (
              <Button
                key={cropKey}
                variant={selectedCrop === cropKey ? 'default' : 'outline'}
                size="sm"
                onClick={() => setSelectedCrop(cropKey)}
                className="text-xs shrink-0"
              >
                {cropKey === 'all' ? (lang === 'uz' ? 'Barchasi' : 'Все') : cropKey}
              </Button>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Fields Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
        {filteredFields.map((field) => {
          const isOptimal = field.status === 'optimal'
          const isWarning = field.status === 'warning'
          const isCritical = field.status === 'critical'

          return (
            <Card
              key={field.id}
              className="hover:border-primary/50 transition-all shadow-xs flex flex-col justify-between"
            >
              <CardHeader className="pb-3">
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <CardTitle className="text-base font-bold text-foreground leading-snug">
                      {field.name}
                    </CardTitle>
                    <CardDescription className="flex items-center gap-1.5 mt-1">
                      <Layers className="h-3.5 w-3.5 text-primary" />
                      <span>{field.crop}</span> • <span>{field.area} ga</span>
                    </CardDescription>
                  </div>

                  <Badge
                    variant="outline"
                    className={
                      isOptimal
                        ? 'bg-emerald-500/15 text-emerald-700 dark:text-emerald-300 border-emerald-500/30'
                        : isWarning
                        ? 'bg-amber-500/15 text-amber-700 dark:text-amber-300 border-amber-500/30'
                        : 'bg-destructive/15 text-destructive border-destructive/30'
                    }
                  >
                    {isOptimal
                      ? (lang === 'uz' ? 'Optimal' : 'Норма')
                      : isWarning
                      ? (lang === 'uz' ? 'Eʼtibor kerak' : 'Внимание')
                      : (lang === 'uz' ? 'Sugʻorish zarur' : 'Требует полива')}
                  </Badge>
                </div>
              </CardHeader>

              <CardContent className="space-y-4">
                {/* Moisture Bar */}
                <div className="space-y-1.5 bg-muted/40 p-3 rounded-xl border border-border/60">
                  <div className="flex items-center justify-between text-xs">
                    <span className="text-muted-foreground font-medium flex items-center gap-1">
                      <Droplets className="h-3.5 w-3.5 text-hydro-600" />
                      {lang === 'uz' ? 'Tuproq namligi' : 'Влажность'}:
                    </span>
                    <span className="font-bold font-mono text-sm">
                      {field.moisture}% / {field.targetMoisture}%
                    </span>
                  </div>

                  <div className="w-full h-2 bg-muted rounded-full overflow-hidden">
                    <div
                      className={`h-full rounded-full transition-all duration-500 ${
                        isCritical
                          ? 'bg-destructive'
                          : isWarning
                          ? 'bg-amber-500'
                          : 'bg-emerald-500'
                      }`}
                      style={{ width: `${Math.min(field.moisture, 100)}%` }}
                    />
                  </div>
                </div>

                {/* Canal & Sensor Specs */}
                <div className="grid grid-cols-2 gap-2 text-xs">
                  <div className="space-y-0.5">
                    <span className="text-muted-foreground text-[11px] block">
                      {lang === 'uz' ? 'Suv taʼminoti' : 'Канал'}:
                    </span>
                    <span className="font-semibold text-foreground truncate block">
                      {field.canal}
                    </span>
                  </div>

                  <div className="space-y-0.5">
                    <span className="text-muted-foreground text-[11px] block">
                      {lang === 'uz' ? 'Datchik ID' : 'Датчик'}:
                    </span>
                    <span className="font-mono text-primary font-semibold truncate block">
                      {field.sensorId}
                    </span>
                  </div>
                </div>

                <div className="flex items-center justify-between pt-2 border-t border-border/60 text-xs text-muted-foreground">
                  <span className="flex items-center gap-1">
                    <Clock className="h-3.5 w-3.5" />
                    {field.lastIrrigated}
                  </span>

                  <span className="font-medium text-emerald-600 flex items-center gap-1">
                    <CheckCircle2 className="h-3.5 w-3.5" />
                    IoT telemetriya faol
                  </span>
                </div>
              </CardContent>
            </Card>
          )
        })}
      </div>
    </div>
  )
}
