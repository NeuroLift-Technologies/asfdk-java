package com.neurolift.asfdk.integration;

import com.neurolift.asfdk.prompt.PromptDefense;
import com.neurolift.asfdk.prompt.SanitizationResult;
import com.neurolift.asfdk.prompt.RiskLevel;
import com.neurolift.asfdk.prompt.SecurityEventType;
import com.neurolift.asfdk.types.Channel;

import java.util.ArrayList;
import java.util.List;

/**
 * Sleepwalker Protocol integration adapter.
 *
 * Classifies emotional states from free-text input.
 * Mirrors TS {@code integration/sleepwalker.ts} from {@code @neurolift-technologies/asfdk} v0.2.4.
 */
public final class SleepwalkerAdapter {

    private static final List<String> DISTRESS_KEYWORDS = List.of(
            "overwhelmed", "stressed", "anxious", "depressed", "sad",
            "hopeless", "desperate", "breaking down", "can't cope"
    );

    private static final List<String> CRISIS_KEYWORDS = List.of(
            "hurt myself", "want to die", "kill myself", "end my life",
            "suicide", "self-harm", "cut myself"
    );

    private SleepwalkerAdapter() {
        // static utility only
    }

    /**
     * Classifies the emotional state expressed in a user's free-text input.
     *
     * Security: Input is sanitized to prevent prompt injection attacks.
     * A flagged result is logged but still assessed defensively (fail-open).
     */
    public static EmotionalStateWithProvenance detectEmotionalState(
            String userInput, List<Object> sessionHistory, Channel channel, String userId
    ) {
        Channel resolved = Channel.normalize(channel);

        SanitizationResult sanitizationResult = PromptDefense.sanitizeInput(userInput);
        boolean flagged = !sanitizationResult.clean();
        if (flagged) {
            SecurityEventType type = sanitizationResult.riskLevel() == RiskLevel.HIGH
                    ? SecurityEventType.INJECTION_ATTEMPT
                    : SecurityEventType.VALIDATION_FAILURE;
            PromptDefense.logSecurityEvent(
                    type, userId,
                    sanitizationResult.reason() != null
                            ? sanitizationResult.reason()
                            : "Input sanitization flagged in Sleepwalker assessment",
                    System.currentTimeMillis()
            );
        }

        EmotionalState state = classifyEmotionalState(sanitizationResult.content());
        return new EmotionalStateWithProvenance(
                state,
                resolved,
                resolved == Channel.USER_INPUT,
                flagged ? Boolean.TRUE : null,
                flagged ? sanitizationResult.reason() : null
        );
    }

    public static EmotionalStateWithProvenance detectEmotionalState(String userInput) {
        return detectEmotionalState(userInput, List.of(), null, "unknown");
    }

    /**
     * Returns a full interaction assessment object for the given input.
     */
    public static java.util.Map<String, Object> assessInteraction(
            String userInput, List<Object> sessionHistory, Channel channel
    ) {
        Channel resolved = Channel.normalize(channel);
        EmotionalState state = classifyEmotionalState(userInput);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("emotionalState", state);
        result.put("channel", resolved);
        result.put("trusted", resolved == Channel.USER_INPUT);
        return result;
    }

    /**
     * Returns {@code true} when the assessed emotional state warrants an RRT Advocate handoff.
     */
    public static boolean requiresRrtaHandoff(EmotionalState state) {
        return state.explicitSuicidalIdeation()
                || state.selfHarmIndicators()
                || state.inabilityToEnsureSafety();
    }


    public static EmotionalStateWithProvenance detectEmotionalState(String userInput, Channel channel) {
        return detectEmotionalState(userInput, List.of(), channel, "unknown");
    }

    public static EmotionalStateWithProvenance detectEmotionalState(String userInput, Channel channel, String userId) {
        return detectEmotionalState(userInput, List.of(), channel, userId);
    }
    /** Returns the active Sleepwalker Protocol component status. */
    public static ComponentAdapterStatus getStatus() {
        return new ComponentAdapterStatus(true, "emotional-continuity");
    }

    /**
     * Stub emotional state classifier.
     */
    private static EmotionalState classifyEmotionalState(String text) {
        String lower = text.toLowerCase();

        boolean explicitSuicidalIdeation = CRISIS_KEYWORDS.stream().anyMatch(lower::contains);
        boolean selfHarmIndicators = lower.contains("hurt myself") || lower.contains("cut myself");
        boolean inabilityToEnsureSafety = explicitSuicidalIdeation || selfHarmIndicators;

        long distressCount = DISTRESS_KEYWORDS.stream().filter(lower::contains).count();
        double valence = clamp(-0.8 + distressCount * 0.15, -1.0, 0.0);
        double arousal = clamp(0.3 + distressCount * 0.2, 0.0, 1.0);
        double sentimentScore = clamp(-0.7 + distressCount * 0.12, -1.0, 0.0);

        String dominant;
        if (explicitSuicidalIdeation) {
            dominant = "crisis";
        } else if (distressCount >= 3) {
            dominant = "deep_distress";
        } else if (distressCount >= 1) {
            dominant = "distress";
        } else {
            dominant = "neutral";
        }

        List<String> flags = new ArrayList<>();
        if (explicitSuicidalIdeation) flags.add("suicidal_ideation");
        if (selfHarmIndicators) flags.add("self_harm");
        if (inabilityToEnsureSafety) flags.add("safety_concern");
        if (distressCount >= 3) flags.add("high_distress");

        return new EmotionalState(valence, arousal, dominant, explicitSuicidalIdeation,
                selfHarmIndicators, inabilityToEnsureSafety, sentimentScore, flags);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
