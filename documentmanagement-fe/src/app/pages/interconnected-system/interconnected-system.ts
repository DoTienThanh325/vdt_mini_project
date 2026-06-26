import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { InterconnectedSystemResponse } from '../../models/interconnected-system.model';
import { InterconnectedSystemService } from '../../services/interconnected-system.service';
import { formatLocalDateTime, LocalDateTimeValue } from '../../utils/local-date-time';

@Component({
  selector: 'app-interconnected-system',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './interconnected-system.html',
  styleUrl: './interconnected-system.css',
})
export class InterconnectedSystem implements OnInit {
  systems: InterconnectedSystemResponse[] = [];
  updatingSystemId: number | null = null;
  loading = false;
  errorMessage = '';

  constructor(
    private readonly service: InterconnectedSystemService,
    private readonly router: Router,
    private readonly changeDetectorRef: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadSystems();
  }

  loadSystems(): void {
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
        next: (systems) => (this.systems = systems),
        error: (error: Error) => {
          this.systems = [];
          this.errorMessage = error.message;
        },
      });
  }

  viewDetail(system: InterconnectedSystemResponse): void {
    this.router.navigate(['/home/interconnected-systems', system.systemCode], {
      state: { system },
    });
  }

  createSystem(): void {
    this.router.navigate(['/home/interconnected-systems/new']);
  }

  updateStatus(system: InterconnectedSystemResponse): void {
    if (this.updatingSystemId !== null) return;

    this.updatingSystemId = system.id;
    this.errorMessage = '';
    const status = system.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';

    this.service
      .update(system.id, { status })
      .pipe(
        finalize(() => {
          this.updatingSystemId = null;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: () => this.loadSystems(),
        error: (error: Error) => (this.errorMessage = error.message),
      });
  }

  statusClass(status: string): string {
    return status.toLowerCase();
  }

  formatDateTime(value: LocalDateTimeValue): string {
    return formatLocalDateTime(value, false);
  }
}
