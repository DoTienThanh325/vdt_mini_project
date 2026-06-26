import { PageResponse } from './user.model';

export type DocumentStatus = '' | 'CREATED' | 'APPROVED' | 'REJECTED' | 'SIGNED';
export type DocumentType = '' | 'CONG_VAN' | 'THONG_BAO' | 'TO_TRINH';

export interface DocumentSearchRequest {
  status: DocumentStatus;
  documentType: DocumentType;
}

export interface NewDocumentRequest {
  documentType: Exclude<DocumentType, ''>;
  summary: string;
}

export interface DocumentResponse {
  id: number;
  documentType: string;
  documentCode: string;
  senderOrgName?: string | null;
  summary: string;
  status: string;
  files?: DocumentFileResponse[] | null;
  tranfers?: DocumentTransferResponse[] | null;
  transfers?: DocumentTransferResponse[] | null;
  creatdAt?: string | number[] | null;
  createdAt?: string | number[] | null;
  updatedAt: string | number[] | null;
  message: string | null;
}

export interface DocumentFileResponse {
  id: number;
  originalFileName: string;
  storedFileName: string;
  filePath: string;
  fileType: string;
  documentCode: string;
  documentSummary: string;
  fileSize: number;
}

export interface DocumentTransferResponse {
  responseContent: string | null;
  status: string;
  senderUsername: string;
  receiverOrgCode: string;
  receiverOrgName?: string | null;
  receiverUsername: string | null;
  documentCode: string;
}

export type DocumentPageResponse = PageResponse<DocumentResponse>;
