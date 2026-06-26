import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import {
  PageResponse,
  RegisterRequest,
  RegisterResponse,
  RoleResponse,
  UserDetailResponse,
  UserResponse,
} from '../models/user.model';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly apiUrl = 'http://localhost:8081/api/users';
  private readonly authUrl = 'http://localhost:8081/api/auth';
  private readonly roleUrl = 'http://localhost:8081/api/roles';

  constructor(private readonly http: HttpClient) {}

  findAll(page: number, size: number): Observable<PageResponse<UserResponse>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);

    return this.http
      .get<PageResponse<UserResponse>>(this.apiUrl, {
        params,
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  findById(id: number): Observable<UserDetailResponse> {
    return this.http
      .get<UserDetailResponse>(`${this.apiUrl}/${id}`, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  findRoles(): Observable<RoleResponse[]> {
    return this.http
      .get<RoleResponse[]>(this.roleUrl, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  register(request: RegisterRequest): Observable<RegisterResponse> {
    return this.http
      .post<RegisterResponse>(`${this.authUrl}/register`, request, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  updateStatus(id: number): Observable<UserResponse> {
    return this.http
      .patch<UserResponse>(
        `${this.apiUrl}/${id}/status`,
        {},
        { headers: this.getAuthorizationHeaders() },
      )
      .pipe(catchError((error) => this.handleError(error)));
  }

  private getAuthorizationHeaders(): HttpHeaders {
    const token = localStorage.getItem('accessToken');
    const tokenType = localStorage.getItem('tokenType') || 'Bearer';

    return token
      ? new HttpHeaders({ Authorization: `${tokenType} ${token}` })
      : new HttpHeaders();
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    const message =
      error.error?.message ||
      error.error?.error ||
      (typeof error.error === 'string' ? error.error : '') ||
      'Không thể tải dữ liệu người dùng';

    return throwError(() => new Error(this.cleanErrorMessage(message)));
  }

  private cleanErrorMessage(message: string): string {
    return message.replace(/^(error|Error):\s*/i, '');
  }
}
