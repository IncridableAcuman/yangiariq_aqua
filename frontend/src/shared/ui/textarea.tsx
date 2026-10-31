import * as React from 'react'
import { cn } from '@/shared/lib/utils'

export interface TextareaProps
  extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?: string
  error?: string
  helperText?: string
}

const Textarea = React.forwardRef<HTMLTextAreaElement, TextareaProps>(
  ({ className, label, error, helperText, id, disabled, ...props }, ref) => {
    const generatedId = React.useId()
    const textareaId = id || (label ? `textarea-${generatedId}` : undefined)

    const textareaElement = (
      <textarea
        id={textareaId}
        disabled={disabled}
        className={cn(
          'flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-1 disabled:cursor-not-allowed disabled:opacity-50 transition-colors',
          error && 'border-destructive focus-visible:ring-destructive',
          className
        )}
        ref={ref}
        {...props}
      />
    )

    if (!label && !error && !helperText) {
      return textareaElement
    }

    return (
      <div className="space-y-1.5 w-full">
        {label && (
          <label
            htmlFor={textareaId}
            className={cn(
              'text-xs font-semibold tracking-wide text-foreground select-none',
              disabled && 'opacity-50 cursor-not-allowed'
            )}
          >
            {label}
          </label>
        )}
        {textareaElement}
        {error && (
          <p className="text-xs font-medium text-destructive animate-in fade-in-50">
            {error}
          </p>
        )}
        {!error && helperText && (
          <p className="text-xs text-muted-foreground">{helperText}</p>
        )}
      </div>
    )
  }
)
Textarea.displayName = 'Textarea'

export { Textarea }
