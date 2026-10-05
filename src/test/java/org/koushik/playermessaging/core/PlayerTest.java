package org.koushik.playermessaging.core;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerTest {

    @Test
    void initiatorExchangesTenMessagesThenSendsTerminationSignal() throws InterruptedException {
        FakeMessageChannel channel = new FakeMessageChannel();
        // Responder echo: whatever the initiator sends, queue it straight back.
        channel.autoEcho = true;

        Player initiator = new Player("initiator", true, 10, channel);
        initiator.run();

        // 10 conversational messages plus the trailing termination signal.
        assertEquals(11, channel.sent.size());
        assertEquals(Player.TERMINATION_SIGNAL, channel.sent.get(channel.sent.size() - 1));
        assertTrue(channel.closed);
    }

    @Test
    void responderRepliesToEachMessageUntilTerminationSignal() throws InterruptedException {
        FakeMessageChannel channel = new FakeMessageChannel();
        channel.incoming.put("ping1");
        channel.incoming.put("ping2");
        channel.incoming.put(Player.TERMINATION_SIGNAL);

        Player responder = new Player("responder", false, 10, channel);
        responder.run();

        assertEquals(List.of("ping1-1", "ping2-2"), channel.sent);
        assertTrue(channel.closed);
    }

    @Test
    void channelIsClosedEvenWhenSendFails() throws InterruptedException {
        FakeMessageChannel channel = new FakeMessageChannel();
        channel.failOnSend = true;

        Player initiator = new Player("initiator", true, 10, channel);
        initiator.run();

        assertTrue(channel.closed);
        assertTrue(channel.sent.isEmpty());
    }

    /**
     * Minimal in-test double for {@link MessageChannel}: records sent messages
     * and serves queued (or auto-echoed) incoming messages.
     */
    private static final class FakeMessageChannel implements MessageChannel {
        final List<String> sent = new ArrayList<>();
        final BlockingQueue<String> incoming = new LinkedBlockingQueue<>();
        boolean closed = false;
        boolean autoEcho = false;
        boolean failOnSend = false;

        @Override
        public void send(String message) throws IOException {
            if (failOnSend) {
                throw new IOException("simulated send failure");
            }
            sent.add(message);
            if (autoEcho) {
                incoming.add(message);
            }
        }

        @Override
        public String receive() throws InterruptedException {
            return incoming.take();
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
