package myPortfolio;

import io.reactivex.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.file.FileSystem;
import io.vertx.reactivex.mysqlclient.MySQLPool;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.RowSet;
import io.vertx.reactivex.sqlclient.Tuple;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

public class MariadbVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(MariadbVerticle.class);

  public Completable rxStart() {

    final FileSystem fs = vertx.fileSystem();
    fs.rxExists("config.properties").subscribe(e -> {
      Boolean exists = e.booleanValue();
      if(!exists) {
        System.out.println("\n------------\nERROR: config.properties file was not found inside resources folder.\nPlease create the file with credentials and then try again.\n------------\n");
        System.exit(1);
      }
    });

    final EventBus eb = vertx.eventBus();
    eb.consumer("mariadb", this::handleInsert);
    //Place pool options here.
    return Completable.complete();
  }

  private void handleInsert(Message<String> message) {

    LOGGER.debug("MariadbVerticle received message : " + message.body());
    final JsonObject json = new JsonObject(message.body());
    final Collection list = json.getMap().values();
    final Properties propConfig = new Properties();
    try {
      propConfig.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    final String myEmail = propConfig.getProperty("myEmail");
    final String emailPass = propConfig.getProperty("emailPass");
    final JsonObject ebEntries = json.copy();
    ebEntries.put("myEmail", myEmail);
    ebEntries.put("emailPass", emailPass);
    vertx.eventBus().rxRequest("email", ebEntries.encode())
    .subscribe(e -> {
      LOGGER.debug("MariadbVerticle received reply: " + e.body());
    },
      err -> {
      LOGGER.debug("Error communicating to EmailVerticle. " + err.getMessage());
      });

    final String host = propConfig.getProperty("host");
    final String database = propConfig.getProperty("database");
    final String user = propConfig.getProperty("user");
    final String password = propConfig.getProperty("password");
    final MySQLConnectOptions connectOptions = new MySQLConnectOptions()
      .setPort(3306)
      .setHost(host)
      .setDatabase(database)
      .setUser(user)
      .setPassword(password);

    PoolOptions poolOptions = new PoolOptions();

    MySQLPool client = MySQLPool.pool(vertx, connectOptions, poolOptions);

    client
      .preparedQuery("INSERT INTO contact(name, business, position, callback, phone, interview_date) values (?, ?, ?, ?, ?, ?)")
      .execute(Tuple.tuple(new ArrayList(list)), ar -> {
        if (ar.succeeded()) {
          RowSet<Row> result = ar.result();
          LOGGER.debug("Got " + result.size() + " rows ");
          message.reply("success");
        } else {
          LOGGER.debug("Failure: " + ar.cause().getMessage());
          message.reply("failure");
        }

        client.close();
      });
  }

}
