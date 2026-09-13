package com.neurolift.asfdk.integration;

import com.neurolift.asfdk.types.Channel;

/**
 * A {@link CrisisAssessment} enriched with channel provenance and trust metadata.
 *
 * @param assessment  the underlying crisis assessment
 * @param channel     the resolved channel
 * @param trusted     whether the channel is trusted
 * @param flagged     whether the input was sanitized/flagged
 * @param flagReason  reason for the flag, if any
 */
public record CrisisAssessmentWithProvenance(
        CrisisAssessment assessment,
        Channel channel,
        boolean trusted,
        Boolean flagged,
        String flagReason
) {
    public CrisisAssessmentWithProvenance(
            CrisisAssessment assessment, Channel channel, boolean trusted
    ) {
        this(assessment, channel, trusted, null, null);
    }
}