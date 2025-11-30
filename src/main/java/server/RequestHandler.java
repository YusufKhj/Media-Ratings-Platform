package server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class RequestHandler implements HttpHandler {
    private final Router router = new Router();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try { router.route(exchange); }
        catch(Exception e){
            e.printStackTrace();
            exchange.sendResponseHeaders(500,-1);
        }
    }
}
