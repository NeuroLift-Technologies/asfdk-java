package com.neurolift.asfdk.foundation;

import com.neurolift.asfdk.integration.*;
import com.neurolift.asfdk.prompt.OutputSchema;
import com.neurolift.asfdk.prompt.PromptDefense;
import com.neurolift.asfdk.prompt.SecurityEventType;
import com.neurolift.asfdk.prompt.SanitizationResult;
import com.neurolift.asfdk.prompt.ValidationResult;
import com.neurolift.asfdk.types.*;

import java.util.*;

public final class NeuroLiftFoundation {

    private final FoundationConfig config;
    private final ComponentFlags active;
    private boolean initialized;
    private ToiDocument toiDocument;

    public NeuroLiftFoundation(FoundationConfig config) {
        this.config = Objects.requireNonNull(config, "FoundationConfig must not be null");
        this.active = resolveComponents(config.mode(), config.components());
    }

    public synchronized void initialize() {
        toiDocument = generateToi();
        initialized = true;
    }

    public synchronized void start() {
        if (!initialized) {
            initialize();
        }
    }

    public ToiDocument getActiveToi() {
        return toiDocument;
    }

    public FoundationResponse processInteraction(UserInteraction interaction) {
        List<String> components = new ArrayList<>();
        Map<String, Object> content = new HashMap<>();

        Channel channel = Channel.normalize(interaction.channel());
        boolean trusted = channel == Channel.USER_INPUT;

        boolean highSeverity = false;

        if (active.swp() && interaction.interactionType() == InteractionType.EMOTIONAL_ASSESSMENT) {
            try {
                String input = interaction.data().get("text") != null
                        ? interaction.data().get("text").toString() : "";
                EmotionalStateWithProvenance stateResult = SleepwalkerAdapter.detectEmotionalState(
                        input, List.of(), channel, interaction.userId()
                );
                EmotionalState state = stateResult.emotionalState();
                highSeverity = state.explicitSuicidalIdeation()
                        || state.selfHarmIndicators()
                        || state.inabilityToEnsureSafety();
                content.put("emotionalState", state);

                if (active.rrt() && SleepwalkerAdapter.requiresRrtaHandoff(state)) {
                    try {
                        CrisisAssessmentWithProvenance rrtResult = (CrisisAssessmentWithProvenance) validateComponentOutput(
                                RrtAdvocateAdapter.assess(interaction.userId(), input, channel)
                        );
                        if (rrtResult != null) {
                            content.put("rrt", rrtResult);
                        }
                        components.add("rrt_advocate");
                    } catch (Exception err) {
                        content.put("error", Map.of("component", "rrt_advocate", "message", err.toString()));
                    }
                }
            } catch (Exception err) {
                content.put("error", Map.of("component", "sleepwalker_protocol", "message", err.toString()));
            }
            components.add("sleepwalker_protocol");
        }

        if (active.toi() && interaction.interactionType() == InteractionType.PREFERENCE_UPDATE) {
            Object payload = interaction.data().get("toi");
            content.put("toiValidation", ToiOtoiAdapter.validateTOI(payload));
            components.add("toi_otoi_framework");
        }

        if (active.rrt() && (interaction.interactionType() == InteractionType.CRISIS_ALERT
                || interaction.interactionType() == InteractionType.EMERGENCY_ESCALATION)) {
            String input = interaction.data().get("text") != null
                    ? interaction.data().get("text").toString() : "";
            try {
                // Assess from the raw input first so crisis severity is never
                // suppressed by the output-filtering gate (Codex P1): a
                // caller-controlled userId in the record metadata must not be
                // able to match LEAK_PATTERNS and hide a genuine crisis.
                CrisisAssessmentWithProvenance raw = RrtAdvocateAdapter.assess(
                        interaction.userId(), input, channel
                );
                if (interaction.interactionType() == InteractionType.CRISIS_ALERT) {
                    CrisisLevel level = raw.assessment().crisisLevel();
                    highSeverity = level == CrisisLevel.RED || level == CrisisLevel.BLACK;
                }
                CrisisAssessmentWithProvenance rrtResult = (CrisisAssessmentWithProvenance) validateComponentOutput(raw);
                if (rrtResult != null) {
                    content.put("rrt", rrtResult);
                }
            } catch (Exception err) {
                content.put("error", Map.of("component", "rrt_advocate", "message", err.toString()));
            }
            components.add("rrt_advocate");
        }

        if (interaction.interactionType() == InteractionType.EMERGENCY_ESCALATION) {
            highSeverity = true;
        }
        if (interaction.interactionType() == InteractionType.CRISIS_ALERT && !active.rrt()) {
            highSeverity = true;
        }

        content.put("channel", channel);
        content.put("trusted", trusted);
        content.put("gateUp", !trusted && highSeverity);

        return new FoundationResponse(
                0L,
                interaction.interactionType().value(),
                content,
                components,
                true
        );
    }

