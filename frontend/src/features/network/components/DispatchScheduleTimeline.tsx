import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/ui/card';
import { Button } from '@/shared/ui/button';
import { Badge } from '@/shared/ui/badge';
import { DispatchSchedule } from '@/entities/network';
import { CanalStatusBadge } from './CanalStatusBadge';
import { DispatchApprovalDialog } from './DispatchApprovalDialog';
import {
  useStartExecutionMutation,
  useCompleteExecutionMutation,
  useRejectScheduleMutation,
} from '../queries/useNetworkQueries';
import { AlertTriangle, Calendar, Check, Clock, Play, X, UserCheck } from 'lucide-react';

interface DispatchScheduleTimelineProps {
  schedule?: DispatchSchedule | null;
  onRefresh?: () => void;
}

export const DispatchScheduleTimeline: React.FC<DispatchScheduleTimelineProps> = ({
  schedule,
  onRefresh,
}) => {
  const [isApprovalOpen, setIsApprovalOpen] = useState(false);

  const startMutation = useStartExecutionMutation();
  const completeMutation = useCompleteExecutionMutation();
  const rejectMutation = useRejectScheduleMutation();

  if (!schedule) {
    return (
      <Card className="border border-border/60 shadow-sm">
        <CardContent className="p-8 text-center text-muted-foreground">
          <Calendar className="h-8 w-8 mx-auto mb-2 text-muted-foreground/60" />
          <p className="text-sm font-medium">Faol taqsimot jadvali mavjud emas</p>
          <p className="text-xs mt-1">Yangi taqsimot taklifini hisoblash uchun yuqoridagi panelni ishlating.</p>
        </CardContent>
      </Card>
    );
  }

  const handleStart = async () => {
    await startMutation.mutateAsync(schedule.id);
    if (onRefresh) onRefresh();
  };

  const handleComplete = async () => {
    await completeMutation.mutateAsync(schedule.id);
    if (onRefresh) onRefresh();
  };

  const handleReject = async () => {
    const reason = window.prompt("Rad etish sababini kiriting:") || "Operator tomonidan rad etildi";
    await rejectMutation.mutateAsync({ id: schedule.id, req: { reason } });
    if (onRefresh) onRefresh();
  };

  const canApprove = schedule.status === 'PROPOSED' || schedule.status === 'REVIEW_REQUIRED';
  const canStart = schedule.status === 'APPROVED' || schedule.status === 'SCHEDULED';
  const canComplete = schedule.status === 'IN_PROGRESS';

  return (
    <div className="space-y-4">
      <Card className="border border-border/60 shadow-sm">
        <CardHeader className="pb-3 flex flex-row items-center justify-between">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <span className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
                {schedule.targetWuaName}
              </span>
              <CanalStatusBadge status={schedule.status} />
            </div>
            <CardTitle className="text-base font-bold text-foreground">
              {schedule.title}
            </CardTitle>
            <div className="flex items-center gap-3 text-xs text-muted-foreground">
              <span className="flex items-center gap-1">
                <Clock className="h-3.5 w-3.5" />
                {new Date(schedule.periodStart).toLocaleDateString()} — {new Date(schedule.periodEnd).toLocaleDateString()}
              </span>
              <span>&bull;</span>
              <span>Adolatlilik: {schedule.fairnessPolicy.policyType}</span>
            </div>
          </div>

          {/* Action buttons */}
          <div className="flex items-center gap-2">
            {canApprove && (
              <>
                <Button
                  size="sm"
                  onClick={() => setIsApprovalOpen(true)}
                  className="bg-emerald-600 hover:bg-emerald-700 text-white text-xs gap-1.5"
                >
                  <UserCheck className="h-3.5 w-3.5" />
                  Tasdiqlash (Inson Ko'rigi)
                </Button>
                <Button
                  variant="outline"
                  size="sm"
                  onClick={handleReject}
                  disabled={rejectMutation.isPending}
                  className="text-xs text-rose-600 border-rose-500/40 hover:bg-rose-500/10"
                >
                  <X className="h-3.5 w-3.5" />
                  Rad Etish
                </Button>
              </>
            )}

            {canStart && (
              <Button
                size="sm"
                onClick={handleStart}
                disabled={startMutation.isPending}
                className="bg-sky-600 hover:bg-sky-700 text-white text-xs gap-1.5"
              >
                <Play className="h-3.5 w-3.5" />
                Ijroni Boshlash (Oqim Ochildi)
              </Button>
            )}

            {canComplete && (
              <Button
                size="sm"
                onClick={handleComplete}
                disabled={completeMutation.isPending}
                className="bg-emerald-600 hover:bg-emerald-700 text-white text-xs gap-1.5"
              >
                <Check className="h-3.5 w-3.5" />
                Yakunlash (Oqim Yopildi)
              </Button>
            )}
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Summary volumetric banner */}
          <div className="grid grid-cols-3 gap-3 rounded-lg bg-muted/30 p-3 text-center text-xs">
            <div>
              <span className="text-muted-foreground">So'ralgan Jami</span>
              <p className="text-sm font-bold text-foreground mt-0.5">
                {schedule.totalRequestedM3.toLocaleString()} m³
              </p>
            </div>
            <div>
              <span className="text-muted-foreground">Rejalashtirilgan Taqsimot</span>
              <p className="text-sm font-bold text-emerald-600 dark:text-emerald-400 mt-0.5">
                {schedule.totalAllocatedM3.toLocaleString()} m³
              </p>
            </div>
            <div>
              <span className="text-muted-foreground">Qondirilmagan / Zaxira</span>
              <p className="text-sm font-bold text-amber-600 dark:text-amber-400 mt-0.5">
                {schedule.totalUnmetM3.toLocaleString()} m³
              </p>
            </div>
          </div>

          {/* Contention Conflicts Warning if any */}
          {schedule.conflicts && schedule.conflicts.length > 0 && (
            <div className="rounded-lg border border-amber-500/40 bg-amber-50/50 dark:bg-amber-950/20 p-3 space-y-2">
              <div className="flex items-center gap-1.5 text-xs font-bold text-amber-800 dark:text-amber-200">
                <AlertTriangle className="h-4 w-4 shrink-0 text-amber-600" />
                Kanal Sig'imi Ziddiyatlari ({schedule.conflicts.length})
              </div>
              <div className="space-y-1.5 max-h-32 overflow-y-auto pr-1">
                {schedule.conflicts.map((c) => (
                  <div key={c.conflictId} className="text-xs p-2 rounded bg-background/80 border border-amber-500/20">
                    <div className="flex items-center justify-between font-medium">
                      <span>{c.canalName}</span>
                      <span className="text-amber-700 dark:text-amber-300 font-semibold">
                        Talab: {c.aggregateFlowM3S.toFixed(2)} m³/s (Sig'im: {c.canalMaxCapacityM3S.toFixed(2)} m³/s)
                      </span>
                    </div>
                    <p className="text-muted-foreground text-[11px] mt-0.5">
                      Raqobatdosh dalalar: {c.competingFieldNames.join(', ')}
                    </p>
                    <p className="text-emerald-700 dark:text-emerald-400 text-[11px] font-medium mt-0.5">
                      Optimizator yechimi: {c.recommendedResolution}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Assignments Timeline List */}
          <div className="space-y-2">
            <h4 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Dala Bo'yicha Suv Olish Oynalari ({schedule.assignments.length})
            </h4>
            <div className="space-y-2 max-h-96 overflow-y-auto pr-1">
              {schedule.assignments.map((a) => (
                <div
                  key={a.assignmentId}
                  className={`rounded-lg border p-3 text-xs space-y-1.5 transition-colors ${
                    a.isBlocked
                      ? 'border-rose-500/40 bg-rose-50/40 dark:bg-rose-950/20'
                      : a.isStaggered
                        ? 'border-indigo-500/30 bg-card hover:bg-muted/10'
                        : 'border-border/60 bg-card hover:bg-muted/10'
                  }`}
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-foreground text-sm">{a.fieldName}</span>
                      <span className="text-muted-foreground text-xs">&bull; {a.cropType}</span>
                      {a.isStaggered && (
                        <Badge variant="outline" className="text-[10px] bg-indigo-500/10 text-indigo-600 border-indigo-500/30">
                          +{a.staggerDelayHours}s surilgan
                        </Badge>
                      )}
                      {a.isBlocked && (
                        <Badge variant="outline" className="text-[10px] bg-rose-500/10 text-rose-600 border-rose-500/30">
                          To'silgan
                        </Badge>
                      )}
                    </div>
                    <div className="text-right">
                      <span className="font-bold text-foreground">{a.allocatedVolumeM3.toLocaleString()} m³</span>
                      <span className="text-muted-foreground text-[11px] ml-1.5 font-mono">({a.flowRateM3S.toFixed(3)} m³/s)</span>
                    </div>
                  </div>

                  <div className="flex items-center justify-between text-muted-foreground text-[11px]">
                    <span className="flex items-center gap-1 font-mono">
                      <Clock className="h-3 w-3" />
                      {new Date(a.deliveryWindowStart).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} —{' '}
                      {new Date(a.deliveryWindowEnd).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} ({new Date(a.deliveryWindowStart).toLocaleDateString()})
                    </span>
                    <span className="font-medium text-foreground/80">Kanal: {a.canalName}</span>
                  </div>

                  {a.blockReason && (
                    <p className="text-rose-600 dark:text-rose-400 text-[11px] font-medium">
                      ✕ Sabab: {a.blockReason}
                    </p>
                  )}
                </div>
              ))}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Approval Dialog */}
      <DispatchApprovalDialog
        isOpen={isApprovalOpen}
        onClose={() => setIsApprovalOpen(false)}
        schedule={schedule}
        onApproved={() => {
          setIsApprovalOpen(false);
          if (onRefresh) onRefresh();
        }}
      />
    </div>
  );
};
