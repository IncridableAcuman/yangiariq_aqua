import React, { useState } from 'react';
import { WaterReconciliationResponse } from '@/entities/water-accounting';
import { useReviewReconciliationMutation } from '../queries/useWaterAccountingQueries';
import { ShieldCheck, X, AlertTriangle, Loader2 } from 'lucide-react';

interface ReconciliationReviewDialogProps {
  item: WaterReconciliationResponse;
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

export const ReconciliationReviewDialog: React.FC<ReconciliationReviewDialogProps> = ({
  item,
  isOpen,
  onClose,
  onSuccess,
}) => {
  const [notes, setNotes] = useState(
    item.reviewNotes ?? 'Gidrometrik oʻlchov natijalari va dalolatnoma asosida tasdiqlandi.'
  );
  const [error, setError] = useState<string | null>(null);

  const reviewMutation = useReviewReconciliationMutation();

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!notes.trim()) {
      setError('Koʻrib chiqish xulosasi va dalolatnoma izohi kiritilishi shart');
      return;
    }
    setError(null);

    try {
      await reviewMutation.mutateAsync({
        id: item.id,
        request: { notes: notes.trim() },
      });
      onSuccess?.();
      onClose();
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Tasdiqlashda xatolik yuz berdi');
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-card border rounded-2xl shadow-xl w-full max-w-lg overflow-hidden animate-in fade-in zoom-in-95 duration-200">
        <div className="flex items-center justify-between p-4 border-b">
          <div className="flex items-center gap-2">
            <ShieldCheck className="w-5 h-5 text-primary" />
            <h3 className="font-semibold text-base text-foreground">
              Tafovutni Rasmiylashtirish (Review Reconciliation)
            </h3>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-md text-muted-foreground hover:bg-muted transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-4 space-y-4">
          <div className="p-3 bg-muted/40 rounded-xl space-y-2 text-xs">
            <div className="flex justify-between">
              <span className="text-muted-foreground">Bosqich:</span>
              <span className="font-semibold text-foreground">{item.boundaryName}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Kutilgan / Qayd qilingan:</span>
              <span className="font-mono">
                {item.expectedVolumeM3.toLocaleString()} / {item.recordedVolumeM3.toLocaleString()} m³
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Tafovut:</span>
              <span className="font-bold text-rose-600 dark:text-rose-400">
                {item.discrepancyVolumeM3.toLocaleString()} m³ ({item.discrepancyPercent.toFixed(2)}%)
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-muted-foreground">Ruxsat etilgan meʼyor:</span>
              <span className="font-mono text-muted-foreground">&plusmn;{item.tolerancePercent}%</span>
            </div>
          </div>

          <div className="space-y-1.5">
            <label className="text-xs font-semibold text-foreground">
              Operator / Dispecher Xulosasi (Audit Notes) *
            </label>
            <textarea
              rows={3}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Tafovut sababi, tekshiruv dalolatnomasi yoki oʻlchov asbobi koʻrsatkichlari..."
              className="w-full p-2.5 text-xs bg-background border rounded-lg focus:outline-hidden focus:ring-1 focus:ring-primary"
            />
            <p className="text-[11px] text-muted-foreground">
              Ushbu xulosa tizim audit daftariga kiritiladi va solishtirish holatini tasdiqlangan deb belgilaydi.
            </p>
          </div>

          {error && (
            <div className="p-2.5 bg-rose-50 dark:bg-rose-950/50 border border-rose-200 dark:border-rose-800 rounded-lg text-xs text-rose-700 dark:text-rose-300 flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div className="flex items-center justify-end gap-2 pt-2 border-t">
            <button
              type="button"
              onClick={onClose}
              disabled={reviewMutation.isPending}
              className="px-3.5 py-2 text-xs font-medium border rounded-lg hover:bg-muted transition-colors"
            >
              Bekor qilish
            </button>
            <button
              type="submit"
              disabled={reviewMutation.isPending}
              className="px-4 py-2 text-xs font-semibold rounded-lg bg-primary text-primary-foreground hover:bg-primary/90 flex items-center gap-1.5 transition-colors disabled:opacity-50"
            >
              {reviewMutation.isPending && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
              <span>Tasdiqlash va Qayd Qilish</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
