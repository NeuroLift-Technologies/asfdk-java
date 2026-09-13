package com.neurolift.asfdk.integration;

import com.neurolift.asfdk.prompt.PromptDefense;
import com.neurolift.asfdk.prompt.SanitizationResult;
import com.neurolift.asfdk.prompt.RiskLevel;
import com.neurolift.asfdk.prompt.SecurityEventType;
import com.neurolift.asfdk.types.Channel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PROTOTYPE -- NOT A SAFETY SYSTEM.
 *
 * This adapter wraps an RRT Advocate crisis-detection engine. It is
 * not medical advice, not a crisis service, performs no real-time
 * monitoring, and can miss real crisis signals. Never rely on it as
 * the sole safety mechanism. If you or someone else needs help now,
 * in the US call or text 988 or chat https://988lifeline.org.
 *
 * Mirrors TS {@code integration/rrt.ts} from {@code @neurolift-technologies/asfdk} v0.2.4.
 */
public final class RrtAdvocateAdapter {

    /** Per-user crisis engines. Shared across channels in the Observe phase. */
    private static final Map<String, CrisisEngine> engines = new ConcurrentHashMap<>();

    private static CrisisEngine getEngine(String userId) {
        return engines.computeIfAbsent(userId, CrisisEngine::new);
    }

    /**
     * Runs the crisis-detection engine on a free-text input.
     *
     * Security: Input is sanitized to prevent prompt injection attacks
     * before assessment. A flagged result is logged but still assessed
     * defensively (fail-open on detection).
     */
    public static CrisisAssessmentWithProvenance assess(String userId, String input, Channel channel) {
        Channel resolved = Channel.normalize(channel);

        SanitizationResult sanitizationResult = PromptDefense.sanitizeInput(input);
        boolean flagged = !sanitizationResult.clean();
        if (flagged) {
            SecurityEventType type = sanitizationResult.riskLevel() == RiskLevel.HIGH
                    ? SecurityEventType.INJECTION_ATTEMPT
                    : SecurityEventType.VALIDATION_FAILURE;
            PromptDefense.logSecurityEvent(
                    type, userId,
                    sanitizationResult.reason() != null
                            ? sanitizationResult.reason()
                            : "Input sanitization flagged in RRT assessment",
                    System.currentTimeMillis()
            );
        }

        CrisisAssessment assessment = getEngine(userId).assess(sanitizationResult.content());
        return new CrisisAssessmentWithProvenance(
                assessment,
                resolved,
                resolved == Channel.USER_INPUT,
                flagged ? Boolean.TRUE : null,
                flagged ? sanitizationResult.reason() : null
        );
    }

    /** Re-baselines a single user's crisis-detection engine. */
    public static void resetSession(String userId) {
        getEngine(userId).resetSession();
    }

    /** Resets per-session detector state. Pass a {@code userId} to reset one user, or {@code null} to clear all. */
    public static void reset(String userId) {
        if (userId == null) {
            engines.clear();
        } else {
            engines.remove(userId);
        }
    }

    /** Returns the active RRT Advocate component status. */
    public static ComponentAdapterStatus getStatus() {
        return new ComponentAdapterStatus(true, "crisis-detection");
    }

    /**
     * Stub crisis engine. Provides deterministic crisis classification for integration testing.
     * Self-harm keywords -> RED; distress keywords -> ORANGE; all other -> GREEN.
     */
    static final class CrisisEngine {

        private static final java.util.List<String> SELF_HARM_KEYWORDS = java.util.List.of(
                "hurt myself", "want to die", "kill myself", "end my life",
                "suicide", "self-harm", "cut myself", "want to end"
        );

        private static final java.util.List<String> DISTRESS_KEYWORDS = java.util.List.of(
                "need help", "overwhelmed", "breaking down", "can't cope",
                "desperate", "hopeless", "crisis"
        );

        private final String userId;

        CrisisEngine(String userId) {
            this.userId = userId;
        }

        CrisisAssessment assess(String input) {
            String lower = input.toLowerCase();

            CrisisLevel level;
            if (SELF_HARM_KEYWORDS.stream().anyMatch(lower::contains)) {
                level = CrisisLevel.RED;
            } else if (DISTRESS_KEYWORDS.stream().anyMatch(lower::contains)) {
                level = CrisisLevel.ORANGE;
            } else {
                level = CrisisLevel.GREEN;
            }

            double safetyScore = switch (level) {
                case GREEN -> 0.9;
                case YELLOW -> 0.7;
                case ORANGE -> 0.4;
                case RED -> 0.1;
                case BLACK -> 0.0;
            };

            java.util.List<String> interventions = switch (level) {
                case RED, BLACK -> java.util.List.of("immediate_safety_assessment", "crisis_resources");
                case ORANGE -> java.util.List.of("safety_check_in", "resource_provision");
                default -> java.util.List.of();
            };

            java.util.Map<String, Object> metadata = new java.util.HashMap<>();
            metadata.put("userId", userId);
            return new CrisisAssessment(level, safetyScore, interventions, metadata);
        }

        void resetSession() {
            // Stub: no persistent state to clear in this implementation
        }
    }
}
