package dev.elyes.workorders.workorder;

import java.util.EnumSet;
import java.util.Set;

/**
 * The life cycle of a work order.
 *
 * <pre>
 * OPEN -> IN_PROGRESS -> DONE
 *   \         |  ^
 *    \        v  |  (can go back to OPEN if the technician hands it back)
 *     +--> CANCELLED
 * </pre>
 *
 * DONE and CANCELLED are final.
 */
public enum WorkOrderStatus {
    OPEN,
    IN_PROGRESS,
    DONE,
    CANCELLED;

    public Set<WorkOrderStatus> allowedNext() {
        return switch (this) {
            case OPEN -> EnumSet.of(IN_PROGRESS, CANCELLED);
            case IN_PROGRESS -> EnumSet.of(OPEN, DONE, CANCELLED);
            case DONE, CANCELLED -> EnumSet.noneOf(WorkOrderStatus.class);
        };
    }

    public boolean canMoveTo(WorkOrderStatus next) {
        return allowedNext().contains(next);
    }

    public boolean isFinal() {
        return allowedNext().isEmpty();
    }
}
