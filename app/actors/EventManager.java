package actors;

import akka.actor.UntypedActor;
import models.DeliveryStatus;
import models.W2GEvent;
import models.W2GEventMsisdn;
import models.W2GEventRepository;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.springframework.context.annotation.Scope;
import java.util.Calendar;
import java.util.Iterator;

/**
 * Created by peyerco on 07.05.15.
 */
@Named
@Singleton
@Scope("prototype")
public class EventManager extends UntypedActor {

    private final W2GEventRepository w2GEventRepository;

    // We are using constructor injection to receive a repository to support our desire for immutability.
    @Inject
    public EventManager(final W2GEventRepository w2GEventRepository) {
        this.w2GEventRepository = w2GEventRepository;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        Calendar from = Calendar.getInstance();
        from.add(Calendar.SECOND, -10);
        Calendar to = Calendar.getInstance();
        to.add(Calendar.SECOND, 10);
        Iterable<W2GEvent> w2GEvents = w2GEventRepository.findByTimeBetweenAndDeliveryStatus(from.getTime(), to.getTime(), DeliveryStatus.PENDING);

        if (w2GEvents != null && w2GEvents.iterator().hasNext()) {
            for (Iterator<W2GEvent> eventIter = w2GEvents.iterator(); eventIter.hasNext(); ) {
                W2GEvent event = eventIter.next();
                System.out.println("Send invitation for show: " + event.show);

                for (W2GEventMsisdn msisdn: event.msisdns) {
                    System.out.println("..sending to: " + msisdn.msisdn);
                }

                event.deliveryStatus = DeliveryStatus.DELIVERED;

                w2GEventRepository.save(event);
            }
        } else {
            Calendar current = Calendar.getInstance();
            System.out.println("I'm bored!! " + current.getTime());
        }


    }
}
