package com.neurolift.asfdk.types;

/**
 * Configuration for creating a {@link com.neurolift.asfdk.foundation.NeuroLiftFoundation}.
 *
 * @param userId     unique identifier for the user session
 * @param mode       foundation operating mode
 * @param components per-component activation overrides; defaults derived from {@link FoundationMode}
 * @param toi        optional TOI source — a preferences map (full or partial) or a path to a
 *                   {@code .toi}/{@code .json} file. When absent, a privacy-first document is
 *                   generated from defaults.
 */
public record FoundationConfig(
        String userId,
        FoundationMode mode,
        ComponentOverrides components,
        Object toi // Map<String, Object> or String (file path); null → defaults
) {
    public FoundationConfig(String userId, FoundationMode mode) {
        this(userId, mode, null, null);
    }
}