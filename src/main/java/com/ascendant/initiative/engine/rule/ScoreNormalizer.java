package com.ascendant.initiative.engine.rule;

import org.springframework.stereotype.Component;

@Component
public class ScoreNormalizer {

    // Weights must sum to 1.0
    private static final double WEIGHT_LENGTH    = 0.25;
    private static final double WEIGHT_DENSITY   = 0.30;
    private static final double WEIGHT_CONNECTOR = 0.25;
    private static final double WEIGHT_HYPOTHESIS = 0.20;

    public double normalize(double lengthScore, double densityScore,
                            double connectorScore, double hypothesisScore) {
        double result = (lengthScore    * WEIGHT_LENGTH)
                      + (densityScore   * WEIGHT_DENSITY)
                      + (connectorScore * WEIGHT_CONNECTOR)
                      + (hypothesisScore * WEIGHT_HYPOTHESIS);
        return Math.max(0.0, Math.min(1.0, result));
    }
}
