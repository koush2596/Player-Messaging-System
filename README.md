# Player Messaging

A small Java exercise demonstrating two `Player`s exchanging text messages over a
pluggable transport, run either as two threads in one JVM or as two separate OS
processes.

## Design

```
org.koushik.playermessaging
├── core
│   ├── MessageChannel   – transport-agnostic contract: send / receive / close
│   └── Player           – conversation logic (initiator vs. responder), independent
│                           of how messages are physically moved
├── transport
│   ├── InMemoryChannel  – MessageChannel over a pair of BlockingQueues (same JVM)
│   └── SocketChannel    – MessageChannel over a plain TCP socket (separate JVMs)
├── SameProcessLauncher      – entry point: both players as threads in one JVM
└── SeperateProcessLauncher  – entry point: one player per JVM, run twice
```

`Player` only depends on the `MessageChannel` interface, so the same conversation
logic works unmodified whether the two players live in one process or two.

- The **initiator** sends a message, waits for a reply, and repeats until it has
  sent and received `stopAfterMessages` (10) messages each, then sends a
  termination signal (`__STOP__`).
- The **responder** echoes back `received-<n>` for every message until it sees
  the termination signal.

## Requirements

- JDK 25
- Maven 3.9+

## Build

```bash
mvn clean package
```

## Run

Via the helper script:

```bash
./run.sh same-process
./run.sh separate-process          # TCP port 6060
./run.sh separate-process 7070     # custom port
```

Or directly with `java`:

```bash
# Same-process mode (single JVM, two threads)
java -cp target/classes org.koushik.playermessaging.SameProcessLauncher

# Separate-process mode (two JVMs, two PIDs)
java -cp target/classes org.koushik.playermessaging.SeperateProcessLauncher responder 6060
java -cp target/classes org.koushik.playermessaging.SeperateProcessLauncher initiator localhost 6060
```

## Test

```bash
mvn test
```

Tests cover `Player`'s conversation logic (via a fake `MessageChannel`) and the
`InMemoryChannel` transport.
