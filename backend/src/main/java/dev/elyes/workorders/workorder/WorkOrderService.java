package dev.elyes.workorders.workorder;

import java.time.Clock;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.elyes.workorders.auth.AppUser;
import dev.elyes.workorders.auth.UserRepository;
import dev.elyes.workorders.common.NotFoundException;
import dev.elyes.workorders.common.PageResponse;
import dev.elyes.workorders.workorder.WorkOrderDtos.StatsResponse;
import dev.elyes.workorders.workorder.WorkOrderDtos.WorkOrderRequest;
import dev.elyes.workorders.workorder.WorkOrderDtos.WorkOrderResponse;

/**
 * Business logic for work orders. Every method maps to DTOs inside the transaction,
 * because open-in-view is off and lazy relations are not available after it ends.
 */
@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrders;
    private final UserRepository users;
    private final Clock clock;

    public WorkOrderService(WorkOrderRepository workOrders, UserRepository users, Clock clock) {
        this.workOrders = workOrders;
        this.users = users;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<WorkOrderResponse> list(WorkOrderStatus status, String search, Pageable pageable) {
        LocalDate today = today();
        Specification<WorkOrder> filter = Specification.allOf(
            WorkOrderSpecs.hasStatus(status),
            WorkOrderSpecs.matchesText(search));
        return PageResponse.of(workOrders.findAll(filter, pageable), w -> WorkOrderResponse.from(w, today));
    }

    @Transactional(readOnly = true)
    public WorkOrderResponse get(long id) {
        return WorkOrderResponse.from(find(id), today());
    }

    @Transactional
    public WorkOrderResponse create(WorkOrderRequest request, String creatorEmail) {
        AppUser creator = users.findByEmailIgnoreCase(creatorEmail)
            .orElseThrow(() -> new NotFoundException("User " + creatorEmail + " not found"));
        WorkOrder order = new WorkOrder(request.title().trim(), request.customer().trim(), request.priority(), creator);
        applyEditableFields(order, request);
        return WorkOrderResponse.from(workOrders.save(order), today());
    }

    @Transactional
    public WorkOrderResponse update(long id, WorkOrderRequest request) {
        WorkOrder order = find(id);
        order.setTitle(request.title().trim());
        order.setCustomer(request.customer().trim());
        order.setPriority(request.priority());
        applyEditableFields(order, request);
        // flush now so an optimistic locking conflict surfaces in this request
        return WorkOrderResponse.from(workOrders.saveAndFlush(order), today());
    }

    @Transactional
    public WorkOrderResponse changeStatus(long id, WorkOrderStatus next) {
        WorkOrder order = find(id);
        order.changeStatus(next);
        return WorkOrderResponse.from(workOrders.saveAndFlush(order), today());
    }

    @Transactional
    public void delete(long id) {
        workOrders.delete(find(id));
    }

    @Transactional(readOnly = true)
    public StatsResponse stats() {
        Map<WorkOrderStatus, Long> byStatus = new EnumMap<>(WorkOrderStatus.class);
        long total = 0;
        for (WorkOrderStatus status : WorkOrderStatus.values()) {
            long count = workOrders.countByStatus(status);
            byStatus.put(status, count);
            total += count;
        }
        long overdue = workOrders.countByDueDateBeforeAndStatusIn(
            today(), EnumSet.of(WorkOrderStatus.OPEN, WorkOrderStatus.IN_PROGRESS));
        return new StatsResponse(byStatus, overdue, total);
    }

    private WorkOrder find(long id) {
        return workOrders.findById(id).orElseThrow(() -> new NotFoundException("Work order " + id + " not found"));
    }

    private static void applyEditableFields(WorkOrder order, WorkOrderRequest request) {
        order.setDescription(blankToNull(request.description()));
        order.setLocation(blankToNull(request.location()));
        order.setAssignee(blankToNull(request.assignee()));
        order.setDueDate(request.dueDate());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
