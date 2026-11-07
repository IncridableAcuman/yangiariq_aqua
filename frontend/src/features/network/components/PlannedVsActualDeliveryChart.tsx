import React, { useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/ui/card';
import { Button } from '@/shared/ui/button';
import { Input } from '@/shared/ui/input';
import { Label } from '@/shared/ui/label';
import { useActualDeliveriesQuery, useRecordActualDeliveryMutation } from '../queries/useNetworkQueries';
import { ClipboardCheck, Plus, TrendingDown, TrendingUp } from 'lucide-react';

interface PlannedVsActualDeliveryChartProps {
  scheduleId?: string;
  fieldId?: string;
}

export const PlannedVsActualDeliveryChart: React.FC<PlannedVsActualDeliveryChartProps> = ({
  scheduleId,
  fieldId,
}) => {
  const { data: deliveries = [], isLoading, refetch } = useActualDeliveriesQuery(scheduleId, fieldId);
  const [isRecording, setIsRecording] = useState(false);

  const [formFieldId, setFormFieldId] = useState(fieldId || '');
  const [formFieldName, setFormFieldName] = useState('');
  const [plannedM3, setPlannedM3] = useState('');
  const [actualM3, setActualM3] = useState('');
  const [measuredFlow, setMeasuredFlow] = useState('');
  const [operatorNotes, setOperatorNotes] = useState('');

  const now = new Date();
  const past4Hours = new Date(now.getTime() - 4 * 60 * 60 * 1000);
  const [startAt, setStartAt] = useState(past4Hours.toISOString().slice(0, 16));
  const [endAt, setEndAt] = useState(now.toISOString().slice(0, 16));

  const recordMutation = useRecordActualDeliveryMutation();

  const handleRecord = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await recordMutation.mutateAsync({
        dispatchScheduleId: scheduleId || undefined,
        fieldId: formFieldId,
        fieldName: formFieldName || undefined,
        actualStartAt: new Date(startAt).toISOString(),
        actualEndAt: new Date(endAt).toISOString(),
        plannedVolumeM3: plannedM3 ? parseFloat(plannedM3) : undefined,
        actualVolumeM3: parseFloat(actualM3),
        measuredFlowM3S: measuredFlow ? parseFloat(measuredFlow) : undefined,
        operatorNotes: operatorNotes || undefined,
      });
      setIsRecording(false);
      setActualM3('');
      setPlannedM3('');
      setOperatorNotes('');
      refetch();
    } catch (err) {
      console.error('Failed to record actual delivery', err);
    }
  };

  return (
    <Card className="border border-border/60 shadow-sm">
      <CardHeader className="pb-3 flex flex-row items-center justify-between">
        <div>
          <CardTitle className="text-base font-semibold flex items-center gap-2">
            <ClipboardCheck className="h-4 w-4 text-emerald-600" />
            Reja va Haqiqiy Yetkazib Berish Solishtiruvi (Variance Audit)
          </CardTitle>
          <p className="text-xs text-muted-foreground mt-0.5">
            Dala darajasida o'lchangan suv hajmi va jadval bo'yicha og'ish tahlili.
          </p>
        </div>
        <Button
          size="sm"
          variant="outline"
          onClick={() => setIsRecording(!isRecording)}
          className="text-xs gap-1.5"
        >
          <Plus className="h-3.5 w-3.5" />
          {isRecording ? 'Bekor Qilish' : 'Fakt Yetkazishni Kiritish'}
        </Button>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Record Form */}
        {isRecording && (
          <form onSubmit={handleRecord} className="rounded-lg border border-border/80 bg-muted/20 p-3 space-y-3 text-xs">
            <h4 className="font-semibold text-foreground">Operator Fakt Hisoboti Kiritish</h4>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-2.5">
              <div className="space-y-1">
                <Label htmlFor="recFieldId" className="text-[11px]">Dala ID (UUID)</Label>
                <Input
                  id="recFieldId"
                  value={formFieldId}
                  onChange={(e) => setFormFieldId(e.target.value)}
                  placeholder="Dala UUID"
                  required
                  className="text-xs h-8"
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="recFieldName" className="text-[11px]">Dala Nomi</Label>
                <Input
                  id="recFieldName"
                  value={formFieldName}
                  onChange={(e) => setFormFieldName(e.target.value)}
                  placeholder="Masalan: Paxta-4"
                  className="text-xs h-8"
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="recActualM3" className="text-[11px]">Haqiqiy Yetkazilgan Hajm (m³)</Label>
                <Input
                  id="recActualM3"
                  type="number"
                  step="0.01"
                  value={actualM3}
                  onChange={(e) => setActualM3(e.target.value)}
                  placeholder="0.00"
                  required
                  className="text-xs h-8"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-3 gap-2.5">
              <div className="space-y-1">
                <Label htmlFor="recPlannedM3" className="text-[11px]">Rejalashtirilgan Hajm (m³)</Label>
                <Input
                  id="recPlannedM3"
                  type="number"
                  step="0.01"
                  value={plannedM3}
                  onChange={(e) => setPlannedM3(e.target.value)}
                  placeholder="0.00"
                  className="text-xs h-8"
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="recStartAt" className="text-[11px]">Boshlangan Vaqt</Label>
                <Input
                  id="recStartAt"
                  type="datetime-local"
                  value={startAt}
                  onChange={(e) => setStartAt(e.target.value)}
                  required
                  className="text-xs h-8"
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="recEndAt" className="text-[11px]">Tugagan Vaqt</Label>
                <Input
                  id="recEndAt"
                  type="datetime-local"
                  value={endAt}
                  onChange={(e) => setEndAt(e.target.value)}
                  required
                  className="text-xs h-8"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
              <div className="space-y-1">
                <Label htmlFor="recFlow" className="text-[11px]">O'lchangan Oqim Tezligi (m³/s)</Label>
                <Input
                  id="recFlow"
                  type="number"
                  step="0.001"
                  value={measuredFlow}
                  onChange={(e) => setMeasuredFlow(e.target.value)}
                  placeholder="0.000"
                  className="text-xs h-8"
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="recNotes" className="text-[11px]">Operator Izohi</Label>
                <Input
                  id="recNotes"
                  value={operatorNotes}
                  onChange={(e) => setOperatorNotes(e.target.value)}
                  placeholder="Gidropost ko'rsatkichi yoki suv sarfi tafsilotlari"
                  className="text-xs h-8"
                />
              </div>
            </div>

            <div className="flex justify-end gap-2 pt-1">
              <Button type="button" variant="ghost" size="sm" onClick={() => setIsRecording(false)} className="text-xs h-7">
                Bekor
              </Button>
              <Button type="submit" size="sm" disabled={recordMutation.isPending} className="text-xs h-7 bg-emerald-600 text-white">
                {recordMutation.isPending ? 'Saqlanmoqda...' : 'Auditga Saqlash'}
              </Button>
            </div>
          </form>
        )}

        {/* Deliveries Table */}
        {isLoading ? (
          <div className="h-32 bg-muted/30 rounded animate-pulse" />
        ) : deliveries.length === 0 ? (
          <p className="text-xs text-muted-foreground italic text-center py-6">
            Hali haqiqiy suv yetkazib berish audit yozuvlari kiritilmagan.
          </p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs text-left">
              <thead>
                <tr className="border-b border-border/60 text-muted-foreground">
                  <th className="pb-2 font-medium">Dala & Kanal</th>
                  <th className="pb-2 font-medium">Yetkazish Vaqti</th>
                  <th className="pb-2 font-medium text-right">Reja (m³)</th>
                  <th className="pb-2 font-medium text-right">Haqiqiy (m³)</th>
                  <th className="pb-2 font-medium text-right">Farq (m³ / %)</th>
                  <th className="pb-2 font-medium">Izoh</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border/40">
                {deliveries.map((d) => {
                  const isOver = d.varianceVolumeM3 > 0;
                  const isUnder = d.varianceVolumeM3 < 0;

                  return (
                    <tr key={d.id} className="hover:bg-muted/10">
                      <td className="py-2.5 font-medium text-foreground">
                        <div>{d.fieldName}</div>
                        <div className="text-[10px] text-muted-foreground">{d.canalName}</div>
                      </td>
                      <td className="py-2.5 text-muted-foreground font-mono text-[11px]">
                        {new Date(d.actualStartAt).toLocaleDateString()} {new Date(d.actualStartAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                      </td>
                      <td className="py-2.5 text-right font-medium">{d.plannedVolumeM3.toLocaleString()}</td>
                      <td className="py-2.5 text-right font-bold text-foreground">{d.actualVolumeM3.toLocaleString()}</td>
                      <td className="py-2.5 text-right font-medium">
                        <div className={`inline-flex items-center gap-1 ${
                          isOver ? 'text-amber-600 dark:text-amber-400' : isUnder ? 'text-sky-600 dark:text-sky-400' : 'text-emerald-600'
                        }`}>
                          {isOver ? <TrendingUp className="h-3 w-3" /> : isUnder ? <TrendingDown className="h-3 w-3" /> : null}
                          <span>{d.varianceVolumeM3 > 0 ? `+${d.varianceVolumeM3}` : d.varianceVolumeM3} m³</span>
                          <span className="text-[10px]">({d.variancePercent > 0 ? `+${d.variancePercent}` : d.variancePercent}%)</span>
                        </div>
                      </td>
                      <td className="py-2.5 text-muted-foreground text-[11px] max-w-[200px] truncate">
                        {d.operatorNotes || '-'}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </CardContent>
    </Card>
  );
};
