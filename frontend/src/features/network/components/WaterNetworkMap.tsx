import React, { useEffect, useRef } from 'react';
import L from 'leaflet';
import {
  WaterNetworkNode,
  WaterNetworkEdge,
  WaterNetworkNodeType,
} from '@/entities/network';

interface WaterNetworkMapProps {
  nodes?: WaterNetworkNode[];
  edges?: WaterNetworkEdge[];
  selectedEdge?: WaterNetworkEdge | null;
  selectedNode?: WaterNetworkNode | null;
  onSelectEdge: (edge: WaterNetworkEdge | null) => void;
  onSelectNode: (node: WaterNetworkNode | null) => void;
  discrepantEdgeIds?: string[];
  className?: string;
  height?: string;
}

const YANGIARIQ_CENTER = { lat: 41.3650, lng: 60.5900 };

function getNodeColor(type: WaterNetworkNodeType): string {
  switch (type) {
    case 'WATER_SOURCE':
    case 'RESERVOIR':
      return '#0284c7'; // sky-600
    case 'PUMP_STATION':
      return '#7c3aed'; // violet-600
    case 'CONTROL_GATE':
      return '#d97706'; // amber-600
    case 'CANAL_JUNCTION':
      return '#475569'; // slate-600
    case 'DISTRIBUTION_POINT':
      return '#0d9488'; // teal-600
    case 'FIELD_INLET':
      return '#16a34a'; // green-600
    case 'TERMINAL_NODE':
    default:
      return '#64748b'; // slate-500
  }
}

function getNodeIconHtml(type: WaterNetworkNodeType): string {
  switch (type) {
    case 'WATER_SOURCE':
      return '🌊';
    case 'PUMP_STATION':
      return '⚙️';
    case 'CONTROL_GATE':
      return '⛩️';
    case 'CANAL_JUNCTION':
      return '🔀';
    case 'DISTRIBUTION_POINT':
      return '💧';
    case 'FIELD_INLET':
      return '🌱';
    default:
      return '📍';
  }
}

function getEdgeColor(edge: WaterNetworkEdge): string {
  if (edge.operationalStatus === 'BLOCKED') return '#dc2626'; // rose-600
  if (edge.operationalStatus === 'MAINTENANCE' || edge.operationalStatus === 'OFFLINE') return '#64748b'; // slate-500
  if (edge.utilizationPercent >= 90) return '#ea580c'; // orange-600
  if (edge.utilizationPercent >= 70) return '#eab308'; // yellow-500
  if (edge.edgeType === 'PRIMARY_CANAL') return '#0284c7'; // sky-600
  return '#10b981'; // emerald-500
}

function getEdgeWeight(edge: WaterNetworkEdge): number {
  switch (edge.edgeType) {
    case 'PRIMARY_CANAL':
      return 5;
    case 'SECONDARY_CANAL':
      return 3.5;
    case 'DISTRIBUTION_CHANNEL':
      return 2.5;
    case 'PIPELINE':
    default:
      return 2;
  }
}

