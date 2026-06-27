import { PageResponse } from './user.model';

export interface OrgResponse {
  id: number;
  orgCode: string;
  orgName: string;
  address: string;
  email: string;
  phone: string;
  status: string;
  systemId: number;
  systemCode: string;
  systemName: string;
  createdAt: string | number[];
  updatedAt: string | number[] | null;
  message: string | null;
}

export interface UpdateOrgRequest {
  orgCode?: string;
  address?: string;
  email?: string;
  phone?: string;
  systemId?: number;
}

export interface NewOrgRequest {
  orgCode: string;
  orgName: string;
  address?: string;
  email: string;
  phone?: string;
  systemId: number;
}

export type OrgPageResponse = PageResponse<OrgResponse>;
