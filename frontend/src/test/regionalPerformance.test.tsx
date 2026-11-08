import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import React from 'react';
import { ToastProvider } from '@/shared/ui/toast';
import {
  EvidenceClassificationBadge,
  AttributionLevelBadge,
  PerformanceOverviewCards,
  PerformanceTrendChart,
  PerformanceIndicatorCatalog,
  ImpactAssessmentWorkspace,
  BaselineTargetManager,
  GeographicPerformanceTable,
  CreateImpactAssessmentDialog,
  ReviewImpactAssessmentDialog,
  CreateBaselineDialog,
  CreateTargetDialog,
  PerformanceReportDialog,
  RegionalPerformancePage,
} from '@/features/performance';
import {
  PerformanceIndicator,
  PerformanceBaseline,
  PerformanceTarget,
  ImpactAssessment,
  PerformanceOverview,
  PerformanceTrendSeries,
} from '@/entities/performance';
import { performanceApi } from '@/entities/performance/api';

// Mock AuthContext
vi.mock('@/features/auth/context/AuthContext', () => ({
  useAuth: () => ({
    user: {
      id: 'operator-bahodir',
      firstName: 'Bahodir',
      lastName: 'Olimov',
      role: 'OPERATOR',
    },
    isAuthenticated: true,
  }),
}));

// Mock ResizeObserver for jsdom
global.ResizeObserver = class ResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
};

// Mock Recharts ResponsiveContainer to render in jsdom
vi.mock('recharts', async () => {
  const OriginalModule = await vi.importActual<any>('recharts');
  return {
    ...OriginalModule,
    ResponsiveContainer: ({ children }: any) => (
      <div className="recharts-responsive-container" style={{ width: 800, height: 300 }}>
        {children}
      </div>
    ),
  };
});

// Mock API
vi.mock('@/entities/performance/api', () => ({
  performanceApi: {
    getOverview: vi.fn(),
    listIndicators: vi.fn(),
    getIndicator: vi.fn(),
    listBaselines: vi.fn(),
    createBaseline: vi.fn(),
    listTargets: vi.fn(),
    createTarget: vi.fn(),
    runEvaluation: vi.fn(),
    getEvaluation: vi.fn(),
    listImpactAssessments: vi.fn(),
    createImpactAssessment: vi.fn(),
    reviewImpactAssessment: vi.fn(),
    getTrends: vi.fn(),
    generateReport: vi.fn(),
  },
}));

const mockOverview: PerformanceOverview = {
  totalWaterDeliveredM3: 4500000,
  totalWaterDeliveredEvidence: 'OBSERVED',

  deliveryFulfillmentPct: 92.4,
  deliveryFulfillmentEvidence: 'OBSERVED',

  averageConveyanceLossPct: 8.8,
  totalLossVolumeM3: 396000,
  conveyanceLossEvidence: 'CALCULATED',

  quotaCompliancePct: 94.6,
  quotaComplianceEvidence: 'CALCULATED',

  distributionEquityCv: 0.14,
  distributionEquityLevel: 'EQUITABLE',
  distributionEquityEvidence: 'CALCULATED',

  totalIncidents: 12,
  overdueIncidents: 1,
  verifiedResolvedIncidents: 10,
  verifiedResolutionRatePct: 83.3,
  incidentRecurrenceRatePct: 4.2,
  accountabilityEvidence: 'OBSERVED',

  sensorObservedCoveragePct: 91.5,
  activeSensorsCount: 28,
  dataQualityEvidence: 'OBSERVED',

  verifiedConservationSavingsM3: 42000,
  conservationEvidence: 'CALCULATED',
};

