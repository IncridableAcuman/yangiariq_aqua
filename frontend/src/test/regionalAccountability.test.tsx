import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import React from 'react';
import { ToastProvider } from '@/shared/ui/toast';
import {
  IncidentSeverityBadge,
  IncidentStatusBadge,
  AccountabilityEvidenceBadge,
  AccountabilityMetricsCards,
  IncidentListTable,
  IncidentDetailModal,
  IncidentReviewDialog,
  AssignInvestigatorDialog,
  ProposeActionDialog,
  VerifyResolutionDialog,
  ReopenIncidentDialog,
} from '@/features/accountability/components';
import { RegionalAccountabilityPage } from '@/pages/accountability/RegionalAccountabilityPage';
import {
  WaterIncident,
  IncidentDetails,
  AccountabilityDashboardSummary,
} from '@/entities/accountability';
import { accountabilityApi } from '@/entities/accountability/api';

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

// Mock API
vi.mock('@/entities/accountability/api', () => ({
  accountabilityApi: {
    getDashboard: vi.fn(),
    listIncidents: vi.fn(),
    getIncident: vi.fn(),
    createIncident: vi.fn(),
    reviewIncident: vi.fn(),
    assignInvestigator: vi.fn(),
    recordInvestigation: vi.fn(),
    attachEvidence: vi.fn(),
    proposeAction: vi.fn(),
    approveAction: vi.fn(),
    completeAction: vi.fn(),
    verifyResolution: vi.fn(),
    reopenIncident: vi.fn(),
    getOverdue: vi.fn(),
    ingestFindings: vi.fn(),
  },
}));

