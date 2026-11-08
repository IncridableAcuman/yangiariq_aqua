export type IndicatorCategory =
  | 'DELIVERY_RELIABILITY'
  | 'CONVEYANCE_EFFICIENCY'
  | 'ALLOCATION_COMPLIANCE'
  | 'DISTRIBUTION_EQUITY'
  | 'IRRIGATION_OPERATIONS'
  | 'ACCOUNTABILITY_EFFECTIVENESS'
  | 'DATA_QUALITY'
  | 'PREDICTIVE_INTELLIGENCE'
  | 'CONSERVATION_OUTCOME'

export type MeasurementUnit =
  | 'M3'
  | 'M3_HA'
  | 'PERCENT'
  | 'HOURS'
  | 'DAYS'
  | 'COUNT'
  | 'RATIO'
  | 'HECTARES'

export type GeographicScopeType =
  | 'DISTRICT'
  | 'CANAL_REACH'
  | 'WUA'
  | 'FIELD'
  | 'ALL'

export type EvidenceClassification =
  | 'OBSERVED'
  | 'CALCULATED'
  | 'ESTIMATED'
  | 'SIMULATED'
  | 'MISSING'

export type BaselineQuality =
  | 'VERIFIED_BASELINE'
  | 'PARTIALLY_VERIFIED_BASELINE'
  | 'ESTIMATED_BASELINE'
  | 'SIMULATED_BASELINE'
  | 'INSUFFICIENT_BASELINE'

export type AttributionLevel =
  | 'DIRECTLY_VERIFIED'
  | 'STRONGLY_ASSOCIATED'
  | 'ASSOCIATED'
  | 'INCONCLUSIVE'
  | 'NOT_ASSESSABLE'

export type TargetType =
  | 'MINIMUM'
  | 'MAXIMUM'
  | 'RANGE'
  | 'DIRECTIONAL_IMPROVEMENT'

export type TargetCategory =
  | 'POLICY_THRESHOLD'
  | 'ENGINEERING_CONSTRAINT'
  | 'OPERATIONAL_TARGET'
  | 'PILOT_DEMO_TARGET'

export type TargetStatus =
  | 'APPROVED'
  | 'PROPOSED'
  | 'SUPERSEDED'
  | 'REJECTED'

export type TargetComplianceStatus =
  | 'MET'
  | 'EXCEEDED'
  | 'UNMET'
  | 'WITHIN_TOLERANCE'
  | 'INSUFFICIENT_DATA'

export type PerformanceTrend =
  | 'IMPROVING'
  | 'STABLE'
  | 'DEGRADING'
  | 'VOLATILE'
  | 'INSUFFICIENT_DATA'

export type AssessmentReviewStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'VERIFIED_OUTCOME'
  | 'REJECTED'
  | 'INCONCLUSIVE'

export type ChangeDirection =
  | 'FAVORABLE'
  | 'UNFAVORABLE'
  | 'NEUTRAL'

export interface PerformanceIndicator {
  id: string
  code: string
  name: string
  description: string
  category: IndicatorCategory
  measurementUnit: MeasurementUnit
  calculationMethod: string
  aggregationMethod: string
  supportedGeographicScope: GeographicScopeType
  evidenceRequirement: string
  minimumDataQualityPct: number
  applicableTimePeriod: string
  sourceModule: string
  calculationVersion: string
  createdAt: string
  updatedAt: string
}

export interface PerformanceBaseline {
  id: string
  indicatorCode: string
  name: string
  description?: string
  scopeType: GeographicScopeType
  scopeId?: string
  scopeName: string
  periodStart: string
  periodEnd: string
  baselineValue: number
  measurementUnit: MeasurementUnit
  dataCompletenessPct: number
  evidenceClassification: EvidenceClassification
  baselineQuality: BaselineQuality
  calculationMethod: string
  seasonContext?: string
  approvalStatus: string
  approvedBy?: string
  createdAt: string
}

