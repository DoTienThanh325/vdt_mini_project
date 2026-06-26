import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { DocumentResponse } from '../../models/document.model';
import { DocumentService } from '../../services/document.service';
import { formatLocalDateTime, LocalDateTimeValue } from '../../utils/local-date-time';

type IncomingStatus = '' | 'SENT' | 'RECEIVED' | 'RESPONDED';

@Component({
  selector: 'app-incoming-document',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './incoming-document.html',
  styleUrl: './incoming-document.css',
})
export class IncomingDocument implements OnInit {
  readonly statuses: Exclude<IncomingStatus, ''>[] = ['SENT', 'RECEIVED', 'RESPONDED'];

  documents: DocumentResponse[] = [];
  selectedStatus: IncomingStatus = '';
  page = 0;
  size = 10;
  totalElements = 0;
  totalPages = 0;
  first = true;
  last = true;
  loading = false;
  errorMessage = '';

  constructor(
    private readonly documentService: DocumentService,
    private readonly router: Router,
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

  get role(): string {
    return (localStorage.getItem('role') || '').trim().toUpperCase().replace(/^ROLE_/, '');
  }

  loadDocuments(page = this.page): void {
    if (page < 0 || (this.totalPages > 0 && page >= this.totalPages)) return;

    this.loading = true;
    this.errorMessage = '';

    const request$ = this.selectedStatus
      ? this.documentService.findIncomingByStatus(this.selectedStatus, page, this.size)
      : this.documentService.findIncoming(page, this.size);

    request$.subscribe({
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

  onStatusChange(): void {
    this.page = 0;
    this.totalPages = 0;
    this.loadDocuments(0);
  }

  canViewDetail(document: DocumentResponse): boolean {
    return document.status !== 'SENT' || this.role === 'CLERK';
  }

  viewDetail(document: DocumentResponse): void {
    if (!this.canViewDetail(document)) {
      return;
    }

    this.router.navigate(['/home/documents/incoming', document.id], {
      state: { document },
    });
  }

  formatDateTime(value: LocalDateTimeValue | null | undefined): string {
    return value ? formatLocalDateTime(value, false) : '-';
  }

  documentCreatedAt(document: DocumentResponse): LocalDateTimeValue | null | undefined {
    return document.createdAt || document.creatdAt;
  }

  statusClass(status: string): string {
    return status.toLowerCase();
  }

  statusLabel(status: string): string {
    const labels: Record<string, string> = {
      SENT: 'Đã gửi',
      RECEIVED: 'Đã nhận',
      RESPONDED: 'Đã phản hồi',
      FAILED: 'Gửi bị lỗi',
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
}
