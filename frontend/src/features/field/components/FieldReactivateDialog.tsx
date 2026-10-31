import React from 'react'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { RotateCcw } from 'lucide-react'
import { Field } from '@/entities/field'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface FieldReactivateDialogProps {
  field: Field | null
  isOpen: boolean
  onClose: () => void
  onConfirm: () => Promise<void>
  isSubmitting: boolean
  lang: SupportedLanguage
}

export const FieldReactivateDialog: React.FC<FieldReactivateDialogProps> = ({
  field,
  isOpen,
  onClose,
  onConfirm,
  isSubmitting,
  lang,
}) => {
  const t = translations[lang]?.fields || translations.uz.fields

  if (!field) return null

  return (
    <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader className="space-y-2">
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 rounded-full bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 flex items-center justify-center shrink-0">
              <RotateCcw className="h-5 w-5" />
            </div>
            <div>
              <DialogTitle className="text-base font-bold text-foreground">
                {t.reactivateDialog.title}
              </DialogTitle>
              <p className="text-xs font-semibold text-primary mt-0.5">
                {field.name}
              </p>
            </div>
          </div>
          <DialogDescription className="text-xs text-muted-foreground leading-relaxed pt-2">
            {t.reactivateDialog.description}
          </DialogDescription>
        </DialogHeader>

        <DialogFooter className="pt-4 border-t border-border/60 gap-2">
          <Button
            type="button"
            variant="outline"
            onClick={onClose}
            disabled={isSubmitting}
            className="text-xs"
          >
            {translations[lang]?.common?.cancel || 'Bekor qilish'}
          </Button>
          <Button
            type="button"
            onClick={onConfirm}
            isLoading={isSubmitting}
            disabled={isSubmitting}
            className="text-xs bg-emerald-600 hover:bg-emerald-700 text-white"
          >
            {isSubmitting ? t.reactivateDialog.activating : t.reactivateDialog.confirm}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