const mockIndicators: PerformanceIndicator[] = [
  {
    id: 'ind-001',
    code: 'WDP_DELIVERY_FULFILLMENT',
    name: 'Suv Yetkazib Berish Rejasi Ijrosi',
    description: 'Haqiqiy yetkazilgan suv hajmining tasdiqlangan rejaga nisbati',
    category: 'DELIVERY_RELIABILITY',
    measurementUnit: 'PERCENT',
    calculationMethod: 'VOLUMETRIC_RATIO',
    aggregationMethod: 'WEIGHTED_AVERAGE',
    supportedGeographicScope: 'CANAL_REACH',
    evidenceRequirement: 'Bosh post va qabul nuqtalaridagi oʻlchovlar',
    minimumDataQualityPct: 90.0,
    applicableTimePeriod: 'MONTHLY',
    sourceModule: 'REGIONAL_NETWORK',
    calculationVersion: '1.0',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  },
  {
    id: 'ind-002',
    code: 'CCP_LOSS_PERCENTAGE',
    name: 'Kanal Oʻzanidagi Filtrlash va Bugʻlanish Yoʻqotishi',
    description: 'Bosh post va quyi postlar orasidagi mass-balans oʻlchov farqi',
    category: 'CONVEYANCE_EFFICIENCY',
    measurementUnit: 'PERCENT',
    calculationMethod: 'CONVEYANCE_BALANCE_RATIO',
    aggregationMethod: 'WEIGHTED_AVERAGE',
    supportedGeographicScope: 'CANAL_REACH',
    evidenceRequirement: 'Ikki tomonlama gidropost telemetriyasi',
    minimumDataQualityPct: 85.0,
    applicableTimePeriod: 'MONTHLY',
    sourceModule: 'WATER_ACCOUNTING',
    calculationVersion: '1.0',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  },
];

const mockBaselines: PerformanceBaseline[] = [
  {
    id: 'base-001',
    indicatorCode: 'WDP_DELIVERY_FULFILLMENT',
    name: '2025-Yil Magistral Kanal Yetkazish Bazasi',
    description: '2025-yilgi oʻrtacha qoniqish koʻrsatkichi',
    scopeType: 'CANAL_REACH',
    scopeId: 'canal-bozyop-01',
    scopeName: 'Boʻzyop Kanali PK0-PK12',
    periodStart: '2025-05-01T00:00:00Z',
    periodEnd: '2025-09-30T23:59:59Z',
    baselineValue: 81.2,
    measurementUnit: 'PERCENT',
    dataCompletenessPct: 96.5,
    evidenceClassification: 'CALCULATED',
    baselineQuality: 'VERIFIED_BASELINE',
    calculationMethod: 'HISTORICAL_WEIGHTED_AVERAGE',
    seasonContext: 'MAIN_IRRIGATION_SEASON',
    approvalStatus: 'APPROVED',
    approvedBy: 'Suv Xoʻjaligi Boshqarmasi',
    createdAt: '2025-10-15T10:00:00Z',
  },
];

const mockTargets: PerformanceTarget[] = [
  {
    id: 'targ-001',
    indicatorCode: 'WDP_DELIVERY_FULFILLMENT',
    title: 'Boʻzyop Kanali Reja Ijrosi Meʼyori',
    scopeType: 'CANAL_REACH',
    scopeId: 'canal-bozyop-01',
    scopeName: 'Boʻzyop Kanali PK0-PK12',
    targetType: 'MINIMUM',
    targetCategory: 'OPERATIONAL_TARGET',
    targetValue: 92.0,
    measurementUnit: 'PERCENT',
    effectiveStart: '2026-01-01T00:00:00Z',
    effectiveEnd: '2026-12-31T23:59:59Z',
    status: 'APPROVED',
    targetOwner: 'Yangiariq Tuman Suv Taʼminoti',
    targetVersion: '1.0',
    createdAt: '2026-01-01T00:00:00Z',
  },
];

const mockImpactAssessments: ImpactAssessment[] = [
  {
    id: 'imp-001',
    assessmentNumber: 'IMP-2026-0001',
    title: 'Boʻzyop Magistral Kanali PK4-PK8 Oʻzanini Betonlash',
    interventionType: 'CANAL_MAINTENANCE',
    interventionId: 'act-001',
    interventionReference: 'ACT-2026-0042',
    interventionDate: '2026-03-15T00:00:00Z',
    indicatorCode: 'CCP_LOSS_PERCENTAGE',
    scopeType: 'CANAL_REACH',
    scopeName: 'Boʻzyop Kanali PK4-PK8',
    baselinePeriodStart: '2025-06-01T00:00:00Z',
    baselinePeriodEnd: '2025-08-31T00:00:00Z',
    baselineValue: 14.5,
    evaluationPeriodStart: '2026-06-01T00:00:00Z',
    evaluationPeriodEnd: '2026-08-31T00:00:00Z',
    postInterventionValue: 7.2,
    absoluteChange: -7.3,
    percentageChange: -50.3,
    changeDirection: 'FAVORABLE',
    measurementUnit: 'PERCENT',
    evidenceClassification: 'OBSERVED',
    baselineQuality: 'VERIFIED_BASELINE',
    attributionLevel: 'DIRECTLY_VERIFIED',
    confoundingFactors: 'Yogʻingarchilik va oqim tezligi oʻzgarmas saqlangan',
    comparisonValid: true,
    methodologyNotes: 'Ikki tomonlama gidropost ultratovush hisoblagichlari orqali tekshirilgan',
    reviewStatus: 'VERIFIED_OUTCOME',
    reviewerName: 'Tuman Bosh Muhandisi',
    reviewNotes: 'Dalillar toʻliq tasdiqlandi, yoʻqotish 7.3% ga pasaygan',
    reviewedAt: '2026-09-06T10:00:00Z',
    createdAt: '2026-09-05T14:30:00Z',
  },
];

