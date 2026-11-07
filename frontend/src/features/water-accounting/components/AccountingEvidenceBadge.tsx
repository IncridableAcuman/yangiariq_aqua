import React from 'react';
import { EvidenceClassification } from '@/entities/water-accounting';
import { CheckCircle2, Calculator, Sparkles, AlertCircle, HelpCircle } from 'lucide-react';

interface AccountingEvidenceBadgeProps {
  classification: EvidenceClassification;
  showIcon?: boolean;
  className?: string;
}

export const AccountingEvidenceBadge: React.FC<AccountingEvidenceBadgeProps> = ({
  classification,
  showIcon = true,
  className = '',
}) => {
  switch (classification) {
    case 'OBSERVED':
      return (
        <span
          className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-medium bg-emerald-100 text-emerald-800 dark:bg-emerald-950/60 dark:text-emerald-300 border border-emerald-300 dark:border-emerald-800 ${className}`}
          title="Directly measured or authoritatively recorded in the field"
        >
          {showIcon && <CheckCircle2 className="w-3 h-3 text-emerald-600 dark:text-emerald-400" />}
          Kuzatilgan (Observed)
        </span>
      );
    case 'CALCULATED':
      return (
        <span
          className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-950/60 dark:text-blue-300 border border-blue-300 dark:border-blue-800 ${className}`}
          title="Derived from verified source values using documented formulas"
        >
          {showIcon && <Calculator className="w-3 h-3 text-blue-600 dark:text-blue-400" />}
          Hisoblangan (Calculated)
        </span>
      );
    case 'ESTIMATED':
      return (
        <span
          className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-800 dark:bg-amber-950/60 dark:text-amber-300 border border-amber-300 dark:border-amber-800 ${className}`}
          title="Based on explicit agronomic or hydrometric estimation methods"
        >
          {showIcon && <HelpCircle className="w-3 h-3 text-amber-600 dark:text-amber-400" />}
          Baholangan (Estimated)
        </span>
      );
    case 'SIMULATED':
      return (
        <span
          className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800 dark:bg-purple-950/60 dark:text-purple-300 border border-purple-300 dark:border-purple-800 ${className}`}
          title="Simulated via network hydrological model or mock sensor telemetry"
        >
          {showIcon && <Sparkles className="w-3 h-3 text-purple-600 dark:text-purple-400" />}
          Simulyatsiya (Simulated)
        </span>
      );
    case 'MISSING':
    default:
      return (
        <span
          className={`inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-medium bg-rose-100 text-rose-800 dark:bg-rose-950/60 dark:text-rose-300 border border-rose-300 dark:border-rose-800 ${className}`}
          title="Telemetry or field records are missing"
        >
          {showIcon && <AlertCircle className="w-3 h-3 text-rose-600 dark:text-rose-400" />}
          Mavjud emas (Missing)
        </span>
      );
  }
};
