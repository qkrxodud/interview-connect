import { clsx, type ClassValue } from 'clsx';

export function cn(...inputs: ClassValue[]) {
  return clsx(inputs);
}

export function formatDate(dateString: string): string {
  const date = new Date(dateString);
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

export function formatDateTime(dateString: string): string {
  const date = new Date(dateString);
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: 'numeric',
    minute: 'numeric',
  });
}

export function getDifficultyText(difficulty: number): string {
  const levels = ['매우 쉬움', '쉬움', '보통', '어려움', '매우 어려움'];
  return levels[difficulty - 1] || '알 수 없음';
}

export function getResultText(result: string): string {
  const results: { [key: string]: string } = {
    PASS: '합격',
    FAIL: '불합격',
    PENDING: '대기중',
  };
  return results[result] || '알 수 없음';
}

export function truncateText(text: string, maxLength: number): string {
  if (text.length <= maxLength) {
    return text;
  }
  return text.substring(0, maxLength) + '...';
}