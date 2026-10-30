import React, { useState } from 'react'
import { Plus } from 'lucide-react'
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
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/shared/ui/dialog'
import { SupportedLanguage } from '@/shared/i18n'

interface IrrigationRecord {
  id: string
  fieldName: string
  date: string
  volume: number
  duration: string
  canal: string
  status: 'COMPLETED' | 'SCHEDULED' | 'IN_PROGRESS'
}

const INITIAL_RECORDS: IrrigationRecord[] = [
  {
    id: 'IRR-101',
    fieldName: 'Dala №01 (Yangiariq Markaz)',
    date: '2026-10-28',
    volume: 1450,
    duration: '4 soat 30 daqiqa',
    canal: 'Yangiariq Bosh Kanali',
    status: 'COMPLETED',
  },
  {
    id: 'IRR-102',
    fieldName: 'Dala №04 (Shix-Ariq boʻyi)',
    date: '2026-10-30',
    volume: 1200,
    duration: 'Bugun, 22:00 - 04:00',
    canal: 'Shix-Ariq Tarmogʻi',
    status: 'SCHEDULED',
  },
  {
    id: 'IRR-103',
    fieldName: 'Dala №07 (Qatagʻon sektori)',
    date: '2026-10-29',
    volume: 1800,
    duration: '5 soat',
    canal: 'Qatagʻon Kanali',
    status: 'COMPLETED',
  },
  {
    id: 'IRR-104',
    fieldName: 'Dala №12 (Chigʻirchi mavzesi)',
    date: '2026-10-30',
    volume: 900,
    duration: 'Hozir davom etmoqda',
    canal: 'Chigʻirchi Tarmoq Posti',
    status: 'IN_PROGRESS',
  },
]

interface IrrigationPageProps {
  lang: SupportedLanguage
}

