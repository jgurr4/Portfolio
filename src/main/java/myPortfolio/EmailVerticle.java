package myPortfolio;

import io.reactivex.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mail.LoginOption;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.StartTLSOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.ext.mail.MailConfig;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.ext.mail.MailClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.TreeSet;

public class EmailVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmailVerticle.class);

  public Completable rxStart() {

    final EventBus eb = vertx.eventBus();
    eb.consumer("email", this::handleEmail);
    return Completable.complete();
  }

  private void handleEmail(Message<String> message) {

    LOGGER.debug("EmailVerticle received message : " + message.body());
    final JsonObject json = new JsonObject(message.body());
    final String myEmail = json.getString("myEmail");
    final String emailPass = json.getString("pass");
    MailConfig config = new MailConfig();
    config.setKeepAlive(false);
    config.setHostname("smtp.siteprotect.com")
      .setUsername(myEmail)
      .setPassword(emailPass)
      .setPort(587);
    MailClient mailClient = MailClient.create(vertx, config);
    MailMessage mailMessage = new MailMessage();
    mailMessage.setFrom(myEmail)
      .setTo(myEmail)
      .setText("Someone has submitted a request for contact on porfolio page.")
      .setHtml("name: " + json.getString("name") +
        "\nbusiness: " + json.getString("business") +
        "\nposition: " + json.getString("position") +
        "\ncallback: " + json.getString("callback") +
        "\nphone: " + json.getString("phone") +
        "\ninterview_date: " + json.getString("interview_date")
      );

    mailClient.rxSendMail(mailMessage)
      .subscribe(e -> {
        LOGGER.debug("Email message sent Successfully!");
        message.reply("success");
      }, err -> {
        LOGGER.debug("Email message failed to send. " + err.getMessage());
        message.reply("failure");
      });

  }

}
