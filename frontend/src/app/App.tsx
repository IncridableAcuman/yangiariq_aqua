import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { useState } from 'react'
import { Droplets, ShieldCheck, Cpu, MapPin, Sparkles, UserCheck, ChevronRight } from 'lucide-react'
import { translations, SupportedLanguage } from '@/shared/i18n'
import { AuthProvider, useAuth } from '@/features/auth/context/AuthContext'
import { Header } from '@/widgets/header'
import { UserRoleBadge } from '@/entities/user'
import { AuthModal } from '@/features/auth/ui/AuthModal'
import { Button } from '@/shared/ui/Button'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      staleTime: 1000 * 60 * 5,
    },
  },
})

export function AppContent() {
  const [lang, setLang] = useState<SupportedLanguage>('uz')
  const [authModalOpen, setAuthModalOpen] = useState(false)
  const [authModalView, setAuthModalView] = useState<'login' | 'register'>('register')

  const t = translations[lang]
  const { user, isAuthenticated } = useAuth()

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground selection:bg-primary/20">
      {/* Dynamic Header with Auth & Language support */}
      <Header lang={lang} onLangChange={setLang} />

      {/* Main Container */}
      <main className="flex-1 p-4 sm:p-6 max-w-7xl mx-auto w-full space-y-6">
        {/* Authenticated Welcome Banner or Guest Hero */}
        {isAuthenticated && user ? (
          <div className="relative overflow-hidden rounded-2xl border border-border bg-gradient-to-r from-card via-card to-primary/5 p-6 shadow-sm">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div className="space-y-1.5">
                <div className="flex items-center gap-2">
                  <span className="text-xs font-semibold uppercase tracking-wider text-primary flex items-center gap-1">
                    <Sparkles className="h-3.5 w-3.5" />
                    {lang === 'uz' ? 'Xush kelibsiz' : lang === 'ru' ? 'Добро пожаловать' : 'Welcome'}
                  </span>
                  <UserRoleBadge role={user.role} lang={lang} />
                </div>
                <h2 className="text-2xl font-bold tracking-tight text-foreground">
                  {user.fullName || `${user.firstName} ${user.lastName}`}
                </h2>
                <p className="text-sm text-muted-foreground">
                  {lang === 'uz'
                    ? `${user.district || 'Yangiariq tumani'} boʻyicha suv taqsimoti va dala monitoring axborot tizimi.`
                    : lang === 'ru'
                    ? `Информационная система водораспределения и мониторинга полей района ${user.district || 'Янгиарык'}.`
                    : `Water distribution and field monitoring system for ${user.district || 'Yangiariq district'}.`}
                </p>
              </div>

              <div className="flex items-center gap-3">
                <div className="text-right hidden sm:block">
                  <div className="text-xs text-muted-foreground">{t.auth.accountStatus}</div>
                  <div className="text-sm font-semibold text-emerald-600 flex items-center gap-1.5 justify-end">
                    <span className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse" />
                    {t.auth.statuses.ACTIVE}
                  </div>
                </div>
                <div className="h-10 w-10 rounded-xl bg-emerald-500/10 text-emerald-600 flex items-center justify-center font-bold">
                  <UserCheck className="h-5 w-5" />
                </div>
              </div>
            </div>
          </div>
        ) : (
          <div className="relative overflow-hidden rounded-2xl border border-primary/20 bg-gradient-to-br from-primary/10 via-background to-secondary/10 p-6 sm:p-8 shadow-sm">
            <div className="max-w-2xl space-y-3">
              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-primary/10 text-primary text-xs font-semibold">
                <Droplets className="h-3.5 w-3.5" />
                <span>Yangiariq Aqua AI • Smart Water Management</span>
              </div>
              <h2 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-foreground">
                {lang === 'uz'
                  ? 'Fermer xoʻjaliklari uchun intellektual suv taqsimoti'
                  : lang === 'ru'
                  ? 'Интеллектуальное водораспределение для фермерских хозяйств'
                  : 'Intelligent Water Allocation for Farming Communities'}
              </h2>
              <p className="text-sm text-muted-foreground leading-relaxed">
                {lang === 'uz'
                  ? 'Yangiariq tumanidagi kanallar, datchiklar telemetriyasi va AI tavsiyalaridan foydalanish uchun tizimga kiring yoki yangi fermer hisobini oching.'
                  : lang === 'ru'
                  ? 'Войдите в систему или создайте аккаунт фермера для доступа к аналитике каналов, телеметрии датчиков и рекомендациям ИИ.'
                  : 'Sign in or create a farmer account to access canal network analytics, sensor telemetry, and AI-driven irrigation advisories.'}
              </p>
              <div className="flex items-center gap-3 pt-2">
                <Button
                  variant="primary"
                  size="md"
                  onClick={() => {
                    setAuthModalView('register')
                    setAuthModalOpen(true)
                  }}
                  rightIcon={<ChevronRight className="h-4 w-4" />}
                >
                  {t.auth.signUp}
                </Button>
                <Button
                  variant="outline"
                  size="md"
                  onClick={() => {
                    setAuthModalView('login')
                    setAuthModalOpen(true)
                  }}
                >
                  {t.auth.signIn}
                </Button>
              </div>
            </div>
          </div>
        )}

        {/* Feature Cards Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="p-6 rounded-xl border bg-card shadow-sm space-y-2 hover:border-primary/40 transition-colors">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium text-muted-foreground">Water Governance</span>
              <div className="h-8 w-8 rounded-lg bg-hydro-500/10 flex items-center justify-center">
                <Droplets className="h-4 w-4 text-hydro-600" />
              </div>
            </div>
            <div className="text-2xl font-bold">Yangiariq Canal Network</div>
            <p className="text-xs text-muted-foreground">
              Automated allocation and irrigation scheduling engine
            </p>
          </div>

          <div className="p-6 rounded-xl border bg-card shadow-sm space-y-2 hover:border-secondary/40 transition-colors">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium text-muted-foreground">Sensor Abstraction</span>
              <div className="h-8 w-8 rounded-lg bg-agri-500/10 flex items-center justify-center">
                <Cpu className="h-4 w-4 text-agri-600" />
              </div>
            </div>
            <div className="text-2xl font-bold">Simulated Telemetry</div>
            <p className="text-xs text-muted-foreground">
              Decoupled soil moisture and water flow adapters ready for IoT
            </p>
          </div>

          <div className="p-6 rounded-xl border bg-card shadow-sm space-y-2 hover:border-primary/40 transition-colors">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium text-muted-foreground">AI Decision Support</span>
              <div className="h-8 w-8 rounded-lg bg-primary/10 flex items-center justify-center">
                <ShieldCheck className="h-4 w-4 text-primary" />
              </div>
            </div>
            <div className="text-2xl font-bold">Gemini Provider</div>
            <p className="text-xs text-muted-foreground">
              Smart recommendations for crop drought prevention & water saving
            </p>
          </div>
        </div>

        {/* Architecture Foundation Notice */}
        <div className="border border-dashed border-border rounded-xl p-8 text-center space-y-3 bg-muted/20">
          <div className="inline-flex items-center justify-center p-3 rounded-full bg-primary/10 text-primary">
            <MapPin className="h-6 w-6" />
          </div>
          <h2 className="text-lg font-semibold">Architectural Foundation Stage</h2>
          <p className="text-sm text-muted-foreground max-w-xl mx-auto">
            The core architecture, modular domain boundaries, DTO schemas, authentication lifecycle,
            and AI / Sensor abstractions are established. Ready for modular feature implementations.
          </p>
        </div>
      </main>

      <footer className="border-t border-border py-4 px-6 text-center text-xs text-muted-foreground">
        © 2026 Yangiariq Aqua AI. Smart Water-Management Decision Support Platform.
      </footer>

      {/* Guest Auth Modal trigger */}
      <AuthModal
        isOpen={authModalOpen}
        onClose={() => setAuthModalOpen(false)}
        initialView={authModalView}
        lang={lang}
      />
    </div>
  )
}

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </QueryClientProvider>
  )
}

export default App
