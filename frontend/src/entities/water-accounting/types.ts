export type EvidenceClassification = 'OBSERVED' | 'CALCULATED' | 'ESTIMATED' | 'SIMULATED' | 'MISSING';

export type AccountingDimension =
  | 'DISTRICT'
  | 'WUA'
  | 'PRIMARY_CANAL'
  | 'SECONDARY_CANAL'
  | 'DISTRIBUTION_POINT'
  | 'WATER_SOURCE'
  | 'FARMER'
  | 'FIELD';

export type AccountingPeriodType = 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'SEASONAL' | 'CUSTOM';

export type LedgerEntryType =
  | 'SOURCE_INFLOW'
  | 'CANAL_TRANSFER'
  | 'TURNOUT_DELIVERY'
  | 'FIELD_CONSUMPTION'
  | 'RETURN_FLOW'
  | 'VERIFIED_LOSS'
  | 'ACCOUNTING_CORRECTION';

export type AllocationComplianceStatus =
  | 'ALLOCATION_RESPECTED'
  | 'APPROACHING_LIMIT'
  | 'ALLOCATION_EXCEEDED'
  | 'UNDER_DELIVERED'
  | 'UNVERIFIED_DELIVERY'
  | 'INSUFFICIENT_DATA';

export type ConveyanceLossClassification =
  | 'NO_SIGNIFICANT_DISCREPANCY'
  | 'VERIFIED_LOSS'
  | 'SUSPECTED_LOSS'
  | 'UNACCOUNTED_WITHDRAWAL'
  | 'MEASUREMENT_DISCREPANCY'
  | 'INSUFFICIENT_DATA';

export type EquityDisparityLevel =
  | 'BALANCED'
  | 'POTENTIAL_DISPARITY'
  | 'SIGNIFICANT_DISPARITY'
  | 'INSUFFICIENT_DATA'
  | 'NOT_COMPARABLE';

export type ReconciliationStatus =
  | 'RECONCILED'
  | 'WITHIN_TOLERANCE'
  | 'DISCREPANCY'
  | 'UNVERIFIED'
  | 'INSUFFICIENT_DATA';

export interface RegionalWaterBalanceResponse {
  boundaryDimension: string;
  boundaryName: string;
  boundaryId?: string | null;
  periodType: string;
  periodStart: string;
  periodEnd: string;
  openingBalanceM3: number;
  inflowReceivedM3: number;
  waterTransferredInM3: number;
  waterTransferredOutM3: number;
  waterDeliveredM3: number;
  waterConsumedM3: number;
  verifiedLossesM3: number;
  recordedReturnFlowM3: number;
  closingBalanceM3: number;
  discrepancyM3: number;
  tolerancePercent: number;
  isReconciled: boolean;
  evidenceClassification: EvidenceClassification;
  dataCompletenessPercent: number;
  notes?: string | null;
}

export interface RegionalWaterOverviewResponse {
  districtBalance: RegionalWaterBalanceResponse;
  totalLedgerEntries: number;
  totalReconciliations: number;
  unverifiedReconciliationsCount: number;
  averageConveyanceLossPercent: number;
  nonCompliantParcelsCount: number;
  activeConservationOpportunitiesCount: number;
  equityStatusSummary: string;
}

export interface WaterLedgerEntryResponse {
  id: string;
  entryType: LedgerEntryType;
  accountingBoundary: AccountingDimension;
  volumeM3: number;
  flowRateM3S: number;
  sourceNodeOrCanal?: string | null;
  destinationNodeOrField?: string | null;
  fieldId?: string | null;
  wuaId?: string | null;
  edgeId?: string | null;
  dispatchScheduleId?: string | null;
  allocationId?: string | null;
  eventTimestamp: string;
  evidenceClassification: EvidenceClassification;
  dataSource: string;
  notes?: string | null;
  recordedBy?: string | null;
  isCorrection: boolean;
  replacesEntryId?: string | null;
  createdAt: string;
}

export interface ConveyanceLossResponse {
  edgeId: string;
  canalName: string;
  edgeType: string;
  startNodeName: string;
  endNodeName: string;
  periodStart: string;
  periodEnd: string;
  upstreamVolumeM3: number;
  downstreamVolumeM3: number;
  lossVolumeM3: number;
  lossPercentage: number;
  classification: ConveyanceLossClassification;
  evidenceClassification: EvidenceClassification;
  confidence: string;
  explanation?: string | null;
}

export interface AllocationComplianceResponse {
  targetId: string;
  targetName: string;
  targetType: string;
  wuaName: string;
  cropType: string;
  areaHectares: number;
  allocatedVolumeM3: number;
  reservedVolumeM3: number;
  approvedDispatchVolumeM3: number;
  actualDeliveredVolumeM3: number;
  actualConsumptionM3: number;
  remainingAllocationM3: number;
  overDeliveryM3: number;
  underDeliveryM3: number;
  deliveryCompletionPercent: number;
  status: AllocationComplianceStatus;
  evidenceClassification: EvidenceClassification;
  notes?: string | null;
}

export interface DistributionEquityResponse {
  clusterId: string;
  clusterName: string;
  clusterType: string;
  sampleCount: number;
  meanDeliveryPerHectareM3: number;
  varianceM3: number;
  coefficientOfVariation: number;
  meanAllocationFulfillmentPercent: number;
  averageDeliveryDelayHours: number;
  disparityLevel: EquityDisparityLevel;
  fairnessPolicyApplied: string;
  isConsistentWithPolicy: boolean;
  evidenceClassification: EvidenceClassification;
  supportingEvidence: string[];
  notes?: string | null;
}

export interface ConservationOpportunityResponse {
  id: string;
  title: string;
  problemDescription: string;
  category: string;
  affectedComponentType: string;
  affectedComponentId?: string | null;
  affectedComponentName: string;
  estimatedPotentialSavingsM3?: number | null;
  estimationMethodology: string;
  confidence: string;
  operationalImpact: string;
  suggestedAction: string;
  targetWorkflowUri: string;
  evidenceClassification: EvidenceClassification;
  supportingEvidence: string[];
}

export interface WaterReconciliationResponse {
  id: string;
  boundaryType: string;
  boundaryName: string;
  targetId?: string | null;
  periodStart: string;
  periodEnd: string;
  expectedVolumeM3: number;
  recordedVolumeM3: number;
  discrepancyVolumeM3: number;
  discrepancyPercent: number;
  tolerancePercent: number;
  status: ReconciliationStatus;
  evidenceClassification: EvidenceClassification;
  explanation?: string | null;
  reviewedBy?: string | null;
  reviewedAt?: string | null;
  reviewNotes?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ReconciliationReviewRequest {
  notes: string;
}

export interface FieldWaterAccountingResponse {
  fieldId: string;
  fieldName: string;
  ownerId: string;
  wuaName: string;
  areaHectares: number;
  cropType: string;
  compliance?: AllocationComplianceResponse | null;
  recentMovements: WaterLedgerEntryResponse[];
  recommendations: ConservationOpportunityResponse[];
}

export interface WuaWaterAccountingResponse {
  wuaId: string;
  wuaName: string;
  wuaCode: string;
  district: string;
  waterBalance: RegionalWaterBalanceResponse;
  equityAssessment?: DistributionEquityResponse | null;
  fieldCompliances: AllocationComplianceResponse[];
  canalLosses: ConveyanceLossResponse[];
  conservationOpportunities: ConservationOpportunityResponse[];
}
