import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { useState } from 'react'
import { Droplets, ShieldCheck, Cpu, MapPin } from 'lucide-react'
import { translations, SupportedLanguage } from '@/shared/i18n'

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
  const t = translations[lang]

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground">
      {/* Top Header */}
      <header className="border-b border-border bg-card/50 backdrop-blur px-6 py-4 flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="h-10 w-10 rounded-lg bg-hydro-500/10 text-hydro-600 flex items-center justify-center font-bold">
            <Droplets className="h-6 w-6 text-primary" />
          </div>
          <div>
            <h1 className="text-xl font-bold tracking-tight text-foreground">{t.app.title}</h1>
            <p className="text-xs text-muted-foreground">{t.app.subtitle}</p>
          </div>
        </div>

        {/* Language selector & Status */}
        <div className="flex items-center space-x-4">
          <div className="flex items-center space-x-1 border rounded-md p-1 bg-background text-xs">
            {(['uz', 'ru', 'en'] as SupportedLanguage[]).map((locale) => (
              <button
                key={locale}
                onClick={() => setLang(locale)}
                className={`px-2 py-1 rounded font-medium transition-colors ${
                  lang === locale
                    ? 'bg-primary text-primary-foreground'
                    : 'text-muted-foreground hover:text-foreground'
                }`}
              >
                {locale.toUpperCase()}
              </button>
            ))}
          </div>

          <div className="flex items-center space-x-2 text-xs text-muted-foreground border-l pl-4">
            <span className="inline-block h-2 w-2 rounded-full bg-emerald-500 animate-pulse" />
            <span>District: Yangiariq, Khorezm</span>
          </div>
        </div>
      </header>

      {/* Main Container */}
      <main className="flex-1 p-6 max-w-7xl mx-auto w-full space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="p-6 rounded-xl border bg-card shadow-sm space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium text-muted-foreground">Water Governance</span>
              <Droplets className="h-4 w-4 text-hydro-600" />
            </div>
            <div className="text-2xl font-bold">Yangiariq Canal Network</div>
            <p className="text-xs text-muted-foreground">
              Automated allocation and irrigation scheduling engine
            </p>
          </div>

          <div className="p-6 rounded-xl border bg-card shadow-sm space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium text-muted-foreground">Sensor Abstraction</span>
              <Cpu className="h-4 w-4 text-agri-600" />
            </div>
            <div className="text-2xl font-bold">Simulated Telemetry</div>
            <p className="text-xs text-muted-foreground">
              Decoupled soil moisture and water flow adapters ready for IoT
            </p>
          </div>

          <div className="p-6 rounded-xl border bg-card shadow-sm space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium text-muted-foreground">AI Decision Support</span>
              <ShieldCheck className="h-4 w-4 text-primary" />
            </div>
            <div className="text-2xl font-bold">Gemini Provider</div>
            <p className="text-xs text-muted-foreground">
              Smart recommendations for crop drought prevention & water saving
            </p>
          </div>
        </div>

        {/* Foundation Notice */}
        <div className="border border-dashed border-border rounded-xl p-8 text-center space-y-3 bg-muted/20">
          <div className="inline-flex items-center justify-center p-3 rounded-full bg-primary/10 text-primary">
            <MapPin className="h-6 w-6" />
          </div>
          <h2 className="text-lg font-semibold">Architectural Foundation Stage</h2>
          <p className="text-sm text-muted-foreground max-w-xl mx-auto">
            The core architecture, modular domain boundaries, DTO schemas, and AI / Sensor
            abstractions are established. Ready for modular feature implementations.
          </p>
        </div>
      </main>

      <footer className="border-t border-border py-4 px-6 text-center text-xs text-muted-foreground">
        © 2026 Yangiariq Aqua AI. Smart Water-Management Decision Support Platform.
      </footer>
    </div>
  )
}

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AppContent />
    </QueryClientProvider>
  )
}

export default App