const mockTrendData: PerformanceTrendSeries = {
  indicatorCode: 'WDP_DELIVERY_FULFILLMENT',
  indicatorName: 'Suv Yetkazib Berish Rejasi Ijrosi',
  measurementUnit: '%',
  dataPoints: [
    {
      timestamp: '2026-06-01T00:00:00Z',
      label: 'Iyun',
      actualValue: 84.0,
      baselineValue: 81.2,
      targetValue: 92.0,
      evidenceClassification: 'OBSERVED',
      isMissing: false,
      isSimulated: false,
    },
    {
      timestamp: '2026-07-01T00:00:00Z',
      label: 'Iyul',
      actualValue: null,
      baselineValue: 81.2,
      targetValue: 92.0,
      evidenceClassification: 'MISSING',
      isMissing: true,
      isSimulated: false,
    },
    {
      timestamp: '2026-08-01T00:00:00Z',
      label: 'Avgust',
      actualValue: 94.5,
      baselineValue: 81.2,
      targetValue: 92.0,
      evidenceClassification: 'SIMULATED',
      isMissing: false,
      isSimulated: true,
    },
  ],
};

function renderWithProviders(ui: React.ReactElement) {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });
  return render(
    <QueryClientProvider client={queryClient}>
      <ToastProvider>{ui}</ToastProvider>
    </QueryClientProvider>
  );
}

