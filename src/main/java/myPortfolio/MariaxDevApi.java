package myPortfolio;

//This page is a test. Do not use until it's finished or just save it as template if you don't use it.

import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.eventbus.EventBus;
import io.vertx.rxjava3.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mysql.cj.xdevapi.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MariaxDevApi extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(myPortfolio.MariadbVerticle.class);

    public Completable rxStart() {
        final EventBus eb = vertx.eventBus();
        eb.consumer("mariadb", this::handleInsert);
        return Completable.complete();
    }

    private void handleInsert(Message<String> message) {
        LOGGER.debug("MariadbVerticle received message : " + message.body());
        final JsonObject json = JsonObject.mapFrom(message.body());
        final List list = json.mapTo(List.class);
        final String name = json.getString("name");
        final String business = json.getString("business");
        final String position = json.getString("position");
        final String callback = json.getString("callback");
        final String phone = json.getString("phone");
        final String interview_date = json.getString("interview_date");

    try {
      propConfig.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    final String pass = propConfig.getProperty("password");

        final Session mySession = new SessionFactory().getSession("mysqlx://localhost:3306/test?user=root&password=" + pass);
//          mySession.sql("USE contact");
        //use contact database:
        mySession.createSchema("xcontact");
        final Schema db = mySession.getSchema("xcontact");
        final Collection myColl = db.createCollection("xcontacts");
        //Create index to make collection queryable.
//        myColl.createIndex("id", "{fields:[{\"field\": \"$.id\", \"type\":\"INT\", required:true}]}");


        LOGGER.debug("list of indexes: " + mySession.sql("SHOW INDEX FROM xcontact.xcontacts"));

        // Using named parameters with a Map
        final Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("business", business);
        params.put("position", position);
        params.put("callback", callback);
        params.put("phone", phone);
        params.put("interview_date", interview_date);
        myColl.add(params);


        DocResult myRes1 = myColl.find("name = " + name).execute();
        LOGGER.debug("result 1 = " + myRes1);

        // Using the .bind() function to bind parameters
        DocResult myRes2 = myColl.find("name = :param1 AND phone = :param2").bind("param1", name).bind("param2", phone).execute();
        LOGGER.debug("result 2 = " + myRes2);

        // Using named parameters
        myColl.modify("name = :param").set("business", "Not Special Inc.")
            .bind("param", name).execute();

        DocResult myRes3 = myColl.find("business = " + business).execute();
        LOGGER.debug("result 3 = " + myRes3);

        // Binding works for all CRUD statements except add()
        //Here is how to do LIKE statement.
        DocResult myRes4 = myColl.find("name like :param")
            .bind("param", "J%").execute();
        LOGGER.debug("result 4 = " + myRes4);
    }
}

