import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { OrgResponse } from '../../models/organization.model';
import { RegisterRequest, RoleResponse, UserResponse } from '../../models/user.model';
import { OrganizationService } from '../../services/organization.service';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-user',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user.html',
  styleUrl: './user.css',
})
export class User implements OnInit {
  users: UserResponse[] = [];
  roles: RoleResponse[] = [];
  organizations: OrgResponse[] = [];
  updatingUserId: number | null = null;
  page = 0;
  size = 10;
  totalElements = 0;
  totalPages = 0;
  first = true;
  last = true;
  loading = false;
  loadingRoles = false;
  loadingOrganizations = false;
  savingUser = false;
  showCreateForm = false;
  errorMessage = '';
  createErrorMessage = '';
  successMessage = '';
  orgPage = 0;
  orgSize = 10;
  orgTotalPages = 0;
  orgFirst = true;
  orgLast = true;
  registerForm: RegisterRequest = this.getEmptyRegisterForm();

  constructor(
    private readonly userService: UserService,
    private readonly organizationService: OrganizationService,
    private readonly router: Router,
    private readonly changeDetectorRef: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  get displayedPages(): number[] {
    if (this.totalPages <= 1) {
      return this.totalPages === 1 ? [0] : [];
    }

    const start = Math.max(0, Math.min(this.page - 2, this.totalPages - 5));
    const end = Math.min(this.totalPages, start + 5);
    return Array.from({ length: end - start }, (_, index) => start + index);
  }

  loadUsers(page = this.page): void {
    if (page < 0 || (this.totalPages > 0 && page >= this.totalPages)) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.userService
      .findAll(page, this.size)
      .pipe(
        finalize(() => {
          this.loading = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (response) => {
          this.users = response.content;
          this.page = response.page;
          this.size = response.size;
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.first = response.first;
          this.last = response.last;
        },
        error: (error: Error) => {
          this.users = [];
          this.errorMessage = error.message;
        },
      });
  }

  viewDetail(id: number): void {
    this.router.navigate(['/home/users', id]);
  }

  updateStatus(user: UserResponse): void {
    if (this.updatingUserId !== null) return;

    this.updatingUserId = user.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.userService
      .updateStatus(user.id)
      .pipe(
        finalize(() => {
          this.updatingUserId = null;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: () => this.loadUsers(this.page),
        error: (error: Error) => (this.errorMessage = error.message),
      });
  }

  openCreateForm(): void {
    this.showCreateForm = true;
    this.registerForm = this.getEmptyRegisterForm();
    this.createErrorMessage = '';
    this.successMessage = '';
    this.loadRoles();
    this.loadOrganizationsForForm(0);
  }

  closeCreateForm(): void {
    if (this.savingUser) return;

    this.showCreateForm = false;
    this.createErrorMessage = '';
  }

  loadRoles(): void {
    this.loadingRoles = true;
    this.createErrorMessage = '';

    this.userService
      .findRoles()
      .pipe(
        finalize(() => {
          this.loadingRoles = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (roles) => (this.roles = roles),
        error: (error: Error) => (this.createErrorMessage = error.message),
      });
  }

  loadOrganizationsForForm(page = this.orgPage): void {
    if (page < 0 || (this.orgTotalPages > 0 && page >= this.orgTotalPages)) {
      return;
    }

    this.loadingOrganizations = true;
    this.createErrorMessage = '';

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
          this.createErrorMessage = error.message;
        },
      });
  }

  submitCreateForm(): void {
    if (this.savingUser) return;

    const request: RegisterRequest = {
      username: this.registerForm.username.trim(),
      password: this.registerForm.password,
      fullName: this.registerForm.fullName.trim(),
      phone: this.registerForm.phone?.trim() || undefined,
      email: this.registerForm.email.trim(),
      roleId: Number(this.registerForm.roleId),
      organizationId: Number(this.registerForm.organizationId),
    };

    if (
      !request.username ||
      !request.password ||
      !request.fullName ||
      !request.email ||
      !request.roleId ||
      !request.organizationId
    ) {
      this.createErrorMessage = 'Vui lòng nhập đầy đủ thông tin bắt buộc';
      return;
    }

    this.savingUser = true;
    this.createErrorMessage = '';
    this.successMessage = '';

    this.userService
      .register(request)
      .pipe(
        finalize(() => {
          this.savingUser = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (response) => {
          this.successMessage = response.message || 'Tạo người dùng thành công';
          this.showCreateForm = false;
          this.loadUsers(this.page);
        },
        error: (error: Error) => (this.createErrorMessage = error.message),
      });
  }

  isActive(status: string): boolean {
    return status?.toUpperCase() === 'ACTIVE';
  }

  statusClass(status: string): string {
    return status?.toUpperCase() === 'ACTIVE' ? 'active' : 'inactive';
  }

  private getEmptyRegisterForm(): RegisterRequest {
    return {
      username: '',
      password: '',
      fullName: '',
      phone: '',
      email: '',
      roleId: 0,
      organizationId: 0,
    };
  }
}
