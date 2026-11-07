import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/ui/card';
import { Button } from '@/shared/ui/button';
import { Input } from '@/shared/ui/input';
import { Label } from '@/shared/ui/label';
import { useOptimizeDispatchMutation } from '../queries/useNetworkQueries';
import { Sparkles, AlertCircle } from 'lucide-react';

interface DispatchOptimizationPanelProps {
  onSuccess?: () => void;
}

export const DispatchOptimizationPanel: React.FC<DispatchOptimizationPanelProps> = ({ onSuccess }) => {
  const [fairnessPolicy, setFairnessPolicy] = useState<string>('CROP_CRITICALITY_FIRST');
  const [notes, setNotes] = useState<string>('');

  const now = new Date();
  const nextWeek = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);

  const [periodStart, setPeriodStart] = useState<string>(now.toISOString().slice(0, 16));
  const [periodEnd, setPeriodEnd] = useState<string>(nextWeek.toISOString().slice(0, 16));

  const optimizeMutation = useOptimizeDispatchMutation();

  const handleOptimize = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await optimizeMutation.mutateAsync({
        periodStart: new Date(periodStart).toISOString(),
        periodEnd: new Date(periodEnd).toISOString(),
        fairnessPolicyType: fairnessPolicy,
        notes: notes || undefined,
      });
      if (onSuccess) onSuccess();
    } catch (err) {
      console.error('Optimization failed', err);
    }
  };

  return (
    <Card className="border border-border/60 shadow-sm">
      <CardHeader className="pb-3">
        <CardTitle className="text-base font-semibold flex items-center gap-2">
          <Sparkles className="h-4 w-4 text-indigo-500" />
          Kanal Suv Taqsimotini Optimizatsiya Qilish
        </CardTitle>
        <p className="text-xs text-muted-foreground">
          Ko'p dalali suv talabi va kanal o'tkazuvchanligi bo'yicha ziddiyatlarni hal qiluvchi taklif ishlab chiqish.
        </p>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleOptimize} className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div className="space-y-1.5">
              <Label htmlFor="periodStart" className="text-xs">Boshlanish Vaqti</Label>
              <Input
                id="periodStart"
                type="datetime-local"
                value={periodStart}
                onChange={(e) => setPeriodStart(e.target.value)}
                className="text-xs"
                required
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="periodEnd" className="text-xs">Tugash Vaqti</Label>
              <Input
                id="periodEnd"
                type="datetime-local"
                value={periodEnd}
                onChange={(e) => setPeriodEnd(e.target.value)}
                className="text-xs"
                required
              />
            </div>
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="fairnessPolicy" className="text-xs">Adolatlilik Siyosati (Fairness Policy)</Label>
            <select
              id="fairnessPolicy"
              value={fairnessPolicy}
              onChange={(e) => setFairnessPolicy(e.target.value)}
              className="flex h-9 w-full rounded-md border border-input bg-transparent px-3 py-1 text-xs shadow-xs focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring"
            >
              <option value="CROP_CRITICALITY_FIRST">
                Ekin Nozikligi Bo'yicha (Paxta/Bug'doy so'lish chegarasi birinchi)
              </option>
              <option value="MOISTURE_DEFICIT_PRIORITY">
                Namlik Tanqisligi Bo'yicha (Tuproq namligi eng past dalalar birinchi)
              </option>
              <option value="PROPORTIONAL_DEFICIT">
                Proporsional Taqsimot (Barcha dalalar o'rtasida teng defitsit ulushi)
              </option>
            </select>
          </div>

          <div className="space-y-1.5">
            <Label htmlFor="notes" className="text-xs">Qo'shimcha Izoh (Ixtiyoriy)</Label>
            <Input
              id="notes"
              type="text"
              placeholder="Masalan: Polvon magistral kanalida ta'mirlash rejalashtirilgan"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              className="text-xs"
            />
          </div>

          <div className="rounded-md border border-indigo-500/20 bg-indigo-50/50 dark:bg-indigo-950/20 p-2.5 text-xs text-indigo-900 dark:text-indigo-200 flex items-start gap-2">
            <AlertCircle className="h-4 w-4 shrink-0 mt-0.5 text-indigo-600 dark:text-indigo-400" />
            <span>
              <strong>Diqqat:</strong> Optimizatsiya tizimi faqat tavsiyaviy jadval taklifini shakllantiradi. Nasos yoki shlyuzlar avtomatik tarzda ochilmaydi; inson tasdig'i shart.
            </span>
          </div>

          <Button
            type="submit"
            disabled={optimizeMutation.isPending}
            className="w-full text-xs font-semibold gap-2 bg-indigo-600 hover:bg-indigo-700 text-white"
          >
            {optimizeMutation.isPending ? 'Optimizatsiya hisoblanmoqda...' : 'Taqsimot Rejasini Hisoblash & Taklif Yaratish'}
          </Button>

          {optimizeMutation.isError && (
            <p className="text-xs text-rose-600 dark:text-rose-400 font-medium text-center">
              Xatolik: {(optimizeMutation.error as Error).message}
            </p>
          )}
        </form>
      </CardContent>
    </Card>
  );
};
