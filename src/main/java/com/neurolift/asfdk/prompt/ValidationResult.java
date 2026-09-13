package com.neurolift.asfdk.prompt;

/**
 * Result of LLM output validation.
 *
 * @param valid  whether the output passed validation
 * @param reason human-readable reason if invalid
 */
public record ValidationResult(
        boolean valid,
        String reason
) {
    public ValidationResult(boolean valid) {
        this(valid, null);
    }
}