export const IrrigationPage: React.FC<IrrigationPageProps> = ({ lang }) => {
  const [records, setRecords] = useState<IrrigationRecord[]>(INITIAL_RECORDS)
  const [dialogOpen, setDialogOpen] = useState(false)

  // Form State
  const [fieldName, setFieldName] = useState('Dala №01 (Yangiariq Markaz)')
  const [volume, setVolume] = useState('1200')
  const [date, setDate] = useState('2026-10-31')
  const [canal, setCanal] = useState('Yangiariq Bosh Kanali')

  const handleAddSchedule = (e: React.FormEvent) => {
    e.preventDefault()
    const newRecord: IrrigationRecord = {
      id: `IRR-${105 + records.length}`,
      fieldName,
      date,
      volume: parseInt(volume, 10) || 1000,
      duration: 'Belgilangan reja',
      canal,
      status: 'SCHEDULED',
    }
    setRecords([newRecord, ...records])
    setDialogOpen(false)
  }

  return (
    <div className="space-y-6">
      {/* Page Title & Action */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-foreground">
            {lang === 'uz' ? 'Suv taqsimoti va sugʻorish jurnali' : 'Водораспределение и журнал полива'}
          </h2>
          <p className="text-xs text-muted-foreground mt-0.5">
            {lang === 'uz'
              ? 'Yangiariq tumani kanallari boʻyicha suv sarfi kvotasi va navbatdagi sugʻorish jadvallari'
              : 'Квоты расхода воды по каналам Янгиарыка и расписание поливов'}
          </p>
        </div>

        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger asChild>
            <Button size="sm" className="gap-1.5 shadow-xs">
              <Plus className="h-4 w-4" />
              <span>{lang === 'uz' ? 'Yangi sugʻorish soʻrovi' : 'Запрос на полив'}</span>
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>
                {lang === 'uz' ? 'Sugʻorish soʻrovini shakllantirish' : 'Создать заявку на полив'}
              </DialogTitle>
              <DialogDescription>
                {lang === 'uz'
                  ? 'Kanal tizimidan suv olish navbati uchun parametrlar'
                  : 'Параметры для очереди забора воды из канала'}
              </DialogDescription>
            </DialogHeader>

            <form onSubmit={handleAddSchedule} className="space-y-4 py-2">
              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-foreground">
                  {lang === 'uz' ? 'Dala maydoni' : 'Поле'}
                </label>
                <select
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                  value={fieldName}
                  onChange={(e) => setFieldName(e.target.value)}
                >
                  <option value="Dala №01 (Yangiariq Markaz)">Dala №01 (Yangiariq Markaz)</option>
                  <option value="Dala №04 (Shix-Ariq boʻyi)">Dala №04 (Shix-Ariq boʻyi)</option>
                  <option value="Dala №07 (Qatagʻon sektori)">Dala №07 (Qatagʻon sektori)</option>
                  <option value="Dala №12 (Chigʻirchi mavzesi)">Dala №12 (Chigʻirchi mavzesi)</option>
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <Input
                  label={lang === 'uz' ? 'Talab etiladigan suv (m³)' : 'Объем воды (м³)'}
                  type="number"
                  value={volume}
                  onChange={(e) => setVolume(e.target.value)}
                  required
                />
                <Input
                  label={lang === 'uz' ? 'Rejalashtirilgan sana' : 'Дата полива'}
                  type="date"
                  value={date}
                  onChange={(e) => setDate(e.target.value)}
                  required
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-foreground">
                  {lang === 'uz' ? 'Magistral kanal' : 'Канал'}
                </label>
                <select
                  className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                  value={canal}
                  onChange={(e) => setCanal(e.target.value)}
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
                  {lang === 'uz' ? 'Yuborish' : 'Отправить'}
                </Button>
              </DialogFooter>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      {/* Water Quota & Analytics Header */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-xs uppercase font-semibold text-muted-foreground">
              {lang === 'uz' ? 'Mavsumiy suv kvotasi' : 'Сезонная квота'}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold font-mono">48,000 m³</div>
            <div className="text-xs text-muted-foreground mt-1">
              Yangiariq tumani suv xoʻjaligi tomonidan ajratilgan
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-xs uppercase font-semibold text-muted-foreground">
              {lang === 'uz' ? 'Hozirgi foydalanilgan' : 'Фактический расход'}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold font-mono text-primary">31,450 m³</div>
            <div className="w-full h-1.5 bg-muted rounded-full mt-2 overflow-hidden">
              <div className="h-full bg-primary rounded-full" style={{ width: '65%' }} />
            </div>
            <div className="flex justify-between text-[11px] text-muted-foreground mt-1 font-mono">
              <span>65.5% ishlatildi</span>
              <span>16,550 m³ qoldi</span>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-xs uppercase font-semibold text-muted-foreground">
              {lang === 'uz' ? 'AI optimallashtirilgan tejov' : 'Сэкономлено с ИИ'}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold font-mono text-emerald-600">+4,230 m³</div>
            <div className="text-xs text-emerald-600 font-semibold mt-1">
              {lang === 'uz' ? 'Tungi sugʻorish orqali tejaldi' : 'Сэкономлено за счет ночного полива'}
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Table of Records */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base font-bold">
            {lang === 'uz' ? 'Sugʻorish jurnali yozuvlari' : 'Журнал проведенных и запланированных поливов'}
          </CardTitle>
          <CardDescription>
            {lang === 'uz'
              ? 'Har bir dala boʻyicha suv hisoblagich telemetriyasi va navbat holati'
              : 'Телеметрия водомеров и статус очереди по каждому полю'}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>{lang === 'uz' ? 'Kod' : 'Код'}</TableHead>
                <TableHead>{lang === 'uz' ? 'Dala maydoni' : 'Поле'}</TableHead>
                <TableHead>{lang === 'uz' ? 'Sana & Vaqt' : 'Дата и время'}</TableHead>
                <TableHead>{lang === 'uz' ? 'Hajm (m³)' : 'Объем (м³)'}</TableHead>
                <TableHead>{lang === 'uz' ? 'Kanal manbai' : 'Канал'}</TableHead>
                <TableHead className="text-right">{lang === 'uz' ? 'Holat' : 'Статус'}</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {records.map((r) => (
                <TableRow key={r.id}>
                  <TableCell className="font-mono text-xs font-semibold text-muted-foreground">
                    {r.id}
                  </TableCell>
                  <TableCell className="font-medium text-foreground">
                    {r.fieldName}
                  </TableCell>
                  <TableCell className="text-xs text-muted-foreground">
                    <div>{r.date}</div>
                    <div className="text-[11px] font-mono">{r.duration}</div>
                  </TableCell>
                  <TableCell className="font-mono font-bold text-foreground">
                    {r.volume.toLocaleString()} m³
                  </TableCell>
                  <TableCell className="text-xs text-muted-foreground">
                    {r.canal}
                  </TableCell>
                  <TableCell className="text-right">
                    {r.status === 'COMPLETED' && (
                      <Badge variant="outline" className="bg-emerald-500/15 text-emerald-700 border-emerald-500/30 text-xs">
                        {lang === 'uz' ? 'Yakunlangan' : 'Завершен'}
                      </Badge>
                    )}
                    {r.status === 'SCHEDULED' && (
                      <Badge variant="outline" className="bg-sky-500/15 text-sky-700 border-sky-500/30 text-xs">
                        {lang === 'uz' ? 'Rejalashtirilgan' : 'В очереди'}
                      </Badge>
                    )}
                    {r.status === 'IN_PROGRESS' && (
                      <Badge variant="outline" className="bg-amber-500/15 text-amber-700 border-amber-500/30 text-xs animate-pulse">
                        {lang === 'uz' ? 'Suv oqmoqda' : 'Идет полив'}
                      </Badge>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  )
}
