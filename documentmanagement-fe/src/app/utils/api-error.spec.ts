import { HttpErrorResponse } from '@angular/common/http';
import { describe, expect, it } from 'vitest';
import { getApiErrorMessage } from './api-error';

describe('getApiErrorMessage', () => {
  const fallback = 'Gửi văn bản, tài liệu không thành công';

  it('replaces backend technical errors with the operation message', () => {
    const error = new HttpErrorResponse({
      status: 500,
      error: 'Error: Connection refused',
    });

    expect(getApiErrorMessage(error, fallback)).toBe(fallback);
  });

  it('keeps a customized backend string message', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: 'Vui lòng xác minh chữ ký trước khi gửi',
    });

    expect(getApiErrorMessage(error, fallback)).toBe('Vui lòng xác minh chữ ký trước khi gửi');
  });

  it('keeps a customized backend object message', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: { message: 'Tài khoản của bạn chưa được kích hoạt' },
    });

    expect(getApiErrorMessage(error, fallback)).toBe('Tài khoản của bạn chưa được kích hoạt');
  });

  it('uses the operation message for network errors', () => {
    const error = new HttpErrorResponse({ status: 0 });

    expect(getApiErrorMessage(error, fallback)).toBe(fallback);
  });
});
