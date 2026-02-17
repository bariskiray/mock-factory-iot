package com.mockfactory.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a single simulated industrial device on the factory floor.
 * <p>
 * Think of it as a virtual PLC (Programmable Logic Controller) register:
 * it has a tag name, an engineering-unit range (min/max), and a live
 * process value ({@code currentVal}) that the simulation engine updates
 * on every tick.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulatedDevice {

    /** Unique tag — analogous to a PLC tag address (e.g. "TT-4201"). */
    private String id;

    /** Human-readable name shown on the HMI panel. */
    private String name;

    /** Sensor / signal type (TEMPERATURE, PRESSURE, VIBRATION, FLOW_RATE). */
    private DeviceType type;

    /** Low-end of the engineering-unit range (e.g. 0 °C). */
    private double min;

    /** High-end of the engineering-unit range (e.g. 150 °C). */
    private double max;

    /** Current process value — updated by the simulation engine each tick. */
    private double currentVal;

    /**
     * Previous process value — fed into drift-based strategies (Random Walk,
     * Brownian Motion) so the next reading is derived from the last one
     * rather than generated independently.  This is how real analogue
     * instruments behave: the reading drifts, it doesn't teleport.
     */
    private double lastValue;

    /** Which generation algorithm drives this device's signal. */
    private SimulationType simulationType;

    /** Tick interval in milliseconds (how often the value is recalculated). */
    private long frequencyMs;

    /** Whether the simulation loop is actively running. */
    private boolean active;

    /** Epoch-millis timestamp of the last value update. */
    private long timestamp;

    // ── Industrial device types ────────────────────────────────
    public enum DeviceType {
        TEMPERATURE,
        PRESSURE,
        VIBRATION,
        FLOW_RATE
    }

    // ── Signal generation algorithms ───────────────────────────
    public enum SimulationType {
        /** Brownian-motion random walk — smooth, realistic drift. */
        REALISTIC,
        /** Bounded random walk — moderate noise, still correlated. */
        RANDOM,
        /** Smooth sinusoidal oscillation. */
        SINE_WAVE,
        /** Mostly stable with 5 % catastrophic fault injection. */
        CHAOS
    }
}
