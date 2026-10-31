import React from 'react'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Button } from '@/shared/ui/button'
import { SupportedLanguage, translations } from '@/shared/i18n'

interface FieldPaginationProps {
  page: number
  size: number
  totalElements: number
  totalPages: number
  onPageChange: (newPage: number) => void
  lang: SupportedLanguage
}

export const FieldPagination: React.FC<FieldPaginationProps> = ({
  page,
  size,
  totalElements,
  totalPages,
  onPageChange,
  lang,
}) => {
  const t = translations[lang]?.fields || translations.uz.fields

  if (totalElements === 0 || totalPages <= 1) {
    return null
  }

  const from = page * size + 1
  const to = Math.min((page + 1) * size, totalElements)

  // Build page number range (max 5 page buttons)
  const getPageNumbers = () => {
    const pages: number[] = []
    const start = Math.max(0, page - 2)
    const end = Math.min(totalPages - 1, start + 4)

    for (let i = start; i <= end; i++) {
      pages.push(i)
    }
    return pages
  }

  const pageNumbers = getPageNumbers()

  return (
    <div className="flex flex-col sm:flex-row items-center justify-between gap-3 pt-4 border-t border-border/60 text-xs">
      {/* Showing range text */}
      <div className="text-muted-foreground select-none">
        {t.pagination.showing
          .replace('{from}', from.toString())
          .replace('{to}', to.toString())
          .replace('{total}', totalElements.toString())}
      </div>

      {/* Page navigation buttons */}
      <div className="flex items-center gap-1">
        {/* Previous Button */}
        <Button
          variant="outline"
          size="sm"
          onClick={() => onPageChange(page - 1)}
          disabled={page <= 0}
          className="h-8 px-2.5 gap-1 text-xs"
          aria-label={t.pagination.previous}
        >
          <ChevronLeft className="h-3.5 w-3.5" />
          <span className="hidden sm:inline">{t.pagination.previous}</span>
        </Button>

        {/* Page Buttons */}
        <div className="flex items-center gap-1">
          {pageNumbers.map((p) => {
            const isCurrent = p === page
            return (
              <Button
                key={p}
                variant={isCurrent ? 'default' : 'outline'}
                size="sm"
                onClick={() => onPageChange(p)}
                className="h-8 w-8 p-0 text-xs"
                aria-current={isCurrent ? 'page' : undefined}
              >
                {p + 1}
              </Button>
            )
          })}
        </div>

        {/* Next Button */}
        <Button
          variant="outline"
          size="sm"
          onClick={() => onPageChange(page + 1)}
          disabled={page >= totalPages - 1}
          className="h-8 px-2.5 gap-1 text-xs"
          aria-label={t.pagination.next}
        >
          <span className="hidden sm:inline">{t.pagination.next}</span>
          <ChevronRight className="h-3.5 w-3.5" />
        </Button>
      </div>
    </div>
  )
}
