import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize, switchMap } from 'rxjs';
import { InterconnectedSystemResponse } from '../../models/interconnected-system.model';
import { NewOrgRequest, OrgResponse } from '../../models/organization.model';
import { InterconnectedSystemService } from '../../services/interconnected-system.service';
import { OrganizationService } from '../../services/organization.service';

@Component({
  selector: 'app-organization-registration',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './organization-registration.html',
  styleUrl: './organization-registration.css',
})
export class OrganizationRegistration implements OnInit {
  request: NewOrgRequest = this.getEmptyRequest();
  systems: InterconnectedSystemResponse[] = [];
  createdOrganization: OrgResponse | null = null;
  checkingCreatedOrganization = false;
  loadingSystems = false;
  submitting = false;
  errorMessage = '';

  constructor(
    private readonly organizationService: OrganizationService,
    private readonly interconnectedSystemService: InterconnectedSystemService,
    private readonly changeDetectorRef: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadCreatedOrganization();
  }

  get activeSystems(): InterconnectedSystemResponse[] {
    return this.systems.filter((system) => system.status === 'ACTIVE');
  }

  loadSystems(): void {
    this.loadingSystems = true;
    this.errorMessage = '';

    this.interconnectedSystemService
      .findAll()
      .pipe(
        finalize(() => {
          this.loadingSystems = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (systems) => (this.systems = systems),
        error: (error: Error) => (this.errorMessage = error.message),
      });
  }

  submit(): void {
    if (this.submitting) return;

    const request: NewOrgRequest = {
      orgCode: this.request.orgCode.trim(),
      orgName: this.request.orgName.trim(),
      address: this.request.address?.trim() || undefined,
      email: this.request.email.trim(),
      phone: this.request.phone?.trim() || undefined,
      systemId: Number(this.request.systemId),
    };

    if (!request.orgCode || !request.orgName || !request.email || !request.systemId) {
      this.errorMessage = 'Vui lòng nhập đầy đủ thông tin bắt buộc';
      return;
    }

    this.submitting = true;
    this.errorMessage = '';

    this.organizationService
      .create(request)
      .pipe(
        switchMap(() => this.organizationService.findCreated()),
        finalize(() => {
          this.submitting = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (organization) => {
          this.createdOrganization = organization;
          this.request = this.getEmptyRequest();
        },
        error: (error: Error) => (this.errorMessage = error.message),
      });
  }

  refreshStatus(): void {
    this.loadCreatedOrganization();
  }

  private loadCreatedOrganization(): void {
    this.checkingCreatedOrganization = true;
    this.errorMessage = '';

    this.organizationService
      .findCreated()
      .pipe(
        finalize(() => {
          this.checkingCreatedOrganization = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (organization) => (this.createdOrganization = organization),
        error: () => {
          this.createdOrganization = null;
          this.errorMessage = '';
          this.loadSystems();
        },
      });
  }

  private getEmptyRequest(): NewOrgRequest {
    return {
      orgCode: '',
      orgName: '',
      address: '',
      email: '',
      phone: '',
      systemId: 0,
    };
  }
}
