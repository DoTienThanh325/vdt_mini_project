import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import {
  DocumentFileResponse,
  DocumentPageResponse,
  DocumentSearchRequest,
  NewDocumentRequest,
} from '../models/document.model';

@Injectable({
  providedIn: 'root',
})
export class DocumentService {
  private readonly apiUrl = 'http://localhost:8081/api/documents';

  constructor(private readonly http: HttpClient) {}

  findOutgoing(page: number, size: number): Observable<DocumentPageResponse> {
    const params = this.getPageParams(page, size);

    return this.http
      .get<DocumentPageResponse>(this.apiUrl, {
        params,
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  findIncoming(page: number, size: number): Observable<DocumentPageResponse> {
    const params = this.getPageParams(page, size);

    return this.http
      .get<DocumentPageResponse>(`${this.apiUrl}/receive`, {
        params,
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  findIncomingByStatus(status: string, page: number, size: number): Observable<DocumentPageResponse> {
    const params = this.getPageParams(page, size);

    return this.http
      .get<DocumentPageResponse>(`${this.apiUrl}/receive/status/${status}`, {
        params,
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  findCreatedBy(page: number, size: number): Observable<DocumentPageResponse> {
    const params = this.getPageParams(page, size);

    return this.http
      .get<DocumentPageResponse>(`${this.apiUrl}/createdBy`, {
        params,
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  searchOutgoing(
    request: DocumentSearchRequest,
    page: number,
    size: number,
  ): Observable<DocumentPageResponse> {
    const params = this.getPageParams(page, size);

    return this.http
      .post<DocumentPageResponse>(`${this.apiUrl}/search`, request, {
        params,
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  createDocument(request: NewDocumentRequest): Observable<DocumentPageResponse['content'][number]> {
    return this.http
      .post<DocumentPageResponse['content'][number]>(`${this.apiUrl}/new`, request, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  uploadDocumentFiles(documentId: number, files: File[]): Observable<DocumentFileResponse[]> {
    const formData = new FormData();
    files.forEach((file) => formData.append('files', file));

    return this.http
      .post<DocumentFileResponse[]>(`${this.apiUrl}/${documentId}/files`, formData, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  deleteDocument(documentId: number): Observable<unknown> {
    return this.http
      .delete(`${this.apiUrl}/delete/${documentId}`, {
        headers: this.getAuthorizationHeaders(),
        responseType: 'text',
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  findById(documentId: number): Observable<DocumentPageResponse['content'][number]> {
    return this.http
      .get<DocumentPageResponse['content'][number]>(`${this.apiUrl}/${documentId}`, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  approve(documentId: number): Observable<unknown> {
    return this.http
      .patch(`${this.apiUrl}/${documentId}/approve`, {}, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  reject(documentId: number): Observable<unknown> {
    return this.http
      .patch(`${this.apiUrl}/${documentId}/reject`, {}, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  sign(documentId: number): Observable<unknown> {
    return this.http
      .post(`${this.apiUrl}/${documentId}/sign`, {}, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  checkSignature(documentId: number): Observable<unknown> {
    return this.http
      .patch(`${this.apiUrl}/${documentId}/sign/check`, {}, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  receiveDocument(documentId: number): Observable<unknown> {
    return this.http
      .patch(`${this.apiUrl}/${documentId}/receive`, {}, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  transfer(documentId: number, receiverOrgId: number): Observable<unknown> {
    return this.http
      .post(`${this.apiUrl}/${documentId}/transfer`, { receiverOrgId }, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  getFile(file: DocumentFileResponse): Observable<Blob> {
    return this.http
      .get(this.toFileUrl(file.filePath), {
        headers: this.getAuthorizationHeaders(),
        responseType: 'blob',
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  private getPageParams(page: number, size: number): HttpParams {
    return new HttpParams().set('page', page).set('size', size);
  }

  private getAuthorizationHeaders(): HttpHeaders {
    const token = localStorage.getItem('accessToken');
    const tokenType = localStorage.getItem('tokenType') || 'Bearer';

    return token
      ? new HttpHeaders({ Authorization: `${tokenType} ${token}` })
      : new HttpHeaders();
  }

  private toFileUrl(filePath: string): string {
    const normalizedPath = filePath.replace(/\\/g, '/');

    if (/^https?:\/\//i.test(normalizedPath)) {
      return normalizedPath;
    }

    const uploadsIndex = normalizedPath.indexOf('/uploads/');
    if (uploadsIndex >= 0) {
      return `http://localhost:8081${encodeURI(normalizedPath.slice(uploadsIndex))}`;
    }

    const relativeUploadsIndex = normalizedPath.indexOf('uploads/');
    if (relativeUploadsIndex >= 0) {
      return `http://localhost:8081/${encodeURI(normalizedPath.slice(relativeUploadsIndex))}`;
    }

    return `http://localhost:8081/${encodeURI(normalizedPath.replace(/^\/+/, ''))}`;
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    const rawMessage =
      error.error?.message ||
      error.error?.error ||
      (typeof error.error === 'string' ? error.error : '') ||
      'Không thể tải dữ liệu văn bản';
    const message = rawMessage.replace(/^(error|Error):\s*/i, '');

    return throwError(() => new Error(message));
  }
}
