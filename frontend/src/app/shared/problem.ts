import { HttpErrorResponse } from '@angular/common/http';

import { Problem } from '../core/models';

/** Turns any HTTP error into a short message for the user. */
export function errorMessage(error: unknown, fallback = 'Something went wrong. Please try again.'): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'The server is not reachable. Is the API running?';
    }
    const problem = error.error as Problem | null;
    if (problem?.errors) {
      return Object.entries(problem.errors)
        .map(([field, message]) => `${field}: ${message}`)
        .join(', ');
    }
    if (problem?.detail) {
      return problem.detail;
    }
  }
  return fallback;
}