describe('Milestone 08.17: Regional Water Operations Performance & Impact Assessment Suite', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('1. EvidenceClassificationBadge renders correct classification styles and labels', () => {
    const { rerender } = render(<EvidenceClassificationBadge classification="OBSERVED" />);
    expect(screen.getByText(/OBSERVED/i)).toBeInTheDocument();

    rerender(<EvidenceClassificationBadge classification="CALCULATED" />);
    expect(screen.getByText(/CALCULATED/i)).toBeInTheDocument();

    rerender(<EvidenceClassificationBadge classification="ESTIMATED" />);
    expect(screen.getByText(/ESTIMATED/i)).toBeInTheDocument();

    rerender(<EvidenceClassificationBadge classification="SIMULATED" />);
    expect(screen.getByText(/SIMULATED/i)).toBeInTheDocument();

    rerender(<EvidenceClassificationBadge classification="MISSING" />);
    expect(screen.getByText(/MISSING/i)).toBeInTheDocument();
  });

  it('2. AttributionLevelBadge renders conservative attribution levels', () => {
    const { rerender } = render(<AttributionLevelBadge level="DIRECTLY_VERIFIED" />);
    expect(screen.getByText('TOʻGʻRIDAN-TOʻGʻRI TASDIQLANGAN')).toBeInTheDocument();

    rerender(<AttributionLevelBadge level="STRONGLY_ASSOCIATED" />);
    expect(screen.getByText('KUCHLI BOGʻLIQLIK')).toBeInTheDocument();

    rerender(<AttributionLevelBadge level="ASSOCIATED" />);
    expect(screen.getByText('BOGʻLIQ (KORRELYATSIYA)')).toBeInTheDocument();

    rerender(<AttributionLevelBadge level="INCONCLUSIVE" />);
    expect(screen.getByText('NOANIQ (INCONCLUSIVE)')).toBeInTheDocument();

    rerender(<AttributionLevelBadge level="NOT_ASSESSABLE" />);
    expect(screen.getByText('BAHOLAB BOʻLMAYDI')).toBeInTheDocument();
  });

  it('3. PerformanceOverviewCards displays core metrics and evidence badges', () => {
    render(<PerformanceOverviewCards overview={mockOverview} />);

    expect(screen.getByText('Yetkazib berish rejasi ijrosi')).toBeInTheDocument();
    expect(screen.getByText('92.4%')).toBeInTheDocument();

    expect(screen.getByText('Magistral kanal yoʻqotishi')).toBeInTheDocument();
    expect(screen.getByText('8.8%')).toBeInTheDocument();

    expect(screen.getByText('Kvota limiti intizomi')).toBeInTheDocument();
    expect(screen.getByText('94.6%')).toBeInTheDocument();

    expect(screen.getByText('Taqsimot tengligi (CV)')).toBeInTheDocument();
    expect(screen.getByText('0.14')).toBeInTheDocument();

    expect(screen.getByText('Datchiklar empirik qamrovi')).toBeInTheDocument();
    expect(screen.getByText('91.5%')).toBeInTheDocument();
  });

  it('4. PerformanceTrendChart displays interval buttons and flags missing/simulated data points', () => {
    renderWithProviders(
      <PerformanceTrendChart
        trendData={mockTrendData}
        selectedInterval="MONTHLY"
        onIntervalChange={vi.fn()}
        selectedIndicatorCode="WDP_DELIVERY_FULFILLMENT"
        onIndicatorCodeChange={vi.fn()}
      />
    );

    expect(screen.getByText('Vaqt Kesimidagi Dinamika va Trend Tahlili')).toBeInTheDocument();
    expect(screen.getByText('Kunlik')).toBeInTheDocument();
    expect(screen.getByText('Haftalik')).toBeInTheDocument();
    expect(screen.getByText('Oylik')).toBeInTheDocument();
    expect(screen.getByText('Mavsumiy')).toBeInTheDocument();

    // Verify warning for missing and simulated points
    expect(screen.getByText(/Ayrim oraliqlarda oʻlchov yetishmaydi/i)).toBeInTheDocument();
    expect(screen.getByText(/Rang bilan belgilangan oraliq ML\/gidravlik model bahosi/i)).toBeInTheDocument();
  });

  it('5. PerformanceIndicatorCatalog lists indicators and filters by search', () => {
    const handleSelect = vi.fn();
    renderWithProviders(
      <PerformanceIndicatorCatalog
        indicators={mockIndicators}
        onSelectIndicator={handleSelect}
      />
    );

    expect(screen.getByText(/Operatsion Koʻrsatkichlar Katalogi/i)).toBeInTheDocument();
    expect(screen.getByText('Suv Yetkazib Berish Rejasi Ijrosi')).toBeInTheDocument();
    expect(screen.getByText('Kanal Oʻzanidagi Filtrlash va Bugʻlanish Yoʻqotishi')).toBeInTheDocument();

    // Test search filter
    const searchInput = screen.getByPlaceholderText(/Indikatorni qidirish/i);
    fireEvent.change(searchInput, { target: { value: 'Filtrlash' } });

    expect(screen.queryByText('Suv Yetkazib Berish Rejasi Ijrosi')).not.toBeInTheDocument();
    expect(screen.getByText('Kanal Oʻzanidagi Filtrlash va Bugʻlanish Yoʻqotishi')).toBeInTheDocument();
  });

  it('6. ImpactAssessmentWorkspace displays intervention records and conservative attribution', () => {
    const handleReview = vi.fn();
    renderWithProviders(
      <ImpactAssessmentWorkspace
        assessments={mockImpactAssessments}
        onReviewClick={handleReview}
      />
    );

    expect(screen.getByText(/Amaliy Taʼsir va Natijadorlikni Baholash Markazi/i)).toBeInTheDocument();
    expect(screen.getByText('Boʻzyop Magistral Kanali PK4-PK8 Oʻzanini Betonlash')).toBeInTheDocument();
    expect(screen.getByText('TOʻGʻRIDAN-TOʻGʻRI TASDIQLANGAN')).toBeInTheDocument();
    expect(screen.getByText('Farq: -7.3 PERCENT')).toBeInTheDocument();
    expect(screen.getByText('-50.3%')).toBeInTheDocument();
    expect(screen.getByText('Tasdiqlangan')).toBeInTheDocument();
  });

  it('7. BaselineTargetManager toggles between operational targets and historical baselines', () => {
    renderWithProviders(
      <BaselineTargetManager
        baselines={mockBaselines}
        targets={mockTargets}
      />
    );

    // Initial tab is Operatsion Meʼyorlar
    expect(screen.getByText('Boʻzyop Kanali Reja Ijrosi Meʼyori')).toBeInTheDocument();
    expect(screen.getByText('92 PERCENT')).toBeInTheDocument();

    // Switch to Baselines tab
    const baselinesTab = screen.getByText('Tarixiy Bazalar (Baselines)');
    fireEvent.click(baselinesTab);

    expect(screen.getByText('2025-Yil Magistral Kanal Yetkazish Bazasi')).toBeInTheDocument();
    expect(screen.getByText('81.2 PERCENT')).toBeInTheDocument();
  });

  it('8. GeographicPerformanceTable displays regional levels (canals, WUAs, fields)', () => {
    renderWithProviders(<GeographicPerformanceTable />);

    expect(screen.getByText('Hududiy Geografik Samaradorlik Koʻrsatkichlari')).toBeInTheDocument();
    expect(screen.getByText('Boʻzyop Bosh Kanali (PK0 - PK12)')).toBeInTheDocument();

    // Switch to WUA level
    const wuaButton = screen.getByText('WUA Uyushmalari');
    fireEvent.click(wuaButton);
    expect(screen.getByText('Bogʻolon WUA')).toBeInTheDocument();

    // Switch to Fields level
    const fieldsButton = screen.getByText('Dalalar');
    fireEvent.click(fieldsButton);
    expect(screen.getByText(/Field F-101/i)).toBeInTheDocument();
  });

  it('9. CreateImpactAssessmentDialog submits impact assessment form', async () => {
    const handleClose = vi.fn();
    (performanceApi.createImpactAssessment as any).mockResolvedValueOnce({
      id: 'imp-new',
      title: 'Kanal tozalash natijasi',
    });

    renderWithProviders(
      <CreateImpactAssessmentDialog isOpen={true} onClose={handleClose} />
    );

    expect(screen.getByText('Yangi Taʼsir Baholash Yaratish')).toBeInTheDocument();
    const titleInput = screen.getByPlaceholderText(/Masalan: Boʻzyop kanali oʻzanini betonlash natijasi/i);
    fireEvent.change(titleInput, { target: { value: 'Kanal tozalash natijasi' } });

    const submitBtn = screen.getByText('Baholashni Saqlash');
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(performanceApi.createImpactAssessment).toHaveBeenCalled();
      expect(handleClose).toHaveBeenCalled();
    });
  });

  it('10. ReviewImpactAssessmentDialog allows auditor review submission', async () => {
    const handleClose = vi.fn();
    (performanceApi.reviewImpactAssessment as any).mockResolvedValueOnce({
      id: 'imp-001',
      reviewStatus: 'VERIFIED_OUTCOME',
    });

    renderWithProviders(
      <ReviewImpactAssessmentDialog
        isOpen={true}
        onClose={handleClose}
        assessment={mockImpactAssessments[0]}
      />
    );

    expect(screen.getByText('Taʼsir Baholashni Koʻrib Chiqish va Tasdiqlash')).toBeInTheDocument();
    expect(screen.getByText(/Boʻzyop Magistral Kanali PK4-PK8 Oʻzanini Betonlash/i)).toBeInTheDocument();

    const notesTextarea = screen.getByPlaceholderText(/Joyiga chiqib koʻrik oʻtkazildi, oʻlchov koʻrsatkichlari solishtirildi.../i);
    fireEvent.change(notesTextarea, { target: { value: 'Qayta tekshirildi, dalolatnoma imzolandi' } });

    const submitBtn = screen.getByText('Audit Xulosasini Tasdiqlash');
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(performanceApi.reviewImpactAssessment).toHaveBeenCalledWith(
        'imp-001',
        expect.objectContaining({
          reviewStatus: 'VERIFIED_OUTCOME',
          reviewNotes: 'Qayta tekshirildi, dalolatnoma imzolandi',
        })
      );
      expect(handleClose).toHaveBeenCalled();
    });
  });

  it('11. CreateBaselineDialog handles baseline submission', async () => {
    const handleClose = vi.fn();
    (performanceApi.createBaseline as any).mockResolvedValueOnce({
      id: 'base-new',
      name: 'Yangi Tarixiy Baza',
    });

    renderWithProviders(
      <CreateBaselineDialog isOpen={true} onClose={handleClose} />
    );

    expect(screen.getByText('Yangi Tarixiy Baza (Baseline) Qoʻshish')).toBeInTheDocument();
    const nameInput = screen.getByPlaceholderText(/2025-yilgi mavsum oʻrtacha yetkazish bazasi/i);
    fireEvent.change(nameInput, { target: { value: 'Yangi Tarixiy Baza' } });

    const submitBtn = screen.getByText('Bazani Saqlash');
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(performanceApi.createBaseline).toHaveBeenCalled();
      expect(handleClose).toHaveBeenCalled();
    });
  });

  it('12. CreateTargetDialog handles operational target submission', async () => {
    const handleClose = vi.fn();
    (performanceApi.createTarget as any).mockResolvedValueOnce({
      id: 'targ-new',
      title: 'Yangi Operatsion Meʼyor',
    });

    renderWithProviders(
      <CreateTargetDialog isOpen={true} onClose={handleClose} />
    );

    expect(screen.getByText('Yangi Operatsion Meʼyor (Target) Belgilash')).toBeInTheDocument();
    const titleInput = screen.getByPlaceholderText(/Reja bajarilishi eng kamida 92%/i);
    fireEvent.change(titleInput, { target: { value: 'Yangi Operatsion Meʼyor' } });

    const submitBtn = screen.getByText('Meʼyorni Saqlash');
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(performanceApi.createTarget).toHaveBeenCalled();
      expect(handleClose).toHaveBeenCalled();
    });
  });

  it('13. PerformanceReportDialog generates formal audit report with legal disclaimer', async () => {
    const handleClose = vi.fn();
    (performanceApi.generateReport as any).mockResolvedValueOnce({
      reportId: 'rep-001',
      reportTitle: 'Yangiariq Suv Xoʻjaligi Operatsion Samaradorlik Hisoboti',
      generatedAt: '2026-11-08T12:00:00Z',
      generatedBy: 'Auditor',
      reportingPeriodStart: '2026-10-01T00:00:00Z',
      reportingPeriodEnd: '2026-11-01T00:00:00Z',
      geographicScope: 'Yangiariq Tumani',
      legalDisclaimer: 'Ushbu hisobot qonuniy kuchga ega emas, faqat tavsiyaviy xarakterga ega.',
      overview: mockOverview,
      indicators: mockIndicators,
      evaluations: [],
      impactAssessments: mockImpactAssessments,
    });

    renderWithProviders(
      <PerformanceReportDialog isOpen={true} onClose={handleClose} />
    );

    expect(screen.getByText('Mintaqaviy Operatsion Samaradorlik Hisoboti')).toBeInTheDocument();

    await waitFor(() => {
      expect(performanceApi.generateReport).toHaveBeenCalled();
      expect(screen.getByText(/Eslatma:/i)).toBeInTheDocument();
      expect(screen.getByText(/Ushbu hisobot qonuniy kuchga ega emas/i)).toBeInTheDocument();
    });
  });

  it('14. RegionalPerformancePage renders full page and switches workspace tabs', async () => {
    (performanceApi.getOverview as any).mockResolvedValue(mockOverview);
    (performanceApi.listIndicators as any).mockResolvedValue(mockIndicators);
    (performanceApi.listBaselines as any).mockResolvedValue(mockBaselines);
    (performanceApi.listTargets as any).mockResolvedValue(mockTargets);
    (performanceApi.getTrends as any).mockResolvedValue(mockTrendData);
    (performanceApi.listImpactAssessments as any).mockResolvedValue(mockImpactAssessments);

    renderWithProviders(<RegionalPerformancePage />);

    expect(screen.getByText('Operatsion Samaradorlik va Taʼsir Baholash Markazi')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('Yetkazib berish rejasi ijrosi')).toBeInTheDocument();
    });

    // Switch to Impact tab
    const impactTab = screen.getByText(/Amaliy Taʼsir Baholash/i);
    fireEvent.click(impactTab);
    expect(screen.getByText(/Amaliy Taʼsir va Natijadorlikni Baholash Markazi/i)).toBeInTheDocument();

    // Switch to Baselines tab
    const baselinesTab = screen.getByText(/Meʼyorlar & Tarixiy Baza/i);
    fireEvent.click(baselinesTab);
    expect(screen.getByText('Operatsion Meʼyorlar (Targets & Limits)')).toBeInTheDocument();

    // Switch to Catalog tab
    const catalogTab = screen.getByText(/Indikatorlar Katalogi/i);
    fireEvent.click(catalogTab);
    expect(screen.getByText(/Operatsion Koʻrsatkichlar Katalogi/i)).toBeInTheDocument();
  });
});
