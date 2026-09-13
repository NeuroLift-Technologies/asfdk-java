package com.neurolift.asfdk;

import com.neurolift.asfdk.foundation.NeuroLiftFoundation;
import com.neurolift.asfdk.types.FoundationConfig;
import com.neurolift.asfdk.types.FoundationMode;

/**
 * Entry point for constructing and initializing a {@link NeuroLiftFoundation} instance.
 *
 * Mirrors TS {@code createFoundation} from {@code @neurolift-technologies/asfdk} v0.2.4.
 */
public final class ASFDK {

    private ASFDK() {
        // static utility only
    }

    /**
     * Constructs and initializes a {@link NeuroLiftFoundation} instance.
     *
     * @param userId the user identifier
     * @param mode   foundation mode; defaults to {@link FoundationMode#UNIFIED}
     * @return an initialized {@link NeuroLiftFoundation}
     */
    public static NeuroLiftFoundation createFoundation(String userId, FoundationMode mode) {
        FoundationConfig config = new FoundationConfig(userId, mode);
        NeuroLiftFoundation foundation = new NeuroLiftFoundation(config);
        foundation.initialize();
        return foundation;
    }

    /**
     * Constructs and initializes a {@link NeuroLiftFoundation} from a full config object.
     *
     * @param config full foundation configuration
     * @return an initialized {@link NeuroLiftFoundation}
     */
    public static NeuroLiftFoundation createFoundation(FoundationConfig config) {
        NeuroLiftFoundation foundation = new NeuroLiftFoundation(config);
        foundation.initialize();
        return foundation;
    }
}
