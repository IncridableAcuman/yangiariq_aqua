import React, { useState } from 'react';
import {
  useWaterAccountingOverview,
  useWaterBalance,
  useWaterLedger,
  useConveyanceLosses,
  useAllocationCompliance,
  useDistributionEquity,
  useConservationOpportunities,
  useWaterReconciliation,
} from '@/features/water-accounting/queries/useWaterAccountingQueries';
import {
  RegionalWaterBalanceCards,
  WaterBalanceChart,
  WaterLedgerTable,
  CanalLossAnalysisTable,
  AllocationComplianceTable,
  WuaEquityComparison,
  ConservationOpportunityCard,
  WaterReconciliationPanel,
  FieldAccountingDetailModal,
} from '@/features/water-accounting/components';
import {
  Scale,
  BookOpen,
  TrendingDown,
  CheckSquare,
  Users,
  Lightbulb,
  Layers,
  AlertTriangle,
  RefreshCw,
  Loader2,
} from 'lucide-react';
import { NavSection } from '@/widgets/sidebar';

interface RegionalWaterAccountingPageProps {
  onNavigate?: (section: NavSection) => void;
}

export const RegionalWaterAccountingPage: React.FC<RegionalWaterAccountingPageProps> = ({
  onNavigate,
}) => {
  const [activeTab, setActiveTab] = useState<
    'balance' | 'ledger' | 'losses' | 'compliance' | 'equity' | 'conservation'
  >('balance');
  const [selectedFieldId, setSelectedFieldId] = useState<string | null>(null);

  // Queries
  const { data: overview, refetch: refetchOverview } = useWaterAccountingOverview();
  const { data: balance, isLoading: isBalanceLoading } = useWaterBalance();
  const { data: ledger = [], isLoading: isLedgerLoading } = useWaterLedger();
  const { data: losses = [], isLoading: isLossesLoading } = useConveyanceLosses();
  const { data: compliances = [], isLoading: isComplianceLoading } = useAllocationCompliance();
  const { data: equities = [], isLoading: isEquityLoading } = useDistributionEquity();
  const { data: opportunities = [], isLoading: isOpportunitiesLoading } =
    useConservationOpportunities();
  const { data: reconciliations = [], isLoading: isReconciliationLoading, refetch: refetchReconciliations } =
    useWaterReconciliation();

  const handleRefreshAll = () => {
    refetchOverview();
    refetchReconciliations();
  };

  return (
    <div className="space-y-6 pb-12 animate-in fade-in duration-300">
      {/* Top Header */}
      <div className="flex flex-wrap items-center justify-between gap-4 p-5 bg-card border rounded-2xl shadow-xs">
        <div className="space-y-1">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-xl bg-primary/10 text-primary">
              <Scale className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-xl font-bold text-foreground">
                Mintaqaviy Suv Hisobi va Tejamkorlik Markazi
              </h1>
              <p className="text-xs text-muted-foreground">
                Regional Water Accounting, Distribution Equity & Conservation Intelligence (Milestone 08.15)
              </p>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={handleRefreshAll}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-semibold rounded-lg border bg-background hover:bg-muted transition-colors text-foreground"
            title="Maʼlumotlarni yangilash"
          >
            <RefreshCw className="w-3.5 h-3.5 text-muted-foreground" />
            <span>Yangilash</span>
          </button>
        </div>
      </div>

      {/* KPI Overview Row */}
      {overview && (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-3.5">
          <div className="p-3.5 bg-card border rounded-xl shadow-xs">
            <div className="flex items-center justify-between text-muted-foreground mb-1 text-xs">
              <span>Harakatlar Daftari</span>
              <BookOpen className="w-4 h-4 text-primary" />
            </div>
            <div className="text-xl font-bold text-foreground">
              {overview.totalLedgerEntries}
            </div>
            <p className="text-[11px] text-muted-foreground mt-0.5">Audit yozuvlari</p>
          </div>

          <div className="p-3.5 bg-card border rounded-xl shadow-xs">
            <div className="flex items-center justify-between text-muted-foreground mb-1 text-xs">
              <span>Oʻrtacha Kanal Isrofi</span>
              <TrendingDown className="w-4 h-4 text-rose-500" />
            </div>
            <div className="text-xl font-bold text-rose-600 dark:text-rose-400">
              {overview.averageConveyanceLossPercent.toFixed(2)}%
            </div>
            <p className="text-[11px] text-muted-foreground mt-0.5">Magistral kanallarda</p>
          </div>

          <div className="p-3.5 bg-card border rounded-xl shadow-xs">
            <div className="flex items-center justify-between text-muted-foreground mb-1 text-xs">
              <span>Muvofiqlikdan Chiqqan</span>
              <AlertTriangle className="w-4 h-4 text-amber-500" />
            </div>
            <div className="text-xl font-bold text-amber-600 dark:text-amber-400">
              {overview.nonCompliantParcelsCount}
            </div>
            <p className="text-[11px] text-muted-foreground mt-0.5">Dala maydoni</p>
          </div>

          <div className="p-3.5 bg-card border rounded-xl shadow-xs">
            <div className="flex items-center justify-between text-muted-foreground mb-1 text-xs">
              <span>Ochiq Tafovutlar</span>
              <Scale className="w-4 h-4 text-blue-500" />
            </div>
            <div className="text-xl font-bold text-blue-600 dark:text-blue-400">
              {overview.unverifiedReconciliationsCount}
            </div>
            <p className="text-[11px] text-muted-foreground mt-0.5">Koʻrib chiqish zarur</p>
          </div>

          <div className="p-3.5 bg-card border rounded-xl shadow-xs">
            <div className="flex items-center justify-between text-muted-foreground mb-1 text-xs">
              <span>Tejamkorlik Tavsiyalari</span>
              <Lightbulb className="w-4 h-4 text-emerald-500" />
            </div>
            <div className="text-xl font-bold text-emerald-600 dark:text-emerald-400">
              {overview.activeConservationOpportunitiesCount}
            </div>
            <p className="text-[11px] text-muted-foreground mt-0.5">Dalillarga asoslangan</p>
          </div>
        </div>
      )}

      {/* Tabs Navigation */}
      <div className="flex border-b overflow-x-auto gap-2">
        <button
          onClick={() => setActiveTab('balance')}
          className={`flex items-center gap-1.5 py-2.5 px-3.5 text-xs font-semibold border-b-2 transition-colors whitespace-nowrap ${
            activeTab === 'balance'
              ? 'border-primary text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          <Layers className="w-4 h-4" />
          <span>Suv Balansi (Water Balance)</span>
        </button>

        <button
          onClick={() => setActiveTab('ledger')}
          className={`flex items-center gap-1.5 py-2.5 px-3.5 text-xs font-semibold border-b-2 transition-colors whitespace-nowrap ${
            activeTab === 'ledger'
              ? 'border-primary text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          <BookOpen className="w-4 h-4" />
          <span>Harakatlar Daftari (Ledger)</span>
        </button>

        <button
          onClick={() => setActiveTab('losses')}
          className={`flex items-center gap-1.5 py-2.5 px-3.5 text-xs font-semibold border-b-2 transition-colors whitespace-nowrap ${
            activeTab === 'losses'
              ? 'border-primary text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          <TrendingDown className="w-4 h-4" />
          <span>Kanal Isroflari (Conveyance Losses)</span>
        </button>

        <button
          onClick={() => setActiveTab('compliance')}
          className={`flex items-center gap-1.5 py-2.5 px-3.5 text-xs font-semibold border-b-2 transition-colors whitespace-nowrap ${
            activeTab === 'compliance'
              ? 'border-primary text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          <CheckSquare className="w-4 h-4" />
          <span>Kvota Muvofiqligi (Compliance)</span>
        </button>

        <button
          onClick={() => setActiveTab('equity')}
          className={`flex items-center gap-1.5 py-2.5 px-3.5 text-xs font-semibold border-b-2 transition-colors whitespace-nowrap ${
            activeTab === 'equity'
              ? 'border-primary text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          <Users className="w-4 h-4" />
          <span>Adolatli Taqsimot (Distribution Equity)</span>
        </button>

        <button
          onClick={() => setActiveTab('conservation')}
          className={`flex items-center gap-1.5 py-2.5 px-3.5 text-xs font-semibold border-b-2 transition-colors whitespace-nowrap ${
            activeTab === 'conservation'
              ? 'border-primary text-primary'
              : 'border-transparent text-muted-foreground hover:text-foreground'
          }`}
        >
          <Lightbulb className="w-4 h-4" />
          <span>Tejamkorlik va Solishtiruv (Conservation & Reconciliation)</span>
        </button>
      </div>

      {/* Tab 1: Water Balance */}
      {activeTab === 'balance' && (
        <div className="space-y-6 animate-in fade-in duration-200">
          {isBalanceLoading || !balance ? (
            <div className="py-12 flex flex-col items-center justify-center gap-2 text-muted-foreground">
              <Loader2 className="w-6 h-6 animate-spin text-primary" />
              <p className="text-xs">Mintaqaviy suv balansi hisoblanmoqda...</p>
            </div>
          ) : (
            <>
              <RegionalWaterBalanceCards balance={balance} />
              <WaterBalanceChart balance={balance} />
            </>
          )}
        </div>
      )}

      {/* Tab 2: Water Movement Ledger */}
      {activeTab === 'ledger' && (
        <div className="animate-in fade-in duration-200">
          {isLedgerLoading ? (
            <div className="py-12 flex flex-col items-center justify-center gap-2 text-muted-foreground">
              <Loader2 className="w-6 h-6 animate-spin text-primary" />
              <p className="text-xs">Harakatlar daftari yuklanmoqda...</p>
            </div>
          ) : (
            <WaterLedgerTable entries={ledger} />
          )}
        </div>
      )}

      {/* Tab 3: Conveyance Losses */}
      {activeTab === 'losses' && (
        <div className="animate-in fade-in duration-200">
          {isLossesLoading ? (
            <div className="py-12 flex flex-col items-center justify-center gap-2 text-muted-foreground">
              <Loader2 className="w-6 h-6 animate-spin text-primary" />
              <p className="text-xs">Kanal oʻtkazish isroflari hisoblanmoqda...</p>
            </div>
          ) : (
            <CanalLossAnalysisTable losses={losses} />
          )}
        </div>
      )}

      {/* Tab 4: Allocation Compliance */}
      {activeTab === 'compliance' && (
        <div className="animate-in fade-in duration-200">
          {isComplianceLoading ? (
            <div className="py-12 flex flex-col items-center justify-center gap-2 text-muted-foreground">
              <Loader2 className="w-6 h-6 animate-spin text-primary" />
              <p className="text-xs">Kvota muvofiqligi audit qilinmoqda...</p>
            </div>
          ) : (
            <AllocationComplianceTable
              compliances={compliances}
              onSelectField={(id) => setSelectedFieldId(id)}
            />
          )}
        </div>
      )}

      {/* Tab 5: Distribution Equity */}
      {activeTab === 'equity' && (
        <div className="animate-in fade-in duration-200">
          {isEquityLoading ? (
            <div className="py-12 flex flex-col items-center justify-center gap-2 text-muted-foreground">
              <Loader2 className="w-6 h-6 animate-spin text-primary" />
              <p className="text-xs">Taqsimot adolatliligi koeffitsientlari tahlil qilinmoqda...</p>
            </div>
          ) : (
            <WuaEquityComparison equityList={equities} />
          )}
        </div>
      )}

      {/* Tab 6: Conservation & Reconciliation */}
      {activeTab === 'conservation' && (
        <div className="space-y-6 animate-in fade-in duration-200">
          {/* Section A: Multi-Boundary Reconciliation */}
          <div className="space-y-3">
            <h3 className="font-bold text-sm text-foreground flex items-center gap-2">
              <Scale className="w-4 h-4 text-primary" />
              <span>Bosqichlararo Solishtiruv (Multi-Boundary Reconciliation)</span>
            </h3>
            {isReconciliationLoading ? (
              <div className="py-8 flex flex-col items-center justify-center gap-2 text-muted-foreground">
                <Loader2 className="w-5 h-5 animate-spin text-primary" />
                <p className="text-xs">Solishtirish yozuvlari yuklanmoqda...</p>
              </div>
            ) : (
              <WaterReconciliationPanel
                reconciliations={reconciliations}
                onReviewSuccess={refetchReconciliations}
              />
            )}
          </div>

          {/* Section B: Conservation Opportunities */}
          <div className="space-y-3 pt-2">
            <div className="flex items-center justify-between">
              <h3 className="font-bold text-sm text-foreground flex items-center gap-2">
                <Lightbulb className="w-4 h-4 text-emerald-500" />
                <span>Dalillarga Asoslangan Tejamkorlik Tavsiyalari</span>
              </h3>
              <span className="text-xs text-muted-foreground">
                Aniqlangan imkoniyatlar: {opportunities.length} ta
              </span>
            </div>

            {isOpportunitiesLoading ? (
              <div className="py-8 flex flex-col items-center justify-center gap-2 text-muted-foreground">
                <Loader2 className="w-5 h-5 animate-spin text-primary" />
                <p className="text-xs">Tejamkorlik imkoniyatlari sintez qilinmoqda...</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {opportunities.map((opp) => (
                  <ConservationOpportunityCard
                    key={opp.id}
                    opportunity={opp}
                    onNavigateWorkflow={(uri) => {
                      if (onNavigate) {
                        const sec = uri.replace('/', '') as NavSection;
                        onNavigate(sec);
                      }
                    }}
                  />
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {/* Field Detail Modal */}
      {selectedFieldId && (
        <FieldAccountingDetailModal
          fieldId={selectedFieldId}
          onClose={() => setSelectedFieldId(null)}
        />
      )}
    </div>
  );
};
export default RegionalWaterAccountingPage;
