package actors;

import akka.actor.UntypedActor;
import models.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.springframework.context.annotation.Scope;
import services.W2GService;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by peyerco on 07.05.15.
 */
@Named
@Singleton
@Scope("prototype")
public class EventManager extends UntypedActor {

    private final W2GEventRepository w2GEventRepository;
    private final W2GService w2GService;

    // We are using constructor injection to receive a repository to support our desire for immutability.
    @Inject
    public EventManager(final W2GEventRepository w2GEventRepository, final W2GService w2GService) {
        this.w2GEventRepository = w2GEventRepository;
        this.w2GService = w2GService;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        Calendar from = Calendar.getInstance();
        from.add(Calendar.MINUTE, -180);
        Calendar to = Calendar.getInstance();
        to.add(Calendar.MINUTE, 5);
        Iterable<W2GEvent> w2GEvents = w2GEventRepository.findByTimeBetweenAndDeliveryStatus(from.getTime(), to.getTime(), DeliveryStatus.PENDING);

        if (w2GEvents != null && w2GEvents.iterator().hasNext()) {
            for (Iterator<W2GEvent> eventIter = w2GEvents.iterator(); eventIter.hasNext(); ) {
                W2GEvent event = eventIter.next();
                System.out.println("Send invitation for show: " + event.show);

                //VoiceChatRoom chatRoom = w2GService.getVoiceChatRoom();
                //System.out.println("Got chat room: " + chatRoom.conferenceUrl);

                SimpleDateFormat date = new SimpleDateFormat("dd. MMM yyyy", Locale.GERMAN);
                SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss", Locale.GERMAN);

                Calendar now = Calendar.getInstance();
                Calendar eventTime = Calendar.getInstance();
                eventTime.setTime(event.time);
                String dateString = date.format(event.time) + " " + time.format(event.time);
                if (isSameDay(now, eventTime)) {
                    dateString = "heute um " + time.format(event.time);
                }

                System.out.println("..sending to: " + getMsisdns(event.msisdns));
                w2GService.sendSMS(getMsisdns(event.msisdns), "Watch2Gether!\n"
                        + "Sendung '" + event.show
                        + "' auf '" + event.channel
                        + "' " + dateString + "\n" + event.chatUrl);

                event.deliveryStatus = DeliveryStatus.DELIVERED;

                w2GEventRepository.save(event);
            }
        } else {
            Calendar current = Calendar.getInstance();
            System.out.println("I'm bored!! " + current.getTime());
        }
    }

    private Set<String> getMsisdns(Set<W2GEventMsisdn> msisdns) {
        Set<String> setElements = new HashSet<>();
        for (W2GEventMsisdn msisdn: msisdns) {
            setElements.add(msisdn.msisdn);
        }
        return  setElements;
    }

    public boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));

    }
}
