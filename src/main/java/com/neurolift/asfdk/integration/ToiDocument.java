package com.neurolift.asfdk.integration;

/**
 * Represents a generated/validated TOI document.
 *
 * Mirrors the TS {@code ToiDocument} shape from {@code @neurolift-technologies/toi}.
 *
 * @param toi       version string, e.g. {@code "1.0.0"}
 * @param tier      tier, e.g. {@code "personal"}
 * @param identity  document identity
 */
public record ToiDocument(
        String toi,
        String tier,
        ToiIdentity identity
) {
    public ToiDocument {
        toi = toi == null ? "1.0.0" : toi;
        tier = tier == null ? "personal" : tier;
        identity = identity == null ? new ToiIdentity() : identity;
    }

    public ToiDocument() {
        this("1.0.0", "personal", new ToiIdentity());
    }
}