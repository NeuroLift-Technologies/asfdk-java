package com.neurolift.asfdk.integration;

/**
 * Crisis level classification from the RRT Advocate engine.
 *
 * Mirrors TS {@code CrisisLevel} from {@code @neurolift-technologies/rrt-advocate}.
 */
public enum CrisisLevel {
    GREEN("green"),
    YELLOW("yellow"),
    ORANGE("orange"),
    RED("red"),
    BLACK("black");

    private final String value;

    CrisisLevel(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    /** Returns the matching {@link CrisisLevel} for the given value, or {@code null}. */
    public static CrisisLevel fromValue(String value) {
        for (CrisisLevel level : values()) {
            if (level.value.equals(value)) {
                return level;
            }
        }
        return null;
    }
}