    public Map<String, Object> assessEmotionalState(String input, Map<String, Object> context, Channel channel) {
        if (!active.swp()) {
            return null;
        }
        Channel resolved = Channel.normalize(channel);
        boolean trusted = resolved == Channel.USER_INPUT;

        // Route the supplied input through the Sleepwalker classifier (Codex P1).
        @SuppressWarnings("unchecked")
        Map<String, Object> result = new HashMap<>(SleepwalkerAdapter.assessInteraction(input, List.of(), resolved));
        EmotionalState inner = (EmotionalState) result.get("emotionalState");
        if (inner == null) {
            inner = new EmotionalState();
        }
        boolean highSeverity = inner.explicitSuicidalIdeation()
                || inner.selfHarmIndicators()
                || inner.inabilityToEnsureSafety();
        result.put("channel", resolved);
        result.put("trusted", trusted);
        result.put("gateUp", !trusted && highSeverity);
        return result;
    }

    public void updatePreferences(Map<String, Object> prefs) {
        if (active.toi()) {
            TOIValidationResult result = ToiOtoiAdapter.validateTOI(prefs);
            if (!result.valid()) {
                throw new IllegalStateException("TOI validation failed: " + result.errors());
            }
        }
    }

    public Map<String, Object> getSystemStatus() {
        Map<String, Object> components = new HashMap<>();
        components.put("toi_otoi_framework",
                active.toi() ? ToiOtoiAdapter.getStatus() : new ComponentAdapterStatus(false, "disabled"));
        components.put("sleepwalker_protocol",
                active.swp() ? SleepwalkerAdapter.getStatus() : new ComponentAdapterStatus(false, "disabled"));
        components.put("rrt_advocate",
                active.rrt() ? RrtAdvocateAdapter.getStatus() : new ComponentAdapterStatus(false, "disabled"));
        Map<String, Object> status = new HashMap<>();
        status.put("mode", config.mode().value());
        status.put("userId", config.userId());
        status.put("initialized", initialized);
        Map<String, Object> toi = new HashMap<>();
        toi.put("generated", toiDocument != null);
        toi.put("document", toiDocument);
        status.put("toi", toi);
        status.put("components", components);
        return status;
    }

    public HealthCheckResult healthCheck() {
        Map<String, Object> status = getSystemStatus();
        @SuppressWarnings("unchecked")
        Map<String, ComponentAdapterStatus> compMap = (Map<String, ComponentAdapterStatus>) status.get("components");
        Map<String, ComponentStatus> components = new HashMap<>();
        for (Map.Entry<String, ComponentAdapterStatus> entry : compMap.entrySet()) {
            components.put(entry.getKey(),
                    new ComponentStatus(entry.getValue().active(), entry.getValue().mode()));
        }
        return new HealthCheckResult(true, components, 0L);
    }

    public void shutdown() {
        RrtAdvocateAdapter.reset(config.userId());
        initialized = false;
    }

    private ToiDocument generateToi() {
        Object source = config.toi();
        if (source == null) {
            return new ToiDocument("1.0.0", "personal", new ToiIdentity("anonymous"));
        }
        if (source instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) source;
            TOIValidationResult result = ToiOtoiAdapter.validateTOI(map);
            if (result.valid() && result.toi() != null) {
                return result.toi();
            }
            throw new IllegalStateException("Invalid TOI source: " + result.errors());
        }
        if (source instanceof String path) {
            java.io.File file = new java.io.File(path);
            if (!file.exists()) {
                throw new IllegalArgumentException("TOI source file not found: " + path);
            }
            try {
                Map<String, Object> map = Json.parseFile(file);
                TOIValidationResult result = ToiOtoiAdapter.validateTOI(map);
                if (result.valid() && result.toi() != null) {
                    return result.toi();
                }
                throw new IllegalStateException("Invalid TOI file content: " + result.errors());
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException("Invalid TOI file content: " + e.getMessage());
            }
        }
        throw new IllegalArgumentException(
                "Invalid TOI source type: " + (source.getClass().getSimpleName()));
    }

    private Object validateComponentOutput(Object result) {
        if (result == null) {
            return null;
        }
        String serialized = result.toString();
        ValidationResult outputValid = PromptDefense.validateOutput(serialized, null); // leak detection only; internal objects are not LLM output
        if (outputValid.valid()) {
            return result;
        }
        PromptDefense.logSecurityEvent(
                SecurityEventType.VALIDATION_FAILURE,
                config.userId(),
                "Component output validation failed: " + outputValid.reason(),
                System.currentTimeMillis()
        );
        return null;
    }

    static ComponentFlags resolveComponents(FoundationMode mode, ComponentOverrides overrides) {
        // Fail-loud (T16): an unrecognized/null mode must never silently disable every component.
        if (mode == null) {
            throw new IllegalArgumentException("FoundationMode must not be null");
        }
        ComponentFlags defaults = switch (mode) {
            case UNIFIED -> new ComponentFlags(true, true, true);
            case CRISIS_ONLY -> new ComponentFlags(false, false, true);
            case CONTINUITY_ONLY -> new ComponentFlags(false, true, false);
            case FRAMEWORK_ONLY -> new ComponentFlags(true, false, false);
            case DEVELOPMENT -> new ComponentFlags(true, true, false);
        };
        return new ComponentFlags(
                overrides != null && overrides.toiOtoiFramework() != null ? overrides.toiOtoiFramework() : defaults.toi(),
                overrides != null && overrides.sleepwalkerProtocol() != null ? overrides.sleepwalkerProtocol() : defaults.swp(),
                overrides != null && overrides.rrtAdvocate() != null ? overrides.rrtAdvocate() : defaults.rrt()
        );
    }

    record ComponentFlags(boolean toi, boolean swp, boolean rrt) {}
}
