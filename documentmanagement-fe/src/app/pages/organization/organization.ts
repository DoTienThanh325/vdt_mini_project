import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { OrgResponse } from '../../models/organization.model';
import { OrganizationService } from '../../services/organization.service';
import { formatLocalDateTime, LocalDateTimeValue } from '../../utils/local-date-time';

@Component({
  selector: 'app-organization',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './organization.html',
  styleUrl: './organization.css',
})
export class Organization implements OnInit {
  selectedStatus: '' | 'ACTIVE' | 'INACTIVE' = '';
  organizations: OrgResponse[] = [];
  updatingOrganizationId: number | null = null;
  deletingOrganizationId: number | null = null;
  page = 0;
  size = 10;
  totalElements = 0;
  totalPages = 0;
  first = true;
  last = true;
  loading = false;
  errorMessage = '';

  constructor(
    private readonly service: OrganizationService,
    private readonly router: Router,
    private readonly changeDetectorRef: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadOrganizations();
  }

  get displayedPages(): number[] {
    if (this.totalPages <= 1) {
      return this.totalPages === 1 ? [0] : [];
    }

    const start = Math.max(0, Math.min(this.page - 2, this.totalPages - 5));
    const end = Math.min(this.totalPages, start + 5);
    return Array.from({ length: end - start }, (_, index) => start + index);
  }

  loadOrganizations(page = this.page): void {
    if (page < 0 || (this.totalPages > 0 && page >= this.totalPages)) return;

    this.loading = true;
    this.errorMessage = '';

    const request$ = this.selectedStatus
      ? this.service.findByStatus(this.selectedStatus, page, this.size)
      : this.service.findAll(page, this.size);

    request$
      .pipe(
        finalize(() => {
          this.loading = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (response) => {
          this.organizations = response.content;
          this.page = response.page;
          this.size = response.size;
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.first = response.first;
          this.last = response.last;
        },
        error: (error: Error) => {
          this.organizations = [];
          this.errorMessage = error.message;
        },
      });
  }

  onStatusChange(): void {
    this.page = 0;
    this.totalPages = 0;
    this.loadOrganizations(0);
  }

  viewDetail(organization: OrgResponse): void {
    this.router.navigate(['/home/organizations', organization.orgCode], {
      state: { organization },
    });
  }

  softDelete(organization: OrgResponse): void {
    if (
      this.deletingOrganizationId !== null ||
      this.updatingOrganizationId !== null
    ) {
      return;
    }

    this.deletingOrganizationId = organization.id;
    this.errorMessage = '';

    this.service
      .softDelete(organization.id)
      .pipe(
        finalize(() => {
          this.deletingOrganizationId = null;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: () => this.loadOrganizations(this.page),
        error: (error: Error) => (this.errorMessage = error.message),
      });
  }

  updateStatus(organization: OrgResponse): void {
    if (
      organization.status === 'DELETED' ||
      this.updatingOrganizationId !== null ||
      this.deletingOrganizationId !== null
    ) {
      return;
    }

    this.updatingOrganizationId = organization.id;
    this.errorMessage = '';

    this.service
      .activate(organization.id)
      .pipe(
        finalize(() => {
          this.updatingOrganizationId = null;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: () => this.loadOrganizations(this.page),
        error: (error: Error) => (this.errorMessage = error.message),
      });
  }

  isActive(status: string): boolean {
    return status === 'ACTIVE';
  }

  formatDateTime(value: LocalDateTimeValue): string {
    return formatLocalDateTime(value, false);
  }

  statusClass(status: string): string {
    return status.toLowerCase();
  }
}
