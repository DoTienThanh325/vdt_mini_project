import { CommonModule, Location } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { finalize, Observable } from 'rxjs';
import {
  DocumentFileResponse,
  DocumentResponse,
  DocumentTransferResponse,
} from '../../models/document.model';
import { OrgResponse } from '../../models/organization.model';
import { DocumentService } from '../../services/document.service';
import { OrganizationService } from '../../services/organization.service';
import { formatLocalDateTime, LocalDateTimeValue } from '../../utils/local-date-time';

@Component({
  selector: 'app-outgoing-document-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './outgoing-document-detail.html',
  styleUrl: './outgoing-document-detail.css',
})
export class OutgoingDocumentDetail implements OnInit {
  documentId = 0;
  document: DocumentResponse | null = null;
  organizations: OrgResponse[] = [];
  selectedReceiverOrgId = 0;
  orgPage = 0;
  orgSize = 10;
  orgTotalPages = 0;
  orgFirst = true;
  orgLast = true;
  loading = false;
  actionLoading = false;
  transferLoading = false;
  loadingOrganizations = false;
  openingFileId: number | null = null;
  downloadingFileId: number | null = null;
  errorMessage = '';
  actionMessage = '';
  actionErrorMessage = '';
  organizationErrorMessage = '';
  fileErrorMessage = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly location: Location,
    private readonly documentService: DocumentService,
    private readonly organizationService: OrganizationService,
    private readonly changeDetectorRef: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.documentId = Number(this.route.snapshot.paramMap.get('documentId'));
    this.loadDocument();

    if (this.isClerk) {
      this.loadOrganizationsForTransfer(0);
    }
  }

  get role(): string {
    return (localStorage.getItem('role') || '').trim().toUpperCase().replace(/^ROLE_/, '');
  }

  get isLeader(): boolean {
    return this.role === 'LEADER';
  }

  get isManager(): boolean {
    return this.role === 'MANAGER';
  }

  get isClerk(): boolean {
    return this.role === 'CLERK';
  }

  get canApproveOrReject(): boolean {
    return this.document?.status === 'CREATED';
  }

  get canSign(): boolean {
    return this.document?.status !== 'SIGNED';
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

  approveDocument(): void {
    if (!this.document || !this.canApproveOrReject) return;
    this.runDocumentAction(this.documentService.approve(this.document.id));
  }

  rejectDocument(): void {
    if (!this.document || !this.canApproveOrReject) return;
    this.runDocumentAction(this.documentService.reject(this.document.id));
  }

  signDocument(): void {
    if (!this.document) return;
    this.runDocumentAction(this.documentService.sign(this.document.id));
  }

  checkSignature(): void {
    if (!this.document) return;
    this.runDocumentAction(this.documentService.checkSignature(this.document.id));
  }

  transferDocument(): void {
    if (!this.document || !this.selectedReceiverOrgId || this.transferLoading) {
      this.actionErrorMessage = 'Vui lòng chọn tổ chức nhận';
      return;
    }

    this.transferLoading = true;
    this.actionMessage = '';
    this.actionErrorMessage = '';

    this.documentService
      .transfer(this.document.id, this.selectedReceiverOrgId)
      .pipe(
        finalize(() => {
          this.transferLoading = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (response) => {
          this.actionMessage = this.extractMessage(response, 'Gửi văn bản thành công');
          this.loadDocument();
        },
        error: (error: Error) => (this.actionErrorMessage = this.cleanErrorMessage(error.message)),
      });
  }

  loadOrganizationsForTransfer(page = this.orgPage): void {
    if (page < 0 || (this.orgTotalPages > 0 && page >= this.orgTotalPages)) return;

    this.loadingOrganizations = true;
    this.organizationErrorMessage = '';

    this.organizationService
      .findAll(page, this.orgSize)
      .pipe(
        finalize(() => {
          this.loadingOrganizations = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (response) => {
          this.organizations = response.content;
          this.orgPage = response.page;
          this.orgSize = response.size;
          this.orgTotalPages = response.totalPages;
          this.orgFirst = response.first;
          this.orgLast = response.last;
        },
        error: (error: Error) => {
          this.organizations = [];
          this.organizationErrorMessage = this.cleanErrorMessage(error.message);
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
    return value ? formatLocalDateTime(value, false) : '—';
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
    if (!fileSize && fileSize !== 0) return '—';
    if (fileSize < 1024) return `${fileSize} B`;
    if (fileSize < 1024 * 1024) return `${(fileSize / 1024).toFixed(1)} KB`;
    return `${(fileSize / (1024 * 1024)).toFixed(1)} MB`;
  }

  statusClass(status: string): string {
    return status.toLowerCase();
  }

  statusLabel(status: string): string {
    const labels: Record<string, string> = {
      CREATED: 'Đã tạo',
      APPROVED: 'Đã cấp phép',
      REJECTED: 'Đã từ chối',
      SIGNED: 'Đã ký số',
    };

    return labels[status] || status;
  }

  transferStatusLabel(status: string): string {
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

  private runDocumentAction(request$: Observable<unknown>): void {
    if (this.actionLoading) return;

    this.actionLoading = true;
    this.actionMessage = '';
    this.actionErrorMessage = '';

    request$
      .pipe(
        finalize(() => {
          this.actionLoading = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (response) => {
          this.actionMessage = this.extractMessage(response, 'Thao tác thành công');
          this.loadDocument();
        },
        error: (error: Error) => (this.actionErrorMessage = this.cleanErrorMessage(error.message)),
      });
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

  private cleanErrorMessage(message: string): string {
    return message.replace(/^(error|Error):\s*/i, '');
  }
}
