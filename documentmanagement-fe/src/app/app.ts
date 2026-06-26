import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from './services/auth';

interface DocumentMenuItem {
  label: string;
  route: string;
  roles: string[];
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  role = '';
  fullName = '';
  sidebarOpen = false;
  documentsExpanded = true;
  currentUrl = '';

  readonly documentMenuItems: DocumentMenuItem[] = [
    {
      label: 'Văn bản gửi đi',
      route: '/home/documents/outgoing',
      roles: ['MANAGER', 'LEADER', 'STAFF', 'CLERK', 'ORGADMIN'],
    },
    {
      label: 'Văn bản nhận',
      route: '/home/documents/incoming',
      roles: ['MANAGER', 'LEADER', 'STAFF', 'CLERK', 'ORGADMIN'],
    },
    {
      label: 'Văn bản đã tạo',
      route: '/home/documents/created',
      roles: ['STAFF'],
    },
  ];

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.updateLayoutState(this.router.url);
    this.router.events
      .pipe(filter((event): event is NavigationEnd => event instanceof NavigationEnd))
      .subscribe((event) => this.updateLayoutState(event.urlAfterRedirects));
  }

  get showLayout(): boolean {
    return this.currentUrl.startsWith('/home');
  }

  get isAdmin(): boolean {
    return this.role === 'ADMIN';
  }

  get isOrgAdmin(): boolean {
    return this.role === 'ORGADMIN';
  }

  get canViewDocuments(): boolean {
    return !this.isAdmin;
  }

  get visibleDocumentMenuItems(): DocumentMenuItem[] {
    return this.documentMenuItems.filter((item) => item.roles.includes(this.role));
  }

  get activeTitle(): string {
    if (this.currentUrl === '/home') return 'Trang chủ';
    if (this.currentUrl === '/home/users') return 'Người dùng';
    if (this.currentUrl.startsWith('/home/users/')) return 'Chi tiết người dùng';
    if (this.currentUrl === '/home/interconnected-systems') return 'Hệ thống liên thông';
    if (this.currentUrl === '/home/interconnected-systems/new') return 'Tạo hệ thống liên thông';
    if (this.currentUrl.startsWith('/home/interconnected-systems/')) return 'Chi tiết hệ thống liên thông';
    if (this.currentUrl === '/home/organizations') return 'Đơn vị liên thông';
    if (this.currentUrl.startsWith('/home/organizations/')) return 'Chi tiết đơn vị liên thông';
    if (this.currentUrl === '/home/organization-registration') return 'Đăng ký đơn vị liên thông';
    if (this.currentUrl === '/home/documents/outgoing') return 'Văn bản gửi đi';
    if (this.currentUrl === '/home/documents/incoming') return 'Văn bản nhận';
    if (this.currentUrl === '/home/documents/created') return 'Văn bản đã tạo';
    return 'Hệ thống';
  }

  toggleDocuments(): void {
    this.documentsExpanded = !this.documentsExpanded;
  }

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  closeSidebar(): void {
    this.sidebarOpen = false;
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  private updateLayoutState(url: string): void {
    this.currentUrl = url.split('?')[0];
    this.role = (this.authService.getRole() || '').trim().toUpperCase().replace(/^ROLE_/, '');
    this.fullName =
      localStorage.getItem('fullName') ||
      localStorage.getItem('username') ||
      'Người dùng';

    if (this.currentUrl.startsWith('/home/documents')) {
      this.documentsExpanded = true;
    }
  }
}
