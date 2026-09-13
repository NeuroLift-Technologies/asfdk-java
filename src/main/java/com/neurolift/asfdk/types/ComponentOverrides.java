package com.neurolift.asfdk.types;

/**
 * Per-component activation overrides.
 *
 * @param toiOtoiFramework   override for TOI-OTOI component; {@code null} → mode default
 * @param sleepwalkerProtocol override for Sleepwalker Protocol; {@code null} → mode default
 * @param rrtAdvocate        override for RRT Advocate; {@code null} → mode default
 */
public record ComponentOverrides(
        Boolean toiOtoiFramework,
        Boolean sleepwalkerProtocol,
        Boolean rrtAdvocate
) {
    public ComponentOverrides {
        // no-op compact constructor: fields may be null (defer to mode defaults)
    }

    public ComponentOverrides() {
        this(null, null, null);
    }
}