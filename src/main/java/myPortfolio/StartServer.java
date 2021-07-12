package myPortfolio;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

public class StartServer {

    public static void main(final String[] args) {
        final StartServer startServer = new StartServer();
    }

    private final Vertx vertx;
    private final boolean debug = true;

    StartServer() {

        final VertxOptions options = new VertxOptions();
        if(debug) {
            options.setBlockedThreadCheckInterval(Long.MAX_VALUE >> 2);
        }

        vertx = Vertx.vertx(options);
        vertx.deployVerticle(new HttpServerVerticle());
        vertx.deployVerticle(new MariadbVerticle());
//        vertx.deployVerticle(new EmailVerticle());

    }

}
