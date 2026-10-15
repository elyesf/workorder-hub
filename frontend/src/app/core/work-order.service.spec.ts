import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { WorkOrderService } from './work-order.service';

describe('WorkOrderService', () => {
  let service: WorkOrderService;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(WorkOrderService);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('only sends filters that are set', () => {
    service.list({ status: 'OPEN', q: '  pump ' }).subscribe();

    const req = backend.expectOne((r) => r.url === '/api/work-orders');
    expect(req.request.params.get('status')).toBe('OPEN');
    expect(req.request.params.get('q')).toBe('pump');
    expect(req.request.params.get('page')).toBe('0');
    req.flush({ items: [], page: 0, size: 20, totalItems: 0, totalPages: 0 });
  });

  it('skips an empty search', () => {
    service.list({ q: '   ' }).subscribe();

    const req = backend.expectOne((r) => r.url === '/api/work-orders');
    expect(req.request.params.has('q')).toBeFalse();
    expect(req.request.params.has('status')).toBeFalse();
    req.flush({ items: [], page: 0, size: 20, totalItems: 0, totalPages: 0 });
  });

  it('uses PATCH for status changes', () => {
    service.changeStatus(7, 'DONE').subscribe();

    const req = backend.expectOne('/api/work-orders/7/status');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ status: 'DONE' });
    req.flush({});
  });
});
