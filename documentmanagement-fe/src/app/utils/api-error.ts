import { HttpErrorResponse } from '@angular/common/http';

const TECHNICAL_ERROR_PATTERNS = [
  /^error\s*:/i,
  /^internal server error$/i,
  /^unknown error$/i,
  /^http failure response/i,
  /^failed to fetch$/i,
  /^network error$/i,
  /^(bad request|unauthorized|forbidden|not found|method not allowed)$/i,
  /^(service unavailable|bad gateway|gateway timeout)$/i,
  /^bad credentials$/i,
  /^user is not authenticated$/i,
  /^notification not found$/i,
  /^<!doctype html/i,
  /^<html/i,
];

const UNREADABLE_ERROR_PATTERNS = [
  /[\r\n\t]/,
  /https?:\/\//i,
  /\b(?:exception|stack trace|sqlstate|org\.springframework|java\.)\b/i,
  /\bat\s+[\w.$]+\([^)]*\)/i,
  /^\s*[{[]/,
  /<\/?[a-z][^>]*>/i,
  /^[A-Z0-9_.:-]+$/,
  /\uFFFD/,
];

export function getApiErrorMessage(error: HttpErrorResponse, operationFallback: string): string {
  const backendMessage = extractBackendMessage(error.error);

  if (
    !backendMessage ||
    error.status === 0 ||
    backendMessage.length > 200 ||
    TECHNICAL_ERROR_PATTERNS.some((pattern) => pattern.test(backendMessage)) ||
    UNREADABLE_ERROR_PATTERNS.some((pattern) => pattern.test(backendMessage))
  ) {
    return operationFallback;
  }

  return backendMessage;
}

function extractBackendMessage(payload: unknown): string {
  if (typeof payload === 'string') {
    return payload.trim();
  }

  if (!payload || typeof payload !== 'object') {
    return '';
  }

  const errorPayload = payload as { message?: unknown; error?: unknown };
  if (typeof errorPayload.message === 'string' && errorPayload.message.trim()) {
    return errorPayload.message.trim();
  }

  if (typeof errorPayload.error === 'string' && errorPayload.error.trim()) {
    return errorPayload.error.trim();
  }

  return '';
}
