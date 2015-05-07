package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.*;
import play.libs.Json;
import play.mvc.*;

import views.html.*;

import java.util.Iterator;
import java.util.List;

public class Application extends Controller {

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

}
