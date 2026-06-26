export type LocalDateTimeValue = string | number[] | null | undefined;

export function formatLocalDateTime(
  value: LocalDateTimeValue,
  includeSeconds = true,
): string {
  if (!value) {
    return 'Chưa cập nhật';
  }

  let year: number;
  let month: number;
  let day: number;
  let hour: number;
  let minute: number;
  let second: number;

  if (Array.isArray(value)) {
    [year, month, day, hour = 0, minute = 0, second = 0] = value;
  } else {
    const match = value.match(
      /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?/,
    );

    if (!match) {
      return value;
    }

    year = Number(match[1]);
    month = Number(match[2]);
    day = Number(match[3]);
    hour = Number(match[4]);
    minute = Number(match[5]);
    second = Number(match[6] || 0);
  }

  const pad = (number: number): string => String(number).padStart(2, '0');
  const date = `${pad(day)}/${pad(month)}/${year}`;
  const time = `${pad(hour)}:${pad(minute)}`;

  return includeSeconds
    ? `${date} ${time}:${pad(second)}`
    : `${date} ${time}`;
}
