import React, { useState } from 'react'
import {
  Cpu,
  Battery,
  Wifi,
  RefreshCw,
  Droplets,
  Activity,
} from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/shared/ui/tabs'
import { SupportedLanguage } from '@/shared/i18n'

interface SensorData {
  id: string
  name: string
  type: 'SOIL_MOISTURE' | 'WATER_FLOW' | 'CANAL_LEVEL'
  location: string
  value: string
  unit: string
  temperature?: string
  battery: number
  signal: 'STRONG' | 'MEDIUM' | 'WEAK'
  lastPing: string
  status: 'ONLINE' | 'WARNING'
}

const INITIAL_SENSORS: SensorData[] = [
  {
    id: 'ST-SOIL-01',
    name: 'Tuproq Koʻp Qatlamli Datchik №1',
    type: 'SOIL_MOISTURE',
    location: 'Dala №01 (Yangiariq Markaz)',
    value: '68.4',
    unit: '%',
    temperature: '+21.5 °C',
    battery: 92,
    signal: 'STRONG',
    lastPing: '3 daq oldin',
    status: 'ONLINE',
  },
  {
    id: 'ST-SOIL-04',
    name: 'Tuproq Datchigi №4',
    type: 'SOIL_MOISTURE',
    location: 'Dala №04 (Shix-Ariq)',
    value: '52.1',
    unit: '%',
    temperature: '+23.0 °C',
    battery: 78,
    signal: 'STRONG',
    lastPing: '1 daq oldin',
    status: 'ONLINE',
  },
  {
    id: 'ST-SOIL-09',
    name: 'Tuproq Datchigi №9',
    type: 'SOIL_MOISTURE',
    location: 'Dala №09 (Poliz maydoni)',
    value: '46.0',
    unit: '%',
    temperature: '+24.2 °C',
    battery: 34,
    signal: 'MEDIUM',
    lastPing: '12 daq oldin',
    status: 'WARNING',
  },
  {
    id: 'FL-FLOW-01',
    name: 'Magistral Suv Hisoblagich Posti #1',
    type: 'WATER_FLOW',
    location: 'Yangiariq Bosh Kanal Kirishi',
    value: '14.8',
    unit: 'm³/s',
    battery: 98,
    signal: 'STRONG',
    lastPing: 'Hozir',
    status: 'ONLINE',
  },
  {
    id: 'FL-FLOW-02',
    name: 'Shix-Ariq Suv Taqsimlagich Datchigi',
    type: 'WATER_FLOW',
    location: 'Shix-Ariq Bosh Posti',
    value: '6.2',
    unit: 'm³/s',
    battery: 88,
    signal: 'STRONG',
    lastPing: '5 daq oldin',
    status: 'ONLINE',
  },
  {
    id: 'LV-CANAL-01',
    name: 'Ultratovushli Suv Sathi Oʻlchagich',
    type: 'CANAL_LEVEL',
    location: 'Yangiariq gidrouzel toʻgʻoni',
    value: '1.82',
    unit: 'm',
    battery: 95,
    signal: 'STRONG',
    lastPing: 'Hozir',
    status: 'ONLINE',
  },
]

interface SensorsPageProps {
  lang: SupportedLanguage
}

