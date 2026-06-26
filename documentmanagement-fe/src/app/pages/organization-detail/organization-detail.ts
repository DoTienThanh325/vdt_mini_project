import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { forkJoin, finalize } from 'rxjs';
import { InterconnectedSystemResponse } from '../../models/interconnected-system.model';
import { OrgResponse, UpdateOrgRequest } from '../../models/organization.model';
import { InterconnectedSystemService } from '../../services/interconnected-system.service';
import { OrganizationService } from '../../services/organization.service';
import { formatLocalDateTime, LocalDateTimeValue } from '../../utils/local-date-time';

interface OrganizationEditForm {
  orgCode: string;
  address: string;
  email: string;
  phone: string;
  systemId: number | null;
}

@Component({
  selector: 'app-organization-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './organization-detail.html',
  styleUrl: './organization-detail.css',
})
export class OrganizationDetail implements OnInit {
  organization: OrgResponse | null = null;
  systems: InterconnectedSystemResponse[] = [];
  orgCode = '';
  editing = false;
  loading = true;
  saving = false;
  activating = false;
  errorMessage = '';
  successMessage = '';

  form: OrganizationEditForm = {
    orgCode: '',
    address: '',
    email: '',
    phone: '',
    systemId: null,
  };

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly organizationService: OrganizationService,
    private readonly systemService: InterconnectedSystemService,
    private readonly changeDetectorRef: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.orgCode = this.route.snapshot.paramMap.get('orgCode') || '';
    const navigationOrganization = history.state?.organization as
      | OrgResponse
      | undefined;

    if (navigationOrganization?.orgCode === this.orgCode) {
      this.setOrganization(navigationOrganization);
      this.loadSystems();
      return;
    }

    this.reloadData();
  }

  reloadData(): void {
    this.loading = true;
    this.errorMessage = '';

    forkJoin({
      organization: this.organizationService.findByCode(this.orgCode),
      systems: this.systemService.findAll(),
    })
      .pipe(
        finalize(() => {
          this.loading = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: ({ organization, systems }) => {
          this.systems = systems;
          if (!organization) {
            this.errorMessage = 'Không tìm thấy đơn vị liên thông';
            return;
          }
          this.setOrganization(organization);
        },
        error: (error: Error) => (this.errorMessage = error.message),
      });
  }

  loadSystems(): void {
    this.loading = true;
    this.systemService
      .findAll()
      .pipe(
        finalize(() => {
          this.loading = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (systems) => (this.systems = systems),
        error: (error: Error) => (this.errorMessage = error.message),
      });
  }

  startEdit(): void {
    if (!this.organization) return;
    this.syncForm();
    this.editing = true;
    this.errorMessage = '';
    this.successMessage = '';
  }

  cancelEdit(): void {
    this.editing = false;
    this.errorMessage = '';
    this.syncForm();
  }

  save(): void {
    if (!this.organization || this.saving) return;

    const request: UpdateOrgRequest = {};
    const orgCode = this.form.orgCode.trim();
    const address = this.form.address.trim();
    const email = this.form.email.trim();
    const phone = this.form.phone.trim();

    if (!orgCode || !address || !email || !phone || !this.form.systemId) {
      this.errorMessage = 'Vui lòng nhập đầy đủ thông tin';
      return;
    }

    if (orgCode !== this.organization.orgCode) request.orgCode = orgCode;
    if (address !== this.organization.address) request.address = address;
    if (email !== this.organization.email) request.email = email;
    if (phone !== this.organization.phone) request.phone = phone;
    if (this.form.systemId !== this.organization.systemId) {
      request.systemId = this.form.systemId;
    }

    if (Object.keys(request).length === 0) {
      this.editing = false;
      return;
    }

    this.saving = true;
    this.errorMessage = '';

    this.organizationService
      .update(this.organization.id, request)
      .pipe(
        finalize(() => {
          this.saving = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (organization) => {
          const oldCode = this.orgCode;
          this.setOrganization(organization);
          this.orgCode = organization.orgCode;
          this.editing = false;
          this.successMessage =
            organization.message || 'Cập nhật đơn vị liên thông thành công';

          if (oldCode !== organization.orgCode) {
            this.router.navigate(
              ['/home/organizations', organization.orgCode],
              { replaceUrl: true, state: { organization } },
            );
          }
        },
        error: (error: Error) => (this.errorMessage = error.message),
      });
  }

  activateOrganization(): void {
    if (!this.organization || this.activating || this.organization.status === 'ACTIVE') {
      return;
    }

    this.activating = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.organizationService
      .activate(this.organization.id)
      .pipe(
        finalize(() => {
          this.activating = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (organization) => {
          this.setOrganization(organization);
          this.successMessage =
            organization.message || 'Kích hoạt đơn vị liên thông thành công';
        },
        error: (error: Error) => {
          this.errorMessage = error.message.replace(/^Error:\s*/i, '');
        },
      });
  }

  goBack(): void {
    this.router.navigate(['/home/organizations']);
  }

  formatDateTime(value: LocalDateTimeValue): string {
    return formatLocalDateTime(value);
  }

  statusClass(status: string): string {
    return status.toLowerCase();
  }

  private setOrganization(organization: OrgResponse): void {
    this.organization = organization;
    this.syncForm();
  }

  private syncForm(): void {
    if (!this.organization) return;
    this.form = {
      orgCode: this.organization.orgCode,
      address: this.organization.address,
      email: this.organization.email,
      phone: this.organization.phone,
      systemId: this.organization.systemId,
    };
  }
}
