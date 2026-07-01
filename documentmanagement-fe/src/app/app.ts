import { CommonModule } from '@angular/common';
import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Subject, filter, takeUntil } from 'rxjs';
import { Notification } from './models/notification.model';
import { AuthService } from './services/auth';
import { NotificationService } from './services/notification.service';
import { formatLocalDateTime } from './utils/local-date-time';

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
export class App implements OnInit, OnDestroy {
  role = '';
  fullName = '';
  username = '';
  sidebarOpen = false;
  documentsExpanded = true;
  currentUrl = '';
  notifications: Notification[] = [];
  selectedNotification: Notification | null = null;
  notificationError = '';
  markingNotificationId = '';
  darkMode = false;

  private notificationsActive = false;
  private readonly destroy$ = new Subject<void>();

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
    private readonly notificationService: NotificationService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    this.initializeTheme();
    this.updateLayoutState(this.router.url);
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntil(this.destroy$),
      )
      .subscribe((event) => this.updateLayoutState(event.urlAfterRedirects));

    this.notificationService.notifications$
      .pipe(takeUntil(this.destroy$))
      .subscribe((notifications) => (this.notifications = notifications));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    this.notificationService.disconnect();
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

  get unreadNotifications(): Notification[] {
    return this.notifications.filter((notification) => !notification.isRead);
  }

  get unreadCountLabel(): string {
    const count = this.unreadNotifications.length;
    return count > 99 ? '99+' : String(count);
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

  toggleTheme(): void {
    this.darkMode = !this.darkMode;
    this.applyTheme();
  }

  closeSidebar(): void {
    this.sidebarOpen = false;
  }

  openNotification(notification: Notification): void {
    if (this.markingNotificationId) {
      return;
    }

    this.selectedNotification = notification;
    this.notificationError = '';

    if (notification.isRead) {
      return;
    }

    this.markingNotificationId = notification.id;
    this.notificationService
      .markAsRead(notification.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedNotification) => {
          this.selectedNotification = updatedNotification;
          this.markingNotificationId = '';
        },
        error: (error: Error) => {
          this.notificationError = error.message;
          this.markingNotificationId = '';
        },
      });
  }

  closeNotificationDetail(): void {
    this.selectedNotification = null;
  }

  formatNotificationDate(value: Notification['createdAt']): string {
    return formatLocalDateTime(value);
  }

  @HostListener('document:keydown.escape')
  closeNotificationDetailOnEscape(): void {
    this.closeNotificationDetail();
  }

  logout(): void {
    this.notificationService.disconnect();
    this.notificationService.clear();
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  private updateLayoutState(url: string): void {
    this.currentUrl = url.split('?')[0];
    this.role = (this.authService.getRole() || '').trim().toUpperCase().replace(/^ROLE_/, '');
    this.username = localStorage.getItem('username') || '';
    this.fullName =
      localStorage.getItem('fullName') ||
      this.username ||
      'Người dùng';

    if (this.currentUrl.startsWith('/home/documents')) {
      this.documentsExpanded = true;
    }

    this.syncNotifications();
  }

  private syncNotifications(): void {
    if (this.showLayout && !this.notificationsActive) {
      this.notificationsActive = true;
      this.notificationError = '';
      this.notificationService
        .loadToday()
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          error: (error: Error) => (this.notificationError = error.message),
        });
      this.notificationService.connect();
      return;
    }

    if (!this.showLayout && this.notificationsActive) {
      this.notificationsActive = false;
      this.notificationService.disconnect();
      this.notificationService.clear();
    }
  }

  private initializeTheme(): void {
    const savedTheme = localStorage.getItem('theme');
    const prefersDark =
      typeof window.matchMedia === 'function' &&
      window.matchMedia('(prefers-color-scheme: dark)').matches;
    this.darkMode =
      savedTheme === 'dark' ||
      (savedTheme === null && prefersDark);
    this.applyTheme();
  }

  private applyTheme(): void {
    const theme = this.darkMode ? 'dark' : 'light';
    document.documentElement.dataset['theme'] = theme;
    document.documentElement.style.colorScheme = theme;
    localStorage.setItem('theme', theme);
  }
}
