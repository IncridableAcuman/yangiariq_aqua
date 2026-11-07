import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import React from 'react';
import {
  AccountingEvidenceBadge,
  RegionalWaterBalanceCards,
  WaterBalanceChart,
  WaterLedgerTable,
  CanalLossAnalysisTable,
  AllocationComplianceTable,
  WuaEquityComparison,
  ConservationOpportunityCard,
  WaterReconciliationPanel,
  ReconciliationReviewDialog,
  FieldAccountingDetailModal,
} from '@/features/water-accounting/components';
import { RegionalWaterAccountingPage } from '@/pages/water-accounting/RegionalWaterAccountingPage';
import {
  RegionalWaterBalanceResponse,
  RegionalWaterOverviewResponse,
  WaterLedgerEntryResponse,
  ConveyanceLossResponse,
  AllocationComplianceResponse,
  DistributionEquityResponse,
  ConservationOpportunityResponse,
  WaterReconciliationResponse,
  FieldWaterAccountingResponse,
} from '@/entities/water-accounting';
import { waterAccountingApi } from '@/entities/water-accounting/api';

// Mock recharts ResponsiveContainer and components for jsdom
vi.mock('recharts', async () => {
  const actual = await vi.importActual<Record<string, unknown>>('recharts');
  return {
    ...actual,
    ResponsiveContainer: ({ children }: { children: React.ReactNode }) => (
      <div data-testid="responsive-container" style={{ width: 800, height: 400 }}>
        {children}
      </div>
    ),
    BarChart: ({ children }: { children: React.ReactNode }) => (
      <div data-testid="bar-chart">{children}</div>
    ),
    Bar: () => <div data-testid="bar" />,
    XAxis: () => <div data-testid="xaxis" />,
    YAxis: () => <div data-testid="yaxis" />,
    Tooltip: () => <div data-testid="tooltip" />,
    CartesianGrid: () => <div data-testid="grid" />,
    Cell: () => <div data-testid="cell" />,
  };
});

// Mock AuthContext
vi.mock('@/features/auth/context/AuthContext', () => ({
  useAuth: () => ({
    user: {
      id: 'operator-1',
      firstName: 'Bahodir',
      lastName: 'Olimov',
      role: 'OPERATOR',
    },
    isAuthenticated: true,
  }),
}));

