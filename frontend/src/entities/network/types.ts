export type WaterNetworkNodeType =
  | 'WATER_SOURCE'
  | 'RESERVOIR'
  | 'PUMP_STATION'
  | 'CANAL_JUNCTION'
  | 'CONTROL_GATE'
  | 'DISTRIBUTION_POINT'
  | 'FIELD_INLET'
  | 'TERMINAL_NODE';

export type WaterNetworkEdgeType =
  | 'PRIMARY_CANAL'
  | 'SECONDARY_CANAL'
  | 'DISTRIBUTION_CHANNEL'
  | 'PIPELINE';

export type OperationalStatus =
  | 'OPERATIONAL'
  | 'CONSTRAINED'
  | 'MAINTENANCE'
  | 'BLOCKED'
  | 'OFFLINE';

export type DispatchStatus =
  | 'DRAFT'
  | 'OPTIMIZING'
  | 'PROPOSED'
  | 'REVIEW_REQUIRED'
  | 'APPROVED'
  | 'SCHEDULED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'REJECTED'
  | 'CANCELLED'
  | 'EXPIRED'
  | 'BLOCKED';

export interface CoordinatePoint {
  longitude: number;
  latitude: number;
}

export interface WaterNetworkNode {
  id: string;
  name: string;
  code: string;
  nodeType: WaterNetworkNodeType;
  latitude: number | null;
  longitude: number | null;
  elevationMeters: number | null;
  waterSourceId: string | null;
  fieldId: string | null;
  wuaId: string | null;
  isActive: boolean;
  properties: Record<string, any>;
}

export interface WaterNetworkEdge {
  id: string;
  name: string;
  code: string;
  edgeType: WaterNetworkEdgeType;
  startNodeId: string;
  endNodeId: string;
  wuaId: string | null;
  lengthMeters: number;
  maxCapacityM3S: number;
  minFlowM3S: number;
  currentFlowM3S: number;
  operationalStatus: OperationalStatus;
  utilizationPercent: number;
  geometry: CoordinatePoint[];
}

export interface WaterNetworkOverview {
  totalNodes: number;
  totalEdges: number;
  operationalEdges: number;
  constrainedEdges: number;
  blockedEdges: number;
  totalConveyanceCapacityM3S: number;
  totalCurrentFlowM3S: number;
  overallNetworkUtilizationPercent: number;
  activeScheduleCount: number;
  latestActiveScheduleTitle: string;
  latestActiveScheduleStatus: string;
  recentSurgeRiskWarnings: string[];
}

export interface NetworkTopology {
  nodes: WaterNetworkNode[];
  edges: WaterNetworkEdge[];
  validationErrors: string[];
  validationWarnings: string[];
  isValid: boolean;
}

export interface SegmentCapacity {
  edgeId: string;
  edgeName: string;
  edgeCode: string;
  edgeType: string;
  maxCapacityM3S: number;
  currentFlowM3S: number;
  availableCapacityM3S: number;
  utilizationPercent: number;
  status: string;
  isBlocked: boolean;
  blockReason: string | null;
  safetyConstraintViolations: string[];
  dependentFieldCount: number;
  dependentFieldNames: string[];
}

export interface AffectedFieldItem {
  fieldId: string;
  name: string;
  cropType: string;
  areaHectares: number;
  ownerName: string;
}

export interface AffectedFieldsResponse {
  edgeId: string;
  edgeName: string;
  dependentFieldCount: number;
  dependentFields: AffectedFieldItem[];
}

export interface WuaDemandItem {
  wuaId: string;
  wuaName: string;
  requestedM3: number;
  allocatedM3: number;
  unmetM3: number;
  fieldCount: number;
}

export interface CanalDemandItem {
  edgeId: string;
  canalName: string;
  totalDemandM3: number;
  canalCapacityM3S: number;
  isConstrained: boolean;
}

export interface RegionalDemandResponse {
  districtTotalRequestedM3: number;
  districtTotalAllocatedM3: number;
  districtTotalUnmetM3: number;
  wuaDemands: WuaDemandItem[];
  canalDemands: CanalDemandItem[];
}

export interface DispatchAssignment {
  assignmentId: string;
  fieldId: string;
  fieldName: string;
  wuaId: string | null;
  wuaName: string;
  edgeId: string | null;
  canalName: string;
  deliveryWindowStart: string;
  deliveryWindowEnd: string;
  allocatedVolumeM3: number;
  flowRateM3S: number;
  cropType: string;
  isStaggered: boolean;
  staggerDelayHours: number;
  isBlocked: boolean;
  blockReason: string | null;
  allocationRatioPercent: number;
}

export interface DispatchConflict {
  conflictId: string;
  conflictType: string;
  edgeId: string | null;
  canalName: string;
  competingFieldIds: string[];
  competingFieldNames: string[];
  aggregateFlowM3S: number;
  canalMaxCapacityM3S: number;
  deficitVolumeM3: number;
  recommendedResolution: string;
  isResolvedByOptimizer: boolean;
}

export interface FairnessPolicy {
  policyType: string;
  description: string;
  minGuaranteedRatioPercent: number;
}

export interface DispatchSchedule {
  id: string;
  title: string;
  periodStart: string;
  periodEnd: string;
  status: DispatchStatus;
  targetWuaId: string | null;
  targetWuaName: string;
  totalRequestedM3: number;
  totalAllocatedM3: number;
  totalUnmetM3: number;
  assignments: DispatchAssignment[];
  conflicts: DispatchConflict[];
  metrics: Record<string, any>;
  fairnessPolicy: FairnessPolicy;
  notes: string | null;
  approvedBy: string | null;
  approvedAt: string | null;
  rejectionReason: string | null;
  createdBy: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface ActualDispatchDelivery {
  id: string;
  dispatchScheduleId: string | null;
  fieldId: string;
  fieldName: string;
  edgeId: string | null;
  canalName: string;
  actualStartAt: string;
  actualEndAt: string;
  plannedVolumeM3: number;
  actualVolumeM3: number;
  varianceVolumeM3: number;
  variancePercent: number;
  measuredFlowM3S: number;
  operatorId: string | null;
  operatorNotes: string | null;
  createdAt: string;
}

export interface ResilienceScenario {
  id: string;
  name: string;
  scenarioType: string;
  disabledNodeIds: string[];
  disabledEdgeIds: string[];
  capacityReductionPercent: number;
  demandSurgePercent: number;
  affectedFieldCount: number;
  lostCapacityM3S: number;
  unmetDemandM3: number;
  affectedWuaNames: string[];
  alternativeFeasibleRoutesCount: number;
  summary: string;
  simulationResults: Record<string, any>;
  createdBy: string | null;
  createdAt: string;
}

export interface DispatchOptimizationRequest {
  targetWuaId?: string;
  periodStart: string;
  periodEnd: string;
  fairnessPolicyType?: string;
  notes?: string;
}

export interface ApproveDispatchRequest {
  notes?: string;
}

export interface RejectDispatchRequest {
  reason: string;
}

export interface RecordActualDeliveryRequest {
  dispatchScheduleId?: string;
  fieldId: string;
  fieldName?: string;
  edgeId?: string;
  canalName?: string;
  actualStartAt: string;
  actualEndAt: string;
  plannedVolumeM3?: number;
  actualVolumeM3: number;
  measuredFlowM3S?: number;
  operatorNotes?: string;
}

export interface ResilienceScenarioRequest {
  name: string;
  scenarioType: string;
  disabledNodeIds?: string[];
  disabledEdgeIds?: string[];
  capacityReductionPercent?: number;
  demandSurgePercent?: number;
}
