import React, { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/ui/dialog';
import { Button } from '@/shared/ui/button';
import { Input } from '@/shared/ui/input';
import { Label } from '@/shared/ui/label';
import { DispatchSchedule } from '@/entities/network';
import { useApproveScheduleMutation } from '../queries/useNetworkQueries';
import { AlertTriangle, ShieldCheck, UserCheck } from 'lucide-react';

interface DispatchApprovalDialogProps {
  isOpen: boolean;
  onClose: () => void;
  schedule: DispatchSchedule;
  onApproved: () => void;
}

export const DispatchApprovalDialog: React.FC<DispatchApprovalDialogProps> = ({
  isOpen,
  onClose,
  schedule,
  onApproved,
}) => {
  const [notes, setNotes] = useState('');
  const approveMutation = useApproveScheduleMutation();

  const handleApprove = async () => {
    try {
      await approveMutation.mutateAsync({
        id: schedule.id,
        req: { notes: notes || undefined },
      });
      onApproved();
    } catch (err) {
      console.error('Approval failed', err);
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-base font-bold text-foreground">
            <ShieldCheck className="h-5 w-5 text-emerald-600" />
            Suv Taqsimot Jadvalini Tasdiqlash
          </DialogTitle>
          <DialogDescription className="text-xs text-muted-foreground pt-1">
            Ushbu taklif tuman suv xo'jaligi mutaxassisi tomonidan tasdiqlanishi talab etiladi.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-2 text-xs">
          {/* Key Facts Summary */}
          <div className="rounded-lg bg-muted/40 p-3 space-y-1.5 border border-border/40">
            <div className="flex justify-between">
              <span className="text-muted-foreground">Reja Nomi:</span>
              <span className="font-semibold text-foreground">{schedule.title}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Hudud / SIU:</span>
              <span className="font-semibold text-foreground">{schedule.targetWuaName}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Rejalashtirilgan Hajm:</span>
              <span className="font-semibold text-emerald-600 dark:text-emerald-400">
                {schedule.totalAllocatedM3.toLocaleString()} m³
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Dalalar Soni:</span>
              <span className="font-semibold text-foreground">{schedule.assignments.length} ta</span>
            </div>
          </div>

          {/* Operational Safety Warning */}
          <div className="rounded-lg border border-amber-500/30 bg-amber-50/50 dark:bg-amber-950/20 p-3 text-amber-900 dark:text-amber-200 flex items-start gap-2.5">
            <AlertTriangle className="h-4 w-4 shrink-0 mt-0.5 text-amber-600 dark:text-amber-400" />
            <div className="space-y-1 text-[11px] leading-relaxed">
              <strong>Xavfsizlik & Inson Mas'uliyati:</strong>
              <p>
                Ushbu tizim qaror qabul qilishni qo'llab-quvvatlovchi intellektual rejalashtirish moduli hisoblanadi.
                Nasoslar, shlyuzlar yoki klapanlar avtonom tarzda boshqarilmaydi. Haqiqiy suv ochish/yopish harakatlari faoliyatdagi navbatchi operator tomonidan qo'lda amalga oshiriladi.
              </p>
            </div>
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="approvalNotes" className="text-xs font-medium">
              Tasdiqlash Izohi (Ixtiyoriy)
            </Label>
            <Input
              id="approvalNotes"
              type="text"
              placeholder="Masalan: Gidrotexnik ko'rikdan o'tkazildi, navbatchilik tasdiqlandi"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              className="text-xs"
            />
          </div>

          {approveMutation.isError && (
            <p className="text-xs text-rose-600 dark:text-rose-400 font-medium">
              Xatolik: {(approveMutation.error as Error).message}
            </p>
          )}
        </div>

        <DialogFooter className="gap-2 sm:gap-0">
          <Button variant="outline" size="sm" onClick={onClose} className="text-xs">
            Bekor Qilish
          </Button>
          <Button
            size="sm"
            onClick={handleApprove}
            disabled={approveMutation.isPending}
            className="bg-emerald-600 hover:bg-emerald-700 text-white text-xs gap-1.5"
          >
            <UserCheck className="h-3.5 w-3.5" />
            {approveMutation.isPending ? 'Tasdiqlanmoqda...' : 'Tasdiqlayman & Jadvalga Kiritaman'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};
