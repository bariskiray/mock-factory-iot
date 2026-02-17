package com.mockfactory.strategy;

/**
 * Strategy interface for industrial signal generation.
 * <p>
 * Each implementation models a different real-world signal behaviour
 * that you would encounter on a factory floor — from steady-state
 * readings to catastrophic sensor faults.
 */
public interface DataGenerationStrategy {

    /**
     * Produce the next process value for a simulated device.
     *
     * @param current the device's current reading (may be used for drift-based algorithms)
     * @param min     engineering-unit low range
     * @param max     engineering-unit high range
     * @return the new simulated reading
     */
    double generate(double current, double min, double max);
}
