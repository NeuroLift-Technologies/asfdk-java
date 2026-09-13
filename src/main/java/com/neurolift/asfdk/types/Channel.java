package com.neurolift.asfdk.types;

/**
 * Closed set of channels an interaction can arrive on (D3).
 *
 * Trust is derived from this value alone (D4/D6): only {@link #USER_INPUT} is trusted.
 * Anything that does not exactly match a member collapses to {@link #UNKNOWN}
 * and is never elevated by {@link #normalize}.
 *
 * Mirrors TS {@code Channel} enum from {@code @neurolift-technologies/asfdk} v0.2.4.
 */
public enum Channel {
    USER_INPUT("user_input"),
    MODEL_OUTPUT("model_output"),
    TOOL_RESULT("tool_result"),
    SYSTEM("system"),
    UNKNOWN("unknown");

    private final String value;

    Channel(String value) {
        this.value = value;
    }

    /** Canonical JSON/value string, e.g. {@code "user_input"}. */
    public String value() {
        return value;
    }

    /**
     * Coerces an arbitrary runtime value to a {@link Channel}.
     *
     * Exact closed-enum members pass through (both {@link Channel} instances and
     * their canonical strings); every malformed value — wrong casing, surrounding
     * whitespace, non-strings, nested objects, null — collapses to {@link #UNKNOWN}.
     * Never elevates.
     */
    public static Channel normalize(Object value) {
        if (value instanceof Channel channel) {
            return channel;
        }
        if (value instanceof String s) {
            for (Channel channel : values()) {
                if (channel.value.equals(s)) {
                    return channel;
                }
            }
        }
        return UNKNOWN;
    }

    /** Returns the matching {@link Channel} for the given canonical value, or {@code null}. */
    public static Channel fromValue(String value) {
        for (Channel channel : values()) {
            if (channel.value.equals(value)) {
                return channel;
            }
        }
        return null;
    }
}