export const WaterNetworkMap: React.FC<WaterNetworkMapProps> = ({
  nodes = [],
  edges = [],
  selectedEdge,
  selectedNode,
  onSelectEdge,
  onSelectNode,
  discrepantEdgeIds = [],
  className = '',
  height = '560px',
}) => {
  const mapContainerRef = useRef<HTMLDivElement | null>(null);
  const mapInstanceRef = useRef<L.Map | null>(null);
  const networkLayerGroupRef = useRef<L.FeatureGroup | null>(null);
  const isInitialFitDone = useRef(false);

  // Initialize Leaflet Map
  useEffect(() => {
    if (!mapContainerRef.current) return;

    if (mapInstanceRef.current) {
      mapInstanceRef.current.remove();
      mapInstanceRef.current = null;
    }

    const map = L.map(mapContainerRef.current, {
      center: [YANGIARIQ_CENTER.lat, YANGIARIQ_CENTER.lng],
      zoom: 12,
      zoomControl: true,
      scrollWheelZoom: true,
      attributionControl: true,
    });

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution:
        '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(map);

    const layerGroup = L.featureGroup().addTo(map);
    networkLayerGroupRef.current = layerGroup;
    mapInstanceRef.current = map;

    const timer = setTimeout(() => {
      map.invalidateSize();
    }, 200);

    return () => {
      clearTimeout(timer);
      if (mapInstanceRef.current) {
        mapInstanceRef.current.remove();
        mapInstanceRef.current = null;
      }
      networkLayerGroupRef.current = null;
    };
  }, []);

  // Render Network Topology: Canals and Nodes
  useEffect(() => {
    const map = mapInstanceRef.current;
    const layerGroup = networkLayerGroupRef.current;
    if (!map || !layerGroup) return;

    layerGroup.clearLayers();

    const nodeCoordsMap = new Map<string, [number, number]>();
    for (const node of nodes) {
      if (node.latitude != null && node.longitude != null) {
        nodeCoordsMap.set(node.id, [node.latitude, node.longitude]);
      }
    }

    const allBoundsPoints: [number, number][] = [];

    // 1. Draw Edges (Canal Lines)
    for (const edge of edges) {
      let latLngs: [number, number][] = [];

      if (edge.geometry && edge.geometry.length > 0) {
        latLngs = edge.geometry.map((pt) => [pt.latitude, pt.longitude]);
      } else {
        const start = nodeCoordsMap.get(edge.startNodeId);
        const end = nodeCoordsMap.get(edge.endNodeId);
        if (start && end) {
          latLngs = [start, end];
        }
      }

      if (latLngs.length < 2) continue;

      latLngs.forEach((pt) => allBoundsPoints.push(pt));

      const isSelected = selectedEdge?.id === edge.id;
      const color = getEdgeColor(edge);
      const weight = getEdgeWeight(edge) + (isSelected ? 3 : 0);

      const isDiscrepant = discrepantEdgeIds.includes(edge.id);
      const polyline = L.polyline(latLngs, {
        color: isSelected ? '#3b82f6' : isDiscrepant ? '#f43f5e' : color,
        weight: isDiscrepant ? weight + 1 : weight,
        opacity: isSelected ? 1 : 0.85,
        dashArray: edge.operationalStatus === 'BLOCKED' ? '6, 6' : isDiscrepant ? '4, 4' : undefined,
      });

      polyline.bindTooltip(
        `<div class="text-xs font-sans">
          <strong>${edge.name}</strong><br/>
          Turi: ${edge.edgeType}<br/>
          Sig'im: ${edge.currentFlowM3S.toFixed(2)} / ${edge.maxCapacityM3S.toFixed(2)} m³/s (${edge.utilizationPercent}%)<br/>
          Holat: <span class="font-bold">${edge.operationalStatus}</span>
          ${isDiscrepant ? '<br/><span class="text-rose-600 font-bold">⚠️ Yuqori Uzatish Isrofi (&gt;15%)</span>' : ''}
        </div>`,
        { sticky: true }
      );

      polyline.on('click', () => {
        onSelectEdge(edge);
        onSelectNode(null);
      });

      layerGroup.addLayer(polyline);
    }

    // 2. Draw Nodes (Infrastructures)
    for (const node of nodes) {
      if (node.latitude == null || node.longitude == null) continue;

      const isSelected = selectedNode?.id === node.id;
      const color = getNodeColor(node.nodeType);
      const iconEmoji = getNodeIconHtml(node.nodeType);

      const divIcon = L.divIcon({
        className: 'custom-network-node',
        html: `
          <div style="
            background: ${color};
            color: white;
            border: ${isSelected ? '3px solid #1e293b' : '2px solid white'};
            border-radius: 50%;
            width: ${isSelected ? '28px' : '24px'};
            height: ${isSelected ? '28px' : '24px'};
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: ${isSelected ? '13px' : '11px'};
            box-shadow: 0 2px 6px rgba(0,0,0,0.3);
            cursor: pointer;
            transition: transform 0.15s ease;
          ">
            ${iconEmoji}
          </div>
        `,
        iconSize: [24, 24],
        iconAnchor: [12, 12],
      });

      const marker = L.marker([node.latitude, node.longitude], { icon: divIcon });

      marker.bindTooltip(
        `<div class="text-xs font-sans">
          <strong>${node.name}</strong><br/>
          Turi: ${node.nodeType}<br/>
          Kod: ${node.code}<br/>
          Balandlik: ${node.elevationMeters ?? 0} m
        </div>`,
        { sticky: true }
      );

      marker.on('click', () => {
        onSelectNode(node);
        onSelectEdge(null);
      });

      layerGroup.addLayer(marker);
      allBoundsPoints.push([node.latitude, node.longitude]);
    }

    // Auto-fit bounds on initial load if points exist
    if (!isInitialFitDone.current && allBoundsPoints.length > 0) {
      try {
        const bounds = L.latLngBounds(allBoundsPoints);
        map.fitBounds(bounds, { padding: [40, 40], maxZoom: 14 });
        isInitialFitDone.current = true;
      } catch (e) {
        // Fallback to default center
        map.setView([YANGIARIQ_CENTER.lat, YANGIARIQ_CENTER.lng], 12);
      }
    }
  }, [nodes, edges, selectedEdge, selectedNode, onSelectEdge, onSelectNode]);

  return (
    <div
      ref={mapContainerRef}
      style={{ height, width: '100%' }}
      className={`relative rounded-xl overflow-hidden border border-border/80 shadow-sm z-0 ${className}`}
    />
  );
};
