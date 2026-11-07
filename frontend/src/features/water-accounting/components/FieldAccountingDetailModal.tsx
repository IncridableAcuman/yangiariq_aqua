import React from 'react';
import { useFieldWaterAccounting } from '../queries/useWaterAccountingQueries';
import { AccountingEvidenceBadge } from './AccountingEvidenceBadge';
import { X, Droplets, ArrowRight, Lightbulb, Loader2 } from 'lucide-react';

interface FieldAccountingDetailModalProps {
  fieldId: string | null;
  onClose: () => void;
}

export const FieldAccountingDetailModal: React.FC<FieldAccountingDetailModalProps> = ({
  fieldId,
  onClose,
}) => {
  const { data: fieldData, isLoading, error } = useFieldWaterAccounting(fieldId ?? undefined);

  if (!fieldId) return null;

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-xs flex items-center justify-center p-4">
      <div className="bg-card border rounded-2xl shadow-xl w-full max-w-2xl max-h-[90vh] flex flex-col overflow-hidden animate-in fade-in zoom-in-95 duration-200">
        <div className="flex items-center justify-between p-4 border-b">
          <div className="flex items-center gap-2">
            <Droplets className="w-5 h-5 text-primary" />
            <div>
              <h3 className="font-semibold text-base text-foreground">
                {fieldData ? `${fieldData.fieldName} — Suv Hisoboti` : 'Dala Suv Hisoboti'}
              </h3>
              {fieldData && (
                <p className="text-xs text-muted-foreground">
                  SIU: {fieldData.wuaName} • Ekin: {fieldData.cropType} • Maydon: {fieldData.areaHectares} ga
                </p>
              )}
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-md text-muted-foreground hover:bg-muted transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-4 overflow-y-auto space-y-4 flex-1">
          {isLoading ? (
            <div className="flex flex-col items-center justify-center py-12 gap-2 text-muted-foreground">
              <Loader2 className="w-6 h-6 animate-spin text-primary" />
              <p className="text-xs">Dala hisob-kitob maʼlumotlari yuklanmoqda...</p>
            </div>
          ) : error ? (
            <div className="p-3 bg-rose-50 dark:bg-rose-950 border border-rose-200 dark:border-rose-800 rounded-lg text-xs text-rose-700 dark:text-rose-300">
              Maʼlumotlarni yuklashda xatolik yuz berdi.
            </div>
          ) : fieldData ? (
            <>
              {/* Compliance Box */}
              {fieldData.compliance && (
                <div className="p-3.5 bg-muted/40 border rounded-xl space-y-2.5">
                  <div className="flex items-center justify-between">
                    <span className="font-semibold text-xs text-foreground">Mavsumiy Kvota Muvofiqligi</span>
                    <AccountingEvidenceBadge classification={fieldData.compliance.evidenceClassification} />
                  </div>
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-xs">
                    <div>
                      <span className="text-muted-foreground text-[11px] block">Kvota</span>
                      <span className="font-bold">{fieldData.compliance.allocatedVolumeM3.toLocaleString()} m³</span>
                    </div>
                    <div>
                      <span className="text-muted-foreground text-[11px] block">Yetkazilgan</span>
                      <span className="font-bold text-blue-600 dark:text-blue-400">
                        {fieldData.compliance.actualDeliveredVolumeM3.toLocaleString()} m³
                      </span>
                    </div>
                    <div>
                      <span className="text-muted-foreground text-[11px] block">Isteʼmol</span>
                      <span className="font-bold text-indigo-600 dark:text-indigo-400">
                        {fieldData.compliance.actualConsumptionM3.toLocaleString()} m³
                      </span>
                    </div>
                    <div>
                      <span className="text-muted-foreground text-[11px] block">Qoldiq Kvota</span>
                      <span className="font-bold text-emerald-600 dark:text-emerald-400">
                        {fieldData.compliance.remainingAllocationM3.toLocaleString()} m³
                      </span>
                    </div>
                  </div>
                </div>
              )}

              {/* Recent Movements */}
              <div className="space-y-2">
                <h4 className="font-semibold text-xs text-foreground uppercase tracking-wider">
                  Oxirgi Suv Harakatlari (Recent Movements)
                </h4>
                {fieldData.recentMovements.length === 0 ? (
                  <p className="text-xs text-muted-foreground italic">Ushbu dala boʻyicha harakat yozuvlari mavjud emas.</p>
                ) : (
                  <div className="border rounded-xl divide-y text-xs">
                    {fieldData.recentMovements.slice(0, 5).map((m) => (
                      <div key={m.id} className="p-2.5 flex items-center justify-between gap-2">
                        <div>
                          <div className="flex items-center gap-1 font-medium">
                            <span>{m.sourceNodeOrCanal}</span>
                            <ArrowRight className="w-3 h-3 text-muted-foreground" />
                            <span className="text-primary">{m.destinationNodeOrField}</span>
                          </div>
                          <span className="text-[11px] text-muted-foreground">{new Date(m.eventTimestamp).toLocaleString()}</span>
                        </div>
                        <span className="font-bold font-mono">{m.volumeM3.toLocaleString()} m³</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {/* Recommendations */}
              {fieldData.recommendations.length > 0 && (
                <div className="space-y-2">
                  <h4 className="font-semibold text-xs text-foreground uppercase tracking-wider flex items-center gap-1.5">
                    <Lightbulb className="w-3.5 h-3.5 text-primary" />
                    <span>Tejamkorlik Tavsiyalari</span>
                  </h4>
                  <div className="space-y-2">
                    {fieldData.recommendations.map((rec) => (
                      <div key={rec.id} className="p-3 bg-card border rounded-lg text-xs space-y-1">
                        <p className="font-semibold text-foreground">{rec.title}</p>
                        <p className="text-muted-foreground">{rec.suggestedAction}</p>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </>
          ) : null}
        </div>
      </div>
    </div>
  );
};
