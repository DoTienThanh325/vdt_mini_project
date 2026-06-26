import { CommonModule, Location } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { finalize } from 'rxjs';
import { DocumentFileResponse, DocumentResponse } from '../../models/document.model';
import { DocumentService } from '../../services/document.service';
import { formatLocalDateTime, LocalDateTimeValue } from '../../utils/local-date-time';

@Component({
  selector: 'app-incoming-document-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './incoming-document-detail.html',
  styleUrl: './incoming-document-detail.css',
})
export class IncomingDocumentDetail implements OnInit {
  documentId = 0;
  document: DocumentResponse | null = null;
  listDocument: DocumentResponse | null = null;
  loading = false;
  actionLoading = false;
  openingFileId: number | null = null;
  downloadingFileId: number | null = null;
  errorMessage = '';
  actionMessage = '';
  actionErrorMessage = '';
  fileErrorMessage = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly location: Location,
    private readonly documentService: DocumentService,
    private readonly changeDetectorRef: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.documentId = Number(this.route.snapshot.paramMap.get('documentId'));
    this.listDocument = history.state?.document || null;
    this.loadDocument();
  }

  get displayDocument(): DocumentResponse | null {
    if (!this.document && !this.listDocument) return null;

    return {
      ...(this.document || ({} as DocumentResponse)),
      ...(this.listDocument || {}),
      files: this.document?.files || this.listDocument?.files || [],
      summary: this.document?.summary || this.listDocument?.summary || '',
      documentCode: this.document?.documentCode || this.listDocument?.documentCode || '',
      documentType: this.document?.documentType || this.listDocument?.documentType || '',
      status: this.listDocument?.status || this.document?.status || '',
      id: this.document?.id || this.listDocument?.id || this.documentId,
      updatedAt: this.document?.updatedAt || this.listDocument?.updatedAt || null,
      message: null,
    };
  }

  get role(): string {
    return (localStorage.getItem('role') || '').trim().toUpperCase().replace(/^ROLE_/, '');
  }

  get isClerk(): boolean {
    return this.role === 'CLERK';
  }

  get canConfirmReceive(): boolean {
    return this.displayDocument?.status === 'SENT';
  }

  loadDocument(): void {
    if (!this.documentId) {
      this.errorMessage = 'Không tìm thấy mã văn bản';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.documentService
      .findById(this.documentId)
      .pipe(
        finalize(() => {
          this.loading = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (document) => (this.document = document),
        error: (error: Error) => (this.errorMessage = this.cleanErrorMessage(error.message)),
      });
  }

  backToList(): void {
    this.location.back();
  }

  confirmReceive(): void {
    const document = this.displayDocument;
    if (!document || !this.isClerk || !this.canConfirmReceive || this.actionLoading) return;

    this.actionLoading = true;
    this.actionMessage = '';
    this.actionErrorMessage = '';

    this.documentService
      .receiveDocument(document.id)
      .pipe(
        finalize(() => {
          this.actionLoading = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (response) => {
          const status = this.extractStatus(response) || 'RECEIVED';
          this.listDocument = { ...document, status };
          if (this.document) {
            this.document = { ...this.document, status };
          }
          this.actionMessage = this.extractMessage(response, 'Xác nhận nhận văn bản thành công');
          this.loadDocument();
        },
        error: (error: Error) => (this.actionErrorMessage = this.cleanErrorMessage(error.message)),
      });
  }

  openFile(file: DocumentFileResponse): void {
    if (this.openingFileId !== null) return;

    this.openingFileId = file.id;
    this.fileErrorMessage = '';

    this.documentService.getFile(file).subscribe({
      next: (blob) => {
        const fileUrl = URL.createObjectURL(blob);
        window.open(fileUrl, '_blank', 'noopener');
        window.setTimeout(() => URL.revokeObjectURL(fileUrl), 60_000);
        this.openingFileId = null;
        this.changeDetectorRef.markForCheck();
      },
      error: (error: Error) => {
        this.fileErrorMessage = this.cleanErrorMessage(error.message);
        this.openingFileId = null;
        this.changeDetectorRef.markForCheck();
      },
    });
  }

  downloadFile(file: DocumentFileResponse): void {
    if (this.downloadingFileId !== null) return;

    this.downloadingFileId = file.id;
    this.fileErrorMessage = '';

    this.documentService.getFile(file).subscribe({
      next: (blob) => {
        const fileUrl = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = fileUrl;
        link.download = file.originalFileName || file.storedFileName || 'document-file';
        link.click();
        URL.revokeObjectURL(fileUrl);
        this.downloadingFileId = null;
        this.changeDetectorRef.markForCheck();
      },
      error: (error: Error) => {
        this.fileErrorMessage = this.cleanErrorMessage(error.message);
        this.downloadingFileId = null;
        this.changeDetectorRef.markForCheck();
      },
    });
  }

  formatDateTime(value: LocalDateTimeValue | null | undefined): string {
    return value ? formatLocalDateTime(value, false) : '-';
  }

  documentCreatedAt(document: DocumentResponse): LocalDateTimeValue | null | undefined {
    return document.createdAt || document.creatdAt;
  }

  documentFiles(document: DocumentResponse | null): DocumentFileResponse[] {
    return document?.files || [];
  }

  formatFileSize(fileSize: number | null | undefined): string {
    if (!fileSize && fileSize !== 0) return '-';
    if (fileSize < 1024) return `${fileSize} B`;
    if (fileSize < 1024 * 1024) return `${(fileSize / 1024).toFixed(1)} KB`;
    return `${(fileSize / (1024 * 1024)).toFixed(1)} MB`;
  }

  statusClass(status: string): string {
    return status.toLowerCase();
  }

  statusLabel(status: string): string {
    const labels: Record<string, string> = {
      SENT: 'Đã gửi',
      FAILED: 'Gửi bị lỗi',
      RECEIVED: 'Đã nhận',
      RESPONDED: 'Đã phản hồi',
    };

    return labels[status] || status;
  }

  documentTypeLabel(documentType: string): string {
    const labels: Record<string, string> = {
      CONG_VAN: 'Công văn',
      THONG_BAO: 'Thông báo',
      TO_TRINH: 'Tờ trình',
    };

    return labels[documentType] || documentType;
  }

  private extractMessage(response: unknown, fallback: string): string {
    if (response && typeof response === 'object' && 'message' in response) {
      const message = (response as { message?: unknown }).message;
      if (typeof message === 'string' && message.trim()) {
        return this.cleanErrorMessage(message);
      }
    }

    if (typeof response === 'string' && response.trim()) {
      return this.cleanErrorMessage(response);
    }

    return fallback;
  }

  private extractStatus(response: unknown): string | null {
    if (response && typeof response === 'object' && 'status' in response) {
      const status = (response as { status?: unknown }).status;
      return typeof status === 'string' && status.trim() ? status : null;
    }

    return null;
  }

  private cleanErrorMessage(message: string): string {
    return message.replace(/^(error|Error):\s*/i, '');
  }
}
