package com.neurolift.asfdk.types;

import java.util.List;
import java.util.Map;

/**
 * Result returned by {@link com.neurolift.asfdk.foundation.NeuroLiftFoundation#processInteraction}.
 *
 * @param timestampMillis    epoch-millis when the response was produced
 * @param responseType       the interaction type that triggered this response
 * @param content            aggregated response content from active components
 * @param componentsInvolved names of components that contributed to this response
 * @param success            whether the interaction was processed without error
 */
public record FoundationResponse(
        long timestampMillis,
        String responseType,
        Map<String, Object> content,
        List<String> componentsInvolved,
        boolean success
) {
    public FoundationResponse {
        content = content == null ? Map.of() : content;
        componentsInvolved = componentsInvolved == null ? List.of() : componentsInvolved;
    }
}