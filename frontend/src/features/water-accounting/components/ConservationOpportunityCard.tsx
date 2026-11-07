import React from 'react';
import { ConservationOpportunityResponse } from '@/entities/water-accounting';
import { AccountingEvidenceBadge } from './AccountingEvidenceBadge';
import { Lightbulb, ArrowUpRight, Zap, Target } from 'lucide-react';

interface ConservationOpportunityCardProps {
  opportunity: ConservationOpportunityResponse;
  onNavigateWorkflow?: (uri: string) => void;
}

export const ConservationOpportunityCard: React.FC<ConservationOpportunityCardProps> = ({
  opportunity,
  onNavigateWorkflow,
}) => {
  const getCategoryBadge = (cat: string) => {
    switch (cat) {
      case 'CONVEYANCE_LOSS':
        return (
          <span className="px-2 py-0.5 rounded text-xs font-semibold bg-rose-100 text-rose-800 dark:bg-rose-950 dark:text-rose-300">
            Kanal Isrofi
          </span>
        );
      case 'IRRIGATION_INEFFICIENCY':
        return (
          <span className="px-2 py-0.5 rounded text-xs font-semibold bg-amber-100 text-amber-800 dark:bg-amber-950 dark:text-amber-300">
            Sugʻorish Samaradorligi
          </span>
        );
      case 'METER_CALIBRATION':
        return (
          <span className="px-2 py-0.5 rounded text-xs font-semibold bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-300">
            Oʻlchagich Kalibratsiyasi
          </span>
        );
      default:
        return (
          <span className="px-2 py-0.5 rounded text-xs font-semibold bg-muted text-muted-foreground">
            {cat}
          </span>
        );
    }
  };

  return (
    <div className="p-4 bg-card border rounded-xl shadow-xs space-y-3 flex flex-col justify-between">
      <div className="space-y-2">
        <div className="flex items-start justify-between gap-2">
          <div className="flex items-center gap-2">
            <div className="p-1.5 rounded-lg bg-primary/10 text-primary">
              <Lightbulb className="w-4 h-4" />
            </div>
            <div>
              <h4 className="font-semibold text-sm text-foreground">{opportunity.title}</h4>
              <p className="text-xs text-muted-foreground mt-0.5">
                Obyekt: <strong className="text-foreground">{opportunity.affectedComponentName}</strong> ({opportunity.affectedComponentType})
              </p>
            </div>
          </div>
          {getCategoryBadge(opportunity.category)}
        </div>

        <p className="text-xs text-muted-foreground leading-relaxed">
          {opportunity.problemDescription}
        </p>

        {/* Potential Savings Box */}
        <div className="p-2.5 bg-muted/40 rounded-lg text-xs space-y-1">
          <div className="flex items-center justify-between">
            <span className="text-muted-foreground font-medium flex items-center gap-1">
              <Zap className="w-3.5 h-3.5 text-amber-500" />
              Potentsial Suv Tejamkorligi:
            </span>
            <span className="font-bold text-emerald-600 dark:text-emerald-400">
              {opportunity.estimatedPotentialSavingsM3 != null
                ? `${opportunity.estimatedPotentialSavingsM3.toLocaleString()} m³`
                : 'Hisoblash imkonsiz (Aniq oʻlchov zarur)'}
            </span>
          </div>
          <p className="text-[11px] text-muted-foreground italic">
            Metodologiya: {opportunity.estimationMethodology}
          </p>
        </div>

        {/* Suggested Action */}
        <div className="text-xs space-y-0.5">
          <span className="font-semibold text-foreground flex items-center gap-1">
            <Target className="w-3.5 h-3.5 text-primary" />
            Tavsiya etilgan chora:
          </span>
          <p className="text-muted-foreground">{opportunity.suggestedAction}</p>
        </div>
      </div>

      <div className="pt-2 border-t flex items-center justify-between text-xs">
        <AccountingEvidenceBadge classification={opportunity.evidenceClassification} />

        {onNavigateWorkflow && opportunity.targetWorkflowUri && (
          <button
            onClick={() => onNavigateWorkflow(opportunity.targetWorkflowUri)}
            className="inline-flex items-center gap-1 text-xs font-semibold text-primary hover:underline"
          >
            <span>Bajarish</span>
            <ArrowUpRight className="w-3.5 h-3.5" />
          </button>
        )}
      </div>
    </div>
  );
};
