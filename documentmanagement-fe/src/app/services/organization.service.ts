import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Observable, of, switchMap, throwError } from 'rxjs';
import { OrgPageResponse, OrgResponse, UpdateOrgRequest } from '../models/organization.model';

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
      .pipe(catchError((error) => this.handleError(error)));
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
      .pipe(catchError((error) => this.handleError(error)));
  }

  update(id: number, request: UpdateOrgRequest): Observable<OrgResponse> {
    return this.http
      .patch<OrgResponse>(`${this.apiUrl}/${id}/`, request, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  activate(id: number): Observable<OrgResponse> {
    return this.http
      .patch<OrgResponse>(
        `${this.apiUrl}/${id}/status`,
        {},
        { headers: this.getAuthorizationHeaders() },
      )
      .pipe(catchError((error) => this.handleError(error)));
  }

  softDelete(id: number): Observable<OrgResponse> {
    return this.http
      .patch<OrgResponse>(
        `${this.apiUrl}/${id}/softDelete`,
        {},
        { headers: this.getAuthorizationHeaders() },
      )
      .pipe(catchError((error) => this.handleError(error)));
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

  private handleError(error: HttpErrorResponse): Observable<never> {
    const message = (
      error.error?.message ||
      error.error?.error ||
      (typeof error.error === 'string' ? error.error : '') ||
      'Không thể tải dữ liệu đơn vị liên thông'
    ).replace(/^(error|Error):\s*/i, '');

    return throwError(() => new Error(message));
  }
}
