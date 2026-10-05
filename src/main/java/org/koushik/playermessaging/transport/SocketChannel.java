package org.koushik.playermessaging.transport;

import org.koushik.playermessaging.core.MessageChannel;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Responsibility: implements {@link MessageChannel} for two players running
 * as separate OS processes, using a plain TCP socket as the transport.
 *
 */
public class SocketChannel implements MessageChannel {

    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    private SocketChannel(Socket socket) throws IOException{
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), false);
    }

    public static SocketChannel listenOn(int port) throws IOException {
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            Socket accepted = serverSocket.accept();
            return new SocketChannel(accepted);
        }
    }

    public static SocketChannel connectTo(String host, int port, Long maxWaitMillis) throws IOException {
        long deadline = System.currentTimeMillis() + maxWaitMillis;
        IOException lastFailure = null;

        while(System.currentTimeMillis() < deadline){
            try {
                return new SocketChannel(new Socket(host, port));
            } catch (IOException exp) {
                lastFailure = exp;
                sleepQuietly(200);
            }
        }
        throw new IOException("Could not connect to :" + host + ":" + port + "within " + maxWaitMillis + "ms " + lastFailure);
    }

    private static void sleepQuietly(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void send(String message) throws IOException {
        writer.println(message);
        if(writer.checkError()){
            throw new IOException("Failed to write to Socket");
        }
    }

    @Override
    public String receive() throws IOException, InterruptedException {
        String line = reader.readLine();
        if(line == null){
            throw new IOException("Remote closed the connection");
        }
        return line;
    }

    @Override
    public void close() {
        try{
            socket.close();
        } catch (IOException ignored) {

        }
    }
}
