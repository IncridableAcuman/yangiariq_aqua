import React, { useState } from 'react'
import {
  Droplets,
  Sparkles,
  Cpu,
  ArrowRight,
  ShieldCheck,
  CalendarCheck,
  Waves,
  LogIn,
  UserPlus,
  HelpCircle,
  CheckCircle2,
  XCircle,
  Tractor,
  Compass,
  Building2,
  ChevronDown,
  Info,
  Clock,
  Radio,
  FileText,
} from 'lucide-react'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/ui/card'
import { SupportedLanguage } from '@/shared/i18n'
import { useAuth } from '@/features/auth/context/AuthContext'

interface LandingPageProps {
  lang: SupportedLanguage
  onLangChange: (lang: SupportedLanguage) => void
  onOpenAuth: (view: 'login' | 'register') => void
  onGoToDashboard: () => void
}

export const LandingPage: React.FC<LandingPageProps> = ({
  lang,
  onLangChange,
  onOpenAuth,
  onGoToDashboard,
}) => {
  const { isAuthenticated, user } = useAuth()
  const [openFaqIndex, setOpenFaqIndex] = useState<number | null>(0)

  const toggleFaq = (index: number) => {
    setOpenFaqIndex(openFaqIndex === index ? null : index)
  }

  const scrollToSection = (id: string) => {
    const el = document.getElementById(id)
    if (el) {
      el.scrollIntoView({ behavior: 'smooth' })
    }
  }

  const faqItems = [
    {
      q: lang === 'uz'
        ? 'Yangiariq Aqua AI platformasi nima va u kimga kerak?'
        : 'Что такое платформа Yangiariq Aqua AI и для кого она предназначена?',
      a: lang === 'uz'
        ? 'Bu Xorazm viloyati Yangiariq tumani sharoitida suv resurslarini intellektual taqsimlash, dalalar tuproq namligini datchiklar orqali kuzatish va suv isrofini kamaytirish uchun yaratilgan platformadir. U fermerlar, tuman miroblari va suv xoʻjaligi mutasaddilariga xizmat qiladi.'
        : 'Это цифровая платформа для справедливого и эффективного распределения воды в Янгиарыкском районе, мониторинга влажности почвы с помощью датчиков и рекомендаций ИИ для фермеров и операторов каналов.',
    },
    {
      q: lang === 'uz'
        ? 'Sunʼiy intellekt (Gemini AI) qanday qilib suvni tejashga yordam beradi?'
        : 'Как искусственный интеллект (Gemini AI) помогает экономить воду?',
      a: lang === 'uz'
        ? 'AI ob-havo prognozi, shamol tezligi, havo harorati va ekinning oʻsish bosqichini hisoblab chiqadi. Kunduzgi jaziramada sugʻorilganda suvning 30-40% qismi havoga bugʻlanib ketadi. Gemini AI esa sugʻorishni tungi salqin soatlarda (22:00 dan 05:00 gacha) amalga oshirishni va aniq meʼyorni tavsiya etib, suv sarfini 22-25% gacha qisqartiradi.'
        : 'ИИ анализирует температуру, прогноз погоды и влажность. Дневной полив приводит к испарению до 30-40% воды. Gemini AI рассчитывает оптимальные ночные часы полива, сохраняя влагу у корней и сокращая расход на 22-25%.',
    },
    {
      q: lang === 'uz'
        ? 'Fermer sifatida foydalanish uchun qanday uskunalar talab qilinadi?'
        : 'Какое оборудование необходимо фермеру для использования?',
      a: lang === 'uz'
        ? 'Hech qanday murakkab qurilma kerak emas! Istalgan smartfon yoki kompyuter orqali tizimga kirish mumkin. Dala maydoniga oʻrnatilgan tuproq datchiklari maʼlumotlari avtomatik tarzda shlyuzlar orqali tizimga uzatiladi va siz oʻz telefoningizda natijalarni koʻrib turasiz.'
        : 'Сложного оборудования не требуется! Достаточно обычного смартфона. Данные полевых датчиков автоматически поступают в систему, и вы видите аналитику прямо на экране телефона.',
    },
    {
      q: lang === 'uz'
        ? 'Dala maydonimda hali jismoniy IoT datchik oʻrnatilmagan boʻlsa-chi?'
        : 'Что если на моем поле пока не установлен физический датчик?',
      a: lang === 'uz'
        ? 'Platformamizda datchiklar telemetriyasini simulyatsiya qilish va tuman magistral gidropostlari maʼlumotlari asosida modellashtirish tizimi mavjud. Shuningdek, datchiksiz ham ekin turi va maydoniga qarab tavsiyalar olishingiz mumkin.'
        : 'В платформе предусмотрена архитектура симуляции датчиков и расчет на основе гидропостов района. Вы можете получать рекомендации даже до установки физического оборудования.',
    },
    {
      q: lang === 'uz'
        ? 'Platformaga aʼzo boʻlish yoki roʻyxatdan oʻtish qanday boʻladi?'
        : 'Как зарегистрироваться в платформе?',
      a: lang === 'uz'
        ? 'Juda oddiy! Yuqoridagi "Hisob ochish" tugmasini bosing, telefon raqamingiz, ismingiz va dalangiz joylashgan hududni kiriting. 1 daqiqa ichida hisobingiz faollashadi va siz oʻz dalangizni boshqarishni boshlaysiz.'
        : 'Нажмите кнопку «Регистрация», введите имя, номер телефона и прикрепите ваше поле. В течение 1 минуты доступ будет открыт.',
    },
  ]

  return (
    <div className="min-h-screen bg-background text-foreground flex flex-col selection:bg-primary/20">
      {/* Top Navigation Bar */}
      <header className="sticky top-0 z-40 border-b border-border bg-card/85 backdrop-blur-md px-4 sm:px-8 py-3.5 transition-all">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          {/* Logo & Title */}
          <div
            className="flex items-center space-x-3 cursor-pointer"
            onClick={() => window.scrollTo({ top: 0, behavior: 'smooth' })}
          >
            <div className="h-10 w-10 rounded-xl bg-primary/10 text-primary flex items-center justify-center font-bold shadow-xs">
              <Droplets className="h-6 w-6 text-primary animate-pulse" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="font-extrabold text-lg sm:text-xl tracking-tight text-foreground leading-none">
                  Yangiariq Aqua AI
                </span>
                <Badge
                  variant="outline"
                  className="bg-primary/10 text-primary border-primary/20 text-[10px] hidden sm:inline-flex py-0 px-1.5"
                >
                  Smart Water
                </Badge>
              </div>
              <p className="text-xs text-muted-foreground hidden sm:block mt-0.5">
                {lang === 'uz'
                  ? 'Xorazm viloyati Yangiariq tumani suv resurslari boshqaruvi'
                  : 'Управление водными ресурсами Янгиарыкского района'}
              </p>
            </div>
          </div>

          {/* Quick Nav Anchor Links */}
          <nav className="hidden lg:flex items-center space-x-6 text-xs font-semibold text-muted-foreground">
            <button
              onClick={() => scrollToSection('about')}
              className="hover:text-foreground transition-colors cursor-pointer"
            >
              {lang === 'uz' ? 'Loyiha haqida' : 'О проекте'}
            </button>
            <button
              onClick={() => scrollToSection('comparison')}
              className="hover:text-foreground transition-colors cursor-pointer"
            >
              {lang === 'uz' ? 'Yechim & Taqqoslash' : 'Решение'}
            </button>
            <button
              onClick={() => scrollToSection('features')}
              className="hover:text-foreground transition-colors cursor-pointer"
            >
              {lang === 'uz' ? 'Imkoniyatlar' : 'Возможности'}
            </button>
            <button
              onClick={() => scrollToSection('roles')}
              className="hover:text-foreground transition-colors cursor-pointer"
            >
              {lang === 'uz' ? 'Kimlar uchun?' : 'Для кого?'}
            </button>
            <button
              onClick={() => scrollToSection('canals')}
              className="hover:text-foreground transition-colors cursor-pointer"
            >
              {lang === 'uz' ? 'Kanallar tarmogʻi' : 'Каналы'}
            </button>
            <button
              onClick={() => scrollToSection('faq')}
              className="hover:text-foreground transition-colors cursor-pointer"
            >
              {lang === 'uz' ? 'Savol-javob' : 'FAQ'}
            </button>
          </nav>

          {/* Right Actions: Lang selector + Auth / Dashboard */}
          <div className="flex items-center space-x-3">
            {/* Language Selector */}
            <div className="flex items-center space-x-0.5 border border-border rounded-lg p-0.5 bg-background text-xs shadow-2xs">
              {(['uz', 'ru', 'en'] as SupportedLanguage[]).map((locale) => (
                <button
                  key={locale}
                  onClick={() => onLangChange(locale)}
                  className={`px-2 py-1 rounded-md text-[11px] font-medium transition-colors ${
                    lang === locale
                      ? 'bg-primary text-primary-foreground font-semibold shadow-2xs'
                      : 'text-muted-foreground hover:text-foreground'
                  }`}
                >
                  {locale.toUpperCase()}
                </button>
              ))}
            </div>

            {/* Auth Buttons */}
            {isAuthenticated && user ? (
              <Button
                variant="default"
                size="sm"
                onClick={onGoToDashboard}
                className="gap-2 shadow-xs"
              >
                <span>{lang === 'uz' ? 'Boshqaruv paneli' : 'Панель управления'}</span>
                <ArrowRight className="h-4 w-4" />
              </Button>
            ) : (
              <div className="flex items-center space-x-2">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => onOpenAuth('login')}
                  className="text-xs"
                >
                  <LogIn className="h-3.5 w-3.5 mr-1.5" />
                  <span>{lang === 'uz' ? 'Kirish' : lang === 'ru' ? 'Вход' : 'Sign In'}</span>
                </Button>
                <Button
                  variant="default"
                  size="sm"
                  onClick={() => onOpenAuth('register')}
                  className="text-xs shadow-xs"
                >
                  <UserPlus className="h-3.5 w-3.5 mr-1.5" />
                  <span>{lang === 'uz' ? 'Hisob ochish' : lang === 'ru' ? 'Регистрация' : 'Register'}</span>
                </Button>
              </div>
            )}
          </div>
        </div>
      </header>

      {/* Main Content Area */}
      <main className="flex-1">
        {/* HERO SECTION */}
        <section className="relative overflow-hidden py-16 sm:py-24 border-b border-border bg-gradient-to-b from-primary/10 via-card to-background">
          <div className="max-w-7xl mx-auto px-4 sm:px-8 relative z-10">
            <div className="max-w-3xl mx-auto text-center space-y-6">
              <Badge
                variant="outline"
                className="bg-primary/10 text-primary border-primary/20 px-3.5 py-1.5 gap-2 text-xs font-semibold inline-flex animate-in fade-in"
              >
                <Sparkles className="h-4 w-4" />
                <span>
                  {lang === 'uz'
                    ? 'Yangiariq tumani aqlli suv resurslari axborot tizimi • 2026'
                    : 'Интеллектуальная система водораспределения Янгиарыка'}
                </span>
              </Badge>

              <h1 className="text-3xl sm:text-5xl lg:text-6xl font-extrabold tracking-tight text-foreground leading-[1.15]">
                {lang === 'uz' ? (
                  <>
                    Fermer xoʻjaliklari uchun <span className="text-primary">intellektual</span> suv taqsimoti
                  </>
                ) : lang === 'ru' ? (
                  <>
                    <span className="text-primary">Интеллектуальное</span> водораспределение для фермеров
                  </>
                ) : (
                  <>
                    Intelligent <span className="text-primary">Water Allocation</span> for Farmers
                  </>
                )}
              </h1>

              <p className="text-base sm:text-lg text-muted-foreground leading-relaxed max-w-2xl mx-auto">
                {lang === 'uz'
                  ? 'Magistral kanallar telemetriyasi, tuproq namligi datchiklari va Google Gemini sunʼiy intellekti yordamida ekinlarni qurgʻoqchilikdan asrang va suv sarfini 25% gacha tejang.'
                  : 'Телеметрия магистральных каналов, датчики влажности почвы и искусственный интеллект Google Gemini для предотвращения засухи и экономии до 25% воды.'}
              </p>

              {/* Action Buttons */}
              <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
                <Button
                  size="lg"
                  onClick={() => onOpenAuth('register')}
                  className="w-full sm:w-auto gap-2 shadow-md text-sm font-semibold h-12 px-7"
                >
                  <UserPlus className="h-4 w-4" />
                  <span>{lang === 'uz' ? 'Fermer hisobini ochish' : 'Открыть аккаунт фермера'}</span>
                  <ArrowRight className="h-4 w-4 ml-1" />
                </Button>

                <Button
                  variant="outline"
                  size="lg"
                  onClick={() => onOpenAuth('login')}
                  className="w-full sm:w-auto gap-2 text-sm font-semibold h-12 px-6"
                >
                  <LogIn className="h-4 w-4" />
                  <span>{lang === 'uz' ? 'Mavjud hisobga kirish' : 'Войти в систему'}</span>
                </Button>

                <Button
                  variant="ghost"
                  size="lg"
                  onClick={onGoToDashboard}
                  className="w-full sm:w-auto text-xs text-muted-foreground hover:text-foreground"
                >
                  {lang === 'uz' ? 'Boshqaruv panelini koʻrish (Demo) →' : 'Демо панель →'}
                </Button>
              </div>

              {/* District Notice */}
              <div className="inline-flex items-center gap-2 text-xs text-muted-foreground bg-muted/60 px-3.5 py-1.5 rounded-full border border-border/80">
                <span className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse" />
                <span>
                  {lang === 'uz'
                    ? 'Yangiariq tumani gidrotizimi barqaror faoliyat koʻrsatmoqda'
                    : 'Гидросистема Янгиарыкского района активна и стабильна'}
                </span>
              </div>
            </div>

            {/* Quick Metrics Grid */}
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-16 max-w-5xl mx-auto">
              <Card className="border-border/70 bg-card/80 backdrop-blur-xs text-center p-4">
                <CardContent className="p-0 space-y-1">
                  <div className="text-2xl sm:text-3xl font-extrabold text-foreground font-mono">14,800+</div>
                  <div className="text-xs text-muted-foreground font-medium">
                    {lang === 'uz' ? 'Gektar ekin maydoni' : 'Гектаров под контролем'}
                  </div>
                </CardContent>
              </Card>

              <Card className="border-border/70 bg-card/80 backdrop-blur-xs text-center p-4">
                <CardContent className="p-0 space-y-1">
                  <div className="text-2xl sm:text-3xl font-extrabold text-emerald-600 font-mono">22% - 25%</div>
                  <div className="text-xs text-muted-foreground font-medium">
                    {lang === 'uz' ? 'Suv tejamkorligi' : 'Экономия расхода воды'}
                  </div>
                </CardContent>
              </Card>

              <Card className="border-border/70 bg-card/80 backdrop-blur-xs text-center p-4">
                <CardContent className="p-0 space-y-1">
                  <div className="text-2xl sm:text-3xl font-extrabold text-primary font-mono">4 ta</div>
                  <div className="text-xs text-muted-foreground font-medium">
                    {lang === 'uz' ? 'Magistral kanal tarmogʻi' : 'Магистральных канала'}
                  </div>
                </CardContent>
              </Card>

              <Card className="border-border/70 bg-card/80 backdrop-blur-xs text-center p-4">
                <CardContent className="p-0 space-y-1">
                  <div className="text-2xl sm:text-3xl font-extrabold text-sky-600 font-mono">24/7</div>
                  <div className="text-xs text-muted-foreground font-medium">
                    {lang === 'uz' ? 'Gemini AI monitoring' : 'Мониторинг Gemini AI'}
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>
        </section>

        {/* SECTION 1: ABOUT THE PROJECT (LOYIHA HAQIDA YANGI KELGANLAR UCHUN) */}
        <section id="about" className="py-16 sm:py-24 max-w-7xl mx-auto px-4 sm:px-8 space-y-12">
          <div className="text-center space-y-3 max-w-3xl mx-auto">
            <Badge variant="outline" className="bg-primary/10 text-primary border-primary/30">
              <Info className="h-3.5 w-3.5 mr-1" />
              {lang === 'uz' ? 'Loyiha Haqida Qisqacha' : 'О Проекте'}
            </Badge>
            <h2 className="text-2xl sm:text-4xl font-extrabold tracking-tight text-foreground">
              {lang === 'uz'
                ? 'Yangiariq Aqua AI nima va u qanday yaratilgan?'
                : 'Что представляет собой Yangiariq Aqua AI?'}
            </h2>
            <p className="text-sm text-muted-foreground leading-relaxed">
              {lang === 'uz'
                ? 'Xorazm viloyati Amudaryoning quyi havzasida joylashgan boʻlib, yoz oylarida suv taqchilligi va kanallar oxiridagi fermerlarga suv yetib bormasligi jiddiy muammo hisoblanadi. Yangiariq Aqua AI ushbu muammoni raqamlashtirish, datchiklar va sunʼiy intellekt orqali hal qilish uchun maxsus ishlab chiqildi.'
                : 'Янгиарыкский район находится в низовьях Амударьи, где в жаркие месяцы остро ощущается дефицит воды. Платформа создана для оцифровки каналов, справедливого распределения и минимизации потерь влаги.'}
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <Card className="border-primary/20 bg-card/60 p-6 space-y-3">
              <div className="h-10 w-10 rounded-xl bg-primary/10 text-primary flex items-center justify-center font-bold">
                <Waves className="h-5 w-5" />
              </div>
              <h3 className="text-base font-bold text-foreground">
                {lang === 'uz' ? 'Hududiy muammo yechimi' : 'Решение локальной проблемы'}
              </h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                {lang === 'uz'
                  ? 'Yangiariq tumani kanallari boʻylab suv sarfi koʻz bilan emas, elektron hisoblagichlar orqali aniq m³/s oʻlchovda yuritiladi.'
                  : 'Расход воды по каналам района фиксируется не на глаз, а точными приборами учета в м³/с.'}
              </p>
            </Card>

            <Card className="border-secondary/20 bg-card/60 p-6 space-y-3">
              <div className="h-10 w-10 rounded-xl bg-secondary/10 text-secondary flex items-center justify-center font-bold">
                <Sparkles className="h-5 w-5" />
              </div>
              <h3 className="text-base font-bold text-foreground">
                {lang === 'uz' ? 'Google Gemini AI kuchi' : 'Сила Google Gemini AI'}
              </h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                {lang === 'uz'
                  ? 'Sunʼiy intellekt agrometeorologik ob-havo prognozi va tuproq namligi kesishmasida qachon suv berish kerakligini hisoblaydi.'
                  : 'ИИ рассчитывает необходимость полива на основе погоды, влажности и потребностей конкретных культур.'}
              </p>
            </Card>

            <Card className="border-primary/20 bg-card/60 p-6 space-y-3">
              <div className="h-10 w-10 rounded-xl bg-emerald-500/10 text-emerald-600 flex items-center justify-center font-bold">
                <CheckCircle2 className="h-5 w-5" />
              </div>
              <h3 className="text-base font-bold text-foreground">
                {lang === 'uz' ? 'Shaffoflik va adolat' : 'Прозрачность и учет'}
              </h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                {lang === 'uz'
                  ? 'Fermerlar oʻrtasida suv talashish va noroziliklarga barham beriladi: barcha navbatlar va kvotalar ochiq elektron jurnalda koʻrinadi.'
                  : 'Исключаются споры за воду: график полива и лимиты прозрачно отображаются в электронном журнале.'}
              </p>
            </Card>
          </div>
        </section>

        {/* SECTION 2: COMPARISON (ESKI USUL VS YANGIARIQ AQUA AI) */}
        <section id="comparison" className="py-16 sm:py-24 bg-muted/40 border-y border-border">
          <div className="max-w-7xl mx-auto px-4 sm:px-8 space-y-12">
            <div className="text-center space-y-3 max-w-2xl mx-auto">
              <Badge variant="outline" className="bg-emerald-500/10 text-emerald-600 border-emerald-500/30">
                {lang === 'uz' ? 'Aniq Farq & Natija' : 'Сравнение и Результат'}
              </Badge>
              <h2 className="text-2xl sm:text-4xl font-extrabold tracking-tight text-foreground">
                {lang === 'uz'
                  ? 'Eski anʼanaviy usul va Yangiariq Aqua AI farqi'
                  : 'Традиционный подход против Yangiariq Aqua AI'}
              </h2>
              <p className="text-sm text-muted-foreground">
                {lang === 'uz'
                  ? 'Raqamlashtirish suv sarfi va hosildorlikka qanday taʼsir koʻrsatadi?'
                  : 'Как цифровизация влияет на расход воды и урожайность?'}
              </p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8 max-w-5xl mx-auto">
              {/* Old Way */}
              <div className="p-6 sm:p-8 rounded-3xl border border-destructive/20 bg-destructive/5 space-y-5">
                <div className="flex items-center gap-3">
                  <div className="h-10 w-10 rounded-xl bg-destructive/10 text-destructive flex items-center justify-center font-bold">
                    <XCircle className="h-6 w-6" />
                  </div>
                  <div>
                    <h3 className="text-lg font-bold text-foreground">
                      {lang === 'uz' ? 'Eski anʼanaviy usul' : 'Традиционный способ'}
                    </h3>
                    <p className="text-xs text-muted-foreground">
                      {lang === 'uz' ? 'Samarasiz va nazoratsiz suv sarfi' : 'Неэффективный расход'}
                    </p>
                  </div>
                </div>

                <ul className="space-y-3.5 text-xs sm:text-sm text-muted-foreground">
                  <li className="flex items-start gap-2.5">
                    <span className="text-destructive font-bold text-base leading-none">✕</span>
                    <span>
                      {lang === 'uz'
                        ? 'Suv hajmi koʻz bilan chamalanadi, aniq oʻlchov va hisob yoʻq'
                        : 'Объем воды оценивается на глаз, нет точных данных'}
                    </span>
                  </li>
                  <li className="flex items-start gap-2.5">
                    <span className="text-destructive font-bold text-base leading-none">✕</span>
                    <span>
                      {lang === 'uz'
                        ? 'Kunduzi +35°C issiqda sugʻorilib, suvning 30-40% qismi bugʻlanib ketadi'
                        : 'Полив днем на жаре приводит к испарению 30-40% драгоценной воды'}
                    </span>
                  </li>
                  <li className="flex items-start gap-2.5">
                    <span className="text-destructive font-bold text-base leading-none">✕</span>
                    <span>
                      {lang === 'uz'
                        ? 'Tuproq qachon quriyotganini faqat ekin soʻlgandan keyin seziladi'
                        : 'Влажность почвы не контролируется вовремя, растения испытывают стресс'}
                    </span>
                  </li>
                  <li className="flex items-start gap-2.5">
                    <span className="text-destructive font-bold text-base leading-none">✕</span>
                    <span>
                      {lang === 'uz'
                        ? 'Suv taqsimotida nizolar va kanal oxiriga suv yetib bormasligi'
                        : 'Конфликты за воду и перебои на концах распределительных каналов'}
                    </span>
                  </li>
                </ul>
              </div>

              {/* Aqua AI Way */}
              <div className="p-6 sm:p-8 rounded-3xl border border-emerald-500/30 bg-emerald-500/5 space-y-5 shadow-xs">
                <div className="flex items-center gap-3">
                  <div className="h-10 w-10 rounded-xl bg-emerald-500/10 text-emerald-600 flex items-center justify-center font-bold">
                    <CheckCircle2 className="h-6 w-6" />
                  </div>
                  <div>
                    <h3 className="text-lg font-bold text-foreground">
                      {lang === 'uz' ? 'Yangiariq Aqua AI bilan' : 'С Yangiariq Aqua AI'}
                    </h3>
                    <p className="text-xs text-emerald-700 dark:text-emerald-300 font-semibold">
                      {lang === 'uz' ? '25% gacha suv tejamkorligi va kafolatli hosil' : 'Экономия до 25% и урожай'}
                    </p>
                  </div>
                </div>

                <ul className="space-y-3.5 text-xs sm:text-sm text-foreground">
                  <li className="flex items-start gap-2.5">
                    <CheckCircle2 className="h-4 w-4 text-emerald-600 shrink-0 mt-0.5" />
                    <span>
                      {lang === 'uz'
                        ? 'Datchiklar orqali tuproq namligi va kanal oqimi aniq m³/s koʻrinadi'
                        : 'Точные данные влажности почвы и расхода воды с датчиков онлайн'}
                    </span>
                  </li>
                  <li className="flex items-start gap-2.5">
                    <CheckCircle2 className="h-4 w-4 text-emerald-600 shrink-0 mt-0.5" />
                    <span>
                      {lang === 'uz'
                        ? 'Gemini AI tavsiyasi bilan tungi soatlarda sugʻoriladi (bugʻlanish yoʻqoladi)'
                        : 'ИИ рекомендует ночной полив, исключая испарение и экономя до четверти ресурса'}
                    </span>
                  </li>
                  <li className="flex items-start gap-2.5">
                    <CheckCircle2 className="h-4 w-4 text-emerald-600 shrink-0 mt-0.5" />
                    <span>
                      {lang === 'uz'
                        ? 'Namlik 45% ga tushishidan 24 soat oldin avtomatik ogohlantiradi'
                        : 'Предупреждение о риске стресса корней за 24 часа до критической точки'}
                    </span>
                  </li>
                  <li className="flex items-start gap-2.5">
                    <CheckCircle2 className="h-4 w-4 text-emerald-600 shrink-0 mt-0.5" />
                    <span>
                      {lang === 'uz'
                        ? 'Shaffof elektron navbat: har bir fermer oʻz vaqtida suv oladi'
                        : 'Прозрачная электронная очередь забора воды из магистрального канала'}
                    </span>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </section>

        {/* SECTION 3: KEY FEATURES (IMKONIYATLAR) */}
        <section id="features" className="py-16 sm:py-24 max-w-7xl mx-auto px-4 sm:px-8 space-y-12">
          <div className="text-center space-y-3 max-w-2xl mx-auto">
            <Badge variant="outline" className="bg-secondary/15 text-secondary border-secondary/30">
              {lang === 'uz' ? 'Asosiy Imkoniyatlar' : 'Ключевые Возможности'}
            </Badge>
            <h2 className="text-2xl sm:text-3xl font-bold tracking-tight text-foreground">
              {lang === 'uz'
                ? 'Suv resurslarini zamonaviy boshqarish vositalari'
                : 'Инструменты современного управления водой'}
            </h2>
            <p className="text-sm text-muted-foreground">
              {lang === 'uz'
                ? 'Fermer xoʻjaliklari, kanal dispetcherlari va tuman suv xoʻjaligi boʻlimi uchun yagona intellektual muhit.'
                : 'Единая интеллектуальная платформа для фермерских хозяйств, диспетчеров и водного хозяйства района.'}
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <Card className="p-6 rounded-2xl border hover:border-primary/50 transition-all space-y-3">
              <div className="h-12 w-12 rounded-xl bg-hydro-500/10 text-hydro-600 flex items-center justify-center font-bold">
                <Droplets className="h-6 w-6" />
              </div>
              <h3 className="text-lg font-bold text-foreground">
                {lang === 'uz' ? 'Kanal Telemetriyasi & Oqim Nazorati' : 'Телеметрия каналов и расход'}
              </h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                {lang === 'uz'
                  ? 'Yangiariq Bosh kanali, Shix-Ariq, Qatagʻon va Chigʻirchi kanallaridagi suv sarfi (m³/s) va sathi real vaqtda monitoring qilinadi.'
                  : 'Расход воды (м³/с) и уровень в магистральных каналах Янгиарыка отслеживаются в реальном времени.'}
              </p>
            </Card>

            <Card className="p-6 rounded-2xl border hover:border-primary/50 transition-all space-y-3">
              <div className="h-12 w-12 rounded-xl bg-primary/10 text-primary flex items-center justify-center font-bold">
                <Sparkles className="h-6 w-6" />
              </div>
              <h3 className="text-lg font-bold text-foreground">
                {lang === 'uz' ? 'Gemini AI Agromaslahatchi' : 'ИИ Агросоветник Gemini'}
              </h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                {lang === 'uz'
                  ? 'Havo harorati, bugʻlanish va ekin ehtiyoji tahlil qilinib, eng optimal tungi sugʻorish vaqtlari boʻyicha tavsiyalar beriladi.'
                  : 'Анализ температуры воздуха, испарения и потребностей культур с рекомендациями оптимального ночного полива.'}
              </p>
            </Card>

            <Card className="p-6 rounded-2xl border hover:border-primary/50 transition-all space-y-3">
              <div className="h-12 w-12 rounded-xl bg-agri-500/10 text-agri-600 flex items-center justify-center font-bold">
                <Cpu className="h-6 w-6" />
              </div>
              <h3 className="text-lg font-bold text-foreground">
                {lang === 'uz' ? 'Tuproq Namligi Datchiklari' : 'Датчики влажности почвы'}
              </h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                {lang === 'uz'
                  ? '10cm, 30cm va 60cm qatlamlardagi tuproq namligi va shoʻrlanish darajasi doimiy kuzatiladi va kritik chegarada ogohlantiradi.'
                  : 'Контроль слоев 10см, 30см и 60см влажности почвы и оповещение при приближении к критической отметке.'}
              </p>
            </Card>

            <Card className="p-6 rounded-2xl border hover:border-primary/50 transition-all space-y-3">
              <div className="h-12 w-12 rounded-xl bg-sky-500/10 text-sky-600 flex items-center justify-center font-bold">
                <CalendarCheck className="h-6 w-6" />
              </div>
              <h3 className="text-lg font-bold text-foreground">
                {lang === 'uz' ? 'Shaffof Sugʻorish Navbatlari' : 'Прозрачный график поливов'}
              </h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                {lang === 'uz'
                  ? 'Fermerlar oʻrtasida suv taqsimoti nizolarini bartaraf etuvchi avtomatlashtirilgan navbat va kvota hisobi.'
                  : 'Автоматизированный учет очереди и квот, исключающий споры за воду между фермерскими хозяйствами.'}
              </p>
            </Card>

            <Card className="p-6 rounded-2xl border hover:border-primary/50 transition-all space-y-3">
              <div className="h-12 w-12 rounded-xl bg-amber-500/10 text-amber-600 flex items-center justify-center font-bold">
                <Waves className="h-6 w-6" />
              </div>
              <h3 className="text-lg font-bold text-foreground">
                {lang === 'uz' ? 'Tezkor Avariya & Muammo Xabarlari' : 'Экстренные инциденты и аварии'}
              </h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                {lang === 'uz'
                  ? 'Kanal toʻsilishi, suv sathining keskin pasayishi yoki texnik nosozliklarda dispetcherga bir zumda xabar yuboriladi.'
                  : 'Мгновенная отправка обращений диспетчеру при засоре русла каналов, утечках или поломках шлюзов.'}
              </p>
            </Card>

            <Card className="p-6 rounded-2xl border hover:border-primary/50 transition-all space-y-3">
              <div className="h-12 w-12 rounded-xl bg-purple-500/10 text-purple-600 flex items-center justify-center font-bold">
                <ShieldCheck className="h-6 w-6" />
              </div>
              <h3 className="text-lg font-bold text-foreground">
                {lang === 'uz' ? 'Xavfsiz & Maxsus Rollar' : 'Безопасность и роли доступа'}
              </h3>
              <p className="text-xs text-muted-foreground leading-relaxed">
                {lang === 'uz'
                  ? 'Fermer, Suv operatori va Tizim administratori uchun maxsus chegaralangan va himoyalangan huquqlar tizimi.'
                  : 'Ролевая модель доступа: Фермер, Оператор гидроузла и Администратор с разделением полномочий.'}
              </p>
            </Card>
          </div>
        </section>

        {/* SECTION 4: ROLES (KIMLAR UCHUN?) */}
        <section id="roles" className="py-16 sm:py-24 bg-muted/30 border-y border-border">
          <div className="max-w-7xl mx-auto px-4 sm:px-8 space-y-12">
            <div className="text-center space-y-3 max-w-2xl mx-auto">
              <Badge variant="outline" className="bg-primary/10 text-primary border-primary/20">
                {lang === 'uz' ? 'Foydalanuvchilar Toifalari' : 'Для Кого Платформа'}
              </Badge>
              <h2 className="text-2xl sm:text-4xl font-extrabold tracking-tight text-foreground">
                {lang === 'uz'
                  ? 'Har bir ishtirokchi uchun qulay imkoniyatlar'
                  : 'Возможности для каждого участника'}
              </h2>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {/* Farmer */}
              <Card className="p-6 rounded-2xl border bg-card space-y-4">
                <div className="h-12 w-12 rounded-xl bg-emerald-500/10 text-emerald-600 flex items-center justify-center">
                  <Tractor className="h-6 w-6" />
                </div>
                <div>
                  <h3 className="text-lg font-bold text-foreground">
                    {lang === 'uz' ? 'Fermer xoʻjaliklari' : 'Фермерские хозяйства'}
                  </h3>
                  <Badge variant="outline" className="text-[10px] mt-1 bg-emerald-500/10 text-emerald-600">
                    {lang === 'uz' ? 'Ekin egasi' : 'Владелец полей'}
                  </Badge>
                </div>
                <p className="text-xs text-muted-foreground leading-relaxed">
                  {lang === 'uz'
                    ? 'Smartfondan dalasining namligini koʻradi, sugʻorishga onlayn soʻrov beradi va AI tavsiyalariga amal qilib suv xarajatlarini tejaydi.'
                    : 'Контролирует влажность со смартфона, подает заявки на полив и экономит воду по советам ИИ.'}
                </p>
              </Card>

              {/* Operator */}
              <Card className="p-6 rounded-2xl border bg-card space-y-4">
                <div className="h-12 w-12 rounded-xl bg-sky-500/10 text-sky-600 flex items-center justify-center">
                  <Compass className="h-6 w-6" />
                </div>
                <div>
                  <h3 className="text-lg font-bold text-foreground">
                    {lang === 'uz' ? 'Miroblar va Operatorlar' : 'Операторы гидроузлов'}
                  </h3>
                  <Badge variant="outline" className="text-[10px] mt-1 bg-sky-500/10 text-sky-600">
                    {lang === 'uz' ? 'Suv taqsimotchisi' : 'Распределение'}
                  </Badge>
                </div>
                <p className="text-xs text-muted-foreground leading-relaxed">
                  {lang === 'uz'
                    ? 'Magistral kanallar va shlyuzlarni real vaqtda nazorat qiladi, navbatlarni tasdiqlaydi, isrofgarchilik va noqonuniy ulanishlarni aniqlaydi.'
                    : 'Управляет шлюзами, утверждает график забора воды и предотвращает несанкционированные утечки.'}
                </p>
              </Card>

              {/* Admin / Government */}
              <Card className="p-6 rounded-2xl border bg-card space-y-4">
                <div className="h-12 w-12 rounded-xl bg-purple-500/10 text-purple-600 flex items-center justify-center">
                  <Building2 className="h-6 w-6" />
                </div>
                <div>
                  <h3 className="text-lg font-bold text-foreground">
                    {lang === 'uz' ? 'Tuman Suv Xoʻjaligi' : 'Водное хозяйство района'}
                  </h3>
                  <Badge variant="outline" className="text-[10px] mt-1 bg-purple-500/10 text-purple-600">
                    {lang === 'uz' ? 'Tuman boshqaruvi' : 'Мониторинг'}
                  </Badge>
                </div>
                <p className="text-xs text-muted-foreground leading-relaxed">
                  {lang === 'uz'
                    ? 'Yangiariq tumani boʻyicha yagona suv balansi, qurgʻoqchilik xavfi xaritasi va umumiy tejamkorlik statistikasini tahlil qiladi.'
                    : 'Сводная аналитика водопотребления, карта риска засухи и долгосрочное планирование ресурсов района.'}
                </p>
              </Card>
            </div>
          </div>
        </section>

        {/* SECTION 5: YANGIARIQ CANAL NETWORK */}
        <section id="canals" className="py-16 sm:py-24 max-w-7xl mx-auto px-4 sm:px-8 space-y-12">
          <div className="text-center space-y-3 max-w-2xl mx-auto">
            <Badge variant="outline" className="bg-hydro-500/10 text-hydro-600 border-hydro-500/30">
              <Radio className="h-3.5 w-3.5 mr-1 text-emerald-500 animate-pulse" />
              {lang === 'uz' ? 'Jonli Telemetriya' : 'Телеметрия'}
            </Badge>
            <h2 className="text-2xl sm:text-4xl font-extrabold tracking-tight text-foreground">
              {lang === 'uz'
                ? 'Yangiariq tumani magistral kanallari tarmogʻi'
                : 'Сеть магистральных каналов Янгиарыка'}
            </h2>
            <p className="text-sm text-muted-foreground">
              {lang === 'uz'
                ? 'Platformaga ulangan tuman gidropostlari va suv oʻtkazish quvvati'
                : 'Подключенные гидропосты района и пропускная способность'}
            </p>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <Card className="p-5 space-y-2 border-border/80 hover:border-primary/50 transition-all">
              <div className="flex items-center justify-between text-xs text-muted-foreground">
                <span>Magistral</span>
                <span className="font-mono text-emerald-600 font-bold">Faol</span>
              </div>
              <h4 className="font-bold text-foreground text-sm">Yangiariq Bosh Kanali</h4>
              <p className="text-xs text-muted-foreground">Oqim: 14.8 m³/s (Max: 18.0)</p>
              <div className="text-[11px] text-primary font-medium flex items-center gap-1">
                <Clock className="h-3 w-3" /> Sath: 1.82 m
              </div>
            </Card>

            <Card className="p-5 space-y-2 border-border/80 hover:border-primary/50 transition-all">
              <div className="flex items-center justify-between text-xs text-muted-foreground">
                <span>Tarmoq</span>
                <span className="font-mono text-emerald-600 font-bold">Faol</span>
              </div>
              <h4 className="font-bold text-foreground text-sm">Shix-Ariq Tarmogʻi</h4>
              <p className="text-xs text-muted-foreground">Oqim: 6.2 m³/s (Max: 8.5)</p>
              <div className="text-[11px] text-primary font-medium flex items-center gap-1">
                <Clock className="h-3 w-3" /> Sath: 1.25 m
              </div>
            </Card>

            <Card className="p-5 space-y-2 border-border/80 hover:border-primary/50 transition-all">
              <div className="flex items-center justify-between text-xs text-muted-foreground">
                <span>Tarmoq</span>
                <span className="font-mono text-emerald-600 font-bold">Faol</span>
              </div>
              <h4 className="font-bold text-foreground text-sm">Qatagʻon Kanali</h4>
              <p className="text-xs text-muted-foreground">Oqim: 4.5 m³/s (Max: 6.0)</p>
              <div className="text-[11px] text-primary font-medium flex items-center gap-1">
                <Clock className="h-3 w-3" /> Sath: 0.95 m
              </div>
            </Card>

            <Card className="p-5 space-y-2 border-border/80 hover:border-primary/50 transition-all">
              <div className="flex items-center justify-between text-xs text-muted-foreground">
                <span>Nazorat posti</span>
                <span className="font-mono text-emerald-600 font-bold">Faol</span>
              </div>
              <h4 className="font-bold text-foreground text-sm">Chigʻirchi Tarmoq Posti</h4>
              <p className="text-xs text-muted-foreground">Oqim: 2.1 m³/s (Max: 4.0)</p>
              <div className="text-[11px] text-primary font-medium flex items-center gap-1">
                <Clock className="h-3 w-3" /> Sath: 0.62 m
              </div>
            </Card>
          </div>
        </section>

        {/* SECTION 6: FAQ ACCORDION (SAVOL-JAVOBLAR) */}
        <section id="faq" className="py-16 sm:py-24 bg-muted/40 border-t border-border">
          <div className="max-w-4xl mx-auto px-4 sm:px-8 space-y-10">
            <div className="text-center space-y-3">
              <Badge variant="outline" className="bg-primary/10 text-primary border-primary/20">
                <HelpCircle className="h-3.5 w-3.5 mr-1" />
                {lang === 'uz' ? 'Savol-Javob' : 'Часто Задаваемые Вопросы'}
              </Badge>
              <h2 className="text-2xl sm:text-4xl font-extrabold tracking-tight text-foreground">
                {lang === 'uz'
                  ? 'Yangi tashrif buyuruvchilar uchun koʻp beriladigan savollar'
                  : 'Ответы на частые вопросы'}
              </h2>
              <p className="text-sm text-muted-foreground">
                {lang === 'uz'
                  ? 'Platformadan foydalanish, datchiklar va AI ishlash tamoyillari haqida maʼlumot'
                  : 'Все что нужно знать о начале работы с платформой'}
              </p>
            </div>

            <div className="space-y-3">
              {faqItems.map((item, idx) => {
                const isOpen = openFaqIndex === idx
                return (
                  <div
                    key={idx}
                    className="border border-border rounded-2xl bg-card overflow-hidden transition-all shadow-2xs"
                  >
                    <button
                      type="button"
                      onClick={() => toggleFaq(idx)}
                      className="w-full p-4 sm:p-5 text-left flex items-center justify-between gap-4 font-semibold text-sm sm:text-base text-foreground hover:bg-muted/30 transition-colors"
                    >
                      <span>{item.q}</span>
                      <ChevronDown
                        className={`h-4 w-4 text-muted-foreground shrink-0 transition-transform duration-200 ${
                          isOpen ? 'rotate-180 text-primary' : ''
                        }`}
                      />
                    </button>
                    {isOpen && (
                      <div className="px-4 sm:px-5 pb-5 text-xs sm:text-sm text-muted-foreground leading-relaxed border-t border-border/50 pt-3">
                        {item.a}
                      </div>
                    )}
                  </div>
                )
              })}
            </div>
          </div>
        </section>

        {/* BOTTOM CTA BANNER */}
        <section className="py-16 sm:py-24 max-w-7xl mx-auto px-4 sm:px-8">
          <div className="rounded-3xl border border-primary/30 bg-gradient-to-br from-primary/15 via-card to-secondary/15 p-8 sm:p-14 text-center space-y-6 shadow-sm">
            <h2 className="text-2xl sm:text-4xl font-extrabold tracking-tight text-foreground max-w-2xl mx-auto">
              {lang === 'uz'
                ? 'Yangiariq tumani intellektual suv boshqaruv tizimiga qoʻshiling'
                : 'Присоединяйтесь к платформе умного орошения Янгиарыка'}
            </h2>
            <p className="text-sm text-muted-foreground max-w-xl mx-auto leading-relaxed">
              {lang === 'uz'
                ? 'Hoziroq bepul roʻyxatdan oʻting va dalangizdagi suv taqsimotini zamonaviy texnologiyalar bilan boshqaring.'
                : 'Зарегистрируйтесь бесплатно и управляйте поливом с помощью современных технологий.'}
            </p>
            <div className="flex flex-col sm:flex-row items-center justify-center gap-3 pt-2">
              <Button
                size="lg"
                onClick={() => onOpenAuth('register')}
                className="w-full sm:w-auto gap-2 shadow-md h-12 px-8 font-semibold"
              >
                <UserPlus className="h-4 w-4" />
                <span>{lang === 'uz' ? 'Roʻyxatdan oʻtish' : 'Регистрация'}</span>
              </Button>
              <Button
                variant="outline"
                size="lg"
                onClick={() => onOpenAuth('login')}
                className="w-full sm:w-auto gap-2 h-12 px-8 font-semibold"
              >
                <LogIn className="h-4 w-4" />
                <span>{lang === 'uz' ? 'Tizimga kirish' : 'Вход'}</span>
              </Button>
            </div>
          </div>
        </section>
      </main>

      {/* Footer */}
      <footer className="border-t border-border py-8 px-4 sm:px-8 bg-card/60">
        <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-4 text-xs text-muted-foreground">
          <div className="flex items-center space-x-2">
            <div className="h-6 w-6 rounded-md bg-primary/10 text-primary flex items-center justify-center font-bold">
              <Droplets className="h-3.5 w-3.5" />
            </div>
            <span className="font-semibold text-foreground">Yangiariq Aqua AI</span>
            <span>• © 2026 Barcha huquqlar himoyalangan.</span>
          </div>

          <p className="text-center sm:text-right">
            {lang === 'uz'
              ? 'Xorazm viloyati Yangiariq tumani suv resurslarini oqilona boshqarish platformasi'
              : 'Платформа рационального водопользования Янгиарыкского района'}
          </p>
        </div>
      </footer>
    </div>
  )
}