describe('Milestone 08.15: Regional Water Accounting & Distribution Equity Suite', () => {
  let queryClient: QueryClient;

  const mockBalance: RegionalWaterBalanceResponse = {
    boundaryDimension: 'DISTRICT',
    boundaryName: 'Yangiariq Tumani',
    boundaryId: 'dist-1',
    periodType: 'MONTHLY',
    periodStart: '2026-06-01T00:00:00Z',
    periodEnd: '2026-06-30T23:59:59Z',
    openingBalanceM3: 50000.0,
    inflowReceivedM3: 1500000.0,
    waterTransferredInM3: 0.0,
    waterTransferredOutM3: 0.0,
    waterDeliveredM3: 1250000.0,
    waterConsumedM3: 1100000.0,
    verifiedLossesM3: 250000.0,
    recordedReturnFlowM3: 40000.0,
    closingBalanceM3: 160000.0,
    discrepancyM3: 0.0,
    tolerancePercent: 5.0,
    isReconciled: true,
    evidenceClassification: 'CALCULATED',
    dataCompletenessPercent: 96.5,
  };

  const mockOverview: RegionalWaterOverviewResponse = {
    districtBalance: mockBalance,
    totalLedgerEntries: 12,
    totalReconciliations: 3,
    unverifiedReconciliationsCount: 1,
    averageConveyanceLossPercent: 16.67,
    nonCompliantParcelsCount: 2,
    activeConservationOpportunitiesCount: 3,
    equityStatusSummary: 'Adolatli taqsimot meʼyorda (CV = 0.12)',
  };

  const mockLedgerEntries: WaterLedgerEntryResponse[] = [
    {
      id: 'led-1',
      entryType: 'SOURCE_INFLOW',
      accountingBoundary: 'DISTRICT',
      volumeM3: 500000,
      flowRateM3S: 5.2,
      sourceNodeOrCanal: 'Tuyamuyun Intake',
      destinationNodeOrField: 'Polvon Main Canal',
      fieldId: null,
      wuaId: null,
      edgeId: 'edge-1',
      dispatchScheduleId: 'sch-1',
      allocationId: 'alloc-1',
      eventTimestamp: '2026-06-05T08:00:00Z',
      evidenceClassification: 'OBSERVED',
      dataSource: 'SCADA_TELEMETRY',
      notes: 'Main intake canal telemetry',
      recordedBy: 'system',
      isCorrection: false,
      replacesEntryId: null,
      createdAt: '2026-06-05T08:00:00Z',
    },
    {
      id: 'led-2',
      entryType: 'TURNOUT_DELIVERY',
      accountingBoundary: 'FIELD',
      volumeM3: 45000,
      flowRateM3S: 0.8,
      sourceNodeOrCanal: 'Polvon Turnout 1',
      destinationNodeOrField: 'Field 101 Gate',
      fieldId: 'field-101',
      wuaId: 'wua-1',
      edgeId: 'edge-2',
      dispatchScheduleId: 'sch-1',
      allocationId: 'alloc-2',
      eventTimestamp: '2026-06-06T10:00:00Z',
      evidenceClassification: 'OBSERVED',
      dataSource: 'DISPATCH_CONFIRMATION',
      notes: 'Delivered to parcel gate',
      recordedBy: 'system',
      isCorrection: false,
      replacesEntryId: null,
      createdAt: '2026-06-06T10:00:00Z',
    },
  ];

  const mockLosses: ConveyanceLossResponse[] = [
    {
      edgeId: 'edge-1',
      canalName: 'Polvon-Yopiq Magistral Kanali',
      edgeType: 'MAIN_CANAL',
      startNodeName: 'Tuyamuyun Intake',
      endNodeName: 'Polvon Turnout 1',
      periodStart: '2026-06-01T00:00:00Z',
      periodEnd: '2026-06-30T23:59:59Z',
      upstreamVolumeM3: 200000,
      downstreamVolumeM3: 164000,
      lossVolumeM3: 36000,
      lossPercentage: 18.0,
      classification: 'SUSPECTED_LOSS',
      evidenceClassification: 'CALCULATED',
      confidence: 'HIGH',
      explanation: 'Loss percentage (18.0%) exceeds normal threshold (15%)',
    },
  ];

  const mockCompliances: AllocationComplianceResponse[] = [
    {
      targetId: 'field-101',
      targetName: 'Bogʻdorchilik 1-Uchastka',
      targetType: 'FIELD',
      wuaName: 'Yangiariq Suv Ziyosi',
      cropType: 'Paxta (Cotton)',
      areaHectares: 25.0,
      allocatedVolumeM3: 50000,
      reservedVolumeM3: 5000,
      approvedDispatchVolumeM3: 45000,
      actualDeliveredVolumeM3: 44000,
      actualConsumptionM3: 43500,
      remainingAllocationM3: 6500,
      overDeliveryM3: 0,
      underDeliveryM3: 0,
      deliveryCompletionPercent: 87.0,
      status: 'ALLOCATION_RESPECTED',
      evidenceClassification: 'OBSERVED',
      notes: 'Optimal irrigation scheduling compliance',
    },
  ];

  const mockEquities: DistributionEquityResponse[] = [
    {
      clusterId: 'wua-1',
      clusterName: 'Yangiariq Suv Ziyosi',
      clusterType: 'WUA',
      sampleCount: 15,
      meanDeliveryPerHectareM3: 1850.5,
      varianceM3: 4200.0,
      coefficientOfVariation: 0.12,
      meanAllocationFulfillmentPercent: 89.2,
      averageDeliveryDelayHours: 1.4,
      disparityLevel: 'BALANCED',
      fairnessPolicyApplied: 'EQUAL_PROPORTIONAL_ALLOCATION',
      isConsistentWithPolicy: true,
      evidenceClassification: 'OBSERVED',
      supportingEvidence: ['SCADA flow meters'],
      notes: 'Maintain current rotation equity',
    },
  ];

  const mockOpportunities: ConservationOpportunityResponse[] = [
    {
      id: 'opp-1',
      title: 'Polvon-Yopiq Seepage Reduction',
      category: 'CONVEYANCE_LOSS',
      affectedComponentType: 'CANAL_EDGE',
      affectedComponentId: 'edge-1',
      affectedComponentName: 'Polvon-Yopiq Magistral Kanali',
      problemDescription: 'Lining the unlined section will reduce elevated conveyance loss.',
      estimatedPotentialSavingsM3: 25000,
      estimationMethodology: 'Differential flow telemetry mass-balance calculation',
      confidence: 'HIGH',
      operationalImpact: 'HIGH',
      suggestedAction: 'Repair unlined concrete sections',
      targetWorkflowUri: '/network/dispatch',
      evidenceClassification: 'CALCULATED',
      supportingEvidence: ['Differential flow: 36,000 m3 loss'],
    },
  ];

  const mockReconciliations: WaterReconciliationResponse[] = [
    {
      id: 'rec-1',
      boundaryType: 'CANAL_REACH',
      boundaryName: 'Polvon-Yopiq Reach 1',
      targetId: 'edge-1',
      periodStart: '2026-06-01T00:00:00Z',
      periodEnd: '2026-06-30T23:59:59Z',
      expectedVolumeM3: 200000,
      recordedVolumeM3: 198000,
      discrepancyVolumeM3: -2000,
      discrepancyPercent: 1.0,
      tolerancePercent: 5.0,
      status: 'RECONCILED',
      evidenceClassification: 'CALCULATED',
      explanation: 'Minor meter deviation',
      reviewedBy: 'operator-1',
      reviewedAt: '2026-07-01T10:00:00Z',
      reviewNotes: 'Audit verified with canal meters.',
      createdAt: '2026-07-01T00:00:00Z',
      updatedAt: '2026-07-01T10:00:00Z',
    },
  ];

  const mockFieldAccounting: FieldWaterAccountingResponse = {
    fieldId: 'field-101',
    fieldName: 'Bogʻdorchilik 1-Uchastka',
    ownerId: 'farmer-1',
    wuaName: 'Yangiariq Suv Ziyosi',
    areaHectares: 25.0,
    cropType: 'Paxta (Cotton)',
    compliance: mockCompliances[0],
    recentMovements: mockLedgerEntries,
    recommendations: mockOpportunities,
  };

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    });
    vi.clearAllMocks();
  });

  const renderWithProviders = (ui: React.ReactElement) => {
    return render(<QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>);
  };

  it('1. AccountingEvidenceBadge renders all 5 evidence classifications', () => {
    const { rerender } = renderWithProviders(
      <AccountingEvidenceBadge classification="OBSERVED" />
    );
    expect(screen.getByText(/Kuzatilgan \(Observed\)/i)).toBeInTheDocument();

    rerender(<AccountingEvidenceBadge classification="CALCULATED" />);
    expect(screen.getByText(/Hisoblangan \(Calculated\)/i)).toBeInTheDocument();

    rerender(<AccountingEvidenceBadge classification="ESTIMATED" />);
    expect(screen.getByText(/Baholangan \(Estimated\)/i)).toBeInTheDocument();

    rerender(<AccountingEvidenceBadge classification="SIMULATED" />);
    expect(screen.getByText(/Simulyatsiya \(Simulated\)/i)).toBeInTheDocument();

    rerender(<AccountingEvidenceBadge classification="MISSING" />);
    expect(screen.getByText(/Mavjud emas \(Missing\)/i)).toBeInTheDocument();
  });

  it('2. RegionalWaterBalanceCards renders intake, delivery, consumption, losses and balance status', () => {
    renderWithProviders(<RegionalWaterBalanceCards balance={mockBalance} />);

    expect(screen.getByText(/Kiruvchi Suv \(Inflow\)/i)).toBeInTheDocument();
    expect(screen.getByText(/Yetkazilgan \(Delivered\)/i)).toBeInTheDocument();
    expect(screen.getByText(/Isteʼmol \(Consumed\)/i)).toBeInTheDocument();
    expect(screen.getByText(/Kanal Isrofi \(Losses\)/i)).toBeInTheDocument();
    expect(screen.getByText(/Qoldiq Balans \(Closing\)/i)).toBeInTheDocument();
    expect(screen.getByText(/Muvofiqlashtirilgan \(Reconciled\)/i)).toBeInTheDocument();
  });

  it('3. WaterBalanceChart renders mass balance breakdown', () => {
    renderWithProviders(<WaterBalanceChart balance={mockBalance} />);
    expect(screen.getByText(/Suv Taqsimoti Bosqichlari \(Water Balance Stages\)/i)).toBeInTheDocument();
  });

  it('4. WaterLedgerTable renders immutable transactions and filters', () => {
    renderWithProviders(<WaterLedgerTable entries={mockLedgerEntries} />);

    expect(screen.getAllByText(/Bosh Oqim/i).length).toBeGreaterThanOrEqual(1);
    expect(screen.getAllByText(/Quloq Yetkazmasi/i).length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('Tuyamuyun Intake')).toBeInTheDocument();

    // Filter test
    const select = screen.getByRole('combobox');
    fireEvent.change(select, { target: { value: 'TURNOUT_DELIVERY' } });
    expect(screen.queryByText('Tuyamuyun Intake')).not.toBeInTheDocument();
    expect(screen.getByText('Polvon Turnout 1')).toBeInTheDocument();
  });

  it('5. CanalLossAnalysisTable renders canal losses and classification badge', () => {
    renderWithProviders(<CanalLossAnalysisTable losses={mockLosses} />);

    expect(screen.getByText('Polvon-Yopiq Magistral Kanali')).toBeInTheDocument();
    expect(screen.getByText(/Shubhali Isrof \(15–30%\)/i)).toBeInTheDocument();
    expect(screen.getByText('18.00%')).toBeInTheDocument();
  });

  it('6. AllocationComplianceTable displays compliance items and triggers field detail callback', () => {
    const handleSelectField = vi.fn();
    renderWithProviders(
      <AllocationComplianceTable compliances={mockCompliances} onSelectField={handleSelectField} />
    );

    expect(screen.getByText('Bogʻdorchilik 1-Uchastka')).toBeInTheDocument();
    expect(screen.getAllByText(/Meʼyorda \(Respected\)/i).length).toBeGreaterThanOrEqual(1);
    expect(screen.getByText('87.0%')).toBeInTheDocument();

    const viewBtn = screen.getByTitle('Dala boʻyicha toʻliq hisob-kitobni koʻrish');
    fireEvent.click(viewBtn);
    expect(handleSelectField).toHaveBeenCalledWith('field-101');
  });

  it('7. WuaEquityComparison displays equity distribution and CV coefficient', () => {
    renderWithProviders(<WuaEquityComparison equityList={mockEquities} />);

    expect(screen.getByText('Yangiariq Suv Ziyosi')).toBeInTheDocument();
    expect(screen.getByText(/Muvozanatli \(Balanced\)/i)).toBeInTheDocument();
    expect(screen.getByText('0.120')).toBeInTheDocument(); // CV
    expect(screen.getByText(/1[,. ]850/i)).toBeInTheDocument();
  });

  it('8. ConservationOpportunityCard handles non-fabricated quantifiable savings', () => {
    renderWithProviders(<ConservationOpportunityCard opportunity={mockOpportunities[0]} />);

    expect(screen.getByText('Polvon-Yopiq Seepage Reduction')).toBeInTheDocument();
    expect(screen.getByText(/25[,. ]000\s*m³/)).toBeInTheDocument();
    expect(screen.getByText(/Differential flow telemetry mass-balance calculation/i)).toBeInTheDocument();
  });

  it('9. WaterReconciliationPanel displays reconciliation status and allows opening review dialog', () => {
    renderWithProviders(
      <WaterReconciliationPanel reconciliations={mockReconciliations} />
    );

    expect(screen.getByText('Polvon-Yopiq Reach 1')).toBeInTheDocument();
    expect(screen.getByText(/Muvofiq \(Reconciled\)/i)).toBeInTheDocument();

    const reviewBtn = screen.getByText('Tekshirish');
    expect(reviewBtn).toBeInTheDocument();
    fireEvent.click(reviewBtn);

    expect(screen.getByText(/Tafovutni Rasmiylashtirish/i)).toBeInTheDocument();
  });

  it('10. ReconciliationReviewDialog triggers mutation upon form submit', async () => {
    const reviewSpy = vi.spyOn(waterAccountingApi, 'reviewReconciliation').mockResolvedValue(mockReconciliations[0]);
    const handleClose = vi.fn();
    const handleSuccess = vi.fn();

    renderWithProviders(
      <ReconciliationReviewDialog
        isOpen={true}
        item={mockReconciliations[0]}
        onClose={handleClose}
        onSuccess={handleSuccess}
      />
    );

    expect(screen.getByText(/Tafovutni Rasmiylashtirish/i)).toBeInTheDocument();

    const commentInput = screen.getByRole('textbox');
    fireEvent.change(commentInput, { target: { value: 'Verification confirmed against SCADA intake' } });

    const submitBtn = screen.getByText('Tasdiqlash va Qayd Qilish');
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(reviewSpy).toHaveBeenCalledWith('rec-1', {
        notes: 'Verification confirmed against SCADA intake',
      });
    });
  });

  it('11. FieldAccountingDetailModal displays turnout vs consumption breakdown', async () => {
    vi.spyOn(waterAccountingApi, 'getFieldAccounting').mockResolvedValue(mockFieldAccounting);
    const handleClose = vi.fn();

    renderWithProviders(
      <FieldAccountingDetailModal
        fieldId="field-101"
        onClose={handleClose}
      />
    );

    await waitFor(() => {
      expect(screen.getByText(/Bogʻdorchilik 1-Uchastka — Suv Hisoboti/i)).toBeInTheDocument();
      expect(screen.getByText(/Mavsumiy Kvota Muvofiqligi/i)).toBeInTheDocument();
      expect(screen.getByText(/44.*000\s*m³/)).toBeInTheDocument(); // Turnout delivered
      expect(screen.getByText(/43.*500\s*m³/)).toBeInTheDocument(); // Applied consumption
    });
  });

  it('12. RegionalWaterAccountingPage renders and navigates tabs', async () => {
    vi.spyOn(waterAccountingApi, 'getOverview').mockResolvedValue(mockOverview);
    vi.spyOn(waterAccountingApi, 'getBalance').mockResolvedValue(mockBalance);
    vi.spyOn(waterAccountingApi, 'getLedger').mockResolvedValue(mockLedgerEntries);
    vi.spyOn(waterAccountingApi, 'getConveyanceLosses').mockResolvedValue(mockLosses);
    vi.spyOn(waterAccountingApi, 'getAllocationCompliance').mockResolvedValue(mockCompliances);
    vi.spyOn(waterAccountingApi, 'getEquity').mockResolvedValue(mockEquities);
    vi.spyOn(waterAccountingApi, 'getConservationOpportunities').mockResolvedValue(mockOpportunities);
    vi.spyOn(waterAccountingApi, 'getReconciliation').mockResolvedValue(mockReconciliations);

    renderWithProviders(<RegionalWaterAccountingPage />);

    await waitFor(() => {
      expect(screen.getByText(/Mintaqaviy Suv Hisobi va Tejamkorlik Markazi/i)).toBeInTheDocument();
    });

    // Check tabs exist
    expect(screen.getByText(/Suv Balansi \(Water Balance\)/i)).toBeInTheDocument();
    expect(screen.getByText(/Harakatlar Daftari \(Ledger\)/i)).toBeInTheDocument();
    expect(screen.getByText(/Kanal Isroflari \(Conveyance Losses\)/i)).toBeInTheDocument();
    expect(screen.getByText(/Kvota Muvofiqligi \(Compliance\)/i)).toBeInTheDocument();
    expect(screen.getByText(/Adolatli Taqsimot \(Distribution Equity\)/i)).toBeInTheDocument();
    expect(screen.getByText(/Tejamkorlik va Solishtiruv \(Conservation & Reconciliation\)/i)).toBeInTheDocument();

    // Click on Canal Losses tab
    fireEvent.click(screen.getByText(/Kanal Isroflari \(Conveyance Losses\)/i));
    await waitFor(() => {
      expect(screen.getByText('Polvon-Yopiq Magistral Kanali')).toBeInTheDocument();
    });
  });
});
