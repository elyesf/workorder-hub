export type Priority = 'LOW' | 'MEDIUM' | 'HIGH';
export type WorkOrderStatus = 'OPEN' | 'IN_PROGRESS' | 'DONE' | 'CANCELLED';

export const STATUSES: WorkOrderStatus[] = ['OPEN', 'IN_PROGRESS', 'DONE', 'CANCELLED'];
export const PRIORITIES: Priority[] = ['LOW', 'MEDIUM', 'HIGH'];

export interface AuthResponse {
  token: string;
  expiresAt: string;
  email: string;
  displayName: string;
  role: 'USER' | 'ADMIN';
}

export interface WorkOrder {
  id: number;
  title: string;
  description: string | null;
  customer: string;
  location: string | null;
  priority: Priority;
  status: WorkOrderStatus;
  allowedNextStatuses: WorkOrderStatus[];
  assignee: string | null;
  dueDate: string | null;
  overdue: boolean;
  createdBy: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface WorkOrderRequest {
  title: string;
  description: string | null;
  customer: string;
  location: string | null;
  priority: Priority;
  assignee: string | null;
  dueDate: string | null;
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
}

export interface Stats {
  byStatus: Record<WorkOrderStatus, number>;
  overdue: number;
  total: number;
}

/** Shape of the RFC 9457 problem responses the API sends on errors. */
export interface Problem {
  status: number;
  detail?: string;
  errors?: Record<string, string>;
}
