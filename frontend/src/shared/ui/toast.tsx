import React, { createContext, useContext, useState, useCallback } from 'react'
import { CheckCircle2, AlertCircle, Info, X } from 'lucide-react'
import { cn } from '@/shared/lib/utils'

export type ToastVariant = 'default' | 'success' | 'destructive' | 'info'

export interface ToastItem {
  id: string
  title?: string
  description: string
  variant?: ToastVariant
}

interface ToastContextType {
  toast: (options: Omit<ToastItem, 'id'>) => void
  success: (description: string, title?: string) => void
  error: (description: string, title?: string) => void
  info: (description: string, title?: string) => void
  dismiss: (id: string) => void
}

const ToastContext = createContext<ToastContextType | undefined>(undefined)

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<ToastItem[]>([])

  const dismiss = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
  }, [])

  const toast = useCallback(
    ({ title, description, variant = 'default' }: Omit<ToastItem, 'id'>) => {
      const id = `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
      const newToast: ToastItem = { id, title, description, variant }

      setToasts((prev) => [...prev, newToast])

      setTimeout(() => {
        dismiss(id)
      }, 4500)
    },
    [dismiss]
  )

  const success = useCallback(
    (description: string, title?: string) => {
      toast({ title, description, variant: 'success' })
    },
    [toast]
  )

  const error = useCallback(
    (description: string, title?: string) => {
      toast({ title, description, variant: 'destructive' })
    },
    [toast]
  )

  const info = useCallback(
    (description: string, title?: string) => {
      toast({ title, description, variant: 'info' })
    },
    [toast]
  )

  return (
    <ToastContext.Provider value={{ toast, success, error, info, dismiss }}>
      {children}
      {/* Toast container in bottom right */}
      <div
        role="region"
        aria-live="polite"
        aria-label="Notifications"
        className="fixed bottom-4 right-4 z-50 flex flex-col gap-2 max-w-sm w-full pointer-events-none px-4 sm:px-0"
      >
        {toasts.map((item) => {
          const isSuccess = item.variant === 'success'
          const isError = item.variant === 'destructive'
          const isInfo = item.variant === 'info'

          return (
            <div
              key={item.id}
              className={cn(
                'pointer-events-auto flex items-start gap-3 p-3.5 rounded-lg border shadow-lg text-sm transition-all duration-200 animate-in slide-in-from-bottom-5 fade-in-50',
                isSuccess && 'bg-emerald-50 dark:bg-emerald-950/80 border-emerald-200 dark:border-emerald-800 text-emerald-900 dark:text-emerald-100',
                isError && 'bg-red-50 dark:bg-red-950/80 border-red-200 dark:border-red-800 text-red-900 dark:text-red-100',
                isInfo && 'bg-blue-50 dark:bg-blue-950/80 border-blue-200 dark:border-blue-800 text-blue-900 dark:text-blue-100',
                !isSuccess && !isError && !isInfo && 'bg-card border-border text-card-foreground'
              )}
            >
              <div className="shrink-0 mt-0.5">
                {isSuccess && <CheckCircle2 className="h-4 w-4 text-emerald-600 dark:text-emerald-400" />}
                {isError && <AlertCircle className="h-4 w-4 text-red-600 dark:text-red-400" />}
                {isInfo && <Info className="h-4 w-4 text-blue-600 dark:text-blue-400" />}
              </div>

              <div className="flex-1 min-w-0">
                {item.title && <p className="font-semibold text-xs leading-none mb-1">{item.title}</p>}
                <p className="text-xs leading-relaxed opacity-90 break-words">{item.description}</p>
              </div>

              <button
                type="button"
                onClick={() => dismiss(item.id)}
                className="shrink-0 rounded p-1 opacity-70 hover:opacity-100 transition-opacity"
                aria-label="Close notification"
              >
                <X className="h-3.5 w-3.5" />
              </button>
            </div>
          )
        })}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast(): ToastContextType {
  const context = useContext(ToastContext)
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider')
  }
  return context
}
