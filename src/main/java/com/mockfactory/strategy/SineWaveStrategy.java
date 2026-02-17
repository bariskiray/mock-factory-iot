package com.mockfactory.strategy;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Smooth sine-wave oscillation generator.
 * <p>
 * Models a temperature or pressure signal that follows a predictable
 * cyclic pattern — similar to what you would see on a heat-exchanger
 * outlet probe during normal steady-state operation.  The waveform
 * sweeps between {@code min} and {@code max} over ~200 ticks
 * (one full period), making it easy to eyeball on a trend chart.
 */
@Component("SINE_WAVE")
public class SineWaveStrategy implements DataGenerationStrategy {

    /**
     * Global tick counter shared across all sine-wave devices.
     * Each call to {@link #generate} advances the phase by one step.
     */
    private final AtomicLong tick = new AtomicLong(0);

    /** Number of ticks for one complete sine cycle. */
    private static final double PERIOD = 200.0;

    @Override
    public double generate(double current, double min, double max) {
        long t = tick.getAndIncrement();

        // Map sine output (-1…+1) into the engineering-unit range.
        double amplitude = (max - min) / 2.0;
        double midpoint  = (max + min) / 2.0;
        double raw       = midpoint + amplitude * Math.sin(2 * Math.PI * t / PERIOD);

        return Math.round(raw * 100.0) / 100.0;
    }
}
