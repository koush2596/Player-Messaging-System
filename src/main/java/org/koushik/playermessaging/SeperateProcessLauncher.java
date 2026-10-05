package org.koushik.playermessaging;

import org.koushik.playermessaging.core.MessageChannel;
import org.koushik.playermessaging.core.Player;
import org.koushik.playermessaging.transport.SocketChannel;

import java.io.IOException;

/**
 * Responsibility: entry point for the multi-process mode (requirement 7).
 * Starts exactly one {@link Player} wired to a {@link SocketChannel} as a
 * standalone JVM. Run twice (once per role) to get two different PIDs.
 *
 */
public class SeperateProcessLauncher {

    private static final int MESSAGES_TO_EXCHANGE = 10;
    private static final long CONNECT_RETRY_LIMIT = 10_000;

    private SeperateProcessLauncher() {}

    public static void main(String[] args) throws IOException {
        if(args.length < 2){
            printUsageOnExit();
        }

        String role = args[0];
        MessageChannel channel;
        boolean initiator;

        switch (role) {
            case "responder" -> {
                int port = Integer.parseInt(args[1]);
                System.out.printf("Seperate-process mode role = responder (PID %d)%n", ProcessHandle.current().pid());
                System.out.println("Responder listening to port: " + port);
                channel = SocketChannel.listenOn(port);
                initiator = false;
            }
            case "initiator" -> {
                if(args.length < 3){
                    printUsageOnExit();
                    return;
                }
                String host = args[1];
                int port = Integer.parseInt(args[2]);
                System.out.printf("Seperate-process mode role=initiator (PID %d)%n ", ProcessHandle.current().pid());
                System.out.println("Initiator connecting to: " + host + " " + port);
                channel = SocketChannel.connectTo(host, port, CONNECT_RETRY_LIMIT);
                initiator = true;
            }
            default -> {
                printUsageOnExit();
                return;
            }
        }
        Player player = new Player(role, initiator, MESSAGES_TO_EXCHANGE, channel);
        player.run();
        System.out.println("Process for role: " + role + " Finished");
    }

    private static void printUsageOnExit(){
        System.out.println("Usage: java SeperateProcessLauncher responder <port> & initiator <host> <port>");
        System.exit(1);
    }
}
