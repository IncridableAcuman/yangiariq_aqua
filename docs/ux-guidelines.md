# Agricultural UX & UI Design Guidelines

## 1. Design Philosophy: Serious Agricultural Operations

Yangiariq Aqua AI is an mission-critical decision-support system designed for farmers, canal operators, and water resource authorities in the Yangiariq district of Khorezm. 

### What to Avoid
- **No Generic AI Gimmicks**: Avoid fake futuristic glowing gradients, pulsing AI neon borders, and meaningless "AI Magic" badges.
- **No Decorative Overhead**: Avoid high-blur glassmorphism that degrades readability in direct outdoor sunlight on mobile screens.
- **No Meaningless Numbers**: Raw data (e.g., "Moisture: 18%") must never be shown without operational context (e.g., "18% — Below 25% Stress Threshold — Irrigation Advised").

### What to Prioritize
- **High Information Density & Contrast**: High-contrast typography and clear visual grouping suitable for low-brightness field smartphones.
- **Explainable Reasoning**: Whenever an AI or algorithmic priority is presented, a one-click explanation dialog must explain *why* (e.g. soil deficit, crop stage, hydraulic interlock).
- **Action-First Architecture**: Every alert, anomaly, or recommendation must be accompanied by a direct, single-click action button routing to the resolution workflow.

---

## 2. The 10-Second Farm Overview Rule

Upon logging into the dashboard, any user must be able to absorb the operational state of their entire farm within **10 seconds**:

```
Viewport 1: Status & Urgent Actions
├── 1. Who am I & what is my operational scope? (Header)
├── 2. What requires urgent human intervention? (Action Center Banner)
└── 3. What is the overall operational health? (5 KPI Dimension Cards)

Viewport 2: Operational Deep-Dive
├── 4. Which parcels are in danger and why? (Priority Parcels Table)
├── 5. Where are they located spatially? (Interactive Leaflet GIS Map)
└── 6. Do I have enough seasonal water left? (Water Balance Breakdown)

Viewport 3: Planning & Traceability
├── 7. What is the status of current irrigation operations? (6-Stage Lifecycle)
└── 8. What has happened recently? (Operational Event Feed)
```

---

## 3. Visual Hierarchy & Screen Structure

