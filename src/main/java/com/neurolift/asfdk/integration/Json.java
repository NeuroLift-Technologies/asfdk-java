package com.neurolift.asfdk.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Shared JSON utilities for the Java port.
 *
 * The Kotlin reference uses {@code kotlinx.serialization.json}; Java mirrors
 * this with Jackson (jackson-databind). Only a small surface is exercised:
 * parsing TOI files and validating output JSON structure.
 */
public final class Json {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Json() {
        // static utility only
    }

    /** Shared, thread-safe (re-entrant) ObjectMapper. */
    public static ObjectMapper mapper() {
        return MAPPER;
    }

    /** Parses a JSON file into a {@code Map<String, Object>} tree. */
    public static java.util.Map<String, Object> parseFile(java.io.File file) throws Exception {
        return MAPPER.readValue(file, new TypeReference<>() {});
    }

    /** Parses a JSON string into a {@code Map<String, Object>} tree. */
    public static java.util.Map<String, Object> parse(String json) throws Exception {
        return MAPPER.readValue(json, new TypeReference<>() {});
    }
}