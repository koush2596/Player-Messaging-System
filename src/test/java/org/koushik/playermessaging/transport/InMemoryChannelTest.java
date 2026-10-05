package org.koushik.playermessaging.transport;

import org.junit.jupiter.api.Test;
import org.koushik.playermessaging.core.MessageChannel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryChannelTest {

    @Test
    void messageSentOnOneEndIsReceivedOnTheOther() throws Exception {
        MessageChannel[] pair = InMemoryChannel.createPair();
        MessageChannel a = pair[0];
        MessageChannel b = pair[1];

        a.send("hello from A");
        assertEquals("hello from A", b.receive());

        b.send("hello from B");
        assertEquals("hello from B", a.receive());
    }

    @Test
    void sendingOnAClosedChannelThrows() {
        MessageChannel[] pair = InMemoryChannel.createPair();
        MessageChannel a = pair[0];

        a.close();

        assertThrows(IllegalArgumentException.class, () -> a.send("too late"));
    }
}
