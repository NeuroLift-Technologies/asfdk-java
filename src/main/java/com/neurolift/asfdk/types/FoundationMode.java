package com.neurolift.asfdk.types;

/**
 * Operating modes that determine which Solidarity Framework components are active.
 *
 * Mirrors TS {@code FoundationMode} enum from {@code @neurolift-technologies/asfdk} v0.2.4.
 */
public enum FoundationMode {
    /** All components active: TOI-OTOI, Sleepwalker Protocol, and RRT Advocate. */
    UNIFIED("unified"),
    /** Crisis routing only: RRT Advocate active, others disabled. */
    CRISIS_ONLY("crisis_only"),
    /** Emotional continuity only: Sleepwalker Protocol active, others disabled. */
    CONTINUITY_ONLY("continuity"),
    /** TOI governance only: TOI-OTOI validation active, others disabled. */
    FRAMEWORK_ONLY("framework"),
    /** Development mode: TOI-OTOI and Sleepwalker active, RRT Advocate disabled. */
    DEVELOPMENT("development");

    private final String value;

    FoundationMode(String value) {
        this.value = value;
    }

    /** Canonical JSON/value string, e.g. {@code "unified"}. */
    public String value() {
        return value;
    }

    /**
     * Returns the matching {@link FoundationMode} for the given value,
     * or {@code null} if no member matches exactly (case-sensitive, no trimming).
     */
    public static FoundationMode fromValue(String value) {
        for (FoundationMode mode : values()) {
            if (mode.value.equals(value)) {
                return mode;
            }
        }
        return null;
    }
}