package myPortfolio;

import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.reactivex.mysqlclient.MySQLPool;
import io.vertx.reactivex.sqlclient.Row;
import io.vertx.reactivex.sqlclient.RowSet;
import io.vertx.reactivex.sqlclient.Tuple;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.*;
import io.vertx.reactivex.core.*;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;

import java.util.List;

public class MariadbVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MariadbVerticle.class);

    public Completable rxStart() {
        final EventBus eb = vertx.eventBus();
        eb.consumer("mariadb", this::handleInsert);
        return Completable.complete();
    }

    private void handleInsert(Message<String> message) {
        LOGGER.debug("MariadbVerticle received message : " + message.body());
        final JsonObject json = JsonObject.mapFrom(message.body());
        final List list = json.mapTo(List.class);
/*      final String name = json.getString("name");
        final String business = json.getString("business"); */
        final MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                .setPort(3306)
                .setHost("localhost")
                .setDatabase("contact")
                .setUser("root")
                .setPassword("super03");

        PoolOptions poolOptions = new PoolOptions();

        MySQLPool client = MySQLPool.pool(connectOptions, poolOptions);

        client
                .preparedQuery("INSERT INTO contacts(name, business, position, callback, phone, interview_date) values (?, ?, ?, ?, ?, ?)")
                .execute(Tuple.tuple(list), ar -> {
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
