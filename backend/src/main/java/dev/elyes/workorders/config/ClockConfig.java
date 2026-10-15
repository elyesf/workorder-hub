package dev.elyes.workorders.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** One injectable clock, so "today" (used for overdue checks) can be fixed in tests. */
@Configuration
public class ClockConfig {

    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("Europe/Berlin"));
    }
}
