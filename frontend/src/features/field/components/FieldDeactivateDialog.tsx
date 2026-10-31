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
import { AlertTriangle } from 'lucide-react'
import { Field } from '@/entities/field'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface FieldDeactivateDialogProps {
  field: Field | null
  isOpen: boolean
  onClose: () => void
  onConfirm: () => Promise<void>
  isSubmitting: boolean
  lang: SupportedLanguage
}

export const FieldDeactivateDialog: React.FC<FieldDeactivateDialogProps> = ({
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
            <div className="h-10 w-10 rounded-full bg-amber-500/15 text-amber-600 dark:text-amber-400 flex items-center justify-center shrink-0">
              <AlertTriangle className="h-5 w-5" />
            </div>
            <div>
              <DialogTitle className="text-base font-bold text-foreground">
                {t.deactivateDialog.title}
              </DialogTitle>
              <p className="text-xs font-semibold text-primary mt-0.5">
                {field.name}
              </p>
            </div>
          </div>
          <DialogDescription className="text-xs text-muted-foreground leading-relaxed pt-2">
            {t.deactivateDialog.description}
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
            variant="destructive"
            onClick={onConfirm}
            isLoading={isSubmitting}
            disabled={isSubmitting}
            className="text-xs bg-amber-600 hover:bg-amber-700 text-white"
          >
            {isSubmitting ? t.deactivateDialog.canceling : t.deactivateDialog.confirm}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
