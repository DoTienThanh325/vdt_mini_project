import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { NewInterconnectedSystemRequest } from '../../models/interconnected-system.model';
import { InterconnectedSystemService } from '../../services/interconnected-system.service';

@Component({
  selector: 'app-interconnected-system-create',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './interconnected-system-create.html',
  styleUrl: './interconnected-system-create.css',
})
export class InterconnectedSystemCreate {
  request: NewInterconnectedSystemRequest = {
    systemCode: '',
    systemName: '',
    endpointUrl: '',
    apiKey: '',
  };

  submitting = false;
  errorMessage = '';

  constructor(
    private readonly service: InterconnectedSystemService,
    private readonly router: Router,
    private readonly changeDetectorRef: ChangeDetectorRef,
  ) {}

  submit(): void {
    this.errorMessage = '';

    const request: NewInterconnectedSystemRequest = {
      systemCode: this.request.systemCode.trim(),
      systemName: this.request.systemName.trim(),
      endpointUrl: this.request.endpointUrl.trim(),
      apiKey: this.request.apiKey.trim(),
    };

    if (!request.systemCode || !request.systemName || !request.endpointUrl || !request.apiKey) {
      this.errorMessage = 'Vui lòng nhập đầy đủ thông tin hệ thống';
      return;
    }

    this.submitting = true;

    this.service
      .create(request)
      .pipe(
        finalize(() => {
          this.submitting = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (system) => {
          this.router.navigate(
            ['/home/interconnected-systems', system.systemCode],
            { state: { system } },
          );
        },
        error: (error: Error) => {
          this.errorMessage = error.message;
        },
      });
  }

  cancel(): void {
    this.router.navigate(['/home/interconnected-systems']);
  }
}
