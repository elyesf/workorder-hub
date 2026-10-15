import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../core/auth.service';
import { PRIORITIES, WorkOrder, WorkOrderRequest, WorkOrderStatus } from '../../core/models';
import { WorkOrderService } from '../../core/work-order.service';
import { errorMessage } from '../../shared/problem';
import { StatusLabelPipe } from '../../shared/status-label.pipe';

/** One component for both "new" and "edit". The route param id is bound as an input. */
@Component({
  selector: 'app-work-order-form',
  imports: [ReactiveFormsModule, RouterLink, DatePipe, StatusLabelPipe],
  templateUrl: './work-order-form.html',
})
export class WorkOrderForm implements OnInit {
  private readonly service = inject(WorkOrderService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  readonly auth = inject(AuthService);

  /** Set from the route (withComponentInputBinding). Undefined on /work-orders/new. */
  readonly id = input<string>();

  readonly priorities = PRIORITIES;
  readonly order = signal<WorkOrder | null>(null);
  readonly error = signal<string | null>(null);
  readonly saving = signal(false);

  readonly form = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(160)]],
    customer: ['', [Validators.required, Validators.maxLength(160)]],
    location: ['', Validators.maxLength(160)],
    priority: ['MEDIUM', Validators.required],
    assignee: ['', Validators.maxLength(120)],
    dueDate: [''],
    description: ['', Validators.maxLength(2000)],
  });

  get isNew(): boolean {
    return !this.id();
  }

  ngOnInit(): void {
    const id = this.id();
    if (id) {
      this.service.get(Number(id)).subscribe({
        next: (order) => this.fill(order),
        error: (err) => this.error.set(errorMessage(err)),
      });
    }
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const request: WorkOrderRequest = {
      title: v.title ?? '',
      customer: v.customer ?? '',
      location: v.location || null,
      priority: (v.priority ?? 'MEDIUM') as WorkOrderRequest['priority'],
      assignee: v.assignee || null,
      dueDate: v.dueDate || null,
      description: v.description || null,
    };
    this.saving.set(true);
    this.error.set(null);

    const current = this.order();
    const call = current ? this.service.update(current.id, request) : this.service.create(request);
    call.subscribe({
      next: (saved) => {
        this.saving.set(false);
        if (current) {
          this.fill(saved);
        } else {
          this.router.navigate(['/work-orders', saved.id]);
        }
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.saving.set(false);
      },
    });
  }

  moveTo(status: WorkOrderStatus): void {
    const current = this.order();
    if (!current) {
      return;
    }
    this.service.changeStatus(current.id, status).subscribe({
      next: (updated) => this.fill(updated),
      error: (err) => this.error.set(errorMessage(err)),
    });
  }

  remove(): void {
    const current = this.order();
    if (!current) {
      return;
    }
    this.service.delete(current.id).subscribe({
      next: () => this.router.navigate(['/work-orders']),
      error: (err) => this.error.set(errorMessage(err)),
    });
  }

  private fill(order: WorkOrder): void {
    this.order.set(order);
    this.form.reset({
      title: order.title,
      customer: order.customer,
      location: order.location ?? '',
      priority: order.priority,
      assignee: order.assignee ?? '',
      dueDate: order.dueDate ?? '',
      description: order.description ?? '',
    });
  }
}
