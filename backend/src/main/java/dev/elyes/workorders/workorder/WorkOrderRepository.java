package dev.elyes.workorders.workorder;

import java.time.LocalDate;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long>, JpaSpecificationExecutor<WorkOrder> {

    long countByStatus(WorkOrderStatus status);

    long countByDueDateBeforeAndStatusIn(LocalDate date, Collection<WorkOrderStatus> statuses);
}
