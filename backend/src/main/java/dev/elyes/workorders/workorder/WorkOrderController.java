package dev.elyes.workorders.workorder;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.elyes.workorders.common.PageResponse;
import dev.elyes.workorders.workorder.WorkOrderDtos.StatsResponse;
import dev.elyes.workorders.workorder.WorkOrderDtos.StatusChangeRequest;
import dev.elyes.workorders.workorder.WorkOrderDtos.WorkOrderRequest;
import dev.elyes.workorders.workorder.WorkOrderDtos.WorkOrderResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService service;

    public WorkOrderController(WorkOrderService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<WorkOrderResponse> list(
            @RequestParam(required = false) WorkOrderStatus status,
            @RequestParam(required = false, name = "q") String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.list(status, search, pageable);
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        return service.stats();
    }

    @GetMapping("/{id}")
    public WorkOrderResponse get(@PathVariable long id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkOrderResponse create(@Valid @RequestBody WorkOrderRequest request, @AuthenticationPrincipal Jwt jwt) {
        return service.create(request, jwt.getSubject());
    }

    @PutMapping("/{id}")
    public WorkOrderResponse update(@PathVariable long id, @Valid @RequestBody WorkOrderRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public WorkOrderResponse changeStatus(@PathVariable long id, @Valid @RequestBody StatusChangeRequest request) {
        return service.changeStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        service.delete(id);
    }
}
