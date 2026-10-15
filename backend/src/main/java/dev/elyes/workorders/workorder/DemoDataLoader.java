package dev.elyes.workorders.workorder;

import java.time.Clock;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import dev.elyes.workorders.auth.AppUser;
import dev.elyes.workorders.auth.Role;
import dev.elyes.workorders.auth.UserRepository;

/** Fills an empty database with a demo admin and some sample orders. Only runs when app.demo-data=true. */
@Component
@ConditionalOnProperty(name = "app.demo-data", havingValue = "true")
class DemoDataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);
    static final String DEMO_EMAIL = "demo@workorderhub.dev";
    static final String DEMO_PASSWORD = "demo12345";

    private final UserRepository users;
    private final WorkOrderRepository workOrders;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    DemoDataLoader(UserRepository users, WorkOrderRepository workOrders, PasswordEncoder passwordEncoder, Clock clock) {
        this.users = users;
        this.workOrders = workOrders;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.count() > 0) {
            return;
        }
        AppUser admin = users.save(new AppUser(
            DEMO_EMAIL, passwordEncoder.encode(DEMO_PASSWORD), "Demo Dispatcher", Role.ADMIN));
        LocalDate today = LocalDate.now(clock);

        save(admin, "Replace pump seal", "Stadtwerke Beispielstadt", "Pumpstation Nord", Priority.HIGH,
            "Jonas", today.plusDays(2), null);
        save(admin, "Annual inspection of heating system", "Kita Sonnenschein", "Hauptstr. 12", Priority.MEDIUM,
            "Mira", today.plusDays(10), null);
        save(admin, "Camera inspection of sewer line", "Wohnbau Rhein GmbH", "Block C", Priority.HIGH,
            "Jonas", today.minusDays(3), WorkOrderStatus.IN_PROGRESS);
        save(admin, "Fix leaking valve in basement", "Bäckerei Klein", "Marktplatz 3", Priority.LOW,
            null, today.minusDays(1), null);
        save(admin, "Install water meter", "Familie Schmidt", "Lindenweg 8", Priority.LOW,
            "Mira", today.minusDays(7), WorkOrderStatus.DONE);
        save(admin, "Quote for pipe renovation", "Autohaus Weber", "Industriestr. 40", Priority.MEDIUM,
            null, null, WorkOrderStatus.CANCELLED);

        log.info("Demo data created. Log in with {} / {}", DEMO_EMAIL, DEMO_PASSWORD);
    }

    private void save(AppUser creator, String title, String customer, String location, Priority priority,
                      String assignee, LocalDate due, WorkOrderStatus targetStatus) {
        WorkOrder order = new WorkOrder(title, customer, priority, creator);
        order.setLocation(location);
        order.setAssignee(assignee);
        order.setDueDate(due);
        if (targetStatus == WorkOrderStatus.DONE) {
            order.changeStatus(WorkOrderStatus.IN_PROGRESS);
        }
        if (targetStatus != null) {
            order.changeStatus(targetStatus);
        }
        workOrders.save(order);
    }
}
