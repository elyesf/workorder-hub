package dev.elyes.workorders.workorder;

import java.time.Instant;
import java.time.LocalDate;

import dev.elyes.workorders.auth.AppUser;
import dev.elyes.workorders.common.ConflictException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "work_order")
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String customer;

    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkOrderStatus status;

    private String assignee;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private AppUser createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Optimistic locking: two people saving the same order at once get a 409 instead of a silent overwrite. */
    @Version
    private long version;

    protected WorkOrder() {
        // for JPA
    }

    public WorkOrder(String title, String customer, Priority priority, AppUser createdBy) {
        this.title = title;
        this.customer = customer;
        this.priority = priority;
        this.createdBy = createdBy;
        this.status = WorkOrderStatus.OPEN;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    /** All status changes go through here so the rules in WorkOrderStatus are always applied. */
    public void changeStatus(WorkOrderStatus next) {
        if (status == next) {
            return;
        }
        if (!status.canMoveTo(next)) {
            throw new ConflictException("Cannot move a work order from " + status + " to " + next);
        }
        status = next;
    }

    public boolean isOverdue(LocalDate today) {
        return dueDate != null && dueDate.isBefore(today) && !status.isFinal();
    }

    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getCustomer() { return customer; }
    public String getLocation() { return location; }
    public Priority getPriority() { return priority; }
    public WorkOrderStatus getStatus() { return status; }
    public String getAssignee() { return assignee; }
    public LocalDate getDueDate() { return dueDate; }
    public AppUser getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCustomer(String customer) { this.customer = customer; }
    public void setLocation(String location) { this.location = location; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}
