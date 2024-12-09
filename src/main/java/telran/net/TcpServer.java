package telran.net;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import telran.net.exceptions.TooManyFailuresException;

public class TcpServer implements Runnable {
    Protocol protocol;
    int port;
    ExecutorService executor;

    public TcpServer(Protocol protocol, int port) {
        this.protocol = protocol;
        this.port = port;
    }

    @Override
    public void run() {
        executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on the port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                var session = new TcpClientServerSession(protocol, socket);
                executor.execute(session);
            }
        } catch (TooManyFailuresException e) {
            shutdown();
        } catch (SocketTimeoutException e) {
            shutdown();
        } catch (IOException e) {
            System.out.println(e);
        } 
    }

    public void shutdown() {
        executor.shutdown();
    }

}
