package org.koushik.playermessaging.core;

import java.io.IOException;

/**
 * Responsibility: defines the contract for exchanging text messages between
 * two {@link Player} instances, without exposing how those messages are
 * physically transported.
 *
 */
public interface MessageChannel {

    void send(String message) throws IOException;

    String receive() throws IOException, InterruptedException;

    void close();
}
