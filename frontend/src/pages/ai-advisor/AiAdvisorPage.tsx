import React, { useState } from 'react'
import {
  Bot,
  Send,
  Droplets,
  TrendingDown,
  Sun,
  ShieldCheck,
  Clock,
  Lightbulb,
  MessageSquare,
} from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { Input } from '@/shared/ui/input'
import { Tabs, TabsList, TabsTrigger } from '@/shared/ui/tabs'
import { SupportedLanguage } from '@/shared/i18n'
import { RecommendationsPage } from '@/pages/recommendations'

interface Message {
  id: string
  sender: 'ai' | 'user'
  text: string
  timestamp: string
}

const INITIAL_MESSAGES: Message[] = [
  {
    id: 'm-1',
    sender: 'ai',
    text: 'Assalomu alaykum! Men Yangiariq Aqua AI - aqlli agrosuv maslahatchisiman. Tumandagi datchiklar, ob-havo prognozi va kanallar oqimi asosida qaysi dalani qachon va qancha sugʻorish boʻyicha eng maqbul tavsiyalarni bera olaman. Sizga qanday yordam bera olaman?',
    timestamp: '10:00',
  },
]

interface AiAdvisorPageProps {
  lang: SupportedLanguage
}

export const AiAdvisorPage: React.FC<AiAdvisorPageProps> = ({ lang }) => {
  const [activeTab, setActiveTab] = useState<'recommendations' | 'chat'>('recommendations')
  const [messages, setMessages] = useState<Message[]>(INITIAL_MESSAGES)
  const [inputText, setInputText] = useState('')
  const [isTyping, setIsTyping] = useState(false)

  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault()
    if (!inputText.trim()) return

    const userMsg: Message = {
      id: `u-${Date.now()}`,
      sender: 'user',
      text: inputText.trim(),
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    }

    setMessages((prev) => [...prev, userMsg])
    const currentInput = inputText.trim()
    setInputText('')
    setIsTyping(true)

    // Simulate Gemini AI Response
    setTimeout(() => {
      let aiReply = ''
      if (currentInput.toLowerCase().includes('paxta') || currentInput.toLowerCase().includes('dala')) {
        aiReply =
          'Dala №01 va №07 da paxta ekinining tuproq namligi hozir 68% atrofida. Yaqin 48 soatda havo harorati +33°C boʻlishi kutilmoqda. Bugʻlanishni oldini olish uchun sugʻorishni tungi 22:00 dan 04:00 gacha amalga oshirishni tavsiya qilaman. Bu suv sarfini 22% ga qisqartiradi.'
      } else if (currentInput.toLowerCase().includes('suv') || currentInput.toLowerCase().includes('kanal')) {
        aiReply =
          'Yangiariq Bosh kanalida suv sarfi barqaror (14.8 m³/s). Bugungi limit boʻyicha sizning fermer xoʻjaligingizga 1,200 m³ suv ajratish imkoniyati mavjud. Sugʻorish soʻrovini sugʻorish jurnalidan tasdiqlashingiz mumkin.'
      } else {
        aiReply = `Savolingiz tahlil qilindi: "${currentInput}". Yangiariq tumani agrometerologik maʼlumotlariga koʻra, tuproq shoʻrlanishini oldini olish uchun navbatdagi sugʻorishda suv sathini normada ushlab turish va datchik koʻrsatkichlarini doimiy kuzatib borish lozim.`
      }

      const aiMsg: Message = {
        id: `ai-${Date.now()}`,
        sender: 'ai',
        text: aiReply,
        timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      }

      setMessages((prev) => [...prev, aiMsg])
      setIsTyping(false)
    }, 900)
  }

  return (
    <div className="space-y-6">
      {/* Top Tabs Switcher */}
      <div className="flex items-center justify-between border-b pb-3">
        <Tabs value={activeTab} onValueChange={(val) => setActiveTab(val as 'recommendations' | 'chat')}>
          <TabsList className="grid grid-cols-2 w-80 h-9">
            <TabsTrigger value="recommendations" className="text-xs gap-1.5">
              <Lightbulb className="w-3.5 h-3.5" />
              <span>
                {lang === 'uz'
                  ? 'Tavsiyalar va Qarorlar'
                  : lang === 'ru'
                  ? 'Рекомендации и Решения'
                  : 'Recommendations & Decisions'}
              </span>
            </TabsTrigger>
            <TabsTrigger value="chat" className="text-xs gap-1.5">
              <MessageSquare className="w-3.5 h-3.5" />
              <span>
                {lang === 'uz'
                  ? 'Interaktiv Maslahatchi'
                  : lang === 'ru'
                  ? 'Интерактивный Чат'
                  : 'Interactive Chat'}
              </span>
            </TabsTrigger>
          </TabsList>
        </Tabs>

        <Badge
          variant="outline"
          className="bg-primary/10 text-primary border-primary/20 gap-1.5 py-1 px-3 hidden sm:flex"
        >
          <ShieldCheck className="h-4 w-4" />
          <span>Gemini & Hybrid Engine</span>
        </Badge>
      </div>

      {activeTab === 'recommendations' ? (
        <RecommendationsPage lang={lang} />
      ) : (
        <div className="space-y-6">
          {/* Advisory Insight Cards */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <Card className="border-primary/30 bg-gradient-to-br from-primary/5 to-card">
              <CardHeader className="pb-2">
                <div className="flex items-center justify-between">
                  <Badge variant="outline" className="bg-emerald-500/10 text-emerald-600 border-emerald-500/30 text-[10px]">
                    Yuqori Samara
                  </Badge>
                  <Sun className="h-4 w-4 text-amber-500" />
                </div>
                <CardTitle className="text-sm font-bold mt-1">
                  {lang === 'uz' ? 'Tungi sugʻorish strategiyasi' : 'Стратегия ночного полива'}
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-2">
                <p className="text-xs text-muted-foreground">
                  {lang === 'uz'
                    ? 'Kunduzgi bugʻlanish koeffitsienti 1.8x yuqori. Tungi soat 22:00 dan 05:00 gacha sugʻorish tavsiya qilinadi.'
                    : 'Дневное испарение выше в 1.8 раза. Рекомендуется ночной полив с 22:00 до 05:00.'}
                </p>
                <div className="text-xs font-semibold text-emerald-600 flex items-center gap-1">
                  <TrendingDown className="h-3.5 w-3.5" />
                  <span>-22% suv isrofi tejaladi</span>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-2">
                <div className="flex items-center justify-between">
                  <Badge variant="outline" className="bg-amber-500/10 text-amber-600 border-amber-500/30 text-[10px]">
                    Diqqat
                  </Badge>
                  <Clock className="h-4 w-4 text-primary" />
                </div>
                <CardTitle className="text-sm font-bold mt-1">
                  {lang === 'uz' ? 'Bugʻlanish monitoringi' : 'Мониторинг испарения'}
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-2">
                <p className="text-xs text-muted-foreground">
                  {lang === 'uz'
                    ? 'Yozgi quruq havo (garmsel) taʼsirida tuproq namligi odatdagidan tez pasayishi mumkin.'
                    : 'В период суховея влажность почвы снижается быстрее нормы.'}
                </p>
                <div className="text-xs font-semibold text-amber-600 flex items-center gap-1">
                  <Droplets className="h-3.5 w-3.5" />
                  <span>Datchiklar nazoratda</span>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-2">
                <div className="flex items-center justify-between">
                  <Badge variant="outline" className="bg-primary/10 text-primary border-primary/30 text-[10px]">
                    Limit Nazorati
                  </Badge>
                  <Droplets className="h-4 w-4 text-primary" />
                </div>
                <CardTitle className="text-sm font-bold mt-1">
                  {lang === 'uz' ? 'Kanal suvi taqsimoti' : 'Распределение по каналам'}
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-2">
                <p className="text-xs text-muted-foreground">
                  {lang === 'uz'
                    ? 'Yangiariq tuman magistral kanallaridagi suv oqimi normativ talablarga mos kelmoqda.'
                    : 'Расход воды в магистральных каналах соответствует нормам.'}
                </p>
                <div className="text-xs font-semibold text-primary flex items-center gap-1">
                  <ShieldCheck className="h-3.5 w-3.5" />
                  <span>Barqaror taqsimot</span>
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Interactive Chat Card */}
          <Card className="h-[480px] flex flex-col">
            <CardHeader className="border-b py-3 px-4">
              <div className="flex items-center gap-2">
                <Bot className="h-5 w-5 text-primary" />
                <CardTitle className="text-sm">
                  {lang === 'uz' ? 'AI bilan toʻgʻridan-toʻgʻri muloqot' : 'Диалог с ИИ-ассистентом'}
                </CardTitle>
              </div>
            </CardHeader>

            <CardContent className="flex-1 overflow-y-auto p-4 space-y-3">
              {messages.map((msg) => (
                <div
                  key={msg.id}
                  className={`flex ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}
                >
                  <div
                    className={`max-w-[85%] rounded-2xl px-3.5 py-2.5 text-xs shadow-sm ${
                      msg.sender === 'user'
                        ? 'bg-primary text-primary-foreground rounded-br-none'
                        : 'bg-muted text-foreground border border-border/60 rounded-bl-none'
                    }`}
                  >
                    <p className="leading-relaxed">{msg.text}</p>
                    <p
                      className={`text-[9px] mt-1 ${
                        msg.sender === 'user'
                          ? 'text-primary-foreground/75 text-right'
                          : 'text-muted-foreground'
                      }`}
                    >
                      {msg.timestamp}
                    </p>
                  </div>
                </div>
              ))}

              {isTyping && (
                <div className="flex items-center gap-2 text-xs text-muted-foreground">
                  <div className="h-7 w-7 rounded-lg bg-primary/10 text-primary flex items-center justify-center shrink-0">
                    <Bot className="h-4 w-4 animate-spin" />
                  </div>
                  <div className="bg-muted px-3 py-2 rounded-xl text-xs border border-border/60">
                    Gemini tavsiya ishlab chiqmoqda...
                  </div>
                </div>
              )}
            </CardContent>

            {/* Input area */}
            <form
              onSubmit={handleSendMessage}
              className="p-3 border-t border-border flex items-center gap-2 bg-card/60"
            >
              <Input
                placeholder={
                  lang === 'uz'
                    ? 'Savol bering (masalan: Paxta maydonini qachon sugʻorish kerak?)...'
                    : 'Задайте вопрос по поливу...'
                }
                value={inputText}
                onChange={(e) => setInputText(e.target.value)}
                className="flex-1"
              />
              <Button type="submit" size="sm" disabled={!inputText.trim() || isTyping}>
                <Send className="h-4 w-4 mr-1" />
                <span>{lang === 'uz' ? 'Yuborish' : 'Отправить'}</span>
              </Button>
            </form>
          </Card>
        </div>
      )}
    </div>
  )
}
