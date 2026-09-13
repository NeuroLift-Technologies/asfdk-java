package com.neurolift.asfdk.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * TOI-OTOI integration adapter.
 *
 * Validates {@code .toi} documents.
 * Mirrors TS {@code integration/toi-otoi.ts} from {@code @neurolift-technologies/asfdk} v0.2.4.
 */
public final class ToiOtoiAdapter {

    private static final Pattern TOI_VERSION = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");

    private ToiOtoiAdapter() {
        // static utility only
    }

    /**
     * Validates {@code candidate} against the canonical {@code .toi} v1.0.0 schema.
     *
     * @return {@link TOIValidationResult} indicating validity and any issues
     */
    public static TOIValidationResult validateTOI(Object candidate) {
        if (candidate == null) {
            return new TOIValidationResult(
                    false,
                    List.of(new ValidationIssue("TOI candidate is null"))
            );
        }
        if (!(candidate instanceof Map)) {
            return new TOIValidationResult(
                    false,
                    List.of(new ValidationIssue("TOI candidate must be a JSON object (Map)"))
            );
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) candidate;

        // Check required top-level keys
        List<String> missing = new ArrayList<>();
        for (String key : List.of("$toi", "$tier", "identity")) {
            if (!map.containsKey(key)) {
                missing.add(key);
            }
        }
        if (!missing.isEmpty()) {
            List<ValidationIssue> errors = new ArrayList<>();
            for (String key : missing) {
                errors.add(new ValidationIssue("Missing required field: " + key, key, "missing_field"));
            }
            return new TOIValidationResult(false, errors);
        }

        // Validate $toi is a version string
        Object toiVersion = map.get("$toi");
        if (!(toiVersion instanceof String) || !TOI_VERSION.matcher((String) toiVersion).matches()) {
            return new TOIValidationResult(
                    false,
                    List.of(new ValidationIssue("Invalid $toi version format", "$toi", "invalid_format"))
            );
        }

        // Validate identity.author exists
        Object identity = map.get("identity");
        if (!(identity instanceof Map) || !(((Map<?, ?>) identity).get("author") instanceof String)) {
            return new TOIValidationResult(
                    false,
                    List.of(new ValidationIssue("Missing or invalid identity.author", "identity.author", "invalid_field"))
            );
        }

        // Build a ToiDocument from the validated map
        Map<String, Object> idMap = (Map<String, Object>) identity;
        ToiDocument document = new ToiDocument(
                (String) toiVersion,
                map.get("$tier") instanceof String ? (String) map.get("$tier") : "personal",
                new ToiIdentity((String) idMap.get("author"))
        );
        return new TOIValidationResult(true, null, document);
    }

    /** Returns the active TOI-OTOI component status. */
    public static ComponentAdapterStatus getStatus() {
        return new ComponentAdapterStatus(true, "toi-otoi-validation");
    }
}