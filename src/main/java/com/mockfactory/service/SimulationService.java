package com.mockfactory.service;

import com.mockfactory.model.SimulatedDevice;
import com.mockfactory.model.SimulatedDevice.SimulationType;
import com.mockfactory.model.SimulationConfig;
import com.mockfactory.strategy.DataGenerationStrategy;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

/**
 * The Factory Floor — orchestrates every running device simulation.
 * <p>
 * Responsibilities:
 * <ol>
 *   <li>Maintain an in-memory registry of {@link SimulatedDevice}s
 *       (no database — this is a lightweight mock service).</li>
 *   <li>Schedule a periodic task for each device that computes a new
 *       reading using the selected {@link DataGenerationStrategy}.</li>
 *   <li>Immediately broadcast every new reading to the STOMP topic
 *       {@code /topic/telemetry/{deviceId}} so WebSocket subscribers
 *       receive it in real time.</li>
 * </ol>
 */
@Slf4j
@Service
public class SimulationService {

    /** In-memory device registry — acts like an OPC-UA address space. */
    private final Map<String, SimulatedDevice> devices = new ConcurrentHashMap<>();

    /** One scheduled future per device so we can cancel individual simulations. */
    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();

    /** Shared thread pool for all scan-cycle timers. */
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(16);

    /** Strategy beans keyed by SimulationType name (RANDOM, SINE_WAVE, CHAOS). */
    private final Map<String, DataGenerationStrategy> strategies;

    /** Spring's STOMP message relay — pushes telemetry to WebSocket subscribers. */
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Spring injects every {@link DataGenerationStrategy} bean.
     * The beans are named after the enum value they represent
     * (see {@code @Component("RANDOM")} etc.).
     */
    public SimulationService(Map<String, DataGenerationStrategy> strategies,
                             SimpMessagingTemplate messagingTemplate) {
        this.strategies = strategies;
        this.messagingTemplate = messagingTemplate;
    }

    // ── Public API ─────────────────────────────────────────────

    /**
     * Commission a new simulated device and start its scan cycle.
     *
     * @param config device blueprint supplied by the REST caller
     * @return the fully initialised device with a generated tag-id
     */
    public SimulatedDevice createDevice(SimulationConfig config) {
        String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        SimulatedDevice device = SimulatedDevice.builder()
                .id(id)
                .name(config.getName())
                .type(config.getType())
                .min(config.getMin())
                .max(config.getMax())
                .currentVal((config.getMin() + config.getMax()) / 2.0)   // start at midpoint
                .lastValue((config.getMin() + config.getMax()) / 2.0)    // lastValue = initial midpoint
                .simulationType(config.getSimulationType())
                .frequencyMs(config.getFrequencyMs() > 0 ? config.getFrequencyMs() : 1000)
                .active(true)
                .timestamp(System.currentTimeMillis())
                .build();

        devices.put(id, device);
        startSimulation(device);

        log.info("▶ Device commissioned: {} [{}] — strategy={}, freq={}ms",
                device.getName(), id, device.getSimulationType(), device.getFrequencyMs());
        return device;
    }

    /**
     * Decommission a device and stop its scan cycle.
     *
     * @param deviceId the tag-id to remove
     * @return {@code true} if the device existed and was removed
     */
    public boolean removeDevice(String deviceId) {
        ScheduledFuture<?> future = tasks.remove(deviceId);
        if (future != null) {
            future.cancel(false);
        }

        SimulatedDevice removed = devices.remove(deviceId);
        if (removed != null) {
            log.info("■ Device decommissioned: {} [{}]", removed.getName(), deviceId);
            return true;
        }
        return false;
    }

    /** Snapshot of all active devices — used by the GET /api/devices endpoint. */
    public Collection<SimulatedDevice> getAllDevices() {
        return Collections.unmodifiableCollection(devices.values());
    }

    /** Look up a single device by tag-id. */
    public Optional<SimulatedDevice> getDevice(String deviceId) {
        return Optional.ofNullable(devices.get(deviceId));
    }

    // ── Internal: scan-cycle scheduling ────────────────────────

    /**
     * Schedule the periodic scan cycle for one device.
     * <p>
     * Every {@code device.frequencyMs} milliseconds the scheduler will:
     * <ol>
     *   <li>Resolve the correct {@link DataGenerationStrategy}.</li>
     *   <li>Compute the next process value.</li>
     *   <li>Update the in-memory device record.</li>
     *   <li>Broadcast the update over STOMP.</li>
     * </ol>
     */
    private void startSimulation(SimulatedDevice device) {
        DataGenerationStrategy strategy = resolveStrategy(device.getSimulationType());

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                // Feed the previous reading into the strategy so drift-based
                // algorithms (Random Walk / Brownian Motion) can derive the
                // next value from it — exactly how a real instrument works.
                double previous = device.getCurrentVal();
                double newVal = strategy.generate(previous, device.getMin(), device.getMax());

                device.setLastValue(previous);      // archive the reading we just consumed
                device.setCurrentVal(newVal);        // publish the freshly generated value
                device.setTimestamp(System.currentTimeMillis());

                // Broadcast to all subscribers on this device's telemetry channel.
                messagingTemplate.convertAndSend(
                        "/topic/telemetry/" + device.getId(),
                        device
                );
            } catch (Exception ex) {
                log.error("Scan-cycle error for device [{}]: {}", device.getId(), ex.getMessage());
            }
        }, 0, device.getFrequencyMs(), TimeUnit.MILLISECONDS);

        tasks.put(device.getId(), future);
    }

    /** Map the enum value to the matching Spring bean. */
    private DataGenerationStrategy resolveStrategy(SimulationType type) {
        DataGenerationStrategy strategy = strategies.get(type.name());
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy registered for: " + type);
        }
        return strategy;
    }

    /** Graceful shutdown — cancel all running simulations. */
    @PreDestroy
    public void shutdown() {
        log.info("Shutting down Mock Factory — cancelling all simulations…");
        tasks.values().forEach(f -> f.cancel(false));
        scheduler.shutdownNow();
    }
}
