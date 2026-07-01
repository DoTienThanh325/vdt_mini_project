import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { LoginRequest, LoginResponse } from '../models/auth.model';
import { getApiErrorMessage } from '../utils/api-error';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = 'http://localhost:8081/api/auth';

  constructor(private http: HttpClient) { }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        tap((response: LoginResponse) => {
          localStorage.setItem('accessToken', response.accessToken);
          localStorage.setItem('tokenType', response.tokenType);
          localStorage.setItem('username', response.username);
          localStorage.setItem('fullName', response.fullName);
          localStorage.setItem('role', response.role);
          localStorage.setItem('orgCode', response.orgCode);
        }),
        catchError((error) => this.handleError(error))
      );
  }

  logout(): void {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('tokenType');
    localStorage.removeItem('username');
    localStorage.removeItem('fullName');
    localStorage.removeItem('role');
    localStorage.removeItem('orgCode');
    localStorage.removeItem('organizationId');
  }

  getToken(): string | null {
    return localStorage.getItem('accessToken');
  }

  getRole(): string | null {
    return localStorage.getItem('role');
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  private handleError(error: HttpErrorResponse) {
    return throwError(() => new Error(getApiErrorMessage(error, 'Đăng nhập không thành công')));
  }
}
