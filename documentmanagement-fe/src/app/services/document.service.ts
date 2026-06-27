import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import {
  DocumentFileResponse,
  DocumentPageResponse,
  DocumentSearchRequest,
  DocumentTransferResponse,
  NewDocumentRequest,
} from '../models/document.model';
import { getApiErrorMessage } from '../utils/api-error';

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
      .pipe(catchError((error) => this.handleError(error, 'Tải danh sách văn bản, tài liệu không thành công')));
  }

  findIncoming(page: number, size: number): Observable<DocumentPageResponse> {
    const params = this.getPageParams(page, size);

    return this.http
      .get<DocumentPageResponse>(`${this.apiUrl}/receive`, {
        params,
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error, 'Tải danh sách văn bản, tài liệu nhận không thành công')));
  }

  findIncomingByStatus(status: string, page: number, size: number): Observable<DocumentPageResponse> {
    const params = this.getPageParams(page, size);

    return this.http
      .get<DocumentPageResponse>(`${this.apiUrl}/receive/status/${status}`, {
        params,
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error, 'Lọc văn bản, tài liệu nhận không thành công')));
  }

  findCreatedBy(page: number, size: number): Observable<DocumentPageResponse> {
    const params = this.getPageParams(page, size);

    return this.http
      .get<DocumentPageResponse>(`${this.apiUrl}/createdBy`, {
        params,
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error, 'Tải danh sách văn bản, tài liệu đã tạo không thành công')));
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
      .pipe(catchError((error) => this.handleError(error, 'Tìm kiếm văn bản, tài liệu không thành công')));
  }

  createDocument(request: NewDocumentRequest): Observable<DocumentPageResponse['content'][number]> {
    return this.http
      .post<DocumentPageResponse['content'][number]>(`${this.apiUrl}/new`, request, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error, 'Tạo văn bản, tài liệu không thành công')));
  }

  uploadDocumentFiles(
    documentId: number,
    files: File[],
    operationFallback = 'Tải tệp văn bản, tài liệu lên không thành công',
  ): Observable<DocumentFileResponse[]> {
    const formData = new FormData();
    files.forEach((file) => formData.append('files', file));

    return this.http
      .post<DocumentFileResponse[]>(`${this.apiUrl}/${documentId}/files`, formData, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error, operationFallback)));
  }

  deleteDocument(documentId: number): Observable<unknown> {
    return this.http
      .delete(`${this.apiUrl}/delete/${documentId}`, {
        headers: this.getAuthorizationHeaders(),
        responseType: 'text',
      })
      .pipe(catchError((error) => this.handleError(error, 'Xóa văn bản, tài liệu không thành công')));
  }

  findById(documentId: number): Observable<DocumentPageResponse['content'][number]> {
    return this.http
      .get<DocumentPageResponse['content'][number]>(`${this.apiUrl}/${documentId}`, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error, 'Tải thông tin văn bản, tài liệu không thành công')));
  }

  approve(documentId: number): Observable<unknown> {
    return this.http
      .patch(`${this.apiUrl}/${documentId}/approve`, {}, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error, 'Phê duyệt văn bản, tài liệu không thành công')));
  }

  reject(documentId: number): Observable<unknown> {
    return this.http
      .patch(`${this.apiUrl}/${documentId}/reject`, {}, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error, 'Từ chối văn bản, tài liệu không thành công')));
  }

  sign(documentId: number): Observable<unknown> {
    return this.http
      .post(`${this.apiUrl}/${documentId}/sign`, {}, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error, 'Ký văn bản, tài liệu không thành công')));
  }

  checkSignature(documentId: number): Observable<unknown> {
    return this.http
      .patch(`${this.apiUrl}/${documentId}/sign/check`, {}, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error, 'Kiểm tra chữ ký văn bản, tài liệu không thành công')));
  }

  receiveDocument(documentId: number): Observable<unknown> {
    return this.http
      .patch(`${this.apiUrl}/${documentId}/receive`, {}, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error, 'Xác nhận nhận văn bản, tài liệu không thành công')));
  }

  respond(documentId: number, responseContent: string): Observable<DocumentTransferResponse> {
    return this.http
      .patch<DocumentTransferResponse>(
        `${this.apiUrl}/${documentId}/response`,
        { responseContent },
        { headers: this.getAuthorizationHeaders() },
      )
      .pipe(catchError((error) => this.handleError(error, 'Phản hồi văn bản, tài liệu không thành công')));
  }

  transfer(documentId: number, receiverOrgId: number): Observable<unknown> {
    return this.http
      .post(`${this.apiUrl}/${documentId}/transfer`, { receiverOrgId }, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(catchError((error) => this.handleError(error, 'Gửi văn bản, tài liệu không thành công')));
  }

  getFile(file: DocumentFileResponse): Observable<Blob> {
    return this.http
      .get(this.toFileUrl(file.filePath), {
        headers: this.getAuthorizationHeaders(),
        responseType: 'blob',
      })
      .pipe(catchError((error) => this.handleError(error, 'Tải tệp văn bản, tài liệu không thành công')));
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

  private handleError(
    error: HttpErrorResponse,
    operationFallback: string,
  ): Observable<never> {
    return throwError(() => new Error(getApiErrorMessage(error, operationFallback)));
  }
}
