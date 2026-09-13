package com.neurolift.asfdk.integration;

import com.neurolift.asfdk.types.Channel;

/**
 * An {@link EmotionalState} enriched with channel provenance and trust metadata.
 *
 * @param emotionalState the underlying emotional state
 * @param channel        the resolved channel
 * @param trusted        whether the channel is trusted (only {@link Channel#USER_INPUT})
 * @param flagged        whether the input was sanitized/flagged
 * @param flagReason     reason for the flag, if any
 */
public record EmotionalStateWithProvenance(
        EmotionalState emotionalState,
        Channel channel,
        boolean trusted,
        Boolean flagged,
        String flagReason
) {
    public EmotionalStateWithProvenance(
            EmotionalState emotionalState, Channel channel, boolean trusted
    ) {
        this(emotionalState, channel, trusted, null, null);
    }
}