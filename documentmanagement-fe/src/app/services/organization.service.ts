import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Observable, of, switchMap, throwError } from 'rxjs';
import {
  NewOrgRequest,
  OrgPageResponse,
  OrgResponse,
  UpdateOrgRequest,
} from '../models/organization.model';
import { getApiErrorMessage } from '../utils/api-error';

@Injectable({
  providedIn: 'root',
})
export class OrganizationService {
  private readonly apiUrl = 'http://localhost:8081/api/organizations';

  constructor(private readonly http: HttpClient) {}

  findAll(page: number, size: number): Observable<OrgPageResponse> {
    const params = new HttpParams().set('page', page).set('size', size);

    return this.http
      .get<OrgPageResponse>(this.apiUrl, {
        params,
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error, 'Tải danh sách đơn vị liên thông không thành công')));
  }

  findByStatus(
    status: 'ACTIVE' | 'INACTIVE',
    page: number,
    size: number,
  ): Observable<OrgPageResponse> {
    const params = new HttpParams().set('page', page).set('size', size);

    return this.http
      .get<OrgPageResponse>(`${this.apiUrl}/${status}`, {
        params,
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error, 'Lọc đơn vị liên thông không thành công')));
  }

  update(id: number, request: UpdateOrgRequest): Observable<OrgResponse> {
    return this.http
      .patch<OrgResponse>(`${this.apiUrl}/${id}/`, request, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error, 'Cập nhật đơn vị liên thông không thành công')));
  }

  create(request: NewOrgRequest): Observable<OrgResponse> {
    return this.http
      .post<OrgResponse>(`${this.apiUrl}/new`, request, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(
        catchError((error) =>
          this.handleError(error, 'Đăng ký đơn vị liên thông không thành công'),
        ),
      );
  }

  findCreated(): Observable<OrgResponse> {
    return this.http
      .get<OrgResponse>(`${this.apiUrl}/created`, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(
        catchError((error) =>
          this.handleError(error, 'Tải thông tin đơn vị đã đăng ký không thành công'),
        ),
      );
  }

  activate(id: number): Observable<OrgResponse> {
    return this.http
      .patch<OrgResponse>(
        `${this.apiUrl}/${id}/status`,
        {},
        { headers: this.getAuthorizationHeaders() },
      )
      .pipe(catchError((error) => this.handleError(error, 'Kích hoạt đơn vị liên thông không thành công')));
  }

  softDelete(id: number): Observable<OrgResponse> {
    return this.http
      .patch<OrgResponse>(
        `${this.apiUrl}/${id}/softDelete`,
        {},
        { headers: this.getAuthorizationHeaders() },
      )
      .pipe(catchError((error) => this.handleError(error, 'Ngừng hoạt động đơn vị liên thông không thành công')));
  }

  findByCode(orgCode: string, page = 0, size = 50): Observable<OrgResponse | null> {
    return this.findAll(page, size).pipe(
      switchMap((response) => {
        const organization =
          response.content.find((item) => item.orgCode === orgCode) || null;

        if (organization || response.last) {
          return of(organization);
        }

        return this.findByCode(orgCode, page + 1, size);
      }),
    );
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
