import React from 'react'
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  CardDescription,
  Badge,
} from '@/shared/ui'
import {
  Cpu,
  ShieldCheck,
  CloudSun,
  Archive,
  Lock,
  CheckCircle2,
} from 'lucide-react'
import { SystemSettingsDto } from '@/entities/admin'
import { translations, SupportedLang } from '@/shared/i18n'

interface SystemSettingsViewProps {
  settings: SystemSettingsDto | null | undefined
  lang: SupportedLang
  isLoading?: boolean
}

export const SystemSettingsView: React.FC<SystemSettingsViewProps> = ({
  settings,
  lang,
  isLoading,
}) => {
  const t = translations[lang].admin

  if (isLoading || !settings) {
    return (
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 animate-pulse">
        {[1, 2, 3, 4].map((i) => (
          <div key={i} className="h-44 bg-muted rounded-xl" />
        ))}
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Settings Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Environment & Version */}
        <Card className="border shadow-xs">
          <CardHeader className="pb-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <div className="p-2 bg-primary/10 rounded-lg text-primary">
                  <Cpu className="w-4 h-4" />
                </div>
                <CardTitle className="text-sm font-semibold">{t.settingsSection.environment}</CardTitle>
              </div>
              <Badge variant="outline" className="font-mono text-xs uppercase bg-primary/5 text-primary border-primary/20">
                {settings.environment}
              </Badge>
            </div>
            <CardDescription className="text-xs">{t.settingsSection.runtimeDetails}</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2 text-xs">
            <div className="flex justify-between py-1.5 border-b">
              <span className="text-muted-foreground">{t.settingsSection.appVersion}:</span>
              <span className="font-mono font-medium">{settings.version}</span>
            </div>
            <div className="flex justify-between py-1.5 border-b">
              <span className="text-muted-foreground">Framework:</span>
              <span className="font-medium">Java 21 / Spring Boot 3.2</span>
            </div>
            <div className="flex justify-between py-1.5">
              <span className="text-muted-foreground">Architecture:</span>
              <span className="font-medium text-emerald-600 dark:text-emerald-400">Hexagonal / Clean</span>
            </div>
          </CardContent>
        </Card>

        {/* Security & Authentication Policy */}
        <Card className="border shadow-xs">
          <CardHeader className="pb-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <div className="p-2 bg-emerald-500/10 rounded-lg text-emerald-600">
                  <ShieldCheck className="w-4 h-4" />
                </div>
                <CardTitle className="text-sm font-semibold">{t.settingsSection.rbacPolicy}</CardTitle>
              </div>
              <Badge variant="outline" className="font-mono text-xs bg-emerald-500/10 text-emerald-700 dark:text-emerald-400 border-emerald-300">
                {t.settingsSection.enforced}
              </Badge>
            </div>
            <CardDescription className="text-xs">Centralized Role-Based Access Control</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2 text-xs">
            <div className="flex justify-between py-1.5 border-b">
              <span className="text-muted-foreground">{t.settingsSection.tokenExpiration}:</span>
              <span className="font-mono font-medium">{settings.accessTokenExpirationMinutes} min</span>
            </div>
            <div className="flex justify-between py-1.5 border-b">
              <span className="text-muted-foreground">Four-Eyes Principle:</span>
              <span className="font-semibold text-emerald-600 dark:text-emerald-400">
                {settings.fourEyesPrincipleStrict ? 'STRICT (Enforced)' : 'OPTIONAL'}
              </span>
            </div>
            <div className="flex justify-between py-1.5">
              <span className="text-muted-foreground">Rate Limiting:</span>
              <span className="font-medium text-foreground">
                {settings.rateLimitingEnabled ? `${settings.rateLimitPerMinute} req/min` : 'Disabled'}
              </span>
            </div>
          </CardContent>
        </Card>

        {/* External Adapters & Data Ingestion */}
        <Card className="border shadow-xs">
          <CardHeader className="pb-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <div className="p-2 bg-sky-500/10 rounded-lg text-sky-600">
                  <CloudSun className="w-4 h-4" />
                </div>
                <CardTitle className="text-sm font-semibold">{t.settingsSection.weatherProvider}</CardTitle>
              </div>
              <Badge variant="outline" className="font-mono text-xs bg-sky-500/10 text-sky-700 dark:text-sky-400 border-sky-300">
                {settings.weatherProvider}
              </Badge>
            </div>
            <CardDescription className="text-xs">Milestone 08.19 Ingestion Adapter</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2 text-xs">
            <div className="flex justify-between py-1.5 border-b">
              <span className="text-muted-foreground">Weather Connectivity:</span>
              <span className="font-medium text-foreground">Configured Adapter (Graceful Fallback)</span>
            </div>
            <div className="flex justify-between py-1.5 border-b">
              <span className="text-muted-foreground">{t.settingsSection.sensorGateway}:</span>
              <span className="font-mono font-medium">{settings.activeSensorProvider}</span>
            </div>
            <div className="flex justify-between py-1.5">
              <span className="text-muted-foreground">Hardware Actuation:</span>
              <span className="font-medium text-amber-600 dark:text-amber-400">
                {settings.physicalActuationProhibited ? 'PROHIBITED (Advisory Only)' : 'PERMITTED'}
              </span>
            </div>
          </CardContent>
        </Card>

        {/* Database & Disaster Recovery */}
        <Card className="border shadow-xs">
          <CardHeader className="pb-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <div className="p-2 bg-purple-500/10 rounded-lg text-purple-600">
                  <Archive className="w-4 h-4" />
                </div>
                <CardTitle className="text-sm font-semibold">{t.settingsSection.backupSchedule}</CardTitle>
              </div>
              <Badge variant="outline" className="font-mono text-xs bg-purple-500/10 text-purple-700 dark:text-purple-400 border-purple-300">
                DAILY 02:00 UTC
              </Badge>
            </div>
            <CardDescription className="text-xs">Milestone 08.18 Automated Snapshots</CardDescription>
          </CardHeader>
          <CardContent className="space-y-2 text-xs">
            <div className="flex justify-between py-1.5 border-b">
              <span className="text-muted-foreground">Migrations Engine:</span>
              <span className="font-medium">Liquibase (28 changesets)</span>
            </div>
            <div className="flex justify-between py-1.5 border-b">
              <span className="text-muted-foreground">Database Engine:</span>
              <span className="font-medium">PostgreSQL 16 + PostGIS</span>
            </div>
            <div className="flex justify-between py-1.5">
              <span className="text-muted-foreground">Server Time:</span>
              <span className="font-mono font-medium text-foreground">{new Date(settings.serverTime).toUTCString()}</span>
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Security Governance Principles Banner */}
      <Card className="border shadow-xs bg-muted/20">
        <CardHeader className="pb-2">
          <div className="flex items-center gap-2">
            <Lock className="w-4 h-4 text-primary" />
            <CardTitle className="text-xs font-semibold uppercase tracking-wider">
              Platform Security Governance Principles
            </CardTitle>
          </div>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3 text-xs">
            <div className="p-3 bg-card rounded-lg border">
              <div className="flex items-center gap-1.5 font-semibold text-foreground mb-1">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                <span>Zero Trust Default-Deny</span>
              </div>
              <p className="text-muted-foreground text-[11px]">
                All administrative endpoints require explicit role authorities; non-authenticated requests are rejected with 401/403.
              </p>
            </div>

            <div className="p-3 bg-card rounded-lg border">
              <div className="flex items-center gap-1.5 font-semibold text-foreground mb-1">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                <span>Anti-Privilege Escalation</span>
              </div>
              <p className="text-muted-foreground text-[11px]">
                Ordinary administrators cannot view or modify Super Administrator accounts, nor self-elevate permissions.
              </p>
            </div>

            <div className="p-3 bg-card rounded-lg border">
              <div className="flex items-center gap-1.5 font-semibold text-foreground mb-1">
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                <span>Immutable Audit Logging</span>
              </div>
              <p className="text-muted-foreground text-[11px]">
                Every user provisioning, role change, status mutation, and permission adjustment is immutably logged with actor, target, and IP.
              </p>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
