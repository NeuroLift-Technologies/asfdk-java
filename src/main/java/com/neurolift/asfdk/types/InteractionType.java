package com.neurolift.asfdk.types;

/**
 * Interaction categories that can be routed through the foundation.
 *
 * Mirrors TS {@code InteractionType} enum from {@code @neurolift-technologies/asfdk} v0.2.4.
 */
public enum InteractionType {
    EMOTIONAL_ASSESSMENT("emotional_assessment"),
    CRISIS_ALERT("crisis_alert"),
    PREFERENCE_UPDATE("preference_update"),
    OPTIMIZATION_REQUEST("optimization_request"),
    STATUS_INQUIRY("status_inquiry"),
    EMERGENCY_ESCALATION("emergency_escalation");

    private final String value;

    InteractionType(String value) {
        this.value = value;
    }

    /** Canonical JSON/value string, e.g. {@code "crisis_alert"}. */
    public String value() {
        return value;
    }

    /** Returns the matching {@link InteractionType} for the given value, or {@code null}. */
    public static InteractionType fromValue(String value) {
        for (InteractionType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}