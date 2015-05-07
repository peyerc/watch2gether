package controllers;

import models.W2GEvent;
import models.W2GEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.mvc.*;

import views.html.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
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


    public static Result index() {
        return ok(index.render("Your new application is ready."));
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
    public static Result saveEvent() {
        JsonNode json = request().body().asJson();
        JsonNode channelN = json.findValue("channel");
        JsonNode timeN = json.findValue("time");

        JsonNode showN = json.findValue("show");
        if(channelN == null || timeN == null || showN == null) {
            badRequest("A channel, time or show must be provided");
        }

        String channel = channelN.asText();
        Long time = timeN.asLong();
        String show = showN.asText();

        JsonNode numbersN = json.findValue("numbers");
        if(numbersN != null) {
            Iterator<JsonNode> elements = numbersN.elements();
            while (elements.hasNext()) {
                JsonNode next = elements.next();
                int hdyNr = next.asInt();

            }
        }

        // TODO: Now Save does data to database


        ObjectNode result = Json.newObject();
        result.put("status", "ok");
        result.put("show", show);
        return ok(result);
    }

    public Result dbTest() {

        final W2GEvent event = new W2GEvent();
        event.channel = "SRF 1";
        event.show = "10vor10";
        event.time = new Date();
        //event.msisdns.add("+41767202020");

        final W2GEvent savedEvent = w2GEventRepository.save(event);

        final W2GEvent retrievedEvent = w2GEventRepository.findOne(savedEvent.id);

        // Deliver the index page with a message showing the id that was generated.

        return ok(views.html.index.render("Found id: " + retrievedEvent.id));
    }


}
