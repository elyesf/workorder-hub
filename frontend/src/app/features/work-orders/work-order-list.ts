import { DatePipe, LowerCasePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

import { PageResponse, STATUSES, Stats, WorkOrder, WorkOrderStatus } from '../../core/models';
import { WorkOrderService } from '../../core/work-order.service';
import { errorMessage } from '../../shared/problem';
import { StatusLabelPipe } from '../../shared/status-label.pipe';

@Component({
  selector: 'app-work-order-list',
  imports: [RouterLink, FormsModule, DatePipe, LowerCasePipe, StatusLabelPipe],
  templateUrl: './work-order-list.html',
})
export class WorkOrderList implements OnInit {
  private readonly service = inject(WorkOrderService);
  private readonly searchInput = new Subject<string>();

  readonly statuses = STATUSES;
  readonly page = signal<PageResponse<WorkOrder> | null>(null);
  readonly stats = signal<Stats | null>(null);
  readonly error = signal<string | null>(null);
  readonly loading = signal(false);

  status: WorkOrderStatus | null = null;
  search = '';
  pageIndex = 0;

  constructor() {
    // Wait until the user stops typing before calling the API
    this.searchInput
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe(() => {
        this.pageIndex = 0;
        this.load();
      });
  }

  ngOnInit(): void {
    this.load();
  }

  onSearch(value: string): void {
    this.searchInput.next(value);
  }

  filterByStatus(status: WorkOrderStatus | null): void {
    this.status = status;
    this.pageIndex = 0;
    this.load();
  }

  goToPage(index: number): void {
    this.pageIndex = index;
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.list({ status: this.status, q: this.search, page: this.pageIndex }).subscribe({
      next: (page) => {
        this.page.set(page);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      },
    });
    this.service.stats().subscribe({ next: (stats) => this.stats.set(stats), error: () => this.stats.set(null) });
  }
}
