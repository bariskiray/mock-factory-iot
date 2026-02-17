package com.mockfactory.controller;

import com.mockfactory.model.SimulatedDevice;
import com.mockfactory.model.SimulationConfig;
import com.mockfactory.service.SimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

/**
 * REST interface for commissioning and decommissioning simulated
 * industrial devices.
 * <p>
 * Think of this as the "Engineering Workstation" — the control-room
 * operator uses it to bring virtual sensors online, inspect their
 * current state, and take them offline when the test run is complete.
 *
 * <pre>
 *   POST   /api/devices        → commission a new device
 *   GET    /api/devices        → list all active devices
 *   GET    /api/devices/{id}   → inspect a single device
 *   DELETE /api/devices/{id}   → decommission a device
 * </pre>
 */
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SimulationController {

    private final SimulationService simulationService;

    /**
     * Commission a new simulated device and start its telemetry stream.
     *
     * @param config device blueprint (name, type, range, strategy, frequency)
     * @return the created device with its assigned tag-id
     */
    @PostMapping
    public ResponseEntity<SimulatedDevice> createDevice(@RequestBody SimulationConfig config) {
        SimulatedDevice device = simulationService.createDevice(config);
        return ResponseEntity.status(HttpStatus.CREATED).body(device);
    }

    /**
     * List every device currently running on the simulated factory floor.
     */
    @GetMapping
    public ResponseEntity<Collection<SimulatedDevice>> listDevices() {
        return ResponseEntity.ok(simulationService.getAllDevices());
    }

    /**
     * Inspect a single device's latest process value.
     *
     * @param id the device tag-id
     */
    @GetMapping("/{id}")
    public ResponseEntity<SimulatedDevice> getDevice(@PathVariable String id) {
        return simulationService.getDevice(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Decommission a device — stops its scan cycle and removes it
     * from the in-memory registry.
     *
     * @param id the device tag-id
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeDevice(@PathVariable String id) {
        boolean removed = simulationService.removeDevice(id);
        return removed
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
