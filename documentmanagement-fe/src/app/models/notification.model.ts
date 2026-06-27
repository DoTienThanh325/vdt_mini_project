import { LocalDateTimeValue } from '../utils/local-date-time';

export interface Notification {
  id: string;
  userId: number;
  title: string;
  content: string;
  isRead: boolean;
  createdAt: LocalDateTimeValue;
}

