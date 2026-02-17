package com.mockfactory.model;

import com.mockfactory.model.SimulatedDevice.DeviceType;
import com.mockfactory.model.SimulatedDevice.SimulationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO carried by the {@code POST /api/devices} request.
 * <p>
 * The caller defines what kind of industrial signal to simulate
 * (type + range), how fast the PLC should scan (frequencyMs),
 * and which generation strategy to apply (random noise, smooth
 * sine wave, or chaotic fault injection).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationConfig {

    /** Human-readable device name (e.g. "Boiler-Room Temp Sensor"). */
    private String name;

    /** Industrial signal type. */
    private DeviceType type;

    /** Engineering-unit low range. */
    private double min;

    /** Engineering-unit high range. */
    private double max;

    /** Scan-cycle interval in milliseconds (default 1 000 ms). */
    private long frequencyMs = 1000;

    /** Algorithm used to produce each new reading (defaults to Brownian-motion walk). */
    private SimulationType simulationType = SimulationType.REALISTIC;
}
