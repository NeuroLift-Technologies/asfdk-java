package com.neurolift.asfdk.integration;

/**
 * Adapter status shared across integration adapters.
 *
 * @param active whether the component is active
 * @param mode   human-readable mode label
 */
public record ComponentAdapterStatus(
        boolean active,
        String mode
) {}