# Cooperative Multi-Parcel Irrigation Scheduling

## 1. Challenge: Uncoordinated Irrigation & Canal Contention

When individual farmers irrigate simultaneously along the same secondary canal, aggregate volumetric demand can exceed the physical carrying capacity ($Q_{\text{max}}$) of the canal segment. This results in:
1. Severe head-to-tail inequity: Upstream parcels take full flow, leaving tail-end parcels with insufficient pressure or zero flow.
2. Hydraulic surge and overflow risk: Opening too many turnouts or abrupt gate closures causes water hammer and canal bank scouring.
3. Wasteful drainage losses: Unused tail-end water drains into saline collector systems.

---

## 2. Multi-Level Demand Aggregation

`RegionalDemandAggregator` aggregates volumetric requirements hierarchically without double-counting:

1. **District Tier**: Total district water demand ($m^3$) over the planning horizon (e.g. 7 days).
2. **WUA (SIU) Tier**: Demands partitioned across Water User Associations.
3. **Canal Segment Tier**: Aggregate concurrent flow rates ($m^3/s$) passing through each conveyance channel.

---

## 3. Cooperative Delivery Staggering

When multiple parcels share a common canal and their simultaneous demand exceeds carrying capacity:
1. The contention detection engine flags a `CANAL_CAPACITY_OVERLOAD` conflict.
2. The optimizer staggers delivery start times (e.g., Parcel A receives water from 08:00 to 12:00; Parcel B from 12:00 to 16:00).
3. Peak flow rate along the canal segment is maintained below $90\%$ of maximum capacity.
