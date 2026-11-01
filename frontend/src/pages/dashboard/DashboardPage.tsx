import React from 'react'
import {
  Droplets,
  Activity,
  Layers,
  Map,
  Sparkles,
  ArrowUpRight,
  TrendingDown,
  Calendar,
  AlertCircle,
  CheckCircle2,
  Radio,
  ArrowRight,
} from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { SupportedLanguage } from '@/shared/i18n'
import { useAuth } from '@/features/auth/context/AuthContext'
import { NavSection } from '@/widgets/sidebar'

interface DashboardPageProps {
  lang: SupportedLanguage
  onNavigate: (section: NavSection) => void
}

export const DashboardPage: React.FC<DashboardPageProps> = ({ lang, onNavigate }) => {
  const { user } = useAuth()

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="relative overflow-hidden rounded-2xl border border-primary/20 bg-gradient-to-br from-card via-card to-primary/5 p-6 shadow-xs">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
          <div className="space-y-1.5">
            <div className="flex items-center gap-2">
              <Badge variant="outline" className="bg-primary/10 text-primary border-primary/20 gap-1 text-[11px]">
                <Sparkles className="h-3 w-3" />
                <span>Yangiariq Aqua AI • v0.1.0</span>
              </Badge>
              <Badge variant="outline" className="bg-emerald-500/10 text-emerald-600 border-emerald-500/20 text-[11px]">
                {lang === 'uz' ? 'Gidro-tizim onlayn' : 'Hydro-system online'}
              </Badge>
            </div>
            <h2 className="text-2xl font-bold tracking-tight text-foreground">
              {lang === 'uz'
                ? `Assalomu alaykum, ${user?.firstName || 'Fermer'}!`
                : `Добро пожаловать, ${user?.firstName || 'Фермер'}!`}
            </h2>
            <p className="text-sm text-muted-foreground max-w-2xl">
              {lang === 'uz'
                ? 'Yangiariq tumani kanallari boʻyicha real vaqt telemetriyasi, sunʼiy intellekt sugʻorish tavsiyalari va dala monitoringi.'
                : 'Телеметрия каналов Янгиарыкского района в реальном времени, рекомендации ИИ по орошению и мониторинг полей.'}
            </p>
          </div>

          <div className="flex items-center gap-2.5">
            <Button
              variant="default"
              size="sm"
              onClick={() => onNavigate('gis')}
              className="gap-1.5 shadow-xs"
            >
              <Map className="h-4 w-4" />
              <span>{lang === 'uz' ? 'GIS Xarita' : 'ГИС Карта'}</span>
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => onNavigate('irrigation')}
              className="gap-1.5"
            >
              <Droplets className="h-4 w-4" />
              <span>{lang === 'uz' ? 'Sugʻorish jurnali' : 'Журнал полива'}</span>
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => onNavigate('ai-advisor')}
              className="gap-1.5"
            >
              <Sparkles className="h-4 w-4 text-primary" />
              <span>{lang === 'uz' ? 'AI Tavsiya' : 'ИИ Советник'}</span>
            </Button>
          </div>
        </div>
      </div>

      {/* Metric Cards Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Metric 1 */}
        <Card className="hover:border-primary/40 transition-colors">
          <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
            <CardTitle className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
              {lang === 'uz' ? 'Jami ekin maydoni' : 'Общая площадь'}
            </CardTitle>
            <div className="h-8 w-8 rounded-lg bg-emerald-500/10 text-emerald-600 flex items-center justify-center">
              <Layers className="h-4 w-4" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-foreground">148.5 ga</div>
            <div className="flex items-center gap-1.5 text-xs text-muted-foreground mt-1">
              <span className="text-emerald-600 font-semibold flex items-center">
                <ArrowUpRight className="h-3.5 w-3.5" /> 12 dala
              </span>
              <span>• {lang === 'uz' ? 'Paxta, bugʻdoy' : 'Хлопок, пшеница'}</span>
            </div>
          </CardContent>
        </Card>

        {/* Metric 2 */}
        <Card className="hover:border-primary/40 transition-colors">
          <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
            <CardTitle className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
              {lang === 'uz' ? 'Oʻrtacha tuproq namligi' : 'Влажность почвы'}
            </CardTitle>
            <div className="h-8 w-8 rounded-lg bg-sky-500/10 text-sky-600 flex items-center justify-center">
              <Activity className="h-4 w-4" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-foreground">64.2%</div>
            <div className="flex items-center gap-1.5 text-xs text-muted-foreground mt-1">
              <Badge variant="outline" className="bg-emerald-500/15 text-emerald-700 border-emerald-500/30 text-[10px] py-0 px-1.5">
                OPTIMAL
              </Badge>
              <span>{lang === 'uz' ? 'Meʼyor chegarasida' : 'В норме'}</span>
            </div>
          </CardContent>
        </Card>

        {/* Metric 3 */}
        <Card className="hover:border-primary/40 transition-colors">
          <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
            <CardTitle className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
              {lang === 'uz' ? 'Kanal suv oqimi' : 'Расход воды в канале'}
            </CardTitle>
            <div className="h-8 w-8 rounded-lg bg-hydro-500/10 text-hydro-600 flex items-center justify-center">
              <Droplets className="h-4 w-4" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-foreground">14.8 m³/s</div>
            <div className="flex items-center gap-1.5 text-xs text-muted-foreground mt-1">
              <span className="text-emerald-600 font-semibold flex items-center font-mono">
                Q_max: 18.0
              </span>
              <span>• Yangiariq bosh kanali</span>
            </div>
          </CardContent>
        </Card>

        {/* Metric 4 */}
        <Card className="hover:border-primary/40 transition-colors">
          <CardHeader className="flex flex-row items-center justify-between pb-2 space-y-0">
            <CardTitle className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">
              {lang === 'uz' ? 'AI Suv Tejamkorligi' : 'Экономия воды с ИИ'}
            </CardTitle>
            <div className="h-8 w-8 rounded-lg bg-primary/10 text-primary flex items-center justify-center">
              <Sparkles className="h-4 w-4" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-foreground">+24.5%</div>
            <div className="flex items-center gap-1.5 text-xs text-emerald-600 font-semibold mt-1">
              <TrendingDown className="h-3.5 w-3.5" />
              <span>{lang === 'uz' ? 'Isrofgarchilik kamaygan' : 'Сокращение потерь'}</span>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Main Two-Column Layout */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left 2 cols: Irrigation Schedule & Canal Status */}
        <div className="lg:col-span-2 space-y-6">
          {/* Quick Canal Status */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-3">
              <div>
                <CardTitle className="text-base">
                  {lang === 'uz' ? 'Yangiariq tuman magistral kanallari' : 'Магистральные каналы Янгиарыка'}
                </CardTitle>
                <CardDescription>
                  {lang === 'uz' ? 'Gidro-postlar telemetriyasi va suv sathi' : 'Телеметрия гидропостов и уровень воды'}
                </CardDescription>
              </div>
              <Button
                variant="ghost"
                size="sm"
                className="text-xs text-primary"
                onClick={() => onNavigate('irrigation')}
              >
                <span>{lang === 'uz' ? 'Barchasi' : 'Все'}</span>
                <ArrowRight className="h-3.5 w-3.5 ml-1" />
              </Button>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {[
                  { name: 'Yangiariq Bosh Kanali', flow: '14.8 m³/s', status: 'Optimal', level: '1.82 m', pct: 82 },
                  { name: 'Shix-Ariq Tarmogʻi', flow: '6.2 m³/s', status: 'Faol', level: '1.25 m', pct: 70 },
                  { name: 'Qatagʻon Kanali', flow: '4.5 m³/s', status: 'Normal', level: '0.95 m', pct: 60 },
                  { name: 'Chigʻirchi Tarmoq Posti', flow: '2.1 m³/s', status: 'Nazoratda', level: '0.62 m', pct: 45 },
                ].map((canal, idx) => (
                  <div
                    key={idx}
                    className="p-3 rounded-lg border border-border/70 bg-card hover:bg-muted/40 transition-colors flex items-center justify-between gap-4"
                  >
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="h-9 w-9 rounded-lg bg-hydro-500/10 text-hydro-600 flex items-center justify-center shrink-0">
                        <Droplets className="h-4 w-4" />
                      </div>
                      <div className="truncate">
                        <div className="font-semibold text-sm text-foreground truncate">{canal.name}</div>
                        <div className="text-xs text-muted-foreground">
                          {lang === 'uz' ? 'Sath' : 'Уровень'}: {canal.level} • {canal.flow}
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-3">
                      <div className="text-right hidden sm:block">
                        <div className="text-xs font-mono font-bold text-foreground">{canal.pct}%</div>
                        <div className="w-20 h-1.5 bg-muted rounded-full overflow-hidden mt-1">
                          <div
                            className="h-full bg-primary rounded-full"
                            style={{ width: `${canal.pct}%` }}
                          />
                        </div>
                      </div>
                      <Badge variant="outline" className="bg-emerald-500/10 text-emerald-600 border-emerald-500/20 text-xs">
                        {canal.status}
                      </Badge>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>

          {/* Upcoming Irrigation Schedules */}
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-3">
              <div>
                <CardTitle className="text-base">
                  {lang === 'uz' ? 'Rejadagi sugʻorish navbatlari' : 'Ближайшая очередь полива'}
                </CardTitle>
                <CardDescription>
                  {lang === 'uz' ? 'Fermer xoʻjaliklariga suv ochish taqvimi' : 'График подачи воды для фермерских хозяйств'}
                </CardDescription>
              </div>
              <Button
                variant="outline"
                size="sm"
                className="text-xs"
                onClick={() => onNavigate('irrigation')}
              >
                <Calendar className="h-3.5 w-3.5 mr-1" />
                {lang === 'uz' ? 'Jadval' : 'График'}
              </Button>
            </CardHeader>
            <CardContent>
              <div className="divide-y divide-border/60">
                {[
                  { field: 'Dala №04 (Paxta)', time: 'Bugun, 22:00 - 04:00', canal: 'Yangiariq Bosh', volume: '1,200 m³', status: 'Tayyor' },
                  { field: 'Dala №07 (Bugʻdoy)', time: 'Ertaga, 06:00 - 12:00', canal: 'Shix-Ariq', volume: '950 m³', status: 'Kutilmoqda' },
                  { field: 'Dala №12 (Poliz)', time: 'Ertaga, 14:00 - 18:00', canal: 'Qatagʻon', volume: '600 m³', status: 'Kutilmoqda' },
                ].map((item, idx) => (
                  <div key={idx} className="py-3 flex items-center justify-between gap-4">
                    <div className="space-y-0.5 min-w-0">
                      <p className="font-semibold text-sm text-foreground truncate">{item.field}</p>
                      <p className="text-xs text-muted-foreground">
                        {item.time} • <span className="font-medium text-foreground/80">{item.canal}</span>
                      </p>
                    </div>
                    <div className="text-right shrink-0">
                      <p className="text-xs font-mono font-bold text-foreground">{item.volume}</p>
                      <Badge variant="outline" className="text-[10px] mt-1">
                        {item.status}
                      </Badge>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </div>

        {/* Right 1 col: Gemini AI Recommendations & Telemetry Alerts */}
        <div className="space-y-6">
          {/* Gemini AI Recommendation Box */}
          <Card className="border-primary/30 bg-gradient-to-b from-primary/5 via-card to-card">
            <CardHeader className="pb-3">
              <div className="flex items-center justify-between">
                <Badge variant="outline" className="bg-primary/10 text-primary border-primary/20 gap-1 text-[11px]">
                  <Sparkles className="h-3 w-3" />
                  <span>Gemini AI Engine</span>
                </Badge>
                <span className="text-[10px] text-muted-foreground font-mono">10 min avval</span>
              </div>
              <CardTitle className="text-base mt-2">
                {lang === 'uz' ? 'Tungi sugʻorish tavsiyasi' : 'Рекомендация ночного полива'}
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <p className="text-xs text-muted-foreground leading-relaxed">
                {lang === 'uz'
                  ? 'Ertaga kunduzi havo harorati +34°C gacha koʻtarilishi kutilmoqda. Bugʻlanish yoʻqotishlarini kamaytirish uchun Dala №4 va №7 ni tungi soat 22:00 dan keyin sugʻorish tavsiya etiladi. Bu 22% gacha suvni tejaydi.'
                  : 'Завтра днем ожидается повышение температуры до +34°C. Для снижения испарения рекомендуется полив полей №4 и №7 после 22:00. Это сбережет до 22% воды.'}
              </p>

              <div className="p-3 rounded-lg bg-background border border-border/80 text-xs space-y-1.5">
                <div className="flex justify-between">
                  <span className="text-muted-foreground">{lang === 'uz' ? 'Namlik ehtiyoji' : 'Потребность'}:</span>
                  <span className="font-semibold text-emerald-600">+18% m³</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-muted-foreground">{lang === 'uz' ? 'Kutilayotgan tejamkorlik' : 'Экономия'}:</span>
                  <span className="font-semibold text-primary">~320 m³</span>
                </div>
              </div>

              <Button
                variant="outline"
                size="sm"
                className="w-full text-xs"
                onClick={() => onNavigate('ai-advisor')}
              >
                <span>{lang === 'uz' ? 'AI tahlilini toʻliq koʻrish' : 'Открыть полный анализ ИИ'}</span>
                <ArrowRight className="h-3.5 w-3.5 ml-1" />
              </Button>
            </CardContent>
          </Card>

          {/* Quick Telemetry & Field Alerts */}
          <Card>
            <CardHeader className="pb-3">
              <CardTitle className="text-base flex items-center gap-2">
                <Radio className="h-4 w-4 text-emerald-500 animate-pulse" />
                <span>{lang === 'uz' ? 'Datchik ogohlantirishlari' : 'Оповещения датчиков'}</span>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3 text-xs">
                <div className="p-2.5 rounded-lg border border-amber-500/30 bg-amber-500/5 space-y-1">
                  <div className="flex items-center justify-between text-amber-700 dark:text-amber-400 font-semibold">
                    <span className="flex items-center gap-1">
                      <AlertCircle className="h-3.5 w-3.5" />
                      Dala №09: Namlik 46%
                    </span>
                    <span className="text-[10px] font-mono">15 daq</span>
                  </div>
                  <p className="text-muted-foreground text-[11px]">
                    {lang === 'uz'
                      ? 'Kritik chegaraga (45%) yaqinlashmoqda. 12 soat ichida sugʻorish talab etiladi.'
                      : 'Близко к критическому порогу (45%). Требуется полив в течение 12 часов.'}
                  </p>
                </div>

                <div className="p-2.5 rounded-lg border border-border bg-card space-y-1">
                  <div className="flex items-center justify-between font-semibold text-foreground">
                    <span className="flex items-center gap-1 text-emerald-600">
                      <CheckCircle2 className="h-3.5 w-3.5" />
                      Kanal Post №2
                    </span>
                    <span className="text-[10px] text-muted-foreground font-mono">35 daq</span>
                  </div>
                  <p className="text-muted-foreground text-[11px]">
                    {lang === 'uz'
                      ? 'Suv oqimi barqaror: 14.8 m³/s. Suv sathida chetlanish kuzatilmadi.'
                      : 'Расход воды стабилен: 14.8 м³/с. Отклонений по уровню нет.'}
                  </p>
                </div>

                <Button
                  variant="ghost"
                  size="sm"
                  className="w-full text-xs text-muted-foreground hover:text-foreground"
                  onClick={() => onNavigate('sensors')}
                >
                  {lang === 'uz' ? 'Barcha datchiklar telemetriyasi' : 'Телеметрия всех датчиков'}
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}
