package com.neurolift.asfdk.integration;

/**
 * Result of TOI schema validation.
 *
 * @param valid  whether the candidate passed validation
 * @param errors list of validation issues, if any
 * @param toi    the parsed document, if valid
 */
public record TOIValidationResult(
        boolean valid,
        java.util.List<ValidationIssue> errors,
        ToiDocument toi
) {
    public TOIValidationResult(boolean valid, java.util.List<ValidationIssue> errors) {
        this(valid, errors, null);
    }
}