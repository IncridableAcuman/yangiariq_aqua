import React, { useState } from 'react'
import {
  AlertTriangle,
  Plus,
  Clock,
  MapPin,
  PhoneCall,
} from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/ui/card'
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

interface ProblemReport {
  id: string
  title: string
  type: string
  location: string
  canal: string
  urgency: 'HIGH' | 'MEDIUM' | 'LOW'
  status: 'PENDING' | 'IN_REVIEW' | 'RESOLVED'
  reportedAt: string
  description: string
}

const INITIAL_PROBLEMS: ProblemReport[] = [
  {
    id: 'PR-2026-08',
    title: 'Shix-Ariq kanalida shox-shabba toʻplanib qolishi',
    type: 'Kanal toʻsilib qolishi',
    location: 'Shix-Ariq, Dala №04 kirish qismi',
    canal: 'Shix-Ariq Tarmogʻi',
    urgency: 'HIGH',
    status: 'IN_REVIEW',
    reportedAt: 'Bugun, 09:30',
    description: 'Suv oqimi 30% ga pasayib ketgan. Shlyuz oldida tozalash talab etiladi.',
  },
  {
    id: 'PR-2026-07',
    title: 'Dala №09 datchik batareyasi past',
    type: 'Datchik nosozligi',
    location: 'Dala №09 poliz uchastkasi',
    canal: 'Yangiariq Bosh Kanali',
    urgency: 'LOW',
    status: 'PENDING',
    reportedAt: 'Kecha, 16:45',
    description: 'Telemetriya datchigi quvvati 34% ga tushgan. Texnik koʻrik zarur.',
  },
  {
    id: 'PR-2026-05',
    title: 'Qatagʻon kanali 14-pikketda suv sathining pasayishi',
    type: 'Suv yetishmovchiligi',
    location: 'Qatagʻon kanali, 14-pikket',
    canal: 'Qatagʻon Kanali',
    urgency: 'MEDIUM',
    status: 'RESOLVED',
    reportedAt: '28-oktyabr',
    description: 'Gidrouzel bosh shlyuzi rostlandi va suv oqimi normallashtirildi.',
  },
]

interface ProblemsPageProps {
  lang: SupportedLanguage
}

