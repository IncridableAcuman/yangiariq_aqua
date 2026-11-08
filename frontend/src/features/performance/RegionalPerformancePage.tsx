import React, { useState } from 'react'
import {
  TrendingUp,
  Sparkles,
  Layers,
  BookOpen,
  FileText,
  RefreshCw,
} from 'lucide-react'
import {
  usePerformanceBaselines,
  usePerformanceIndicators,
  usePerformanceOverview,
  usePerformanceTargets,
  usePerformanceTrends,
  useImpactAssessments,
} from './queries/usePerformanceQueries'
import { PerformanceOverviewCards } from './components/PerformanceOverviewCards'
import { PerformanceTrendChart } from './components/PerformanceTrendChart'
import { GeographicPerformanceTable } from './components/GeographicPerformanceTable'
import { ImpactAssessmentWorkspace } from './components/ImpactAssessmentWorkspace'
import { BaselineTargetManager } from './components/BaselineTargetManager'
import { PerformanceIndicatorCatalog } from './components/PerformanceIndicatorCatalog'
import { CreateImpactAssessmentDialog } from './components/CreateImpactAssessmentDialog'
import { ReviewImpactAssessmentDialog } from './components/ReviewImpactAssessmentDialog'
import { CreateBaselineDialog } from './components/CreateBaselineDialog'
import { CreateTargetDialog } from './components/CreateTargetDialog'
import { PerformanceReportDialog } from './components/PerformanceReportDialog'
import { ImpactAssessment } from '@/entities/performance'

interface Props {
  onNavigate?: (section: any) => void
}

