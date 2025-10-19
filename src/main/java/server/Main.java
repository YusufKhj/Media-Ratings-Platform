package server;

public class Main {
    public static void main(String[] args) {
        HttpServerApp server = new HttpServerApp(8080);
        server.start();

    }
}