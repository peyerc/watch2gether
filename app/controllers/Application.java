package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.W2GEvent;
import models.W2GEventMsisdn;
import models.W2GEventRepository;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Iterator;

@Named
@Singleton
public class Application extends Controller {

    private final W2GEventRepository w2GEventRepository;

    // We are using constructor injection to receive a repository to support our desire for immutability.
    @Inject
    public Application(final W2GEventRepository w2GEventRepository) {
        this.w2GEventRepository = w2GEventRepository;
    }

    public Result index() {
        return listAllEvents();
    }

    public Result listAllEvents() {
        final Iterable<W2GEvent> w2GEvents = w2GEventRepository.findAll();
        return ok(views.html.index.render(w2GEvents.iterator()));
    }

    /**
     * Example Json:
     *
     * {
     "channel": "SRF1",
     "time": "123",
     "show": "Fusball Bundesliga live"
     }
     * @return
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result saveEvent() {

        JsonNode json = request().body().asJson();
        JsonNode channelN = json.findValue("channel");
        JsonNode timeN = json.findValue("time");

        JsonNode showN = json.findValue("show");
        if(channelN == null || timeN == null || showN == null) {
            badRequest("A channel, time or show must be provided");
        }

        final W2GEvent event = new W2GEvent();

        event.channel = channelN.asText();
        event.time = new Date(timeN.asLong() * 1000);
        event.show = showN.asText();

        JsonNode numbersN = json.findValue("numbers");
        if(numbersN != null) {
            Iterator<JsonNode> elements = numbersN.elements();
            while (elements.hasNext()) {
                JsonNode next = elements.next();
                event.msisdns.add(new W2GEventMsisdn(next.asText()));

            }
        }

        w2GEventRepository.save(event);

        ObjectNode result = Json.newObject();
        result.put("status", "ok");
        result.put("show", event.show);
        return ok(result);
    }

    public Result dbTest() {

        final W2GEvent event = new W2GEvent();
        event.channel = "SRF 1";
        event.show = "10vor10";
        event.time = new Date();
        event.msisdns.add(new W2GEventMsisdn("41767202020"));
        event.msisdns.add(new W2GEventMsisdn("41767201234"));
        event.msisdns.add(new W2GEventMsisdn("41767204444"));

        final W2GEvent savedEvent = w2GEventRepository.save(event);


        final Iterable<W2GEvent> w2GEvents = w2GEventRepository.findAll();

        // Deliver the index page with a message showing the id that was generated.

        return ok(views.html.index.render(w2GEvents.iterator()));
    }


    public static Result sendMessage() throws IOException {

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://api.swisscom.com/v1/messaging/sms/outbound/tel:+40000000000/requests");

        String json = createJsonMessage();

        StringEntity entity = new StringEntity(json);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("client_id", "7V3QbSNyGonv4wETAIltvnN5bPYZbgyk");

        CloseableHttpResponse response = client.execute(httpPost);
        String bodyAsString = EntityUtils.toString(response.getEntity());
        int statusCode = response.getStatusLine().getStatusCode();
        client.close();

        if(statusCode == 200) {
            return ok("Message sent: " + bodyAsString);
        } else {
            return badRequest("Message couldn't be sent\n\nRequest: " + json + "\nResponse: " + bodyAsString);
        }
    }

    private static String createJsonMessage() throws IOException {
        // Create the node factory that gives us nodes.
        JsonNodeFactory factory = new JsonNodeFactory(false);

        // create a json factory to write the treenode as json. for the example
        // we just write to console
        StringWriter sw = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();

        // the root node - rootNode
        JsonNode rootNode = factory.objectNode();
        ObjectNode outboundSMSMessageRequest = mapper.createObjectNode();
        outboundSMSMessageRequest.put("senderAddress", "tel:+40000000000");
        ArrayNode address = mapper.createArrayNode();
        address.add("tel:+41767202020");
        address.add("tel:+41765671938");
        outboundSMSMessageRequest.put("address", address);
        ObjectNode message = mapper.createObjectNode();
        message.put("message", "This was a triumph!");
        outboundSMSMessageRequest.put("outboundSMSTextMessage", message);
        outboundSMSMessageRequest.put("clientCorrelator", "Any id");
        outboundSMSMessageRequest.put("senderName", "watch2gether");

        ((ObjectNode) rootNode).put("outboundSMSMessageRequest", outboundSMSMessageRequest);

        mapper.writeValue(sw, rootNode);

        return sw.toString();
    }

}