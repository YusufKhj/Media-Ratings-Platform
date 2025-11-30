package server;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class HttpServerApp {
    private final int port;

    public HttpServerApp(int port) { this.port = port; }

    public void start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/api", new RequestHandler());
            server.start();
            System.out.println("Server running on http://localhost:" + port);
        } catch(Exception e) { e.printStackTrace(); }
    }
}