export const ProblemsPage: React.FC<ProblemsPageProps> = ({ lang }) => {
  const [problems, setProblems] = useState<ProblemReport[]>(INITIAL_PROBLEMS)
  const [dialogOpen, setDialogOpen] = useState(false)

  // Form State
  const [title, setTitle] = useState('')
  const [type, setType] = useState('Kanal toʻsilib qolishi')
  const [location, setLocation] = useState('')
  const [canal, setCanal] = useState('Yangiariq Bosh Kanali')
  const [urgency, setUrgency] = useState<'HIGH' | 'MEDIUM' | 'LOW'>('MEDIUM')
  const [description, setDescription] = useState('')

  const handleCreateReport = (e: React.FormEvent) => {
    e.preventDefault()
    if (!title.trim() || !description.trim()) return

    const newReport: ProblemReport = {
      id: `PR-2026-${String(problems.length + 9).padStart(2, '0')}`,
      title: title.trim(),
      type,
      location: location.trim() || 'Yangiariq tumani',
      canal,
      urgency,
      status: 'PENDING',
      reportedAt: 'Hozirgina',
      description: description.trim(),
    }

    setProblems([newReport, ...problems])
    setTitle('')
    setLocation('')
    setDescription('')
    setDialogOpen(false)
  }

  return (
    <div className="space-y-6">
      {/* Title & Action */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-foreground flex items-center gap-2">
            <AlertTriangle className="h-6 w-6 text-amber-500" />
            <span>{lang === 'uz' ? 'Suv Muammolari va Murojaatlar' : 'Проблемы и инциденты водораспределения'}</span>
          </h2>
          <p className="text-xs text-muted-foreground mt-0.5">
            {lang === 'uz'
              ? 'Kanal toʻsilishi, suv bosimi pasayishi va texnik nosozliklar haqida tezkor xabar berish'
              : 'Оперативные сообщения о засорах каналов, утечках и снижении расхода воды'}
          </p>
        </div>

        <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
          <DialogTrigger asChild>
            <Button size="sm" className="gap-1.5 shadow-xs">
              <Plus className="h-4 w-4" />
              <span>{lang === 'uz' ? 'Yangi murojaat yuborish' : 'Сообщить о проблеме'}</span>
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>
                {lang === 'uz' ? 'Suv muammosi haqida xabar berish' : 'Создать инцидент по воде'}
              </DialogTitle>
              <DialogDescription>
                {lang === 'uz'
                  ? 'Yangiariq tumani suv xoʻjaligi dispetcheriga tezkor xabarnoma joʻnatiladi'
                  : 'Заявка будет направлена диспетчеру водного хозяйства'}
              </DialogDescription>
            </DialogHeader>

            <form onSubmit={handleCreateReport} className="space-y-4 py-2">
              <Input
                label={lang === 'uz' ? 'Muammo mavzusi' : 'Тема инцидента'}
                placeholder="Masalan: Kanal toʻsilib qolishi yoki suv kelmasligi"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                required
              />

              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-foreground">
                    {lang === 'uz' ? 'Muammo toifasi' : 'Категория'}
                  </label>
                  <select
                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    value={type}
                    onChange={(e) => setType(e.target.value)}
                  >
                    <option value="Kanal toʻsilib qolishi">Kanal toʻsilib qolishi</option>
                    <option value="Suv yetishmovchiligi">Suv yetishmovchiligi</option>
                    <option value="Nasos / Shlyuz nosozligi">Nasos / Shlyuz nosozligi</option>
                    <option value="Datchik nosozligi">Datchik nosozligi</option>
                    <option value="Noqonuniy suv olish">Noqonuniy suv olish</option>
                  </select>
                </div>

                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-foreground">
                    {lang === 'uz' ? 'Shoshilinchlik darajasi' : 'Срочность'}
                  </label>
                  <select
                    className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                    value={urgency}
                    onChange={(e) => setUrgency(e.target.value as any)}
                  >
                    <option value="HIGH">Yuqori (Kritik)</option>
                    <option value="MEDIUM">Oʻrta</option>
                    <option value="LOW">Past</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-foreground">
                    {lang === 'uz' ? 'Kanal nomi' : 'Канал'}
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

                <Input
                  label={lang === 'uz' ? 'Aniq joylashuv' : 'Локация'}
                  placeholder="Masalan: 12-pikket yoki Shix-Ariq koʻprigi"
                  value={location}
                  onChange={(e) => setLocation(e.target.value)}
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-foreground">
                  {lang === 'uz' ? 'Batafsil tavsif' : 'Описание проблемы'}
                </label>
                <textarea
                  className="flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                  placeholder="Vaziyatni qisqacha bayon qiling..."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  required
                />
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
                  {lang === 'uz' ? 'Murojaatni joʻnatish' : 'Отправить'}
                </Button>
              </DialogFooter>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      {/* Dispatcher Notice Banner */}
      <Card className="border-amber-500/20 bg-amber-500/5">
        <CardContent className="p-4 flex items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-xl bg-amber-500/10 text-amber-600 flex items-center justify-center shrink-0">
              <PhoneCall className="h-5 w-5" />
            </div>
            <div>
              <p className="text-sm font-bold text-foreground">
                {lang === 'uz' ? 'Yangiariq tumani Suv Dispetcherlik Xizmati (24/7)' : 'Диспетчерская служба (24/7)'}
              </p>
              <p className="text-xs text-muted-foreground">
                {lang === 'uz'
                  ? 'Kanal toshishi yoki avariya holatlarida toʻgʻridan-toʻgʻri tezkor raqam: +998 (62) 382-14-20'
                  : 'При аварийных ситуациях звоните по прямому номеру: +998 (62) 382-14-20'}
              </p>
            </div>
          </div>
          <Badge variant="outline" className="bg-amber-500/10 text-amber-600 border-amber-500/30 text-xs hidden sm:inline-flex">
            24/7 Navbatchilik
          </Badge>
        </CardContent>
      </Card>

      {/* Problem Reports List */}
      <div className="space-y-4">
        {problems.map((prob) => {
          const isResolved = prob.status === 'RESOLVED'
          const isInReview = prob.status === 'IN_REVIEW'

          return (
            <Card key={prob.id} className="hover:border-primary/40 transition-colors shadow-2xs">
              <CardHeader className="pb-3">
                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2">
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <span className="font-mono text-xs font-bold text-primary">{prob.id}</span>
                      <Badge
                        variant="outline"
                        className={
                          prob.urgency === 'HIGH'
                            ? 'bg-destructive/15 text-destructive border-destructive/30 text-[10px]'
                            : prob.urgency === 'MEDIUM'
                            ? 'bg-amber-500/15 text-amber-700 border-amber-500/30 text-[10px]'
                            : 'bg-muted text-muted-foreground text-[10px]'
                        }
                      >
                        {prob.urgency === 'HIGH' ? 'Shoshilinch' : prob.urgency === 'MEDIUM' ? 'Oʻrta' : 'Past'}
                      </Badge>
                      <span className="text-xs text-muted-foreground">• {prob.type}</span>
                    </div>
                    <CardTitle className="text-base font-bold text-foreground">
                      {prob.title}
                    </CardTitle>
                  </div>

                  <Badge
                    variant="outline"
                    className={
                      isResolved
                        ? 'bg-emerald-500/15 text-emerald-700 border-emerald-500/30 text-xs'
                        : isInReview
                        ? 'bg-sky-500/15 text-sky-700 border-sky-500/30 text-xs'
                        : 'bg-amber-500/15 text-amber-700 border-amber-500/30 text-xs'
                    }
                  >
                    {isResolved
                      ? (lang === 'uz' ? 'Hal etildi' : 'Решено')
                      : isInReview
                      ? (lang === 'uz' ? 'Oʻrganilmoqda' : 'В работе')
                      : (lang === 'uz' ? 'Koʻrib chiqilmoqda' : 'На рассмотрении')}
                  </Badge>
                </div>
              </CardHeader>

              <CardContent className="space-y-3">
                <p className="text-xs sm:text-sm text-muted-foreground leading-relaxed">
                  {prob.description}
                </p>

                <div className="pt-2 border-t border-border/60 flex flex-wrap items-center justify-between gap-3 text-xs text-muted-foreground">
                  <div className="flex items-center gap-4">
                    <span className="flex items-center gap-1">
                      <MapPin className="h-3.5 w-3.5 text-primary" />
                      <span>{prob.location}</span>
                    </span>
                    <span>• {prob.canal}</span>
                  </div>

                  <span className="flex items-center gap-1 font-mono text-[11px]">
                    <Clock className="h-3 w-3" />
                    <span>{prob.reportedAt}</span>
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
