# Field Management UI Architecture (Milestone 05.4)

## 1. Overview & Objectives
The **Field Management UI** (`src/features/field` & `src/entities/field`) provides agricultural producers, water operators, and platform administrators with an intuitive, responsive interface for managing agricultural land parcels across Yangiariq district.

The UI fully integrates with the backend Field REST API (`/api/v1/fields`) developed in Milestones 05.1–05.3, honoring server-authoritative ownership, anti-enumeration security, and role-based operational permissions.

---

## 2. Layered Architecture (Feature-Sliced Design)

```
frontend/src/
├── entities/field/
│   ├── types.ts                     # Domain contracts (Field, CropType, SoilType, IrrigationMethod, FieldStatus)
│   ├── api.ts                       # Typed REST calls via apiClient (/api/v1/fields)
│   ├── ui/FieldStatusBadge.tsx      # Accessible semantic status indicator (Active / Inactive)
│   └── index.ts                     # Public entity exports
│
├── features/field/
│   ├── schemas/fieldSchema.ts       # Zod validation matching backend domain invariants
│   ├── queries/useFieldQueries.ts   # TanStack Query hooks with targeted cache invalidation
│   ├── components/
│   │   ├── FieldToolbar.tsx         # Metric summary cards, search bar, role-aware Add Field button
│   │   ├── FieldFilters.tsx         # Status, crop, soil, irrigation, and whitelist-safe sort selectors
│   │   ├── FieldTable.tsx           # High-density desktop table with skeleton loaders and action menus
│   │   ├── FieldCard.tsx            # Mobile and tablet responsive card view
│   │   ├── FieldPagination.tsx      # Pagination bar with range display and page jump controls
│   │   ├── FieldFormDialog.tsx      # React Hook Form + Zod create/edit modal (immutable attributes omitted)
│   │   ├── FieldDetailsModal.tsx    # Comprehensive parcel passport view
│   │   ├── FieldDeactivateDialog.tsx# Confirmation modal explaining soft-deactivation history retention
│   │   ├── FieldReactivateDialog.tsx# Confirmation modal for reactivation
│   │   ├── FieldEmptyState.tsx      # Agricultural empty state (default vs filtered)
│   │   └── index.ts
│   └── index.ts
│
├── pages/fields/
│   ├── FieldsPage.tsx               # Orchestrator integrating toolbar, filters, views, dialogs, and toasts
│   └── index.ts
│
└── shared/
    ├── i18n/index.ts                # Multilingual translations (UZ, RU, EN) for all field strings
    └── ui/
        ├── skeleton.tsx             # Shimmer skeleton loader
        ├── textarea.tsx             # Form textarea primitive
        └── toast.tsx                # Toast notifications provider and hook (useToast)
```

---

## 3. Security & Ownership Invariants

1. **Server-Derived Identity**:
   - The frontend never submits or accepts `ownerId` in request payloads (`CreateFieldPayload` and `UpdateFieldPayload` exclude `ownerId`).
   - Ownership is determined strictly from the authenticated JWT session on the backend.
2. **Immutable Attributes**:
   - `id`, `ownerId`, `status`, `createdAt`, and `updatedAt` are immutable in the form. The edit form only submits editable agronomic properties (`name`, `description`, `areaHectares`, `cropType`, `soilType`, `irrigationMethod`, `latitude`, `longitude`).
3. **Role-Aware UI Hiding**:
   - `FARMER`: Can create new parcels, view own parcels, edit, deactivate, and reactivate.
   - `OPERATOR`: District monitoring role. Add Field button and mutation controls (edit, deactivate, reactivate) are hidden from the UI.
   - `ADMIN`: Platform audit role. Can view, edit, and deactivate existing parcels, but cannot register parcels (parcels belong to farmers).
4. **Soft-Deactivation Communication**:
   - Deactivation confirmation explicitly informs the user: *"This field parcel will be transitioned to inactive status. All historical water records, sensor measurements, and incident logs will remain preserved."*

---

## 4. Query & Mutation Strategy (TanStack Query)

Centralized query keys ensure optimal caching without unnecessary global refetches:

```typescript
export const fieldKeys = {
  all: ['fields'] as const,
  lists: () => [...fieldKeys.all, 'list'] as const,
  list: (params: FieldQueryParams) => [...fieldKeys.lists(), params] as const,
  details: () => [...fieldKeys.all, 'detail'] as const,
  detail: (id: string) => [...fieldKeys.details(), id] as const,
}
```

- **List Query**: `useFieldsQuery(params)` fetches paginated, filtered parcels.
- **Detail Query**: `useFieldQuery(id)` fetches single parcel details.
- **Mutations & Cache Invalidation**:
  - `createField` -> Invalidates `fieldKeys.lists()`
  - `updateField` -> Invalidates `fieldKeys.detail(id)` and `fieldKeys.lists()`
  - `deactivateField` -> Invalidates `fieldKeys.detail(id)` and `fieldKeys.lists()`
  - `reactivateField` -> Invalidates `fieldKeys.detail(id)` and `fieldKeys.lists()`

---

## 5. Form Validation & Invariants (React Hook Form + Zod)

The Zod schema (`fieldFormSchema`) enforces domain rules identical to backend validation:
- `name`: Required, trimmed, 1–128 characters.
- `areaHectares`: Number, strictly positive (> 0), maximum 100,000 ha.
- `cropType`, `soilType`, `irrigationMethod`: Strictly typed against backend enums.
- `latitude`: Optional decimal, constrained to Uzbekistan bounds (37.0°N to 45.6°N).
- `longitude`: Optional decimal, constrained to Uzbekistan bounds (56.0°E to 73.2°E).

---

## 6. Internationalization (i18n)

All user-visible labels, validation messages, agricultural enums, dialog titles, and notification toasts are referenced from typed dictionaries:
- **`uz`** (Uzbek): "Paxta", "Tomchilatib sugʻorish", "Qumoq (boʻz)", "Dala pasporti"
- **`ru`** (Russian): "Хлопчатник", "Капельное", "Суглинистая (серозём)", "Паспорт поля"
- **`en`** (English): "Cotton", "Drip", "Loam (Serozem)", "Field Passport"

---

## 7. Responsive & Theme Design

- **Desktop (>= 1024px)**: Data-dense table with sorting, status badges, metric summary grid, and action dropdowns.
- **Mobile (< 1024px)**: Responsive stacked cards with large touch targets, 2x2 agronomic attribute grid, and bottom action bars.
- **Theme Support**: Seamless contrast and accessible colors across Light and Dark modes using Tailwind CSS variables (`bg-background`, `border-border`, `text-foreground`).
