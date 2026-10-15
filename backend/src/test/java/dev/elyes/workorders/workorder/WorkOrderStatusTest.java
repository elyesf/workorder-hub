package dev.elyes.workorders.workorder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import dev.elyes.workorders.common.ConflictException;

class WorkOrderStatusTest {

    @ParameterizedTest(name = "{0} -> {1} allowed: {2}")
    @CsvSource({
        "OPEN, IN_PROGRESS, true",
        "OPEN, CANCELLED, true",
        "OPEN, DONE, false",
        "IN_PROGRESS, DONE, true",
        "IN_PROGRESS, OPEN, true",
        "IN_PROGRESS, CANCELLED, true",
        "DONE, OPEN, false",
        "DONE, CANCELLED, false",
        "CANCELLED, OPEN, false"
    })
    void transitions(WorkOrderStatus from, WorkOrderStatus to, boolean allowed) {
        assertThat(from.canMoveTo(to)).isEqualTo(allowed);
    }

    @Test
    void doneAndCancelledAreFinal() {
        assertThat(WorkOrderStatus.DONE.isFinal()).isTrue();
        assertThat(WorkOrderStatus.CANCELLED.isFinal()).isTrue();
        assertThat(WorkOrderStatus.OPEN.isFinal()).isFalse();
    }

    @Test
    void workOrderRejectsSkippingInProgress() {
        WorkOrder order = new WorkOrder("Fix valve", "Customer", Priority.LOW, null);

        assertThatThrownBy(() -> order.changeStatus(WorkOrderStatus.DONE))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("OPEN to DONE");
        assertThat(order.getStatus()).isEqualTo(WorkOrderStatus.OPEN);
    }

    @Test
    void overdueOnlyWhenPastDueAndNotFinished() {
        LocalDate today = LocalDate.of(2026, 10, 15);
        WorkOrder order = new WorkOrder("Fix valve", "Customer", Priority.LOW, null);
        order.setDueDate(today.minusDays(1));

        assertThat(order.isOverdue(today)).isTrue();

        order.changeStatus(WorkOrderStatus.CANCELLED);
        assertThat(order.isOverdue(today)).isFalse();
    }
}
