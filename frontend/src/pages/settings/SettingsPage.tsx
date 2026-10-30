import React, { useState } from 'react'
import {
  Settings,
  Bell,
  Sparkles,
  Save,
  CheckCircle2,
  KeyRound,
  User,
} from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { useAuth } from '@/features/auth/context/AuthContext'
import { SupportedLanguage } from '@/shared/i18n'
import { ChangePasswordModal } from '@/features/auth/ui/ChangePasswordModal'
import { UserProfileModal } from '@/features/auth/ui/UserProfileModal'

interface SettingsPageProps {
  lang: SupportedLanguage
}

export const SettingsPage: React.FC<SettingsPageProps> = ({ lang }) => {
  const { user } = useAuth()
  const [profileModalOpen, setProfileModalOpen] = useState(false)
  const [changePasswordModalOpen, setChangePasswordModalOpen] = useState(false)
  const [saveSuccess, setSaveSuccess] = useState(false)

  // Settings State
  const [telemetryInterval, setTelemetryInterval] = useState('5')
  const [aiMode, setAiMode] = useState('MAX_SAVING')
  const [smsAlerts, setSmsAlerts] = useState(true)
  const [telegramAlerts, setTelegramAlerts] = useState(true)

  const handleSave = () => {
    setSaveSuccess(true)
    setTimeout(() => setSaveSuccess(false), 2500)
  }

  return (
    <div className="space-y-6 max-w-4xl">
      {/* Title */}
      <div>
        <h2 className="text-2xl font-bold tracking-tight text-foreground flex items-center gap-2">
          <Settings className="h-6 w-6 text-primary" />
          <span>{lang === 'uz' ? 'Tizim va Profil Sozlamalari' : 'Настройки системы и профиля'}</span>
        </h2>
        <p className="text-xs text-muted-foreground mt-0.5">
          {lang === 'uz'
            ? 'Yangiariq Aqua AI boshqaruv parametrlari, bildirishnomalar va shaxsiy hisob sozlamalari'
            : 'Параметры платформы, уведомления и настройки аккаунта'}
        </p>
      </div>

      {saveSuccess && (
        <div className="p-3.5 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-700 dark:text-emerald-300 text-xs flex items-center gap-2">
          <CheckCircle2 className="h-4 w-4 shrink-0" />
          <span>{lang === 'uz' ? 'Sozlamalar muvaffaqiyatli saqlandi!' : 'Настройки успешно сохранены!'}</span>
        </div>
      )}

      {/* Profile Summary Card */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle className="text-base font-bold">
                {lang === 'uz' ? 'Foydalanuvchi hisobi' : 'Профиль пользователя'}
              </CardTitle>
              <CardDescription>
                {lang === 'uz' ? 'Shaxsiy maʼlumotlar va avtorizatsiya' : 'Личные данные и авторизация'}
              </CardDescription>
            </div>
            <Badge variant="outline" className="bg-primary/10 text-primary border-primary/20">
              {user?.role || 'FARMER'}
            </Badge>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-xs">
            <div className="p-3 rounded-lg bg-muted/40 border border-border/60">
              <span className="text-muted-foreground block text-[11px]">
                {lang === 'uz' ? 'Foydalanuvchi' : 'Пользователь'}:
              </span>
              <span className="font-bold text-foreground text-sm mt-0.5 block">
                {user?.fullName || `${user?.firstName || 'Rustam'} ${user?.lastName || 'Karimov'}`}
              </span>
            </div>

            <div className="p-3 rounded-lg bg-muted/40 border border-border/60">
              <span className="text-muted-foreground block text-[11px]">
                {lang === 'uz' ? 'Telefon raqam' : 'Телефон'}:
              </span>
              <span className="font-bold text-foreground font-mono text-sm mt-0.5 block">
                {user?.phoneNumber || '+998 90 123 45 67'}
              </span>
            </div>
          </div>

          <div className="flex items-center gap-2 pt-2">
            <Button
              variant="outline"
              size="sm"
              onClick={() => setProfileModalOpen(true)}
              className="text-xs"
            >
              <User className="h-3.5 w-3.5 mr-1.5" />
              {lang === 'uz' ? 'Profilni tahrirlash' : 'Редактировать профиль'}
            </Button>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setChangePasswordModalOpen(true)}
              className="text-xs"
            >
              <KeyRound className="h-3.5 w-3.5 mr-1.5" />
              {lang === 'uz' ? 'Parolni yangilash' : 'Сменить пароль'}
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* AI & Telemetry Preferences */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base font-bold flex items-center gap-2">
            <Sparkles className="h-4 w-4 text-primary" />
            <span>{lang === 'uz' ? 'AI Maslahat va Telemetriya Parametrlari' : 'Параметры ИИ и телеметрии'}</span>
          </CardTitle>
          <CardDescription>
            {lang === 'uz'
              ? 'Datchiklar oʻlchash chastotasi va suv tejash algoritmi darajasi'
              : 'Частота опроса датчиков и алгоритм экономии воды'}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-foreground">
                {lang === 'uz' ? 'Datchik telemetriyasi yangilanish oraligʻi' : 'Частота обновления телеметрии'}
              </label>
              <select
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                value={telemetryInterval}
                onChange={(e) => setTelemetryInterval(e.target.value)}
              >
                <option value="1">Har 1 daqiqada (Real-vaqt)</option>
                <option value="5">Har 5 daqiqada (Tavsiya etiladi)</option>
                <option value="15">Har 15 daqiqada</option>
                <option value="60">Har 1 soatda (Batareyani tejash)</option>
              </select>
            </div>

            <div className="space-y-1.5">
              <label className="text-xs font-semibold text-foreground">
                {lang === 'uz' ? 'AI Suv Tejash Rejimi' : 'Режим оптимизации ИИ'}
              </label>
              <select
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-ring"
                value={aiMode}
                onChange={(e) => setAiMode(e.target.value)}
              >
                <option value="MAX_SAVING">Maksimal suv tejash (Tungi sugʻorish ustuvor)</option>
                <option value="BALANCED">Balansli (Hosildorlik va suv balansi)</option>
                <option value="HIGH_YIELD">Hosildorlikka yoʻnaltirilgan</option>
              </select>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Notifications Settings */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base font-bold flex items-center gap-2">
            <Bell className="h-4 w-4 text-primary" />
            <span>{lang === 'uz' ? 'Xabarnomalar va Ogohlantirishlar' : 'Уведомления и оповещения'}</span>
          </CardTitle>
          <CardDescription>
            {lang === 'uz'
              ? 'Kritik namlik pasayishi va suv ochilish vaqtlari boʻyicha bildirishnomalar'
              : 'Оповещения о снижении влажности и графике подачи воды'}
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          <label className="flex items-center justify-between p-3 rounded-lg border border-border/70 bg-card hover:bg-muted/40 cursor-pointer">
            <div>
              <p className="text-xs font-semibold text-foreground">
                {lang === 'uz' ? 'SMS orqali tezkor ogohlantirishlar' : 'SMS-уведомления'}
              </p>
              <p className="text-[11px] text-muted-foreground">
                {lang === 'uz' ? 'Kritik qurgʻoqchilik va avariya holatlarida' : 'При критической засухе и авариях'}
              </p>
            </div>
            <input
              type="checkbox"
              checked={smsAlerts}
              onChange={(e) => setSmsAlerts(e.target.checked)}
              className="h-4 w-4 rounded border-input text-primary focus:ring-primary"
            />
          </label>

          <label className="flex items-center justify-between p-3 rounded-lg border border-border/70 bg-card hover:bg-muted/40 cursor-pointer">
            <div>
              <p className="text-xs font-semibold text-foreground">
                {lang === 'uz' ? 'Telegram bot xabarnomalari (@YangiariqAquaBot)' : 'Telegram-бот'}
              </p>
              <p className="text-[11px] text-muted-foreground">
                {lang === 'uz' ? 'Kundalik suv balansi va AI agronom tavsiyalari' : 'Ежедневный гидробаланс и советы ИИ'}
              </p>
            </div>
            <input
              type="checkbox"
              checked={telegramAlerts}
              onChange={(e) => setTelegramAlerts(e.target.checked)}
              className="h-4 w-4 rounded border-input text-primary focus:ring-primary"
            />
          </label>
        </CardContent>
      </Card>

      {/* Save Button */}
      <div className="flex justify-end">
        <Button onClick={handleSave} className="gap-2">
          <Save className="h-4 w-4" />
          <span>{lang === 'uz' ? 'Sozlamalarni saqlash' : 'Сохранить настройки'}</span>
        </Button>
      </div>

      {/* Modals */}
      <UserProfileModal
        isOpen={profileModalOpen}
        onClose={() => setProfileModalOpen(false)}
        onOpenChangePassword={() => {
          setProfileModalOpen(false)
          setChangePasswordModalOpen(true)
        }}
        lang={lang}
      />
      <ChangePasswordModal
        isOpen={changePasswordModalOpen}
        onClose={() => setChangePasswordModalOpen(false)}
        lang={lang}
      />
    </div>
  )
}
