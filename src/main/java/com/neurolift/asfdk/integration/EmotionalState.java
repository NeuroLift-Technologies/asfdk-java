package com.neurolift.asfdk.integration;

import java.util.List;

/**
 * Emotional state detected by the Sleepwalker Protocol.
 *
 * Mirrors TS {@code EmotionalState} from {@code @neurolift-technologies/sleepwalker-protocol}.
 *
 * @param valence                  valence score, clamped to [-1.0, 0.0]
 * @param arousal                  arousal score, clamped to [0.0, 1.0]
 * @param dominant                 dominant category, e.g. {@code "crisis"}, {@code "distress"}, {@code "neutral"}
 * @param explicitSuicidalIdeation whether suicidal ideation was detected
 * @param selfHarmIndicators       whether self-harm indicators were detected
 * @param inabilityToEnsureSafety  whether the user cannot ensure their own safety
 * @param sentimentScore           sentiment score, clamped to [-1.0, 0.0]
 * @param flags                    human-readable flag labels
 */
public record EmotionalState(
        double valence,
        double arousal,
        String dominant,
        boolean explicitSuicidalIdeation,
        boolean selfHarmIndicators,
        boolean inabilityToEnsureSafety,
        double sentimentScore,
        List<String> flags
) {
    public EmotionalState {
        flags = flags == null ? List.of() : flags;
    }

    public EmotionalState() {
        this(0.0, 0.0, null, false, false, false, 0.0, List.of());
    }
}