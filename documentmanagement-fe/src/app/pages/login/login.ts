import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';
import { LoginRequest } from '../../models/auth.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class LoginComponent {

  loginRequest: LoginRequest = {
    username: '',
    password: ''
  };

  loading = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) { }

  onLogin(): void {
    this.errorMessage = '';

    if (!this.loginRequest.username.trim()) {
      this.errorMessage = 'Username không được để trống';
      return;
    }

    if (!this.loginRequest.password.trim()) {
      this.errorMessage = 'Password không được để trống';
      return;
    }

    this.loading = true;

    this.authService.login(this.loginRequest).subscribe({
      next: (response) => {
        this.loading = false;

        console.log('Login response:', response);

        this.router.navigate(['/home']);
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = error.message;
      }
    });
  }
}
