package com.neurolift.asfdk.integration;

import java.util.List;
import java.util.Map;

/**
 * A crisis assessment result from the RRT Advocate engine.
 *
 * Mirrors TS {@code CrisisAssessment} from {@code @neurolift-technologies/rrt-advocate}.
 *
 * @param crisisLevel              the classified crisis level
 * @param safetyScore              safety score in [0.0, 1.0]
 * @param recommendedInterventions recommended intervention labels
 * @param metadata                 arbitrary metadata (e.g. {@code userId})
 */
public record CrisisAssessment(
        CrisisLevel crisisLevel,
        double safetyScore,
        List<String> recommendedInterventions,
        Map<String, Object> metadata
) {
    public CrisisAssessment {
        recommendedInterventions = recommendedInterventions == null ? List.of() : recommendedInterventions;
        metadata = metadata == null ? Map.of() : metadata;
    }
}