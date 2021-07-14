package myPortfolio;

import io.vertx.core.http.HttpMethod;
import io.vertx.rxjava3.core.http.HttpClient;
import io.vertx.rxjava3.core.http.HttpClientRequest;
import io.vertx.rxjava3.ext.unit.Async;
import io.vertx.rxjava3.ext.unit.TestContext;
import org.junit.jupiter.api.Test;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


@ExtendWith(VertxExtension.class)
public class HttpTests {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpTests.class);

  // TODO: Finish implementing test.
  @Test
  public void testServerUserRegister(Vertx vertx, VertxTestContext context) throws IOException {

    String postParams = "name=John&business=Borg&position=Backend+Web+Developer&callback=Anytime&interview_date=Anytime";
    URL url = new URL("http://localhost:8080/api/contact");
    HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
    httpCon.setDoOutput(true);
    httpCon.setRequestMethod("POST");
    OutputStreamWriter out = new OutputStreamWriter(
      httpCon.getOutputStream());
    out.write(postParams);
    System.out.println(httpCon.getResponseCode());
    System.out.println(httpCon.getResponseMessage());
    out.close();
/*
    HttpClient client = vertx.createHttpClient();
    HttpClientRequest request = client
      .request(HttpMethod.POST, 8080, "localhost", "/api/contact")

    String body = "{'username':'www','password':'www'}";
    request.putHeader("content-length", "1000");
    request.putHeader("content-type", "application/x-www-form-urlencoded");
    request.write(body);
    request.end();
*/
  }
}