describe('Milestone 08.16: Regional Water Accountability & Incident Investigation Suite', () => {
  let queryClient: QueryClient;

  const mockIncident1: WaterIncident = {
    id: 'inc-001',
    incidentNumber: 'INC-2026-0001',
    title: 'Yuqori magistral kanalda meʼyordan ortiq suv yoʻqotilishi',
    description: 'Suv oqimi kirish va chiqish oʻlchovlarida 28% farq aniqlandi.',
    category: 'CONVEYANCE_LOSS',
    severity: 'HIGH',
    confidence: 'PARTIALLY_VERIFIED',
    status: 'INVESTIGATING',
    sourceModule: 'CANAL_NETWORK',
    sourceEntityType: 'CANAL_SEGMENT',
    sourceEntityId: 'seg-101',
    fieldId: 'field-01',
    canalId: 'seg-101',
    detectionTimestamp: '2026-06-01T10:00:00Z',
    slaDeadline: '2026-06-02T10:00:00Z',
    isOverdue: false,
    responsibleOrganization: 'Bogʻot Suv Yetkazib Beruvchi',
    assignedInvestigatorId: 'inv-01',
    assignedInvestigatorName: 'Rustam Karimov',
    recurrenceCount: 0,
    createdAt: '2026-06-01T10:00:00Z',
    updatedAt: '2026-06-01T12:00:00Z',
  };

  const mockIncident2: WaterIncident = {
    id: 'inc-002',
    incidentNumber: 'INC-2026-0002',
    title: 'Gidrotexnik inshootda oqim xavfsizligi chegarasi buzilishi',
    description: 'Darvoza 4 da suv sathi ruxsat etilgan meʼyordan 15 sm yuqori.',
    category: 'HYDRAULIC_SAFETY',
    severity: 'CRITICAL',
    confidence: 'VERIFIED',
    status: 'DETECTED',
    sourceModule: 'CANAL_DISPATCH',
    sourceEntityType: 'CANAL_GATE',
    sourceEntityId: 'gate-04',
    detectionTimestamp: '2026-06-01T11:00:00Z',
    slaDeadline: '2026-06-01T15:00:00Z',
    isOverdue: true,
    recurrenceCount: 0,
    createdAt: '2026-06-01T11:00:00Z',
    updatedAt: '2026-06-01T11:00:00Z',
  };

  const mockDetails: IncidentDetails = {
    incident: {
      ...mockIncident1,
      status: 'PENDING_VERIFICATION',
    },
    evidence: [
      {
        id: 'ev-01',
        incidentId: 'inc-001',
        title: 'Akustik oqim oʻlchagich telemetriya qaydlari',
        evidenceType: 'SENSOR_TELEMETRY',
        confidence: 'VERIFIED',
        isSimulated: false,
        recordedAt: '2026-06-01T09:30:00Z',
        sourceReference: 'sensor-flow-99',
        description: 'Kirish nuqtasida oʻlchangan oqim hajmi',
      },
      {
        id: 'ev-02',
        incidentId: 'inc-001',
        title: 'Gidravlik model simulyatsiyasi',
        evidenceType: 'SIMULATION_RESULT',
        confidence: 'SIMULATED',
        isSimulated: true,
        recordedAt: '2026-06-01T09:45:00Z',
        sourceReference: 'Saint-Venant 1D Simulation Engine',
        description: 'Prognoz qilingan oqim qiymati (hisobiy)',
      },
    ],
    actions: [
      {
        id: 'act-01',
        incidentId: 'inc-001',
        actionNumber: 'ACT-001',
        title: 'Kanalning 101-segmentini jismoniy koʻzdan kechirish',
        description: 'Damba va qirgʻoq yoriqlarini qidirish',
        objective: 'Suv sizib chiqish nuqtasini topish va bartaraf etish',
        status: 'COMPLETED',
        priority: 'HIGH',
        actionOwnerId: 'worker-ali',
        actionOwnerName: 'Ali Vohidov',
        dueDate: '2026-06-03T18:00:00Z',
        expectedOutcome: 'Yoriqlar aniqlanadi va berkitiladi',
        completionNotes: 'Dambaning 40-metrida sizish aniqlandi va qum qoplari bilan berkitildi.',
        completedAt: '2026-06-02T16:00:00Z',
        verificationRequirement: 'Joyida mustaqil tekshiruv',
        createdAt: '2026-06-01T13:00:00Z',
        updatedAt: '2026-06-02T16:00:00Z',
      },
    ],
    verifications: [
      {
        id: 'ver-01',
        incidentId: 'inc-001',
        actionId: 'act-01',
        verifierId: 'inspector-valiyev',
        verifierName: 'Komil Valiyev',
        result: 'VERIFIED_RESOLVED',
        verifiedAt: '2026-06-02T17:30:00Z',
        verificationMethod: 'ONSITE_PHYSICAL_INSPECTION',
        explanatoryNotes: 'Joyida nazorat oʻlchovi oʻtkazildi, oqim yoʻqotilishi 4.2% gacha tushdi.',
        supportingEvidenceUri: 'ev-01',
      },
    ],
    timeline: [
      {
        id: 'tl-01',
        incidentId: 'inc-001',
        eventType: 'STATUS_CHANGE',
        previousState: 'DETECTED',
        newState: 'UNDER_REVIEW',
        actorId: 'system',
        actorName: 'Avtomatlashtirilgan Nazorat',
        reason: 'Telemetriya meʼyordan ogʻish aniqladi',
        timestamp: '2026-06-01T10:00:00Z',
      },
      {
        id: 'tl-02',
        incidentId: 'inc-001',
        eventType: 'STATUS_CHANGE',
        previousState: 'UNDER_REVIEW',
        newState: 'OPEN',
        actorId: 'operator-bahodir',
        actorName: 'Bahodir Olimov',
        reason: 'Hodisa koʻrib chiqildi va tasdiqlandi',
        timestamp: '2026-06-01T10:30:00Z',
      },
    ],
    isOverdue: false,
  };

  const mockDashboard: AccountabilityDashboardSummary = {
    totalIncidents: 24,
    openIncidents: 5,
    criticalIncidents: 2,
    underInvestigation: 4,
    overdueIncidents: 3,
    pendingVerification: 3,
    resolvedIncidents: 18,
    reopenedIncidents: 1,
  };

  beforeEach(() => {
    queryClient = new QueryClient({
      defaultOptions: {
        queries: { retry: false },
      },
    });
    vi.clearAllMocks();
  });

  const renderWithClient = (ui: React.ReactElement) => {
    return render(
      <QueryClientProvider client={queryClient}>
        <ToastProvider>{ui}</ToastProvider>
      </QueryClientProvider>
    );
  };

  describe('Badges Suite', () => {
    it('renders all IncidentSeverityBadge variants with accessible labels', () => {
      const { rerender } = render(<IncidentSeverityBadge severity="CRITICAL" />);
      expect(screen.getByText(/CRITICAL/i)).toBeInTheDocument();

      rerender(<IncidentSeverityBadge severity="HIGH" />);
      expect(screen.getByText(/HIGH/i)).toBeInTheDocument();

      rerender(<IncidentSeverityBadge severity="MEDIUM" />);
      expect(screen.getByText(/MEDIUM/i)).toBeInTheDocument();

      rerender(<IncidentSeverityBadge severity="LOW" />);
      expect(screen.getByText(/LOW/i)).toBeInTheDocument();

      rerender(<IncidentSeverityBadge severity="INFORMATIONAL" />);
      expect(screen.getByText(/INFO/i)).toBeInTheDocument();
    });

    it('renders all IncidentStatusBadge variants', () => {
      const { rerender } = render(<IncidentStatusBadge status="DETECTED" />);
      expect(screen.getByText(/Detected/i)).toBeInTheDocument();

      rerender(<IncidentStatusBadge status="UNDER_REVIEW" />);
      expect(screen.getByText(/Koʻrib chiqilmoqda/i)).toBeInTheDocument();

      rerender(<IncidentStatusBadge status="PENDING_VERIFICATION" />);
      expect(screen.getByText(/Tekshiruv kutilmoqda/i)).toBeInTheDocument();

      rerender(<IncidentStatusBadge status="RESOLVED" />);
      expect(screen.getByText(/Resolved/i)).toBeInTheDocument();

      rerender(<IncidentStatusBadge status="DISMISSED" />);
      expect(screen.getByText(/Dismissed/i)).toBeInTheDocument();
    });

    it('renders AccountabilityEvidenceBadge with correct confidence colors and warnings', () => {
      const { rerender } = render(
        <AccountabilityEvidenceBadge confidence="VERIFIED" />
      );
      expect(screen.getByText(/TASDIQLANGAN/i)).toBeInTheDocument();

      rerender(<AccountabilityEvidenceBadge confidence="SIMULATED" />);
      expect(screen.getByText(/SIMULYATSIYA/i)).toBeInTheDocument();

      rerender(<AccountabilityEvidenceBadge confidence="INSUFFICIENT_DATA" />);
      expect(screen.getByText(/YETARLI EMAS/i)).toBeInTheDocument();
    });
  });

  describe('AccountabilityMetricsCards Component', () => {
    it('renders aggregate metrics accurately and triggers filter clicks', () => {
      const onFilterClick = vi.fn();

      render(
        <AccountabilityMetricsCards
          summary={mockDashboard}
          onFilterClick={onFilterClick}
        />
      );

      expect(screen.getByText('5')).toBeInTheDocument(); // open
      expect(screen.getByText('2')).toBeInTheDocument(); // critical
      expect(screen.getByText('4')).toBeInTheDocument(); // under investigation
      expect(screen.getByText('18')).toBeInTheDocument(); // resolved

      // Click on overdue card
      const overdueCard = screen.getByText('Muddat buzilgan').closest('div');
      if (overdueCard) fireEvent.click(overdueCard);
      expect(onFilterClick).toHaveBeenCalledWith('OVERDUE');

      // Click on critical card
      const criticalCard = screen.getByText('Kritik xavflar').closest('div');
      if (criticalCard) fireEvent.click(criticalCard);
      expect(onFilterClick).toHaveBeenCalledWith('CRITICAL');
    });
  });

  describe('IncidentListTable Component', () => {
    it('renders incident list with columns and allows row click', () => {
      const onSelectIncident = vi.fn();

      render(
        <IncidentListTable
          incidents={[mockIncident1, mockIncident2]}
          onSelectIncident={onSelectIncident}
        />
      );

      expect(screen.getByText('INC-2026-0001')).toBeInTheDocument();
      expect(screen.getByText('INC-2026-0002')).toBeInTheDocument();
      expect(screen.getByText('Yuqori magistral kanalda meʼyordan ortiq suv yoʻqotilishi')).toBeInTheDocument();

      // Click incident row
      fireEvent.click(screen.getByText('INC-2026-0001'));
      expect(onSelectIncident).toHaveBeenCalledWith(mockIncident1);
    });

    it('filters incidents by search text and status filter', () => {
      render(
        <IncidentListTable
          incidents={[mockIncident1, mockIncident2]}
          onSelectIncident={vi.fn()}
        />
      );

      const searchInput = screen.getByPlaceholderText(/qidirish/i);
      fireEvent.change(searchInput, { target: { value: 'Gidrotexnik' } });

      expect(screen.queryByText('INC-2026-0001')).not.toBeInTheDocument();
      expect(screen.getByText('INC-2026-0002')).toBeInTheDocument();
    });

    it('shows empty state when no incidents match', () => {
      render(
        <IncidentListTable
          incidents={[]}
          onSelectIncident={vi.fn()}
        />
      );

      expect(screen.getByText(/Hech qanday operatsion hodisa topilmadi/i)).toBeInTheDocument();
    });
  });

  describe('IncidentDetailModal Component', () => {
    it('renders tabs and non-accusatory governance banner', () => {
      render(
        <IncidentDetailModal
          isOpen={true}
          onClose={vi.fn()}
          details={mockDetails}
          isOperatorOrAdmin={true}
          onReview={vi.fn()}
          onAssign={vi.fn()}
          onProposeAction={vi.fn()}
          onVerify={vi.fn()}
          onReopen={vi.fn()}
        />
      );

      expect(screen.getByText('INC-2026-0001')).toBeInTheDocument();
      // Non-accusatory banner notice
      expect(screen.getByText(/Aybsizlik prezumpsiyasi va xolis operatsion boshqaruv/i)).toBeInTheDocument();

      // Switch to evidence tab
      fireEvent.click(screen.getByText(/Dalillar toʻplami/i));
      expect(screen.getByText('Akustik oqim oʻlchagich telemetriya qaydlari')).toBeInTheDocument();
      expect(screen.getByText('Gidravlik model simulyatsiyasi')).toBeInTheDocument();

      // Switch to actions tab
      fireEvent.click(screen.getByText(/Tuzatish choralari/i));
      expect(screen.getByText('Kanalning 101-segmentini jismoniy koʻzdan kechirish')).toBeInTheDocument();
      expect(screen.getByText(/Ali Vohidov/i)).toBeInTheDocument();

      // Switch to verification tab
      fireEvent.click(screen.getByText(/Mustaqil Tekshiruv/i));
      expect(screen.getByText(/Komil Valiyev/i)).toBeInTheDocument();

      // Switch to timeline tab
      fireEvent.click(screen.getByText(/Audit Tarixi/i));
      expect(screen.getByText('Avtomatlashtirilgan Nazorat')).toBeInTheDocument();
      expect(screen.getByText('Bahodir Olimov')).toBeInTheDocument();
    });
  });

  describe('Dialogs Suite', () => {
    it('handles IncidentReviewDialog approve and dismiss actions', () => {
      const onSubmit = vi.fn().mockResolvedValue(undefined);
      const onClose = vi.fn();

      render(
        <IncidentReviewDialog
          isOpen={true}
          onClose={onClose}
          incident={mockIncident1}
          onSubmit={onSubmit}
        />
      );

      expect(screen.getByText(/Operatsion hodisani koʻrib chiqish/i)).toBeInTheDocument();

      const notesInput = screen.getByPlaceholderText(/Faktlar tasdiqlandi/i);
      fireEvent.change(notesInput, { target: { value: 'Tekshirishga ruxsat berildi' } });

      // Click approve
      const approveBtn = screen.getByRole('button', { name: /Ishni ochish \(OPEN\)/i });
      fireEvent.click(approveBtn);

      expect(onSubmit).toHaveBeenCalledWith(true, 'Tekshirishga ruxsat berildi');
    });

    it('handles AssignInvestigatorDialog submission', () => {
      const onSubmit = vi.fn().mockResolvedValue(undefined);
      const onClose = vi.fn();

      render(
        <AssignInvestigatorDialog
          isOpen={true}
          onClose={onClose}
          incident={mockIncident1}
          onSubmit={onSubmit}
        />
      );

      fireEvent.change(screen.getByPlaceholderText(/Muxandis Shokirov Alisher/i), {
        target: { value: 'Jamshid Ismoilov' },
      });
      fireEvent.change(screen.getByPlaceholderText(/Gidropost tekshiruvi/i), {
        target: { value: 'Maxsus tekshiruv uchun masʼul etib tayinlandi' },
      });

      fireEvent.click(screen.getByRole('button', { name: /Tergovchini tayinlash/i }));

      expect(onSubmit).toHaveBeenCalledWith(
        expect.any(String),
        'Jamshid Ismoilov',
        expect.any(String),
        'Maxsus tekshiruv uchun masʼul etib tayinlandi'
      );
    });

    it('handles ProposeActionDialog submission', () => {
      const onSubmit = vi.fn().mockResolvedValue(undefined);
      const onClose = vi.fn();

      render(
        <ProposeActionDialog
          isOpen={true}
          onClose={onClose}
          incident={mockIncident1}
          onSubmit={onSubmit}
        />
      );

      fireEvent.change(screen.getByPlaceholderText(/Gidropost datchiklarini kalibrlash/i), {
        target: { value: 'Darvozani tozalash' },
      });
      fireEvent.change(screen.getByPlaceholderText(/Amalga oshirilishi kerak boʻlgan/i), {
        target: { value: 'Darvozadagi loy va shox-shabbalarni tozalash' },
      });
      fireEvent.change(screen.getByPlaceholderText(/Oqishni 0% ga tushirish/i), {
        target: { value: 'Suv oqimini normallashtirish' },
      });
      fireEvent.change(screen.getByPlaceholderText(/Shovot kanali taʼmirlash brigadasi/i), {
        target: { value: 'Olim Shokirov' },
      });
      fireEvent.change(screen.getByPlaceholderText(/Suv oqimi yoʻqotishining/i), {
        target: { value: 'Oqim sigʻimi tiklanadi' },
      });

      fireEvent.click(screen.getByRole('button', { name: /Chorani taklif etish/i }));

      expect(onSubmit).toHaveBeenCalledWith(
        expect.objectContaining({
          title: 'Darvozani tozalash',
          objective: 'Suv oqimini normallashtirish',
          actionOwnerName: 'Olim Shokirov',
        })
      );
    });

    it('enforces separation of duties in VerifyResolutionDialog', () => {
      const onSubmit = vi.fn().mockResolvedValue(undefined);
      const onClose = vi.fn();

      render(
        <VerifyResolutionDialog
          isOpen={true}
          onClose={onClose}
          incident={mockDetails.incident}
          actions={mockDetails.actions}
          onSubmit={onSubmit}
        />
      );

      // Warning banner displayed
      expect(
        screen.getByText(/Vazifalar taqsimoti talabi:/i)
      ).toBeInTheDocument();

      const notesInput = screen.getByPlaceholderText(/Joyida tekshiruv oʻtkazildi/i);
      fireEvent.change(notesInput, { target: { value: 'Joyida mustaqil tekshiruv oʻtkazildi, oqim meʼyorda' } });

      const submitBtn = screen.getByRole('button', { name: /Xulosani rasmiylashtirish/i });
      fireEvent.click(submitBtn);

      expect(onSubmit).toHaveBeenCalledWith(
        expect.objectContaining({
          verificationMethod: 'ONSITE_PHYSICAL_INSPECTION',
          result: 'VERIFIED_RESOLVED',
          explanatoryNotes: 'Joyida mustaqil tekshiruv oʻtkazildi, oqim meʼyorda',
        })
      );
    });

    it('handles ReopenIncidentDialog submission with mandatory justification', () => {
      const onSubmit = vi.fn().mockResolvedValue(undefined);
      const onClose = vi.fn();

      render(
        <ReopenIncidentDialog
          isOpen={true}
          onClose={onClose}
          incident={mockIncident1}
          onSubmit={onSubmit}
        />
      );

      expect(screen.getByText(/Hodisani qayta ochish \(Reopen\)/i)).toBeInTheDocument();

      const reasonInput = screen.getByPlaceholderText(/Masalan: Taʼmirlangan flumeda yana 10% tafovut/i);
      fireEvent.change(reasonInput, {
        target: { value: 'Suv sizish holati yana qaytalangani aniqlandi.' },
      });

      const submitBtn = screen.getByRole('button', { name: /Hodisani qayta ochish/i });
      fireEvent.click(submitBtn);

      expect(onSubmit).toHaveBeenCalledWith(
        'Suv sizish holati yana qaytalangani aniqlandi.'
      );
    });
  });

  describe('RegionalAccountabilityPage Integration', () => {
    it('loads dashboard summary and incident list, and supports finding ingestion', async () => {
      vi.mocked(accountabilityApi.getDashboard).mockResolvedValue(mockDashboard);
      vi.mocked(accountabilityApi.listIncidents).mockResolvedValue([mockIncident1, mockIncident2]);
      vi.mocked(accountabilityApi.ingestFindings).mockResolvedValue([mockIncident1]);

      renderWithClient(<RegionalAccountabilityPage />);

      // Verify page titles and summary cards
      await waitFor(() => {
        expect(screen.getByText(/Mintaqaviy Suv Javobgarligi/i)).toBeInTheDocument();
        expect(screen.getByText('INC-2026-0001')).toBeInTheDocument();
      });

      // Click ingest findings button
      const ingestBtn = screen.getByRole('button', { name: /Topilmalarni import qilish/i });
      fireEvent.click(ingestBtn);

      await waitFor(() => {
        expect(accountabilityApi.ingestFindings).toHaveBeenCalledTimes(1);
      });
    });
  });
});
