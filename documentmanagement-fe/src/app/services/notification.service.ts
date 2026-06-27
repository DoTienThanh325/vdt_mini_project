import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { BehaviorSubject, Observable, catchError, tap, throwError } from 'rxjs';
import { Notification } from '../models/notification.model';
import { getApiErrorMessage } from '../utils/api-error';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private readonly apiUrl = 'http://localhost:8081/api/notifications';
  private readonly websocketUrl = 'ws://localhost:8081/ws';
  private readonly notificationsSubject = new BehaviorSubject<Notification[]>([]);

  private stompClient?: Client;
  private notificationSubscription?: StompSubscription;

  readonly notifications$ = this.notificationsSubject.asObservable();

  constructor(private readonly http: HttpClient) {}

  loadToday(): Observable<Notification[]> {
    return this.http
      .get<Notification[]>(`${this.apiUrl}/today`, {
        headers: this.getAuthorizationHeaders(),
      })
      .pipe(
        tap((notifications) => {
          const currentNotifications = this.notificationsSubject.value;
          const currentIds = new Set(currentNotifications.map((notification) => notification.id));
          this.notificationsSubject.next([
            ...currentNotifications,
            ...notifications.filter((notification) => !currentIds.has(notification.id)),
          ]);
        }),
        catchError((error) => this.handleError(error, 'Tải thông báo không thành công')),
      );
  }

  markAsRead(notificationId: string): Observable<Notification> {
    return this.http
      .patch<Notification>(
        `${this.apiUrl}/${encodeURIComponent(notificationId)}/status`,
        { isRead: true },
        { headers: this.getAuthorizationHeaders() },
      )
      .pipe(
        tap((updatedNotification) => this.upsert(updatedNotification)),
        catchError((error) =>
          this.handleError(error, 'Cập nhật trạng thái thông báo không thành công'),
        ),
      );
  }

  connect(): void {
    const token = localStorage.getItem('accessToken');
    if (!token || this.stompClient?.active) {
      return;
    }

    const tokenType = localStorage.getItem('tokenType') || 'Bearer';
    this.stompClient = new Client({
      brokerURL: this.websocketUrl,
      connectHeaders: {
        Authorization: `${tokenType} ${token}`,
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        this.notificationSubscription?.unsubscribe();
        this.notificationSubscription = this.stompClient?.subscribe(
          '/user/queue/notifications',
          (message) => this.handleSocketMessage(message),
        );
      },
      onStompError: (frame) => {
        console.error('Không thể kết nối kênh thông báo:', frame.headers['message']);
      },
      onWebSocketError: () => {
        console.error('Kết nối WebSocket thông báo gặp lỗi.');
      },
    });

    this.stompClient.activate();
  }

  disconnect(): void {
    this.notificationSubscription?.unsubscribe();
    this.notificationSubscription = undefined;
    void this.stompClient?.deactivate();
    this.stompClient = undefined;
  }

  clear(): void {
    this.notificationsSubject.next([]);
  }

  private handleSocketMessage(message: IMessage): void {
    try {
      this.upsert(JSON.parse(message.body) as Notification);
    } catch {
      console.error('Payload thông báo từ WebSocket không hợp lệ.');
    }
  }

  private upsert(notification: Notification): void {
    const currentNotifications = this.notificationsSubject.value;
    const existingIndex = currentNotifications.findIndex((item) => item.id === notification.id);

    if (existingIndex < 0) {
      this.notificationsSubject.next([notification, ...currentNotifications]);
      return;
    }

    const updatedNotifications = [...currentNotifications];
    updatedNotifications[existingIndex] = notification;
    this.notificationsSubject.next(updatedNotifications);
  }

  private getAuthorizationHeaders(): HttpHeaders {
    const token = localStorage.getItem('accessToken');
    const tokenType = localStorage.getItem('tokenType') || 'Bearer';

    return token ? new HttpHeaders({ Authorization: `${tokenType} ${token}` }) : new HttpHeaders();
  }

  private handleError(error: HttpErrorResponse, operationFallback: string): Observable<never> {
    return throwError(() => new Error(getApiErrorMessage(error, operationFallback)));
  }
}
