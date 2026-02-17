package com.mockfactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Mock Factory — Industrial IoT Simulation API.
 * <p>
 * Generates fake but realistic telemetry data (temperature, pressure,
 * vibration, flow-rate) and streams it over WebSocket so frontend
 * developers and QA testers can work against a live data feed without
 * needing real hardware on the factory floor.
 */
@SpringBootApplication
public class MockFactoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockFactoryApplication.class, args);
    }
}
