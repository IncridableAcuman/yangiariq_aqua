import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { useState, useEffect } from 'react'
import { SupportedLanguage } from '@/shared/i18n'
import { ToastProvider } from '@/shared/ui'
import { AuthProvider, useAuth } from '@/features/auth/context/AuthContext'
import { Header } from '@/widgets/header'
import { Sidebar, MobileSidebar, NavSection } from '@/widgets/sidebar'
import { AuthModal, AuthModalView } from '@/features/auth/ui/AuthModal'
import { UserProfileModal } from '@/features/auth/ui/UserProfileModal'
import { ChangePasswordModal } from '@/features/auth/ui/ChangePasswordModal'
import { LandingPage } from '@/pages/landing'
import { DashboardPage } from '@/pages/dashboard'
import { FieldsPage } from '@/pages/fields'
import { GisPage } from '@/pages/gis'
import { IrrigationPage } from '@/pages/irrigation'
import { SensorsPage } from '@/pages/sensors'
import { AnomaliesPage } from '@/pages/anomalies'
import { AiAdvisorPage } from '@/pages/ai-advisor'
import { ProblemsPage } from '@/pages/problems'
import { SettingsPage } from '@/pages/settings'
import { OperationsPage } from '@/pages/operations'
import { AnalyticsPage } from '@/pages/analytics'
import { PredictionsPage } from '@/pages/predictions'
import { RisksPage } from '@/pages/risks'
import { RegionalOperationsPage } from '@/pages/network/RegionalOperationsPage'
import { RegionalWaterAccountingPage } from '@/pages/water-accounting/RegionalWaterAccountingPage'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      staleTime: 1000 * 60 * 5,
    },
  },
})

