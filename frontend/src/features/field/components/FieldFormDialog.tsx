import React, { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { Input } from '@/shared/ui/input'
import { Textarea } from '@/shared/ui/textarea'
import { MapPin, Pentagon, Trash2, Edit3, Plus } from 'lucide-react'
import { fieldFormSchema, FieldFormData } from '../schemas/fieldSchema'
import { Field, FieldBoundary, CreateFieldPayload, UpdateFieldPayload } from '@/entities/field'
import { SupportedLanguage, translations } from '@/shared/i18n'
import { FieldLocationPickerModal } from '../gis/FieldLocationPickerModal'
import { FieldBoundaryEditorModal } from '../gis/FieldBoundaryEditorModal'
import { calculatePolygonAreaHectares } from '../gis/gis.utils'

interface FieldFormDialogProps {
  isOpen: boolean
  onClose: () => void
  fieldToEdit?: Field | null
  onSubmit: (payload: CreateFieldPayload | UpdateFieldPayload) => Promise<void>
  isSubmitting: boolean
  lang: SupportedLanguage
}

export const FieldFormDialog: React.FC<FieldFormDialogProps> = ({
  isOpen,
  onClose,
  fieldToEdit,
  onSubmit,
  isSubmitting,
  lang,
}) => {
  const t = translations[lang]?.fields || translations.uz.fields
  const isEditMode = Boolean(fieldToEdit)

  // Geospatial states
  const [boundary, setBoundary] = useState<FieldBoundary | null>(null)
  const [locationPickerOpen, setLocationPickerOpen] = useState(false)
  const [boundaryEditorOpen, setBoundaryEditorOpen] = useState(false)

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors },
  } = useForm<FieldFormData>({
    resolver: zodResolver(fieldFormSchema),
    mode: 'onTouched',
    defaultValues: {
      name: '',
      description: '',
      areaHectares: 10,
      cropType: 'COTTON',
      soilType: 'LOAM',
      irrigationMethod: 'CANAL',
      latitude: undefined,
      longitude: undefined,
    },
  })

  const currentLat = watch('latitude')
  const currentLng = watch('longitude')
  const currentArea = watch('areaHectares')

  // Synchronize form when opened or fieldToEdit changes
  useEffect(() => {
    if (isOpen) {
      if (fieldToEdit) {
        reset({
          name: fieldToEdit.name,
          description: fieldToEdit.description || '',
          areaHectares: Number(fieldToEdit.areaHectares),
          cropType: fieldToEdit.cropType,
          soilType: fieldToEdit.soilType,
          irrigationMethod: fieldToEdit.irrigationMethod,
          latitude: fieldToEdit.latitude ? Number(fieldToEdit.latitude) : undefined,
          longitude: fieldToEdit.longitude ? Number(fieldToEdit.longitude) : undefined,
        })
        setBoundary(fieldToEdit.boundary || null)
      } else {
        reset({
          name: '',
          description: '',
          areaHectares: 10,
          cropType: 'COTTON',
          soilType: 'LOAM',
          irrigationMethod: 'CANAL',
          latitude: undefined,
          longitude: undefined,
        })
        setBoundary(null)
      }
    }
  }, [isOpen, fieldToEdit, reset])

  const onFormSubmit = async (data: FieldFormData) => {
    const payload = {
      name: data.name.trim(),
      description: data.description?.trim() || null,
      areaHectares: data.areaHectares,
      cropType: data.cropType,
      soilType: data.soilType,
      irrigationMethod: data.irrigationMethod,
      latitude: data.latitude ?? null,
      longitude: data.longitude ?? null,
      boundary: boundary ?? null,
    }

    await onSubmit(payload)
  }

  // Location picker callback
  const handleLocationSelected = (lat: number, lng: number) => {
    setValue('latitude', Number(lat.toFixed(6)), { shouldValidate: true })
    setValue('longitude', Number(lng.toFixed(6)), { shouldValidate: true })
  }

  // Boundary editor callback
  const handleBoundarySaved = (newBoundary: FieldBoundary | null) => {
    setBoundary(newBoundary)
  }

  // Helper to translate validation error keys
  const getErrorMessage = (errorKey?: string) => {
    if (!errorKey) return undefined
    if (errorKey.startsWith('validation.')) {
      const key = errorKey.replace('validation.', '') as keyof typeof t.validation
      return t.validation[key] || errorKey
    }
    return errorKey
  }

  const selectClassName =
    'flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-1 disabled:cursor-not-allowed disabled:opacity-50 transition-colors'

  // Boundary points count and estimated area
  const boundaryPointsCount = boundary?.coordinates?.[0]?.length || 0
  const boundaryEstimatedArea =
    boundaryPointsCount >= 4
      ? calculatePolygonAreaHectares(
          boundary!.coordinates[0].map(([lng, lat]) => ({ lat, lng }))
        )
      : 0

  return (
    <>
      <Dialog open={isOpen} onOpenChange={(open) => !open && onClose()}>
        <DialogContent className="sm:max-w-xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-lg font-bold text-foreground">
              {isEditMode ? t.form.editTitle : t.form.createTitle}
            </DialogTitle>
            <DialogDescription className="text-xs text-muted-foreground">
              {isEditMode ? t.form.editSubtitle : t.form.createSubtitle}
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmit(onFormSubmit)} className="space-y-4 py-2">
            {/* Field Name */}
            <Input
              label={t.form.nameLabel}
              placeholder={t.form.namePlaceholder}
              {...register('name')}
              error={getErrorMessage(errors.name?.message)}
              disabled={isSubmitting}
              autoFocus
            />

            {/* Description */}
            <Textarea
              label={t.form.descriptionLabel}
              placeholder={t.form.descriptionPlaceholder}
              {...register('description')}
              error={getErrorMessage(errors.description?.message)}
              disabled={isSubmitting}
              rows={2}
            />

            {/* Area & Crop Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <Input
                label={t.form.areaLabel}
                placeholder={t.form.areaPlaceholder}
                type="number"
                step="0.01"
                min="0.01"
                {...register('areaHectares')}
                error={getErrorMessage(errors.areaHectares?.message)}
                disabled={isSubmitting}
              />

              <div className="space-y-1.5 w-full">
                <label className="text-xs font-semibold tracking-wide text-foreground select-none">
                  {t.form.cropLabel}
                </label>
                <select
                  className={selectClassName}
                  {...register('cropType')}
                  disabled={isSubmitting}
                >
                  <option value="COTTON">{t.crops.COTTON}</option>
                  <option value="WHEAT">{t.crops.WHEAT}</option>
                  <option value="RICE">{t.crops.RICE}</option>
                  <option value="MAIZE">{t.crops.MAIZE}</option>
                  <option value="VEGETABLE">{t.crops.VEGETABLE}</option>
                  <option value="MELON">{t.crops.MELON}</option>
                  <option value="OTHER">{t.crops.OTHER}</option>
                </select>
                {errors.cropType && (
                  <p className="text-xs font-medium text-destructive">
                    {getErrorMessage(errors.cropType.message)}
                  </p>
                )}
              </div>
            </div>

            {/* Soil & Irrigation Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div className="space-y-1.5 w-full">
                <label className="text-xs font-semibold tracking-wide text-foreground select-none">
                  {t.form.soilLabel}
                </label>
                <select
                  className={selectClassName}
                  {...register('soilType')}
                  disabled={isSubmitting}
                >
                  <option value="SANDY">{t.soils.SANDY}</option>
                  <option value="LOAM">{t.soils.LOAM}</option>
                  <option value="CLAY">{t.soils.CLAY}</option>
                  <option value="SANDY_LOAM">{t.soils.SANDY_LOAM}</option>
                  <option value="CLAY_LOAM">{t.soils.CLAY_LOAM}</option>
                  <option value="OTHER">{t.soils.OTHER}</option>
                </select>
                {errors.soilType && (
                  <p className="text-xs font-medium text-destructive">
                    {getErrorMessage(errors.soilType.message)}
                  </p>
                )}
              </div>

              <div className="space-y-1.5 w-full">
                <label className="text-xs font-semibold tracking-wide text-foreground select-none">
                  {t.form.irrigationLabel}
                </label>
                <select
                  className={selectClassName}
                  {...register('irrigationMethod')}
                  disabled={isSubmitting}
                >
                  <option value="CANAL">{t.irrigations.CANAL}</option>
                  <option value="DITCH">{t.irrigations.DITCH}</option>
                  <option value="FURROW">{t.irrigations.FURROW}</option>
                  <option value="DRIP">{t.irrigations.DRIP}</option>
                  <option value="SPRINKLER">{t.irrigations.SPRINKLER}</option>
                  <option value="OTHER">{t.irrigations.OTHER}</option>
                </select>
                {errors.irrigationMethod && (
                  <p className="text-xs font-medium text-destructive">
                    {getErrorMessage(errors.irrigationMethod.message)}
                  </p>
                )}
              </div>
            </div>

            {/* Geographic Coordinates & Location Picker */}
            <div className="space-y-2 pt-2 border-t border-border/50">
              <div className="flex items-center justify-between">
                <label className="text-xs font-semibold tracking-wide text-foreground">
                  Markaziy koordinatalar (Centroid)
                </label>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => setLocationPickerOpen(true)}
                  className="h-7 text-xs gap-1 text-primary hover:text-primary"
                >
                  <MapPin className="h-3.5 w-3.5" />
                  <span>Xaritada tanlash</span>
                </Button>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <Input
                  label={t.form.latitudeLabel}
                  placeholder={t.form.latitudePlaceholder}
                  type="number"
                  step="0.000001"
                  {...register('latitude')}
                  error={getErrorMessage(errors.latitude?.message)}
                  disabled={isSubmitting}
                />

                <Input
                  label={t.form.longitudeLabel}
                  placeholder={t.form.longitudePlaceholder}
                  type="number"
                  step="0.000001"
                  {...register('longitude')}
                  error={getErrorMessage(errors.longitude?.message)}
                  disabled={isSubmitting}
                />
              </div>
            </div>

            {/* Boundary Polygon Section */}
            <div className="space-y-2 pt-2 border-t border-border/50">
              <div className="flex items-center justify-between">
                <label className="text-xs font-semibold tracking-wide text-foreground">
                  Dala chegarasi (GeoJSON Poligon)
                </label>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => setBoundaryEditorOpen(true)}
                  className="h-7 text-xs gap-1 text-emerald-600 dark:text-emerald-400 hover:text-emerald-700"
                >
                  {boundary ? (
                    <>
                      <Edit3 className="h-3.5 w-3.5" />
                      <span>Chegarani tahrirlash</span>
                    </>
                  ) : (
                    <>
                      <Plus className="h-3.5 w-3.5" />
                      <span>Xaritada chegara chizish</span>
                    </>
                  )}
                </Button>
              </div>

              {boundary ? (
                <div className="flex items-center justify-between p-2.5 rounded-lg bg-emerald-50/60 dark:bg-emerald-950/30 border border-emerald-200 dark:border-emerald-800 text-xs">
                  <div className="flex items-center gap-2">
                    <Pentagon className="h-4 w-4 text-emerald-600 dark:text-emerald-400 shrink-0" />
                    <div>
                      <span className="font-semibold text-emerald-900 dark:text-emerald-100">
                        Poligon biriktirilgan
                      </span>
                      <p className="text-[11px] text-muted-foreground mt-0.5">
                        {boundaryPointsCount} nuqta • Chamalangan maydon: {boundaryEstimatedArea.toFixed(2)} ga
                      </p>
                    </div>
                  </div>

                  <Button
                    type="button"
                    variant="ghost"
                    size="sm"
                    onClick={() => setBoundary(null)}
                    className="h-7 w-7 p-0 text-muted-foreground hover:text-destructive"
                    title="Chegarani oʻchirish"
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </Button>
                </div>
              ) : (
                <p className="text-xs text-muted-foreground italic bg-muted/30 p-2.5 rounded-lg border border-border/40">
                  Chegara belgilanmagan (ixtiyoriy). Dala konturini belgilash uchun xaritadan foydalaning.
                </p>
              )}
            </div>

            <DialogFooter className="pt-3 border-t border-border/60 gap-2">
              <Button
                type="button"
                variant="outline"
                onClick={onClose}
                disabled={isSubmitting}
              >
                {translations[lang]?.common?.cancel || 'Bekor qilish'}
              </Button>
              <Button
                type="submit"
                isLoading={isSubmitting}
                disabled={isSubmitting}
              >
                {isSubmitting
                  ? t.form.submitting
                  : isEditMode
                  ? t.form.submitEdit
                  : t.form.submitCreate}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Geospatial Modals */}
      <FieldLocationPickerModal
        isOpen={locationPickerOpen}
        onClose={() => setLocationPickerOpen(false)}
        initialLat={currentLat}
        initialLng={currentLng}
        onSelectLocation={handleLocationSelected}
        lang={lang}
      />

      <FieldBoundaryEditorModal
        isOpen={boundaryEditorOpen}
        onClose={() => setBoundaryEditorOpen(false)}
        initialBoundary={boundary}
        initialLat={currentLat}
        initialLng={currentLng}
        registeredAreaHectares={Number(currentArea)}
        onSaveBoundary={handleBoundarySaved}
        lang={lang}
      />
    </>
  )
}
