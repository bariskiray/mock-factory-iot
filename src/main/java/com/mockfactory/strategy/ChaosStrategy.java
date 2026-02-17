package com.mockfactory.strategy;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Chaos / fault-injection signal generator.
 * <p>
 * Simulates a sensor that is <em>mostly</em> stable but occasionally
 * produces a catastrophic reading — the kind of spike that a SCADA
 * alarm system must catch.
 * <ul>
 *   <li><b>95 %</b> of ticks → small random drift around the current value
 *       (mimics normal instrument noise).</li>
 *   <li><b>5 %</b>  of ticks → critical fault value ({@code -999} or
 *       {@code max × 2}), representing a sensor wire-break or a
 *       transmitter saturation condition.</li>
 * </ul>
 * QA engineers use this strategy to verify that the UI correctly renders
 * alarm states and out-of-range markers.
 */
@Component("CHAOS")
public class ChaosStrategy implements DataGenerationStrategy {

    /** Probability threshold for injecting a fault (5 %). */
    private static final double FAULT_PROBABILITY = 0.05;

    /** Sentinel value representing a sensor wire-break / open-circuit. */
    private static final double WIRE_BREAK_VALUE = -999.0;

    @Override
    public double generate(double current, double min, double max) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        if (rng.nextDouble() < FAULT_PROBABILITY) {
            // ── Fault injection ────────────────────────────────
            // Randomly choose between wire-break and transmitter saturation.
            return rng.nextBoolean() ? WIRE_BREAK_VALUE : max * 2;
        }

        // ── Normal operation: small drift ±2 % of range ───────
        double range = max - min;
        double noise = (rng.nextDouble() - 0.5) * range * 0.04;
        double next  = current + noise;

        // Clamp within engineering limits during normal operation.
        next = Math.max(min, Math.min(max, next));
        return Math.round(next * 100.0) / 100.0;
    }
}