export function AppContent() {
  const { isAuthenticated, isLoading } = useAuth()
  const [lang, setLang] = useState<SupportedLanguage>('uz')
  const [currentSection, setCurrentSection] = useState<NavSection>('dashboard')
  const [selectedOperationFieldId, setSelectedOperationFieldId] = useState<string | null>(null)
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false)
  const [isMobileSidebarOpen, setIsMobileSidebarOpen] = useState(false)

  // Landing Page vs Dashboard View state
  // If user is not authenticated, default to landing page
  const [showLanding, setShowLanding] = useState<boolean>(!isAuthenticated)

  // Automatically sync landing state when auth status changes (e.g., on logout or login)
  useEffect(() => {
    if (!isLoading) {
      if (!isAuthenticated) {
        setShowLanding(true)
      } else {
        setShowLanding(false)
      }
    }
  }, [isAuthenticated, isLoading])

  // Auth & Profile Modals
  const [authModalOpen, setAuthModalOpen] = useState(false)
  const [authModalView, setAuthModalView] = useState<AuthModalView>('login')
  const [profileModalOpen, setProfileModalOpen] = useState(false)
  const [changePasswordModalOpen, setChangePasswordModalOpen] = useState(false)

  const handleOpenAuth = (view: AuthModalView) => {
    setAuthModalView(view)
    setAuthModalOpen(true)
  }

  const handleOpenProfile = () => {
    setProfileModalOpen(true)
  }

  const handleGoToLanding = () => {
    setShowLanding(true)
  }

  const handleGoToDashboard = () => {
    setShowLanding(false)
    setCurrentSection('dashboard')
  }

  const handleNavigate = (section: NavSection, fieldId?: string) => {
    if (fieldId) {
      setSelectedOperationFieldId(fieldId)
    }
    setCurrentSection(section)
  }

  // If loading user profile on initial cold-start, display smooth loader
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-background text-foreground">
        <div className="flex flex-col items-center space-y-3">
          <div className="h-10 w-10 rounded-xl bg-primary/10 text-primary flex items-center justify-center font-bold animate-pulse">
            <span className="h-5 w-5 rounded-full border-2 border-primary border-t-transparent animate-spin" />
          </div>
          <p className="text-xs text-muted-foreground font-medium">Yangiariq Aqua AI yuklanmoqda...</p>
        </div>
      </div>
    )
  }

  // If viewing landing page (e.g. after logout or guest landing)
  if (showLanding) {
    return (
      <>
        <LandingPage
          lang={lang}
          onLangChange={setLang}
          onOpenAuth={handleOpenAuth}
          onGoToDashboard={handleGoToDashboard}
        />

        {/* Global Auth Modal accessible from Landing page */}
        <AuthModal
          isOpen={authModalOpen}
          onClose={() => setAuthModalOpen(false)}
          initialView={authModalView}
          lang={lang}
        />
      </>
    )
  }

  // Render current active page based on sidebar selection
  const renderPageContent = () => {
    switch (currentSection) {
      case 'dashboard':
        return <DashboardPage lang={lang} onNavigate={handleNavigate} />
      case 'fields':
        return <FieldsPage lang={lang} />
      case 'gis':
        return <GisPage lang={lang} onNavigate={handleNavigate} />
      case 'irrigation':
        return <IrrigationPage lang={lang} />
      case 'operations':
        return (
          <OperationsPage
            lang={lang}
            initialFieldId={selectedOperationFieldId}
            onNavigate={handleNavigate}
          />
        )
      case 'analytics':
        return (
          <AnalyticsPage
            lang={lang}
            initialFieldId={selectedOperationFieldId}
            onNavigate={handleNavigate}
          />
        )
      case 'predictions':
        return (
          <PredictionsPage
            lang={lang}
            initialFieldId={selectedOperationFieldId}
            onNavigate={handleNavigate}
          />
        )
      case 'sensors':
        return <SensorsPage lang={lang} />
      case 'anomalies':
        return <AnomaliesPage lang={lang} />
      case 'risks':
        return (
          <RisksPage
            lang={lang}
            initialFieldId={selectedOperationFieldId}
            onNavigate={handleNavigate}
          />
        )
      case 'network':
        return <RegionalOperationsPage />
      case 'water-accounting':
        return <RegionalWaterAccountingPage onNavigate={handleNavigate} />
      case 'ai-advisor':
        return <AiAdvisorPage lang={lang} />
      case 'problems':
        return <ProblemsPage lang={lang} />
      case 'settings':
        return <SettingsPage lang={lang} />
      default:
        return <DashboardPage lang={lang} onNavigate={handleNavigate} />
    }
  }

  return (
    <div className="flex h-screen w-screen overflow-hidden bg-background text-foreground selection:bg-primary/20">
      {/* Desktop Sidebar (Collapsible) */}
      <div className="hidden lg:block shrink-0">
        <Sidebar
          currentSection={currentSection}
          onSelectSection={setCurrentSection}
          lang={lang}
          isCollapsed={isSidebarCollapsed}
          onToggleCollapse={() => setIsSidebarCollapsed(!isSidebarCollapsed)}
          onOpenAuthModal={handleOpenAuth}
          onOpenProfileModal={handleOpenProfile}
          onGoToLanding={handleGoToLanding}
        />
      </div>

      {/* Mobile Drawer Sidebar */}
      <MobileSidebar
        isOpen={isMobileSidebarOpen}
        onClose={() => setIsMobileSidebarOpen(false)}
        currentSection={currentSection}
        onSelectSection={setCurrentSection}
        lang={lang}
        onOpenAuthModal={handleOpenAuth}
        onOpenProfileModal={handleOpenProfile}
        onGoToLanding={handleGoToLanding}
      />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col h-full min-w-0 overflow-hidden">
        {/* Top Navigation Header */}
        <Header
          lang={lang}
          onLangChange={setLang}
          currentSection={currentSection}
          onOpenMobileMenu={() => setIsMobileSidebarOpen(true)}
          onOpenAuthModal={handleOpenAuth}
          onOpenProfileModal={handleOpenProfile}
          onGoToLanding={handleGoToLanding}
        />

        {/* Scrollable Page Body */}
        <main className="flex-1 overflow-y-auto p-4 sm:p-6 lg:p-8 space-y-6">
          <div className="max-w-7xl mx-auto w-full">
            {renderPageContent()}
          </div>

          {/* District Footer */}
          <footer className="border-t border-border pt-6 pb-2 text-center text-xs text-muted-foreground">
            <p>
              © 2026 Yangiariq Aqua AI • {lang === 'uz' ? 'Xorazm viloyati Yangiariq tumani suv xoʻjaligi intellektual boshqaruv tizimi' : 'Интеллектуальная система управления водными ресурсами Янгиарыкского района'}
            </p>
          </footer>
        </main>
      </div>

      {/* Global Modals */}
      <AuthModal
        isOpen={authModalOpen}
        onClose={() => setAuthModalOpen(false)}
        initialView={authModalView}
        lang={lang}
      />

      <UserProfileModal
        isOpen={profileModalOpen}
        onClose={() => setProfileModalOpen(false)}
        onOpenChangePassword={() => {
          setProfileModalOpen(false)
          setChangePasswordModalOpen(true)
        }}
        lang={lang}
      />

      <ChangePasswordModal
        isOpen={changePasswordModalOpen}
        onClose={() => setChangePasswordModalOpen(false)}
        lang={lang}
      />
    </div>
  )
}

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <ToastProvider>
          <AppContent />
        </ToastProvider>
      </AuthProvider>
    </QueryClientProvider>
  )
}

export default App
