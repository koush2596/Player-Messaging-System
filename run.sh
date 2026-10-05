#!/usr/bin/env bash
#
# Builds and runs the player-messaging application in one of two modes:
#
#   ./run.sh same-process
#       Runs SameProcessLauncher: both players run as threads inside a
#       single JVM, communicating via an in-memory queue.
#
#   ./run.sh separate-process [port]
#       Runs SeparateProcessLauncher twice, as two independent OS
#       processes (two different PIDs), communicating over a TCP socket
#       on localhost. Port defaults to 6060 if not given.
#
# Examples:
#   ./run.sh same-process
#   ./run.sh separate-process
#   ./run.sh separate-process 7070

set -e  # exit immediately if any command fails

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

MODE="$1"

print_usage() {
    echo "Usage:"
    echo "  ./run.sh same-process"
    echo "  ./run.sh separate-process [port]"
}

if [[ -z "$MODE" ]]; then
    echo "Error: no mode specified."
    print_usage
    exit 1
fi

echo "Building project with Maven..."
mvn -q clean package
echo "Build complete."
echo ""

case "$MODE" in

    same-process)
        echo "Running in SAME-PROCESS mode (one JVM, two threads)."
        echo ""
        java -cp target/classes org.koushik.playermessaging.SameProcessLauncher
        ;;

    separate-process)
        PORT="${2:-6060}"
        RESPONDER_LOG="responder.log"
        INITIATOR_LOG="initiator.log"

        echo "Running in SEPARATE-PROCESS mode (two OS processes, port ${PORT})."
        echo ""

        echo "Starting responder process..."
        java -cp target/classes org.koushik.playermessaging.SeperateProcessLauncher \
            responder "${PORT}" > "${RESPONDER_LOG}" 2>&1 &
        RESPONDER_PID=$!
        echo "Responder started, PID=${RESPONDER_PID} (output: ${RESPONDER_LOG})"

        # Small head start so the responder is listening before the
        # initiator tries to connect. Not strictly required, since the
        # initiator retries on connection failure, but avoids retry noise.
        sleep 1

        echo "Starting initiator process..."
        java -cp target/classes org.koushik.playermessaging.SeperateProcessLauncher \
            initiator localhost "${PORT}" > "${INITIATOR_LOG}" 2>&1 &
        INITIATOR_PID=$!
        echo "Initiator started, PID=${INITIATOR_PID} (output: ${INITIATOR_LOG})"

        echo ""
        echo "Two separate OS processes are now running:"
        echo "  Responder PID: ${RESPONDER_PID}"
        echo "  Initiator PID: ${INITIATOR_PID}"
        echo ""
        echo "Waiting for both to finish..."

        wait "${INITIATOR_PID}"
        INITIATOR_EXIT=$?
        wait "${RESPONDER_PID}"
        RESPONDER_EXIT=$?

        echo ""
        echo "Initiator exit code: ${INITIATOR_EXIT}"
        echo "Responder exit code: ${RESPONDER_EXIT}"
        echo ""
        echo "--- Initiator output (${INITIATOR_LOG}) ---"
        cat "${INITIATOR_LOG}"
        echo ""
        echo "--- Responder output (${RESPONDER_LOG}) ---"
        cat "${RESPONDER_LOG}"
        ;;

    *)
        echo "Error: unknown mode '${MODE}'."
        print_usage
        exit 1
        ;;
esac
