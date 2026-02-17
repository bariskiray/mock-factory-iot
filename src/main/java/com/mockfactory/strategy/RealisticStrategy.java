package com.mockfactory.strategy;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Brownian-motion (Random Walk) signal generator.
 * <p>
 * Models a real industrial instrument whose reading <em>drifts</em>
 * gradually rather than jumping to an unrelated value each scan
 * cycle.  This is the default strategy because it produces the most
 * realistic-looking telemetry:
 *
 * <ol>
 *   <li>Take the device's previous reading ({@code current}).</li>
 *   <li>Add a small random delta drawn from
 *       [{@code −volatility}, {@code +volatility}].</li>
 *   <li>If the result would exceed the engineering-unit limits,
 *       <em>bounce</em> it back inside the range — exactly like a
 *       physical process that hits a safety relief valve.</li>
 * </ol>
 *
 * The volatility is proportional to the configured range so the
 * strategy works naturally whether the span is 0–100 or 0–10 000.
 */
@Component("REALISTIC")
public class RealisticStrategy implements DataGenerationStrategy {

    /**
     * Maximum per-tick drift expressed as a fraction of the full range.
     * 1.5 % keeps the signal smooth at 1 s intervals; at faster scan
     * rates the visual movement stays gentle.
     */
    private static final double VOLATILITY_FACTOR = 0.015;

    @Override
    public double generate(double current, double min, double max) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        double range      = max - min;
        double volatility = range * VOLATILITY_FACTOR;

        // Random delta in [−volatility, +volatility] (Brownian step).
        double delta = (rng.nextDouble() * 2.0 - 1.0) * volatility;
        double next  = current + delta;

        // ── Boundary bounce ───────────────────────────────────
        // If the walk would leave the engineering-unit range,
        // reflect it back — simulating a physical constraint
        // (e.g. pressure relief valve, thermal cutoff).
        if (next < min) {
            next = min + (min - next);      // mirror above floor
        } else if (next > max) {
            next = max - (next - max);      // mirror below ceiling
        }

        // Final safety clamp (handles edge case where bounce
        // overshoots due to very large volatility).
        next = Math.max(min, Math.min(max, next));

        return Math.round(next * 100.0) / 100.0;
    }
}