export const SensorsPage: React.FC<SensorsPageProps> = ({ lang }) => {
  const [sensors, setSensors] = useState<SensorData[]>(INITIAL_SENSORS)
  const [isRefreshing, setIsRefreshing] = useState(false)

  // Simulation: Trigger sensor value randomization
  const handleSimulateRefresh = () => {
    setIsRefreshing(true)
    setTimeout(() => {
      setSensors((prev) =>
        prev.map((s) => {
          if (s.type === 'SOIL_MOISTURE') {
            const current = parseFloat(s.value)
            const delta = (Math.random() - 0.48).toFixed(1)
            const next = Math.max(30, Math.min(95, current + parseFloat(delta))).toFixed(1)
            return { ...s, value: next, lastPing: 'Hozirgina' }
          }
          if (s.type === 'WATER_FLOW') {
            const current = parseFloat(s.value)
            const delta = (Math.random() - 0.5) * 0.4
            const next = Math.max(1, current + delta).toFixed(1)
            return { ...s, value: next, lastPing: 'Hozirgina' }
          }
          return { ...s, lastPing: 'Hozirgina' }
        })
      )
      setIsRefreshing(false)
    }, 600)
  }

  return (
    <div className="space-y-6">
      {/* Title & Simulation Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-foreground flex items-center gap-2">
            <Cpu className="h-6 w-6 text-primary" />
            <span>{lang === 'uz' ? 'IoT Datchiklar Telemetriyasi' : 'Телеметрия IoT-датчиков'}</span>
          </h2>
          <p className="text-xs text-muted-foreground mt-0.5">
            {lang === 'uz'
              ? 'SimulatedSensorProvider arxitekturasi: tuproq namligi va kanal telemetriyasi simulyatsiyasi'
              : 'Архитектура SimulatedSensorProvider: телеметрия влажности почвы и каналов'}
          </p>
        </div>

        <Button
          variant="outline"
          size="sm"
          onClick={handleSimulateRefresh}
          disabled={isRefreshing}
          className="gap-2 shadow-xs"
        >
          <RefreshCw className={`h-4 w-4 ${isRefreshing ? 'animate-spin' : ''}`} />
          <span>{lang === 'uz' ? 'Telemetriyani yangilash' : 'Обновить телеметрию'}</span>
        </Button>
      </div>

      {/* Sensor Metrics Overview */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-xs uppercase text-muted-foreground font-semibold">
              {lang === 'uz' ? 'Datchiklar holati' : 'Статус датчиков'}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex items-center gap-2">
              <span className="text-2xl font-bold text-foreground font-mono">6 / 6</span>
              <Badge variant="outline" className="bg-emerald-500/10 text-emerald-600 border-emerald-500/30 text-xs">
                Faol
              </Badge>
            </div>
            <p className="text-xs text-muted-foreground mt-1">
              {lang === 'uz' ? '5 online, 1 quvvat past' : '5 онлайн, 1 низкий заряд'}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-xs uppercase text-muted-foreground font-semibold">
              {lang === 'uz' ? 'Maʼlumot uzatish protokoli' : 'Протокол передачи'}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-primary font-mono">LoRaWAN / Sim</div>
            <p className="text-xs text-muted-foreground mt-1">
              Yangiariq telemetriya shlyuzi (868 MHz)
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-xs uppercase text-muted-foreground font-semibold">
              {lang === 'uz' ? 'Namlik monitoringi' : 'Мониторинг влажности'}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-emerald-600 font-mono">61.5% oʻrtacha</div>
            <p className="text-xs text-muted-foreground mt-1">
              3 ta chuqurlik qatlami: 10cm, 30cm, 60cm
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Tabs for Sensor Categories */}
      <Tabs defaultValue="all" className="space-y-4">
        <TabsList>
          <TabsTrigger value="all">{lang === 'uz' ? 'Barcha datchiklar' : 'Все датчики'}</TabsTrigger>
          <TabsTrigger value="soil">{lang === 'uz' ? 'Tuproq namligi' : 'Влажность почвы'}</TabsTrigger>
          <TabsTrigger value="water">{lang === 'uz' ? 'Suv oqimi & Kanallar' : 'Расход воды и каналы'}</TabsTrigger>
        </TabsList>

        <TabsContent value="all" className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {sensors.map((sensor) => (
              <SensorCard key={sensor.id} sensor={sensor} lang={lang} />
            ))}
          </div>
        </TabsContent>

        <TabsContent value="soil" className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {sensors
              .filter((s) => s.type === 'SOIL_MOISTURE')
              .map((sensor) => (
                <SensorCard key={sensor.id} sensor={sensor} lang={lang} />
              ))}
          </div>
        </TabsContent>

        <TabsContent value="water" className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {sensors
              .filter((s) => s.type !== 'SOIL_MOISTURE')
              .map((sensor) => (
                <SensorCard key={sensor.id} sensor={sensor} lang={lang} />
              ))}
          </div>
        </TabsContent>
      </Tabs>
    </div>
  )
}

