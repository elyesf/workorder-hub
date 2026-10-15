package dev.elyes.workorders.workorder;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

/**
 * Filters for the list endpoint, built in code instead of one big JPQL query
 * so each optional filter is only added when it is actually used.
 */
final class WorkOrderSpecs {

    private WorkOrderSpecs() {
    }

    static Specification<WorkOrder> hasStatus(WorkOrderStatus status) {
        return (root, query, cb) -> status == null
            ? cb.conjunction()
            : cb.equal(root.get("status"), status);
    }

    /** Case-insensitive search in title, customer and assignee. */
    static Specification<WorkOrder> matchesText(String text) {
        return (root, query, cb) -> {
            if (text == null || text.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + text.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("customer")), pattern),
                cb.like(cb.lower(root.get("assignee")), pattern));
        };
    }
}
