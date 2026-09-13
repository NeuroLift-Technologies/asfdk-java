package com.neurolift.asfdk.types;

import java.util.Map;

/**
 * Aggregate health report returned by
 * {@link com.neurolift.asfdk.foundation.NeuroLiftFoundation#healthCheck()}.
 *
 * @param healthy    overall health flag
 * @param components per-component status map
 * @param timestampMillis epoch-millis when the health check was performed
 */
public record HealthCheckResult(
        boolean healthy,
        Map<String, ComponentStatus> components,
        long timestampMillis
) {
    public HealthCheckResult {
        components = components == null ? Map.of() : components;
    }
}