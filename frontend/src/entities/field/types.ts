export type CropType =
  | 'COTTON'
  | 'WHEAT'
  | 'RICE'
  | 'MAIZE'
  | 'VEGETABLE'
  | 'MELON'
  | 'OTHER'

export type SoilType =
  | 'SANDY'
  | 'LOAM'
  | 'CLAY'
  | 'SANDY_LOAM'
  | 'CLAY_LOAM'
  | 'OTHER'

export type IrrigationMethod =
  | 'CANAL'
  | 'DITCH'
  | 'FURROW'
  | 'DRIP'
  | 'SPRINKLER'
  | 'OTHER'

export type FieldStatus = 'ACTIVE' | 'INACTIVE'

export interface FieldBoundary {
  type: 'Polygon'
  coordinates: number[][][]
}

export interface Field {
  id: string
  name: string
  description?: string | null
  areaHectares: number
  cropType: CropType
  soilType: SoilType
  irrigationMethod: IrrigationMethod
  latitude?: number | null
  longitude?: number | null
  boundary?: FieldBoundary | null
  status: FieldStatus
  createdAt: string
  updatedAt: string
}

export interface FieldSummary {
  id: string
  name: string
  description?: string | null
  areaHectares: number
  cropType: CropType
  soilType: SoilType
  irrigationMethod: IrrigationMethod
  status: FieldStatus
  createdAt: string
  updatedAt: string
}

export interface FieldQueryParams {
  status?: FieldStatus
  cropType?: CropType
  soilType?: SoilType
  irrigationMethod?: IrrigationMethod
  ownerId?: string
  search?: string
  page?: number
  size?: number
  sort?: string
  sortBy?: 'createdAt' | 'updatedAt' | 'name' | 'areaHectares' | 'status'
  direction?: 'asc' | 'desc'
}

export interface CreateFieldPayload {
  name: string
  description?: string | null
  areaHectares: number
  cropType: CropType
  soilType: SoilType
  irrigationMethod: IrrigationMethod
  latitude?: number | null
  longitude?: number | null
  boundary?: FieldBoundary | null
}

export interface UpdateFieldPayload {
  name?: string
  description?: string | null
  areaHectares?: number
  cropType?: CropType
  soilType?: SoilType
  irrigationMethod?: IrrigationMethod
  latitude?: number | null
  longitude?: number | null
  boundary?: FieldBoundary | null
}
