import React from 'react';
import { Badge } from '@/shared/ui/badge';
import { OperationalStatus, DispatchStatus } from '@/entities/network';

interface CanalStatusBadgeProps {
  status: OperationalStatus | DispatchStatus | string;
  className?: string;
}

export const CanalStatusBadge: React.FC<CanalStatusBadgeProps> = ({ status, className = '' }) => {
  const normalized = status.toUpperCase();

  switch (normalized) {
    case 'OPERATIONAL':
    case 'APPROVED':
    case 'SCHEDULED':
    case 'COMPLETED':
      return (
        <Badge variant="outline" className={`border-emerald-500/50 bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 font-medium ${className}`}>
          ● {status}
        </Badge>
      );
    case 'CONSTRAINED':
    case 'PROPOSED':
    case 'REVIEW_REQUIRED':
    case 'IN_PROGRESS':
    case 'OPTIMIZING':
      return (
        <Badge variant="outline" className={`border-amber-500/50 bg-amber-500/10 text-amber-600 dark:text-amber-400 font-medium ${className}`}>
          ▲ {status}
        </Badge>
      );
    case 'BLOCKED':
    case 'REJECTED':
      return (
        <Badge variant="outline" className={`border-rose-500/50 bg-rose-500/10 text-rose-600 dark:text-rose-400 font-semibold ${className}`}>
          ✕ {status}
        </Badge>
      );
    case 'MAINTENANCE':
    case 'OFFLINE':
    case 'CANCELLED':
    case 'EXPIRED':
    default:
      return (
        <Badge variant="outline" className={`border-slate-500/50 bg-slate-500/10 text-slate-600 dark:text-slate-400 ${className}`}>
          ○ {status}
        </Badge>
      );
  }
};
