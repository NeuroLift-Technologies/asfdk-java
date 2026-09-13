package com.neurolift.asfdk.integration;

/**
 * A single validation issue.
 *
 * @param message human-readable description
 * @param path    dotted path to the offending field
 * @param code    machine-readable error code
 */
public record ValidationIssue(
        String message,
        String path,
        String code
) {
    public ValidationIssue {
        path = path == null ? "" : path;
        code = code == null ? "error" : code;
    }

    public ValidationIssue(String message) {
        this(message, "", "error");
    }
}