import { Pipe, PipeTransform } from '@angular/core';

import { WorkOrderStatus } from '../core/models';

const LABELS: Record<WorkOrderStatus, string> = {
  OPEN: 'Open',
  IN_PROGRESS: 'In progress',
  DONE: 'Done',
  CANCELLED: 'Cancelled',
};

@Pipe({ name: 'statusLabel' })
export class StatusLabelPipe implements PipeTransform {
  transform(status: WorkOrderStatus | null | undefined): string {
    return status ? LABELS[status] : '';
  }
}
