package com.neurolift.asfdk.prompt;

/**
 * Result of input sanitization.
 *
 * @param clean     whether the input passed all checks
 * @param content   sanitized content (delimited, escaped)
 * @param reason    human-readable reason if not clean
 * @param riskLevel severity of the finding
 */
public record SanitizationResult(
        boolean clean,
        String content,
        String reason,
        RiskLevel riskLevel
) {}