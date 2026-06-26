export type InterconnectedSystemStatus = 'ACTIVE' | 'INACTIVE' | 'DELETED';

export interface InterconnectedSystemResponse {
  id: number;
  systemCode: string;
  systemName: string;
  endpointUrl: string;
  status: InterconnectedSystemStatus;
  createdAt: string | number[];
  updatedAt: string | number[] | null;
  message: string | null;
}

export interface UpdateInterconnectedSystemRequest {
  endpointUrl?: string;
  status?: InterconnectedSystemStatus;
}

export interface NewInterconnectedSystemRequest {
  systemCode: string;
  systemName: string;
  endpointUrl: string;
  apiKey: string;
}
