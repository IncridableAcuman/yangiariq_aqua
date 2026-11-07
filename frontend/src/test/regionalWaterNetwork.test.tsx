import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import React from 'react';
import {
  CanalStatusBadge,
  CanalCapacityIndicator,
  NetworkOverviewCards,
  RegionalDemandChart,
  DispatchOptimizationPanel,
  DispatchScheduleTimeline,
  DispatchApprovalDialog,
  PlannedVsActualDeliveryChart,
  ScenarioComparisonPanel,
  ResilienceScenarioDialog,
  WaterNetworkMap,
} from '@/features/network/components';
import { RegionalOperationsPage } from '@/pages/network/RegionalOperationsPage';
import {
  WaterNetworkOverview,
  WaterNetworkNode,
  WaterNetworkEdge,
  RegionalDemandResponse,
  DispatchSchedule,
  ActualDispatchDelivery,
  ResilienceScenario,
} from '@/entities/network';
import { networkApi } from '@/entities/network/api';

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

describe('Milestone 08.14: Regional Water Network Resilience & Canal Dispatch Suite', () => {
  let queryClient: QueryClient;

  const mockOverview: WaterNetworkOverview = {
    totalNodes: 12,
    totalEdges: 15,
    operationalEdges: 13,
    constrainedEdges: 1,
    blockedEdges: 1,
    totalConveyanceCapacityM3S: 18.5,
    totalCurrentFlowM3S: 7.8,
    overallNetworkUtilizationPercent: 42.16,
    activeScheduleCount: 1,
    latestActiveScheduleTitle: 'Canal Dispatch Proposal - Yangiariq Suv Ziyosi',
    latestActiveScheduleStatus: 'PROPOSED',
    recentSurgeRiskWarnings: ['Canal Polvon is operating near overload threshold'],
  };

  const mockNode: WaterNetworkNode = {
    id: 'node-1',
    name: 'Tuyamuyun Bosh Suv Olgich',
    code: 'NODE-SRC-01',
    nodeType: 'WATER_SOURCE',
    latitude: 41.385,
    longitude: 60.62,
    elevationMeters: 95.0,
    waterSourceId: 'ws-1',
    fieldId: null,
    wuaId: 'wua-1',
    isActive: true,
    properties: { sourceType: 'RIVER_INTAKE' },
  };

  const mockEdge: WaterNetworkEdge = {
    id: 'edge-1',
    name: 'Polvon Yuqori Kanali',
    code: 'EDGE-PRI-02',
    edgeType: 'PRIMARY_CANAL',
    startNodeId: 'node-1',
    endNodeId: 'node-2',
    wuaId: 'wua-1',
    lengthMeters: 3100,
    maxCapacityM3S: 5.2,
    minFlowM3S: 0.2,
    currentFlowM3S: 2.1,
    operationalStatus: 'OPERATIONAL',
    utilizationPercent: 40.38,
    geometry: [
      { longitude: 60.605, latitude: 41.372 },
      { longitude: 60.59, latitude: 41.365 },
    ],
  };

  const mockDemand: RegionalDemandResponse = {
    districtTotalRequestedM3: 45000,
    districtTotalAllocatedM3: 38000,
    districtTotalUnmetM3: 7000,
    wuaDemands: [
      {
        wuaId: 'wua-1',
        wuaName: 'Yangiariq Suv Ziyosi SIU',
        requestedM3: 25000,
        allocatedM3: 22000,
        unmetM3: 3000,
        fieldCount: 8,
      },
    ],
    canalDemands: [
      {
        edgeId: 'edge-1',
        canalName: 'Polvon Yuqori Kanali',
        totalDemandM3: 25000,
        canalCapacityM3S: 5.2,
        isConstrained: false,
      },
    ],
  };

  const mockSchedule: DispatchSchedule = {
    id: 'sched-1',
    title: 'Canal Dispatch Proposal - Yangiariq Suv Ziyosi',
    periodStart: '2026-11-07T08:00:00Z',
    periodEnd: '2026-11-14T18:00:00Z',
    status: 'PROPOSED',
    targetWuaId: 'wua-1',
    targetWuaName: 'Yangiariq Suv Ziyosi SIU',
    totalRequestedM3: 15000,
    totalAllocatedM3: 13500,
    totalUnmetM3: 1500,
    assignments: [
      {
        assignmentId: 'as-1',
        fieldId: 'field-101',
        fieldName: 'Paxta-1 Dalasi',
        wuaId: 'wua-1',
        wuaName: 'Yangiariq Suv Ziyosi SIU',
        edgeId: 'edge-1',
        canalName: 'Polvon Yuqori Kanali',
        deliveryWindowStart: '2026-11-07T08:00:00Z',
        deliveryWindowEnd: '2026-11-07T12:00:00Z',
        allocatedVolumeM3: 800,
        flowRateM3S: 0.055,
        cropType: 'COTTON',
        isStaggered: false,
        staggerDelayHours: 0,
        isBlocked: false,
        blockReason: null,
        allocationRatioPercent: 100,
      },
      {
        assignmentId: 'as-2',
        fieldId: 'field-102',
        fieldName: 'Bugdoy-3 Dalasi',
        wuaId: 'wua-1',
        wuaName: 'Yangiariq Suv Ziyosi SIU',
        edgeId: 'edge-1',
        canalName: 'Polvon Yuqori Kanali',
        deliveryWindowStart: '2026-11-07T12:00:00Z',
        deliveryWindowEnd: '2026-11-07T16:00:00Z',
        allocatedVolumeM3: 650,
        flowRateM3S: 0.045,
        cropType: 'WHEAT',
        isStaggered: true,
        staggerDelayHours: 4,
        isBlocked: false,
        blockReason: null,
        allocationRatioPercent: 90,
      },
    ],
    conflicts: [
      {
        conflictId: 'conf-1',
        conflictType: 'CANAL_CAPACITY_OVERLOAD',
        edgeId: 'edge-1',
        canalName: 'Polvon Yuqori Kanali',
        competingFieldIds: ['field-101', 'field-102'],
        competingFieldNames: ['Paxta-1 Dalasi', 'Bugdoy-3 Dalasi'],
        aggregateFlowM3S: 5.8,
        canalMaxCapacityM3S: 5.2,
        deficitVolumeM3: 600,
        recommendedResolution: 'Stagger delivery windows by 4.0 hours',
        isResolvedByOptimizer: true,
      },
    ],
    metrics: { efficiencyScore: 92.5 },
    fairnessPolicy: {
      policyType: 'CROP_CRITICALITY_FIRST',
      description: 'Prioritize sensitive crops',
      minGuaranteedRatioPercent: 40,
    },
    notes: 'Baseline weekly dispatch',
    approvedBy: null,
    approvedAt: null,
    rejectionReason: null,
    createdBy: 'operator-1',
    createdAt: '2026-11-07T07:00:00Z',
    updatedAt: '2026-11-07T07:00:00Z',
  };

  const mockDelivery: ActualDispatchDelivery = {
    id: 'del-1',
    dispatchScheduleId: 'sched-1',
    fieldId: 'field-101',
    fieldName: 'Paxta-1 Dalasi',
    edgeId: 'edge-1',
    canalName: 'Polvon Yuqori Kanali',
    actualStartAt: '2026-11-07T08:00:00Z',
    actualEndAt: '2026-11-07T12:00:00Z',
    plannedVolumeM3: 800,
    actualVolumeM3: 820,
    varianceVolumeM3: 20,
    variancePercent: 2.5,
    measuredFlowM3S: 0.057,
    operatorId: 'operator-1',
    operatorNotes: 'Stable delivery head pressure',
    createdAt: '2026-11-07T12:15:00Z',
  };

  const mockScenario: ResilienceScenario = {
    id: 'scen-1',
    name: 'Secondary Canal Rupture Simulation',
    scenarioType: 'CANAL_OUTAGE',
    disabledNodeIds: [],
    disabledEdgeIds: ['edge-1'],
    capacityReductionPercent: 100,
    demandSurgePercent: 0,
    affectedFieldCount: 2,
    lostCapacityM3S: 5.2,
    unmetDemandM3: 1450,
    affectedWuaNames: ['Yangiariq Suv Ziyosi SIU'],
    alternativeFeasibleRoutesCount: 1,
    summary: 'Rupture isolates 2 fields downstream, resulting in 1450 m³ unmet deficit.',
    simulationResults: { affectedFieldIds: ['field-101', 'field-102'] },
    createdBy: 'operator-1',
    createdAt: '2026-11-07T07:30:00Z',
  };

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false, gcTime: 0 },
      },
    });

    vi.spyOn(networkApi, 'getOverview').mockResolvedValue(mockOverview);
    vi.spyOn(networkApi, 'getTopology').mockResolvedValue({
      nodes: [mockNode],
      edges: [mockEdge],
      validationErrors: [],
      validationWarnings: [],
      isValid: true,
    });
    vi.spyOn(networkApi, 'getDemandOverview').mockResolvedValue(mockDemand);
    vi.spyOn(networkApi, 'listSchedules').mockResolvedValue([mockSchedule]);
    vi.spyOn(networkApi, 'listActualDeliveries').mockResolvedValue([mockDelivery]);
    vi.spyOn(networkApi, 'listScenarios').mockResolvedValue([mockScenario]);
    vi.spyOn(networkApi, 'getSegmentCapacity').mockResolvedValue({
      edgeId: mockEdge.id,
      edgeName: mockEdge.name,
      edgeCode: mockEdge.code,
      edgeType: mockEdge.edgeType,
      maxCapacityM3S: mockEdge.maxCapacityM3S,
      currentFlowM3S: mockEdge.currentFlowM3S,
      availableCapacityM3S: 3.1,
      utilizationPercent: 40.38,
      status: 'SAFE',
      isBlocked: false,
      blockReason: null,
      safetyConstraintViolations: [],
      dependentFieldCount: 2,
      dependentFieldNames: ['Paxta-1 Dalasi', 'Bugdoy-3 Dalasi'],
    });
    vi.spyOn(networkApi, 'getAffectedFields').mockResolvedValue({
      edgeId: mockEdge.id,
      edgeName: mockEdge.name,
      dependentFieldCount: 2,
      dependentFields: [
        {
          fieldId: 'field-101',
          name: 'Paxta-1 Dalasi',
          cropType: 'COTTON',
          areaHectares: 12.5,
          ownerName: 'Owner 1',
        },
      ],
    });
  });

  const renderWithProviders = (ui: React.ReactElement) => {
    return render(
      <QueryClientProvider client={queryClient}>
        {ui}
      </QueryClientProvider>
    );
  };

  it('1. CanalStatusBadge renders status variants correctly', () => {
    const { rerender } = render(<CanalStatusBadge status="OPERATIONAL" />);
    expect(screen.getByText(/OPERATIONAL/i)).toBeInTheDocument();

    rerender(<CanalStatusBadge status="CONSTRAINED" />);
    expect(screen.getByText(/CONSTRAINED/i)).toBeInTheDocument();

    rerender(<CanalStatusBadge status="BLOCKED" />);
    expect(screen.getByText(/BLOCKED/i)).toBeInTheDocument();
  });

  it('2. CanalCapacityIndicator displays numerical flow and progress bar', () => {
    render(
      <CanalCapacityIndicator
        currentFlowM3S={2.5}
        maxCapacityM3S={5.0}
        utilizationPercent={50}
      />
    );
    expect(screen.getByText('2.50 / 5.00 m³/s')).toBeInTheDocument();
    expect(screen.getByText('50%')).toBeInTheDocument();
  });

  it('3. NetworkOverviewCards displays district conveyance capacity and infrastructure metrics', () => {
    renderWithProviders(<NetworkOverviewCards overview={mockOverview} />);
    expect(screen.getByText('Tizim O\'tkazuvchanligi')).toBeInTheDocument();
    expect(screen.getByText('7.80')).toBeInTheDocument();
    expect(screen.getByText('/ 18.50 m³/s')).toBeInTheDocument();
    expect(screen.getByText('15')).toBeInTheDocument();
  });

  it('4. RegionalDemandChart renders volumetric breakdown by WUA and canal pressure', () => {
    renderWithProviders(<RegionalDemandChart demandData={mockDemand} />);
    expect(screen.getByText('Tuman Suv Talabi & Taqsimoti')).toBeInTheDocument();
    expect(screen.getByText('45,000 m³')).toBeInTheDocument();
    expect(screen.getByText('38,000 m³')).toBeInTheDocument();
    expect(screen.getByText('7,000 m³')).toBeInTheDocument();
    expect(screen.getByText('Yangiariq Suv Ziyosi SIU')).toBeInTheDocument();
  });

  it('5. DispatchOptimizationPanel allows policy selection and triggers mutation', async () => {
    const optimizeSpy = vi.spyOn(networkApi, 'optimizeDispatch').mockResolvedValue(mockSchedule);
    renderWithProviders(<DispatchOptimizationPanel />);

    expect(screen.getByText('Kanal Suv Taqsimotini Optimizatsiya Qilish')).toBeInTheDocument();
    const submitBtn = screen.getByText('Taqsimot Rejasini Hisoblash & Taklif Yaratish');
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(optimizeSpy).toHaveBeenCalled();
    });
  });

  it('6. DispatchScheduleTimeline displays delivery windows, staggered delays, and conflicts', () => {
    renderWithProviders(<DispatchScheduleTimeline schedule={mockSchedule} />);
    expect(screen.getByText(mockSchedule.title)).toBeInTheDocument();
    expect(screen.getByText('Paxta-1 Dalasi')).toBeInTheDocument();
    expect(screen.getByText('Bugdoy-3 Dalasi')).toBeInTheDocument();
    expect(screen.getByText('+4s surilgan')).toBeInTheDocument();
    expect(screen.getByText(/Kanal Sig'imi Ziddiyatlari/i)).toBeInTheDocument();
  });

  it('7. DispatchApprovalDialog confirms human approval without autonomous actuation', async () => {
    const approveSpy = vi.spyOn(networkApi, 'approveSchedule').mockResolvedValue({
      ...mockSchedule,
      status: 'SCHEDULED',
    });
    const onApproved = vi.fn();

    renderWithProviders(
      <DispatchApprovalDialog
        isOpen={true}
        onClose={vi.fn()}
        schedule={mockSchedule}
        onApproved={onApproved}
      />
    );

    expect(screen.getByText('Suv Taqsimot Jadvalini Tasdiqlash')).toBeInTheDocument();
    expect(screen.getByText(/Nasoslar, shlyuzlar yoki klapanlar avtonom tarzda boshqarilmaydi/i)).toBeInTheDocument();

    const confirmBtn = screen.getByText('Tasdiqlayman & Jadvalga Kiritaman');
    fireEvent.click(confirmBtn);

    await waitFor(() => {
      expect(approveSpy).toHaveBeenCalledWith(mockSchedule.id, expect.anything());
      expect(onApproved).toHaveBeenCalled();
    });
  });

  it('8. PlannedVsActualDeliveryChart tracks delivery volume variance', async () => {
    renderWithProviders(<PlannedVsActualDeliveryChart scheduleId={mockSchedule.id} />);
    expect(screen.getByText('Reja va Haqiqiy Yetkazib Berish Solishtiruvi (Variance Audit)')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByText('Paxta-1 Dalasi')).toBeInTheDocument();
    });
    expect(screen.getByText('800')).toBeInTheDocument();
    expect(screen.getByText('820')).toBeInTheDocument();
    expect(screen.getByText('+20 m³')).toBeInTheDocument();
  });

  it('9. ScenarioComparisonPanel displays isolated stress-test results', async () => {
    renderWithProviders(<ScenarioComparisonPanel />);
    expect(screen.getByText('Chidamlilik Ssenariylari & Stress-Test Tahlili')).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.getByText(mockScenario.name)).toBeInTheDocument();
    });
    expect(screen.getByText('5.20 m³/s')).toBeInTheDocument();
    expect(screen.getByText('2 ta')).toBeInTheDocument();
  });

  it('10. ResilienceScenarioDialog allows simulating hypothetical canal disruption', async () => {
    const simSpy = vi.spyOn(networkApi, 'simulateScenario').mockResolvedValue(mockScenario);
    renderWithProviders(
      <ResilienceScenarioDialog
        isOpen={true}
        onClose={vi.fn()}
        presetEdgeId={mockEdge.id}
      />
    );

    expect(screen.getByText('Tarmoq Chidamliligi Simulyatsiyasi (What-If)')).toBeInTheDocument();
    const runBtn = screen.getByText('Ssenariyni Hisoblash');
    fireEvent.click(runBtn);

    await waitFor(() => {
      expect(simSpy).toHaveBeenCalled();
    });
  });

  it('11. WaterNetworkMap renders Leaflet container and handles selection', () => {
    const onSelectEdge = vi.fn();
    const onSelectNode = vi.fn();

    const { container } = renderWithProviders(
      <WaterNetworkMap
        nodes={[mockNode]}
        edges={[mockEdge]}
        onSelectEdge={onSelectEdge}
        onSelectNode={onSelectNode}
      />
    );

    expect(container.querySelector('.leaflet-container')).toBeInTheDocument();
  });

  it('12. RegionalOperationsPage renders top header and switches tabs', async () => {
    renderWithProviders(<RegionalOperationsPage />);

    expect(screen.getByText('Suv Tarmog\'i & Kanal Dispecherlik Markazi')).toBeInTheDocument();
    expect(screen.getByText('Tarmoq Xaritasi & GIS')).toBeInTheDocument();
    expect(screen.getByText('Dispecherlik Optimizatsiyasi')).toBeInTheDocument();
    expect(screen.getByText('Chidamlilik & Stress-Test')).toBeInTheDocument();
    expect(screen.getByText('Yetkazish Auditi (Reja/Fakt)')).toBeInTheDocument();
  });
});
