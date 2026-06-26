export interface UserResponse {
  id: number;
  fullName: string;
  status: string;
  roleName: string;
  organizationName: string | null;
  message: string | null;
}

export interface RoleResponse {
  id: number;
  roleCode: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  fullName: string;
  phone?: string;
  email: string;
  roleId: number;
  organizationId: number;
}

export interface RegisterResponse {
  id: number;
  username: string;
  fullName: string;
  email: string;
  role: string;
  organization: string;
  status: string;
  message: string;
}

export interface UserDetailResponse {
  username: string;
  fullName: string;
  phone: string | null;
  email: string | null;
  status: string;
  roleCode: string;
  roleName: string;
  orgCode: string | null;
  orgName: string | null;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
