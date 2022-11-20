package com.example.demo.bpp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class MetricService {
    private final MeterRegistry meterRegistry;
    private final Executor executor;

    public void incSql(Status status, String sql, String isolation, Duration duration) {
        executor.execute(
            () -> DistributionSummary
                .builder("sql.duration")
                .tags(List.of(
                    Tag.of("status", status.name()),
                    Tag.of("sql", sql),
                    Tag.of("isolation", isolation)
                ))
                .minimumExpectedValue(0.1)
                .maximumExpectedValue(5000.0)
                .publishPercentiles(0.5, 0.95, 0.99)
                .serviceLevelObjectives(10, 50, 100, 200, 500, 1000, 2000, 5000)
                .register(meterRegistry)
                .record(duration.toMillis())
        );
    }

    public enum Status {
        SUCCESS, FAILURE
    }

    @Configuration
    static class Config {
        @Bean
        public MetricService metricService(MeterRegistry meterRegistry) {
            return new MetricService(meterRegistry, Executors.newSingleThreadExecutor());
        }
    }
}
