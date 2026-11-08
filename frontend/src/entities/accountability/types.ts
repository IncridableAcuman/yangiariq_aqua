export type IncidentCategory =
  | 'WATER_BALANCE_DISCREPANCY'
  | 'CONVEYANCE_LOSS'
  | 'UNACCOUNTED_WITHDRAWAL'
  | 'ALLOCATION_EXCEEDED'
  | 'UNDER_DELIVERY'
  | 'DISTRIBUTION_DISPARITY'
  | 'HYDRAULIC_SAFETY'
  | 'SENSOR_DATA_QUALITY'
  | 'OPERATIONAL_NON_COMPLIANCE'
  | 'OTHER'

export type IncidentSeverity =
  | 'CRITICAL'
  | 'HIGH'
  | 'MEDIUM'
  | 'LOW'
  | 'INFORMATIONAL'

export type EvidenceConfidence =
  | 'VERIFIED'
  | 'PARTIALLY_VERIFIED'
  | 'UNVERIFIED'
  | 'SIMULATED'
  | 'INSUFFICIENT_DATA'

export type IncidentStatus =
  | 'DETECTED'
  | 'UNDER_REVIEW'
  | 'OPEN'
  | 'INVESTIGATING'
  | 'ACTION_PROPOSED'
  | 'ACTION_APPROVED'
  | 'ACTION_IN_PROGRESS'
  | 'PENDING_VERIFICATION'
  | 'RESOLVED'
  | 'DISMISSED'
  | 'REOPENED'
  | 'CLOSED'

export type ActionStatus =
  | 'PROPOSED'
  | 'APPROVED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED'

export type VerificationOutcome =
  | 'VERIFIED_RESOLVED'
  | 'PARTIALLY_RESOLVED'
  | 'NOT_RESOLVED'
  | 'INSUFFICIENT_EVIDENCE'

export interface WaterIncident {
  id: string
  incidentNumber: string
  title: string
  description: string
  category: IncidentCategory
  severity: IncidentSeverity
  confidence: EvidenceConfidence
  status: IncidentStatus
  sourceModule: string
  sourceEntityType: string
  sourceEntityId?: string
  findingReference?: string
  wuaId?: string
  canalId?: string
  fieldId?: string
  locationDescription?: string
  detectionTimestamp: string
  slaDeadline?: string
  isOverdue: boolean
  assignedInvestigatorId?: string
  assignedInvestigatorName?: string
  responsibleOrganization?: string
  rootCauseCategory?: string
  rootCauseDetails?: string
  reviewNotes?: string
  reviewedBy?: string
  reviewedAt?: string
  resolutionNotes?: string
  recurrenceCount: number
  parentIncidentId?: string
  createdAt: string
  updatedAt: string
}

export interface IncidentEvidence {
  id: string
  incidentId: string
  evidenceType: string
  title: string
  description?: string
  evidenceUri?: string
  confidence: EvidenceConfidence
  isSimulated: boolean
  sourceReference?: string
  recordedBy?: string
  recordedAt: string
}

export interface CorrectiveAction {
  id: string
  incidentId: string
  actionNumber: string
  title: string
  description: string
  objective: string
  actionOwnerId?: string
  actionOwnerName?: string
  priority: string
  dueDate?: string
  expectedOutcome: string
  status: ActionStatus
  approvedBy?: string
  approvedAt?: string
  approvalNotes?: string
  completedBy?: string
  completedAt?: string
  completionNotes?: string
  verificationRequirement?: string
  createdAt: string
  updatedAt: string
}

export interface IncidentVerification {
  id: string
  incidentId: string
  actionId?: string
  verifierId: string
  verifierName: string
  verifiedAt: string
  verificationMethod: string
  result: VerificationOutcome
  explanatoryNotes: string
  supportingEvidenceUri?: string
}

export interface IncidentTimelineEntry {
  id: string
  incidentId: string
  eventType: string
  actorId?: string
  actorName?: string
  actorRole?: string
  timestamp: string
  previousState?: IncidentStatus
  newState?: IncidentStatus
  reason: string
  correlationId?: string
  metadataJson?: string
}

export interface IncidentDetails {
  incident: WaterIncident
  evidence: IncidentEvidence[]
  actions: CorrectiveAction[]
  verifications: IncidentVerification[]
  timeline: IncidentTimelineEntry[]
  canalName?: string
  wuaName?: string
  fieldName?: string
  isOverdue: boolean
}

export interface AccountabilityDashboardSummary {
  totalIncidents: number
  openIncidents: number
  criticalIncidents: number
  underInvestigation: number
  overdueIncidents: number
  pendingVerification: number
  resolvedIncidents: number
  reopenedIncidents: number
}

export interface CreateIncidentPayload {
  title: string
  description: string
  category: IncidentCategory
  severity: IncidentSeverity
  confidence?: EvidenceConfidence
  sourceModule?: string
  sourceEntityType?: string
  sourceEntityId?: string
  findingReference?: string
  wuaId?: string
  canalId?: string
  fieldId?: string
  locationDescription?: string
  detectionTimestamp?: string
}

export interface ReviewIncidentPayload {
  accept: boolean
  notes: string
}

export interface AssignInvestigatorPayload {
  investigatorId: string
  investigatorName?: string
  organization?: string
  reason?: string
}

export interface RecordInvestigationPayload {
  rootCauseCategory?: string
  rootCauseDetails?: string
  updatedConfidence?: EvidenceConfidence
  notes?: string
}

export interface AttachEvidencePayload {
  evidenceType: string
  title: string
  description?: string
  evidenceUri?: string
  confidence: EvidenceConfidence
  isSimulated?: boolean
  sourceReference?: string
}

export interface ProposeActionPayload {
  title: string
  description: string
  objective: string
  actionOwnerId?: string
  actionOwnerName?: string
  priority?: string
  dueDate?: string
  expectedOutcome: string
  verificationRequirement?: string
}

export interface ApproveActionPayload {
  actionId: string
  notes?: string
}

export interface CompleteActionPayload {
  actionId: string
  completionNotes: string
}

export interface VerifyResolutionPayload {
  actionId?: string
  verificationMethod: string
  result: VerificationOutcome
  explanatoryNotes: string
  supportingEvidenceUri?: string
  administrativeOverride?: boolean
}

export interface ReopenIncidentPayload {
  reason: string
}
