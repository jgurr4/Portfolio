package myPortfolio;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.annotations.Nullable;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.*;
import io.vertx.rxjava3.core.http.*;
import io.vertx.rxjava3.ext.web.*;
import io.vertx.rxjava3.ext.web.handler.BodyHandler;
import io.vertx.rxjava3.core.eventbus.EventBus;


public class HttpServerVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

  @Override
  public Completable rxStart() {
    LOGGER.info("Http Verticle is starting. Make sure MariaDB is running.");
    HttpServer server = vertx.createHttpServer();

    Router router = Router.router(vertx);
    router.route().handler(StaticHandler.create());
    router.get("/").handler(this::redirect);
    router.get("/lawnpage").handler(this::redirect);
    router.get("/med_dissector").handler(this::redirect);
    router.get("/static/*").handler(this::staticHandler);
    router.route().handler(BodyHandler.create());
    router.post("/api/contact").handler(this::busHandler);
    final int port = 8080;
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

  private void redirect(RoutingContext context) {
    final HttpServerResponse response = context.response();
    final HttpServerRequest request = context.request();
    String target = "";
    if (request.host().contains("arizonapomeranians.com")) {
//      target = "/static/azpom/index.html";
	target = "wikijs:8083";
    } else if (request.host().contains("jaredgurr.com")) {
      target = "/static/jared.html";
    }
    if (request.path().equals("/lawnpage")) {
      target = "http://" + request.host().replaceFirst(":.*", "") + ":8081";
    } else if (request.path().equals("/med_dissector")) {
      target = "http://" + request.host().replaceFirst(":.*", "") + ":8082";
    }
    response.putHeader("location", target);
    response.setStatusCode(302);
    response.end();
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
      } else if (path.endsWith(".js")) {
        response.putHeader("Content-Type", "text/javascript");
      } else if (path.endsWith(".png")) {
        response.putHeader("Content-Type", "image/png");
        isText = false;
      } else if (path.endsWith(".jpg")) {
        response.putHeader("Content-Type", "image/jpg");
        isText = false;
      } else if (path.endsWith(".pdf")) {
        response.putHeader("Content-Type", "application/pdf");
        isText = false;
      } else if (path.endsWith(".gif")) {
        response.putHeader("Content-Type", "image/gif");
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
      } else if (isText) {
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
      JsonObject object = new JsonObject();
      for (Map.Entry<String, String> entry : params.entries()) {
        object.put(entry.getKey(), entry.getValue());
      }

      eb.rxRequest("mariadb", object.encode())
              .subscribe(e -> {
                        LOGGER.debug("HttpServer Verticle Received reply: " + e.body());
                        response.setStatusCode(303);
                        response.putHeader("Location", "/static/jared.html");
                        response.end();
                      },
                      err -> {
                        LOGGER.debug("Error communicating to MariadbVerticle. " + err.getMessage());
                        response.setStatusCode(303);
                        response.putHeader("Location", "/static/jared.html");
                        response.end();
                      });
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
    }
  }
}

