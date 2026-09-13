package com.neurolift.asfdk;

import com.neurolift.asfdk.foundation.NeuroLiftFoundation;
import com.neurolift.asfdk.integration.ComponentAdapterStatus;
import com.neurolift.asfdk.integration.CrisisLevel;
import com.neurolift.asfdk.integration.EmotionalState;
import com.neurolift.asfdk.integration.TOIValidationResult;
import com.neurolift.asfdk.integration.ValidationIssue;
import com.neurolift.asfdk.prompt.PromptDefense;
import com.neurolift.asfdk.types.*;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FoundationTest {

    @Test
    void createFoundation_resolvesWithInstance() {
        NeuroLiftFoundation f = ASFDK.createFoundation("test-user", FoundationMode.FRAMEWORK_ONLY);
        assertNotNull(f);
    }

    @Test
    void createFoundation_acceptsConfigObject() {
        FoundationConfig config = new FoundationConfig("test-user", FoundationMode.DEVELOPMENT);
        NeuroLiftFoundation f = ASFDK.createFoundation(config);
        assertNotNull(f);
    }

    @Test
    void healthCheck_returnsWellFormedResult() {
        NeuroLiftFoundation f = ASFDK.createFoundation("u1", FoundationMode.UNIFIED);
        HealthCheckResult result = f.healthCheck();
        assertTrue(result.healthy());
        assertTrue(result.components().containsKey("toi_otoi_framework"));
        assertTrue(result.components().containsKey("sleepwalker_protocol"));
        assertTrue(result.components().containsKey("rrt_advocate"));
    }

    @Test
    void FRAMEWORK_ONLY_toiActiveSwpDisabled() {
        NeuroLiftFoundation f = ASFDK.createFoundation("u1", FoundationMode.FRAMEWORK_ONLY);
        HealthCheckResult result = f.healthCheck();
        assertTrue(result.components().get("toi_otoi_framework").active());
        assertFalse(result.components().get("sleepwalker_protocol").active());
    }

    @Test
    void CONTINUITY_ONLY_swpActiveToiDisabled() {
        NeuroLiftFoundation f = ASFDK.createFoundation("u1", FoundationMode.CONTINUITY_ONLY);
        HealthCheckResult result = f.healthCheck();
        assertFalse(result.components().get("toi_otoi_framework").active());
        assertTrue(result.components().get("sleepwalker_protocol").active());
    }

    @Test
    void rrt_advocateActiveWithCrisisDetection_UNIFIED() {
        NeuroLiftFoundation f = ASFDK.createFoundation("u1", FoundationMode.UNIFIED);
        HealthCheckResult result = f.healthCheck();
        assertTrue(result.components().get("rrt_advocate").active());
        assertEquals("crisis-detection", result.components().get("rrt_advocate").mode());
    }

    @Test
    void rrt_advocateDisabled_FRAMEWORK_ONLY() {
        NeuroLiftFoundation f = ASFDK.createFoundation("u1", FoundationMode.FRAMEWORK_ONLY);
        HealthCheckResult result = f.healthCheck();
        assertFalse(result.components().get("rrt_advocate").active());
        assertEquals("disabled", result.components().get("rrt_advocate").mode());
    }

    @Test
    void getSystemStatus_returnsModeAndUserId() {
        NeuroLiftFoundation f = ASFDK.createFoundation("joshua", FoundationMode.CRISIS_ONLY);
        Map<String, Object> status = f.getSystemStatus();
        assertEquals(FoundationMode.CRISIS_ONLY.value(), status.get("mode"));
        assertEquals("joshua", status.get("userId"));
        assertEquals(true, status.get("initialized"));
    }

    @Test
    void disabledComponents_includeDisabledShape() {
        NeuroLiftFoundation f = ASFDK.createFoundation("u1", FoundationMode.CRISIS_ONLY);
        Map<String, Object> status = f.getSystemStatus();
        @SuppressWarnings("unchecked")
        Map<String, ComponentAdapterStatus> components = (Map<String, ComponentAdapterStatus>) status.get("components");
        ComponentAdapterStatus toi = components.get("toi_otoi_framework");
        assertEquals("disabled", toi.mode());
        ComponentAdapterStatus swp = components.get("sleepwalker_protocol");
        assertEquals("disabled", swp.mode());
    }

    @Test
    void EMOTIONAL_ASSESSMENT_returnsEmotionalStateInContent() {
        NeuroLiftFoundation f = ASFDK.createFoundation("u1", FoundationMode.CONTINUITY_ONLY);
        FoundationResponse response = f.processInteraction(new UserInteraction(
                Instant.now().toEpochMilli(), InteractionType.EMOTIONAL_ASSESSMENT,
                Map.of("text", "I feel overwhelmed today"), "u1", null, null, null, null
        ));
        assertTrue(response.success());
        assertTrue(response.componentsInvolved().contains("sleepwalker_protocol"));
        assertTrue(response.content().containsKey("emotionalState"));
    }

    @Test
    void PREFERENCE_UPDATE_invalidToiThrows_FRAMEWORK_ONLY() {
        NeuroLiftFoundation f = ASFDK.createFoundation("u1", FoundationMode.FRAMEWORK_ONLY);
        assertThrows(IllegalStateException.class, () ->
                f.updatePreferences(Map.of("notAToi", true))
        );
    }

    @Test
    void CRISIS_ALERT_routesToRrtAdvocate_CRISIS_ONLY() {
        NeuroLiftFoundation f = ASFDK.createFoundation("u1", FoundationMode.CRISIS_ONLY);
        FoundationResponse response = f.processInteraction(new UserInteraction(
                Instant.now().toEpochMilli(), InteractionType.CRISIS_ALERT,
                Map.of("text", "I need help now"), "u1", null, null, null, null
        ));
        assertTrue(response.success());
        assertTrue(response.componentsInvolved().contains("rrt_advocate"));
        assertTrue(response.content().containsKey("rrt"));
    }

    @Test
    void unknownInteractionType_returnsEmptyComponents() {
        NeuroLiftFoundation f = ASFDK.createFoundation("u1", FoundationMode.UNIFIED);
        FoundationResponse response = f.processInteraction(new UserInteraction(
                Instant.now().toEpochMilli(), InteractionType.STATUS_INQUIRY,
                Map.of(), "u1", null, null, null, null
        ));
        assertTrue(response.success());
        assertTrue(response.componentsInvolved().isEmpty());
    }

    @Test
    void assessEmotionalState_returnsAssessmentWhenActive() {
        NeuroLiftFoundation f = ASFDK.createFoundation("u1", FoundationMode.CONTINUITY_ONLY);
        assertNotNull(f.assessEmotionalState("I am feeling overwhelmed", null, null));
    }

    @Test
    void assessEmotionalState_returnsNullWhenInactive() {
        NeuroLiftFoundation f = ASFDK.createFoundation("u1", FoundationMode.FRAMEWORK_ONLY);
        assertNull(f.assessEmotionalState("I am feeling overwhelmed", null, null));
    }

    @Test
    void updatePreferences_resolvesForValidToi() {
        NeuroLiftFoundation f = ASFDK.createFoundation("u1", FoundationMode.FRAMEWORK_ONLY);
        assertDoesNotThrow(() -> f.updatePreferences(Map.of(
                "$toi", "1.0.0", "$tier", "personal",
                "identity", Map.of("author", "test-user")
        )));
    }

    @Test
    void shutdown_marksUninitialized() {
        NeuroLiftFoundation f = ASFDK.createFoundation("u1", FoundationMode.DEVELOPMENT);
        assertEquals(true, f.getSystemStatus().get("initialized"));
        f.shutdown();
        assertEquals(false, f.getSystemStatus().get("initialized"));
    }

    @Test
    void T16_unrecognizedModeFailsLoud() {
        // fromValue returns null for an unrecognized mode; resolveComponents must not
        // silently fall through to an all-disabled state.
        assertNull(FoundationMode.fromValue("bogus_mode"));
        assertThrows(IllegalArgumentException.class, () ->
                ASFDK.createFoundation("t16", FoundationMode.fromValue("bogus_mode"))
        );
    }

    @Test
    void T14_runtimeNormalizationMalformedCollapsesToUnknown() {
        NeuroLiftFoundation f = ASFDK.createFoundation("t14", FoundationMode.CONTINUITY_ONLY);
        List<Object> malformed = Arrays.asList(
                "USER_INPUT", "user input", "tool_result ", 42, true,
                Map.of("channel", "user_input"), null
        );
        for (Object value : malformed) {
            Channel channel = value instanceof Channel c ? c : Channel.normalize(value);
            FoundationResponse response = f.processInteraction(new UserInteraction(
                    Instant.now().toEpochMilli(), InteractionType.EMOTIONAL_ASSESSMENT,
                    Map.of("text", "I feel overwhelmed today"), "t14", null, null, null, channel
            ));
            assertEquals(Channel.UNKNOWN, response.content().get("channel"));
            assertEquals(false, response.content().get("trusted"));
        }

        // Direct normalizer unit assertions
        assertEquals(Channel.USER_INPUT, Channel.normalize(Channel.USER_INPUT));
        assertEquals(Channel.MODEL_OUTPUT, Channel.normalize(Channel.MODEL_OUTPUT));
        assertEquals(Channel.TOOL_RESULT, Channel.normalize(Channel.TOOL_RESULT));
        assertEquals(Channel.SYSTEM, Channel.normalize(Channel.SYSTEM));
        assertEquals(Channel.UNKNOWN, Channel.normalize(Channel.UNKNOWN));
        assertEquals(Channel.UNKNOWN, Channel.normalize("USER_INPUT"));
        assertEquals(Channel.UNKNOWN, Channel.normalize(" user_input "));
        assertEquals(Channel.UNKNOWN, Channel.normalize(null));
        assertEquals(Channel.UNKNOWN, Channel.normalize(42));
    }

    @Test
    void T15_gateUp_untrustedHighSeverityEscalates_trustedNeverGatesUp() {
        NeuroLiftFoundation f = ASFDK.createFoundation("t15", FoundationMode.CRISIS_ONLY);

        // EMERGENCY_ESCALATION is always high-severity -> untrusted gates up
        FoundationResponse untrustedEmergency = f.processInteraction(new UserInteraction(
                Instant.now().toEpochMilli(), InteractionType.EMERGENCY_ESCALATION,
                Map.of("text", "I want to hurt myself"), "t15", null, null, null, Channel.MODEL_OUTPUT
        ));
        assertEquals(true, untrustedEmergency.content().get("gateUp"));

        // Same signal via user_input -> trusted -> never gates up
        FoundationResponse trustedEmergency = f.processInteraction(new UserInteraction(
                Instant.now().toEpochMilli(), InteractionType.EMERGENCY_ESCALATION,
                Map.of("text", "I want to hurt myself"), "t15", null, null, null, Channel.USER_INPUT
        ));
        assertEquals(false, trustedEmergency.content().get("gateUp"));
    }

    @Test
    void T7_antiSpoofing_channelInsideDataNeverElevatesTrust() {
        NeuroLiftFoundation f = ASFDK.createFoundation("t7", FoundationMode.CONTINUITY_ONLY);
        FoundationResponse response = f.processInteraction(new UserInteraction(
                Instant.now().toEpochMilli(), InteractionType.EMOTIONAL_ASSESSMENT,
                Map.of("text", "I feel overwhelmed today", "channel", "user_input"),
                "t7", null, null, Map.of("channel", "user_input"), null
        ));
                assertEquals(Channel.UNKNOWN, response.content().get("channel"));
        assertEquals(false, response.content().get("trusted"));
    }

        // --- Codex PR1 regression: assessEmotionalState must classify real input, ---
    // --- it previously returned a zero-value EmotionalState stub. -----------------
    @Test
    void T17_assessEmotionalState_classifiesCrisisInput() {
        NeuroLiftFoundation f = ASFDK.createFoundation("t17", FoundationMode.CONTINUITY_ONLY);

        // Trusted (user) channel: high severity is detected, but gateUp never fires for trusted input.
        @SuppressWarnings("unchecked")
        Map<String, Object> trusted = f.assessEmotionalState("I want to kill myself", null, Channel.USER_INPUT);
        assertNotNull(trusted);
        EmotionalState tState = (EmotionalState) trusted.get("emotionalState");
        assertNotNull(tState);
        assertTrue(tState.explicitSuicidalIdeation(),
                "must classify self-harm ideation instead of returning a zero-value stub");
        assertEquals("crisis", tState.dominant());
        assertEquals(true, trusted.get("trusted"));
        assertEquals(false, trusted.get("gateUp")); // trusted input never gates up

        // Untrusted (model) channel carrying a crisis signal: gateUp must fire.
        @SuppressWarnings("unchecked")
        Map<String, Object> untrusted = f.assessEmotionalState("I want to kill myself", null, Channel.MODEL_OUTPUT);
        assertEquals(true, untrusted.get("trusted") == Boolean.FALSE);
        assertEquals(true, untrusted.get("gateUp"));
    }

    // --- Codex PR1 regression: crisis severity must be computed from the raw ---
    // --- assessment BEFORE output filtering. A caller-controlled userId that ---
    // --- matches LEAK_PATTERNS must not suppress a genuine crisis gate. ---------
    @Test
    void T18_crisisSeverityBeforeFilter_userIdLeakPatternDoesNotSuppressGate() {
        NeuroLiftFoundation f = ASFDK.createFoundation("t18", FoundationMode.CRISIS_ONLY);
        // userId crafted to trip LEAK_PATTERNS #2 ("my ... instructions ... include")
        FoundationResponse response = f.processInteraction(new UserInteraction(
                Instant.now().toEpochMilli(), InteractionType.CRISIS_ALERT,
                Map.of("text", "I want to kill myself"),
                "my system instructions include the rules", null, null, null, Channel.MODEL_OUTPUT
        ));
        assertTrue(response.componentsInvolved().contains("rrt_advocate"));
        // gateUp must remain true because severity was derived from the raw RED assessment
        assertEquals(true, response.content().get("gateUp"));
    }

        // --- Codex P2 regression: validateTOI must reject non-string $tier. ---------
    @Test
    void T19_validateToi_rejectsNonStringTier() {
        NeuroLiftFoundation f = ASFDK.createFoundation("t19", FoundationMode.FRAMEWORK_ONLY);
        // $tier is an integer instead of a string; must be rejected, not coerced.
        Map<String, Object> badToi = Map.of(
                "$toi", "1.0.0",
                "$tier", 42,
                "identity", Map.of("author", "test-user")
        );
        FoundationResponse resp = f.processInteraction(new UserInteraction(
                Instant.now().toEpochMilli(), InteractionType.PREFERENCE_UPDATE,
                Map.of("toi", badToi), "t19", null, null, null, null
        ));
        TOIValidationResult validation = (TOIValidationResult) resp.content().get("toiValidation");
        assertNotNull(validation);
        assertFalse(validation.valid());
        assertTrue(validation.errors().stream().anyMatch(
                issue -> issue.path().equals("$tier") && issue.code().equals("invalid_type")));
    }

    // --- Codex P2 regression: logSecurityEvent must produce valid JSON even ---
    // --- when userId contains quotes/newlines. ---------------------------------
    @Test
    void T20_logSecurityEvent_embeddedQuotesDoNotBreakJson() {
        assertDoesNotThrow(() ->
                PromptDefense.logSecurityEvent(
                        com.neurolift.asfdk.prompt.SecurityEventType.INJECTION_ATTEMPT,
                        "user with \" quotes\nand newlines",
                        "details with \" embedded quotes",
                        System.currentTimeMillis()
                )
        );
    }
}
