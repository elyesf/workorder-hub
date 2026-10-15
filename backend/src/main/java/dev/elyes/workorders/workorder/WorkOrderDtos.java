package dev.elyes.workorders.workorder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class WorkOrderDtos {

    private WorkOrderDtos() {
    }

    /** Used for both create (POST) and full update (PUT). Status is changed through its own endpoint. */
    public record WorkOrderRequest(
        @NotBlank @Size(max = 160) String title,
        @Size(max = 2000) String description,
        @NotBlank @Size(max = 160) String customer,
        @Size(max = 160) String location,
        @NotNull Priority priority,
        @Size(max = 120) String assignee,
        LocalDate dueDate) {
    }

    public record StatusChangeRequest(@NotNull WorkOrderStatus status) {
    }

    public record WorkOrderResponse(
        Long id,
        String title,
        String description,
        String customer,
        String location,
        Priority priority,
        WorkOrderStatus status,
        Set<WorkOrderStatus> allowedNextStatuses,
        String assignee,
        LocalDate dueDate,
        boolean overdue,
        String createdBy,
        Instant createdAt,
        Instant updatedAt) {

        static WorkOrderResponse from(WorkOrder w, LocalDate today) {
            return new WorkOrderResponse(
                w.getId(), w.getTitle(), w.getDescription(), w.getCustomer(), w.getLocation(),
                w.getPriority(), w.getStatus(), w.getStatus().allowedNext(), w.getAssignee(),
                w.getDueDate(), w.isOverdue(today),
                w.getCreatedBy() == null ? null : w.getCreatedBy().getDisplayName(),
                w.getCreatedAt(), w.getUpdatedAt());
        }
    }

    public record StatsResponse(Map<WorkOrderStatus, Long> byStatus, long overdue, long total) {
    }
}
