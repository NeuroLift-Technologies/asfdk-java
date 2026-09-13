package com.neurolift.asfdk.prompt;

/**
 * Outcome of injection-pattern detection.
 *
 * @param detected       whether an injection pattern matched
 * @param matchedPattern the regex match text, or {@code null} when not detected
 */
public record InjectionDetection(
        boolean detected,
        String matchedPattern
) {}