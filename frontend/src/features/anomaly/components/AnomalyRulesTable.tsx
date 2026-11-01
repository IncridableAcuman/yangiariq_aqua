import React, { useState } from 'react'
import { AnomalyRuleConfig } from '@/entities/anomaly'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { useAuth } from '@/features/auth/context/AuthContext'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { AnomalySeverityBadge } from './AnomalySeverityBadge'
import { EditAnomalyRuleModal } from './EditAnomalyRuleModal'
import { useAnomalyRules, useToggleAnomalyRuleMutation } from '../queries/useAnomalyQueries'
import { Edit, ShieldAlert, Sliders } from 'lucide-react'

interface AnomalyRulesTableProps {
  lang: SupportedLanguage
}

export const AnomalyRulesTable: React.FC<AnomalyRulesTableProps> = ({ lang }) => {
  const t = translations[lang].anomalies.rules
  const { user } = useAuth()
  const isAdmin = user?.role === 'ADMIN'

  const { data: rules = [], isLoading } = useAnomalyRules()
  const toggleMutation = useToggleAnomalyRuleMutation()

  const [selectedRule, setSelectedRule] = useState<AnomalyRuleConfig | null>(null)
  const [isEditModalOpen, setIsEditModalOpen] = useState(false)

  const handleToggle = async (rule: AnomalyRuleConfig) => {
    if (!isAdmin) return
    try {
      await toggleMutation.mutateAsync({ id: rule.id, enabled: !rule.enabled })
    } catch {
      // handled by mutation error
    }
  }

  const handleEdit = (rule: AnomalyRuleConfig) => {
    if (!isAdmin) return
    setSelectedRule(rule)
    setIsEditModalOpen(true)
  }

  return (
    <div className="space-y-4">
      {/* Header and Disclaimer */}
      <div className="p-4 bg-card border border-border rounded-xl space-y-1">
        <div className="flex items-center gap-2">
          <Sliders className="w-4 h-4 text-primary" />
          <h3 className="font-semibold text-sm text-foreground">{t.title}</h3>
        </div>
        <p className="text-xs text-muted-foreground">{t.subtitle}</p>
        {!isAdmin && (
          <div className="flex items-center gap-1.5 pt-2 text-xs text-amber-600 dark:text-amber-400">
            <ShieldAlert className="w-3.5 h-3.5" />
            <span>{t.adminOnly}</span>
          </div>
        )}
      </div>

      {/* Rules Table */}
      {isLoading ? (
        <div className="py-12 flex justify-center">
          <div className="w-6 h-6 border-2 border-primary border-t-transparent rounded-full animate-spin" />
        </div>
      ) : (
        <div className="border border-border rounded-xl overflow-x-auto bg-card">
          <table className="w-full text-left text-xs">
            <thead className="bg-muted/50 text-muted-foreground font-semibold border-b border-border">
              <tr>
                <th className="p-3">{t.code}</th>
                <th className="p-3">{t.name}</th>
                <th className="p-3">{t.sensorType}</th>
                <th className="p-3">Daraja</th>
                <th className="p-3 text-right">{t.threshold}</th>
                <th className="p-3 text-right">{t.secondaryThreshold}</th>
                <th className="p-3 text-right">{t.window}</th>
                <th className="p-3 text-center">{t.status}</th>
                {isAdmin && <th className="p-3 text-center">Amal</th>}
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {rules.map((rule) => (
                <tr key={rule.id} className="hover:bg-muted/30 transition-colors">
                  <td className="p-3 font-mono font-medium text-foreground">{rule.ruleCode}</td>
                  <td className="p-3">
                    <span className="font-semibold text-foreground block">{rule.name}</span>
                    {rule.description && (
                      <span className="text-[11px] text-muted-foreground block line-clamp-1">
                        {rule.description}
                      </span>
                    )}
                  </td>
                  <td className="p-3">
                    <Badge variant="outline" className="text-[10px]">
                      {rule.sensorType}
                    </Badge>
                  </td>
                  <td className="p-3">
                    <AnomalySeverityBadge severity={rule.severity} lang={lang} />
                  </td>
                  <td className="p-3 text-right font-bold text-foreground">
                    {rule.threshold}
                  </td>
                  <td className="p-3 text-right text-muted-foreground font-medium">
                    {rule.secondaryThreshold !== null && rule.secondaryThreshold !== undefined
                      ? rule.secondaryThreshold
                      : '-'}
                  </td>
                  <td className="p-3 text-right text-muted-foreground">
                    {rule.timeWindowMinutes} daq
                  </td>
                  <td className="p-3 text-center">
                    <div className="flex items-center justify-center gap-1.5">
                      <input
                        type="checkbox"
                        checked={rule.enabled}
                        onChange={() => handleToggle(rule)}
                        disabled={!isAdmin || toggleMutation.isPending}
                        className="h-4 w-4 rounded border-input text-primary focus:ring-primary cursor-pointer disabled:opacity-50"
                      />
                      <span className="text-[10px] text-muted-foreground">
                        {rule.enabled ? t.enabled : t.disabled}
                      </span>
                    </div>
                  </td>
                  {isAdmin && (
                    <td className="p-3 text-center">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleEdit(rule)}
                        className="h-7 w-7 p-0"
                      >
                        <Edit className="w-3.5 h-3.5 text-muted-foreground hover:text-foreground" />
                      </Button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Edit Rule Modal */}
      <EditAnomalyRuleModal
        isOpen={isEditModalOpen}
        onClose={() => {
          setIsEditModalOpen(false)
          setSelectedRule(null)
        }}
        rule={selectedRule}
        lang={lang}
      />
    </div>
  )
}
