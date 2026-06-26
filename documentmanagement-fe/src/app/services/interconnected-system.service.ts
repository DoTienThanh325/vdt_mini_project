import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import {
  InterconnectedSystemResponse,
  NewInterconnectedSystemRequest,
  UpdateInterconnectedSystemRequest,
} from '../models/interconnected-system.model';

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
      .pipe(catchError((error) => this.handleError(error)));
  }

  create(
    request: NewInterconnectedSystemRequest,
  ): Observable<InterconnectedSystemResponse> {
    return this.http
      .post<InterconnectedSystemResponse>(`${this.apiUrl}/new`, request, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  update(
    id: number,
    request: UpdateInterconnectedSystemRequest,
  ): Observable<InterconnectedSystemResponse> {
    return this.http
      .patch<InterconnectedSystemResponse>(`${this.apiUrl}/${id}`, request, {
        headers: this.getAuthorizationHeaders(),
      })
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
      'Không thể tải dữ liệu hệ thống liên thông';

    return throwError(() => new Error(message));
  }
}
