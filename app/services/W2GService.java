package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.VoiceChatRoom;
import models.W2GEventRepository;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;

/**
 * Created by peyerco on 08.05.15.
 */
@Named
@Singleton
public class W2GService {

    private final W2GEventRepository w2GEventRepository;

    // We are using constructor injection to receive a repository to support our desire for immutability.
    @Inject
    public W2GService(final W2GEventRepository w2GEventRepository) {
        this.w2GEventRepository = w2GEventRepository;
    }

    public VoiceChatRoom getVoiceChatRoom() throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://voicechatapi.com/api/v1/conference/");
        CloseableHttpResponse response = client.execute(httpPost);
        String json = EntityUtils.toString(response.getEntity());
        int statusCode = response.getStatusLine().getStatusCode();
        client.close();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode voiceChatJN = mapper.readTree(json);

        VoiceChatRoom chatRoom = new VoiceChatRoom();
        chatRoom.setConferenceName(voiceChatJN.get("conference_name").asText());
        chatRoom.setConferenceUrl(voiceChatJN.get("conference_url").asText());

        return chatRoom;
    }

    public VoiceChatRoom getVoiceChatRoomHangout() throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://plus.google.com/hangouts/_/gs745udxqqrpy7jfub6e4oja4aa");
        CloseableHttpResponse response = client.execute(httpPost);
        String json = EntityUtils.toString(response.getEntity());
        int statusCode = response.getStatusLine().getStatusCode();
        client.close();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode voiceChatJN = mapper.readTree(json);

        VoiceChatRoom chatRoom = new VoiceChatRoom();
        chatRoom.setConferenceName(voiceChatJN.get("conference_name").asText());
        chatRoom.setConferenceUrl(voiceChatJN.get("conference_url").asText());

        return chatRoom;

    }

    public boolean sendSMS(Set<String> msisdns, String message) throws Exception{
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://api.swisscom.com/v1/messaging/sms/outbound/tel:+40000000000/requests");

        String json = createJsonMessage(msisdns, message);

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
            return true;
        } else {
            System.out.print("Message couldn't be sent\n\nRequest: " + json + "\nResponse: " + bodyAsString);
            return false;
        }
    }



    private static String createJsonMessage(Set<String> msisdns, String smsMsg) throws IOException {
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
        for (String msisnd: msisdns) {
            address.add("tel:+" + msisnd);
        }
        outboundSMSMessageRequest.put("address", address);
        ObjectNode message = mapper.createObjectNode();
        message.put("message", smsMsg);
        outboundSMSMessageRequest.put("outboundSMSTextMessage", message);
        outboundSMSMessageRequest.put("clientCorrelator", "Any id");
        outboundSMSMessageRequest.put("senderName", "watch2gether");

        ((ObjectNode) rootNode).put("outboundSMSMessageRequest", outboundSMSMessageRequest);

        mapper.writeValue(sw, rootNode);

        return sw.toString();
    }
}
