import React, { useState, useMemo } from 'react'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { useAuth } from '@/features/auth/context/AuthContext'
import { useToast } from '@/shared/ui/toast'
import { ApiError } from '@/shared/api/client'
import {
  Field,
  FieldQueryParams,
  CreateFieldPayload,
  UpdateFieldPayload,
} from '@/entities/field'
import {
  useFieldsQuery,
  useCreateFieldMutation,
  useUpdateFieldMutation,
  useDeactivateFieldMutation,
  useReactivateFieldMutation,
  FieldToolbar,
  FieldFilters,
  FieldTable,
  FieldCard,
  FieldPagination,
  FieldFormDialog,
  FieldDetailsModal,
  FieldDeactivateDialog,
  FieldReactivateDialog,
  FieldEmptyState,
} from '@/features/field'

interface FieldsPageProps {
  lang: SupportedLanguage
}

export const FieldsPage: React.FC<FieldsPageProps> = ({ lang }) => {
  const { user } = useAuth()
  const toast = useToast()
  const t = translations[lang]?.fields || translations.uz.fields

  // Query & filter states
  const [searchQuery, setSearchQuery] = useState('')
  const [filters, setFilters] = useState<FieldQueryParams>({
    page: 0,
    size: 10,
    sort: 'createdAt,desc',
  })

  // Dialog states
  const [formOpen, setFormOpen] = useState(false)
  const [fieldToEdit, setFieldToEdit] = useState<Field | null>(null)
  const [fieldToView, setFieldToView] = useState<Field | null>(null)
  const [fieldToDeactivate, setFieldToDeactivate] = useState<Field | null>(null)
  const [fieldToReactivate, setFieldToReactivate] = useState<Field | null>(null)

  // Server state via TanStack Query
  const { data, isLoading, error } = useFieldsQuery(filters)

  // Mutations
  const createMutation = useCreateFieldMutation()
  const updateMutation = useUpdateFieldMutation()
  const deactivateMutation = useDeactivateFieldMutation()
  const reactivateMutation = useReactivateFieldMutation()

  // Extract content
  const allFields = data?.content || []

  // Filter fields locally by search query if user typed in search bar
  const displayedFields = useMemo(() => {
    if (!searchQuery.trim()) return allFields
    const q = searchQuery.toLowerCase().trim()
    return allFields.filter(
      (f) =>
        f.name.toLowerCase().includes(q) ||
        (f.description && f.description.toLowerCase().includes(q))
    )
  }, [allFields, searchQuery])

  // Aggregate stats
  const stats = useMemo(() => {
    const total = data?.totalElements ?? allFields.length
    const active = allFields.filter((f) => f.status === 'ACTIVE').length
    const inactive = allFields.filter((f) => f.status === 'INACTIVE').length
    const totalArea = allFields.reduce(
      (sum, f) => sum + (Number(f.areaHectares) || 0),
      0
    )

    return {
      total,
      active,
      inactive,
      totalArea,
    }
  }, [data, allFields])

  // Handle Filter Changes
  const handleFilterChange = (newFilters: Partial<FieldQueryParams>) => {
    setFilters((prev) => ({
      ...prev,
      ...newFilters,
      page: 0, // Reset to first page upon filter change
    }))
  }

  const handleResetFilters = () => {
    setSearchQuery('')
    setFilters({
      page: 0,
      size: 10,
      sort: 'createdAt,desc',
    })
  }

  // Dialog Handlers
  const handleOpenCreate = () => {
    setFieldToEdit(null)
    setFormOpen(true)
  }

  const handleOpenEdit = (field: Field) => {
    setFieldToEdit(field)
    setFormOpen(true)
  }

  const handleFormSubmit = async (
    payload: CreateFieldPayload | UpdateFieldPayload
  ) => {
    try {
      if (fieldToEdit) {
        await updateMutation.mutateAsync({
          id: fieldToEdit.id,
          payload: payload as UpdateFieldPayload,
        })
        toast.success(t.messages.updateSuccess)
      } else {
        await createMutation.mutateAsync(payload as CreateFieldPayload)
        toast.success(t.messages.createSuccess)
      }
      setFormOpen(false)
      setFieldToEdit(null)
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        if (err.status === 409) {
          toast.error(t.messages.nameConflict)
          return
        }
        if (err.status === 403) {
          toast.error(t.messages.unauthorized)
          return
        }
        if (err.status === 404) {
          toast.error(t.messages.notFound)
          return
        }
        toast.error(err.message || t.messages.genericError)
      } else {
        toast.error(t.messages.genericError)
      }
    }
  }

  const handleConfirmDeactivate = async () => {
    if (!fieldToDeactivate) return
    try {
      await deactivateMutation.mutateAsync(fieldToDeactivate.id)
      toast.success(t.messages.deactivateSuccess)
      setFieldToDeactivate(null)
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        if (err.status === 403) {
          toast.error(t.messages.unauthorized)
          return
        }
        if (err.status === 404) {
          toast.error(t.messages.notFound)
          return
        }
        toast.error(err.message || t.messages.genericError)
      } else {
        toast.error(t.messages.genericError)
      }
    }
  }

  const handleConfirmReactivate = async () => {
    if (!fieldToReactivate) return
    try {
      await reactivateMutation.mutateAsync(fieldToReactivate.id)
      toast.success(t.messages.reactivateSuccess)
      setFieldToReactivate(null)
    } catch (err: unknown) {
      if (err instanceof ApiError) {
        if (err.status === 409) {
          toast.error(t.messages.nameConflict)
          return
        }
        if (err.status === 403) {
          toast.error(t.messages.unauthorized)
          return
        }
        if (err.status === 404) {
          toast.error(t.messages.notFound)
          return
        }
        toast.error(err.message || t.messages.genericError)
      } else {
        toast.error(t.messages.genericError)
      }
    }
  }

  const hasFilterActive = Boolean(
    filters.status ||
    filters.cropType ||
    filters.soilType ||
    filters.irrigationMethod ||
    searchQuery.trim()
  )

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-border/60 pb-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">
            {t.title}
          </h1>
          <p className="text-xs text-muted-foreground mt-0.5">
            {t.subtitle}
          </p>
        </div>
      </div>

      {/* Toolbar with Metrics & Search */}
      <FieldToolbar
        lang={lang}
        userRole={user?.role}
        searchQuery={searchQuery}
        onSearchChange={setSearchQuery}
        onOpenCreateDialog={handleOpenCreate}
        stats={stats}
      />

      {/* Filter Bar */}
      <FieldFilters
        lang={lang}
        filters={filters}
        onFilterChange={handleFilterChange}
        onResetFilters={handleResetFilters}
      />

      {/* Error Banner */}
      {error && (
        <div className="rounded-lg border border-destructive/40 bg-destructive/10 p-4 text-xs text-destructive">
          <p className="font-semibold">
            {translations[lang]?.common?.error || 'Xatolik yuz berdi'}:
          </p>
          <p className="mt-0.5 opacity-90">{error.message}</p>
        </div>
      )}

      {/* Main Content: Table on Desktop, Cards on Mobile */}
      {!isLoading && displayedFields.length === 0 ? (
        <FieldEmptyState
          isFiltered={hasFilterActive}
          lang={lang}
          userRole={user?.role}
          onOpenCreateDialog={handleOpenCreate}
          onClearFilters={handleResetFilters}
        />
      ) : (
        <>
          {/* Desktop Table View */}
          <div className="hidden lg:block">
            <FieldTable
              fields={displayedFields}
              isLoading={isLoading}
              lang={lang}
              userRole={user?.role}
              onView={(f) => setFieldToView(f)}
              onEdit={handleOpenEdit}
              onDeactivate={(f) => setFieldToDeactivate(f)}
              onReactivate={(f) => setFieldToReactivate(f)}
            />
          </div>

          {/* Mobile & Tablet Card Grid */}
          <div className="lg:hidden grid grid-cols-1 sm:grid-cols-2 gap-3">
            {displayedFields.map((field) => (
              <FieldCard
                key={field.id}
                field={field}
                lang={lang}
                userRole={user?.role}
                onView={(f) => setFieldToView(f)}
                onEdit={handleOpenEdit}
                onDeactivate={(f) => setFieldToDeactivate(f)}
                onReactivate={(f) => setFieldToReactivate(f)}
              />
            ))}
          </div>

          {/* Pagination */}
          <FieldPagination
            page={filters.page || 0}
            size={filters.size || 10}
            totalElements={data?.totalElements || displayedFields.length}
            totalPages={data?.totalPages || 1}
            onPageChange={(p) => setFilters((prev) => ({ ...prev, page: p }))}
            lang={lang}
          />
        </>
      )}

      {/* Modals & Dialogs */}
      <FieldFormDialog
        isOpen={formOpen}
        onClose={() => {
          setFormOpen(false)
          setFieldToEdit(null)
        }}
        fieldToEdit={fieldToEdit}
        onSubmit={handleFormSubmit}
        isSubmitting={createMutation.isPending || updateMutation.isPending}
        lang={lang}
      />

      <FieldDetailsModal
        field={fieldToView}
        isOpen={Boolean(fieldToView)}
        onClose={() => setFieldToView(null)}
        lang={lang}
        userRole={user?.role}
        onEdit={handleOpenEdit}
        onDeactivate={(f) => setFieldToDeactivate(f)}
        onReactivate={(f) => setFieldToReactivate(f)}
      />

      <FieldDeactivateDialog
        field={fieldToDeactivate}
        isOpen={Boolean(fieldToDeactivate)}
        onClose={() => setFieldToDeactivate(null)}
        onConfirm={handleConfirmDeactivate}
        isSubmitting={deactivateMutation.isPending}
        lang={lang}
      />

      <FieldReactivateDialog
        field={fieldToReactivate}
        isOpen={Boolean(fieldToReactivate)}
        onClose={() => setFieldToReactivate(null)}
        onConfirm={handleConfirmReactivate}
        isSubmitting={reactivateMutation.isPending}
        lang={lang}
      />
    </div>
  )
}
