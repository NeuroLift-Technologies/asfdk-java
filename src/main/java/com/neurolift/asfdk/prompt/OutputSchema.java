package com.neurolift.asfdk.prompt;

/**
 * Discriminated schema descriptor for {@link PromptDefense#validateOutput}.
 * A closed enum so callers can only request supported structural checks.
 */
public enum OutputSchema {
    /** Validate that output is a parseable JSON object. */
    JSON,
    /** Validate that output is non-empty text. */
    TEXT
}