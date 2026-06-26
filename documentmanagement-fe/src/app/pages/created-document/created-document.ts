import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, map, switchMap } from 'rxjs';
import {
  DocumentResponse,
  DocumentType,
  NewDocumentRequest,
} from '../../models/document.model';
import { DocumentService } from '../../services/document.service';
import { formatLocalDateTime, LocalDateTimeValue } from '../../utils/local-date-time';

@Component({
  selector: 'app-created-document',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './created-document.html',
  styleUrl: './created-document.css',
})
export class CreatedDocument implements OnInit {
  readonly documentTypes: Exclude<DocumentType, ''>[] = ['CONG_VAN', 'THONG_BAO', 'TO_TRINH'];

  documents: DocumentResponse[] = [];
  page = 0;
  size = 10;
  totalElements = 0;
  totalPages = 0;
  first = true;
  last = true;
  loading = false;
  createModalOpen = false;
  createLoading = false;
  deletingDocumentId: number | null = null;
  createDocumentType: Exclude<DocumentType, ''> = 'CONG_VAN';
  createSummary = '';
  selectedFiles: File[] = [];
  errorMessage = '';
  actionMessage = '';
  actionErrorMessage = '';

  constructor(
    private readonly documentService: DocumentService,
    private readonly changeDetectorRef: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadDocuments();
  }

  get displayedPages(): number[] {
    if (this.totalPages <= 1) {
      return this.totalPages === 1 ? [0] : [];
    }

    const start = Math.max(0, Math.min(this.page - 2, this.totalPages - 5));
    const end = Math.min(this.totalPages, start + 5);
    return Array.from({ length: end - start }, (_, index) => start + index);
  }

  loadDocuments(page = this.page): void {
    if (page < 0 || (this.totalPages > 0 && page >= this.totalPages)) return;

    this.loading = true;
    this.errorMessage = '';

    this.documentService.findCreatedBy(page, this.size).subscribe({
      next: (response) => {
        this.documents = response.content;
        this.page = response.page;
        this.size = response.size;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
        this.first = response.first;
        this.last = response.last;
        this.loading = false;
        this.changeDetectorRef.markForCheck();
      },
      error: (error: Error) => {
        this.documents = [];
        this.errorMessage = error.message;
        this.loading = false;
        this.changeDetectorRef.markForCheck();
      },
      });
  }

  deleteDocument(document: DocumentResponse): void {
    if (this.deletingDocumentId !== null) return;

    this.deletingDocumentId = document.id;
    this.actionMessage = '';
    this.actionErrorMessage = '';

    this.documentService
      .deleteDocument(document.id)
      .pipe(
        finalize(() => {
          this.deletingDocumentId = null;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (response) => {
          this.actionMessage = this.extractMessage(response, 'Xóa văn bản thành công');
          const nextPage = this.documents.length === 1 && this.page > 0 ? this.page - 1 : this.page;
          this.totalPages = 0;
          this.loadDocuments(nextPage);
        },
        error: (error: Error) => {
          this.actionErrorMessage = this.cleanErrorMessage(error.message);
        },
      });
  }

  openCreateForm(): void {
    this.createModalOpen = true;
    this.actionMessage = '';
    this.actionErrorMessage = '';
  }

  closeCreateForm(): void {
    if (this.createLoading) return;
    this.createModalOpen = false;
    this.resetCreateForm();
  }

  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFiles = [...this.selectedFiles, ...Array.from(input.files || [])];
    input.value = '';
  }

  removeSelectedFile(index: number): void {
    this.selectedFiles = this.selectedFiles.filter((_, fileIndex) => fileIndex !== index);
  }

  createDocument(): void {
    const summary = this.createSummary.trim();

    if (!summary) {
      this.actionErrorMessage = 'Vui lòng nhập tóm tắt văn bản';
      return;
    }

    if (this.selectedFiles.length === 0) {
      this.actionErrorMessage = 'Vui lòng chọn ít nhất một file';
      return;
    }

    const request: NewDocumentRequest = {
      documentType: this.createDocumentType,
      summary,
    };

    this.createLoading = true;
    this.actionMessage = '';
    this.actionErrorMessage = '';

    this.documentService
      .createDocument(request)
      .pipe(
        switchMap((createdDocument) =>
          this.documentService.uploadDocumentFiles(createdDocument.id, this.selectedFiles).pipe(
            map(() => createdDocument),
          ),
        ),
        finalize(() => {
          this.createLoading = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (createdDocument) => {
          this.actionMessage = this.extractMessage(createdDocument, 'Tạo văn bản thành công');
          this.createModalOpen = false;
          this.resetCreateForm();
          this.page = 0;
          this.totalPages = 0;
          this.loadDocuments(0);
        },
        error: (error: Error) => {
          this.actionErrorMessage = this.cleanErrorMessage(error.message);
        },
      });
  }

  formatDateTime(value: LocalDateTimeValue | null | undefined): string {
    return value ? formatLocalDateTime(value, false) : '-';
  }

  documentCreatedAt(document: DocumentResponse): LocalDateTimeValue | null | undefined {
    return document.createdAt || document.creatdAt;
  }

  formatSelectedFileSize(fileSize: number): string {
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

  documentTypeLabel(documentType: string): string {
    const labels: Record<string, string> = {
      CONG_VAN: 'Công văn',
      THONG_BAO: 'Thông báo',
      TO_TRINH: 'Tờ trình',
    };

    return labels[documentType] || documentType;
  }

  private resetCreateForm(): void {
    this.createDocumentType = 'CONG_VAN';
    this.createSummary = '';
    this.selectedFiles = [];
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
