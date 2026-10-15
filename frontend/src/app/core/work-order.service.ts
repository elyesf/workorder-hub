import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { PageResponse, Stats, WorkOrder, WorkOrderRequest, WorkOrderStatus } from './models';

export interface WorkOrderQuery {
  status?: WorkOrderStatus | null;
  q?: string | null;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class WorkOrderService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/work-orders';

  list(query: WorkOrderQuery = {}): Observable<PageResponse<WorkOrder>> {
    let params = new HttpParams()
      .set('page', query.page ?? 0)
      .set('size', query.size ?? 20);
    if (query.status) {
      params = params.set('status', query.status);
    }
    if (query.q?.trim()) {
      params = params.set('q', query.q.trim());
    }
    return this.http.get<PageResponse<WorkOrder>>(this.baseUrl, { params });
  }

  stats(): Observable<Stats> {
    return this.http.get<Stats>(`${this.baseUrl}/stats`);
  }

  get(id: number): Observable<WorkOrder> {
    return this.http.get<WorkOrder>(`${this.baseUrl}/${id}`);
  }

  create(request: WorkOrderRequest): Observable<WorkOrder> {
    return this.http.post<WorkOrder>(this.baseUrl, request);
  }

  update(id: number, request: WorkOrderRequest): Observable<WorkOrder> {
    return this.http.put<WorkOrder>(`${this.baseUrl}/${id}`, request);
  }

  changeStatus(id: number, status: WorkOrderStatus): Observable<WorkOrder> {
    return this.http.patch<WorkOrder>(`${this.baseUrl}/${id}/status`, { status });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
