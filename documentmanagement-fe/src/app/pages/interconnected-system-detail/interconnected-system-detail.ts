import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import {
  InterconnectedSystemResponse,
} from '../../models/interconnected-system.model';
import { InterconnectedSystemService } from '../../services/interconnected-system.service';
import { formatLocalDateTime, LocalDateTimeValue } from '../../utils/local-date-time';

@Component({
  selector: 'app-interconnected-system-detail',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './interconnected-system-detail.html',
  styleUrl: './interconnected-system-detail.css',
})
export class InterconnectedSystemDetail implements OnInit {
  system: InterconnectedSystemResponse | null = null;
  systemCode = '';
  endpointUrl = '';
  editingEndpoint = false;
  loading = true;
  savingEndpoint = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly service: InterconnectedSystemService,
    private readonly changeDetectorRef: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.systemCode = this.route.snapshot.paramMap.get('systemCode') || '';
    const navigationSystem = history.state?.system as
      | InterconnectedSystemResponse
      | undefined;

    if (navigationSystem?.systemCode === this.systemCode) {
      this.setSystem(navigationSystem);
      this.loading = false;
      return;
    }

    this.reloadSystem();
  }

  reloadSystem(): void {
    this.loading = true;
    this.errorMessage = '';

    this.service
      .findAll()
      .pipe(
        finalize(() => {
          this.loading = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (systems) => {
          const system = systems.find(
            (item) => item.systemCode === this.systemCode,
          );

          if (!system) {
            this.errorMessage = 'Không tìm thấy hệ thống liên thông';
            return;
          }

          this.setSystem(system);
        },
        error: (error: Error) => (this.errorMessage = error.message),
      });
  }

  startEndpointEdit(): void {
    if (!this.system) return;

    this.endpointUrl = this.system.endpointUrl;
    this.editingEndpoint = true;
    this.errorMessage = '';
    this.successMessage = '';
  }

  cancelEndpointEdit(): void {
    this.editingEndpoint = false;
    this.errorMessage = '';
  }

  saveEndpoint(): void {
    if (!this.system || this.savingEndpoint) return;

    const endpointUrl = this.endpointUrl.trim();

    if (!endpointUrl) {
      this.errorMessage = 'Endpoint URL không được để trống';
      return;
    }

    if (endpointUrl === this.system.endpointUrl) {
      this.editingEndpoint = false;
      return;
    }

    this.savingEndpoint = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.service
      .update(this.system.id, { endpointUrl })
      .pipe(
        finalize(() => {
          this.savingEndpoint = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (updatedSystem) => {
          this.setSystem(updatedSystem);
          this.editingEndpoint = false;
          this.successMessage =
            updatedSystem.message || 'Cập nhật endpoint thành công';
        },
        error: (error: Error) => (this.errorMessage = error.message),
      });
  }

  goBack(): void {
    this.router.navigate(['/home/interconnected-systems']);
  }

  formatDateTime(value: LocalDateTimeValue): string {
    return formatLocalDateTime(value);
  }

  statusClass(status: string): string {
    return status.toLowerCase();
  }

  private setSystem(system: InterconnectedSystemResponse): void {
    this.system = system;
    this.endpointUrl = system.endpointUrl;
  }
}
