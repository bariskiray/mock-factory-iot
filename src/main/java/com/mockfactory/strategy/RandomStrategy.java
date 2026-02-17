package com.mockfactory.strategy;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Bounded random-walk signal generator (moderate noise).
 * <p>
 * A noisier cousin of {@link RealisticStrategy}.  Instead of the
 * gentle ±1.5 % drift of Brownian motion, this strategy applies a
 * wider ±5 % step per tick — producing a signal that still
 * <em>correlates</em> with the previous reading (no teleportation)
 * but wanders faster, like a vibration sensor on a rotating machine
 * that has some bearing play.
 * <p>
 * Boundary handling uses the same reflection / bounce logic so the
 * value never leaves the engineering-unit range.
 */
@Component("RANDOM")
public class RandomStrategy implements DataGenerationStrategy {

    /**
     * Per-tick drift as a fraction of range — 5 % is noticeably
     * noisier than REALISTIC (1.5 %) but still looks physically
     * plausible on a trend chart.
     */
    private static final double VOLATILITY_FACTOR = 0.05;

    @Override
    public double generate(double current, double min, double max) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        double range      = max - min;
        double volatility = range * VOLATILITY_FACTOR;

        // Wider random step than REALISTIC — still a walk, not a jump.
        double delta = (rng.nextDouble() * 2.0 - 1.0) * volatility;
        double next  = current + delta;

        // Bounce off boundaries (same reflection model as RealisticStrategy).
        if (next < min) {
            next = min + (min - next);
        } else if (next > max) {
            next = max - (next - max);
        }

        next = Math.max(min, Math.min(max, next));
        return Math.round(next * 100.0) / 100.0;
    }
}
