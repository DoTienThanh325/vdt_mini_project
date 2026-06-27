import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import {
  InterconnectedSystemResponse,
  NewInterconnectedSystemRequest,
  UpdateInterconnectedSystemRequest,
} from '../models/interconnected-system.model';
import { getApiErrorMessage } from '../utils/api-error';

@Injectable({
  providedIn: 'root',
})
export class InterconnectedSystemService {
  private readonly apiUrl = 'http://localhost:8081/api/interconnected-systems';

  constructor(private readonly http: HttpClient) {}

  findAll(): Observable<InterconnectedSystemResponse[]> {
    return this.http
      .get<InterconnectedSystemResponse[]>(this.apiUrl, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error, 'Tải danh sách hệ thống liên thông không thành công')));
  }

  create(
    request: NewInterconnectedSystemRequest,
  ): Observable<InterconnectedSystemResponse> {
    return this.http
      .post<InterconnectedSystemResponse>(`${this.apiUrl}/new`, request, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error, 'Tạo hệ thống liên thông không thành công')));
  }

  update(
    id: number,
    request: UpdateInterconnectedSystemRequest,
  ): Observable<InterconnectedSystemResponse> {
    return this.http
      .patch<InterconnectedSystemResponse>(`${this.apiUrl}/${id}`, request, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error, 'Cập nhật hệ thống liên thông không thành công')));
  }

  private getAuthorizationHeaders(): HttpHeaders {
    const token = localStorage.getItem('accessToken');
    const tokenType = localStorage.getItem('tokenType') || 'Bearer';

    return token
      ? new HttpHeaders({ Authorization: `${tokenType} ${token}` })
      : new HttpHeaders();
  }

  private handleError(
    error: HttpErrorResponse,
    operationFallback: string,
  ): Observable<never> {
    return throwError(() => new Error(getApiErrorMessage(error, operationFallback)));
  }
}
