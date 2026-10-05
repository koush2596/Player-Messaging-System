package org.koushik.playermessaging;

import org.koushik.playermessaging.core.MessageChannel;
import org.koushik.playermessaging.core.Player;
import org.koushik.playermessaging.transport.InMemoryChannel;

/**
 * Responsibility: entry point for the single-JVM mode (requirement 5).
 * Wires two {@link Player} instances through an {@link InMemoryChannel} pair
 * and runs each on its own {@link Thread} inside one process.
 *
 */
public class SameProcessLauncher {

    private static final int MESSAGES_TO_EXCHANGE = 10;

    private SameProcessLauncher(){}

    public static void main(String[] args) throws InterruptedException {
        System.out.printf("Same-process mode (PID %d)", ProcessHandle.current().pid());

        MessageChannel[] channels = InMemoryChannel.createPair();

        Player initiator = new Player("initiator", true, MESSAGES_TO_EXCHANGE, channels[0]);
        Player responder = new Player("responder", false, MESSAGES_TO_EXCHANGE, channels[1]);

        Thread initiatorThread = new Thread(initiator::run, "player-initiator");
        Thread responderThread = new Thread(responder::run, "player-responder");

        responderThread.start();
        initiatorThread.start();

        initiatorThread.join();
        responderThread.join();

        System.out.println("Execution is Completed");
    }
}
