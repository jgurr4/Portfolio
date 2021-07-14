package myPortfolio;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.core.eventbus.EventBus;
import io.vertx.rxjava3.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlackVerticle extends AbstractVerticle {

  private static final Logger LOGGER = LoggerFactory.getLogger(SlackVerticle.class);

  public Completable rxStart() {

    final EventBus eb = vertx.eventBus();
    eb.consumer("slack", this::handleSlackMessage);
    return Completable.complete();
  }

  private void handleSlackMessage(Message<String> message) {
    LOGGER.debug("SlackVerticle received message : " + message.body());
    final JsonObject json = new JsonObject(message.body());
    Slack slack = Slack.getInstance();
    String token = json.getString("slackToken");

    MethodsClient methods = slack.methods(token);

    ChatPostMessageRequest request = ChatPostMessageRequest.builder()
      .channel("portfoliocontact")
      .text("name: " + json.getString("name") +
        "\nbusiness: " + json.getString("business") +
        "\nposition: " + json.getString("position") +
        "\ncallback: " + json.getString("callback") +
        "\nphone: " + json.getString("phone") +
        "\ninterview_date: " + json.getString("interview_date"))
      .build();

    try {
      ChatPostMessageResponse response = methods.chatPostMessage(request);
    } catch (Exception err) {
      LOGGER.debug("slack chat message failed: " + err.getCause().getMessage());
      message.reply("failure");
    }
    message.reply("success");

  }

}
