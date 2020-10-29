package myPortfolio;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.reactivex.core.Vertx.*;
import io.reactivex.*;
import io.reactivex.annotations.Nullable;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.*;
import io.vertx.reactivex.core.*;
import io.vertx.reactivex.core.http.*;
import io.vertx.reactivex.ext.web.*;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import io.vertx.reactivex.core.eventbus.EventBus;


public class HttpServerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

    @Override
    public Completable rxStart() {
        LOGGER.info("Http Verticle is starting. Make sure MariaDB is running.");
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.route().handler(StaticHandler.create());
        router.get("/static/*").handler(this::staticHandler);
        router.route().handler(BodyHandler.create());
        router.post("/api/contact").handler(this::busHandler);
        final int port = 8081;
        final Single<HttpServer> rxListen = server
            .requestHandler(router)
            .rxListen(port)
            .doOnSuccess(e -> {
                LOGGER.info("HTTP server running on port " + port);
            })
            .doOnError(e -> {
                LOGGER.error("Could not start a HTTP server", e.getMessage());
            });

        return rxListen.ignoreElement();
    }

    private void staticHandler(RoutingContext context) {

        final HttpServerResponse response = context.response();
        final HttpServerRequest request = context.request();

        @Nullable
        String path = request.path();
        try {
            LOGGER.debug("GET " + path);
            path = path.substring(1);
            boolean isText = true;
            if (path.endsWith(".html")) {
                response.putHeader("Content-Type", "text/html");
            } else if (path.endsWith(".css")) {
                response.putHeader("Content-Type", "text/css");
            } else if (path.endsWith(".png")) {
                response.putHeader("Content-Type", "image/png");
                isText = false;
            } else if (path.endsWith(".jpg")) {
                response.putHeader("Content-Type", "image/jpg");
                isText = false;
            } else {
                response.setStatusCode(502);
                response.end("<html><body>Error filetype unknown: " + path + "</body></html>");
                return;
            }

            final InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
            if (stream == null) {
                LOGGER.warn("Resource not found: " + path);
                response.setStatusCode(404);
                response.end();
            } else if(isText) {
                final String text = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n"));
                response.setStatusCode(200);
                response.end(text);
                LOGGER.debug("text =" + text);
            } else {
                Buffer buffer = Buffer.buffer(stream.readAllBytes());
                response.setStatusCode(200);
                response.end(buffer);
            }
        } catch (Exception e) {
            LOGGER.error("Problem fetching static file: " + path, e);
            response.setStatusCode(502);
            response.end();
        }
    }

    private void busHandler(RoutingContext context) {

        try {
            final EventBus eb = vertx.eventBus();

            final HttpServerRequest request = context.request();
            final HttpServerResponse response = context.response();
            final MultiMap params = request.params();

            //This puts the params from http headers into json object.
            JsonObject object = new JsonObject();
            for (Map.Entry<String, String> entry : params.entries()) {
                object.put(entry.getKey(), entry.getValue());
            }

            eb.rxRequest("mariadb", object.encode())
                .subscribe(e -> {
                        LOGGER.debug("HttpServer Verticle Received reply: " + e.body());
                    },
                    err -> {
                        LOGGER.debug("Error communicating to MariadbVerticle. " + err.getMessage());
                        // TODO: try doing this part with only response.end and see if that successfully gets page to refresh without needing to do redirect.
                        response.setStatusCode(303);
                        response.putHeader("Location", "/jared.html");
                        response.end();
                    });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}

