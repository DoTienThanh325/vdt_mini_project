import { CommonModule, Location } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { finalize } from 'rxjs';
import {
  DocumentFileResponse,
  DocumentResponse,
  DocumentTransferResponse,
} from '../../models/document.model';
import { DocumentService } from '../../services/document.service';
import { formatLocalDateTime, LocalDateTimeValue } from '../../utils/local-date-time';

@Component({
  selector: 'app-incoming-document-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './incoming-document-detail.html',
  styleUrl: './incoming-document-detail.css',
})
export class IncomingDocumentDetail implements OnInit {
  documentId = 0;
  document: DocumentResponse | null = null;
  listDocument: DocumentResponse | null = null;
  loading = false;
  actionLoading = false;
  responseLoading = false;
  openingFileId: number | null = null;
  downloadingFileId: number | null = null;
  errorMessage = '';
  actionMessage = '';
  actionErrorMessage = '';
  fileErrorMessage = '';
  responseContent = '';
  latestResponse: DocumentTransferResponse | null = null;

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

    const detailedTransfers = this.documentTransfers(this.document);
    const incomingTransferStatus =
      detailedTransfers.find(
        (transfer) =>
          this.isCurrentOrganizationTransfer(transfer) &&
          ['SENT', 'RECEIVED', 'RESPONDED', 'FAILED'].includes(transfer.status),
      )?.status || '';
    const transfers =
      this.document?.tranfers ||
      this.document?.transfers ||
      this.listDocument?.tranfers ||
      this.listDocument?.transfers ||
      [];

    return {
      ...(this.document || ({} as DocumentResponse)),
      ...(this.listDocument || {}),
      files: this.document?.files || this.listDocument?.files || [],
      tranfers: transfers,
      transfers,
      summary: this.document?.summary || this.listDocument?.summary || '',
      documentCode: this.document?.documentCode || this.listDocument?.documentCode || '',
      documentType: this.document?.documentType || this.listDocument?.documentType || '',
      status: this.listDocument?.status || incomingTransferStatus || this.document?.status || '',
      id: this.document?.id || this.listDocument?.id || this.documentId,
      updatedAt: this.document?.updatedAt || this.listDocument?.updatedAt || null,
      message: null,
    };
  }

  get role(): string {
    return (localStorage.getItem('role') || '').trim().toUpperCase().replace(/^ROLE_/, '');
  }

  get orgCode(): string {
    return (localStorage.getItem('orgCode') || '').trim().toUpperCase();
  }

  get isClerk(): boolean {
    return this.role === 'CLERK';
  }

  get isManager(): boolean {
    return this.role === 'MANAGER';
  }

  get canConfirmReceive(): boolean {
    return this.displayDocument?.status === 'SENT';
  }

  get responseTransfer(): DocumentTransferResponse | null {
    return (
      (this.latestResponse && this.isCurrentOrganizationTransfer(this.latestResponse)
        ? this.latestResponse
        : null) ||
      this.documentTransfers(this.displayDocument).find(
        (transfer) =>
          this.isCurrentOrganizationTransfer(transfer) && !!transfer.responseContent?.trim(),
      ) ||
      null
    );
  }

  get canRespond(): boolean {
    return this.displayDocument?.status === 'RECEIVED' && !this.responseTransfer;
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

  respondToDocument(): void {
    const document = this.displayDocument;
    const content = this.responseContent.trim();

    if (!document || !this.isManager || !this.canRespond || this.responseLoading) {
      return;
    }

    if (!content) {
      this.actionErrorMessage = 'Vui lòng nhập nội dung phản hồi';
      return;
    }

    this.responseLoading = true;
    this.actionMessage = '';
    this.actionErrorMessage = '';

    this.documentService
      .respond(document.id, content)
      .pipe(
        finalize(() => {
          this.responseLoading = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (response) => {
          this.latestResponse = response;
          this.responseContent = '';
          this.updateDisplayedDocumentStatus(response.status || 'RESPONDED');
          this.actionMessage = 'Phản hồi văn bản, tài liệu thành công';
        },
        error: (error: Error) => {
          this.actionErrorMessage = this.cleanErrorMessage(error.message);
        },
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

  documentTransfers(document: DocumentResponse | null): DocumentTransferResponse[] {
    return document?.tranfers || document?.transfers || [];
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

  private updateDisplayedDocumentStatus(status: string): void {
    const document = this.displayDocument;
    if (!document) return;

    this.listDocument = { ...document, status };
    if (this.document) {
      this.document = { ...this.document, status };
    }
  }

  private isCurrentOrganizationTransfer(transfer: DocumentTransferResponse): boolean {
    return !!this.orgCode && transfer.receiverOrgCode.trim().toUpperCase() === this.orgCode;
  }

  private cleanErrorMessage(message: string): string {
    return message.replace(/^(error|Error):\s*/i, '');
  }
}
