import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { UserDetailResponse } from '../../models/user.model';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-detail.html',
  styleUrl: './user-detail.css',
})
export class UserDetail implements OnInit {
  user: UserDetailResponse | null = null;
  userId = 0;
  loading = true;
  errorMessage = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly userService: UserService,
    private readonly changeDetectorRef: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    const role = (localStorage.getItem('role') || '')
      .toUpperCase()
      .replace(/^ROLE_/, '');

    if (role !== 'ADMIN') {
      this.router.navigate(['/home']);
      return;
    }

    this.userId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadUser();
  }

  loadUser(): void {
    if (!Number.isInteger(this.userId) || this.userId <= 0) {
      this.loading = false;
      this.errorMessage = 'ID người dùng không hợp lệ';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.userService
      .findById(this.userId)
      .pipe(
        finalize(() => {
          this.loading = false;
          this.changeDetectorRef.markForCheck();
        }),
      )
      .subscribe({
        next: (user) => (this.user = user),
        error: (error: Error) => (this.errorMessage = error.message),
      });
  }

  get isActive(): boolean {
    return this.user?.status?.toUpperCase() === 'ACTIVE';
  }

  goBack(): void {
    this.router.navigate(['/home/users']);
  }
}
