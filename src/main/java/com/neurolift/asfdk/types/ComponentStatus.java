package com.neurolift.asfdk.types;

/**
 * Live status snapshot for a single Solidarity Framework component.
 *
 * @param active whether the component is currently active
 * @param mode   human-readable mode label (e.g. {@code "crisis-detection"})
 * @param error  optional error message if the component is in a degraded state
 */
public record ComponentStatus(
        boolean active,
        String mode,
        String error
) {
    public ComponentStatus(boolean active, String mode) {
        this(active, mode, null);
    }
}