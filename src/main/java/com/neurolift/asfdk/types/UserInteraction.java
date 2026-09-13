package com.neurolift.asfdk.types;

import java.util.Map;

/**
 * A single interaction event submitted to the foundation for processing.
 *
 * @param timestampMillis  epoch-millis time the interaction occurred
 * @param interactionType  category of the interaction
 * @param data             arbitrary interaction payload
 * @param userId           user the interaction belongs to
 * @param sessionId        optional session identifier
 * @param priority         optional priority level
 * @param context          optional enrichment context
 * @param channel          optional channel the interaction arrived on (D2); the trust
 *                         attribute is read ONLY from this top-level field (D4); values
 *                         nested inside {@code data} or {@code context} are ignored
 */
public record UserInteraction(
        long timestampMillis,
        InteractionType interactionType,
        Map<String, Object> data,
        String userId,
        String sessionId,
        Integer priority,
        Map<String, Object> context,
        Channel channel
) {
    public UserInteraction {
        data = data == null ? Map.of() : data;
    }
}