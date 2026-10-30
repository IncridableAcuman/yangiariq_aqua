import React, { useState } from 'react'
import {
  Sparkles,
  Bot,
  Send,
  Droplets,
  TrendingDown,
  Sun,
  ShieldCheck,
  Clock,
} from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/shared/ui/card'
import { Button } from '@/shared/ui/button'
import { Badge } from '@/shared/ui/badge'
import { Input } from '@/shared/ui/input'
import { SupportedLanguage } from '@/shared/i18n'

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
        aiReply =
          `Savolingiz tahlil qilindi: "${currentInput}". Yangiariq tumani agrometerologik maʼlumotlariga koʻra, tuproq shoʻrlanishini oldini olish uchun navbatdagi sugʻorishda suv sathini normada ushlab turish va datchik koʻrsatkichlarini doimiy kuzatib borish lozim.`
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
      {/* Title */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-foreground flex items-center gap-2">
            <Sparkles className="h-6 w-6 text-primary" />
            <span>{lang === 'uz' ? 'AI Suv Maslahatchisi' : 'ИИ Водный Советник'}</span>
          </h2>
          <p className="text-xs text-muted-foreground mt-0.5">
            {lang === 'uz'
              ? 'Google Gemini API va hududiy agrometeorologik bilimlar asosida optimallashtirish'
              : 'Оптимизация на основе Google Gemini API и локальных агрометеорологических данных'}
          </p>
        </div>

        <Badge variant="outline" className="bg-primary/10 text-primary border-primary/20 gap-1.5 py-1 px-3">
          <ShieldCheck className="h-4 w-4" />
          <span>Gemini-1.5-Pro Engine</span>
        </Badge>
      </div>

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
              {lang === 'uz' ? 'Dala №09: Qurgʻoqchilik xavfi' : 'Поле №09: Риск стресса'}
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            <p className="text-xs text-muted-foreground">
              {lang === 'uz'
                ? 'Tuproq namligi 46% gacha pasaygan. Poliz ekinlarida ildiz stressini oldini olish uchun 12 soat ichida suv berish zarur.'
                : 'Влажность упала до 46%. Требуется полив бахчевых культур в течение 12 часов.'}
            </p>
            <div className="text-xs font-semibold text-amber-600">
              Kutilayotgan suv hajmi: 600 m³
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <div className="flex items-center justify-between">
              <Badge variant="outline" className="bg-sky-500/10 text-sky-600 border-sky-500/30 text-[10px]">
                Barqaror
              </Badge>
              <Droplets className="h-4 w-4 text-hydro-600" />
            </div>
            <CardTitle className="text-sm font-bold mt-1">
              {lang === 'uz' ? 'Yangiariq gidro-balansi' : 'Гидробаланс Янгиарыка'}
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            <p className="text-xs text-muted-foreground">
              {lang === 'uz'
                ? 'Magistral kanaldan suv oqimi 14.8 m³/s bilan taʼminlangan. Tuman boʻyicha taqsimot meʼyordan 4% yuqori.'
                : 'Расход в канале 14.8 м³/с. Распределение по району выше нормы на 4%.'}
            </p>
            <div className="text-xs font-semibold text-primary">
              Tizim holati: Barqaror
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Interactive AI Chat Box */}
      <Card className="flex flex-col h-[460px] border-border shadow-xs">
        <CardHeader className="py-3 px-4 border-b border-border flex flex-row items-center justify-between space-y-0">
          <div className="flex items-center gap-2.5">
            <div className="h-8 w-8 rounded-lg bg-primary/10 text-primary flex items-center justify-center">
              <Bot className="h-4 w-4" />
            </div>
            <div>
              <CardTitle className="text-sm font-bold">Aqua AI Maslahat Darchasi</CardTitle>
              <CardDescription className="text-[11px]">
                Yangiariq tumani agronomik maʼlumotlari bilan bogʻlangan
              </CardDescription>
            </div>
          </div>
          <span className="text-[11px] font-mono text-emerald-600 bg-emerald-500/10 px-2 py-0.5 rounded-full">
            Onlayn
          </span>
        </CardHeader>

        {/* Chat message list */}
        <CardContent className="flex-1 overflow-y-auto p-4 space-y-3">
          {messages.map((msg) => (
            <div
              key={msg.id}
              className={`flex items-start gap-2.5 ${
                msg.sender === 'user' ? 'justify-end' : 'justify-start'
              }`}
            >
              {msg.sender === 'ai' && (
                <div className="h-7 w-7 rounded-lg bg-primary/10 text-primary flex items-center justify-center shrink-0 mt-0.5">
                  <Bot className="h-4 w-4" />
                </div>
              )}
              <div
                className={`max-w-xl rounded-2xl px-4 py-2.5 text-xs sm:text-sm leading-relaxed ${
                  msg.sender === 'user'
                    ? 'bg-primary text-primary-foreground font-medium rounded-tr-none'
                    : 'bg-muted text-foreground border border-border/60 rounded-tl-none'
                }`}
              >
                <p>{msg.text}</p>
                <span
                  className={`text-[10px] mt-1 block text-right ${
                    msg.sender === 'user' ? 'text-primary-foreground/70' : 'text-muted-foreground'
                  }`}
                >
                  {msg.timestamp}
                </span>
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
  )
}
