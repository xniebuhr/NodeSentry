# NodeSentry
**NodeSentry** is a lightweight, terminal-based REPL (Read-Eval-Print Loop) application designed to monitor the health, availability, and performance of web services in real-time. 

Built entirely around Java's modern `java.net.http.HttpClient`, NodeSentry moves away from legacy synchronous requests to provide a highly concurrent, non-blocking monitoring engine. It allows users to dynamically manage a registry of target URLs—ranging from public websites to secured, token-protected APIs—and tracks vital metrics such as latency, HTTP status codes, and server signatures.

## Key Features
* **Asynchronous Networking:** Utilizes `HttpClient.sendAsync()` and `CompletableFuture` to ping dozens of endpoints concurrently without freezing the terminal interface.
* **Interactive REPL Interface:** A custom command-line interface driven by the **Command Design Pattern** for clean, scalable user input parsing.
* **Authentication Support:** Capable of passing Bearer Tokens/API keys in HTTP headers to monitor private or secured endpoints.
* **Deep Header Inspection:** Extracts and logs hidden HTTP headers (like `Server` signatures) to track underlying infrastructure changes.
* **Graceful Failure Handling:** Built-in exception handling for DNS resolution errors and network timeouts, ensuring the monitor remains stable even when the internet is not.
* **Thread-Safe Architecture:** Safely shares state between the UI thread and the background `ScheduledExecutorService` using `ConcurrentHashMap`.

## Tech Stack & Architecture
* **Language:** Java 21+
* **Build Tool:** Maven
* **Core Libraries:** `java.net.http.HttpClient`, `java.util.concurrent`

### System Architecture
NodeSentry strictly enforces a separation of concerns. The `MonitorEngine` handles all complex I/O and threading, while the `ConsoleREPL` handles user interactions. They communicate strictly through decoupled `Command` objects.

```mermaid
classDiagram
    %% Package: com.sentry.model
    class ServiceTarget {
        -String url
        -String authToken
        -int lastStatusCode
        -long lastLatencyMs
        -LocalDateTime lastCheck
        -boolean isUp
        -String serverSignature
    }

    %% Package: com.sentry.core
    class MonitorEngine {
        -HttpClient client
        -Map~String, ServiceTarget~ targets
        -ScheduledExecutorService scheduler
        -String webhookUrl
        +addTarget(String url, String authToken)
        +startMonitoring(int intervalSeconds)
        +stopMonitoring()
        +getLatestStatus() Map
    }

    %% Package: com.sentry.ui
    class ConsoleREPL {
        -Map~String, Command~ commandMap
        -MonitorEngine engine
        -Scanner scanner
        -boolean running
        +start()
    }

    %% Package: com.sentry.commands
    class Command {
        <<interface>>
        +execute(String[] args)
        +getHelp() String
    }

    class AddCommand
    class StartCommand
    class StatusCommand
    class StopCommand

    %% Relationships
    ConsoleREPL --> MonitorEngine : controls
    ConsoleREPL --> Command : invokes
    MonitorEngine "1" *-- "many" ServiceTarget : manages
    AddCommand ..|> Command : implements
    StartCommand ..|> Command : implements
    StatusCommand ..|> Command : implements
    StopCommand ..|> Command : implements
```

## Getting Started

### Prerequisites
* Java Development Kit (JDK) 11 or higher installed.
* Apache Maven installed.

### Installation & Execution
1. **Clone the repository:**
   `git clone https://github.com/xniebuhr/NodeSentry.git`
   `cd NodeSentry`

2. **Compile the project using Maven:**
   `mvn clean compile`

3. **Run the application:**
   `mvn exec:java -Dexec.mainClass="com.sentry.Main"`

## Usage Commands
Once the REPL is running, use the following commands to interact with the engine:

| Command | Arguments | Description |
| :--- | :--- | :--- |
| `add` | `<url> [authToken]` | Adds a new target to the monitoring pool. Auth token is optional. |
| `remove` | `<url>` | Removes a specific URL from the monitoring pool. |
| `start` | `<interval_in_seconds>` | Spawns a background thread to begin pinging all targets. |
| `stop` | None | Safely shuts down the background monitoring thread. |
| `status` | None | Prints a formatted table of all active targets, their status, and latency. |
| `help` | None | Displays a list of available commands. |
| `exit` | None | Shuts down the application. |

---
*Developed as a demonstration of Java Concurrency, Asynchronous I/O, and Object-Oriented Design Patterns as the final project of my Object Oriented Software Engineering Fundamentals class.*