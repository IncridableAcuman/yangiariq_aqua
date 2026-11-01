import {
  useQuery,
  useMutation,
  useQueryClient,
  UseQueryResult,
  UseMutationResult,
} from '@tanstack/react-query'
import {
  Field,
  FieldQueryParams,
  CreateFieldPayload,
  UpdateFieldPayload,
  getFields,
  getFieldById,
  createField,
  updateField,
  deactivateField,
  reactivateField,
} from '@/entities/field'
import { PageResponse } from '@/shared/api/client'

export const fieldKeys = {
  all: ['fields'] as const,
  lists: () => [...fieldKeys.all, 'list'] as const,
  list: (params: FieldQueryParams) => [...fieldKeys.lists(), params] as const,
  details: () => [...fieldKeys.all, 'detail'] as const,
  detail: (id: string) => [...fieldKeys.details(), id] as const,
}

export function useFieldsQuery(
  params: FieldQueryParams = {}
): UseQueryResult<PageResponse<Field>, Error> {
  return useQuery({
    queryKey: fieldKeys.list(params),
    queryFn: () => getFields(params),
  })
}

export function useFieldQuery(
  fieldId: string | null | undefined
): UseQueryResult<Field, Error> {
  return useQuery({
    queryKey: fieldKeys.detail(fieldId || ''),
    queryFn: () => getFieldById(fieldId!),
    enabled: Boolean(fieldId),
  })
}

export function useCreateFieldMutation(): UseMutationResult<
  Field,
  Error,
  CreateFieldPayload
> {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (payload: CreateFieldPayload) => createField(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: fieldKeys.lists() })
      queryClient.invalidateQueries({ queryKey: ['gis'] })
    },
  })
}

export function useUpdateFieldMutation(): UseMutationResult<
  Field,
  Error,
  { id: string; payload: UpdateFieldPayload }
> {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: UpdateFieldPayload }) =>
      updateField(id, payload),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: fieldKeys.detail(data.id) })
      queryClient.invalidateQueries({ queryKey: fieldKeys.lists() })
      queryClient.invalidateQueries({ queryKey: ['gis'] })
    },
  })
}

export function useDeactivateFieldMutation(): UseMutationResult<
  void,
  Error,
  string
> {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => deactivateField(id),
    onSuccess: (_data, id) => {
      queryClient.invalidateQueries({ queryKey: fieldKeys.detail(id) })
      queryClient.invalidateQueries({ queryKey: fieldKeys.lists() })
      queryClient.invalidateQueries({ queryKey: ['gis'] })
    },
  })
}

export function useReactivateFieldMutation(): UseMutationResult<
  Field,
  Error,
  string
> {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (id: string) => reactivateField(id),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: fieldKeys.detail(data.id) })
      queryClient.invalidateQueries({ queryKey: fieldKeys.lists() })
      queryClient.invalidateQueries({ queryKey: ['gis'] })
    },
  })
}
