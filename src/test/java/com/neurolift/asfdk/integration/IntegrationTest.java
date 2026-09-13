package com.neurolift.asfdk.integration;

import com.neurolift.asfdk.prompt.SecurityEventType;
import com.neurolift.asfdk.types.Channel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToiOtoiAdapterTest {

    @Test
    void validateTOI_acceptsAValidTOIDocument() {
        TOIValidationResult result = ToiOtoiAdapter.validateTOI(
                java.util.Map.of(
                        "$toi", "1.0.0",
                        "$tier", "personal",
                        "identity", java.util.Map.of("author", "test-user")
                )
        );
        assertTrue(result.valid());
        assertNotNull(result.toi());
        assertEquals("1.0.0", result.toi().toi());
        assertEquals("personal", result.toi().tier());
        assertEquals("test-user", result.toi().identity().author());
    }

    @Test
    void validateTOI_rejectsNull() {
        assertFalse(ToiOtoiAdapter.validateTOI(null).valid());
    }

    @Test
    void validateTOI_rejectsNonMapInput() {
        assertFalse(ToiOtoiAdapter.validateTOI("not a map").valid());
    }

    @Test
    void validateTOI_rejectsMissingRequiredFields() {
        TOIValidationResult result = ToiOtoiAdapter.validateTOI(java.util.Map.of("foo", "bar"));
        assertFalse(result.valid());
        assertTrue(result.errors() != null && !result.errors().isEmpty());
    }

    @Test
    void validateTOI_rejectsInvalidVersionFormat() {
        TOIValidationResult result = ToiOtoiAdapter.validateTOI(
                java.util.Map.of(
                        "$toi", "invalid",
                        "$tier", "personal",
                        "identity", java.util.Map.of("author", "test")
                )
        );
        assertFalse(result.valid());
    }

    @Test
    void validateTOI_rejectsMissingIdentityAuthor() {
        TOIValidationResult result = ToiOtoiAdapter.validateTOI(
                java.util.Map.of(
                        "$toi", "1.0.0",
                        "$tier", "personal",
                        "identity", java.util.Map.of("not_author", "test")
                )
        );
        assertFalse(result.valid());
    }

    @Test
    void getStatus_returnsActiveToiOtoiValidation() {
        ComponentAdapterStatus status = ToiOtoiAdapter.getStatus();
        assertTrue(status.active());
        assertEquals("toi-otoi-validation", status.mode());
    }
}

class RrtAdvocateAdapterTest {

    @Test
    void getStatus_returnsActiveCrisisDetection() {
        ComponentAdapterStatus status = RrtAdvocateAdapter.getStatus();
        assertTrue(status.active());
        assertEquals("crisis-detection", status.mode());
    }

    @Test
    void assess_returnsGreenForNonCrisisInput() {
        CrisisAssessmentWithProvenance result = RrtAdvocateAdapter.assess("user1", "just checking in, doing fine", null);
        assertEquals(CrisisLevel.GREEN, result.assessment().crisisLevel());
        assertFalse(result.trusted()); // canonical: absent channel = UNKNOWN = untrusted
        assertEquals(Channel.UNKNOWN, result.channel());
    }

    @Test
    void assess_returnsRedForSelfHarmInput() {
        CrisisAssessmentWithProvenance result = RrtAdvocateAdapter.assess("user2", "I want to hurt myself", null);
        assertEquals(CrisisLevel.RED, result.assessment().crisisLevel());
    }

    @Test
    void assess_recordsChannelProvenance() {
        CrisisAssessmentWithProvenance result = RrtAdvocateAdapter.assess("user3", "I feel overwhelmed", Channel.MODEL_OUTPUT);
        assertEquals(Channel.MODEL_OUTPUT, result.channel());
        assertFalse(result.trusted());
    }

    @Test
    void resetSession_doesNotThrow() {
        assertDoesNotThrow(() -> RrtAdvocateAdapter.resetSession("user1"));
    }

    @Test
    void reset_clearsAllEngines() {
        assertDoesNotThrow(() -> RrtAdvocateAdapter.reset(null));
    }
}

class SleepwalkerAdapterTest {

    @Test
    void getStatus_returnsActiveEmotionalContinuity() {
        ComponentAdapterStatus status = SleepwalkerAdapter.getStatus();
        assertTrue(status.active());
        assertEquals("emotional-continuity", status.mode());
    }

    @Test
    void detectEmotionalState_returnsEmotionalStateWithProvenance() {
        EmotionalStateWithProvenance result = SleepwalkerAdapter.detectEmotionalState(
                "I feel overwhelmed today", Channel.USER_INPUT, "test"
        );
        assertNotNull(result.emotionalState());
        assertEquals(Channel.USER_INPUT, result.channel());
        assertTrue(result.trusted()); // USER_INPUT is the only trusted channel
    }

    @Test
    void detectEmotionalState_recordsChannelProvenance() {
        EmotionalStateWithProvenance result = SleepwalkerAdapter.detectEmotionalState(
                "I feel overwhelmed today", Channel.MODEL_OUTPUT
        );
        assertEquals(Channel.MODEL_OUTPUT, result.channel());
        assertFalse(result.trusted());
    }

    @Test
    void detectEmotionalState_defaultsToUnknownWhenChannelAbsent() {
        EmotionalStateWithProvenance result = SleepwalkerAdapter.detectEmotionalState("I feel overwhelmed today");
        assertEquals(Channel.UNKNOWN, result.channel());
        assertFalse(result.trusted());
    }

    @Test
    void requiresRrtaHandoff_returnsTrueForCrisisState() {
        EmotionalState state = new EmotionalState(0.0, 0.0, "crisis", true, false, true, 0.0, java.util.List.of());
        assertTrue(SleepwalkerAdapter.requiresRrtaHandoff(state));
    }

    @Test
    void requiresRrtaHandoff_returnsFalseForNonCrisisState() {
        EmotionalState state = new EmotionalState();
        assertFalse(SleepwalkerAdapter.requiresRrtaHandoff(state));
    }

    @Test
    void assessInteraction_returnsMapWithEmotionalStateAndChannel() {
        java.util.Map<String, Object> result = SleepwalkerAdapter.assessInteraction(
                "I feel overwhelmed today", java.util.List.of(), Channel.USER_INPUT
        );
        assertTrue(result.containsKey("emotionalState"));
        assertEquals(Channel.USER_INPUT, result.get("channel"));
        assertEquals(Boolean.TRUE, result.get("trusted"));
    }
}
