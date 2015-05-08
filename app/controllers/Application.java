package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.VoiceChatRoom;
import models.W2GEvent;
import models.W2GEventMsisdn;
import models.W2GEventRepository;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import services.W2GService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

@Named
@Singleton
public class Application extends Controller {

    private final W2GEventRepository w2GEventRepository;
    private final W2GService w2GService;

    // We are using constructor injection to receive a repository to support our desire for immutability.
    @Inject
    public Application(final W2GEventRepository w2GEventRepository, final W2GService w2GService) {
        this.w2GEventRepository = w2GEventRepository;
        this.w2GService = w2GService;
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
        event.msisdns.add(new W2GEventMsisdn("41760000000"));
        event.msisdns.add(new W2GEventMsisdn("41760000001"));

        final W2GEvent savedEvent = w2GEventRepository.save(event);

        final Iterable<W2GEvent> w2GEvents = w2GEventRepository.findAll();

        // Deliver the index page with a message showing the id that was generated.

        return ok(views.html.index.render(w2GEvents.iterator()));
    }

    public Result getChatRoom() throws IOException {

        try {
            VoiceChatRoom chatRoom = w2GService.getVoiceChatRoom();
            return ok("Message sent! Chatroom name: " + chatRoom.getConferenceName() + "\nChat room url: " + chatRoom.getConferenceUrl());

        } catch (Exception e) {
            return badRequest("Message couldn't be sent: " + e.getMessage());
        }
    }


    public Result sendMessage() throws IOException {
        try {
            w2GService.sendSMS(new HashSet<String>(Arrays.asList("41767202020", "41765671938")), "This is a test!");
            return ok("Message sent!");

        } catch (Exception e) {
            return badRequest("Message couldn't be sent!");
        }
    }

}