export interface PerformanceTarget {
  id: string
  indicatorCode: string
  title: string
  scopeType: GeographicScopeType
  scopeId?: string
  scopeName: string
  targetType: TargetType
  targetCategory: TargetCategory
  targetValue: number
  rangeMin?: number
  rangeMax?: number
  measurementUnit: MeasurementUnit
  effectiveStart: string
  effectiveEnd?: string
  targetOwner: string
  status: TargetStatus
  rationale?: string
  targetVersion: string
  createdAt: string
}

export interface PerformanceEvaluation {
  id: string
  evaluationNumber: string
  indicatorCode: string
  scopeType: GeographicScopeType
  scopeId?: string
  scopeName: string
  periodStart: string
  periodEnd: string
  calculatedValue: number
  measurementUnit: MeasurementUnit
  baselineId?: string
  baselineValue?: number
  targetId?: string
  targetValue?: number
  targetCompliance: TargetComplianceStatus
  varianceFromBaselinePct?: number
  varianceFromTargetPct?: number
  trend: PerformanceTrend
  evidenceClassification: EvidenceClassification
  observedSampleRatio: number
  missingDataIntervals: number
  comparisonValid: boolean
  validityNotes?: string
  explanatorySummary: string
  calculationVersion: string
  evaluatedAt: string
}

export interface ImpactAssessment {
  id: string
  assessmentNumber: string
  title: string
  interventionType: string
  interventionId: string
  interventionReference: string
  interventionDate: string
  indicatorCode: string
  scopeType: GeographicScopeType
  scopeId?: string
  scopeName: string
  baselineId?: string
  baselinePeriodStart: string
  baselinePeriodEnd: string
  baselineValue: number
  evaluationPeriodStart: string
  evaluationPeriodEnd: string
  postInterventionValue: number
  absoluteChange: number
  percentageChange: number
  changeDirection: ChangeDirection
  measurementUnit: MeasurementUnit
  evidenceClassification: EvidenceClassification
  baselineQuality: BaselineQuality
  attributionLevel: AttributionLevel
  confoundingFactors?: string
  dataLimitations?: string
  comparisonValid: boolean
  methodologyNotes: string
  reviewStatus: AssessmentReviewStatus
  reviewerId?: string
  reviewerName?: string
  reviewNotes?: string
  reviewedAt?: string
  createdAt: string
}

export interface PerformanceOverview {
  totalWaterDeliveredM3: number
  totalWaterDeliveredEvidence: EvidenceClassification

  deliveryFulfillmentPct: number
  deliveryFulfillmentEvidence: EvidenceClassification

  averageConveyanceLossPct: number
  totalLossVolumeM3: number
  conveyanceLossEvidence: EvidenceClassification

  quotaCompliancePct: number
  quotaComplianceEvidence: EvidenceClassification

  distributionEquityCv: number
  distributionEquityLevel: string
  distributionEquityEvidence: EvidenceClassification

  totalIncidents: number
  overdueIncidents: number
  verifiedResolvedIncidents: number
  verifiedResolutionRatePct: number
  incidentRecurrenceRatePct: number
  accountabilityEvidence: EvidenceClassification

  sensorObservedCoveragePct: number
  activeSensorsCount: number
  dataQualityEvidence: EvidenceClassification

  verifiedConservationSavingsM3: number
  conservationEvidence: EvidenceClassification
}

export interface PerformanceTrendPoint {
  timestamp: string
  label: string
  actualValue: number | null
  baselineValue: number
  targetValue: number
  evidenceClassification: EvidenceClassification
  isMissing: boolean
  isSimulated: boolean
}

export interface PerformanceTrendSeries {
  indicatorCode: string
  indicatorName: string
  measurementUnit: string
  dataPoints: PerformanceTrendPoint[]
}

export type PerformanceTrendResponse = PerformanceTrendSeries

export interface PerformanceReport {
  reportId: string
  reportTitle: string
  generatedAt: string
  generatedBy: string
  reportingPeriodStart: string
  reportingPeriodEnd: string
  geographicScope: string
  legalDisclaimer: string
  overview: PerformanceOverview
  indicators: PerformanceIndicator[]
  evaluations: PerformanceEvaluation[]
  impactAssessments: ImpactAssessment[]
}
