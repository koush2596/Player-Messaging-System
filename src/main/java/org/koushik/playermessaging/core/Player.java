package org.koushik.playermessaging.core;

import java.io.IOException;

/**
 * Responsibility: implements the conversation logic for a single player in
 * the message exchange, independent of how messages are physically
 * transported.
 *
 */
public final class Player {
    static final String TERMINATION_SIGNAL = "__STOP__";

    private final String name;
    private final boolean initiator;
    private final int stopAfterMessages;
    private final MessageChannel channel;

    private int sentCount = 0;
    private int receivedCount = 0;

    public Player(String name, boolean initiator, int stopAfterMessages, MessageChannel channel){
        this.name = name;
        this.initiator = initiator;
        this.stopAfterMessages = stopAfterMessages;
        this.channel = channel;
    }

    public void run() {
        try{
            if(initiator){
                runAsInitiator();
            } else {
                runAsResponder();
            }
        } catch (IOException exp){
            log("Occured Channel Error, Stopping: " + exp.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log("Interrupted Due to Exception: " + e.getMessage());
        } finally {
            channel.close();
        }
    }

    private void runAsInitiator() throws IOException, InterruptedException{
        String outgoing = "1";
        while(sentCount < stopAfterMessages || receivedCount < stopAfterMessages){
            if(sentCount < stopAfterMessages){
                send(outgoing);
            }
            if(receivedCount < stopAfterMessages){
                String incoming = channel.receive();
                receivedCount++;
                log("received #" + receivedCount + ": " + incoming);
                outgoing = incoming;
            }
        }
        channel.send(TERMINATION_SIGNAL);
        log("Completed: sent=" + sentCount + " received= " + receivedCount);
    }

    private void runAsResponder() throws IOException, InterruptedException{
        while(true){
            String incoming = channel.receive();
            if (TERMINATION_SIGNAL.equals(incoming)) {
                log("Received Termination Signal, Shutting Down");
                return;
            }
            receivedCount++;
            log("Received " + incoming);
            send(buildReply(incoming));
        }
    }

    private String buildReply(String received){
        return received + "-" + (sentCount + 1);
    }

    private void send(String message) throws IOException {
        channel.send(message);
        sentCount++;
        log("sent #" + sentCount + ": " + message);
    }

    private void log(String message){
        System.out.println(" [" + name + "] " + message);
    }
}
