package org.koushik.playermessaging.transport;

import org.koushik.playermessaging.core.MessageChannel;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Responsibility: implements {@link MessageChannel} for two players running
 * as threads inside the same JVM, using an in-memory queue per direction.
 *
 */
public class InMemoryChannel implements MessageChannel {

    private final BlockingQueue<String> outbound;
    private final BlockingQueue<String> inbound;
    private boolean closed;

    private InMemoryChannel(BlockingQueue<String> inbound, BlockingQueue<String> outbound){
        this.outbound = outbound;
        this.inbound = inbound;
    }

    public static MessageChannel[] createPair(){
        BlockingQueue<String> fromAToB = new LinkedBlockingQueue<>();
        BlockingQueue<String> fromBToA = new LinkedBlockingQueue<>();

        MessageChannel channelForA = new InMemoryChannel(fromAToB, fromBToA);
        MessageChannel channelForB = new InMemoryChannel(fromBToA, fromAToB);
        return new MessageChannel[] {channelForA, channelForB};
    }


    @Override
    public void send(String message) throws IOException {
        if(closed){
            throw new IllegalArgumentException("The Channel is Closed");
        }
        outbound.add(message);
    }

    @Override
    public String receive() throws IOException, InterruptedException {
        return inbound.take();
    }

    @Override
    public void close() {
        closed = true;
    }
}
