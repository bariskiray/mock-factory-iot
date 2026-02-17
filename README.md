<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/WebSocket-STOMP-38bdf8?logo=socketdotio&logoColor=white" alt="WebSocket"/>
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License"/>
</p>

<h1 align="center">Mock Factory</h1>

<p align="center">
  <b>Industrial IoT Simulation API</b> — generates fake but realistic telemetry data<br/>
  for testing dashboards, SCADA systems, and data pipelines without real hardware.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/REST%20API-Available-22c55e" alt="REST API"/>
  <img src="https://img.shields.io/badge/Real--time-WebSocket-818cf8" alt="Real-time"/>
  <img src="https://img.shields.io/badge/Dashboard-Built--in-38bdf8" alt="Dashboard"/>
</p>

---

## Overview

**Mock Factory** is a lightweight Spring Boot service that simulates industrial IoT devices on a virtual factory floor. It produces realistic telemetry streams (temperature, pressure, vibration, flow rate) and broadcasts them in real-time over WebSocket (STOMP), while also exposing a clean REST API.

Perfect for:

- Frontend developers building IoT dashboards without access to real sensors
- QA engineers testing alarm thresholds and fault-handling logic
- Backend developers prototyping data pipelines and time-series storage
- Demos and presentations that need live-updating industrial data

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Mock Factory                         │
│                                                             │
│  ┌──────────┐    ┌──────────────────┐    ┌──────────────┐  │
│  │   REST   │───▶│  SimulationService│───▶│  Strategies  │  │
│  │Controller│    │  (Scheduler)      │    │              │  │
│  └──────────┘    └────────┬─────────┘    │ • Realistic  │  │
│                           │              │ • Random     │  │
│                           ▼              │ • Sine Wave  │  │
│                  ┌────────────────┐      │ • Chaos      │  │
│                  │  STOMP Broker  │      └──────────────┘  │
│                  │  /topic/telemetry      │                 │
│                  └────────┬───────┘                         │
└───────────────────────────┼─────────────────────────────────┘
                            │ WebSocket
                            ▼
                   ┌─────────────────┐
                   │  Browser / Any  │
                   │  STOMP Client   │
                   └─────────────────┘
```

## Features

| Feature | Description |
|---------|-------------|
| **4 Device Types** | Temperature, Pressure, Vibration, Flow Rate |
| **4 Signal Strategies** | Realistic (Brownian motion), Random walk, Sine wave, Chaos (fault injection) |
| **Real-time Streaming** | STOMP over WebSocket with per-device telemetry topics |
| **REST API** | Full CRUD for device lifecycle management |
| **Built-in Dashboard** | Dark-themed web UI with live telemetry feed |
| **Zero Dependencies** | No database, no message broker — runs standalone |
| **Configurable** | Custom ranges, scan frequency, strategy per device |

## Quick Start

### Prerequisites

- **Java 21** or later
- **Maven 3.8+** (or use the included wrapper if available)

### Run

```bash
# Clone the repository
git clone https://github.com/your-username/mock-factory.git
cd mock-factory

# Build & run
./mvnw spring-boot:run
```

Or with Maven directly:

```bash
mvn clean package
java -jar target/mock-factory-0.0.1-SNAPSHOT.jar
```

The application starts on **http://localhost:8080**.

Open the built-in dashboard at [http://localhost:8080](http://localhost:8080) to commission devices and watch live telemetry.

## API Reference

### Commission a New Device

```http
POST /api/devices
Content-Type: application/json

{
  "name": "Boiler-Room Temp Sensor",
  "type": "TEMPERATURE",
  "min": 0,
  "max": 150,
  "frequencyMs": 1000,
  "simulationType": "REALISTIC"
}
```

**Response** `201 Created`

```json
{
  "id": "A3F1B2C8",
  "name": "Boiler-Room Temp Sensor",
  "type": "TEMPERATURE",
  "min": 0.0,
  "max": 150.0,
  "currentVal": 75.0,
  "lastValue": 75.0,
  "simulationType": "REALISTIC",
  "frequencyMs": 1000,
  "active": true,
  "timestamp": 1700000000000
}
```

### List All Active Devices

```http
GET /api/devices
```

### Inspect a Single Device

```http
GET /api/devices/{id}
```

### Decommission a Device

```http
DELETE /api/devices/{id}
```

## WebSocket (Real-time Telemetry)

Connect via SockJS + STOMP:

```javascript
const socket = new SockJS('http://localhost:8080/ws');
const client = Stomp.over(socket);

client.connect({}, () => {
  client.subscribe('/topic/telemetry/{deviceId}', (message) => {
    const telemetry = JSON.parse(message.body);
    console.log(telemetry);
  });
});
```

Each device broadcasts its updated state to `/topic/telemetry/{deviceId}` at the configured frequency.

## Signal Generation Strategies

| Strategy | Behavior | Use Case |
|----------|----------|----------|
| `REALISTIC` | Brownian-motion random walk with boundary bounce. Drift is ±1.5% of range per tick. | Default — most lifelike telemetry |
| `RANDOM` | Wider random walk at ±5% of range per tick. Correlated but noisier. | Simulating vibration sensors or noisy instruments |
| `SINE_WAVE` | Smooth sinusoidal oscillation across the full range over ~200 ticks. | Steady-state cyclic processes (heat exchangers, pumps) |
| `CHAOS` | 95% stable with small drift, 5% catastrophic fault injection (-999 or 2x max). | Testing alarm systems and out-of-range UI handling |

## Project Structure

```
src/main/java/com/mockfactory/
├── MockFactoryApplication.java       # Spring Boot entry point
├── config/
│   └── WebSocketConfig.java          # STOMP/WebSocket configuration
├── controller/
│   └── SimulationController.java     # REST API endpoints
├── model/
│   ├── SimulatedDevice.java          # Device entity (tag, range, value)
│   └── SimulationConfig.java         # DTO for device creation requests
├── service/
│   └── SimulationService.java        # Orchestrates simulations & broadcasts
└── strategy/
    ├── DataGenerationStrategy.java   # Strategy interface
    ├── RealisticStrategy.java        # Brownian-motion walk
    ├── RandomStrategy.java           # Bounded random walk
    ├── SineWaveStrategy.java         # Sinusoidal oscillation
    └── ChaosStrategy.java           # Fault injection

src/main/resources/
├── application.yml                   # Server & scheduler config
└── static/
    └── index.html                    # Built-in IoT dashboard
```

## Configuration

Edit `application.yml` to customize:

```yaml
server:
  port: 8080

mockfactory:
  scheduler:
    core-pool-size: 16    # max concurrent device simulations
```

## Tech Stack

- **Java 21** — modern language features
- **Spring Boot 3.2.5** — application framework
- **Spring WebSocket** — STOMP over SockJS for real-time communication
- **Lombok** — reduces boilerplate in model classes
- **SockJS + STOMP.js** — browser-side WebSocket client (CDN)

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