export const RegionalPerformancePage: React.FC<Props> = () => {
  const [activeTab, setActiveTab] = useState<'overview' | 'impact' | 'baselines' | 'catalog'>('overview')
  const [selectedIndicatorCode, setSelectedIndicatorCode] = useState('WDP_DELIVERY_FULFILLMENT')
  const [selectedInterval, setSelectedInterval] = useState('MONTHLY')

  // Dialog state
  const [createImpactOpen, setCreateImpactOpen] = useState(false)
  const [reviewImpactOpen, setReviewImpactOpen] = useState(false)
  const [selectedForReview, setSelectedForReview] = useState<ImpactAssessment | null>(null)
  const [createBaselineOpen, setCreateBaselineOpen] = useState(false)
  const [createTargetOpen, setCreateTargetOpen] = useState(false)
  const [reportOpen, setReportOpen] = useState(false)

  // Queries
  const { data: overview, isLoading: overviewLoading, refetch: refetchOverview } = usePerformanceOverview()
  const { data: indicators = [] } = usePerformanceIndicators()
  const { data: baselines = [] } = usePerformanceBaselines()
  const { data: targets = [] } = usePerformanceTargets()
  const { data: trendData } = usePerformanceTrends(selectedIndicatorCode, selectedInterval)
  const { data: impactAssessments = [] } = useImpactAssessments()

  const handleReviewClick = (assessment: ImpactAssessment) => {
    setSelectedForReview(assessment)
    setReviewImpactOpen(true)
  }

  return (
    <div className="flex-1 flex flex-col overflow-y-auto bg-background p-4 sm:p-6 lg:p-8 space-y-6">
      {/* Top Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-border/60 pb-5">
        <div>
          <div className="flex items-center gap-2">
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-primary/10 text-primary border border-primary/20">
              Milestone 08.17
            </span>
            <span className="text-xs text-muted-foreground font-mono">Evidence-Based Impact Assessment</span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground mt-1">
            Operatsion Samaradorlik va Taʼsir Baholash Markazi
          </h1>
          <p className="text-xs text-muted-foreground mt-0.5">
            Yangiariq tumani suv taqsimoti, kanal oʻtkazuvchanligi va agrotexnik intervensiyalar natijadorligi
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          <button
            onClick={() => refetchOverview()}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-lg border border-border bg-card text-foreground hover:bg-muted transition-colors"
          >
            <RefreshCw className="w-3.5 h-3.5" />
            <span>Yangilash</span>
          </button>

          <button
            onClick={() => setReportOpen(true)}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-lg bg-secondary text-secondary-foreground hover:bg-secondary/80 transition-colors shadow-sm"
          >
            <FileText className="w-3.5 h-3.5" />
            <span>Hisobotni Eksport Qilish</span>
          </button>
        </div>
      </div>

      {/* Navigation subtabs */}
      <div className="flex items-center gap-2 border-b border-border/40 pb-2 overflow-x-auto scrollbar-none">
        <button
          onClick={() => setActiveTab('overview')}
          className={`flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-lg transition-colors whitespace-nowrap ${
            activeTab === 'overview'
              ? 'bg-primary text-primary-foreground shadow-sm'
              : 'text-muted-foreground hover:text-foreground hover:bg-muted/50'
          }`}
        >
          <TrendingUp className="w-3.5 h-3.5" />
          <span>Umumiy Natijalar & Dinamika</span>
        </button>

        <button
          onClick={() => setActiveTab('impact')}
          className={`flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-lg transition-colors whitespace-nowrap ${
            activeTab === 'impact'
              ? 'bg-primary text-primary-foreground shadow-sm'
              : 'text-muted-foreground hover:text-foreground hover:bg-muted/50'
          }`}
        >
          <Sparkles className="w-3.5 h-3.5" />
          <span>Amaliy Taʼsir Baholash ({impactAssessments.length})</span>
        </button>

        <button
          onClick={() => setActiveTab('baselines')}
          className={`flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-lg transition-colors whitespace-nowrap ${
            activeTab === 'baselines'
              ? 'bg-primary text-primary-foreground shadow-sm'
              : 'text-muted-foreground hover:text-foreground hover:bg-muted/50'
          }`}
        >
          <Layers className="w-3.5 h-3.5" />
          <span>Meʼyorlar & Tarixiy Baza ({targets.length + baselines.length})</span>
        </button>

        <button
          onClick={() => setActiveTab('catalog')}
          className={`flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-lg transition-colors whitespace-nowrap ${
            activeTab === 'catalog'
              ? 'bg-primary text-primary-foreground shadow-sm'
              : 'text-muted-foreground hover:text-foreground hover:bg-muted/50'
          }`}
        >
          <BookOpen className="w-3.5 h-3.5" />
          <span>Indikatorlar Katalogi ({indicators.length})</span>
        </button>
      </div>

      {/* Main Tab Content */}
      {overviewLoading || !overview ? (
        <div className="py-16 text-center text-xs text-muted-foreground animate-pulse">
          Samaradorlik va taʼsir koʻrsatkichlari yuklanmoqda...
        </div>
      ) : (
        <>
          {activeTab === 'overview' && (
            <div className="space-y-6">
              {/* Summary metric cards with evidence badges */}
              <PerformanceOverviewCards overview={overview} />

              {/* Trend chart with missing/simulated data distinction */}
              <PerformanceTrendChart
                trendData={trendData}
                selectedInterval={selectedInterval}
                onIntervalChange={setSelectedInterval}
                selectedIndicatorCode={selectedIndicatorCode}
                onIndicatorCodeChange={setSelectedIndicatorCode}
              />

              {/* Geographic breakdown */}
              <GeographicPerformanceTable />
            </div>
          )}

          {activeTab === 'impact' && (
            <ImpactAssessmentWorkspace
              assessments={impactAssessments}
              onCreateClick={() => setCreateImpactOpen(true)}
              onReviewClick={handleReviewClick}
            />
          )}

          {activeTab === 'baselines' && (
            <BaselineTargetManager
              baselines={baselines}
              targets={targets}
              onCreateBaselineClick={() => setCreateBaselineOpen(true)}
              onCreateTargetClick={() => setCreateTargetOpen(true)}
            />
          )}

          {activeTab === 'catalog' && (
            <PerformanceIndicatorCatalog indicators={indicators} />
          )}
        </>
      )}

      {/* Modals & Dialogs */}
      <CreateImpactAssessmentDialog
        isOpen={createImpactOpen}
        onClose={() => setCreateImpactOpen(false)}
      />

      <ReviewImpactAssessmentDialog
        isOpen={reviewImpactOpen}
        onClose={() => {
          setReviewImpactOpen(false)
          setSelectedForReview(null)
        }}
        assessment={selectedForReview}
      />

      <CreateBaselineDialog
        isOpen={createBaselineOpen}
        onClose={() => setCreateBaselineOpen(false)}
      />

      <CreateTargetDialog
        isOpen={createTargetOpen}
        onClose={() => setCreateTargetOpen(false)}
      />

      <PerformanceReportDialog
        isOpen={reportOpen}
        onClose={() => setReportOpen(false)}
      />
    </div>
  )
}