function SensorCard({ sensor, lang }: { sensor: SensorData; lang: SupportedLanguage }) {
  const isSoil = sensor.type === 'SOIL_MOISTURE'
  const isFlow = sensor.type === 'WATER_FLOW'

  return (
    <Card className="hover:border-primary/50 transition-colors shadow-2xs">
      <CardHeader className="pb-3">
        <div className="flex items-start justify-between gap-2">
          <div className="min-w-0">
            <span className="font-mono text-[11px] text-muted-foreground block truncate">
              {sensor.id}
            </span>
            <CardTitle className="text-sm font-bold text-foreground mt-0.5 leading-snug truncate">
              {sensor.name}
            </CardTitle>
          </div>
          <Badge
            variant="outline"
            className={
              sensor.status === 'ONLINE'
                ? 'bg-emerald-500/15 text-emerald-700 border-emerald-500/30 text-[10px]'
                : 'bg-amber-500/15 text-amber-700 border-amber-500/30 text-[10px]'
            }
          >
            {sensor.status}
          </Badge>
        </div>
      </CardHeader>

      <CardContent className="space-y-3">
        {/* Metric Display */}
        <div className="p-3 rounded-xl bg-muted/40 border border-border/60 flex items-center justify-between">
          <div>
            <div className="text-xs text-muted-foreground">
              {isSoil
                ? (lang === 'uz' ? 'Tuproq namligi' : 'Влажность')
                : isFlow
                ? (lang === 'uz' ? 'Oqim tezligi' : 'Расход воды')
                : (lang === 'uz' ? 'Suv sathi' : 'Уровень воды')}
            </div>
            <div className="text-2xl font-bold font-mono text-foreground mt-0.5">
              {sensor.value} <span className="text-sm font-sans text-muted-foreground">{sensor.unit}</span>
            </div>
          </div>

          <div className="h-10 w-10 rounded-xl bg-primary/10 text-primary flex items-center justify-center">
            {isSoil ? (
              <Activity className="h-5 w-5 text-emerald-600" />
            ) : (
              <Droplets className="h-5 w-5 text-hydro-600" />
            )}
          </div>
        </div>

        {/* Location & Extra Stats */}
        <div className="text-xs space-y-1">
          <div className="flex justify-between text-muted-foreground">
            <span>{lang === 'uz' ? 'Joylashuv' : 'Локация'}:</span>
            <span className="font-medium text-foreground truncate max-w-[160px]">{sensor.location}</span>
          </div>

          {sensor.temperature && (
            <div className="flex justify-between text-muted-foreground">
              <span>{lang === 'uz' ? 'Harorat' : 'Температура'}:</span>
              <span className="font-mono text-foreground">{sensor.temperature}</span>
            </div>
          )}
        </div>

        {/* Telemetry Status Bar */}
        <div className="pt-2 border-t border-border/60 flex items-center justify-between text-[11px] text-muted-foreground">
          <div className="flex items-center gap-3">
            <span className="flex items-center gap-1" title="Battery">
              <Battery className={`h-3.5 w-3.5 ${sensor.battery < 40 ? 'text-amber-500' : 'text-emerald-500'}`} />
              <span>{sensor.battery}%</span>
            </span>
            <span className="flex items-center gap-1" title="Signal">
              <Wifi className="h-3.5 w-3.5 text-primary" />
              <span>{sensor.signal}</span>
            </span>
          </div>

          <span className="font-mono">{sensor.lastPing}</span>
        </div>
      </CardContent>
    </Card>
  )
}