```
┌────────────────────────────────────────────────────────────────────────┐
│ Farm Intelligence Workspace Header       [Refresh] [Lang: UZ|RU|EN]   │
├────────────────────────────────────────────────────────────────────────┤
│ 🔴 Action Center (Urgent banners sorted by CRITICAL -> HIGH -> MEDIUM) │
├────────────────────────────────────────────────────────────────────────┤
│ 5 KPI Dimension Cards:                                                 │
│ [ Parcels ]   [ Water Balance ]  [ Sensors ]  [ Alerts ]  [ Lifecycle ]│
├────────────────────────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────┐ ┌───────────────────────────┐ │
│ │ Priority Fields (Ranked 1..N)        │ │ Farm Spatial GIS Map      │ │
│ │ • Search, Priority Filter, Crops     │ │ • Color-coded parcels     │ │
│ │ • Moisture %, Status, Explain Modal  │ │ • Click to inspect        │ │
│ └──────────────────────────────────────┘ └───────────────────────────┘ │
├────────────────────────────────────────────────────────────────────────┤
│ Seasonal Water Balance (Allocated / Consumed / Approved / Remaining)   │
├────────────────────────────────────────────────────────────────────────┤
│ ┌──────────────────────────────────────┐ ┌───────────────────────────┐ │
│ │ 6-Stage Irrigation Pipeline          │ │ Operational Activity Feed │ │
│ └──────────────────────────────────────┘ └───────────────────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 4. Semantic Color Tokens & Severity Mapping

To avoid visual confusion, colors strictly adhere to domain safety semantics:

| Severity | Color Tokens (Tailwind) | Operational Meaning | Agricultural Example |
| :--- | :--- | :--- | :--- |
| **`CRITICAL`** | `bg-red-50 border-red-200 text-red-700` | Immediate crop damage risk or pipe burst | Moisture $< 15\%$ (wilting), burst canal, pressure collapse |
| **`HIGH`** | `bg-amber-50 border-amber-200 text-amber-700` | Depletion risk or water quota depletion | Moisture $< 25\%$, seasonal water usage $> 90\%$ |
| **`MEDIUM`** | `bg-yellow-50 border-yellow-200 text-yellow-700` | Action recommended or pending review | AI plan ready for approval, mild moisture decline |
| **`LOW / NORMAL`** | `bg-emerald-50 border-emerald-200 text-emerald-700`| Safe, optimal conditions | Moisture $45\dots 65\%$ (field capacity), normal canal intake |
| **`INFO / NEUTRAL`**| `bg-slate-50 border-slate-200 text-slate-700` | System status, historical logs | Sensor online, cycle completed |

---

## 5. Responsive Behavior: Desktop vs. Mobile Field Usage

Farmers frequently view the dashboard on Android smartphones in bright outdoor conditions, while operators use multi-monitor control setups.

### Desktop View ($\ge 1024\text{px}$)
- Full multi-column dashboard grid.
- Expandable high-density data tables with sorting, multi-attribute filtering, and inline modal popups.
- Side-by-side synchronization between the parcel table and the interactive GIS map.

### Mobile Field View ($< 768\text{px}$)
- **Stacked Card Layout**: The data table seamlessly transforms into touch-friendly cards (`hidden md:block` vs `md:hidden`).
- **Touch Target Dimensions**: All clickable CTA buttons, filter toggles, and modal triggers have a minimum height of $44\text{px}$.
- **Horizontal Progress Visualization**: Water budget bar renders as full-width responsive stacked blocks with legible percentage labels.
- **One-Thumb Navigation**: Primary CTAs (e.g. "Avaryani koʻrish", "Rejani koʻrish") span full card width on mobile screens.

---

## 6. Internationalization & Agricultural Terminology

All terminology is localized across Uzbek (`uz`), Russian (`ru`), and English (`en`). Terminology strictly respects local Khorezm water management conventions:

| English Term | Uzbek Term | Russian Term | Context & Usage |
| :--- | :--- | :--- | :--- |
| **Farm Intelligence** | *Fermer Xoʻjaligi Intellekti* | *Интеллект фермерского хозяйства* | Main workspace branding |
| **Priority Parcels** | *Prioritet Dalalar* | *Приоритетные поля* | Fields ranked by urgency |
| **Soil Moisture** | *Tuproq namligi* | *Влажность почвы* | Key telemetry metric |
| **Wilting Point** | *Soʻlish nuqtasi (15%)* | *Точка увядания* | Critical drought boundary |
| **Water Allocation** | *Mavsumiy suv limiti* | *Сезонный лимит воды* | Official quota allocated by water authority |
| **Hydraulic Interlock**| *Gidravlik xavfsizlik cheklovi* | *Гидравлическая блокировка* | Automated canal safety protection |
| **Irrigation Plan** | *Sugʻorish rejasi* | *План орошения* | Scheduled watering event |

---

## 7. Performance, Skeleton Loaders & Error Resilience

1. **Zero Layout Shift (CLS = 0)**:
   - During TanStack Query data fetching, `DashboardLoadingSkeleton` renders structural placeholders that precisely match the dimensions of the final KPI cards and tables.
2. **Graceful Error Recovery**:
   - In case of network failure or server restart, `DashboardErrorState` displays an informative error banner with a prominent **"Qayta urinish" (Retry)** button.
   - If stale cache is available, the interface retains previous data while flagging background retry progress.
3. **Background Auto-Refresh**:
   - Telemetry and urgent anomalies refresh automatically every 2 minutes (`refetchInterval: 120_000`) without flashing or resetting scroll positions.
